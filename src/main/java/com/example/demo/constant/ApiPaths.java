package com.example.demo.constant;

public final class ApiPaths {

    private ApiPaths() {} // Prevent instantiation

    public static final String AUTH_BASE = "/api/auth";

    // Auth endpoints
    public static final String SIGNUP = "/signup";
    public static final String LOGIN = "/login";
    public static final String LOGOUT = "/logout";
    public static final String SSO_LOGIN = "/sso/login";
    public static final String SSO_SUCCESS = "/sso/success";
    public static final String DELETE_ME = "/me";

    public static final String GOOGLE_AUTHORIZATION_URL = "/oauth2/authorization/google";
}
