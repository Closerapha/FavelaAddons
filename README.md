# FavelaAddons

A Fabric client mod for Minecraft 26.1.2, built for the Telos Realms server.

## Features

**Quality of life**

- Chat alerts on a keyword, with configurable text and colour
- DPS meter
- Sounds on a chat trigger
- Trap counter
- Ambush and Deathmark calls by boss health percentage
- Boss health percentage on screen
- Primed timer: counts down to the moment the trait is ready again, on any weapon that carries it

**Livesplits**

LiveSplit-style dungeon timing, with boss phases as sub-splits.

- Runs start on their own when the portal shows up, discounting the time already burned off the portal's countdown
- Segments close on a chat line, a boss health percentage, or another portal
- Live comparison against your personal best at every checkpoint, using the LiveSplit colours
- Gold splits per segment
- Boss phases collapse once the boss is down

Every on-screen element is draggable through `/fa hud`.

## Commands

| Command | What it does |
| --- | --- |
| `/fa` | Opens the config menu |
| `/fa help` | Lists the commands |
| `/fa hud` | Moves the on-screen texts |
| `/fa test` | Tests the alert text and the sound |
| `/fa sound` | Tests the alert sound |
| `/fa split` | Closes the current segment |
| `/fa split delete` | Undoes the last split |
| `/fa split cancel` | Cancels the current run |
| `/fa split reset` | Wipes every personal best |
| `/fa split status` | Shows the cue the mod is waiting for |
| `/fa splits` | Reloads and lists the routes |
| `/fa splits pb [dungeon]` | Shows your personal best |
| `/fa splits start <dungeon>` | Starts a run by hand |

