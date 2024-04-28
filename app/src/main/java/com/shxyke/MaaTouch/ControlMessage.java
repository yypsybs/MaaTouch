package com.shxyke.MaaTouch;

/**
 * Union of all supported event types, identified by their {@code type}.
 */
public final class ControlMessage {

    public static final int TYPE_EVENT_TOUCH_DOWN = 0;
    public static final int TYPE_EVENT_TOUCH_UP = 1;
    public static final int TYPE_EVENT_TOUCH_MOVE = 2;
    public static final int TYPE_EVENT_WAIT = 3;
    public static final int TYPE_EVENT_TOUCH_RESET = 4;
    public static final int TYPE_EVENT_KEY_DOWN = 5;
    public static final int TYPE_EVENT_KEY_UP = 6;
    public static final int TYPE_EVENT_TEXT = 7;
    public static final int TYPE_EVENT_KEY = 8;

    private int type;
    private long pointerId;
    private float pressure;
    private Point point;
    private long millis;
    private int keycode;
    private int repeat;
    private int metaState;
    private String text;
    private int injectMode;

    private ControlMessage(int type) {
        this.type = type;
    }

    public static ControlMessage createTextEvent(String text) {
        ControlMessage msg = new ControlMessage(TYPE_EVENT_TEXT);
        msg.text = text;
        return msg;
    }

    public static ControlMessage createKeyDownEvent(int keycode, int repeat, int metaState,
                                                    int injectMode) {
        return createKeyDownEvent(keycode, repeat, metaState)
                .setInjectMode(injectMode);
    }

    public static ControlMessage createKeyDownEvent(int keycode, int repeat, int metaState) {
        ControlMessage msg = new ControlMessage(TYPE_EVENT_KEY_DOWN);
        msg.keycode = keycode;
        msg.repeat = repeat;
        msg.metaState = metaState;
        return msg;
    }

    public static ControlMessage createKeyEvent(int keycode, int injectMode) {
        return createKeyEvent(keycode)
                .setInjectMode(injectMode);
    }

    public static ControlMessage createKeyEvent(int keycode) {
        ControlMessage msg = new ControlMessage(TYPE_EVENT_KEY);
        msg.keycode = keycode;
        msg.repeat = 0;
        msg.metaState = 0;
        return msg;
    }

    public static ControlMessage createKeyUpEvent(int keycode, int repeat, int metaState,
                                                  int injectMode) {
        return createKeyUpEvent(keycode, repeat, metaState)
                .setInjectMode(injectMode);
    }


    public static ControlMessage createKeyUpEvent(int keycode, int repeat, int metaState) {
        ControlMessage msg = new ControlMessage(TYPE_EVENT_KEY_UP);
        msg.keycode = keycode;
        msg.repeat = repeat;
        msg.metaState = metaState;
        return msg;
    }

    public static ControlMessage createTouchDownEvent(long pointerId, Point point, float pressure,
                                                      int injectMode) {
        return createTouchDownEvent(pointerId, point, pressure)
                .setInjectMode(injectMode);
    }

    public static ControlMessage createTouchDownEvent(long pointerId, Point point, float pressure) {
        ControlMessage msg = new ControlMessage(TYPE_EVENT_TOUCH_DOWN);
        msg.pointerId = pointerId;
        msg.pressure = pressure;
        msg.point = point;
        return msg;
    }

    public static ControlMessage createTouchUpEvent(long pointerId, int injectMode) {
        return createTouchUpEvent(pointerId)
                .setInjectMode(injectMode);
    }

    public static ControlMessage createTouchUpEvent(long pointerId) {
        ControlMessage msg = new ControlMessage(TYPE_EVENT_TOUCH_UP);
        msg.pointerId = pointerId;
        return msg;
    }

    public static ControlMessage createTouchMoveEvent(long pointerId, Point point, float pressure,
                                                      int injectMode) {
        return createTouchMoveEvent(pointerId, point, pressure)
                .setInjectMode(injectMode);
    }

    public static ControlMessage createTouchMoveEvent(long pointerId, Point point, float pressure) {
        ControlMessage msg = new ControlMessage(TYPE_EVENT_TOUCH_MOVE);
        msg.pointerId = pointerId;
        msg.pressure = pressure;
        msg.point = point;
        return msg;
    }

    public static ControlMessage createWaitEvent(long milis) {
        ControlMessage msg = new ControlMessage(TYPE_EVENT_WAIT);
        msg.millis = milis;
        return msg;
    }

    public static ControlMessage createEmpty(int type, int injectMode) {
        return createEmpty(type).setInjectMode(injectMode);
    }

    public static ControlMessage createEmpty(int type) {
        ControlMessage msg = new ControlMessage(type);
        msg.type = type;
        return msg;
    }

    public int getType() {
        return type;
    }

    public long getPointerId() {
        return pointerId;
    }

    public float getPressure() {
        return pressure;
    }

    public Point getPoint() {
        return point;
    }

    public long getMillis() {
        return millis;
    }

    public int getMetaState() {
        return metaState;
    }

    public int getKeycode() {
        return keycode;
    }
    public String getText() {
        return text;
    }

    public int getRepeat() {
        return repeat;
    }

    public int getInjectMode() {
        return injectMode;
    }

    public ControlMessage setInjectMode(int injectMode) {
        this.injectMode = injectMode;
        return this;
    }

    @Override
    public String toString() {
        return "ControlMessage{" +
                "type=" + type +
                ", pointerId=" + pointerId +
                ", pressure=" + pressure +
                ", point=" + point +
                ", millis=" + millis +
                ", keycode=" + keycode +
                ", repeat=" + repeat +
                ", metaState=" + metaState +
                ", text='" + text + '\'' +
                ", injectMode=" + injectMode +
                '}';
    }
}
