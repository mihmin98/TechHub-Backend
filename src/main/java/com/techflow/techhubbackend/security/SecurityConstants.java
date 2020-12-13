package com.techflow.techhubbackend.security;

public class SecurityConstants {
    public static final String SECRET = "fY4fLfqxzMPayP5x";
    public static final long EXPIRATION_TIME = 864_000_000; // 10 days
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String JSON_TOKEN_KEY = "accessToken";
    public static final String SIGN_UP_URL = "/createUser";
}
