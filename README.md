# No Deley

Fabric client-side mod for Minecraft 26.2.

## Build
Requires Java 25 and Gradle. Run:

`gradle build --no-daemon`

Target dependencies:
- Minecraft 26.2
- Fabric Loader 0.19.5
- Fabric API 0.160.0+26.2
- Mod Menu 20.0.2

The mod tracks the player's own thrown Ender Pearl, predicts its trajectory, selects a Wind Charge from the hotbar, aims at a predicted intercept point, uses the item, and restores the previous slot/view. Downward aim is configurable via `downwardAimDegrees` (default 2.0).
