# Release notes

## Release notes version 1.1.0

- Updated Android Gradle Plugin to 9.2.0
- Updated Gradle wrapper to 9.4.1
- Updated target and compile SDK to 36 (Android 16)
- Migrated from Android Support Library to AndroidX
- Modernised build scripts (removed deprecated `buildscript`, `allprojects`, `apply plugin` patterns)

## Release notes version 1.0.0

A sample containing a KeyboardHeightProvider that can calculate the height of a floating soft input keyboard.
The provider is using a hidden PopupWindow to calculate the height of the keyboard.

## Note on modern alternatives

This implementation uses a `PopupWindow` trick to detect keyboard height, which was the only viable approach at the time of writing. Since Android 11 (API 30), the platform provides a first-class API for this:

- `WindowInsetsCompat.Type.ime()` — returns the exact keyboard height once it is fully shown or hidden.
- `ViewCompat.setWindowInsetsAnimationCallback()` — provides interpolated inset values during the open/close animation, useful for syncing UI elements that should move with the keyboard.

Both APIs require `WindowCompat.setDecorFitsSystemWindows(window, false)` to be set on the activity window.

If you are targeting API 30 or higher, the modern WindowInsets approach is recommended over the PopupWindow technique used here.
