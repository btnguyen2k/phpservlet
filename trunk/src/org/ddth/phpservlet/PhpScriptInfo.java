package org.ddth.phpservlet;

/**
 * Encapsulates information of a the requesting PHP script.
 * 
 * @author Thanh Ba Nguyen <btnguyen2k@gmail.com>
 */
public class PhpScriptInfo {
	private String scriptPath;
	private String pathInfo;

	public PhpScriptInfo() {
	}

	public PhpScriptInfo(String scriptPath, String pathInfo) {
		this.scriptPath = scriptPath;
		this.pathInfo = pathInfo;
	}

	/**
	 * Getter for scriptPath
	 * 
	 * @return scriptPath String
	 */
	public String getScriptPath() {
		return scriptPath;
	}

	/**
	 * Setter for scriptPath
	 * 
	 * @param scriptPath
	 *            String
	 */
	public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}

	/**
	 * Getter for pathInfo
	 * 
	 * @return pathInfo String
	 */
	public String getPathInfo() {
		return pathInfo;
	}

	/**
	 * Setter for pathInfo
	 * 
	 * @param pathInfo
	 *            String
	 */
	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}
}
