# ColorApocalypse

**ColorApocalypse** is a Minecraft mod (Forge & Fabric) that turns survival into a chaotic and unpredictable challenge: the world progressively loses its colors, and each color that disappears causes the instant destruction of everything associated with it blocks, mobs, and item drops.

> ⚠️ The mod is functional and playable, but still being tuned and expanded.

## The concept: the experience wheel

The mod repurposes the experience (XP) bar into a **color wheel**:

- At regular intervals, the wheel activates and spins through colors on the XP bar, accompanied by a countdown and sounds.
- As soon as it lands on a specific color, the apocalypse triggers:
  - **Every block tied to that color** (wool, concrete, terracotta, stained glass, naturally colored blocks like grass or leaves, etc.) is destroyed, everywhere around each player.
  - **Every mob of that color** (sheep, wolves, llamas, tropical fish, painted shulkers, and many other mobs treated as "belonging" to a color) dies instantly.
  - **Every dropped item of that color** on the ground is destroyed. Optionally (off by default), the same color can also be stripped straight out of every online player's inventory and equipment.
- Once a color has been "consumed," it's removed from play and can't be drawn again until either an operator resets the pool manually, or **every single color has been eliminated**, at which point the pool automatically refills and the cycle starts over on its own.

## How it works

### Automatic trigger
The time between wheel draws depends on the world's current difficulty (Peaceful, Easy, Normal, Hard). The higher the difficulty, the more frequent the draws.

### Manual trigger
An OP can force a draw at any time via command, to restart the pace or spice up a session.

### Destruction scope
To avoid impacting server performance, block destruction only applies to blocks in **currently loaded** chunks, within a configurable radius around each connected player (`destructionRadius` parameter). This radius is evaluated at the moment of the draw chunks explored afterward are not retroactively affected. Mob and item destruction scan all currently loaded chunks across every dimension.

### Color assignment
Which block/mob/item belongs to which color isn't hardcoded from a fixed list it's computed automatically (from each block's map color, or a mob/item's real in-game color when it has one), then corrected and completed by hand in editable JSON files (`block_colors.json`, `entity_colors.json`, `item_colors.json`) for anything the automatic detection gets wrong or can't determine on its own.

## Commands (OP only)

| Command | Description |
|---|---|
| `/colorapocalypse start` | Manually triggers the wheel immediately |
| `/colorapocalypse status` | Shows the time remaining until the next automatic draw |
| `/colorapocalypse reset` | Resets the list of available colors |
| `/colorapocalypse pause` | Toggles the automatic timer on/off |
| `/colorapocalypse settings <parameter> <value>` | Configures the mod's settings (see below) |

## Settings

| Parameter | Default | Description |
|---|---|---|
| `destructionRadius` | 2 | Radius (in chunks) around each player within which blocks are destroyed |
| `autoTimerEnabled` | true | Whether the wheel draws automatically over time (always off on Peaceful) |
| `mobKillEnabled` | true | Whether mobs of the eliminated color are killed |
| `itemDestroyEnabled` | true | Whether item drops of the eliminated color are destroyed |
| `inventoryItemDestroyEnabled` | false | Whether items are also removed from every online player's inventory and equipment |
| `easyIntervalMinutes` | 5 | Minutes between automatic draws on Easy |
| `normalIntervalMinutes` | 3 | Minutes between automatic draws on Normal |
| `hardIntervalMinutes` | 1 | Minutes between automatic draws on Hard |

## Technical details

- **Platforms:** Forge, Fabric
- **Minecraft:** 26.2
- **Forge:** 65.1.2
- **Fabric Loader:** 0.19.4 / **Fabric API:** 0.158.0+26.2

Both loaders build from the same shared codebase (`src/`), with a small loader-specific bootstrap package each (`org.furranystudio.colorapocalypse.forge` / `.fabric`). Everything else, block/mob/item color detection, destruction logic, the roulette timing, settings, is identical on both.

## Building

`forge/` and `fabric/` are each their own standalone Gradle project (no root `build.gradle`/`settings.gradle`), build them with `-p`:

```
gradle -p forge build
gradle -p fabric build
```

Requires Gradle 9.7+ and a JDK 17+ to run Gradle itself (the mod targets Java 25). The Fabric build specifically needs Gradle running on a JDK 25, since Fabric Loom requires it.