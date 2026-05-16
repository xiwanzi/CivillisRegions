# Civillis Custom Regions

English | [中文](#中文)

## English

Forge 1.20.1 addon for Civillis 1.3.2. This mod does not replace Civillis; install it together with:

- `civillis-forge-1.3.2-release+mc1.20.1.jar`
- `civil-custom-regions-forge-1.20.1-1.0.0.jar`

### Commands

All commands require game-master permission level 2 or higher.

```mcfunction
/civil create <id> <pos1> <pos2>
/civil edit <id> "<enter text>" "<leave text>"
/civil list
/civil delete <id>
```

`create` stores the executor's current dimension and the chunk X/Z rectangle covered by the two block positions. Y is ignored. A created region does not show HUD notices until `edit` sets at least one notice text.

Region ids must match `[a-z0-9_-]+`.

### Build

```powershell
.\gradlew.bat build
```

The built jar is written to `build/libs/`.

## 中文

Civillis 1.3.2 的 Forge 1.20.1 扩展模组。本模组不会替代 Civillis，需要和以下文件一起安装：

- `civillis-forge-1.3.2-release+mc1.20.1.jar`
- `civil-custom-regions-forge-1.20.1-1.0.0.jar`

### 指令

所有指令都需要 2 级或更高的游戏管理员权限。

```mcfunction
/civil create <id> <pos1> <pos2>
/civil edit <id> "<进入提示文本>" "<离开提示文本>"
/civil list
/civil delete <id>
```

`create` 会记录执行者当前所在维度，并保存两个方块坐标覆盖到的区块 X/Z 矩形范围。Y 坐标会被忽略。新建区域在通过 `edit` 设置至少一条提示文本前，不会显示 HUD 提示。

区域 id 必须匹配 `[a-z0-9_-]+`。

### 构建

```powershell
.\gradlew.bat build
```

构建后的 jar 会输出到 `build/libs/`。
