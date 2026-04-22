# Keyboard Height Sample

An Android sample project demonstrating how to calculate the height of a floating soft keyboard.

## How it works

`KeyboardHeightProvider` uses a hidden `PopupWindow` sized to the full height of the activity window. Because the popup is configured with `SOFT_INPUT_ADJUST_RESIZE`, it shrinks when the soft keyboard appears. By comparing the popup's visible area to the screen height, the keyboard height can be derived and reported to any registered `KeyboardHeightObserver`.

## Usage

1. Instantiate `KeyboardHeightProvider` in your activity's `onCreate`.
2. Call `start()` after the activity's root view has been laid out (e.g. via `view.post(...)`).
3. Register an observer in `onResume` and unregister in `onPause`.
4. Close the provider in `onDestroy`.

```java
keyboardHeightProvider = new KeyboardHeightProvider(this);
view.post(() -> keyboardHeightProvider.start());

// onResume
keyboardHeightProvider.setKeyboardHeightObserver(this);

// onPause
keyboardHeightProvider.setKeyboardHeightObserver(null);

// onDestroy
keyboardHeightProvider.close();
```

Implement `KeyboardHeightObserver` to receive height changes:

```java
@Override
public void onKeyboardHeightChanged(int height, int orientation) {
    // height is 0 when the keyboard is closed
}
```

## Modern alternative

Since Android 11 (API 30) the platform provides a first-class API for keyboard height detection. If you are targeting API 30 or higher, consider using:

- `WindowInsetsCompat.Type.ime()` — returns the keyboard height once fully shown or hidden.
- `ViewCompat.setWindowInsetsAnimationCallback()` — provides per-frame inset values during the open/close animation, useful for animating UI elements alongside the keyboard.

Both require `WindowCompat.setDecorFitsSystemWindows(window, false)` on the activity window.

## Requirements

- Min SDK: 21
- Target SDK: 36 (Android 16)
- AndroidX

## License

Lesser GNU General Public License v3. See source file headers for details.
