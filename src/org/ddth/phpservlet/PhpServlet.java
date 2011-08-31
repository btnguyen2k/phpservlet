package org.ddth.phpservlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.caucho.quercus.servlet.QuercusServlet;

public class PhpServlet extends QuercusServlet {

    private static final long serialVersionUID = 1L;
    private String contextPath;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() throws ServletException {
        contextPath = getServletContext().getContextPath();
        super.init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object pathInfo = request.getAttribute(Constants.REQ_ATTR_PATH_INFO);
        if (pathInfo != null) {
            String requestUri = request.getRequestURI();
            String servletPath = StringUtils.substringBeforeLast(StringUtils.substringAfter(
                    requestUri, contextPath), pathInfo.toString());

            request.setAttribute(RequestDispatcher.FORWARD_CONTEXT_PATH, contextPath);
            request.setAttribute(RequestDispatcher.INCLUDE_CONTEXT_PATH, contextPath);

            request.setAttribute(RequestDispatcher.FORWARD_PATH_INFO, pathInfo);
            request.setAttribute(RequestDispatcher.INCLUDE_PATH_INFO, pathInfo);

            String queryString = request.getQueryString();
            if (!StringUtils.isEmpty(queryString)) {
                request.setAttribute(RequestDispatcher.FORWARD_QUERY_STRING, requestUri);
                request.setAttribute(RequestDispatcher.INCLUDE_QUERY_STRING, requestUri);
            }

            request.setAttribute(RequestDispatcher.FORWARD_REQUEST_URI, requestUri);
            request.setAttribute(RequestDispatcher.INCLUDE_REQUEST_URI, requestUri);

            request.setAttribute(RequestDispatcher.FORWARD_SERVLET_PATH, servletPath);
            request.setAttribute(RequestDispatcher.INCLUDE_SERVLET_PATH, servletPath);
        }
        super.service(request, response);
    }
}
