package org.example.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    private static final String COOKIE_NAME = "AUTH_TOKEN";

    public static Cookie createJwtCookie(HttpServletResponse httpServletResponse, String token, int exTime) {
        //1. create cookie
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(exTime); // 1 day

        //2. set cookie
        httpServletResponse.addCookie(cookie);
        return cookie;
    }

    /**
     * Clears JWT cookie (Logout)
     */
    public static Cookie clearJwtCookie(HttpServletResponse httpServletResponse) {
        //1. cookie
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // ⬅️ delete immediately

        //2. add into res
        httpServletResponse.addCookie(cookie);

        return cookie;
    }
}
