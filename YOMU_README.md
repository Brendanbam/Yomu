# Yomu — Your Manga Sanctuary

**Yomu** is a complete native Android UI/UX redesign built on the open-source **Mihon** manga reader engine. It transforms Mihon into a warm, luxurious Japanese private manga library—featuring realistic wooden bookshelves, tactile physical manga volumes, warm lantern lighting, antique gold accents, and amethyst active states—while preserving Mihon's full feature set, database schema, extension ecosystem, tracker integrations, reader engine, and `.tachibk` backup compatibility.

## Highlights

- **Yomu Design System & Theme Suite:**
  - **Midnight Walnut** (default): Espresso `#100D0B`, Dark Wood `#1D1713`, Elevated Walnut `#302219`, Wooden Shelf `#754929`, Antique Gold `#D6AD73`, Warm Ivory `#F4E9D7`, Amethyst `#9460DF`, Sage Green `#83AA8A`.
  - **Black Ebony** (OLED near-black), **Natural Oak** (warm daylight light theme), **Cherry Blossom** (sakura mahogany), plus **Original Mihon** and all classic Mihon themes.
  - Live customization of accent color, shelf material, shelf lighting, book thickness, shadows, wood texture detail, volume titles, reading progress bars, smart shelves, and reduced motion.
- **5-Tab Navigation + Adaptive Tablet Layout:**
  - `Home`, `Library`, `Updates`, `Browse`, `More` with amethyst active pill highlighting, automatic `NavigationRail` on tablets/foldables, and a `Start in Library` startup preference.
- **Home — The Reading Room:**
  - Cover-derived cinematic hero banner, `Continue Reading` with exact chapter & page resume, `Recently Updated`, wooden shelf strips for `Currently Reading`, `Rediscover`, and `Offline Collection`, `History` & Quick Access shortcuts, plus section ordering/visibility customization.
- **Library — The Bookshelves:**
  - Procedural GPU-friendly dark walnut/ebony/oak/cherry shelves with organic wood grain, lit upper ledge, overhead lantern glow, recessed paneling, and physical manga volumes with layered page-block thickness, spine crease, grounded contact shadows, unread badges, sage offline indicators, and spring focus/selection lift.
  - Display modes: `Classic Bookshelf`, `Collector Edition`, `Book Spine`, `Compact Grid`, `Minimal`, `Comfortable Grid`, `Cover-only Grid`, and `List`.
  - Non-destructive automatic **Smart Shelves**: `All`, `Currently Reading`, `Unread`, `New This Week`, `Recently Added`, `Almost Caught Up`, `Ready Offline`, `Forgotten Stories`, `Completed`.
- **Manga Details, Reader, Discovery & Management:**
  - Cinematic full-bleed `MangaScreen` with metadata pills, amethyst `Start/Resume Reading` CTA, 3-tab `Chapters | Tracking | Details` layout (and split-pane layout on tablets), dark-wood catalog chapter cards, and sage downloaded checkmarks.
  - Distraction-free Mihon reader engine preserved with refined espresso/amethyst/gold overlay bars.
  - Redesigned `Browse` (`SourcesScreen` with global search pill, Pinned Sources card strip, Extensions shortcut), `Updates`, `History` (Reading Journal), `More` (Management Center), `Onboarding`, and `About Yomu`.

## Package Identity & Coexistence with Mihon

- **Application ID:** `app.yomu` (debug builds: `app.yomu.dev`, FOSS builds: `app.yomu.foss`).
- **Namespace:** `eu.kanade.tachiyomi` (preserved for internal resource & extension compatibility).
- **Side-by-Side Installation:** Yomu installs cleanly alongside existing `app.mihon` installations without requiring you to uninstall Mihon or delete your existing Mihon library.
- **FileProvider / Shizuku Authorities:** Automatically scoped via `${applicationId}.provider` and `${applicationId}.shizuku`.
- **Tracker OAuth & Deep Links:** Retains `mihon://`, `app.mihon://`, `tachiyomi://`, and adds `yomu://` intent filters so tracker logins and `.tachibk` backup imports continue to work seamlessly.

## Migrating Your Library from Mihon

Because Yomu uses a distinct `applicationId` and signing key, do not attempt to install it over an existing Mihon package. Transfer your library cleanly with Mihon's native backup format:

1. In **Mihon**, open **More → Data and storage → Create backup** and save a `.tachibk` backup file.
2. Install **Yomu** alongside Mihon and complete onboarding (you may point Yomu's storage folder to a dedicated `Yomu` directory or reuse your existing manga storage folder).
3. In **Yomu**, tap **Restore backup** on the empty Home screen (or go to **More → Data and storage → Restore backup**) and select your `.tachibk` file.
4. Your library entries, categories, chapter read states, reading history, bookmarks, and tracker bindings will restore non-destructively.

## Download & Auto-Updates with Obtainium

1. Open **Obtainium** and tap **Add App**.
2. Enter the repository URL: `https://github.com/Brendanbam/Yomu`
3. Under **Filter APKs by Regular Expression**, set:
   - `arm64-v8a` for modern Android phones & tablets (recommended)
   - `universal` if you want the all-ABI APK
4. Tap **Add** and **Install**. Whenever a new `v*` tag is pushed (or run via **Actions → Yomu Release**), GitHub Actions builds and publishes signed APKs automatically for Obtainium.

## Build Instructions

Prerequisites: JDK 21 (or JDK 17+) and Android SDK (`local.properties` pointing to your SDK directory).

```bash
# Build debug APKs (split ABIs + universal)
./gradlew :app:assembleDebug

# Run domain and app unit tests
./gradlew :domain:testDebugUnitTest :app:testDebugUnitTest
```

Output debug APKs are generated in `app/build/outputs/apk/debug/`.

## Attribution & Licensing

Yomu is built directly on the open-source **Mihon** project (https://github.com/mihonapp/mihon). All Mihon and third-party open-source attributions and license notices are preserved in the repository and in **More → About → Open source licenses**.
