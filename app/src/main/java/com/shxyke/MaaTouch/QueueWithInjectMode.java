package com.shxyke.MaaTouch;

import java.util.LinkedList;

public class QueueWithInjectMode<C> extends LinkedList<C> {

    private Integer injectMode;

    public Integer getInjectMode() {
        return injectMode;
    }

    public QueueWithInjectMode<C> setInjectMode(Integer injectMode) {
        this.injectMode = injectMode;
        return this;
    }

}
