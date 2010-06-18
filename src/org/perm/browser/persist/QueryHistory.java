/**
 * 
 */
package org.perm.browser.persist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sesam.utils.swt.PropertyManager;


/**
 *
 * Part of Project PermBrowser
 * @author Boris Glavic
 *
 */
public class QueryHistory {

	static Logger log = Logger.getLogger(QueryHistory.class.getName());
	
	public static final int MAX_QUERY_SIZE = 60;
	
	private static QueryHistory instance;
	
	private List<String> queries;
	
	private QueryHistory () {
		queries = new ArrayList<String> ();
	}
	
	public static QueryHistory getInstance () {
		if (instance == null) {
			instance = new QueryHistory ();
		}
		return instance;
	}
	
	public void addQuery (String query) {
		queries.add(query);
	}
	
	public int getHistorySize () {
		return queries.size();
	}
	
	public String getLastQuery () {
		if (queries.size() == 0)
			return "";
		
		return queries.get(queries.size() - 1);
	}
	
	public String[] getQueries () {
		return queries.toArray(new String[] {});
	}
	
	public String[] getShortQueries () {
		String[] result;
		String query;
		
		result = new String[queries.size()];
		
		for(int i = 0; i < queries.size(); i++) {
			query = queries.get(i);
			
			if (query.length() > MAX_QUERY_SIZE) {
				query = query.replaceAll("\\W"," ").substring(0, MAX_QUERY_SIZE - 3) + "...";
			}
			
			result[i] = query;
		}
		
		return result;
	}
	
	public String getNthQuery (int n) {
		return queries.get(n);
	}
	
	public void storeQueryHistory () throws URISyntaxException, IOException {
		String filename;
		File outFile;
		
		filename = PropertyManager.getInstance().getProperty("QueryHistoryFileName");
		outFile = new File(ClassLoader.getSystemResource("logs").toURI());
		outFile = new File(outFile, "history.log");
		
		if(!outFile.exists())
			outFile.createNewFile();
		
		storeQueryHistory (outFile);
	}
	
	public void storeQueryHistory (File file) throws IOException {
		Properties history;
		OutputStream out;
		
		history = new Properties ();

		for (int i = 0; i < queries.size(); i++) {
			history.setProperty("query" + i, queries.get(i));
		}
		
		out = new FileOutputStream(file);
		history.storeToXML(out, "query log");
	}
	
	public void loadQueryHistory () {
		String filename;
		File inFile;
		Properties history;
		
		filename = "logs/" + PropertyManager.getInstance().getProperty("QueryHistoryFileName");
		history = new Properties ();
		queries = new ArrayList<String> ();
		
		try {
			inFile = new File(ClassLoader.getSystemResource(filename).toURI());
			history.loadFromXML(new FileInputStream(inFile));
			addLoadedQueries(history);
		}
		catch (Exception e) {
			log.error(e);
		}
	}
	
	public void loadQueryHistory (File file) throws Exception {
		Properties history;
		
		history = new Properties();
		history.loadFromXML(new FileInputStream(file));
		
		addLoadedQueries(history);
	}
	
	public void clearHistory () {
		queries = new ArrayList<String> ();
	}
	
	private void addLoadedQueries (Properties history) {
		queries = new ArrayList<String> ();
		
		for(int i = 0; (history.containsKey("query" + i)); i++)
			queries.add(history.getProperty("query" + i));
	}
	
}
