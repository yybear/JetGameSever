package com.handwin.event;

/**
 * User: roger
 * Date: 13-12-13 下午2:32
 */
public enum StatusCode {
    ONLINE(0x02), READY(0x03), START(0x04), OVER(0x05), OFFLINE(0x06), RESET(0x08);
    private int type;
    private StatusCode(int type) {
        this.type = type;
    }

    public int type() {
        return type;
    }

}
