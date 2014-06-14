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
