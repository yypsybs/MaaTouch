package com.shxyke.MaaTouch;

import com.shxyke.MaaTouch.wrappers.ClipboardManager;
import com.shxyke.MaaTouch.wrappers.InputManager;
import com.shxyke.MaaTouch.wrappers.ServiceManager;

import android.os.Build;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class Controller {

    private static final int DEFAULT_DEVICE_ID = 0;
    private static final int DEFAULT_SOURCE = InputDevice.SOURCE_TOUCHSCREEN;

    private final ServiceManager serviceManager;

    private long lastTouchDown;
    private final PointersState pointersState = new PointersState();
    private final MotionEvent.PointerProperties[] pointerProperties = new MotionEvent.PointerProperties[PointersState.MAX_POINTERS];
    private final MotionEvent.PointerCoords[] pointerCoords = new MotionEvent.PointerCoords[PointersState.MAX_POINTERS];

    public Controller(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        initPointers();
    }

    private void initPointers() {
        for (int i = 0; i < PointersState.MAX_POINTERS; ++i) {
            MotionEvent.PointerProperties props = new MotionEvent.PointerProperties();
            props.toolType = MotionEvent.TOOL_TYPE_FINGER;

            MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
            coords.orientation = 0;
            coords.size = 0;

            pointerProperties[i] = props;
            pointerCoords[i] = coords;
        }
    }

    private boolean injectEvent(InputEvent event, int injectMode) {
        return serviceManager.getInputManager().injectInputEvent(event, injectMode);
    }

    public void resetAll(int injectMode) {
        int count = pointersState.size();
        for (int i = 0; i < count; ++i) {
            Pointer pointer = pointersState.get(0);
            this.injectTouch(MotionEvent.ACTION_UP, pointer.getId(), pointer.getPoint(), 0,
                    injectMode);
        }
    }

    public String getClipboardText() {
        ClipboardManager clipboardManager = serviceManager.getClipboardManager();
        if (clipboardManager == null) {
            return null;
        }
        CharSequence s = clipboardManager.getText();
        if (s == null) {
            return null;
        }
        return s.toString();
    }
    public boolean setClipboardText(String text) {
        ClipboardManager clipboardManager = serviceManager.getClipboardManager();
        if (clipboardManager == null) {
            return false;
        }

        String currentClipboard = getClipboardText();
        if (currentClipboard != null && currentClipboard.equals(text)) {
            return true;
        }
        return clipboardManager.setText(text);
    }

    public boolean pressReleaseKeycode(int keyCode) {
        return pressReleaseKeycode(keyCode, InputManager.INJECT_MODE_ASYNC);
    }

    public boolean pressReleaseKeycode(int keyCode, int injectMode) {
        return injectKeyEvent(KeyEvent.ACTION_DOWN, keyCode, 0, 0, injectMode)
                && injectKeyEvent(KeyEvent.ACTION_UP, keyCode, 0, 0, injectMode);
    }

    public boolean setClipboard(String text) {
        return setClipboard(text, InputManager.INJECT_MODE_ASYNC);
    }
    public boolean setClipboard(String text, int injectMode) {
        boolean ok = setClipboardText(text);
        if (ok && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pressReleaseKeycode(KeyEvent.KEYCODE_PASTE, injectMode);
        }
        return ok;
    }

    public boolean injectKeyDown(int keyCode, int repeat, int metaState) {
        return injectKeyDown(keyCode, repeat, metaState, InputManager.INJECT_MODE_ASYNC);
    }

    public boolean injectKeyDown(int keyCode, int repeat, int metaState, int injectMode) {
        return injectKeyEvent(MotionEvent.ACTION_DOWN, keyCode, repeat, metaState, injectMode);
    }

    public boolean injectKeyUp(int keyCode, int repeat, int metaState) {
        return injectKeyUp(keyCode, repeat, metaState, InputManager.INJECT_MODE_ASYNC);
    }

    public boolean injectKeyUp(int keyCode, int repeat, int metaState, int injectMode) {
        return injectKeyEvent(MotionEvent.ACTION_UP, keyCode, repeat, metaState, injectMode);
    }

    public boolean injectKeyEvent(int action, int keyCode, int repeat, int metaState) {
        return injectKeyEvent(action, keyCode, repeat, metaState, InputManager.INJECT_MODE_ASYNC);
    }

    public boolean injectKeyEvent(int action, int keyCode, int repeat, int metaState, int injectMode) {
        long now = SystemClock.uptimeMillis();
        KeyEvent event = new KeyEvent(now, now, action, keyCode, repeat, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0,
                InputDevice.SOURCE_KEYBOARD);
        return injectEvent(event, injectMode);
    }

    public boolean injectTouchDown(long pointerId, Point point, float pressure) {
        return injectTouchDown(pointerId, point, pressure, InputManager.INJECT_MODE_ASYNC);
    }

    public boolean injectTouchDown(long pointerId, Point point, float pressure, int injectMode) {
        return this.injectTouch(MotionEvent.ACTION_DOWN, pointerId, point, pressure, injectMode);
    }

    public boolean injectTouchMove(long pointerId, Point point, float pressure) {
        return injectTouchMove(pointerId, point, pressure, InputManager.INJECT_MODE_ASYNC);
    }

    public boolean injectTouchMove(long pointerId, Point point, float pressure, int injectMode) {
        return this.injectTouch(MotionEvent.ACTION_MOVE, pointerId, point, pressure, injectMode);
    }

    public boolean injectTouchUp(long pointerId) {
        return injectTouchUp(pointerId, InputManager.INJECT_MODE_ASYNC);
    }

    public boolean injectTouchUp(long pointerId, int injectMode) {
        int pointerIndex = pointersState.getPointerIndex(pointerId);
        if (pointerIndex == -1) {
            Ln.w("No such pointer");
            return false;
        }
        Pointer pointer = pointersState.get(pointerIndex);
        return this.injectTouch(MotionEvent.ACTION_UP, pointerId, pointer.getPoint(), 0,
                injectMode);
    }

    public boolean injectTouch(int action, long pointerId, Point point, float pressure) {
        return injectTouch(action, pointerId, point, pressure, InputManager.INJECT_MODE_ASYNC);
    }

    public boolean injectTouch(int action, long pointerId, Point point, float pressure,
                               int injectMode) {
        long now = SystemClock.uptimeMillis();

        if (point == null) {
            Ln.w("Ignore touch event, it was generated for a different device size");
            return false;
        }

        int pointerIndex = pointersState.getPointerIndex(pointerId);
        if (pointerIndex == -1) {
            Ln.w("Too many pointers for touch event");
            return false;
        }
        Pointer pointer = pointersState.get(pointerIndex);
        pointer.setPoint(point);
        pointer.setPressure(pressure);
        pointer.setUp(action == MotionEvent.ACTION_UP);

        int pointerCount = pointersState.update(pointerProperties, pointerCoords);

        if (pointerCount == 1) {
            if (action == MotionEvent.ACTION_DOWN) {
                lastTouchDown = now;
            }
        } else {
            // secondary pointers must use ACTION_POINTER_* ORed with the pointerIndex
            if (action == MotionEvent.ACTION_UP) {
                action = MotionEvent.ACTION_POINTER_UP
                        | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            } else if (action == MotionEvent.ACTION_DOWN) {
                action = MotionEvent.ACTION_POINTER_DOWN
                        | (pointerIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            }
        }

        MotionEvent event = MotionEvent
                .obtain(lastTouchDown, now, action, pointerCount, pointerProperties, pointerCoords,
                        0, 0, 1f, 1f, DEFAULT_DEVICE_ID, 0,
                        DEFAULT_SOURCE, 0);
        return injectEvent(event, injectMode);
    }

}
