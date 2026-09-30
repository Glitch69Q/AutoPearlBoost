# AutoPearlBoost — Fabric 26.2

Client-side Fabric 26.2 mod that detects your own thrown Ender Pearl, predicts its path, and automatically aims/uses a Wind Charge to try to intersect the pearl.

## Build
Requires Java 25 and Gradle. Run:

`gradlew.bat build`

The compiled jar will be in `build/libs/`.

For the Mod Menu config button, install Mod Menu 20.0.3 for Minecraft 26.2.

## Important
This is a client-side automation mod. Whether the server accepts the automated item use depends on the server's normal movement/item-use validation. The trajectory model is intentionally configurable because exact projectile timing can vary with latency and server behavior.
