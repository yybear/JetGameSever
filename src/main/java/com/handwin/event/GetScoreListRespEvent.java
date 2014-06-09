package com.handwin.event;

import com.handwin.entity.UserScore;

import java.util.List;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-4 下午1:35
 */
public class GetScoreListRespEvent extends Event {

    private List<UserScore> userScores;

    public GetScoreListRespEvent(List<UserScore> userScores) {
        this.userScores = userScores;
        this.type = Events.SCORE_LIST;
    }

    public List<UserScore> getUserScores() {
        return userScores;
    }

    public void setUserScores(List<UserScore> userScores) {
        this.userScores = userScores;
    }
}
