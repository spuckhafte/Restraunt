package com.swe.backend.auth;

public final class AuthRequestContext {
    public static final String ATTR_PRINCIPAL = "auth.principal";
    public static final String HEADER_SESSION_TOKEN = "X-Session-Token";

    private AuthRequestContext() {
    }
}
