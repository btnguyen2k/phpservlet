package org.ddth.phpservlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.MapMaker;

public class PhpFilter implements Filter {

    private final static String PARAM_NAME = "quercusServletName";
    private String quercusServletName = "QuercusServlet";
    private String contextPath;
    private ServletContext servletContext;
    private ConcurrentMap<String, Boolean> cache;
    private ConcurrentMap<String, PhpScriptInfo> cache2;

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        if (cache != null) {
            cache.clear();
            cache = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        servletContext = config.getServletContext();
        contextPath = servletContext.getContextPath();
        String quercusServletName = config.getInitParameter(PARAM_NAME);
        if (quercusServletName != null) {
            this.quercusServletName = quercusServletName;
        }
        cache = new MapMaker().concurrencyLevel(4).maximumSize(1024).expireAfterWrite(5,
                TimeUnit.MINUTES).makeMap();
        cache2 = new MapMaker().concurrencyLevel(4).maximumSize(10240).expireAfterWrite(5,
                TimeUnit.MINUTES).makeMap();
    }

    private PhpScriptInfo buildLevel2CacheEntry(String resourcePath, String pathInfo) {
        PhpScriptInfo phpScriptInfo = new PhpScriptInfo(resourcePath, pathInfo);
        cache2.put(resourcePath, phpScriptInfo);
        return phpScriptInfo;
    }

    /**
     * Checks if the current request invokes a PHP script.
     * 
     * @param httpRequest
     *            HttpServletRequest
     * @return PhpScriptInfo if the request invokes a PHP script,
     *         <code>null</code> otherwises
     * @throws MalformedURLException
     */
    protected PhpScriptInfo isPhpRequest(HttpServletRequest httpRequest)
            throws MalformedURLException {
        String requestUri = httpRequest.getRequestURI();
        // remove the context path
        String resourcePath = StringUtils.substringAfter(requestUri, contextPath);

        // check level 2 cache first
        PhpScriptInfo cacheValue2 = cache2.get(resourcePath);
        if (cacheValue2 != null) {
            return cacheValue2;
        }

        String[] tokens = StringUtils.split(resourcePath, '/');
        // check longest path first!
        int index = tokens.length - 1;
        do {
            String pathInfo = StringUtils.substringAfter(requestUri, resourcePath);
            Boolean cacheValue = cache.get(resourcePath);
            if (cacheValue != null) {
                return cacheValue ? buildLevel2CacheEntry(resourcePath, pathInfo) : null;
            }
            URL resource = servletContext.getResource(resourcePath);
            if (resource == null) {
                String token = "/" + tokens[index];
                resourcePath = StringUtils.substringBeforeLast(resourcePath, token);
                // continue to check
                index--;
                continue;
            }
            // the request should invoke a resource
            if (resourcePath.toLowerCase().endsWith(".php")) {
                // it's PHP script
                cacheValue = Boolean.TRUE;
            } else {
                // it's not
                cacheValue = Boolean.FALSE;
            }
            cache.put(resourcePath, cacheValue);
            return cacheValue ? buildLevel2CacheEntry(resourcePath, pathInfo) : null;
        } while (index >= 0);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        PhpScriptInfo phpScriptInfo = isPhpRequest(httpRequest);
        if (phpScriptInfo != null) {
            String pathInfo = phpScriptInfo.getPathInfo();
            if (!StringUtils.isEmpty(pathInfo)) {
                request.setAttribute(Constants.REQ_ATTR_PATH_INFO, pathInfo);
            }
            RequestDispatcher dispatcher = servletContext.getNamedDispatcher(quercusServletName);
            // RequestDispatcher dispatcher =
            // servletContext.getRequestDispatcher(phpScriptInfo
            // .getScriptPath());
            dispatcher.forward(request, response);
        } else {
            chain.doFilter(request, response);
        }
        return;
    }
}
