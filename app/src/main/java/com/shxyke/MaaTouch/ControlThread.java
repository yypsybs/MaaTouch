package com.shxyke.MaaTouch;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ControlThread {

    private final LinkedBlockingQueue<QueueWithInjectMode<ControlMessage>> queue;
    private final Controller controller;
    private final HashMap<Integer, KeyThread> KeyThreads;

    public ControlThread(LinkedBlockingQueue<QueueWithInjectMode<ControlMessage>> queue,
                         Controller controller) {
        this.queue = queue;
        this.controller = controller;
        KeyThreads = new HashMap<>();
    }

    public void KeyDown(int key, int injectMode) {
        if (!KeyThreads.containsKey(key)) {
            KeyThread thread = new KeyThread(this.controller, key, injectMode);
            thread.start();
            KeyThreads.put(key, thread);
        }
    }

    public void KeyUp(int key) {
        if (KeyThreads.containsKey(key)) {
            KeyThread thread = KeyThreads.get(key);
            thread.stopThread();
            KeyThreads.remove(key);
        }
    }

    public void handleMessage(ControlMessage msg, Integer subQueueInjectMode) {
        switch (msg.getType()) {
            case ControlMessage.TYPE_EVENT_TOUCH_RESET:
                controller.resetAll(msg.getInjectMode());
                break;
            case ControlMessage.TYPE_EVENT_TOUCH_DOWN:
                controller.injectTouchDown(
                        msg.getPointerId(), msg.getPoint(), msg.getPressure(),
                        subQueueInjectMode == null ? msg.getInjectMode() : subQueueInjectMode);
                break;
            case ControlMessage.TYPE_EVENT_TOUCH_MOVE:
                controller.injectTouchMove(msg.getPointerId(), msg.getPoint(), msg.getPressure(),
                        subQueueInjectMode == null ? msg.getInjectMode() : subQueueInjectMode);
                break;
            case ControlMessage.TYPE_EVENT_TOUCH_UP:
                controller.injectTouchUp(msg.getPointerId(), subQueueInjectMode == null
                        ? msg.getInjectMode() : subQueueInjectMode);
                break;
            case ControlMessage.TYPE_EVENT_KEY_DOWN:
                KeyDown(msg.getKeycode(), subQueueInjectMode == null
                        ? msg.getInjectMode() : subQueueInjectMode);
                break;
            case ControlMessage.TYPE_EVENT_KEY_UP:
                KeyUp(msg.getKeycode());
                break;
            case ControlMessage.TYPE_EVENT_KEY:
                controller.pressReleaseKeycode(msg.getKeycode(), subQueueInjectMode == null
                        ? msg.getInjectMode() : subQueueInjectMode);
                break;
            case ControlMessage.TYPE_EVENT_TEXT:
                controller.setClipboard(msg.getText(), subQueueInjectMode == null
                        ? msg.getInjectMode() : subQueueInjectMode);
                break;
            case ControlMessage.TYPE_EVENT_WAIT:
                try {
                    Thread.sleep(msg.getMillis());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    public void run() {
        while (true) {
            try {
                QueueWithInjectMode<ControlMessage> subqueue = queue.take();
                // 尝试处理第一条

                while (!subqueue.isEmpty()) {
                    handleMessage(subqueue.poll(), subqueue.getInjectMode());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
