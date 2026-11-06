# Security Policy

English | [简体中文](SECURITY_zh.md) | [繁體中文](SECURITY_zh_TW.md) | [日本語](SECURITY_ja.md)

FakePlayer CE spawns real server-side players and ships an optional HTTP admin API, so a
misconfiguration can hand an attacker control over your server. This document covers what is
supported, how to report a problem, and how to run the plugin safely.

## Supported versions

Only the newest published build receives security fixes. Older builds are not patched — update
to the latest `fp.buildN` from [GitHub Releases](https://github.com/EndlessPixel-Studio/FakePlayer-CE/releases)
or [Hangar](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE).

| Build | Supported |
|---|---|
| Latest published `fp.buildN` | ✅ |
| Any older build | ❌ |

## Reporting a vulnerability

**Do not open a public issue for a security problem.**

Use GitHub's private reporting: open the [Security tab](https://github.com/EndlessPixel-Studio/FakePlayer-CE/security)
→ *Report a vulnerability*. That creates a private advisory only the maintainers can see.

Please include:

- Plugin build (`/version FakePlayer-CE` or the jar file name)
- Server software and version (Paper / Purpur / Leaf / Folia, Minecraft version)
- Steps to reproduce, expected vs. actual behaviour
- Relevant config (redact your token) and log excerpts

You will get an acknowledgement as soon as possible; this is a volunteer project, so please allow
a few days. Once a fix ships, we are happy to credit you in the advisory and the changelog.

## HTTP Admin API

The HTTP API is **disabled by default** and every endpoint has an independent switch. When you
enable it, keep the following in mind.

### What the plugin already does for you

| Protection | Behaviour |
|---|---|
| Token authentication | `Authorization: Bearer <token>` only. `?token=` is deliberately **not** supported, because query strings leak into proxy logs and browser history. An empty `token` generates a random one at startup and prints it to the console. |
| Auth before routing | Authentication is evaluated before the per-endpoint switch, so `401` / `403` responses cannot be used to discover which endpoints are enabled. |
| Constant-time comparison | The token is compared with `MessageDigest.isEqual`, not `String.equals`. |
| Log hygiene | Tokens are redacted and control characters replaced before anything is logged. |
| Rate limiting | Per source IP, with a lockout after repeated authentication failures. |
| Host / Origin allowlist | `allowed-hosts` rejects requests whose `Host` or cross-origin `Origin` does not match, which mitigates DNS rebinding. |
| No caching | Responses carry `Cache-Control: no-store`. |

### What is still on you

- **There is no TLS.** The API speaks plain HTTP. Bind it to `127.0.0.1`, or put it behind a
  reverse proxy that terminates TLS, and restrict access at the firewall.
- **`/cmd` runs commands as a fake player**, with that fake player's permissions. It is the most
  dangerous endpoint — leave `http-admin.interface.cmd` disabled unless you need it, and
  **never OP a fake player**: anyone who can drive that fake player inherits those permissions.
- **Keep the token secret.** Treat it like a root password. Rotate it if it ever appears in a
  screenshot, a log paste, or a public repository.
- **Expose only what you need.** Every endpoint has a switch under `http-admin.interface`; turn
  off the ones you do not use.

### Hardening checklist

```yaml
http-admin:
  enabled: true
  host: 127.0.0.1          # loopback, or an internal address behind a reverse proxy
  token: ""                # empty generates a random token; set a long random value instead
  allowed-hosts: []        # set your public hostname when exposing through a proxy
  rate-limit:
    requests-per-minute: 120
    auth-failures: 10
    lockout-seconds: 60
  interface:
    cmd: false             # disable anything you do not actually call
```

If you expose the API beyond `localhost`, also:

- terminate TLS at the proxy and forward only from the proxy's address
- add authentication at the proxy layer (mTLS, basic auth, or an SSO gateway) on top of the token
- monitor the plugin log for repeated `401` / `429` responses

## Scope

In scope: the plugin jar (command handling, HTTP API, NMS modules under `versions/`, login-plugin
compatibility, update checker, Hangar/GitHub publishing workflows in this repository).

Out of scope: issues that require an already-compromised server, deliberately granting a fake
player OP rights, running with `forced-execution: true` on an unsupported server version, and
griefing performed through features working exactly as documented (for example `/fp cmd`).
