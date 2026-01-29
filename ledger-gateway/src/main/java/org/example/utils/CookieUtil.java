package org.example.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    private static final String COOKIE_NAME = "AUTH_TOKEN";

    // Set to false for local development (HTTP), true for production (HTTPS)
    private static final boolean IS_PRODUCTION = false;

    public static Cookie createJwtCookie(HttpServletResponse httpServletResponse, String token, int exTime) {

        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(exTime);

        // ❗ Force Secure when FE is HTTPS (cross-site cookies REQUIRE this)
        cookie.setSecure(true);

        // Always set cookie manually with SameSite=None for cross-origin auth
        httpServletResponse.setHeader("Set-Cookie",
                String.format(
                        "%s=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                        COOKIE_NAME, token, exTime
                )
        );

        return cookie;
    }


    /**
     * Clears JWT cookie (Logout)
     */
    public static Cookie clearJwtCookie(HttpServletResponse httpServletResponse) {

        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        // Must also be Secure + SameSite=None to delete properly
        cookie.setSecure(true);

        httpServletResponse.setHeader("Set-Cookie",
                String.format(
                        "%s=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=None",
                        COOKIE_NAME
                )
        );

        return cookie;
    }

}
