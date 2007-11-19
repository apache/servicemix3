package org.apache.servicemix.openejb;

import java.util.Arrays;
import java.util.List;

import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.webservices.WsRegistry;
import org.osgi.service.http.HttpService;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Nov 14, 2007
 * Time: 11:59:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class OsgiWsRegistry implements WsRegistry {

    private HttpService httpService;

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public List<String> setWsContainer(String virtualHost, String contextRoot, String servletName, HttpListener wsContainer) throws Exception {
        System.err.println("setWsContainer");
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clearWsContainer(String virtualHost, String contextRoot, String servletName) {
        System.err.println("clearWsContainer");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> addWsContainer(String path, HttpListener httpListener, String virtualHost, String realmName, String transportGuarantee, String authMethod, ClassLoader classLoader) throws Exception {
        if (path == null) throw new NullPointerException("contextRoot is null");
        if (httpListener == null) throw new NullPointerException("httpListener is null");

        // assure context root with a leading slash
        if (!path.startsWith("/")) path = "/" + path;

        httpService.registerServlet(path, new WsServlet(httpListener), null, httpService.createDefaultHttpContext());

        return Arrays.asList("http://localhost" + path);  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeWsContainer(String path) {
        httpService.unregister(path);
    }
}
