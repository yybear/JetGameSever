package com.handwin.event;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-4 下午1:55
 */
public class GetScoreListReqEvent extends Event {
    private int count;

    public GetScoreListReqEvent(int count) {
        this.count = count;
        this.type = Events.SCORE_LIST;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
