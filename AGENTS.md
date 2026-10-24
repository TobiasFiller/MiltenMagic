# AGENTS.md

Guidance for AI agents working in this repository.

## Working Agreement: the human decides, the agent implements

The human owns design and scope decisions; the agent executes them. Do not silently pick an
interpretation when a request leaves room for one.

- **Ask first, then implement.** Whenever there is a real choice — naming, player-facing texts,
  advancement frame/icon/background, balance numbers (XP cost, cooldown, duration, radius),
  recipes, particles/look & feel, or which of several technical approaches to use — present 2-4
  concrete options with trade-offs and let the human pick before writing code.
- **Confirm the scope of a multi-part request** (e.g. "polish the spell") with a short list of
  what will and will not be included.
- **Small, unambiguous fixes** (compile errors, typos, a clearly wrong path or value, a crash)
  may be done directly, but call them out in the summary.
- **Do not expand scope.** If you spot an unrelated bug or improvement while working, report it
  and ask — do not fold it into the current change.
- **Outline larger refactors** (new event systems, API changes, moving packages) and wait for
  approval before starting.
- If something is ambiguous, prefer one short clarifying question over guessing and redoing work
  afterwards.
- Still verify your own work: `gradlew build` and, for gameplay changes, `gradlew
  runGameTestServer` before reporting back.

## Project

**Milten Magic** (`miltenmagic`) is a Minecraft mod for **NeoForge 1.21.1** written in Java 21.
It adds a Gothic-inspired magic system: spell scrolls, runes, a teleportation platform network,
tables, a stone pedestal, swampweed, mob effects, and a "Fire Mage" villager profession.

- Mod ID: `miltenmagic`
- Root package: `net.tobiasfiller.miltenmagic`
- Build system: Gradle with `net.neoforged.gradle.userdev` 7.0.170
- Versions live in `gradle.properties` (single source of truth). Do not hardcode MC/NeoForge versions elsewhere.
- License: MIT (see `LICENSE`)

## Design & Concept (Obsidian Vault)

Feature design and concept work does **not** live in this repo. It lives in the "Zweiter-Kopf"
Obsidian vault at:

`C:\Users\tfill\Documents\Zweiter-Kopf\Minecraft Stuff\Milten Magic`

Consult these notes before designing or implementing a feature, so the code matches the intended
design. Read-only by default — do not modify the vault unless explicitly asked.

Start from these two notes; they link onward to every other note in the vault:

- [Milten Magic](<C:/Users/tfill/Documents/Zweiter-Kopf/Minecraft Stuff/Milten Magic/0 Milten Magic.md>)
  — base note: overview, design pillars (Gothic-inspired, XP-based magic), planned content,
  crafting ideas, and links to the feature notes
- [Development Board MM](<C:/Users/tfill/Documents/Zweiter-Kopf/Minecraft Stuff/Milten Magic/1 Development Board MM.md>)
  — Kanban board: backlog, design, development, testing, done

Do not link individual feature notes from this file; they change frequently and are reachable
through the two notes above.

When a feature is implemented or its design changes, suggest updating the matching vault note
(but leave the actual edit to the user).

## Build & Run

Use the Gradle wrapper. On Windows use `gradlew.bat`, otherwise `./gradlew`.

```bash
./gradlew build                # compile + jar
./gradlew runClient            # dev client
./gradlew runServer            # dev server
./gradlew runData              # datagen -> src/generated/resources
./gradlew runGameTestServer    # headless game tests
```

- `org.gradle.daemon=false` is intentional; do not enable the daemon.
- Game tests are registered in `core/gametest/GameTestRegistration.java` and defined in
  `core/gametest/MiltenMagicGameTests.java`. They use the `miltenmagic:empty` structure template.
- The `data` run writes to `src/generated/resources`, which is registered as a resource source set.

## Source Layout

```
src/main/java/net/tobiasfiller/miltenmagic/
  MiltenMagic.java              mod entry point; registers all DeferredRegisters
  client/                       client-only setup and renderers
  common/
    block/                      Block subclasses
    blockentity/                BlockEntity subclasses
    entity/                     Entity + renderer
    item/                       creative tab, custom items
      helperClasses/            base classes (SpellItem, BuffSpellItem, UseSpellItem)
      spellItems/               concrete spell implementations
    mobeffect/                  custom MobEffects
    particle/                   custom particle types
  core/
    event/                      @EventBusSubscriber game-bus handlers
    gametest/                   GameTest registration + tests
    registry/                   DeferredRegister holders for every registry type
    utility/                    shared helpers
    world/feature/              (legacy) worldgen classes — see note below
```

Resources:

```
src/main/resources/
  META-INF/neoforge.mods.toml   mod metadata (versions are expanded from gradle.properties)
  pack.mcmeta
  assets/miltenmagic/           client assets: blockstates, models, textures, lang, sounds, particles
  data/miltenmagic/             server data: recipes, loot_tables, worldgen, tags, structures
```

## Conventions

- **Java 21**, 4-space indentation, one public class per file.
- Keep the `common/` vs `client/` split. Anything referencing client-only classes
  (`net.minecraft.client.*`, renderers) belongs in `client/` or must be guarded by
  `Dist.CLIENT` / `level.isClientSide`.
- **Registration is centralized**: add every new registry entry to the matching class in
  `core/registry/` (`ItemRegistry`, `BlockRegistry`, `BlockEntityRegistry`, `EntityRegistry`,
  `MobEffectRegistry`, `ParticleRegistry`, `SoundRegistry`, `VillagerRegistry`). Do not create
  ad-hoc `DeferredRegister`s elsewhere.
- Use `DeferredRegister` / `DeferredHolder`; reference registered objects via `.get()`.
- Event handlers use `@EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = ...)` with
  `static @SubscribeEvent` methods. Mod-bus lifetime events go in `client/` or a `Bus.MOD`
  subscriber; gameplay events use `Bus.GAME` (see `core/event/EventHandler.java`).
- All player-facing text is translatable: add keys to `assets/miltenmagic/lang/en_us.json` and
  build them with `Component.translatable("tooltip." + MiltenMagic.MOD_ID + ...)`.
- Recipe, loot table, blockstate and model JSON follow the vanilla 1.21 folder layout and use the
  `miltenmagic` namespace.

## 1.21.1 / NeoForge Pitfalls (important)

These have caused real bugs in this repo — preserve the existing solutions:

1. **Item data is not raw NBT.** Arbitrary item tags live in
   `DataComponents.CUSTOM_DATA` (`CustomData.of(tag)` / `stack.get(DataComponents.CUSTOM_DATA)`).
   `ItemStack#get` returns a detached copy — modify the copy, then write it back
   (see `TeleportationSpellItem#readTags` / `#writeTags`).
2. **Items are singletons, stacks are not.** Never keep per-stack mutable state
   (counters, "armed" flags) in an item field. `inventoryTick` runs once *per stack*.
   Store such state in `CUSTOM_DATA`; the teleport countdown is the reference implementation.
3. **Block entities must sync what the renderer needs.** Override `getUpdateTag`/`saveAdditional`
   and `loadAdditional` so client-side renderers receive the inventory (see `StonePedestalBlockEntity`).
4. **Worldgen registries cannot be filled from code** in 1.21. Configured/placed features go in
   `data/miltenmagic/worldgen/...` and are attached via
   `data/miltenmagic/neoforge/biome_modifier/...`. The `core/world/feature/` classes are legacy.
5. **Villager professions must be acquirable via the POI tag.**
   `data/minecraft/tags/point_of_interest_type/acquirable_job_site.json` must contain the mod's POI.
6. **Registry-dependent logic belongs in events**, not in `static` initializers, so it does not run
   before registries are frozen.
7. Render types for cutout/translucent blocks are set in `client/ClientSetup.java`
   (`ItemBlockRenderTypes.setRenderLayer`).

## Adding Things

**New item:** create the class in `common/item/...` → register in `ItemRegistry` → add
`assets/miltenmagic/models/item/<name>.json` + texture → add `item.miltenmagic.<name>` to `en_us.json`.
Add to `CostumCreativeModeTab` if it should appear in the creative tab.

**New block:** create the class in `common/block/` → register block in `BlockRegistry` and
`BlockItem` in `ItemRegistry` → add blockstate, block model, item model → add loot table under
`data/miltenmagic/loot_tables/blocks/` → add `block.miltenmagic.<name>` translation. If it needs
logic/ticking, add a `BlockEntity` in `common/blockentity/` + register it + register a renderer in
`client/ClientSetup.java` if needed.

**New spell:** extend `SpellItem` (or `BuffSpellItem`/`UseSpellItem`) in `common/item/spellItems/`,
register it in `ItemRegistry`, add model/texture/translation, and cover the behaviour with a
GameTest. Follow the XP-level / XP-cost / cooldown / scroll-vs-rune pattern of existing spells.

## Testing & Verification

- Run `./gradlew build` before considering a change done.
- For gameplay logic (blocks, spells, teleportation, block entity persistence, villager trades),
  add a `@GameTest` in `MiltenMagicGameTests` and run `./gradlew runGameTestServer`.
- Do not "fix" tests by weakening assertions; tests document regressions that were actually reported.

## Repository Hygiene

- **Do not edit generated/legacy directories:** `bin/`, `build/`, `.gradle/`, `run/`, `runs/`,
  `archive/`, `src/generated/resources/` (regenerate with `runData`).
- Do not commit IDE metadata (`.idea/`, `.vscode/`, `*.iml`, `*.ipr`, `*.iws`).
- Use the existing commit message style: short, lowercase, imperative-ish summaries
  (e.g. `add table variants`, `port to neoforge`). Keep changes focused per commit.
- Match surrounding style rather than reformatting unrelated code.
