---
api:
  title: 信息类接口
  description: 只读查询假人与服务器状态，不会改变服务端数据。
  badges:
    - { text: 需要鉴权, type: info }
    - { text: GET / POST, type: info }
    - { text: 默认关闭, type: warn }
  endpoints:
    - title: GET /list
      method: GET
      path: /list
      summary: 返回调用者可见的假人列表：OP 与控制台看到全服假人，普通玩家只看到自己召唤的假人。
      responses:
        - status: 200
          title: 请求成功
          code: |
            {
              "status": "success",
              "fakeplayer": ["Bot_1", "Bot_2"]
            }
        - status: 401
          title: 鉴权失败
          code: |
            { "status": "failure", "msg": "Unauthorized" }
      examples:
        curl: |
          curl -H "Authorization: Bearer $TOKEN" "$API/list"
        powershell: |
          Invoke-RestMethod -Uri "$API/list" -Headers @{ Authorization = "Bearer $TOKEN" }
    - title: GET /status
      method: GET
      path: /status
      summary: 返回坐标、朝向、血量、饥饿、经验、游戏模式、创建者、手持物品与正在进行的动作。
      params:
        - { name: name, required: false, desc: 假人名称；省略时返回全部假人状态 }
      responses:
        - status: 200
          title: 请求成功
          code: |
            {
              "status": "success",
              "fakeplayer": [
                { "name": "Bot_1", "world": "world", "x": 0, "y": 64, "z": 0 }
              ]
            }
        - status: 404
          title: 假人不存在
          code: |
            { "status": "failure", "msg": "Fake player not found" }
      examples:
        curl: |
          curl -H "Authorization: Bearer $TOKEN" "$API/status?name=Bot_1"
        powershell: |
          Invoke-RestMethod -Uri "$API/status?name=Bot_1" -Headers @{ Authorization = "Bearer $TOKEN" }
    - title: GET /info
      method: GET
      path: /info
      summary: 返回插件版本、MC 版本、服务端类型、假人数量、在线玩家数与假人数量上限。
      responses:
        - status: 200
          title: 请求成功
          code: |
            {
              "status": "success",
              "version": "fp.build23",
              "fakeplayer_count": 2,
              "player_count": 1
            }
      examples:
        curl: |
          curl -H "Authorization: Bearer $TOKEN" "$API/info"
        powershell: |
          Invoke-RestMethod -Uri "$API/info" -Headers @{ Authorization = "Bearer $TOKEN" }
  faq:
    - q: 为什么返回 401？
      a: '鉴权只认 Authorization: Bearer <token> 请求头，不支持 ?token=。请确认 http-admin.token 与请求头一致。'
    - q: 为什么普通玩家看不到别人的假人？
      a: 信息类接口沿用游戏内的可见范围：OP/控制台看到全服，普通玩家只看到自己召唤的假人。
  related:
    - { text: 生命周期接口, link: /http/endpoints-lifecycle }
    - { text: 动作与移动接口, link: /http/endpoints-action }
---

<ApiDocPage />
