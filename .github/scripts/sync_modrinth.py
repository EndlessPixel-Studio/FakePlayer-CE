#!/usr/bin/env python3
"""Rewrite relative links in README.md to absolute GitHub URLs and PATCH Modrinth body."""
import json, os, re, sys, time, urllib.error, urllib.request

REPO   = os.environ.get("REPO", "EndlessPixel-Studio/FakePlayer-CE")
REF    = os.environ.get("REF", "master")
BASE   = f"https://github.com/{REPO}/blob/{REF}/"
README = os.environ.get("README", "README.md")
SKIP   = ("http://", "https://", "//", "mailto:", "#", "data:")


def to_absolute(url: str) -> str:
    url = url.strip()
    if not url or url.startswith(SKIP):
        return url
    path, _, frag = url.partition("#")
    path = re.sub(r'^\./+', '', path) or "."
    path = path.replace(" ", "%20")
    out = BASE + path
    return out + ("#" + frag if frag else "")


def rewrite(text: str) -> str:
    # Markdown inline link / image: [..](url) / ![..](url)
    text = re.sub(
        r'(?<=\]\()(?!https?://|//|mailto:|#|data:)([^)\s]+)(?=\))',
        lambda m: to_absolute(m.group(1)),
        text,
    )
    # HTML <img src> / <a href>
    text = re.sub(
        r'(<(?:img|a)\b[^>]*?\b(?:src|href)=["\'])(?!https?://|//|mailto:|#|data:)([^"\']+)(["\'])',
        lambda m: m.group(1) + to_absolute(m.group(2)) + m.group(3),
        text,
        flags=re.IGNORECASE,
    )
    return text


def patch_body(project_id: str, token: str, body: str,
               retries: int = 3, timeout: int = 30) -> None:
    payload = json.dumps({"body": body}).encode()
    url = f"https://api.modrinth.com/v2/project/{project_id}"
    last = None
    for i in range(1, retries + 1):
        req = urllib.request.Request(
            url, data=payload, method="PATCH",
            headers={
                "Authorization": token,
                "Content-Type": "application/json",
                "User-Agent": f"{REPO} GitHub-Actions",
            },
        )
        try:
            with urllib.request.urlopen(req, timeout=timeout) as r:
                print(f"Modrinth PATCH OK status={r.status} body_len={len(body)}")
                return
        except urllib.error.HTTPError as e:
            detail = e.read().decode(errors="replace")[:500]
            print(f"PATCH FAIL #{i} code={e.code} {detail}", file=sys.stderr)
            if 400 <= e.code < 500 and e.code != 429:
                raise SystemExit(1)
            last = e
        except (urllib.error.URLError, TimeoutError) as e:
            print(f"PATCH NETWORK FAIL #{i}: {e}", file=sys.stderr)
            last = e
        time.sleep(2 ** (i - 1))
    raise SystemExit(f"PATCH failed after {retries} attempts: {last}")


def main() -> None:
    if not os.path.exists(README):
        raise SystemExit(f"README not found: {README}")
    text = open(README, encoding="utf-8").read()
    new = rewrite(text)
    print(f"README {len(text)} bytes -> {len(new)} bytes")

    if os.environ.get("DRY_RUN"):
        print(new)
        return

    token = os.environ.get("MODRINTH_TOKEN")
    pid = os.environ.get("MODRINTH_PROJECT_ID")
    if not token or not pid:
        raise SystemExit("MODRINTH_TOKEN / MODRINTH_PROJECT_ID missing")

    patch_body(pid, token, new)


if __name__ == "__main__":
    main()
