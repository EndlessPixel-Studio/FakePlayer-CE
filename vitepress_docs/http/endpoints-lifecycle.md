---
api:
  title: 生命周期接口
  description: 生成、移除、复活与停止 AI 发言。
  badges:
    - { text: 需要鉴权, type: info }
    - { text: 会改变状态, type: warn }
  endpoints:
    - title: GET /spawn
      method: GET
      path: /spawn
      params:
        - { name: name, required: true, desc: 假人名称，需符合命名规则 }
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success", "name": "Bot_1" }
        - status: 400
          title: 超出上限或名称非法
          code: |
            { "status": "failure", "msg": "Spawn limit reached" }
      examples:
        curl: |
          curl -H "Authorization: Bearer $TOKEN" "$API/spawn?name=Bot_1"
        powershell: |
          Invoke-RestMethod -Uri "$API/spawn?name=Bot_1" -Headers @{ Authorization = "Bearer $TOKEN" }
    - title: GET /kick
      method: GET
      path: /kick
      summary: 把假人从服务器移除。
      params:
        - { name: name, required: true, desc: 假人名称 }
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success" }
    - title: GET /kill
      method: GET
      path: /kill
      summary: 让假人真实死亡，可能掉落物品。
      params:
        - { name: name, required: true, desc: 假人名称 }
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success" }
    - title: GET /respawn
      method: GET
      path: /respawn
      summary: '复活已死亡假人；需要 kick-on-dead: false。'
      params:
        - { name: name, required: true, desc: 假人名称 }
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success" }
    - title: GET /aichat/stop
      method: GET
      path: /aichat/stop
      summary: 停止假人的 AI 持续发言。
      params:
        - { name: name, required: true, desc: 假人名称 }
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success", "stopped": true }
      examples:
        curl: |
          curl -H "Authorization: Bearer $TOKEN" "$API/aichat/stop?name=Bot_1"
        powershell: |
          Invoke-RestMethod -Uri "$API/aichat/stop?name=Bot_1" -Headers @{ Authorization = "Bearer $TOKEN" }
  faq:
    - q: /kill 与 /kick 有什么区别？
      a: /kick 直接把假人从服务端移除；/kill 让假人真实死亡，可能掉落物品。需要 /fp respawn 复活时必须关闭 kick-on-dead。
  related:
    - { text: 动作与移动接口, link: /http/endpoints-action }
    - { text: 高危接口, link: /http/endpoints-admin }
---

<ApiDocPage />
