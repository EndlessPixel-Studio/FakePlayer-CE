#!/usr/bin/env python3
"""把 README 转成 TipTap JSON 并 PATCH 到 NexusMC 资源正文。

与 sync_modrinth.py 的区别:
- Modrinth 收 Markdown 纯文本, NexusMC 的 content 必须收 TipTap JSON 对象
  (传纯 Markdown 会被当成无效正文, 报「资源正文至少需要 20 个字」)。
- NexusMC 会校验正文里的不可见/控制字符 (UNICODE_RISK_DETECTED), 需先净化。
"""
import json, os, re, sys, time, unicodedata, urllib.error, urllib.request

REPO   = os.environ.get("REPO", "EndlessPixel-Studio/FakePlayer-CE")
REF    = os.environ.get("REF", "master")
README = os.environ.get("README", "README_zh.md")
BASE   = f"https://github.com/{REPO}/blob/{REF}/"
SKIP   = ("http://", "https://", "//", "mailto:", "#", "data:")

# 平台会以 UNICODE_RISK_DETECTED 拒绝含不可见字符的正文
INVISIBLE = {0xFE0E, 0xFE0F, 0x200B, 0x200C, 0x200D, 0x2060, 0xFEFF, 0x00AD, 0x180E, 0x2028, 0x2029}


def clean(s: str, keep_ws: bool = False) -> str:
    """移除不可见字符与控制字符；代码块内容保留换行/制表。"""
    out = []
    for ch in s:
        if ord(ch) in INVISIBLE:
            continue
        if unicodedata.category(ch) in ("Cc", "Cf", "Cs", "Co", "Cn"):
            if keep_ws and ch in "\n\t":
                out.append(ch)
            continue
        out.append(ch)
    return "".join(out)


def to_absolute(url: str) -> str:
    url = url.strip()
    if not url or url.startswith(SKIP):
        return url
    path, _, frag = url.partition("#")
    path = re.sub(r'^\./+', '', path) or "."
    path = path.replace(" ", "%20")
    return BASE + path + ("#" + frag if frag else "")


INLINE_RE = re.compile(
    r'!\[([^\]]*)\]\(([^)\s]+)(?:\s+"[^"]*")?\)'      # 1,2 image
    r'|\[([^\]]*)\]\(([^)\s]+)(?:\s+"[^"]*")?\)'      # 3,4 link
    r'|`([^`]+)`'                                      # 5 inline code
    r'|\*\*([^*]+?)\*\*'                               # 6 bold
    r'|(?<![\w*])\*([^*\n]+?)\*(?![\w*])'              # 7 italic
    r'|~~([^~]+)~~'                                    # 8 strike
)
# [![alt](img)](href) -> [alt](href): 徽章先规整成普通链接, 否则会把 svg 当链接目标
BADGE = re.compile(r'\[!\[([^\]]*)\]\([^)\s]+\)\]\(([^)\s]+)\)')
PURE_BADGE = re.compile(r'^\[!\[[^\]]*\]\([^)\s]+\)\]\([^)\s]+\)$')


def txt(s, marks=None):
    node = {"type": "text", "text": clean(s)}
    if marks:
        node["marks"] = marks
    return node


def inline(text):
    """把一段 Markdown 行内文本转成 TipTap 行内节点。"""
    text = BADGE.sub(lambda m: f"[{m.group(1)}]({m.group(2)})", text)
    out, pos = [], 0
    for m in INLINE_RE.finditer(text):
        if m.start() > pos:
            out.append(txt(text[pos:m.start()]))
        if m.group(2):                                    # image
            out.append({"type": "image", "attrs": {
                "src": to_absolute(m.group(2)), "alt": clean(m.group(1) or ""), "title": None}})
        elif m.group(4):                                  # link
            inner, href = m.group(3), to_absolute(m.group(4))
            nested = re.match(r'^!\[([^\]]*)\]\(', inner or "")
            if nested:                                    # [![alt](img)](href) -> 文字链接
                out.append(txt(nested.group(1) or inner,
                               [{"type": "link", "attrs": {"href": href, "target": "_blank"}}]))
            else:
                for sub in inline(inner or ""):
                    if sub["type"] == "text":
                        sub.setdefault("marks", []).append(
                            {"type": "link", "attrs": {"href": href, "target": "_blank"}})
                    out.append(sub)
        elif m.group(5):                                  # inline code
            out.append(txt(m.group(5), [{"type": "code"}]))
        elif m.group(6):                                  # bold
            for sub in inline(m.group(6)):
                if sub["type"] == "text":
                    sub.setdefault("marks", []).append({"type": "bold"})
                out.append(sub)
        elif m.group(7):                                  # italic
            for sub in inline(m.group(7)):
                if sub["type"] == "text":
                    sub.setdefault("marks", []).append({"type": "italic"})
                out.append(sub)
        elif m.group(8):                                  # strike
            for sub in inline(m.group(8)):
                if sub["type"] == "text":
                    sub.setdefault("marks", []).append({"type": "strike"})
                out.append(sub)
        pos = m.end()
    if pos < len(text):
        out.append(txt(text[pos:]))
    return [n for n in out if n.get("text") or n["type"] != "text"]


def para(text):
    nodes = inline(text)
    return {"type": "paragraph", "attrs": {"textAlign": None}, "content": nodes} if nodes else None


def cell(text):
    p = para(text)
    return {"type": "tableCell", "attrs": {"colspan": 1, "rowspan": 1}, "content": [p] if p else []}


def split_row(line):
    return [c.strip() for c in line.strip().strip("|").split("|")]


def convert(md: str) -> dict:
    lines, doc, i, n = md.split("\n"), [], 0, len(md.split("\n"))
    while i < n:
        line = lines[i]
        s = line.strip()

        fm = re.match(r'^(```|~~~)\s*(\S*)', s)          # fenced code block
        if fm:
            fence, lang = fm.group(1), fm.group(2)
            i += 1
            buf = []
            while i < n and not lines[i].strip().startswith(fence):
                buf.append(lines[i])
                i += 1
            i += 1
            doc.append({"type": "codeBlock", "attrs": {"language": lang or None},
                        "content": [{"type": "text", "text": clean("\n".join(buf), keep_ws=True)}]})
            continue

        if s.startswith("|") and i + 1 < n and re.match(r'^\|[\s:\-|]+\|$', lines[i + 1].strip()):
            header = split_row(s)                        # table
            i += 2
            rows = []
            while i < n and lines[i].strip().startswith("|"):
                rows.append(split_row(lines[i]))
                i += 1
            trows = [{"type": "tableRow", "content": [cell(c) for c in header]}]
            for r in rows:
                trows.append({"type": "tableRow", "content": [cell(c) for c in r]})
            doc.append({"type": "table", "content": trows})
            continue

        hm = re.match(r'^(#{1,6})\s+(.*)$', s)           # heading
        if hm:
            doc.append({"type": "heading", "attrs": {"level": len(hm.group(1))},
                        "content": inline(hm.group(2).strip())})
            i += 1
            continue

        if re.match(r'^(-{3,}|\*{3,}|_{3,})$', s):       # horizontal rule
            doc.append({"type": "horizontalRule"})
            i += 1
            continue

        if PURE_BADGE.match(s):                          # 纯徽章行: 对资源页无意义
            i += 1
            continue

        if s.startswith(">"):                            # blockquote
            buf = []
            while i < n and lines[i].strip().startswith(">"):
                buf.append(re.sub(r'^\s*>\s?', '', lines[i]))
                i += 1
            inner = [p for p in (para(b) for b in buf if b.strip()) if p]
            doc.append({"type": "blockquote", "content": inner})
            continue

        if re.match(r'^\s*(?:[-*+]|\d+\.)\s+', line):    # list
            ordered = bool(re.match(r'^\s*\d+\.\s', line))
            items = []
            while i < n:
                m2 = re.match(r'^(\s*)(?:[-*+]|\d+\.)\s+(.*)$', lines[i])
                if not m2:
                    break
                items.append((len(m2.group(1)), m2.group(2).strip()))
                i += 1

            def build(idxs, start=0):
                out, k = [], start
                while k < len(idxs):
                    ind, body = idxs[k]
                    sub, j = [], k + 1
                    while j < len(idxs) and idxs[j][0] > ind:
                        sub.append(idxs[j])
                        j += 1
                    p = para(body)
                    content = [p] if p else []
                    if sub:
                        content += build(idxs, k + 1)[:1]
                    out.append({"type": "listItem", "content": content})
                    k = j if sub else k + 1
                return out

            doc.append({"type": "orderedList" if ordered else "bulletList",
                        "attrs": {"tight": True}, "content": build(items)})
            continue

        if not s or re.match(r'^</?\s*(div|p|br|details|summary|center|img|a|sub|sup)\b', s, re.I):
            i += 1                                       # HTML 块标签 / 空行
            continue

        buf = [s]                                        # paragraph
        i += 1
        while i < n:
            nx = lines[i].strip()
            if (not nx or re.match(r'^(#{1,6})\s', nx) or nx.startswith("|") or nx.startswith(">")
                    or nx.startswith("```") or nx.startswith("~~~")
                    or re.match(r'^\s*(?:[-*+]|\d+\.)\s', lines[i])
                    or re.match(r'^</?\s*(div|p|br|details|summary)\b', nx, re.I)):
                break
            buf.append(nx)
            i += 1
        p = para(" ".join(buf))
        if p:
            doc.append(p)
    return {"type": "doc", "content": doc}


def patch_content(resource_id: str, token: str, doc: dict, retries: int = 3, timeout: int = 60) -> None:
    payload = json.dumps({"content": doc}, ensure_ascii=False).encode()
    url = f"https://www.nexusmc.cn/api/resources/{resource_id}"
    last = None
    for i in range(1, retries + 1):
        req = urllib.request.Request(url, data=payload, method="PATCH", headers={
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json",
            "User-Agent": f"{REPO} GitHub-Actions",
        })
        try:
            with urllib.request.urlopen(req, timeout=timeout) as r:
                body = json.loads(r.read().decode(errors="replace"))
                print(f"NexusMC PATCH OK status={r.status} revision={body.get('editRevision')} "
                      f"status={body.get('status')} payload={len(payload) / 1024:.1f}KB")
                return
        except urllib.error.HTTPError as e:
            detail = e.read().decode(errors="replace")[:600]
            print(f"PATCH FAIL #{i} code={e.code} {detail}", file=sys.stderr)
            if e.code == 409 and "UNICODE_RISK_DETECTED" in detail:
                print("提示: 正文含不可见/控制字符, 需在 clean() 中补上对应码点。", file=sys.stderr)
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
    md = open(README, encoding="utf-8").read()
    doc = convert(md)

    counts = {}
    def walk(ns):
        for x in ns:
            counts[x["type"]] = counts.get(x["type"], 0) + 1
            walk(x.get("content") or [])
    walk(doc["content"])
    print(f"README {README} {len(md)} bytes -> {len(doc['content'])} top-level nodes")
    print(f"节点统计: {counts}")

    if os.environ.get("DRY_RUN"):
        payload = json.dumps({"content": doc}, ensure_ascii=False)
        print(f"[DRY_RUN] payload {len(payload.encode()) / 1024:.1f} KB, 不发送 PATCH")
        return

    token = os.environ.get("NEXUSMC_TOKEN")
    rid = os.environ.get("NEXUSMC_RESOURCE_ID")
    if not token or not rid:
        raise SystemExit("NEXUSMC_TOKEN / NEXUSMC_RESOURCE_ID missing")
    patch_content(rid, token, doc)


if __name__ == "__main__":
    main()
