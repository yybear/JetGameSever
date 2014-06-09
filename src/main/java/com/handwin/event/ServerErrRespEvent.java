package com.handwin.event;

import com.handwin.util.Constants;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-21 下午3:44
 */
public class ServerErrRespEvent extends Event {
    private int code;
    private String msg;

    public ServerErrRespEvent(String msg) {
        this.code = Constants.GAME_SERVER_ERR;
        if(msg == null)
            this.msg = "server internal error";
        else
            this.msg = msg;
        this.type = Events.SERVER_ERR;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
