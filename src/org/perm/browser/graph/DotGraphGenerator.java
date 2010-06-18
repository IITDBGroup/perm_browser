/**
 * 
 */
package org.perm.browser.graph;

import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.sesam.utils.common.ExceptionLogger;


/**
 *
 * Part of Project PermBrowser
 * @author Boris Glavic
 *
 */
public class DotGraphGenerator {

	static Logger log = Logger.getLogger (DotGraphGenerator.class.getName());
	
	private static DotGraphGenerator instance;
	
	private DotWrapper dot;
	
	private DotGraphGenerator () {
		dot = new DotWrapper ();
		try {
			dot.init();
		}
		catch (URISyntaxException e) {
			ExceptionLogger.logException(e, log);
		}
	}
	
	public static DotGraphGenerator getInstance () {
		if (instance == null)
			instance = new DotGraphGenerator ();
		
		return instance;
	}
	
	public void generateGraph (String dotCommands) throws Exception {
		dot.runDot(dotCommands);
	}
	
	
}
