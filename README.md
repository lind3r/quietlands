# Quiet Lands

A Minecraft 1.7.10 / [GT New Horizons](https://www.gtnewhorizons.com/) add-on: one shared, **mob-free** dimension
generated with **RWG2** terrain (the new world generator in GTNH 2.9), reached through a placeable portal block.

Made for players whose Overworld still uses the old RWG and who want the new terrain to build in, without starting a
new world, or simply wants a better looking lategame dimension mobs and stuff removed to increase performance. Needs no changes to GTNH or any of its mods.

> [!CAUTION]
> **Back up your world before installing or testing this mod.**
>
> Quiet Lands is a small hobby project, tested by one person in one GTNH 2.9.0 world. It adds a dimension, changes
> world generation and teleports players between dimensions. A bug or an incompatibility can corrupt a save, kill your
> character or lose items. **You use it at your own risk; no one takes responsibility for lost worlds or items.**
>
> - Close the game and copy your whole save folder (or use your launcher's instance backup) before the first start.
> - Best: try it first in a **copy** of your instance and world, not in your main playthrough.
> - Keep backups while you play with it.

## How to use

1. **Back up first.** Either way works:
   - In game (GTNH has ServerUtilities): `/backup start before-quietlands` creates a zip in `.minecraft/backups/`.
     **Copy that zip somewhere else**: with GTNH's default config, named backups are rotated away like the automatic
     ones (`delete_custom_name_backups=true`, 12 kept).
   - Or close the game and copy your save folder (`saves/<world>`), or back up the whole instance in your launcher.

   Better still, try it in a copy of your instance first (see the warning above).
2. **Download** `quietlands-<version>.jar` from the [Releases](https://github.com/lind3r/quietlands/releases) page.
3. **Install:** put the jar into your instance's `mods` folder.
4. **Start the game.**
5. **Get a portal:** craft it (recipe below), add it as a quest reward (see below), or in creative/cheats:
   `/give @p quietlands:portal`.
6. **Place it and right-click it.** Your first visit builds a small stone-brick platform with a return portal near 0,0
   in Quiet Lands.
7. **Going back:** right-click any portal inside Quiet Lands to return to where you entered from.

## What it does

- **Dimension** (default ID `1337`): RWG2 "continents" terrain with its own seed (world seed XOR `seedSalt`).
  Normal day/night (synced with the Overworld). Unloads when nobody is in it.
- **No natural mob spawning**, no animals placed by world gen. Mob spawners, spawn eggs and mobs you bring in still
  work, so mob farms are possible. The first appearance of each mob type is logged (`logMobs`), to spot other mods
  that spawn things.
- **Less to generate and tick** (each can be turned off in `config/quietlands.cfg`): no caves, mineshafts, villages,
  strongholds, dungeons, underground water/lava lakes, springs, vanilla ores, or weather (rain, snow, ice forming,
  lightning). GT ore veins don't generate either, since GT only generates in dimensions it knows.
- **Excluded biomes** (`excludedBiomes`, default `Tainted Land`): RWG2 places Thaumcraft's Tainted Land, whose taint
  spreads and whose Flux Goo spawns Thaumic Slimes. Listed biomes are left out of Quiet Lands only.
- **Portal block** "Quiet Lands Portal": right-click to enter. The first visit creates a small stone-brick platform with
  a return portal near 0,0 (the shared hub). Later visits put you back where you last left Quiet Lands. Right-clicking
  a portal inside Quiet Lands takes you back to where you entered from. Place more portals anywhere: they all lead to
  the same world.
- **Recipe** (HV, needs Titanium):

  ```
  P E P    P = Titanium Plate
  E F E    F = Titanium Frame Box
  P E P    E = Eye of Ender
  ```

## Notes

- Old RWG and RWG2 store their data under the same key (`rwg_data`). In an old-RWG world the mod gives RWG2 its own
  in-memory copy so it never reads old RWG's file (otherwise: `ClassCastException`).
- RWG2 internals are accessed by reflection only. If a future RWG2 version renames something, the mod logs an error and
  doesn't register the dimension instead of crashing.
- Beds work and set your spawn there; dying in Quiet Lands without a bed there respawns you at the hub.

## Adding the portal as a quest reward

The GTNH "Personal Dimension" quest (HV tab, quest ID 217) can get the portal as a second reward: in BetterQuesting edit
mode, add an *Item* reward with `quietlands:portal`, or add a second `bq_standard:item` entry to the quest in your
world's `betterquesting/QuestDatabase.json` (world closed, backup first).

To complete that quest by command (e.g. for testing): `/bq_admin complete AAAAAAAAAAAAAAAAAAAA2Q==`. Quest IDs in
this BetterQuesting version are encoded; a plain `217` fails with "An unknown error occurred".

## Building

`./gradlew build` (GTNH buildscript, RetroFuturaGradle). The jar ends up in `build/libs/`.

## License

MIT, provided "as is", without warranty of any kind (see [LICENSE](LICENSE)).
