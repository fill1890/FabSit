## FabSit: Sit mod for Fabric

Ever wanted to use `/sit` on a fabric server? This mod is for you!

Required server-side. Can be installed client-side for local translation (maybe more features Soon).

The mod currently implements `/sit`, `/lay` and `/spin`.

The mod eventually aims to provide near-feature parity to the [GSit](https://www.spigotmc.org/resources/gsit-modern-sit-seat-and-chair-lay-and-crawl-plugin-1-13-x-1-19-x.62325/)
mod for Spigot however implementation is ongoing. Exact behaviour matching is not guaranteed.

See the [Github](https://github.com/fill1890/FabSit) page for configuration and internationalization details.

Available on [Github](https://github.com/fill1890/FabSit), [Modrinth](https://modrinth.com/mod/fabsit) and
[CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabsit)

Requires the Fabric API

## Usage

Simply add the jar file to your server mods directory, and optionally to the client directory.

Players can then use `/sit` to sit, `/lay` to lie down or `/spin` to start spinning.

## Permissions

Permissions to pose are granted to all players by default.

### `fabsit.commands` node

`sit, lay, spin:` specific permission to use appropriate poses

### `fabsit.reload`
Permission to reload the config file from disk. Requires op level 2 by default

