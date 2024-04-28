package com.shxyke.MaaTouch;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputThread extends Thread {

    private final BufferedReader stdin;
    private final LinkedBlockingQueue<QueueWithInjectMode<ControlMessage>> queue;

    private boolean isRunning = true;

    private static final Pattern DOWN_PATTERN_SYNC = Pattern.compile("d\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern DOWN_PATTERN_ASYNC = Pattern.compile("d\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern MOVE_PATTERN_SYNC = Pattern.compile("m\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern MOVE_PATTERN_ASYNC = Pattern.compile("m\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern KEY_PATTERN_SYNC = Pattern.compile("k\\s+(\\d+)\\s+([duo])\\s+(\\d+)");
    private static final Pattern KEY_PATTERN_ASYNC = Pattern.compile("k\\s+(\\d+)\\s+([duo])");
    private static final Pattern WAIT_PATTERN = Pattern.compile("w\\s+(\\d+)");
    private static final Pattern UP_PATTERN_SYNC = Pattern.compile("u\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern UP_PATTERN_ASYNC = Pattern.compile("u\\s+(\\d+)");
    private static final Pattern COMMIT_PATTERN = Pattern.compile("c");
    private static final Pattern RESET_PATTERN_SYNC = Pattern.compile("r\\s+(\\d+)");
    private static final Pattern RESET_PATTERN_ASYNC = Pattern.compile("r");

    private static final Pattern SET_INJECT_MODE = Pattern.compile("s\\s+(\\d+)");

    private static final Pattern TEXT_PATTERN = Pattern.compile("t\\s+(.+)");

    private QueueWithInjectMode<ControlMessage> subqueue = new QueueWithInjectMode<>();

    public InputThread(BufferedReader stdin,
                       LinkedBlockingQueue<QueueWithInjectMode<ControlMessage>> queue) {
        this.stdin = stdin;
        this.queue = queue;
    }

    private void parseKey(String s) {
        Matcher mSync = KEY_PATTERN_SYNC.matcher(s);
        if (mSync.find()) {
            int keycode = Integer.parseInt(mSync.group(1));
            int injectMode = Integer.parseInt(mSync.group(3));
            if (mSync.group(2).equals("u")) {
                while (!subqueue.offer(ControlMessage.createKeyUpEvent(
                        keycode, 0, 0, injectMode))) ;
            } else if (mSync.group(2).equals("d")) {
                while (!subqueue.offer(ControlMessage.createKeyDownEvent(
                        keycode, 0, 0, injectMode))) ;
            } else {
                while (!subqueue.offer(ControlMessage.createKeyEvent(
                        keycode, injectMode))) ;
            }
        }

        Matcher mAsync = KEY_PATTERN_ASYNC.matcher(s);
        if (mAsync.find()) {
            int keycode = Integer.parseInt(mAsync.group(1));
            if (mAsync.group(2).equals("u")) {
                while (!subqueue.offer(ControlMessage.createKeyUpEvent(keycode, 0, 0))) ;
            } else if (mAsync.group(2).equals("d")) {
                while (!subqueue.offer(ControlMessage.createKeyDownEvent(keycode, 0, 0))) ;
            } else {
                while (!subqueue.offer(ControlMessage.createKeyEvent(keycode))) ;
            }
        }
    }

    private void parseTouchDown(String s) {
        Matcher mSync = DOWN_PATTERN_SYNC.matcher(s);
        if (mSync.find()) {
            long pointerId = Long.parseLong(mSync.group(1));
            Point point = new Point(Integer.parseInt(mSync.group(2)), Integer.parseInt(mSync.group(3)));
            float pressure = Integer.parseInt(mSync.group(4)) / 255;
            int injectMode = Integer.parseInt(mSync.group(5));
            while (!subqueue.offer(ControlMessage.createTouchDownEvent(
                    pointerId, point, pressure, injectMode))) ;
            return;
        }
        Matcher mAsync = DOWN_PATTERN_ASYNC.matcher(s);
        if (mAsync.find()) {
            long pointerId = Long.parseLong(mAsync.group(1));
            Point point = new Point(Integer.parseInt(mAsync.group(2)), Integer.parseInt(mAsync.group(3)));
            float pressure = Integer.parseInt(mAsync.group(4)) / 255;
            while (!subqueue.offer(ControlMessage.createTouchDownEvent(
                    pointerId, point, pressure))) ;
        }
    }

    private void parseTouchMove(String s) {
        Matcher mSync = MOVE_PATTERN_SYNC.matcher(s);
        if (mSync.find()) {
            long pointerId = Long.parseLong(mSync.group(1));
            Point point = new Point(Integer.parseInt(mSync.group(2)), Integer.parseInt(mSync.group(3)));
            float pressure = Integer.parseInt(mSync.group(4)) / 255;
            int injectMode = Integer.parseInt(mSync.group(5));
            while (!subqueue.offer(ControlMessage.createTouchMoveEvent(
                    pointerId, point, pressure, injectMode))) ;
        }
        Matcher mAsync = MOVE_PATTERN_ASYNC.matcher(s);
        if (mAsync.find()) {
            long pointerId = Long.parseLong(mAsync.group(1));
            Point point = new Point(Integer.parseInt(mAsync.group(2)), Integer.parseInt(mAsync.group(3)));
            float pressure = Integer.parseInt(mAsync.group(4)) / 255;
            while (!subqueue.offer(ControlMessage.createTouchMoveEvent(pointerId, point, pressure)))
                ;
        }
    }

    private void parseTouchUp(String s) {
        Matcher mSync = UP_PATTERN_SYNC.matcher(s);
        if (mSync.find()) {
            long pointerId = Long.parseLong(mSync.group(1));
            int injectMode = Integer.parseInt(mSync.group(2));
            while (!subqueue.offer(ControlMessage.createTouchUpEvent(pointerId, injectMode))) ;
        }
        Matcher mAsync = UP_PATTERN_ASYNC.matcher(s);
        if (mAsync.find()) {
            long pointerId = Long.parseLong(mAsync.group(1));
            while (!subqueue.offer(ControlMessage.createTouchUpEvent(pointerId))) ;
        }
    }

    private void parseWait(String s) {
        Matcher m = WAIT_PATTERN.matcher(s);
        if (m.find()) {
            long milis = Long.parseLong(m.group(1));
            while (!subqueue.offer(ControlMessage.createWaitEvent(milis))) ;
        }
    }

    private void commitSubqueue() {
        if (!subqueue.isEmpty()) {
            try {
                queue.put(subqueue);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.subqueue = new QueueWithInjectMode<>();
        }
    }

    private void parseCommit(String s) {
        Matcher m = COMMIT_PATTERN.matcher(s);
        if (m.find()) {
            commitSubqueue();
        }
    }

    private void parseTouchReset(String s) {
        Matcher mSync = RESET_PATTERN_SYNC.matcher(s);
        if (mSync.find()) {
            int injectMode = Integer.parseInt(mSync.group(1));
            subqueue.clear();
            while (!subqueue.offer(ControlMessage.createEmpty(
                    ControlMessage.TYPE_EVENT_TOUCH_RESET, injectMode)))
                ;
            commitSubqueue();
        }
        Matcher mAsync = RESET_PATTERN_ASYNC.matcher(s);
        if (mAsync.find()) {
            subqueue.clear();
            while (!subqueue.offer(ControlMessage.createEmpty(ControlMessage.TYPE_EVENT_TOUCH_RESET)))
                ;
            commitSubqueue();
        }
    }

    private void setInjectMode(String s) {
        Matcher m = SET_INJECT_MODE.matcher(s);
        if (m.find()) {
            int mode = Integer.parseInt(m.group(1));
            subqueue.setInjectMode(mode);
        }
    }

    private void parseText(String s) {
        Matcher m = TEXT_PATTERN.matcher(s);
        if (m.find()) {
            String text = m.group(1);
            while (!subqueue.offer(ControlMessage.createTextEvent(text))) ;
        }
    }

    private void parseInput(String s) {
        switch (s.charAt(0)) {
            case 's':
                setInjectMode(s);
            case 'c':
                parseCommit(s);
                break;
            case 'r':
                parseTouchReset(s);
                break;
            case 'd':
                parseTouchDown(s);
                break;
            case 'm':
                parseTouchMove(s);
                break;
            case 'u':
                parseTouchUp(s);
                break;
            case 'w':
                parseWait(s);
                break;
            case 'k':
                parseKey(s);
                break;
            case 't':
                parseText(s);
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        while (this.isRunning) {
            String s;
            try {
                s = stdin.readLine();
                System.out.println("readLine : " + s);
                if (s.length() != 0) {
                    parseInput(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        this.isRunning = false;
    }
}
