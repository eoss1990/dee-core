package com.seeyon.v3x.dee.util.rest.user;

/**
 * NullUserToken
 *
 * @author zhangfb
 */
public class NullUserToken extends UserToken {
    private static final NullUserToken INSTANCE = new NullUserToken();

    private NullUserToken() {
        super("");
    }

    public static UserToken getInstance() {
        return INSTANCE;
    }

    public String getId() {
        return "-1";
    }
}
