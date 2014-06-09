package com.handwin.util;

import com.google.common.base.Preconditions;

/**
 * Created by sunhao on 13-12-24.
 */
public enum RedisKey {
    RANDOM_MATCH_RESULT("randomMatch_result_"),
    USER_SESSION_MD5("USER_SESSION_"),
    PLAYER_STATUS("PLAYER_STATUS_"),
    USER_SESSION_JSON("USER_SESSION_JSON")
    ;

    private RedisKey(String key) {
        this.key = Preconditions.checkNotNull(key);
    }

    private String key;

    @Override
    public String toString() {
        return this.key;
    }
}
