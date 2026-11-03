# Quiet Lands

A Minecraft 1.7.10 / [GT New Horizons](https://www.gtnewhorizons.com/) add-on: one shared, **mob-free** dimension
generated with **RWG2** terrain (the new world generator in GTNH 2.9), reached through a placeable portal block.

Made for players whose Overworld still uses the old RWG and who want the new terrain to build in, without starting a
new world. Needs no changes to GTNH or any of its mods.

## What it does

- **Dimension** (default ID `1337`): RWG2 "continents" terrain with its own seed (world seed XOR `seedSalt`).
  Normal day/night (synced with the Overworld). Unloads when nobody is in it.
- **No natural mob spawning**, no animals placed by world gen. Mob spawners, spawn eggs and mobs you bring in still
  work, so mob farms are possible. The first appearance of each mob type is logged (`logMobs`), to spot other mods
  that spawn things.
- **Less to generate and tick** (each can be turned off in `config/quietlands.cfg`): no caves, mineshafts, villages,
  strongholds, dungeons, underground water/lava lakes, springs, vanilla ores, or weather (rain, snow, ice forming,
  lightning). GT ore veins don't generate either, since GT only generates in dimensions it knows.
- **Portal block** "Quiet Lands Portal": right-click to enter. The first visit creates a small stone-brick platform with
  a return portal near 0,0 (the shared hub). Later visits put you back where you last left Quiet Lands. Right-clicking
  a portal inside Quiet Lands takes you back to where you entered from. Place more portals anywhere: they all lead to
  the same world.
- **Recipe** (HV, needs Titanium):

  ```
  P C P    P = Titanium Plate, C = HV circuit (circuitAdvanced)
  C F C    F = Titanium Frame Box
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

## Building

`./gradlew build` (GTNH buildscript, RetroFuturaGradle). The jar ends up in `build/libs/`.

## License

MIT
