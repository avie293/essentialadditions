# Changelog

All notable changes to EssentialAdditions are listed here. Every Minecraft version (26.1, 26.1.1, 26.1.2, 26.2 and
26.3) gets the same features unless a section says otherwise.

## 1.0.4 (only 26.2)

### Fixed
- The 26.2 version crashed while the game was starting. The mixin that hides the vanilla effect icons (for the
  Effects HUD) looked for the method `extractEffects` in the class `Gui`, but since 26.2 the effect icons are drawn
  by the new class `Hud`, like in 26.3. The 26.2 version was still using the mixin of 26.1, which compiles fine but
  fails when the game loads the class. The 26.2 version now uses the same mixin as 26.3. The other versions are not
  affected and stay at 1.0.3.

## 1.0.3

### Fixed
- Advanced Tooltips showed "Mining speed: 9223372036854775807" on swords. The tool info took the highest speed of
  all mining rules of the item, and since 1.21.5 swords have a rule that mines bamboo instantly with the largest
  possible speed. Now only the rules for the "mineable/..." block tags count as mining speed, so swords (which have
  no such rule) show no tool info again, like shears and other items that only mine a few blocks.

## 1.0.2 (unreleased)

### Fixed
- Inventory Sorting did nothing in the creative inventory, it only worked in chests and the survival inventory.
  The creative inventory was left out on purpose because inventory clicks work differently there. Now the sort key
  also works on its inventory tab: the sorted inventory is computed first and every changed slot is sent to the
  server like the creative inventory does itself, with the same order, merging and hotbar setting as in survival.
  The item tabs and the search are not affected, so the key can still be typed into the search field. Middle click
  stays off in creative, where it copies items.

## 1.0.1 (unreleased)

### Fixed
- The settings window could not be opened from the main menu, only in a world: the "Settings" button in the
  TabbyLib menu did nothing. Since 26.1 the data of items is only loaded together with a world, and the cards of
  the window created their item icons right away, which failed before the first world was joined. The cards now
  draw the flat item texture as long as no world was loaded, and the real item afterwards.
- The HUD editor previews of the Armor HUD and the Pickup Notifications crashed in the main menu for the same
  reason. Without a world they now show no example items.

### Changed
- New card icons where the old item has no flat texture for the main menu: Keystrokes shows a lever, Toggle Sprint
  a feather and Inventory Sorting a bundle.

## 1.0.0 (development)

First release. EssentialAdditions bundles small quality of life features in one client side mod. Every feature can
be turned on and off on its own, and all of them are configured in one settings window.

### Settings window
- Own settings window in the style of TabbyLib. At the top there is a grid of cards, one for every feature, with an
  item icon, the name and a switch that turns the feature on or off. Turned off features are shown greyed out.
- The cards wrap into rows that fit the window width. At most two rows are visible at once, the others are reached
  with the mouse wheel over the cards (with a small scroll bar); the selected card is always scrolled into view.
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
- Shulker box preview: the content of shulker boxes is shown as an inventory grid with item counts and durability
  bars, tinted in the color of the box (can be turned off). Other filled containers (e.g. chests picked up with
  their content) show the rows they use. The vanilla text list of the content is hidden while the preview is shown
  (can be turned off).
- Ender chest preview: ender chest items show the content of your ender chest. The client only knows it while the
  ender chest is open, so it is remembered from the last time you opened it, until you leave the world; before
  that a hint says to open it once.
- Tool info: mining speed (also the speed with the Efficiency level of the item) and the mining level of tools
  (wood, stone, copper, iron, gold, diamond, netherite), read from the tool component, so modded tools work too.
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

### Fullbright
- Makes everything bright in the dark, as if you had the night vision effect. Toggled with a key (default: G).
- Works through the night vision strength of the light map, so it needs no gamma values above the vanilla limit and
  looks exactly like the vanilla effect, without the effect icon or particles.
- Strength from 10% to 100% (100% is as bright as night vision).
- Optional "Fullbright: On / Off" message above the hotbar when toggled.
- Off by default; the switch on its card and the key do the same.

### Info HUD
- A movable box with information about your surroundings and the game. Every line can be turned on and off:
  - Coordinates (block position, or the exact position with one decimal).
  - Nether / Overworld coordinates: in the Overworld the matching Nether coordinates (divided by 8), in the Nether
    the matching Overworld coordinates (times 8). Handy for linking portals.
  - Facing: compass direction with the axis, e.g. "North (-Z)".
  - Biome, translated into the game language.
  - Light level: block light and sky light at your feet (mobs spawn at block light 0).
  - FPS.
  - Ping (your own latency from the player list).
  - Real time clock of your computer, as 24 hour or 12 hour clock.
- Label and value colors can be changed (default: orange labels, white values).
- Appearance like every HUD of the mod: position (dragged in the TabbyLib HUD editor, default top left), size
  (0.5x - 3x), background with own color and text shadow.
- Also replaces a separate FPS / ping HUD: both are lines of this box.
- Off by default.

### Armor HUD
- Shows your helmet, chestplate, leggings and boots, and optionally the items in your main hand and off hand, as
  item icons with their durability.
- Layout: vertical (one item below the other) or horizontal (side by side).
- Durability as number, as percent or not at all, plus the item's durability bar (can be turned off).
- Held items without durability (blocks, arrows, food) show how many of them are in your whole inventory (can be
  turned off, then the stack size is shown).
- Warning: items with less durability left than the warning limit (0 - 50%, default 10%) are drawn in red, and a
  blinking "Low durability!" appears above the HUD, right aligned (can be turned off). 0% turns the warning off.
- Appearance like every HUD of the mod: position (default bottom right), size, background and text shadow.
- The HUD editor preview uses your real equipment, or example items when you wear nothing.
- Off by default.

### Effects HUD
- Shows your active status effects as a list with the vanilla effect icon, the name with level (e.g. "Speed II")
  and the remaining time. Names and time can be turned off separately.
- Sorting: as added, ending first or good effects first.
- Colors by type: good effects in green, bad effects in red (can be turned off).
- The icon blinks during the last seconds of an effect (0 - 30 seconds, default 10, 0 turns it off).
- Hides the vanilla effect icons at the top right while the HUD is on (can be turned off).
- Effects that hide their icon (e.g. from beacons set to hide particles) are left out, like in vanilla.
- Appearance like every HUD of the mod: position (default top right), size, background and text shadow. The HUD
  editor preview shows example effects when you have none.
- Off by default.
- Version specific: in 26.3 the vanilla effect icons are drawn by the new `Hud` class instead of `Gui`.

### Keystrokes
- Overlay with the movement keys (W / A / S / D layout), the mouse buttons and the jump key that light up while
  pressed, like in PvP clients.
- Shows the keys you really bound, so it also works with other keyboard layouts or changed controls.
- Clicks per second (CPS) of the last second on the mouse buttons, or "LMB" / "RMB" without it.
- Mouse buttons and jump key can be turned off.
- Own colors for keys, pressed keys, text and pressed text (with transparency).
- Appearance like every HUD of the mod: position (default left edge, middle), size and text shadow; the background box
  behind all keys is off by default, as every key has its own.
- Off by default.

### Toggle Sprint
- Own keys for toggle sprint and toggle sneak: one press keeps you sprinting or sneaking until the next press. Both
  keys are unbound by default, so they do not take keys that are already in use; set them in the settings window
  or in the controls screen.
- "Always sprint": sprints whenever you walk forward, without any key.
- Works by holding down the vanilla sprint / sneak key, so it behaves exactly like holding them yourself and works
  on every server. While a screen (inventory, chat, ...) is open nothing is held.
- When toggled off, the keys go back to the state of your physical keys.
- Optional: remember the toggled states after leaving the world or restarting the game.
- Status text like "[Sprinting (Toggled)]" / "[Sneaking (Toggled)]", optionally also for normal sprinting and
  sneaking, with own text color, position (default top middle), size, background and text shadow.
- Off by default.

### Sleep Reminder
- A message (above the hotbar or in the chat) with an optional bell sound when the night starts and beds can be
  used.
- Also when a thunderstorm starts at day, as beds work then too (can be turned off).
- Phantom warning: after a set number of nights without sleep (1 - 5, default 3 like vanilla) the night message
  becomes a red warning that phantoms can come.
- The server keeps the real "time since rest" and does not send it to the client, so the mod counts the nights
  without sleep itself while the game runs; sleeping resets the counter. After a restart it starts at 0 again.
- Only in the Overworld, where beds work. Joining a world at night does not remind you for the night that is
  already running.
- Off by default.

### Pickup Notifications
- Shows "+12 Coal" with the item icon when you pick up items. Pickups of the same item are added up while the
  entry is visible, the newest entry is on top.
- Entries stay for 1 - 10 seconds (default 3) and fade out at the end; at most 1 - 10 entries are shown at once
  (default 5).
- Text in the rarity color of the item (e.g. enchanted books in yellow), can be turned off; item icons can be
  turned off.
- Optional total in your inventory, e.g. "+12 Coal (76)".
- Based on the pickup message of the server, so it only counts items you really picked up (not items that were
  given to you by commands or moved in the inventory). Works on every server.
- Appearance like every HUD of the mod: position (default right side, below the middle), size, background (off by
  default) and text shadow. The entries are right aligned so the HUD can sit at the right edge.
- Off by default.

### Block Outline
- Own color for the outline of the block you look at, with transparency (vanilla: black with 40% opacity).
- Rainbow mode that cycles through all colors (speed 0.25x - 4x), keeping the transparency of the chosen color.
- Thickness from 0.5x to 5x of vanilla.
- The extra black outline of the vanilla high contrast setting stays unchanged.
- Off by default.
- Version specific: the outline method is called `submitHitOutline` since 26.2 (`renderHitOutline` before).

### Chat
- Timestamps in front of every message, e.g. "[14:32]", optionally with seconds, in an own color (default gray).
- Longer chat history: keeps 100 - 2000 messages for scrolling up (default 500, vanilla 100).
- Repeated messages: the same message several times in a row is shown once with "(x3)" behind it instead of
  filling the chat, the counter has its own color. Compares the visible text, so it also works for messages from
  servers and plugins.
- Clearing the chat (e.g. F3 + D) also resets the repeat counter.
- Only changes what your own client shows, nothing is sent to the server.
- Off by default.

### Freelook
- Look around freely while a key is held (default: Left Alt), like the Perspective mod: the mouse turns only the
  camera, your player keeps walking and looking in the same direction. When you let go, the camera snaps back.
- Key mode: hold, or press once to toggle.
- Switches to third person while looking around so you can see yourself, and back to the previous view afterwards
  (can be turned off to look around in first person).
- Up / down can be inverted.
- Works together with the zoom: the lower zoom sensitivity also applies to freelook.
- Only the camera of your own client moves, the server still sees your real looking direction.
- On by default (it only does something while its key is pressed).

### Inventory Sorting
- Sorts with a key in any inventory screen (default: R) or with a middle click on a slot (can be turned off).
- Sorts the inventory under the mouse: your own inventory, or the chest, barrel, shulker box or other container you
  have open. Without a hovered slot the opened container is sorted, or your inventory if there is none.
- Your hotbar, armor, off hand and crafting slots stay as they are (the hotbar can be included). Only real
  storage is sorted: crafting grids, result slots, furnaces and other containers with less than 9 slots are left
  alone.
- Merges stacks of the same item first (can be turned off), then orders them: by item id (keeps kinds like all
  logs together), by name or biggest stacks first. Full stacks come before the rest of the same item.
- Works only with normal inventory clicks, exactly like sorting by hand, so it works on every server and nothing
  can get lost. Does nothing while you hold an item on the cursor.
- In creative mode the sort key works on the inventory tab (since 1.0.2); middle click stays off there, as it
  copies items.
- On by default.

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
