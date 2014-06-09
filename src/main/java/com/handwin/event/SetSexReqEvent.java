package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-5-21 上午11:05
 */
public class SetSexReqEvent extends Event {
    private int sex;


    public SetSexReqEvent(int sex) {
        this.sex = sex;
        this.type = Events.SAVE_SEX;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }
}
