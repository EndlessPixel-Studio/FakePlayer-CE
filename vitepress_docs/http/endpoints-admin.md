---
api:
  title: 高危接口
  description: 这些接口可以重载配置、以假人身份执行控制台命令或一次性移除所有假人。请关闭不需要的接口，不要把管理端口暴露到公网，也不要把假人设为 OP。
  requestType: query 参数
  responseType: application/json
  badges:
    - { text: 需要鉴权, type: info }
    - { text: 默认全部开启, type: warn }
    - { text: 可逐接口关闭, type: ok }
  warnings:
    - title: 高危操作
      text: 这些接口能重载配置、以假人身份执行命令或一次性移除所有假人。请关闭不需要的接口，不要把管理端口暴露到公网，也不要把假人设为 OP。
      type: danger
  endpoints:
    - title: GET /reload
      method: GET
      path: /reload
      summary: 重载插件配置与翻译。
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
    - title: GET /cmd
      method: GET
      path: /cmd
      summary: 以假人身份执行控制台命令。
      params:
        - { name: name, type: string, required: true, desc: 假人名称 }
        - { name: command, type: string, required: true, desc: 控制台命令，需 URL 编码 }
      responses:
        - { status: 200, title: 请求成功, code: '{ "status": "success" }' }
      examples:
        curl: |
          curl -G -H "Authorization: Bearer $TOKEN" "$API/cmd" \
            --data-urlencode "name=Bot_1" --data-urlencode "command=say hello"
        powershell: |
          Invoke-RestMethod -Uri "$API/cmd?name=Bot_1&command=say%20hello" -Headers @{ Authorization = "Bearer $TOKEN" }
    - title: GET /kickall
      method: GET
      path: /kickall
      summary: 踢出服务器所有假人。
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success", "count": 5 }
          fields:
            - { name: status, type: string, desc: 固定为 success }
            - { name: count, type: number, desc: 本次被踢出的假人数量 }
    - title: GET /killall
      method: GET
      path: /killall
      summary: 杀死服务器所有假人，可能掉落物品。
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success", "count": 5 }
    - title: GET /sayall
      method: GET
      path: /sayall
      summary: 让所有假人发送同一条消息。
      params:
        - { name: message, type: string, required: true, desc: 消息内容，需 URL 编码 }
      responses:
        - status: 200
          title: 请求成功
          code: |
            { "status": "success", "count": 5 }
  notes:
    - 在 config.yml 的 http-admin.interface.* 中把对应项设为 false 即可单独关闭某个接口。
    - 鉴权在接口开关之前检查，因此无法用 401/403 差异探测已启用端点。
    - /cmd 的权限等同该假人，不要把假人设为 OP，也只对可信调用方开放。
  faq:
    - q: 如何单独关闭某个高危接口？
      a: 在 config.yml 的 http-admin.interface.* 中把对应项设为 false。关闭后接口返回 403。鉴权在接口开关之前检查，因此无法用 401/403 差异探测已启用端点。
    - q: 为什么假人执行命令的权限很大？
      a: /cmd 以假人身份执行控制台命令，权限等同该假人。不要把假人设为 OP，也只对可信调用方开放该接口。
  related:
    - { text: 信息类接口, link: /http/endpoints-info }
    - { text: 动作与移动接口, link: /http/endpoints-action }
---

<ApiDocPage />
