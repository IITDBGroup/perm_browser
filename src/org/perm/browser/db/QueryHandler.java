/**
 * 
 */
package org.perm.browser.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.perm.browser.textresult.TextResultSet;


/**
 *
 * Part of Project PermBrowser
 * @author Boris Glavic
 *
 */
public class QueryHandler {

	static Logger log = Logger.getLogger(QueryHandler.class.getName());
	
	private static QueryHandler instance;
	
	private QueryHandler () {
		
	}
	
	public static QueryHandler getInstance () {
		if (instance == null) {
			instance = new QueryHandler ();
		}
		
		return instance;
	}
	
	public String getRewrittenQueryText (String query) throws SQLException, ClassNotFoundException {
		StringBuffer sql;
		Statement st;
		ResultSet rs;
		int start, stop;
		
		sql = new StringBuffer ();
		query = "EXPLAIN SQLTEXT " + query;
		
		st = ConnectionManager.getInstance().getConnection().createStatement();
		rs = st.executeQuery(query);
		
		while(rs.next())
			sql.append(rs.getString(1) + "\n");
		
		start = sql.indexOf("Query:\n---------------");
		stop = sql.indexOf("\n---------------", start + 22);
		
		return sql.subSequence(start + 23, stop).toString();
	}
	
	public String queryRemoveProv (String query) {
		int provPos;
		String untilProv;
		
		query = query.trim();
		/* check if provenance computation is top level. 
		 * If not, we cannot remove the PROVENANCE keyword without producing an error.
		 */
		provPos = query.toLowerCase().indexOf("provenance") + 10;
		
		if (provPos == -1)
			return query;
			
		untilProv = query.substring(0, provPos).replaceAll("\\W", "").toLowerCase();
		if(untilProv.compareTo("selectprovenance") != 0)
			return query;

		query = query.toLowerCase().replaceAll("(PROVENANCE|provenance)(\\s)*\\((\\w)*((\\s)*,(\\s)*(\\w)*(\\s)*)*\\)", "");
		
		return query.toLowerCase().replaceAll("PROVENANCE|provenance", "");
	}
	
	public String getQueryDotScript (String query) throws SQLException, ClassNotFoundException {
		return getRewrittenQueryDotScript(queryRemoveProv(query));
	}
	
	public TextResultSet getQueryResult (String query) throws SQLException, ClassNotFoundException {
		TextResultSet result;
		Statement st;
		ResultSet rs;
		ResultSetMetaData cols;
		int numCols;
		int[] colWidths;
		String[] colNames;
		String[] row;
		List<String[]> data;
		
		result = new TextResultSet ();
		
		st = ConnectionManager.getInstance().getConnection().createStatement();
		rs = st.executeQuery(query);
		
		/* get column information */
		cols = rs.getMetaData();
		numCols = cols.getColumnCount();
		colNames = new String[numCols];
		colWidths = new int[numCols];
		
		for(int i = 0; i < numCols; i++) {
			colNames[i] = cols.getColumnLabel(i + 1);
			colWidths[i] = colNames[i].length();
		}
		
		result.setColumns(colNames);
		
		/* get data and compute column width */
		data = new ArrayList<String[]> ();
		
		while(rs.next()) {	//TODO sanity stop at maybe 100 rows
			row = new String[numCols];
			
			for(int i = 0; i < numCols; i++) {
				row[i] = rs.getString(i + 1);
				if (row[i] == null)
					row[i] = "NULL";
				
				colWidths[i] = Math.max(colWidths[i], row[i].length());
			}
			
			data.add(row);
		}
		
		result.setColumnWidth(colWidths);
		result.setRows(data);
		
		rs.close();
		st.close();
		
		return result;
	}
	
	public String getRewrittenQueryDotScript (String query) throws SQLException, ClassNotFoundException {
		StringBuilder dot;
		int openBrackets;
		boolean inComment;
		
		query = "EXPLAIN GRAPH " + query;
		
		log.debug("get graph query:\n<" + query + ">");		
		dot = getExplainString(query);
		
		inComment = false;
		openBrackets = 0;
		for(int i = dot.indexOf("digraph {") + 9; i < dot.length(); i++) {
			if (inComment && dot.charAt(i) == '"')
				inComment = false;
			else if (dot.charAt(i) == '"')
				inComment = true;
			else if (dot.charAt(i) == '{')
				openBrackets++;
			else if (dot.charAt(i) == '}') {
				openBrackets--;
				
				if (openBrackets == 0)
					return dot.substring(0, i + 2);
			}
		}
		
		//TODO error;
		return dot.toString();
	}
	
	public String getExecutionTime (String query) throws Exception {
		StringBuilder result;
		
		query = "EXPLAIN ANALYZE " + query;
		
		result = getExplainString(query);
		
		return result.substring(result.indexOf("Total runtime:") + 15);
	}
	
	public String getCardinality (String query) throws Exception {
		StringBuilder result;
		
		query = query.trim();
		
		if(query.charAt(query.length() - 1) == ';') 
			query = query.substring(0, query.length() - 1);
		
		log.debug(query);
		query = "SELECT count(*) FROM (" + query + ") AS sub;";
		log.debug(query);
		
		result = getExplainString(query);
		
		
		return result.toString().trim();
	}
	
	public void setOption (String name, boolean on) throws SQLException, ClassNotFoundException {
		Statement st;
		String value;
		
		st = ConnectionManager.getInstance().getConnection().createStatement();
		
		if (on)
			value = "on";
		else
			value = "off";
		
		st.execute("SET " + name + " TO " + value + ";");
	}
	
	public boolean getOption (String name) throws SQLException, ClassNotFoundException {
		StringBuilder result;
		
		result = getExplainString("SHOW " + name + ";");
		
		if (result.toString().contains("on"))
			return true;
		
		return false;
	}
	
	private StringBuilder getExplainString (String query) throws SQLException, ClassNotFoundException {
		StringBuilder result;
		Statement st;
		ResultSet rs;
		
		result = new StringBuilder();
		
		st = ConnectionManager.getInstance().getConnection().createStatement();
		rs = st.executeQuery(query);
		
		while(rs.next())
			result.append(rs.getString(1) + "\n");
		
		rs.close();
		st.close();
		
		return result;
	}
}
