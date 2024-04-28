package com.shxyke.MaaTouch;

import com.shxyke.MaaTouch.wrappers.InputManager;

public class KeyThread extends Thread {
    private boolean isRunning = true;
    private final Controller controller;
    private int keyCode;
    private int repeat = 0;

    private int injectMode;

    public KeyThread(Controller controller, int keyCode) {
        this.controller = controller;
        this.keyCode = keyCode;
        this.injectMode = InputManager.INJECT_MODE_ASYNC;
    }

    public KeyThread(Controller controller, int keyCode, int injectMode) {
        this.controller = controller;
        this.keyCode = keyCode;
        this.injectMode = injectMode;
    }

    @Override
    public void run() {
        do {
            controller.injectKeyDown(keyCode, repeat++, 0, this.injectMode);
            try {
                Thread.sleep(1000 / 20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (this.isRunning);
        controller.injectKeyUp(keyCode, 0, 0, this.injectMode);
    }

    public void stopThread() {
        this.isRunning = false;
    }
}
