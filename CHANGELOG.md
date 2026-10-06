# Changelog

All notable changes to EssentialAdditions are listed here. Every Minecraft version (26.1, 26.1.1, 26.1.2, 26.2 and
26.3) gets the same features unless a section says otherwise.

## 1.0.0 (unreleased)

First release. EssentialAdditions bundles small quality of life features in one client side mod. Every feature can
be turned on and off on its own, and all of them are configured in one settings window.

### Settings window
- Own settings window in the style of TabbyLib. At the top there is a row of cards, one for every feature, with an
  item icon, the name and a switch that turns the feature on or off. Turned off features are shown greyed out.
- Clicking a card shows the settings of that feature below the cards: title, short description and the TabbyLib
  option rows (sliders, toggles, color pickers, key bindings, HUD position editor, ...).
- Hovering a card shows the description of the feature, hovering the switch shows whether it is on or off.
- Changes are only applied with "Save" or "Done", like in the TabbyLib screen. "Discard" throws them away,
  "Reset Feature" sets every setting of the selected feature back to its default. Ctrl + S saves.
- Leaving the window with unsaved changes asks whether they should be saved (can be turned off in the TabbyLib
  settings).
- Opened with the new `/esad` command, with `/dc config` (opens the Day Counter card directly) or from the TabbyLib
  menu. In the TabbyLib menu EssentialAdditions shows a short description and a "Settings" button that opens the
  window.

### Config files
- Every feature has its own config file in the folder `config/esad/`: `zoom.json`, `dynamiclights.json`,
  `tooltip.json`, `daycounter.json` and `pingdisplay.json`.
- Settings from the old single file `config/esad.json` (development builds) are taken over automatically the first
  time, the old file is deleted afterwards.

### Zoom
- Zoom like Zoomify / OptiFine on a key (default: C), configurable in the settings window and in the vanilla
  controls screen (category "EssentialAdditions").
- Key mode: hold the key to zoom, or press it once to toggle the zoom.
- Zoom level from 1.5x to 20x (default 4x).
- Scroll wheel zoom while zooming, the hotbar slot does not change meanwhile. Configurable step per scroll
  (default 1.25x), maximum zoom (default 50x) and whether the scrolled zoom is remembered for the next zoom.
- Smooth animation for zooming in and out with separate durations (default 0.25s / 0.2s, 0 zooms instantly) and five
  animation curves: linear, sine, quadratic, cubic and exponential. The animation runs on real time, so it looks
  the same at every frame rate.
- Smooth scrolling: zoom changes from the scroll wheel are animated instead of jumping.
- Relative mouse sensitivity (0 - 100%): lowers the mouse sensitivity the further you zoom in, so aiming feels the
  same at every zoom level.
- Optional cinematic camera while zooming.
- The hand and held item are hidden while zooming (can be turned off).

### Dynamic Lights
- Light sources light up the blocks around them, like LambDynamicLights. Only visual, the real light level of the
  world does not change, so mobs still spawn in the dark.
- Light sources:
  - Items held by players and mobs (torches, lanterns, glowstone, lava buckets, blaze rods, glow berries, ...).
    Every block item that gives light works, mod blocks included.
  - Dropped items.
  - Burning entities.
  - Glowing entities: blazes, magma cubes, glow squids, fireballs, primed TNT, spectral arrows and creepers about to
    explode.
- Your own light can be turned off separately, every kind of light source has its own switch.
- Torches, campfires, lava buckets and similar items give no light under water (can be turned off).
- The light fades out smoothly with distance (fractional light levels) and also lights up entities, block entities
  and particles, with smooth lighting.
- Update rate: realtime (every tick), fast (0.25s) or slow (0.5s). Slower rates rebuild fewer chunks on weak PCs.
- Range: only light sources up to 16 - 128 blocks away from you are used (default 64).

### Advanced Tooltips
- Extra lines in item tooltips. Every info can be shown always, only while Shift is held, or never.
- Durability in the look of the Durability Tooltip mod:
  - Three styles: numbers ("Durability: 261 / 1561"), a bar ("[██▒▒▒▒▒▒▒▒]") or a text ("Slightly damaged",
    "Severely damaged", ...).
  - Colors: varying (green / gold / red depending on the remaining durability), always gold or gray.
  - The "Durability:" label and showing it for undamaged items can be turned off.
- Food values: hunger points and saturation.
- Burn time of furnace fuels in seconds and how many items they smelt.
- Anvil repair cost of items that were repaired or enchanted before.
- Enchantment descriptions: a short explanation below every enchantment, for all 43 vanilla enchantments, in
  English and German.
- Mod name and item id. The mod name is left out when Mod Menu is installed, because Mod Menu adds it itself.
- Map preview: filled maps are shown as a picture in the tooltip, size 32 - 128 pixels.
- "Hold Shift for more info" hint when an info is only visible with Shift (can be turned off).
- No duplicate lines with the vanilla advanced tooltips (F3 + H): durability and item id are left out then.

### Day Counter
- The content of the Day Counter mod: shows the current Minecraft day in a HUD.
- Formats: "Day 12", "12", "Day 12 - 06:30" or an own text with `{day}` and `{time}`, optionally with a 12 hour
  clock.
- Text color, text shadow, size (0.5x - 3x), background and border with own colors.
- The position is set by dragging the counter in the TabbyLib HUD editor, it stays at its place when the window
  size changes.
- Toggle key (default: H) and the `/dc` command: `/dc config`, `/dc position`, `/dc bgtoggle`, `/dc debug` and
  `/dc help`.
- Optional debug output of day, time and position to the log.
- If the standalone Day Counter mod is installed, this feature turns itself off so the day is not shown twice.

### Ping Display
- The content of the Ping Display mod: the ping as colored number in the tab list instead of the signal bars.
- Optionally behind the name tags of players, with an own bracket color.
- Color mode "steps" (one color per ping range) or "gradient" (the color slowly changes from one range to the
  next), five configurable colors and three ping limits.
- "ms" behind the number, text shadow and the text for an unknown ping can be changed.
- If the standalone Ping Display mod is installed, this feature turns itself off so the ping is not shown twice.

### General
- Requires Fabric Loader 0.19.5+, Fabric API and TabbyLib 1.1.0+.
- Client side only, works on every server.
- English and German translations.
- License: CC BY-NC-SA 4.0.
- Issues and suggestions: https://github.com/avie293/avie-issues/issues

### Technical notes
- Version specific code is kept small: the hand renderer and light lookup mixins differ between 26.1.x, 26.2 and
  26.3, the burn time lookup differs in 26.3 (fuels are data driven there and read from the game files), and screens
  are opened through `Minecraft.gui` since 26.2.
- 26.3 uses a new input backend with other key and mouse button codes; all key and button checks use the
  `InputConstants` of the running version.
