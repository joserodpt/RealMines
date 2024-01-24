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

Welcome to the **RealMines plugin**! This is a brand new mine management plugin. Coded in the 1.14 codebase, it's aim is to provide Server Owners and Players with a fast and reliable mines system.

----

## Features
* YAML Configuration
* Simple and performant GUI interface
* Reset System (by time or by percentages)
* Search function for Adding Blocks
* GUI for managing individual Mines
* Player Input System to accept input from the user

----

## Pictures
![img](https://i.imgur.com/35gJCNr.png)
![img2](https://i.imgur.com/DBRwcnl.png)
![img3](https://i.imgur.com/boHe3s9.gif)
![img4](https://i.imgur.com/og8if9B.png)
![img5](https://i.imgur.com/T9yXh0y.png)

----

## Requirements
RealMines requires [WorldEdit](https://dev.bukkit.org/projects/worldedit) or [FAWE](https://www.spigotmc.org/resources/fastasyncworldedit.13932/) to work.

----

## API
You can access the RealMines API via the RealMinesAPI class:

```java
var rmAPI = RealMinesAPI.getInstance();
```

You can get the list of Mines as follows:

```java
var rmAPI = RealMinesAPI.getInstance();
rmAPI.getMineManager().getMines() and that will give you a Map<String, RMine> for you to discover.
```
There are also two events from this API: [RealMinesMineChangeEvent.java](realmines-api%2Fsrc%2Fmain%2Fjava%2Fjoserodpt%2Frealmines%2Fapi%2Fevent%2FRealMinesMineChangeEvent.java) and [RealMinesPluginLoadedEvent.java](realmines-api%2Fsrc%2Fmain%2Fjava%2Fjoserodpt%2Frealmines%2Fapi%2Fevent%2FRealMinesPluginLoadedEvent.java) that can be listened to.

----

## Links
* [SpigotMC](https://www.spigotmc.org/resources/realmines-1-14-to-1-19-2.73707/)
* [Discord Server](https://discord.gg/t7gfnYZKy8)
* [bStats](https://bstats.org/plugin/bukkit/RealMines/10574)
