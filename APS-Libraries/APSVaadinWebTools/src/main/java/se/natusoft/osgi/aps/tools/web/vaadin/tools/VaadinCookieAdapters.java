/* 
 * 
 * PROJECT
 *     Name
 *         APS Vaadin Web Tools
 *     
 *     Code Version
 *         0.11.0
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
 *     tommy ()
 *         Changes:
 *         2014-06-29: Created!
 *         
 */
package se.natusoft.osgi.aps.tools.web.vaadin.tools;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import se.natusoft.osgi.aps.tools.web.CookieTool;

import javax.servlet.http.Cookie;

/**
 * A grouping class for Vaadin cookie adapters.
 */
public interface VaadinCookieAdapters {

    /**
     * A VaadinRequest cookie reader adapter.
     */
    public static class VaadinRequestCookieReaderAdapter implements CookieTool.CookieReader {
        private VaadinRequest request;

        public VaadinRequestCookieReaderAdapter(VaadinRequest request) {
            this.request = request;
        }

        public Cookie[] readCookies() {
            return this.request.getCookies();
        }
    }

    /**
     * A VaadinResponse cookie writer adapter.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static class VaadinResponseCookieWriterAdapter implements CookieTool.CookieWriter {
        private VaadinResponse response;

        public VaadinResponseCookieWriterAdapter(VaadinResponse response) {
            this.response = response;
        }

        public void writeCookie(Cookie cookie) {
            this.response.addCookie(cookie);
        }
    }
}
