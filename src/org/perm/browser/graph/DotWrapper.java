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
	
	public static final String DOTIN_FILENAME = "dotin.dot";
	public static final String DOTOUT_FILENAME = "dotout.gif";
	
	private ProcessBuilder dotCommand;
	private Process dotProcess;
	private StringBuffer output;
	
	public DotWrapper () {
		
	}
	
	public void init () throws URISyntaxException, IOException {
		File inFile;
		File outFile;
		String dotPath;
		
		dotPath = PropertyManager.getInstance().getProperty("DotCommand");
		
		inFile = getDotFile(DOTIN_FILENAME);
		outFile = getDotFile(DOTOUT_FILENAME);
		
		dotCommand = new ProcessBuilder ();
		dotCommand.command(dotPath, "-Tgif", "-o", outFile.getAbsolutePath(), inFile.getAbsolutePath());
		
		log.debug(dotCommand.command().toString());
	}
	
	public void runDot (String commands) throws Exception {
		InputStream dotOut;
		InputStream dotErr;
		OutputStream dotIn;
		BufferedWriter commandFile;
		int e;
		
		output = new StringBuffer ();
		commandFile = new BufferedWriter (new FileWriter (getDotFile(DOTIN_FILENAME)));
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
		
		log.debug("dot returned exit value: " + e);
		log.debug(output);
		
		if (e != 0)
			throw new Exception ("An error occured in dot execution: " + output);
	}
	
	private File getDotFile (String name) throws URISyntaxException, IOException {
		URI dirUri;
		URI uri;
		File result;
		
		dirUri = ClassLoader.getSystemResource("dottemp/").toURI();
		uri = URI.create(dirUri.toString() + name);
		result = new File(uri);
		if(!result.exists())
			result.createNewFile();
		
		return result;
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
