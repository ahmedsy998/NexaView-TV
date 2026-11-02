# NexaView TV — مشغل تلفاز أندرويد خفيف وسريع (Android TV Launcher)

- **Application Name:** `NexaView TV`
- **Project Name:** `NexaViewTV`
- **Application ID:** `com.ahmedsalam.nexaviewtv`
- **Minimum Android Version:** Android 7.0 (API 24)
- **Primary Interface Language & Direction:** Arabic (RTL) with permanent right-side vertical navigation menu and two-row application grid.

## Key Features
1. **Home Screen Design:**
   - Permanent right-side Arabic navigation panel (`الرئيسية`, `التطبيقات`, `الأفلام`, `المسلسلات`, `المفضلة`, `الملفات`, `الإعدادات`).
   - Strictly two horizontal rows of selected applications to the left of the navigation panel.
   - No featured banner, no carousel, no launcher logo/title at the top, and no Bluetooth or notification icons.
   - Top status bar with real-time enlarged Wi-Fi indicator (white waves when validated internet is available; diagonal red slash when internet is unavailable) and device clock/Arabic date.
   - Automatic initial focus on YouTube when installed (without auto-launching it), or fallback to the first available application.
2. **All Applications & Remote MENU Context Menu:**
   - Displays actual installed launchable TV and standard Android applications using `PackageManager`.
   - Pressing `MENU` on the TV remote (or long-pressing `OK` / pressing `INFO` or `GUIDE` on remotes without a dedicated `MENU` key) opens the Arabic context menu:
     1. `معلومات التطبيق` (Opens official Android system App Info screen).
     2. `ضبط التطبيق` (Opens legitimate exported `ACTION_APPLICATION_PREFERENCES` activity if supported, or displays `"لا تتوفر إعدادات مستقلة لهذا التطبيق."`).
     3. `إلغاء` (Closes the menu and restores focus to the same application).
3. **Customization & Low-End Device Mode:**
   - **Icon Customization (`تخصيص الأيقونات`):** Shapes (Original, Square, Rounded Square, Circle, Rounded Rectangle), size presets + slider, label visibility/size, spacing, corner radius, opacity, focus glow, and border thickness.
   - **Arabic Typography (`الخطوط والخطوط العربية`):** Bundled local `.ttf` fonts (`Cairo` default, `Amiri`, `Tajawal`, `IBM Plex Sans Arabic`, `Noto Sans Arabic`, `Noto Naskh Arabic`, `Readex Pro`, `Almarai`) with immediate real font family switching.
   - **Background Import (`الخلفية والمظهر`):** Lightweight built-in static backgrounds plus official Android Document Picker (`OpenDocument`) for `JPEG`, `PNG`, and `WebP` with off-main-thread downsampled decoding, preview before applying, and `Fill Screen` / `Fit Image` / `Center Crop` modes.
   - **Low-End Device Mode (`وضع الأجهزة الضعيفة`):** Disables nonessential glow effects, removes transition delays, and decodes images conservatively (`RGB_565`) for 1 GB / 2 GB RAM TV boxes.

## Build & Installation Instructions

### 1. Build Debug & Release APKs
```bash
gradle assembleDebug assembleRelease
```
- **Debug APK output:** `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK output:** `app/build/outputs/apk/release/app-release.apk`

### 2. Run Unit & Robolectric Tests
```bash
gradle :app:testDebugUnitTest
```

### 3. Install on an Android TV / TV Box via ADB
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```
> **Note on Manufacturer Restrictions:** Some Smart TV and Android TV box manufacturers restrict third-party launchers from automatically becoming the default home screen. After installation, press the **HOME** button on the TV remote and choose **NexaView TV (Always)**, or navigate to **System Settings -> Apps -> Default Apps -> Home App** and select **NexaView TV**.
