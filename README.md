# ColorApocalypse

**ColorApocalypse** is a Minecraft (Forge) mod that turns survival into a chaotic and unpredictable challenge: the world progressively loses its colors, and each color that disappears causes the instant destruction of every associated block in the world.

> ⚠️ The mod is under active development. Some of the mechanics described below are still being implemented.

## The concept: the experience wheel

The mod repurposes the experience (XP) bar into a **color wheel**:

- At regular intervals, the wheel activates and colors scroll through the XP bar.
- As soon as it lands on a specific color, the apocalypse triggers: **every block tied to that color** (wool, concrete, terracotta, stained glass, naturally colored blocks like grass or leaves, etc.) is destroyed, everywhere around the player.
- Once a color has been "consumed," it's removed from play and can't be drawn again until a manual reset.

## How it works

### Automatic trigger
The time between wheel draws depends on the world's current difficulty (Peaceful, Easy, Normal, Hard). The higher the difficulty, the more frequent and destructive the draws.

### Manual trigger
An OP can force a draw at any time via command, to restart the pace or spice up a session.

### Destruction scope
To avoid impacting server performance, destruction only applies to blocks in **currently loaded** chunks, within a configurable radius around each connected player (`destructionRadius` parameter). This radius is evaluated at the moment of the draw chunks explored afterward are not retroactively affected.

## Commands (OP only)

| Command | Description |
|---|---|
| `/colorapocalypse start` | Manually triggers the wheel immediately |
| `/colorapocalypse status` | Shows the time remaining until the next automatic draw |
| `/colorapocalypse reset` | Resets the list of available colors |
| `/colorapocalypse settings <parameter> <value>` | Configures the mod's settings (e.g. `destructionRadius`) |

## Technical details

- **Platform:** Forge
- **Minecraft:** 26.2
- **Forge:** 65.1.2

## Building

The project uses Gradle with ForgeGradle 7 (requires Gradle 9.3 and a JDK 17 to run Gradle itself, the mod targets Java 25).

```
./gradlew build
```
