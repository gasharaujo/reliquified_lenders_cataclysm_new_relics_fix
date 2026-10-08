# Reliquified L_Ender 's Cataclysm new relics fix

Compatibility mod for Minecraft 1.21.1 / NeoForge. It rewrites the Relics
0.10 API references left in Reliquified L_Ender's Cataclysm 0.1.1 and bridges
the relic definitions, runtime calls, and removed motion packet to Relics 0.12.8.

Build 1.0.3 registers the reference rewrite through `META-INF/coremods.json`.
The Java compatibility mixins then provide the current Relics definitions and
runtime behavior.

Version 1.0.2 made the complete legacy Relics surface concrete only on the
add-on's `RECItem` base, preventing Curios default-method conflicts without
changing modern relic classes from other mods. Its verifier audits all 26
add-on classes, the inherited item hierarchy, all five relic definitions, API
types and member descriptors, ability activation, cooldown/stat/experience
bridges, ability ordering, and the old `maxLevel` to new `maxRank` conversion.

Version 1.0.3 restores the missing server-side cooldown tick for the Scouring
Eye's Pursuit ability. It also makes the Void Cloak return a wearer who falls
below the world to their last grounded position (or the Overworld spawn as a
fallback), instead of canceling void damage while leaving them falling forever.

Build and keep a versioned copy in `meusMods\builds`:

```powershell
.\build.ps1
```

Build and install only into the instance's `mods` directory:

```powershell
.\build.ps1 -Install
```
