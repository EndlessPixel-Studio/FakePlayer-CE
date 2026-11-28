# AI 聊天配置

AI 聊天通过 OpenAI 兼容接口生成假人发言，与 MCP 无关。配置文件是 `plugins/FakePlayer-CE/ai-chat.yml`。

## 启用

```yaml
enabled: false
api: 'https://api.openai.com/v1'
api-key: ''
api-key-env: 'FAKEPLAYER_AI_KEY'
```

`api` 不含 `/chat/completions`。环境变量 `api-key-env` 优先级高于 `api-key`。开启后执行 `/fp reload`。

## 多模型

```yaml
model: 'gpt-4o'
active-model: 'default'
models:
  default:
    model: 'gpt-4o'
  # extra:
  #   model: 'qwen2.5'
  #   api: 'http://127.0.0.1:11434/v1'
  #   api-key: 'ollama'
```

- `models` 下每一项都能覆盖 `model`、`api`、`api-key`、`api-key-env`；未填的继承顶层默认值。
- `active-model` 指定默认模型；名称不存在时回退到 `default` 或第一个模型。
- OP 可用 `/fp aichat model <名称>` 运行时切换，选择会写回 `ai-chat.yml`。

## 背包上下文

```yaml
inventory-context:
  fakeplayer: false
  questioner: false
```

两项默认关闭，并且需要玩家个人的开关同时打开：

```
/fp config set aichat_fake_inventory true
/fp config set aichat_questioner_inventory true
```

假人背包按创建者的开关判断；提问者背包按私聊目标或自动接话的发言者判断。只有物品种类与数量会进入提示词。

## 采样与上下文

```yaml
temperature: 1.0
top-p: 1.0
presence-penalty: 0.0
frequency-penalty: 0.0
max-tokens: 0
max-context: 20
recent-chat-lines: 0
```

`max-tokens: 0` 表示不限制；`max-context: 0` 不保留历史；`recent-chat-lines` 控制参考多少条公共聊天。

## 回复节奏

```yaml
auto-reply:
  enabled: false
  chance: 0.15
  cooldown-seconds: 90
  min-length: 4
  max-length: 120
allow-continuous: false
timeout-seconds: 30
interval-ticks: 200
```

`auto-reply` 只在玩家说了像样的话时接话，短消息、纯符号和刷屏会被过滤。`allow-continuous` 是定时主动发言，默认关闭，因为会持续消耗 token。

## 提示词

```yaml
system-prompt: |
  你是 Minecraft 服务器中的一名玩家, 名字是 {name}。
  说话简短、自然、口语化, 每次只说一到两句话, 不要使用括号描述动作, 不要提到自己是 AI。
turn-prompt: '说一句话。'
private-format: '<gray>[{name} -> 你] {message}</gray>'
```

`{name}` 会替换为假人名称。私聊格式支持 MiniMessage；模型输出里的 Markdown 会转成 Adventure 样式，公共聊天则退化为纯文本。
