# NightNotNeeded
This is a (...probably yet another) little Bukkit/Spigot (i.e. Minecraft Server) plugin which reduces the amount of players needed to skip the night by sleeping.

## Disclaimer
This is a one-off plugin which will probably not receive any special treatment by me - that is, not much more than half a day of writing the code down.

## Config
There are two config settings you can change:

* `number_asleep`: The required number of players that need to be sleeping in order for the night to be skipped. This value is loaded as a string, but may only represent integral numbers or integral percentages, i.e. `[0-9]+` or `[0-9]+%` in regex-terms. A fully integral value sets a fixed value, whereas a percentage sets a value relative to the number of online players. The value will be clamped between 1 and the number of online players on the server. For example:
  * `number_asleep: '10'`: 10 Players must be asleep
  * `number_asleep: '44%'`: 44% of the Players that are currently online must be asleep
* `rounding_mode`: This can either be `floor`, `round` or `ceil`, the default value is `round`. If any invalid value is set here, the default value will be used instead. The rounding mode determines how non-integral results will be converted to integral ones. Here again, the value is bounded by 1 and the number of online players on the server.
  * `floor`: Returns the nearest integral value _lower_ than the non-integral one.
  * `ceil`: Returns the nearest integral value _higher_ than the non-integral one.
  * `round`: The default mathematical rounding function, i.e. `ceil` at and over a fraction of .5, `floor` before that.
 * `overworld_only`: A boolean value that changes the behavior of the calculation of online players. If set to `true`, only players that currently are on the overworld are considered in the calculation.

## Commands
The plugin provides two main commands which are prefixed by a `nnn`:
* `\nnn reload`: Reloads the config file e.g. if a server admin changed it and wants the settings to be applied.
* `\nnn set <number_asleep|rounding_mode|overworld_only> <value>` sets the given config property to the desired value. If an invalid value is given, an error message is shown. There is no need for apostrophes when typing the value.
All commands have autocomplete functionality ingame.
The commands require the `nightnotneeded.commands` permission, which defaults to Server-Operator-only.
