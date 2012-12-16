package se.natusoft.osgi.aps.tools.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides simple static cookie tools.
 */
public class CookieTool {

    /**
     * Returns the cookie having the specified name or null if none is found.
     *
     * @param cookies The complete set of cookies from the request.
     */
    public static String getCookie(Cookie[] cookies, String name) {
        Cookie found = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                found = cookie;
                break;
            }
        }

        return found != null ? found.getValue() : null;
    }

    /**
     * Sets a cookie on the specified response.
     *
     * @param resp The servlet response to set the cookie on.
     * @param name The name of the cookie.
     * @param value The value of the cookie.
     * @param maxAge The max age of the cookie.
     */
    public static void setCookie( HttpServletResponse resp, String name, String value, int maxAge, String path) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        cookie.setPath(path);
        resp.addCookie(cookie);
    }

    /**
     * Removes a cookie.
     *
     * @param name The name of the cookie to remove.
     * @param resp The servlet response to remove the cookie on.
     */
    public void deleteCookie(String name, HttpServletResponse resp) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        resp.addCookie(cookie);
    }
}
