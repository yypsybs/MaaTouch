package com.shxyke.MaaTouch;

import java.util.LinkedList;

public class QueueWithInjectMode<C> extends LinkedList<C> {

    private String token;

    public String getToken() {
        return token;
    }

    public QueueWithInjectMode<C> setToken(String token) {
        this.token = token;
        return this;
    }

}
