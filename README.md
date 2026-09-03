<div align="center">

![Logo](https://i.imgur.com/rl6dII1.png)

## RealMines

### Brand new, simple and efficient mine management plugin.

[![Build](https://img.shields.io/github/actions/workflow/status/joserodpt/RealMines/maven.yml?branch=master)](https://github.com/JoseGamerPT/RealMines/actions)
![Issues](https://img.shields.io/github/issues-raw/JoseGamerPT/RealMines)
[![Stars](https://img.shields.io/github/stars/JoseGamerPT/RealMines)](https://github.com/JoseGamerPT/RealMines/stargazers)
[![Chat)](https://img.shields.io/discord/817810368649887744?logo=discord&logoColor=white)](https://discord.gg/t7gfnYZKy8)

<a href="/#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/compact/supported/spigot_46h.png" height="35"></a>
<a href="/#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/compact/supported/paper_46h.png" height="35"></a>
<a href="/#"><img src="https://raw.githubusercontent.com/intergrav/devins-badges/v2/assets/compact/supported/purpur_46h.png" height="35"></a>

</div>

Welcome to the **RealMines plugin**! This is a brand new mine management plugin. Coded in the 1.14 codebase, it's aim is
to provide Server Owners and Players with a fast and reliable mines system.

Everything is managed from in-game GUIs — you rarely need to touch a config file — and every mine lives in its own YAML
file under `plugins/RealMines/mines/`, so mines are easy to back up, move between servers or edit by hand.

----

## Table of Contents

* [Features](#features)
* [Requirements](#requirements)
* [Installation](#installation)
* [Getting Started](#getting-started)
  * [Creating a block mine](#creating-a-block-mine)
  * [Creating a farm mine](#creating-a-farm-mine)
  * [Creating a schematic mine](#creating-a-schematic-mine)
* [Mine Types](#mine-types)
* [Resetting Mines](#resetting-mines)
* [Block Sets, Percentages and Depth Ranges](#block-sets-percentages-and-depth-ranges)
* [Break Actions](#break-actions)
* [Mine Signs](#mine-signs)
* [Stats, Achievements and Leaderboards](#stats-achievements-and-leaderboards)
* [Commands](#commands)
* [Permissions](#permissions)
* [PlaceholderAPI](#placeholderapi)
* [Configuration Files](#configuration-files)
* [Importing From Other Plugins](#importing-from-other-plugins)
* [Mine File Format](#mine-file-format)
* [API](#api)
* [Building From Source](#building-from-source)
* [Pictures](#pictures)
* [Links](#links)

----

## Features

* **Three mine types** — block mines, farm (crop) mines and schematic mines
* **Reset system** — by time, by mined percentage, or grouped into shared reset tasks
* **Block sets** — multiple sets of blocks per mine, cycled incrementally, randomly, or not at all
* **Depth ranges** — materials only spawn at a chosen depth range of the mine, measured from any face
* **Break actions** — give money, give/drop items or run commands when a specific block is broken, with chances
* **Player stats, achievements and leaderboards** — backed by SQLite, MySQL, MariaDB, PostgreSQL or SQL Server
* **Mine signs** — live countdown, remaining blocks, progress bars
* **Simple and performant GUI interface** for everything, including a material search
* **PlaceholderAPI support** for mines, player stats and leaderboards
* **Importers** for CataMines, JetsPrisonMines and MineResetLite
* **Fully translatable** through `language.yml`
* **Developer API** with events and manager interfaces

----

## Requirements

| | |
|---|---|
| **Server software** | Spigot, Paper or Purpur |
| **Minecraft** | 1.14 or newer |
| **Java** | 16 or newer |
| **Required plugin** | [WorldEdit](https://dev.bukkit.org/projects/worldedit) or [FAWE](https://www.spigotmc.org/resources/fastasyncworldedit.13932/) |

Optional, but supported when present:

| Plugin | What it adds |
|---|---|
| [Vault](https://www.spigotmc.org/resources/vault.34315/) | Enables the `GIVE_MONEY` break action and money achievement rewards |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | Registers the `%realmines_...%` placeholders |
| Multiverse-Core, My_Worlds, WorldManager, [RealRegions](https://github.com/joserodpt/RealRegions) | World loading order, so mines in custom worlds load correctly |
| [RealPermissions](https://github.com/joserodpt/RealPermissions) | Permission integration |
| CrazyEnchantments | Compatibility with its custom block breaking |

----

## Installation

1. Install WorldEdit (or FAWE) and restart the server.
2. Drop `RealMines-x.x.jar` into `plugins/`.
3. Start the server. RealMines generates:

```
plugins/RealMines/
├── config.yml          # global settings
├── language.yml        # every message the plugin sends
├── sql.yml             # database settings for stats and achievements
├── achievements.yml    # the achievement list
├── RealMines.db        # SQLite database (default driver)
├── mines/              # one .yml per mine
└── schematics/         # schematics available to schematic mines
```

4. Open `/rm panel` in-game and start creating mines.

----

## Getting Started

### Creating a block mine

1. Select the mine region with WorldEdit (`//wand`, then left-click one corner and right-click the opposite one).
2. Run `/rm create <name> blocks`.
3. RealMines lists every material it found inside the selection and asks in chat whether to add them as mine blocks.
   Type `yes` to add them all (each at 10%), or `cancel` to start with plain stone.
4. Open the mine with `/rm mine <name>` to set the reset mode, icon, colour and teleport point.
5. Open `/rm blocks <name>` to tune block percentages, depth ranges and break actions.

The teleport point defaults to where you were standing when you created the mine. Change it any time with
`/rm settp <name>`.

### Creating a farm mine

1. Select the region the crops should grow in. If the selection is more than one block tall, RealMines uses the top
   layer for crops and the layer below for soil.
2. Run `/rm create <name> farm`.
3. The mine starts with wheat. Open `/rm blocks <name>` to swap in other crops and set their percentages and growth age.

Supported crops: `WHEAT`, `CARROT`, `POTATO`, `BEETROOT`, `MELON_SEED`, `PUMPKIN_SEED`, `NETHER_WART`, `SUGAR_CANE`,
`CACTUS` and `AIR`. Each one knows what it needs underneath it (farmland, soul sand, sand, grass), and
`placeFarmLandBelowCrop` in `config.yml` controls whether RealMines places that soil for you.

### Creating a schematic mine

1. Stand where the schematic should be pasted from and select a region with WorldEdit — the region is what gets cleared
   on reset.
2. Run `/rm create <name> schematic`.
3. A file browser GUI opens on `plugins/RealMines/schematics/`. Pick a `.schem` or `.schematic` file. Files chosen from
   outside the RealMines folder are copied into it.
4. Every reset clears the region and pastes the schematic again.

----

## Mine Types

| Type | What it does | Reset behaviour |
|---|---|---|
| `BLOCKS` | Classic prison mine — a cuboid filled with blocks at configured percentages | Refills the cuboid from the active block set |
| `FARM` | A crop field | Replants crops on their soil at the configured growth age |
| `SCHEMATIC` | A WorldEdit schematic | Clears the region and pastes the schematic again |

All three share the same features: reset modes, signs, break actions, break permissions, freezing, highlighting, icons
and colours.

----

## Resetting Mines

A mine can reset from any of these:

* **By time** — a fixed interval in seconds. Set it with `/rm setcountdown <name> <seconds>` or in the mine GUI.
  `/rm resetcountdown <name>` restarts the current countdown without resetting the mine.
* **By percentage** — resets once a given percentage of the mine has been mined.
* **Reset tasks** — a shared timer that resets several mines together. See `/rmrt` in the [commands](#commands) section.
* **Manually** — `/rm reset <name>`, or from the mine GUI.
* **From the API** — `mine.reset(RMine.ResetCause.PLUGIN)`.

Related behaviour:

* **Countdown announcements** — `announceTimes` in `config.yml` lists the seconds-remaining marks that get announced
  (30, 20, 10, 5, 4, 3, 2, 1 by default).
* **Silent mines** — `/rm silent <name>` stops a mine from broadcasting its resets; `/rm silentall <true|false>` does it
  for every mine at once.
* **Reset commands** — the `reset.commands` list in a mine's file runs from console on every reset.
* **Player safety** — with `teleportPlayers` enabled, players inside the mine are teleported to the mine's teleport point
  before it refills.
* **Empty servers** — `resetMinesWhenNoPlayers` decides whether timers keep running with nobody online.
* **Freezing** — `/rm freeze <name>` makes the mine's blocks unbreakable without touching its timer.
* **Highlighting** — `/rm highlight <name>` outlines the mine with particles in the mine's colour, which is handy for
  checking bounds.

----

## Block Sets, Percentages and Depth Ranges

Every mine holds one or more **block sets**. A block set is a named group of materials, each with a spawn percentage,
its own icon and description. The `default` set is created for you.

**Block sets mode** decides which set is used on each reset:

| Mode | Behaviour |
|---|---|
| `INCREMENTAL` | Walks through the sets in order, one per reset |
| `RANDOM` | Picks a random set each reset |
| `NONE` | Always uses the first set |

Per material, you can configure:

* **Percentage** — how much of the mine it fills.
* **Depth range** (`min`/`max`, block mines only) — restricts the material to a slice of the mine. `0.0`–`0.25` keeps a
  material in the first quarter measured from the mine's depth face. The face itself is configurable per mine
  (`Up` by default, but any of the six faces works), so you can build layered mines that read from the top, bottom or a
  side.
* **Disabled vanilla drop** — the block breaks but drops nothing, leaving break actions to hand out the rewards.
* **Disabled block mining** — the block cannot be broken at all.
* **Break actions** — see below.

`useButtonGUIForPercentages` in `config.yml` switches between a button-based percentage picker and typing the value in
chat.

----

## Break Actions

Break actions fire when a player breaks a specific material inside a mine. Each action has a **chance** (`100.0` always
fires) and a **value**.

| Type | Value | Notes |
|---|---|---|
| `GIVE_MONEY` | amount | Requires Vault and an economy plugin |
| `EXECUTE_COMMAND` | the command, without a leading `/` | Runs from console. `%player%` and `%blockloc%` are replaced |
| `GIVE_ITEM` | a serialized item | Placed straight into the player's inventory |
| `DROP_ITEM` | a serialized item | Dropped at the broken block |

Actions are managed from the mine's block GUI. The `Discard break action messages` per-mine setting silences the
feedback messages if a block fires actions constantly.

The same format is reused for achievement rewards in `achievements.yml`.

----

## Mine Signs

Place a sign with `[RealMines]` (or `[rm]`) on the first line, the mine's name on the second and a modifier on the
third. RealMines rewrites the sign and keeps it updated.

```
[RealMines]
mine_a
tl
```

| Modifier | Shows |
|---|---|
| `tl` | Time left until the next reset, formatted |
| `sl` | Seconds left until the next reset |
| `b` | Progress bar of blocks remaining |
| `pb` | Percentage progress bar |
| `pm` | Percentage of the mine mined |
| `pl` | Percentage of the mine left |
| `bm` | Number of blocks mined |
| `br` | Number of blocks remaining |

`tl` and `sl` only display a value while the mine has a time-based reset running.

----

## Stats, Achievements and Leaderboards

RealMines tracks how many blocks each player mines inside mines, per material, and uses that to drive achievements and
leaderboards.

* `/rm stats` shows a player's own totals; `/rm viewstats <player>` shows someone else's, including offline players.
* `/rm achievements` opens the achievement board; `/rm viewachievements <player>` opens another player's.
* `/rm top` opens the leaderboard GUI, which can be browsed per material as well as by overall total.

Counting happens in memory and is flushed to the database on an interval (`Stats.Flush-Interval-Seconds`), when a player
quits, and on shutdown — so a crash can only lose the last interval's worth of progress. Setting `Stats.Enabled` to
`false` stops all tracking while keeping whatever is already stored.

**Achievements** are defined in `achievements.yml`. Each entry has an id (the id is what gets stored in the database, so
renaming a key lets players earn it again), a display name, an icon, a description, a goal, and optional rewards using
the same format as break actions. Two types exist:

| Type | Counts |
|---|---|
| `TOTAL_BLOCKS` | Every block mined inside any mine |
| `MATERIAL` | Only blocks of the configured `Material` |

**Database** settings live in `sql.yml`. Supported drivers are `SQLITE` (default), `MYSQL`, `MARIADB`, `POSTGRESQL` and
`SQLSERVER`. Changes to `sql.yml` need a full server restart — `/rm reload` will not reconnect.

----

## Commands

Main command: `/realmines`, aliased to `/mine` and `/rm`.

<details open>
<summary><b>Mine management</b></summary>

| Command | Aliases | Description | Permission |
|---|---|---|---|
| `/rm` | | Show the plugin version | — |
| `/rm panel` | `mines`, `p` | Open the main GUI | `realmines.admin` |
| `/rm list` | `l` | List every mine in chat | `realmines.admin` |
| `/rm create <name> <type>` | | Create a mine from your WorldEdit selection. Type is `blocks`/`b`, `farm`/`f` or `schematic`/`schem`/`s` | `realmines.admin` |
| `/rm mine <name>` | `m` | Open that mine's GUI | `realmines.admin` |
| `/rm blocks <name>` | | Open the mine's block set GUI | `realmines.admin` |
| `/rm rename <name> <new_name>` | `rn` | Rename a mine | `realmines.admin` |
| `/rm delete <name>` | `del` | Delete a mine | `realmines.admin` |
| `/rm setbounds <name>` | | Replace the mine's region with your current WorldEdit selection | `realmines.admin` |
| `/rm settp <name>` | | Set the mine's teleport point to where you're standing | `realmines.admin` |
| `/rm tp <name>` | | Teleport to a mine | `realmines.tp` + `realmines.tp.<name>` |

</details>

<details open>
<summary><b>Resets</b></summary>

| Command | Aliases | Description | Permission |
|---|---|---|---|
| `/rm reset <name>` | `r` | Reset a mine now | `realmines.reset` |
| `/rm clear <name>` | `c` | Empty a mine (fill it with air) | `realmines.admin` |
| `/rm setcountdown <name> <seconds>` | | Set the time-based reset interval | `realmines.admin` |
| `/rm resetcountdown <name>` | | Restart the current countdown | `realmines.admin` |
| `/rm freeze <name>` | | Toggle whether the mine's blocks can be broken | `realmines.admin` |
| `/rm silent <name>` | `s` | Toggle reset broadcasts for one mine | `realmines.silent` |
| `/rm silentall <true\|false>` | `sa` | Toggle reset broadcasts for every mine | `realmines.silent` |
| `/rm starttasks` | | Start all reset timers | `realmines.admin` |
| `/rm stoptasks` | | Stop all reset timers | `realmines.admin` |

</details>

<details open>
<summary><b>Stats and achievements</b></summary>

| Command | Aliases | Description | Permission |
|---|---|---|---|
| `/rm achievements` | `ach` | Open your achievement board | `realmines.achievements` |
| `/rm stats` | | Show your mining stats | `realmines.achievements` |
| `/rm top` | `leaderboard`, `lb` | Open the leaderboard GUI | `realmines.top` |
| `/rm viewachievements <player>` | `vach` | Open another player's achievement board | `realmines.achievements.others` |
| `/rm viewstats <player>` | `vstats` | Show another player's stats | `realmines.achievements.others` |

</details>

<details open>
<summary><b>Utility</b></summary>

| Command | Aliases | Description | Permission |
|---|---|---|---|
| `/rm settings` | | Open the global settings GUI | `realmines.admin` |
| `/rm highlight <name>` | | Toggle the mine's particle outline | `realmines.admin` |
| `/rm reload` | `rl` | Reload the configuration files | `realmines.admin` |
| `/rm import <converter>` | `imp`, `conv`, `convert` | Import mines from another plugin | `realmines.import` |

</details>

<details>
<summary><b>Reset tasks — <code>/realminesresettask</code></b></summary>

Aliased to `/minesresettask` and `/rmrt`. A reset task is a shared timer that resets every mine linked to it.

| Command | Description | Permission |
|---|---|---|
| `/rmrt` | Show the plugin version | — |
| `/rmrt create <name> <delay>` | Create a task that fires every `delay` seconds | `realmines.admin` |
| `/rmrt remove <name>` | Delete a task | `realmines.admin` |
| `/rmrt link <taskname> <mine>` | Add a mine to a task | `realmines.admin` |
| `/rmrt unlink <taskname> <mine>` | Remove a mine from a task | `realmines.admin` |

</details>

----

## Permissions

| Permission | Grants |
|---|---|
| `realmines.admin` | Every administrative command and GUI, plus update notifications on join |
| `realmines.reset` | `/rm reset` |
| `realmines.tp` | `/rm tp` |
| `realmines.tp.<mine>` | Teleporting into that specific mine — required in addition to `realmines.tp` |
| `realmines.silent` | `/rm silent` and `/rm silentall` |
| `realmines.import` | `/rm import` |
| `realmines.achievements` | `/rm achievements` and `/rm stats` |
| `realmines.achievements.others` | `/rm viewachievements` and `/rm viewstats` |
| `realmines.top` | `/rm top` |
| `realmines.<mine>.break` | Breaking blocks in that mine, when the mine's `Mine break permission` setting is on |

Operators get every permission by default, as usual in Bukkit.

----

## PlaceholderAPI

Install PlaceholderAPI and the `realmines` expansion registers itself automatically.

**Per mine** — replace `<mine>` with the mine's name:

| Placeholder | Returns |
|---|---|
| `%realmines_totalblocks_<mine>%` | Total block capacity of the mine |
| `%realmines_minedblocks_<mine>%` | Blocks mined since the last reset |
| `%realmines_remainingblocks_<mine>%` | Blocks left |
| `%realmines_perminedblocks_<mine>%` | Percentage mined |
| `%realmines_perremainingblocks_<mine>%` | Percentage left |
| `%realmines_secondsleft_<mine>%` | Seconds until the next reset, or `-1` |
| `%realmines_timeleft_<mine>%` | Formatted time until the next reset, or `-1` |
| `%realmines_bar_<mine>%` | Progress bar of blocks remaining |
| `%realmines_percentage_bar_<mine>%` | Percentage progress bar |

An unknown mine name returns `No mine named: <mine>`.

**Player stats and achievements** — resolved from the in-memory cache, so an offline player returns an empty string:

| Placeholder | Returns |
|---|---|
| `%realmines_stats_totalmined%` | Total blocks the player has mined |
| `%realmines_stats_mined_<MATERIAL>%` | Blocks of that material the player has mined |
| `%realmines_achievements_unlocked%` | Achievements the player has unlocked |
| `%realmines_achievements_total%` | Achievements configured on the server |
| `%realmines_achievements_percentage%` | Completion percentage, rounded |

**Leaderboards** — `<n>` is the position, starting at 1:

| Placeholder | Returns |
|---|---|
| `%realmines_top_name_<n>%` | Name of the player in position `n` by total blocks mined |
| `%realmines_top_value_<n>%` | That player's total |
| `%realmines_top_name_<MATERIAL>_<n>%` | Name of the player in position `n` for that material |
| `%realmines_top_value_<MATERIAL>_<n>%` | That player's count for the material |

Leaderboard placeholders read from a snapshot refreshed in the background, so they never block the server. Positions
beyond the number of tracked players return an empty string.

----

## Configuration Files

Every file RealMines generates is commented in place, so the file itself is the reference. A short summary:

| File | Contents |
|---|---|
| `config.yml` | Global toggles: prefix, teleport and action bar messages, reset announcements, WorldEdit usage for block placement, stats tracking and leaderboard size |
| `language.yml` | Every message, title, action bar and sign label the plugin sends |
| `sql.yml` | Database driver and credentials for stats and achievements. **Requires a restart to apply** |
| `achievements.yml` | The achievement list, their goals and rewards |
| `mines/<name>.yml` | One mine — see [Mine File Format](#mine-file-format) |

Most of `config.yml` is also editable in-game through `/rm settings`, which is the safer route since it saves and applies
immediately.

A few toggles worth knowing about:

| Setting | Effect |
|---|---|
| `teleportPlayers` | Teleport players out of a mine before it refills |
| `sendMinedItemsToInventory` | Send drops straight to the player's inventory instead of the ground |
| `resetMinesWhenNoPlayers` | Keep reset timers running when the server is empty |
| `useWorldEditForBlockPlacement` | Use WorldEdit to fill mines — much faster on large mines |
| `ignoreAirBlocksSchematicPasting` | Skip air blocks when pasting schematic mines |
| `disableMineResetOnServerStart` | Don't reset every mine when the server boots |
| `disableMineClearingWhenDeleting` | Leave the blocks in place when a mine is deleted |
| `broadcastResetMessageOnlyInWorld` | Announce resets only to players in the mine's world |

----

## Importing From Other Plugins

`/rm import <converter>` reads another plugin's mines and recreates them as RealMines mines.

| Converter | Source |
|---|---|
| `CataMines` | CataMines |
| `JetsPrisonMines` | JetsPrisonMines |
| `MineResetLite` | MineResetLite |

The source plugin's configuration has to still be present on the server. Back up `plugins/RealMines/mines/` before
importing, and check the console — mines that fail to convert are logged with a reason.

----

## Mine File Format

Each mine is a single YAML file in `plugins/RealMines/mines/`. You normally never edit these by hand, but they are
straightforward if you need to.

<details>
<summary>Annotated example</summary>

```yaml
name: mine_a
displayName: '&bMine A'
type: BLOCKS              # BLOCKS, FARM or SCHEMATIC
world: world
pos1: 100;64;100          # opposite corners of the cuboid
pos2: 120;80;120
teleport: 110;81;110;90.0;0.0   # where /rm tp sends players (x;y;z;yaw;pitch)
icon: DIAMOND_ORE         # GUI icon
color: BLUE               # highlight colour
schematic: ''             # SCHEMATIC mines only

reset:
  silent: false           # don't broadcast this mine's resets
  commands: []            # commands run from console on every reset
  time:
    active: true
    value: 300            # reset every 300 seconds
    countdown: 300
  percentage:
    active: false
    value: 50             # reset once 50% has been mined

settings:
  break-permission: false             # require realmines.mine_a.break
  discard-break-action-messages: false
  block-sets-mode: INCREMENTAL        # INCREMENTAL, RANDOM or NONE
  depth-direction: Up                 # face the depth ranges are measured from

block-sets:
  default:
    icon: STONE
    description: 'Default set'
    blocks:
      STONE:
        percentage: 0.7
        disabled-vanilla-drop: false
        disabled-block-mining: false
        depth:
          min: 0.0                    # top 40% of the mine
          max: 0.4
      DIAMOND_ORE:
        percentage: 0.3
        depth:
          min: 0.6
          max: 1.0
        break-actions:
          action-1:
            type: GIVE_MONEY
            chance: 100.0
            value: 50.0

faces: {}                 # a material per mine face, set through the mine's Faces GUI
signs: []                 # signs bound to this mine, written by the plugin
```

For `FARM` mines the block keys are crop names (`WHEAT`, `CARROT`, …) and each takes an extra `age` value for the growth
stage. For `SCHEMATIC` mines the block entries only carry the drop settings and break actions, since the layout comes
from the schematic.

</details>

----

## API

RealMines ships a separate API module, `RealMinesAPI`, which the plugin jar already contains at runtime.

### Adding the dependency

Released versions are published through [JitPack](https://jitpack.io/#joserodpt/RealMines) - no manual `mvn install` needed.

**Maven**

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.joserodpt.RealMines</groupId>
    <artifactId>RealMinesAPI</artifactId>
    <version>v1.9</version>
    <scope>provided</scope>
</dependency>
```

**Gradle**

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.joserodpt.RealMines:RealMinesAPI:v1.9'
}
```

Any git tag, branch (`master-SNAPSHOT`) or commit hash works as the version.

<details>
<summary>Building from source instead</summary>

```bash
git clone https://github.com/joserodpt/RealMines.git
cd RealMines
mvn clean install
```

That installs `joserodpt:RealMinesAPI:1.9` into your local `~/.m2` repository, which you can then depend on with those coordinates and no extra repository.

</details>

Then add RealMines to your `plugin.yml`:

```yaml
depend: [ RealMines ]     # or softdepend, if the integration is optional
```

### Entry point

Everything hangs off `RealMinesAPI`:

```java
var rmAPI = RealMinesAPI.getInstance();

rmAPI.getMineManager();          // mines
rmAPI.getMineResetTasksManager();// shared reset timers
rmAPI.getDatabaseManager();      // player stats
rmAPI.getAchievementsManager();  // achievements
rmAPI.getEconomy();              // Vault economy, if present
rmAPI.getVersion();
rmAPI.reload();
```

`getInstance()` returns `null` until RealMines has finished loading. If your plugin only softdepends on RealMines, wait
for `RealMinesPluginLoadedEvent` instead of touching the API in `onEnable`.

### Working with mines

```java
var mineManager = RealMinesAPI.getInstance().getMineManager();

Map<String, RMine> mines = mineManager.getMines();
RMine mine = mineManager.getMine("mine_a");

if (mine != null) {
    mine.getDisplayName();
    mine.getType();               // BLOCKS, SCHEMATIC or FARM
    mine.getBlockCount();
    mine.getMinedBlocks();
    mine.getRemainingBlocks();
    mine.getRemainingBlocksPer(); // percentage
    mine.getCountdown();          // seconds until the next reset, or null
    mine.isFreezed();
    mine.isSilent();

    mine.reset(RMine.ResetCause.PLUGIN);
}

// which mine, if any, contains a block
RMine at = mineManager.getMineWithBlock(block);
```

### Events

| Event | Fired when | Cancellable |
|---|---|---|
| `RealMinesPluginLoadedEvent` | RealMines has finished loading and the API is safe to use | no |
| `RealMinesMineChangeEvent` | A mine is added, removed or modified — see `getChangeOperation()` | no |
| `RealMinesOnMineResetEvent` | A mine is about to reset — `getResetCause()` is `COMMAND`, `PLUGIN`, `TIMER`, `CREATION` or `IMPORT` | yes |
| `RealMinesBlockBreakEvent` | A block inside a mine is broken or changed | via `getCancellable()` |
| `RealMinesPlayerUnlockAchievementEvent` | A player unlocks an achievement | yes |

```java
@EventHandler
public void onMineReset(final RealMinesOnMineResetEvent e) {
    if (e.getResetCause() == RMine.ResetCause.TIMER && e.getMine().getName().equals("event_mine")) {
        e.setCancelled(true);
    }
}

@EventHandler
public void onMineBlockBreak(final RealMinesBlockBreakEvent e) {
    getLogger().info(e.getPlayer().getName() + " broke " + e.getMaterial() + " in " + e.getMine().getName());
}
```

### Player stats and achievements

```java
var db = RealMinesAPI.getInstance().getDatabaseManager();

// cached, non-blocking — returns null for players who aren't loaded
RMPlayerStats stats = db.getStats(player.getUniqueId());
if (stats != null) {
    stats.getTotalBlocksMined();
    stats.getBlocksMined(Material.DIAMOND_ORE);
}

// offline players: loaded asynchronously
db.loadStats(uuid, loaded -> { /* ... */ });

// leaderboard snapshots
List<RMPlayerData> top = db.getTopTotalBlocksMined(10);
List<RMPlayerBlockStat> topDiamond = db.getTopBlocksMined(Material.DIAMOND_ORE, 10);

var achievements = RealMinesAPI.getInstance().getAchievementsManager();
achievements.getAchievements();
achievements.getUnlockedCount(stats);
```

`getStats` and the `getTop...` methods read from memory and are safe on the main thread. Anything that hits the database
(`loadStats`, `findPlayer`, `flush`) is asynchronous or explicitly documented as blocking — don't call the blocking
variants from the main thread.

----

## Building From Source

```bash
git clone https://github.com/joserodpt/RealMines.git
cd RealMines
mvn clean package
```

The plugin jar lands in `realmines-plugin/target/`. Java 16 or newer is required to build; CI builds on Java 21.

`compile.sh` is a convenience wrapper that builds the plugin and copies the jar into a local test server — edit
`TARGET_DIR` inside it before using it.

----

## Pictures

![img](https://i.imgur.com/35gJCNr.png)
![img2](https://i.imgur.com/DBRwcnl.png)
![img3](https://i.imgur.com/boHe3s9.gif)
![img4](https://i.imgur.com/og8if9B.png)
![img5](https://i.imgur.com/T9yXh0y.png)

----

## Links

* [SpigotMC](https://www.spigotmc.org/resources/73707/)
* [Discord Server](https://discord.gg/t7gfnYZKy8)
* [bStats](https://bstats.org/plugin/bukkit/RealMines/10574)
* [Issue tracker](https://github.com/joserodpt/RealMines/issues)

Contributions are welcome — open an issue to discuss larger changes first. RealMines is released under the
[MIT License](LICENSE).
