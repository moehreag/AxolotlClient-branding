## Changelog:

### 3.1.1

- Add screenshot gallery
- Fix various bugs & crashes
- Skip Celestial sky packs while scanning for custom skies
- (1.8.9, 1.16) Enable texture nine-slicing for all ButtonWidgets
- Update bedwars module
- add secondary keybinding for freelook
- add options to display durability numbers for ArmorHuds
- Add option to display the current biome on the CoordHuds
- (1.21.1) remove custom sky module, please just use fabricskyboxes or celestial instead (it was broken anyway)

### 3.1.0

- Add a new Backend (made with help from `@Astralchroma`)
- Features:
	- Friends
	- Chats
	- Activity update notifications
	- Hypixel API
- rewrite DiscordRPC
- 1.8.9 + 1.16_combat-6 now require java 17
- rewritten config library
- removed custom skies on 1.21.4 (there are better implementations available already)
- removed HotbarHUD on 1.21.4 (big maintenance burden with buggy feature)
- add Integration with WorldHost (1.21.1, 1.21.4)
- fix bugs (#131)
- update to 1.21.x
- drop 1.19 version range
- add filter capabilities to AutoBoop (suggested by `@Mocha`)
- refine notification system
- text transparency option for ScoreboardHuds (suggested by `@haovi`)
- migrate 1.21.4 to mojmap
- add Activity support for MCCIsland
- block more disallowed features on MCCIsland
- 1.21.4: temporarily disable broken features: MotionBlur, ReachHud, ComboHud

### 3.0.6

- add more Zoom keybinds
- add option(s) to remove certain messages on hypixel (join, mystery box)
- Removed freelook on MCC island (#118)

### 3.0.5

- the TeamUpgradesHud now uses texture atlases provided by the game to improve performance
- fix issues with the Bedwars Module

### 3.0.4

- add DarkKronicle's Bedwars Overlay
- fix the controls screen crashing in 1.8.9
- add option to remove the vignette
- fix the sky impl mistaking suns for skies
- fix a client lockup issue on 1.8.9
- fix PlayerHud scaling on 1.8.9

### 3.0.3

- update to 1.20
- update Chinese translations (HowardZHY)
- fix a reach hud bug on 1.8.9

### 3.0.2

- fix *more* bugs
- re-instantiate modmenu compat on 1.16 versions
- fix compatibility with darkloadingscreen

### 3.0.1

- add in-game Authentication
	- Supports both Microsoft- & Offline-Accounts
	- Offline accounts can only be added after at least one Microsoft-Account is added
- fix running 1.16 versions with Java 8
- fix resourcepacks on 1.16 versions
- add a notification system to 1.8.9
- fix more bugs

### 3.0.0

- all versions now share parts of a codebase
- add versions for 1.19.2, CTS and 1.16.5
- update French translation (CornetPanique86)

### 2.2.10

- Add a `graphical` option type
- Add custom crosshair texture option
- add customization options to the mouse movement indicator of the KeystrokeHud

### 2.2.9

- Fix some bugs
- update config library, now uses the `multiversion` branch
- Server API for disabling certain features
- refactored AutoGG
- prefix mixins
- Image sharing via [gartbin](https://bin.gart.sh)
- Complete README feature list (RoonMoonlight)
- Weather Changer
- Tablist Customisation

### 2.2.8

- HitColor option
- Allow Snap Perspective mode to be used on servers where Freelook is disallowed
- add a port of the Dynamic FPS mod by juliand665 to 1.8.9

### 2.2.7

- fix KeyBindOptions not being saved
	- since this is a critical fix, this will also be released for 1.19.2
- add 'Snap-Perspective' mode to Freelook

### 2.2.6

- add sourcesJar to files to be uploaded to modrinth
- add option to hide Beacon Beams
- port to 1.19.3

### 2.2.5

- reformat code (@TheKodeToad)
- add PlayerHUD Auto-Hide Option (@TheKodeToad)
- update README
- add license headers
- add missing credits
- add turkish translation of the README
- add Option to hide AutoTip tip messages
- Add Option to toggle Freelook
- update config library to 1.0.13

### 2.2.4

- fix a critical crash that could totally have been avoided

### 2.2.3

- fix some nasty bugs
- re-add an option that had been removed in 2.2.2
- update german translation

### 2.2.2

- KronHUD 2.2.3 feature set
- Various fixes
- now using the provided DefaultConfigManager.
- Full Changelog: https://github.com/AxolotlClient/AxolotlClient-mod/compare/v2.2.1+1.19.2...v2.2.2+1.19.2


### 2.2.1

- use own maven instead of JitPack
- updated Translations
- fix bugs in the config library
- Accessibility Improvements
- Narration Support

### 2.0.0 - 2.2.0

- Separate the config system into a config library
- Add French translation (CornetPanique86)
- Add MotionBlur & Freelook (TheKodeToad)
- Add Particle Modifiers
- Add Screenshot Utils
- Add Scrollable Tooltips
- Add TnTTime
- Add external Module Support
- Add some other stuff I forgot
- Full Changelog: https://github.com/AxolotlClient/AxolotlClient-mod/compare/v2.0.0+1.19...v2.2.0+1.19.2


### 1.0.0 - 2.0.0

- Create the mod
- Learn Java
- Add Badge System
- Add HUD modules
- Add Hypixel mods
- Add DiscordRPC
- Add Custom Skies
- Add Zoom
- Rewrite Configuration System

- Add Turkish translation (YakisikliBaran)
