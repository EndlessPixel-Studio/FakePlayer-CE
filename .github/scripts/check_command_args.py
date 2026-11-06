#!/usr/bin/env python3
"""校验 CommandRegistry 注册出的命令参数顺序。

CommandAPI 在注册期检查「可选参数之后不能再有必选参数」，否则抛
OptionalArgumentException，整个插件都无法启用 (build17 就是这么挂的)。
编译期发现不了，只有真的开服才炸，所以在这里模拟一遍它的规则。

规则来自 AbstractCommandAPICommand#flatten / #getArgumentsToRegister:

  * 注册子命令时会把父命令的 arguments 原地追加进 prevArguments，
    子命令路径 = 父命令参数 + 子命令字面量 + 子命令参数；
  * 路径里只要出现过可选参数，后面就不能再有必选参数 (字面量本身是必选)。

也就是说：带子命令的命令，自己的参数必须全部是必选。
"""

import re
import sys

REGISTRY = "fakeplayer-core/src/main/java/io/github/hello09x/fakeplayer/core/command/CommandRegistry.java"


def skip_string(s, i):
    i += 1
    while i < len(s):
        if s[i] == "\\":
            i += 2
            continue
        if s[i] == '"':
            return i + 1
        i += 1
    return i


def read_balanced(s, i):
    """i 指向 '('，返回 (括号内容, ')' 之后的位置)"""
    depth, start = 0, i + 1
    while i < len(s):
        c = s[i]
        if c == '"':
            i = skip_string(s, i)
            continue
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return s[start:i], i + 1
        i += 1
    raise ValueError("括号不平衡")


def split_top(s):
    parts, depth, buf, i = [], 0, "", 0
    while i < len(s):
        c = s[i]
        if c == '"':
            j = skip_string(s, i)
            buf += s[i:j]
            i = j
            continue
        if c in "([{":
            depth += 1
        elif c in ")]}":
            depth -= 1
        if c == "," and depth == 0:
            parts.append(buf)
            buf = ""
            i += 1
            continue
        buf += c
        i += 1
    if buf.strip():
        parts.append(buf)
    return parts


def arg_name(expr):
    m = re.search(r'"([^"]+)"', expr)
    return m.group(1) if m else "?"


def parse_command(s, i):
    m = re.match(r'\s*command\("([^"]+)"\)', s[i:])
    if not m:
        return None, i

    node = {"name": m.group(1), "args": [], "children": [], "executor": False}
    i += m.end()
    while True:
        m = re.match(r"\s*\.(\w+)\(", s[i:])
        if not m:
            break
        method = m.group(1)
        i += m.end() - 1
        content, i = read_balanced(s, i)
        if method == "withArguments":
            node["args"] += [(arg_name(a), False) for a in split_top(content)]
        elif method == "withOptionalArguments":
            node["args"] += [(arg_name(a), True) for a in split_top(content)]
        elif method == "withSubcommands":
            for part in split_top(content):
                child, _ = parse_command(part, 0)
                if child:
                    node["children"].append(child)
        elif method.startswith("executes"):
            node["executor"] = True
    return node, i


violations = []


def check(args, path):
    seen_optional = False
    for name, optional in args:
        if optional:
            seen_optional = True
        elif seen_optional:
            violations.append((path, name, list(args)))
            return


def flatten(prev, subcommand, path):
    prev = prev + [("literal:" + subcommand["name"], False)]
    if subcommand["executor"]:
        # CommandAPI 里 rootCommand.arguments 与 prevArguments 是同一个列表，
        # 追加子命令参数会污染 prevArguments，后面的孙命令路径也会带上父命令参数
        args = prev + subcommand["args"]
        check(args, path + [subcommand["name"]])
        prev = args
    for child in subcommand["children"]:
        flatten(prev, child, path + [subcommand["name"]])


def main():
    with open(REGISTRY, encoding="utf-8") as f:
        source = f.read()

    root, _ = parse_command(source, source.index('command("fakeplayer")'))
    check(root["args"], ["fakeplayer"])
    for subcommand in root["children"]:
        flatten([], subcommand, [])

    if not violations:
        print("命令参数顺序正常")
        return 0

    print(f"发现 {len(violations)} 处非法参数顺序（可选参数后不能跟必选参数）:")
    for path, name, args in violations:
        formatted = " ".join(("[" + a + "]" if o else a) for a, o in args)
        print(f"  /fp {' '.join(path)}: 必选 '{name}' 出现在 {formatted}")
    return 1


if __name__ == "__main__":
    sys.exit(main())
