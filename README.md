# Civillis Regions

English | [中文](#中文)

Forge 1.20.1 addon for Civillis that adds custom rectangular regions, configurable enter/leave HUD notices, and optional FTB Chunks map overlays for Civillis civilization bands and custom regions.

适用于 Civillis 的 Forge 1.20.1 附属模组，提供自定义矩形区域、可配置的进出 HUD 提示，以及可选的 FTB Chunks 文明区域与自定义区域地图叠加层。

## English

Forge 1.20.1 addon for Civillis 1.3.2. This mod does not replace Civillis; install it together with:

- `civillis-forge-1.3.2-release+mc1.20.1.jar`
- `civillis-regions-forge-1.20.1-1.1.0.jar`

The packaged 1.1.0 jar is included in `releases/`.

### Commands

All commands require game-master permission level 2 or higher.

```mcfunction
/civil create <id> <pos1> <pos2>
/civil edit <id> "<enter text>" "<leave text>" [RRGGBB] [RRGGBB]
/civil overlay <id> "<display name>" [RRGGBB]
/civil list
/civil delete <id>
```

`create` stores the executor's current dimension and the chunk X/Z rectangle covered by the two block positions. Y is ignored. A created region does not show HUD notices until `edit` sets at least one notice text.

`edit` can optionally set enter/leave notice colors. One color applies to both notices; two colors set enter and leave separately.

`overlay` sets the label and optional overlay color used by FTB Chunks maps. Colors accept `RRGGBB` or `#RRGGBB`; command examples should usually omit `#`, for example `d9e6ff`.

Region ids must match `[a-z0-9_-]+`.

### FTB Chunks Overlay

When FTB Chunks `2001.3.6` and FTB Library `2001.2.10` are installed, this addon draws map overlays for:

- Civillis synchronized civilization chunks.
- Custom `/civil create` regions.

Wilderness, unknown, low, medium, and monster Civillis bands are not drawn. Custom regions are drawn above Civillis civilization chunks, while FTB claimed chunks stay visually above this addon overlay. Map text is only shown in the large-map hover tooltip; normal map rendering shows color blocks and borders only.

Client config is written under Forge's client config folder and includes:

- `largeMapEnabled`, default `false`
- `minimapEnabled`, default `false`
- default custom and civilization colors, both low-saturation light gray by default
- large-map hover label for civilization chunks, defaulting to `文明区域`
- fill and border alpha values

The minimap overlay only draws color blocks and borders; it never draws text.

When upgrading from earlier 1.1.0 builds, the old yellow/purple/pink default overlay colors are automatically migrated to the new light gray default. User-chosen non-legacy colors are left unchanged.

### Build

```powershell
.\gradlew.bat build
```

The built jar is written to `build/libs/`.

## 中文

Civillis 1.3.2 的 Forge 1.20.1 扩展模组。本模组不会替代 Civillis，需要和以下文件一起安装：

- `civillis-forge-1.3.2-release+mc1.20.1.jar`
- `civillis-regions-forge-1.20.1-1.1.0.jar`

仓库内已附带 1.1.0 jar，位置在 `releases/`。

### 指令

所有指令都需要 2 级或更高的游戏管理员权限。

```mcfunction
/civil create <id> <pos1> <pos2>
/civil edit <id> "<进入提示文本>" "<离开提示文本>" [RRGGBB] [RRGGBB]
/civil overlay <id> "<地图显示名>" [RRGGBB]
/civil list
/civil delete <id>
```

`create` 会记录执行者当前所在维度，并保存两个方块坐标覆盖到的区块 X/Z 矩形范围。Y 坐标会被忽略。新建区域在通过 `edit` 设置至少一条提示文本前，不会显示 HUD 提示。

`edit` 可以额外设置进入/离开提示颜色。只填一个颜色时进入和离开共用该颜色，填两个颜色时分别设置进入和离开颜色。

`overlay` 用于设置 FTB Chunks 地图上的显示名和可选叠加层颜色。颜色接受 `RRGGBB` 或 `#RRGGBB`；命令示例建议省略 `#`，例如 `d9e6ff`。

区域 id 必须匹配 `[a-z0-9_-]+`。

### FTB Chunks 叠加层

安装 FTB Chunks `2001.3.6` 和 FTB Library `2001.2.10` 后，本扩展会在地图上显示：

- Civillis 同步的文明区块。
- 本模组 `/civil create` 创建的自定义区域。

荒野、未知、低/中等级和 MONSTER band 不显示。自定义区域会画在 Civillis 文明区之上，但 FTB Chunks 自身的 claimed chunk 视觉优先级更高，不会被本叠加层盖住。地图文字只在大地图鼠标悬停 tooltip 中显示，正常地图渲染只显示色块和边框。

Forge 客户端配置包含：

- `largeMapEnabled`，默认关闭
- `minimapEnabled`，默认关闭
- 自定义区域、文明区域的默认颜色，默认都是低饱和浅灰色
- 文明区块的大地图悬浮字样，默认是 `文明区域`
- 填充和边框透明度

小地图只显示色块和边框，永远不显示文字。

从早期 1.1.0 构建升级时，旧的黄/紫/粉默认叠加层颜色会自动迁移为新的浅灰默认色。用户手动设置的其他颜色不会被覆盖。

### 构建

```powershell
.\gradlew.bat build
```

构建后的 jar 会输出到 `build/libs/`。
