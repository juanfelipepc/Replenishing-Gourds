# Replenishing Gourd Mod
**Minecraft 1.20.1 | Forge 47.4.10**

---

## What it adds

| Item | Description |
|---|---|
| 🎃 **Gourd** | Base crafting ingredient |
| 🌱 **Mystery Seed** | Magical catalyst for crafting |
| ✨ **Replenishing Gourd** | Reusable potion vessel with 3 charges |

---

## How to craft

Place these three items **anywhere** on a crafting table (shapeless):

```
[ Gourd ] + [ Mystery Seed ] + [ Any Potion ]
        = Replenishing Gourd of [Effect]
```

The gourd will absorb **the first effect** of whatever potion you use — including its duration and amplifier. Works with Speed, Strength, Regen, Night Vision, etc.

---

## How it works

- **Right-click** to consume a charge and apply the stored potion effect.
- You get **3 charges** total.
- One charge replenishes every **60 seconds** automatically — no crafting table needed.
- The durability bar on the item shows your current charge level:
  - 🟢 Green = full
  - 🟠 Orange = partially charged
  - 🔴 Red = empty
- A golden enchantment glint appears when fully charged.
- Hover over the item to see the stored effect and remaining charges.

---

## Getting started (development setup)

### Prerequisites
- Java 17 (JDK)
- Git
- An IDE (IntelliJ IDEA recommended)

### Steps

1. **Download the Forge MDK** for 1.20.1 from https://files.minecraftforge.net
2. Extract the MDK into a folder.
3. **Replace** the `src/` folder with the `src/` from this project.
4. **Copy** `build.gradle`, `settings.gradle`, and `gradle.properties` from this project into the MDK folder (overwrite the originals).
5. Open a terminal in the MDK folder and run:
   ```
   ./gradlew genIntellijRuns   # (or genEclipseRuns)
   ```
6. Open the project in your IDE.
7. Run the `runClient` Gradle task to launch Minecraft with the mod loaded.

### Building a `.jar`
```
./gradlew build
```
Output will be in `build/libs/replenishinggourd-1.0.0.jar`.
Drop this into your Forge mods folder.

---

## Project structure

```
src/main/java/com/replenishinggourd/mod/
├── ReplenishingGourdMod.java          ← Main mod entry point
├── init/
│   ├── ModItems.java                  ← Item registrations
│   └── ModCreativeTabs.java           ← Creative tab
└── item/
    ├── GourdBaseItem.java             ← Plain gourd (ingredient)
    ├── MysterySeedItem.java           ← Seed (ingredient)
    ├── ReplenishingGourdItem.java     ← Core item logic
    ├── GourdCraftingRecipe.java       ← Custom crafting logic
    └── ModRecipeSerializers.java      ← Recipe serializer registration

src/main/resources/
├── META-INF/mods.toml                 ← Mod metadata
├── pack.mcmeta
└── assets/replenishinggourd/
    ├── lang/en_us.json                ← Item names
    ├── models/item/                   ← Item model JSONs
    └── textures/item/                 ← Item textures (PNG)
```

---

## Expanding the mod (ideas for later)

- Add a **loot table** so Mystery Seeds drop from certain mobs or chests.
- Support **splash/lingering potions** in the recipe for area-effect variants.
- Add **multiple tiers** of gourd (e.g. iron-capped = 5 charges, gold-capped = 10).
- Add a **JEI plugin** so the recipe shows up in Just Enough Items.
- Make the gourd **glow** the color of the stored potion effect.
