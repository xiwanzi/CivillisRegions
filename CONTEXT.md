# Civillis Regions Context

This addon extends Civillis with rectangular custom chunk regions and optional FTB Chunks map overlays.

Domain rules:

- "Civilized region" for FTB Chunks overlays means Civillis synchronized chunk bands `HIGH` and `MONSTER`.
- `WILDERNESS`, unknown, low, and medium bands are intentionally invisible on the overlay.
- "Custom region" means a `/civil create` rectangle stored by this addon, scoped to one dimension and inclusive chunk X/Z bounds.
- Custom regions draw above Civillis bands, but FTB Chunks claimed chunks take visual and tooltip priority over this addon overlay.
- Overlay text is hover-only on the FTB Chunks large map. Normal large-map rendering and minimap rendering draw only color blocks and borders.
- Region colors accept `RRGGBB` or `#RRGGBB` input and are stored as RGB integers. Missing colors fall back to client config defaults, which are low-saturation light grays.
- The public mod name and jar name are `Civillis Regions`; the internal Forge mod id remains `civil_custom_regions` for saved-data compatibility.
- FTB overlay config defaults are all off, so the original custom region enter/leave notice behavior remains active without enabling map overlays.
