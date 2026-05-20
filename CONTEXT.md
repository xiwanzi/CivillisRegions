# Civillis Regions Context

This addon extends Civillis with rectangular custom chunk regions and optional FTB Chunks map overlays.

Domain rules:

- "Civilized region" for FTB Chunks overlays means the Civillis synchronized `HIGH` civilization band only.
- `WILDERNESS`, unknown, low, medium, and monster bands are intentionally invisible on the overlay.
- "Custom region" means a `/civil create` rectangle stored by this addon, scoped to one dimension and inclusive chunk X/Z bounds.
- "Sub-region" means a child rectangle under a custom region, stored as inclusive block X/Y/Z bounds and used only for HUD notices.
- Custom regions draw above Civillis bands, but FTB Chunks claimed chunks take visual and tooltip priority over this addon overlay.
- Sub-regions never draw on FTB Chunks overlays and are not included in the client map sync packet.
- Overlay text is hover-only on the FTB Chunks large map. Normal large-map rendering and minimap rendering draw only color blocks and borders.
- Civillis civilization hover text defaults to `文明区域` and is client-configurable.
- Region colors accept `RRGGBB` or `#RRGGBB` input and are stored as RGB integers. Missing colors fall back to client config defaults, which are low-saturation light grays.
- HUD notice scale is server-controlled. `1.0` is the old size; the default is `1.25`.
- The public mod name and jar name are `Civillis Regions`; the internal Forge mod id remains `civil_custom_regions` for saved-data compatibility.
- FTB overlay config defaults are all off, so the original custom region enter/leave notice behavior remains active without enabling map overlays.
