package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-21 上午11:34
 */
public class WaitReqEvent extends Event {
    private Object data;

    public WaitReqEvent(Object data) {
        this.data = data;
        this.type = Events.JOIN_WAIT_QUEUE;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
