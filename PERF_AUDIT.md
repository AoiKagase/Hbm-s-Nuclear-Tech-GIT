# Performance Audit

Date: 2026-06-17

## Build Baseline

- Task attempted before code changes: `.\gradlew.bat build`
- Result: failed before project configuration completed.
- Initial sandboxed run could not download the Gradle distribution due network restrictions.
- Approved rerun downloaded/used Gradle, but configuration failed because Gradle could not resolve plugin `com.gtnewhorizons.retrofuturagradle:1.4.9`.
- Repositories searched by Gradle: GTNH Maven, Gradle Plugin Portal, Maven Central, Maven Local.

## Post-Change Build

- Task attempted after code changes: `.\gradlew.bat build --stacktrace`
- Result: still blocked during configuration, now on `com.gtnewhorizons.retrofuturagradle:1.4.9` resolution in `build.gradle` line 36 when run in this offline sandbox.
- I also tested a direct Gradle 8.12 launch with a local JDK 21 and a writable project cache directory; the build still stopped before compilation because the plugin could not be resolved from the available local caches/repositories.
- Because Gradle never reached source compilation, compile-time validation of the Java edits is still pending until the build plugin can be resolved in this environment.

## Implemented Optimizations

| File | Method | Hot spot | Risk | Fix | Implemented |
| --- | --- | --- | --- | --- | --- |
| `src/main/java/com/hbm/tileentity/machine/TileEntityMachineGasCent.java` | `update()` | Re-subscribed energy connections every tick. | Low | Throttle connection refresh to every 20 ticks. | Yes |
| `src/main/java/com/hbm/tileentity/machine/TileEntityMachineGasCent.java` | `update()` | Sent `LoopedSoundPacket` every tick while processing. | Low | Send on active transition and then refresh every 20 ticks while active. | Yes |
| `src/main/java/com/hbm/tileentity/machine/TileEntityMachineGasCent.java` | `detectAndSendChanges()` | Built NBT and sent machine state every tick, even for progress/power-only changes. | Medium | Send immediately for processing/tank state changes; coalesce power/progress to 5 tick intervals with pending sync. | Yes |
| `src/main/java/com/hbm/tileentity/machine/TileEntityMachineGasCent.java` | `process()` | Expanded weighted outputs into a temporary list, shuffled it, then selected one entry. | Low | Replace with cumulative-weight random selection using existing total weights. | Yes |
| `src/main/java/com/hbm/tileentity/machine/TileEntityMachineAssembler.java` | `update()` | Re-subscribed energy connections every tick. | Low | Throttle connection refresh to every 20 ticks. | Yes |
| `src/main/java/com/hbm/tileentity/machine/TileEntityMachineAssembler.java` | `update()` | Built NBT and sent GUI/render state every tick. | Medium | Send immediately for progress-state/recipe/max-progress changes; coalesce power/progress to 5 tick intervals with pending sync. | Yes |
| `src/main/java/com/hbm/tileentity/machine/TileEntityMachineChemplant.java` | `update()` | Re-subscribed energy connections every tick. | Low | Throttle connection refresh to every 20 ticks. | Yes |
| `src/main/java/com/hbm/tileentity/machine/TileEntityMachineChemplant.java` | `detectAndSendChanges()` | Sent looped sound, progress state, power, and all tanks every tick. | Medium | Send sound every 20 ticks while active; send state on transition; coalesce power to 5 ticks; send tanks only when changed. | Yes |
| `src/main/java/com/hbm/tileentity/machine/TileEntityMachineChemplant.java` | `getCapability()` | Allocated a new `ChemplantFluidHandler` wrapper on every fluid capability query. | Low | Cache and reuse a single handler over the existing tank/type arrays. | Yes |

## Suspected Hot Spots Left Unfixed

| File | Method | Why expensive | Risk | Proposed fix | Implemented |
| --- | --- | --- | --- | --- | --- |
| `src/main/java/com/hbm/tileentity/machine/TileEntityMachineChemplant.java` | `update()` | Repeated recipe/template lookups and cloned inventory checks in the hot path. | Medium/High | Cache template-derived recipe data and invalidate on template/inventory/tank changes. | No |
| `src/main/java/com/hbm/tileentity/machine/TileEntityMachineAssembler.java` | `update()` | Repeated template output/recipe lookups and cloned inventory checks. | Medium | Cache recipe/output for the current template and invalidate on template/inventory changes. | No |
| `src/main/java/com/hbm/tileentity/machine/TileEntityMachineChemfac.java` | `update()` | Builds large NBT and sends `LoopedSoundPacket`/state every tick. | Medium | Add change detection and low-frequency sync similar to Chemplant. | No |
| `src/main/java/com/hbm/tileentity/machine/TileEntityFEL.java` | `update()` | Sends looped sound and NBT every server tick while active. | Medium | Throttle sound and split state-change sync from meter sync. | No |
| `src/main/java/com/hbm/tileentity/machine/TileEntityMachineMiningLaser.java` | `update()` | Sends looped sound and large NBT every tick. | Medium/High | Throttle sound; only send target/beam changes immediately and progress periodically. | No |
| `src/main/java/com/hbm/tileentity/machine/oil/*` | `update()` methods | Several oil machines send power/fluid packets and refresh connections frequently. | Medium | Apply the same dirty/periodic sync pattern case-by-case. | No |

## Behavior Risks

- GUI meters for Gas Centrifuge and Assembler now update at most every 5 ticks for power/progress-only changes instead of every tick.
- Gas Centrifuge and Chemplant looped sounds now refresh every 20 ticks while active instead of every tick. The packet still starts sound on transition to active.
- Energy connection discovery for Gas Centrifuge, Assembler, and Chemplant now refreshes every 20 ticks. Existing connections remain refreshed periodically, but very rapid cable placement/removal may take up to one second to be noticed.
- Chemplant fluid capability now reuses one wrapper. It references the same mutable tank and tank type arrays as before, so behavior should remain equivalent.

## Manual Test Checklist

- Open Gas Centrifuge, Assembler, and Chemplant GUIs.
- Verify progress bars update while recipes are running.
- Verify energy meters update after charging/discharging.
- Verify fluid tanks update when filling, draining, and processing.
- Verify item input/output automation still inserts and extracts correctly.
- Run Assembler recipes with and without templates.
- Run Gas Centrifuge recipes with and without the centrifuge upgrade.
- Run Chemplant recipes with item and fluid inputs/outputs.
- Verify machine sounds start when processing begins and stop when processing ends.
- Unload and reload chunks containing running machines.
- Restart the server and reload the world.
- Test machines connected to energy cables and fluid pipes before and after rotating/configuring nearby blocks.
- Test a large array of Gas Centrifuges, Assemblers, and Chemplants and compare server tick stability.
