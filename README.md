<div align="center">

# Civillis Regions

**Civillis 的 Forge 1.20.1 附属文明区域扩展**

[![Minecraft 1.20.1](https://img.shields.io/badge/Minecraft-1.20.1-62b47a?style=for-the-badge)](https://www.minecraft.net/)
[![Forge 1.20.1 Only](https://img.shields.io/badge/Forge-1.20.1%20Only-f16436?style=for-the-badge)](https://files.minecraftforge.net/net/minecraftforge/forge/)
[![Civillis 1.3.2+](https://img.shields.io/badge/Civillis-1.3.2+-4b6cb7?style=for-the-badge)](#安装)
[![FTB Chunks Optional](https://img.shields.io/badge/FTB%20Chunks-Optional-8e7cc3?style=for-the-badge)](#ftb-chunks-叠加层)
[![Release](https://img.shields.io/badge/Release-v1.2.0-007ec6?style=for-the-badge)](https://github.com/xiwanzi/CivillisRegions/releases/latest)
[![GitHub Download](https://img.shields.io/badge/GitHub-%E4%B8%8B%E8%BD%BD-181717?style=for-the-badge&logo=github)](https://github.com/xiwanzi/CivillisRegions/releases)
[![Modrinth](https://img.shields.io/badge/Modrinth-备用下载-00af5c?style=for-the-badge)](https://modrinth.com/mod/civillisregions)

</div>

## 简介

Civillis Regions 是适用于主模组 **Civillis** 的 Forge 1.20.1 附属模组。它提供自定义区域的 HUD 显示，并对 **FTB Chunks** 的 Civillis 文明区域与自定义区域地图叠加层提供初步兼容。

当前版本专注于：

- 创建服务器自定义区域，并在玩家进入或离开时显示 HUD 提示。
- 在自定义区域内创建方块级 3D 子区域，并配置独立 HUD 提示。
- 为自定义区域设置地图显示名和可选颜色。
- 在 FTB Chunks 大地图/小地图上显示 Civillis 文明区域与自定义区域叠加层。

## 安装

优先从 [GitHub Releases](https://github.com/xiwanzi/CivillisRegions/releases/latest) 下载最新文件：

```text
civillis-regions-forge-1.20.1-1.2.0.jar
```

也可以从 [Modrinth](https://modrinth.com/mod/civillisregions) 备用下载；该页面可能不会第一时间同步最新版本，请以 GitHub Releases 为准。

本模组仅支持 **Forge 1.20.1**。必须同时安装主模组，Civillis 版本需求为：

```text
civillis-forge-1.3.2+
```

FTB Chunks 叠加层是可选功能。需要地图叠加层时，请额外安装：

```text
ftb-chunks-2001.3.6+
ftb-library-2001.2.10+
```

不安装 FTB Chunks 时，自定义区域指令和 HUD 提示仍可正常使用。

## 使用教程

### 1. 创建自定义区域

站在目标维度内，用两个方块坐标圈出区域范围。这里的 `x y z` 是坐标占位符：

```mcfunction
/civil create spawn <x1> <y1> <z1> <x2> <y2> <z2>
```

模组会记录当前维度，并按两个坐标覆盖到的区块 X/Z 范围保存区域。Y 坐标不会影响区域高度。

### 2. 设置进入/离开 HUD

新建区域默认不会显示 HUD，至少需要设置一条进入或离开提示：

```mcfunction
/civil edit spawn "进入主城区域" "离开主城区域" [RRGGBB] [RRGGBB]
```

颜色是可选配置，格式为 `RRGGBB` 或 `#RRGGBB`。不填写颜色时使用默认 HUD 颜色；只填一个颜色时进入和离开共用该颜色，填两个颜色时分别设置进入和离开颜色。

### 3. 创建子区域

子区域必须挂在已有自定义区域下，并且 X/Z 范围必须完全落在父区域覆盖的区块范围内。子区域按完整方块 X/Y/Z 盒子判断，包含 Y 高度：

```mcfunction
/civil sub create spawn hall <x1> <y1> <z1> <x2> <y2> <z2>
/civil sub edit spawn hall "进入大厅" "离开大厅" [RRGGBB] [RRGGBB]
```

子区域有独立 HUD 提示，但不会显示在 FTB Chunks 地图叠加层上。

### 4. 重设区域范围

不删除区域即可重设范围，并保留原有 HUD 文本、颜色和地图叠加层配置：

```mcfunction
/civil resize spawn <x1> <y1> <z1> <x2> <y2> <z2>
/civil sub resize spawn hall <x1> <y1> <z1> <x2> <y2> <z2>
```

如果父区域重设后会导致已有子区域超出父区域 X/Z 范围，命令会拒绝执行。

### 5. 设置 HUD 提示大小

旧版 HUD 大小等效为 `1.00`。当前默认值是 `1.25`，表示比旧版大 25%。服务器可以通过指令实时调整：

```mcfunction
/civil hudscale
/civil hudscale 1.50
```

也可以在服务端配置中修改 `hud.noticeScale`，范围是 `0.50` 到 `3.00`。服务器值会随之后的区域提示发送给玩家，因此所有玩家都会使用服务器当前配置。

### 6. 设置地图叠加层

如果安装了 FTB Chunks，可以给自定义区域设置地图显示名，并可选设置叠加层颜色：

```mcfunction
/civil overlay spawn "地图显示名称" [RRGGBB]
```

### 7. 启用地图叠加层

在配置菜单中开启大地图或小地图叠加层后，FTB Chunks 地图会显示：

- Civillis 同步的文明区域。
- 通过 `/civil create` 创建的自定义区域。

自定义区域会画在 Civillis 文明区域之上；FTB Chunks 自身的 claimed chunks 仍保持更高的视觉优先级。子区域不会显示在地图叠加层中。小地图只显示色块和边框，不显示文字；大地图文字只在鼠标悬停提示中显示。

## 指令

所有指令都需要 2 级或更高的游戏管理员权限。区域 id 必须匹配 `[a-z0-9_-]+`。

```mcfunction
/civil create <id> <x1> <y1> <z1> <x2> <y2> <z2>
/civil resize <id> <x1> <y1> <z1> <x2> <y2> <z2>
/civil edit <id> "<进入提示>" "<离开提示>" [RRGGBB] [RRGGBB]
/civil overlay <id> "<地图显示名>" [RRGGBB]
/civil sub create <parentId> <id> <x1> <y1> <z1> <x2> <y2> <z2>
/civil sub edit <parentId> <id> "<进入提示>" "<离开提示>" [RRGGBB] [RRGGBB]
/civil sub resize <parentId> <id> <x1> <y1> <z1> <x2> <y2> <z2>
/civil sub list [parentId]
/civil sub delete <parentId> <id>
/civil hudscale [scale]
/civil list
/civil delete <id>
```

| 指令 | 用途 |
| --- | --- |
| `/civil create` | 在当前维度创建自定义区域。 |
| `/civil resize` | 不删除区域，重设自定义区域范围并保留配置。 |
| `/civil edit` | 设置进入/离开 HUD 文本和可选颜色。 |
| `/civil overlay` | 设置 FTB Chunks 地图显示名和可选叠加层颜色。 |
| `/civil sub create` | 在父区域内创建方块级 3D 子区域。 |
| `/civil sub edit` | 设置子区域进入/离开 HUD 文本和可选颜色。 |
| `/civil sub resize` | 不删除子区域，重设子区域 3D 方块范围并保留配置。 |
| `/civil sub list` | 列出所有子区域，或指定父区域下的子区域。 |
| `/civil sub delete` | 删除指定子区域。 |
| `/civil hudscale` | 查看或设置服务器控制的 HUD 提示大小。 |
| `/civil list` | 列出所有自定义区域、范围、颜色和提示状态。 |
| `/civil delete` | 删除指定自定义区域，并级联删除它的子区域。 |

## FTB Chunks 叠加层

安装 FTB Chunks `2001.3.6` 与 FTB Library `2001.2.10` 后，叠加层会读取 Civillis 同步的文明区块和本模组保存的自定义区域。

当前不会绘制荒野、未知、低等级、中等级和 MONSTER band。子区域也不会被绘制。自定义区域未单独设置颜色时，会使用配置菜单里的默认自定义区域颜色。

## 配置菜单

配置属于 Forge 客户端配置。可以通过 Configured 的模组配置菜单修改，也可以直接编辑客户端配置文件：

```text
config/civil_custom_regions-client.toml
```

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `largeMapEnabled` | `false` | 是否在 FTB Chunks 大地图绘制 Civillis/自定义区域叠加层。 |
| `minimapEnabled` | `false` | 是否在 FTB Chunks 小地图绘制 Civillis/自定义区域叠加层。 |
| `defaultCustomColor` | `#d8dcdd` | 没有单独设置颜色的自定义区域默认颜色。 |
| `civilizedHighColor` | `#d8dcdd` | Civillis 文明区域默认颜色。 |
| `civilizedHighLabel` | `文明区域` | 大地图鼠标悬停在 Civillis 文明区域上时显示的文字。 |
| `fillAlpha` | `56` | 叠加层填充透明度，范围 `0-255`。 |
| `borderAlpha` | `168` | 叠加层边框透明度，范围 `0-255`。 |

从早期 1.1.0 构建升级时，旧的黄/紫/粉默认叠加层颜色会自动迁移为浅灰默认值。玩家手动设置的其他颜色不会被覆盖。

HUD 提示大小属于 Forge 服务端配置，可以通过 `/civil hudscale <scale>` 修改，也可以在服务端配置中调整：

```text
serverconfig/civil_custom_regions-server.toml
```

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `hud.noticeScale` | `1.25` | 自定义区域和子区域 HUD 提示缩放。`1.00` 是旧版大小，允许范围 `0.50-3.00`。 |
