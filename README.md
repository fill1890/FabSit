## FabSit: Sit mod for Fabric

Ever wanted to use `/sit` on a fabric server? This mod is for you!

Required server-side, but is optional client-side - currently allows for client-side translation
if installed. New client features may be added later.

The mod currently implements `/sit`, `/lay` and `/spin`.

The mod eventually aims to provide near-feature parity to the [GSit](https://www.spigotmc.org/resources/gsit-modern-sit-seat-and-chair-lay-and-crawl-plugin-1-13-x-1-19-x.62325/)
mod for Spigot however implementation is ongoing. Exact behaviour matching is not guaranteed.

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

## Configuration

Configuration file with default values is as follows:

```json5
{
  // Locale to use for server-side translation
  // Currently only supports en_us
  "locale": "en_us",
  // Allow poses underwater?
  "allow_posing_underwater": false,
  // Allow poses midair? Note that this will likely
  // interfere with fall damage if enabled
  "allow_posing_midair": false,
  // server-enabled poses. If LuckPerms is installed,
  // prefer using permissions instead as they are dynamic
  "allow_poses": {
    "sit": true,
    "lay": true,
    "spin": true
  }
}
```

## Internationalization

FabSit supports both server-side and client-side translation.

If the mod is installed on the client, the server will send a translation key for the client to translate into its own locale,
assuming the mod has support for it.

If not, the server will translate into the locale set in the config file.

Currently the mod is english-only - if you would like to contribute a translation feel free to open an issue or a pull request.