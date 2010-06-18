/**
 * 
 */
package org.perm.browser.graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.sesam.utils.swt.PropertyManager;


/**
 *
 * Part of Project PermBrowser
 * @author Boris Glavic
 *
 */
public class DotWrapper {

	static Logger log = Logger.getLogger (DotWrapper.class.getName());
	
	private ProcessBuilder dotCommand;
	private Process dotProcess;
	private StringBuffer output;
	
	public DotWrapper () {
		
	}
	
	public void init () throws URISyntaxException {
		File inFile;
		File outFile;
		String dotPath;
		
		dotPath = PropertyManager.getInstance().getProperty("DotCommand");
		
		inFile = getDotFile("dotin.dot");
		outFile = getDotFile("dotout.jpg");
		
		dotCommand = new ProcessBuilder ();
		dotCommand.command(dotPath, "-Tjpg", "-o", outFile.getAbsolutePath(), inFile.getAbsolutePath());
		
		log.debug(dotCommand.command().toString());
	}
	
	public void runDot (String commands) throws Exception {
		InputStream dotOut;
		InputStream dotErr;
		OutputStream dotIn;
		BufferedWriter commandFile;
		int e;
		
		output = new StringBuffer ();
		commandFile = new BufferedWriter (new FileWriter (getDotFile("dotin.dot")));
		commandFile.write(commands);
		commandFile.close();
		
		dotProcess = dotCommand.start();
		
		dotOut = dotProcess.getInputStream();
		dotErr = dotProcess.getErrorStream();
		dotIn = dotProcess.getOutputStream();
		
		while((e = getExitValue ()) == -1) {
			Thread.sleep(100);
			readStreams (dotOut,dotErr);
		}
		
		readStreams (dotOut, dotErr);
		
		log.debug(e);
		log.debug(output);
		//TODO error handling
	}
	
	private File getDotFile (String name) throws URISyntaxException {
		URI uri;
		
		uri = ClassLoader.getSystemResource("dottemp/" + name).toURI();
		return new File(uri);
	}
	
	private void readStreams (InputStream out, InputStream err) throws IOException {
		int c;
		
		while ((c = out.read()) != -1)
			output.append((char) c);
		
		while ((c = err.read()) != -1)
			output.append((char) c);
	}
	
	private int getExitValue () {
		int exitValue;
		
		try {
			log.debug("get exit value");
			exitValue = dotProcess.exitValue();
			log.debug("exit value finished");
			return exitValue;
		}
		catch (IllegalThreadStateException e) {
			log.debug("exit value finished");
			return -1;
		}
	}
	
}
