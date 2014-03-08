/* 
 * 
 * PROJECT
 *     Name
 *         APS Web Tools
 *     
 *     Code Version
 *         0.10.0
 *     
 *     Description
 *         This provides some utility classes for web applications.
 *         
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *     
 * LICENSE
 *     Apache 2.0 (Open Source)
 *     
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *     
 * AUTHORS
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2013-02-03: Created!
 *         
 */
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
