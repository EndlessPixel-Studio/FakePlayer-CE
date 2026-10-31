#!/usr/bin/env python3
"""与 Hangar (hangar.papermc.io) 同步。

两种模式:
  version --tag fp.build16   为指定 tag 创建 Hangar 版本 (外链指向 GitHub Release 资产)
  page                       把 README.md 同步为项目主页正文

鉴权是两步: POST /authenticate 用 API key 换 JWT, 之后所有请求用
`Authorization: HangarAuth <jwt>` (不是 Bearer)。
"""

import argparse
import json
import os
import re
import subprocess
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

BASE = "https://hangar.papermc.io/api/v1"
PROJECT = os.environ.get("HANGAR_PROJECT", "FakePlayer-CE")
REPO = os.environ.get("GITHUB_REPOSITORY", "EndlessPixel-Studio/FakePlayer-CE")
TOKEN = os.environ.get("HANGAR_API_TOKEN", "")
COMMANDAPI_PROJECT_ID = 491
USER_AGENT = f"FakePlayer-CE-CI (+https://github.com/{REPO})"


def log(msg):
    print(msg, flush=True)


def http(url, method="GET", body=None, content_type=None, jwt=None, timeout=60):
    headers = {"User-Agent": USER_AGENT, "Accept": "application/json"}
    if content_type:
        headers["Content-Type"] = content_type
    if jwt:
        headers["Authorization"] = f"HangarAuth {jwt}"
    req = urllib.request.Request(url, data=body, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.status, resp.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", "replace")


def authenticate():
    if not TOKEN:
        sys.exit("缺少 HANGAR_API_TOKEN")
    url = f"{BASE}/authenticate?" + urllib.parse.urlencode({"apiKey": TOKEN})
    for attempt in range(3):
        status, text = http(url, method="POST")
        if status == 200:
            return json.loads(text)["token"]
        if status in (429, 500, 502, 503):
            time.sleep(5 * (attempt + 1))
            continue
        sys.exit(f"鉴权失败 HTTP {status}: {text[:300]}")
    sys.exit("鉴权连续失败")


def platform_versions_from_tag(tag):
    """从 tag 上各 NMSBridge 声明的 SUPPORTS 读出支持的 MC 版本。

    不能用目录名推断: 一个模块可以覆盖多个版本, 例如 v1_21_3 同时声明
    "1.21.2" 和 "1.21.3", v1_20_1 同时声明 "1.20" 和 "1.20.1"。
    """
    out = subprocess.run(
        ["git", "grep", "-h", "SUPPORTS = Set.of", tag, "--", "versions/"],
        capture_output=True, text=True,
    )
    if out.returncode != 0:
        sys.exit(f"读取 {tag} 的版本声明失败: {out.stderr.strip()}")
    versions = sorted({v for v in re.findall(r'"(\d[\d.]*)"', out.stdout)})
    if not versions:
        sys.exit(f"{tag} 里没有找到任何 SUPPORTS 声明")

    # 只保留 Hangar 平台表里认得的版本, 免得整个上传因为一个未来版本被拒
    status, text = http(f"{BASE}/platforms/PAPER/versions")
    if status == 200:
        known = set()
        for entry in json.loads(text):
            known.add(entry["version"])
            known.update(entry.get("subVersions") or [])
        unknown = [v for v in versions if v not in known]
        if unknown:
            log(f"跳过 Hangar 尚未收录的版本: {', '.join(sorted(unknown))}")
        versions = [v for v in versions if v in known]
    return sorted(versions, key=lambda v: [int(x) for x in v.split(".")])


def asset_url(tag):
    # tag 是 fp.build16, 资产名是 fakeplayer-fp.build16.jar (版本号本身不带 fp. 前缀)
    jar_version = tag[3:] if tag.startswith("fp.") else tag
    return f"https://github.com/{REPO}/releases/download/{tag}/fakeplayer-fp.{jar_version}.jar"


def wait_for_asset(tag, timeout=180):
    """Release 刚发布时资产可能还没挂上, 轮询到能拿到为止。"""
    url = asset_url(tag)
    deadline = time.time() + timeout
    while time.time() < deadline:
        status, _ = http(url, method="HEAD", timeout=30)
        if status == 200:
            return url
        log(f"资产还不可用 (HTTP {status}), 5 秒后重试: {url}")
        time.sleep(5)
    sys.exit(f"资产 3 分钟内仍不可用, 放弃: {url}")


def publish_version(tag):
    versions = platform_versions_from_tag(tag)
    log(f"{tag} 支持版本 ({len(versions)}): {', '.join(versions)}")

    url = wait_for_asset(tag)
    payload = {
        "version": tag,
        "channel": "Release",
        "description": f"https://github.com/{REPO}/releases/tag/{tag}",
        "platformDependencies": {"PAPER": versions},
        "pluginDependencies": {
            "PAPER": [
                {
                    "name": "CommandAPI",
                    "projectId": COMMANDAPI_PROJECT_ID,
                    "required": True,
                    "platform": "PAPER",
                }
            ]
        },
        "files": [{"platforms": ["PAPER"], "externalUrl": url}],
    }

    boundary = "----FakePlayerCEHangar"
    body = (
        f"--{boundary}\r\n"
        'Content-Disposition: form-data; name="versionUpload"\r\n'
        "Content-Type: application/json\r\n\r\n"
        + json.dumps(payload)
        + f"\r\n--{boundary}--\r\n"
    ).encode()
    status, text = http(
        f"{BASE}/projects/{PROJECT}/upload",
        method="POST",
        body=body,
        content_type=f"multipart/form-data; boundary={boundary}",
        jwt=authenticate(),
    )
    if status in (200, 201):
        log(f"已创建 {tag}: https://hangar.papermc.io/{PROJECT}/versions/{tag}")
        return
    if status in (400, 409) and ("exist" in text.lower() or "duplicate" in text.lower()):
        log(f"{tag} 已存在, 跳过")
        return
    sys.exit(f"创建 {tag} 失败 HTTP {status}: {text[:400]}")


def read_readme(path="README.md"):
    with open(path, encoding="utf-8") as f:
        return f.read()


def to_absolute(markdown):
    """相对链接改写成 GitHub 绝对地址, 并去掉只属于 GitHub 首页的装饰。"""
    blob = f"https://github.com/{REPO}/blob/master"

    markdown = markdown.replace('<div align="center">', "").replace("</div>", "")
    # 主页上的大 logo 在 Hangar 页面里显得突兀, 直接去掉
    markdown = re.sub(r'<img src="[^"]*" alt="[^"]*"[^>]*>\n*', "", markdown)
    markdown = re.sub(
        r"\]\((?!https?://|#|mailto:)([^)\s]+)\)",
        lambda m: f"]({path_url(m.group(1), blob)})",
        markdown,
    )
    return markdown.strip() + "\n"


def path_url(path, base):
    return f"{base}/{path[2:] if path.startswith('./') else path.lstrip('/')}"


def sync_page():
    content = to_absolute(read_readme())
    log(f"README 长度: {len(content)} 字符 (Hangar 上限 40000)")
    status, text = http(
        f"{BASE}/pages/editmain/{PROJECT}",
        method="PATCH",
        body=json.dumps({"path": "home", "content": content}).encode(),
        content_type="application/json",
        jwt=authenticate(),
    )
    if status not in (200, 204):
        sys.exit(f"同步主页失败 HTTP {status}: {text[:400]}")
    log(f"已同步主页: https://hangar.papermc.io/{PROJECT}/")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    sub = parser.add_subparsers(dest="mode", required=True)
    v = sub.add_parser("version")
    v.add_argument("--tag", required=True)
    sub.add_parser("page")
    args = parser.parse_args()

    if args.mode == "version":
        publish_version(args.tag)
    else:
        sync_page()
