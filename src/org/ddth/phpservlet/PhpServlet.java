package org.ddth.phpservlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.caucho.quercus.servlet.QuercusServlet;

public class PhpServlet extends QuercusServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Object pathInfo = request.getAttribute(Constants.REQ_ATTR_PATH_INFO);
		if (pathInfo != null) {
			ServerEnv serverEnv = createServerEnv();
			serverEnv.setProperty("PATH_INFO", pathInfo.toString());
		}
		super.service(request, response);
	}
}
