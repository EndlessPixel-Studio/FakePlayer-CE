---
api:
  title: 动作与移动接口
  description: '驱动假人发言、执行动作、移动与调整状态。所有响应都遵循通用格式 {"status":"success"} 或 {"status":"failure","msg":"..."}。'
  badges:
    - { text: 需要鉴权, type: info }
    - { text: GET / POST, type: info }
  endpoints:
    - title: GET /say
      method: GET
      path: /say
      params:
        - { name: name, required: true, desc: 假人名称 }
        - { name: message, required: true, desc: 消息内容，需 URL 编码 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
      examples:
        curl: |
          curl -G -H "Authorization: Bearer $TOKEN" "$API/say" \
            --data-urlencode "name=Bot_1" --data-urlencode "message=你好 世界"
        powershell: |
          Invoke-RestMethod -Uri "$API/say?name=Bot_1&message=$([uri]::EscapeDataString('你好 世界'))" -Headers @{ Authorization = "Bearer $TOKEN" }
    - title: GET /aichat
      method: GET
      path: /aichat
      params:
        - { name: name, required: false, desc: 假人名称 }
        - { name: mode, required: false, desc: public 或 private }
        - { name: target, required: mode=private, desc: 私聊目标玩家 }
        - { name: continuous, required: false, desc: true 开启持续发言 }
        - { name: interval, required: false, desc: 间隔 tick }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /action
      method: GET
      path: /action
      params:
        - { name: name, required: false, desc: 假人名称 }
        - { name: action, required: true, desc: 动作名，大小写、-、_ 不敏感 }
        - { name: count, required: false, desc: 次数，默认 1；-1 表示持续 }
        - { name: interval, required: false, desc: 间隔 tick，默认 0 }
        - { name: wait, required: false, desc: 首次执行前等待 tick，默认 0 }
        - { name: message, required: SAY 时必填, desc: 发言内容 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
      examples:
        curl: |
          curl -G -H "Authorization: Bearer $TOKEN" "$API/action" \
            --data-urlencode "name=Bot_1" --data-urlencode "action=attack" --data-urlencode "interval=10"
        powershell: |
          Invoke-RestMethod -Uri "$API/action?name=Bot_1&action=attack&interval=10" -Headers @{ Authorization = "Bearer $TOKEN" }
    - title: GET /stop
      method: GET
      path: /stop
      summary: 停止假人当前所有动作。
      params:
        - { name: name, required: false, desc: 假人名称 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /teleport
      method: GET
      path: /teleport
      params:
        - { name: name, required: false, desc: 假人名称 }
        - { name: x / y / z, required: false, desc: 未给出的坐标保持当前值 }
        - { name: yaw / pitch, required: false, desc: 朝向 }
        - { name: world, required: false, desc: 世界名，不存在时报错 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /look
      method: GET
      path: /look
      summary: 四种朝向方式至少提供其一：direction、at、yaw/pitch。
      params:
        - { name: name, required: false, desc: 假人名称 }
        - { name: direction, required: 四选一, desc: NORTH / SOUTH / EAST / WEST / UP / DOWN }
        - { name: at, required: 四选一, desc: x,y,z 坐标 }
        - { name: yaw / pitch, required: 四选一, desc: 精确角度 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /hold
      method: GET
      path: /hold
      params:
        - { name: name, required: false, desc: 假人名称 }
        - { name: slot, required: true, desc: 快捷栏格子 1–9 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /swap
      method: GET
      path: /swap
      summary: 交换主手与副手物品。
      params:
        - { name: name, required: false, desc: 假人名称 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /goto
      method: GET
      path: /goto
      summary: 引导假人走向目标坐标，距离上限 1024 格，且必须在当前世界。
      params:
        - { name: name, required: false, desc: 假人名称 }
        - { name: x / y / z, required: true, desc: 目标坐标 }
        - { name: world, required: false, desc: 必须是假人当前世界 }
      responses:
        - status: 400
          title: 目标不可达
          code: |
            { "status": "failure", "msg": "Target out of range or unreachable" }
    - title: GET /setping
      method: GET
      path: /setping
      params:
        - { name: name, required: false, desc: 假人名称 }
        - { name: ping, required: true, desc: 延迟值，≥0 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /sneak、/sprint
      method: GET
      path: '/sneak | /sprint'
      params:
        - { name: name, required: false, desc: 假人名称 }
        - { name: state, required: false, desc: true / false，省略则切换 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
  faq:
    - q: /action 的 count、interval 怎么配合？
      a: 'count: -1 表示持续；不写 count 但写了 interval 也视为持续。两者都没写时执行一次。'
  related:
    - { text: 生命周期接口, link: /http/endpoints-lifecycle }
    - { text: 高危接口, link: /http/endpoints-admin }
---

<ApiDocPage />
