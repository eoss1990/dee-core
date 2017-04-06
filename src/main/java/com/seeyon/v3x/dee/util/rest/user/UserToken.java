package com.seeyon.v3x.dee.util.rest.user;

import java.util.UUID;

/**
 * UserToken
 *
 * @author zhangfb
 */
public class UserToken {
    private String sessionId;

    public UserToken(String userName) {
        this.sessionId = UUID.randomUUID().toString();
    }

    public String getId() {
        return this.sessionId;
    }

    public static final UserToken getNullToken() {
        return NullUserToken.getInstance();
    }
}
