## FabSit: Sit mod for Fabric

Ever wanted to use `/sit` on a fabric server? This mod is for you! It's even a server-only mod - no client-side installation necessary.

The mod currently implements `/sit`, `/lay` and `/spin`.

The mod eventually aims to provide near-feature parity to the [GSit](https://www.spigotmc.org/resources/gsit-modern-sit-seat-and-chair-lay-and-crawl-plugin-1-13-x-1-19-x.62325/)
mod for Spigot however implementation is ongoing. Exact behaviour matching is not guaranteed.

Available on [Github](https://github.com/fill1890/FabSit) and [Modrinth](https://modrinth.com/mod/fabsit)

Requires the Fabric API

## Usage

Simply add the jar file to your server mods directory, or client for single player worlds.

Players can then use `/sit` to sit, `/lay` to lie down or `/spin` to start spinning.

## Permissions

Permissions to pose are granted to all players by default.

### `fabsit.commands` node

`sit, lay, spin:` specific permission to use appropriate poses