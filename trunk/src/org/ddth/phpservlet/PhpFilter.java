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
	private ConcurrentMap<String, PhpScriptInfo> cache;

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
		cache = new MapMaker().concurrencyLevel(4).maximumSize(1024)
				.expireAfterWrite(5, TimeUnit.MINUTES).makeMap();
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
		String resourcePath = StringUtils.substringAfter(requestUri,
				contextPath);

		String[] tokens = StringUtils.split(resourcePath, '/');
		// check longest path first!
		int index = tokens.length - 1;
		do {
			String cacheValue = cache.get(resourcePath);
			if (cacheValue != null) {
				return !cacheValue.equals("") ? cacheValue : null;
			}
			URL resource = servletContext.getResource(resourcePath);
			if (resource == null) {
				String token = "/" + tokens[index];
				resourcePath = StringUtils.substringBeforeLast(resourcePath,
						token);
				// continue to check
				index--;
				continue;
			}
			// found a resource
			if (resourcePath.toLowerCase().endsWith(".php")) {
				// it's PHP script
				cacheValue = resourcePath;
			} else {
				// it's not
				cacheValue = "";
			}
			cache.put(resourcePath, cacheValue);
			return !cacheValue.equals("") ? cacheValue : null;
		} while (index >= 0);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws ServletException, IOException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String phpScript = isPhpRequest(httpRequest);
		if (phpScript != null) {
			RequestDispatcher dispatcher = servletContext
					.getNamedDispatcher(quercusServletName);
			// RequestDispatcher dispatcher = servletContext
			// .getRequestDispatcher(phpScript);
			dispatcher.forward(request, response);
		} else {
			chain.doFilter(request, response);
		}
		return;
	}
}
