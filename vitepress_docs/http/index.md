---
api:
  title: HTTP 管理接口
  description: '供 Web 面板和自动化脚本使用的内置 HTTP API。整个功能默认关闭；启用后，25 个路由默认全部开放。'
  requestType: query 参数（GET / POST 均可）
  responseType: application/json; charset=utf-8
  badges:
    - { text: 需要 Bearer Token, type: info }
    - { text: 功能默认关闭, type: warn }
    - { text: 25 个路由, type: ok }
  warnings:
    - title: 安全边界
      text: 'HTTP 接口以控制台身份运行，能生成假人、执行命令、重载配置并批量移除假人。默认 host 是 0.0.0.0，虽然功能默认关闭，但一旦启用就会监听所有网卡。请改为 127.0.0.1 或内网地址，并配合反向代理 TLS。'
      type: danger
    - title: 参数位置
      text: 'POST 与 GET 都只从 URL query 读取参数，不解析请求体。token 只认 Authorization: Bearer <token> 请求头，?token= 无效。'
      type: info
  endpoints:
    - title: GET /list
      method: GET
      path: /list
      summary: 列出所有在线假人。HTTP 层没有玩家身份概念，不受游戏内可见范围限制。
      responses:
        - status: 200
          title: 请求成功
          code: |
            {
              "fakeplayer": ["Bot_1", "Bot_2"]
            }
          fields:
            - { name: fakeplayer, type: 'string[]', desc: 在线假人名称列表 }
      examples:
        curl: |
          curl -H "Authorization: Bearer $TOKEN" "$API/list"
        powershell: |
          Invoke-RestMethod -Uri "$API/list" -Headers @{ Authorization = "Bearer $TOKEN" }
    - title: GET /status
      method: GET
      path: /status
      summary: 查看假人详细状态；name 省略时返回全部假人。
      params:
        - { name: name, type: string, required: false, desc: 假人名称，空白视为未指定 }
      responses:
        - status: 200
          title: 请求成功
          code: |
            {
              "fakeplayer": [
                {
                  "name": "Bot_1",
                  "uuid": "...",
                  "world": "world",
                  "x": 0.0,
                  "y": 64.0,
                  "z": 0.0,
                  "dead": false,
                  "alive": true,
                  "health": 20.0,
                  "max-health": 20.0,
                  "creator": "Steve",
                  "actions": []
                }
              ]
            }
          fields:
            - { name: fakeplayer, type: 'object[]', desc: 状态对象数组，成功响应不含 status 字段 }
            - { name: name, type: string, desc: 假人名称 }
            - { name: world, type: string, desc: 所在世界 }
            - { name: dead / alive, type: boolean, desc: 死亡与有效状态 }
            - { name: health / max-health, type: number, desc: 当前与最大生命 }
            - { name: food-level / saturation, type: number, desc: 饥饿与饱和度 }
            - { name: level / exp, type: number, desc: 等级与经验 }
            - { name: game-mode, type: string, desc: 游戏模式 }
            - { name: creator, type: string, desc: 创建者名称 }
            - { name: main-hand / off-hand, type: string, desc: 主手与副手物品类型 }
            - { name: hotbar-slot, type: number, desc: 当前快捷栏格子，1–9 }
            - { name: spawned-at, type: number, desc: 生成 tick }
            - { name: actions, type: 'string[]', desc: 进行中的动作枚举名 }
        - status: 400
          title: 假人不存在
          code: |
            { "status": "failure", "msg": "The dummy does not exist." }
    - title: GET /info
      method: GET
      path: /info
      summary: 返回插件、服务端与数量限制信息。成功响应不含 status 字段。
      responses:
        - status: 200
          title: 请求成功
          code: |
            {
              "plugin": "fp.build23",
              "minecraft": "1.21.1",
              "server": "Paper",
              "server-version": "git-Paper-...",
              "fakeplayer": 2,
              "online-players": 1,
              "server-limit": 1000,
              "player-limit": 1,
              "forced-execution": false
            }
    - title: GET /spawn
      method: GET
      path: /spawn
      summary: 以控制台身份在第一个世界出生点生成假人，创建者记为控制台。
      params:
        - { name: name, type: string, required: true, desc: 假人名称，空白会返回 400 }
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success" }
        - status: 400
          title: 名称冲突或假人已在线
          code: |
            { "status": "failure", "msg": "The dummy is already online." }
        - status: 504
          title: 生成超时
          code: |
            { "status": "failure", "msg": "Spawn is still in progress, check /list later" }
      examples:
        curl: |
          curl -H "Authorization: Bearer $TOKEN" "$API/spawn?name=Bot_1"
    - title: GET /kick
      method: GET
      path: /kick
      summary: 安全移除假人，不会触发真实死亡。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
        - { status: 400, title: 假人不存在, code: '{ "status": "failure", "msg": "Dummies do not exist." }' }
    - title: GET /kill
      method: GET
      path: /kill
      summary: 让假人真实死亡，可能掉落物品。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
        - { status: 400, title: 假人不存在或已死亡, code: '{ "status": "failure", "msg": "The dummy is already dead." }' }
    - title: GET /respawn
      method: GET
      path: /respawn
      summary: 复活已死亡假人；默认 kick-on-dead 为 true 时通常不可用。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
        - { status: 400, title: 假人未死亡或不存在, code: '{ "status": "failure", "msg": "The dummy is not dead." }' }
    - title: GET /say
      method: GET
      path: /say
      summary: 设置一次性 SAY 动作。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
        - { name: message, type: string, required: true, desc: 消息内容，需 URL 编码 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /aichat
      method: GET
      path: /aichat
      summary: 让假人生成一句 AI 回复；continuous 会先停旧任务并立即生成一句。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
        - { name: mode, type: enum, required: false, default: public, desc: public 或 private }
        - { name: target, type: string, required: mode=private, desc: 在线玩家名 }
        - { name: continuous, type: boolean, required: false, default: 'false', desc: 仅字符串 true 视为开启 }
        - { name: interval, type: number, required: false, desc: tick；未给时取 ai-chat.yml，显式值最小 1 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
        - status: 403
          title: AI 未启用或配置不完整
          code: |
            { "status": "failure", "msg": "AI chat is disabled, enable it in ai-chat.yml" }
      examples:
        curl: |
          curl -H "Authorization: Bearer $TOKEN" "$API/aichat?name=Bot_1&mode=public"
    - title: GET /aichat/stop
      method: GET
      path: /aichat/stop
      summary: 停止假人的 AI 持续发言任务；不检查 AI 配置，无任务时 stopped 为 false。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success", "stopped": true }
    - title: GET /action
      method: GET
      path: /action
      summary: 设置假人动作。可用动作共 10 种：ATTACK、MINE、USE、JUMP、LOOK_AT_NEAREST_ENTITY、DROP_ITEM、DROP_STACK、DROP_INVENTORY、SAY、GOTO。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
        - { name: action, type: enum, required: true, desc: 动作名，大小写不敏感，- 等价 _ }
        - { name: count, type: number, required: false, desc: -1 持续，>=0 指定次数 }
        - { name: interval, type: number, required: false, default: 0, desc: 间隔 tick；正值且未给 count 时视为持续 }
        - { name: wait, type: number, required: false, default: 0, desc: 首次执行前等待 tick }
        - { name: message, type: string, required: SAY 时必填, desc: 发言内容 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
        - status: 400
          title: 参数非法
          code: |
            { "status": "failure", "msg": "Invalid number: interval" }
      examples:
        curl: |
          curl -H "Authorization: Bearer $TOKEN" "$API/action?name=Bot_1&action=attack&interval=10"
    - title: GET /stop
      method: GET
      path: /stop
      summary: 停止假人当前所有动作。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /teleport
      method: GET
      path: /teleport
      summary: 直接传送假人，无距离、高度、边界校验，与 /goto 不同。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
        - { name: x / y / z, type: number, required: false, desc: 缺省保持当前坐标 }
        - { name: yaw / pitch, type: number, required: false, desc: 缺省保持当前朝向 }
        - { name: world, type: string, required: false, desc: 不存在时返回 400 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /look
      method: GET
      path: /look
      summary: direction、at、yaw/pitch 至少提供一项；优先级为 direction > at > yaw/pitch。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
        - { name: direction, type: enum, required: false, desc: NORTH / SOUTH / EAST / WEST / UP / DOWN }
        - { name: at, type: string, required: false, desc: x,y,z 三个有限数字 }
        - { name: yaw / pitch, type: number, required: false, desc: 精确角度 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
        - { status: 400, title: 未提供朝向参数, code: '{ "status": "failure", "msg": "One of direction, at, yaw or pitch is required" }' }
    - title: GET /hold
      method: GET
      path: /hold
      summary: 设置手持快捷栏格子。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
        - { name: slot, type: number, required: true, desc: 1–9 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
        - { status: 400, title: 格子非法, code: '{ "status": "failure", "msg": "Slot must be between 1 and 9" }' }
    - title: GET /swap
      method: GET
      path: /swap
      summary: 交换主手与副手物品。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /goto
      method: GET
      path: /goto
      summary: 异步寻路；返回 200 只表示任务已设置，不代表已到达。最大距离 1024 格且不能跨世界。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
        - { name: x / y / z, type: number, required: true, desc: 三个坐标都必须提供 }
        - { name: world, type: string, required: false, desc: 必须与假人当前世界相同 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
        - status: 400
          title: 目标不可达
          code: |
            { "status": "failure", "msg": "This location is unreachable (no safe footing nearby)" }
    - title: GET /setping
      method: GET
      path: /setping
      summary: 设置 Tab 列表显示的延迟。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
        - { name: ping, type: number, required: true, desc: 整数且 >=0 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
        - { status: 400, title: 延迟非法, code: '{ "status": "failure", "msg": "ping must be greater than or equal to 0" }' }
    - title: GET /sneak
      method: GET
      path: /sneak
      summary: state 省略时切换蹲下状态。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
        - { name: state, type: boolean, required: false, desc: true / false，省略则切换 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /sprint
      method: GET
      path: /sprint
      summary: state 省略时切换疾跑状态。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
        - { name: state, type: boolean, required: false, desc: true / false，省略则切换 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /reload
      method: GET
      path: /reload
      summary: 重载主配置、延迟显示与翻译；不重建 HTTP 监听器，也不重载 AI 配置。
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success", "msg": "Config reloaded, but config.yml is out of date" }
    - title: GET /cmd
      method: GET
      path: /cmd
      summary: 以假人身份执行控制台命令，权限等同该假人。不要把假人设为 OP。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
        - { name: command, type: string, required: true, desc: 控制台命令，需 URL 编码 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
      examples:
        curl: |
          curl -G -H "Authorization: Bearer $TOKEN" "$API/cmd" \
            --data-urlencode "name=Bot_1" --data-urlencode "command=say hello"
    - title: GET /kickall
      method: GET
      path: /kickall
      summary: 移除全部假人，受 interface.batch 开关控制。
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success", "count": 5 }
    - title: GET /killall
      method: GET
      path: /killall
      summary: 杀死全部假人，可能掉落物品，受 interface.batch 开关控制。
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success", "count": 5 }
    - title: GET /sayall
      method: GET
      path: /sayall
      summary: 让全部假人发送同一条消息，受 interface.batch 开关控制。
      params:
        - { name: message, type: string, required: true, desc: 消息内容，需 URL 编码 }
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success", "count": 5 }
  errorCodes:
    - { status: 200, desc: 请求成功 }
    - { status: 204, desc: CORS 预检成功，无响应体 }
    - { status: 400, desc: 参数缺失、非法或目标不符合前置条件 }
    - { status: 401, desc: Authorization 头缺失或 token 错误 }
    - { status: 403, desc: Host / Origin 不允许，或接口已被配置关闭 }
    - { status: 404, desc: 路由不存在 }
    - { status: 405, desc: HTTP 方法不允许 }
    - { status: 414, desc: 请求 URI 超过 4096 字符 }
    - { status: 429, desc: 触发限流或因鉴权失败被锁定 }
    - { status: 500, desc: 服务端内部错误或等待实体线程超时 }
    - { status: 504, desc: 生成假人等待超时，后台任务仍可能继续 }
  notes:
    - '请求管线顺序：路由 404 → URI 长度 414 → 限流/锁定 429 → Host/Origin/CORS 403 → 方法 405 → 鉴权 401 → 接口开关 403。方法错误优先于鉴权，鉴权优先于接口开关。'
    - POST 与 GET 都只解析 URL query；参数名大小写敏感，UTF-8 解码。
    - '令牌比较使用 MessageDigest.isEqual 常量时间比较；日志中名为 token 的 query 值会被打码。'
    - 每个 IP 每分钟默认 120 次请求；连续 10 次鉴权失败会锁定 60 秒。
    - /aichat/stop 与 /aichat 共用 interface.aichat 开关；batch 开关同时控制三个批量接口。
    - '/list、/status、/info 的成功响应不含 status 字段，这与大多数接口不同。'
  faq:
    - q: 为什么带 ?token= 还是 401？
      a: '只接受 Authorization: Bearer <token> 请求头。'
    - q: 为什么启用了 HTTP 却连不上？
      a: '检查 config.yml 的 http-admin.enabled 是否为 true、host/port 是否正确，以及是否需要重启。改 host/port/token 必须重启。'
    - q: 为什么 403 的文案不一样？
      a: 'Host not allowed、Origin not allowed、Interface disabled 分别对应白名单、CORS 和接口开关，不是同一种错误。'
    - q: 为什么 POST 带 JSON body 不生效？
      a: '实现不读取请求体，所有参数必须写在 URL query 中。'
  related:
    - { text: 配置完全手册 - HTTP 小节, link: /guide/config/ }
    - { text: 命令完全手册, link: /commands/ }
---

<ApiDocPage />
