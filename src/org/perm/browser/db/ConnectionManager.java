/**
 * 
 */
package org.perm.browser.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.postgresql.util.PSQLException;
import org.sesam.utils.swt.PropertyManager;


/**
 *
 * Part of Project PermBrowser
 * @author Boris Glavic
 *
 */
public class ConnectionManager {

	static Logger log = Logger.getLogger(ConnectionManager.class.getName());
	
	private static ConnectionManager instance;
	
	private Connection dbCon;
	
	private ConnectionManager () {
	}
	
	public static ConnectionManager getInstance () {
		if (instance == null) {
			instance = new ConnectionManager ();
		}
		
		return instance;
	}
	
	public boolean isConnected () throws SQLException {
		return dbCon != null && !dbCon.isClosed();
	}
	
	public Connection getConnection () throws ClassNotFoundException, SQLException {
		if (!isConnected())
			establishConnection ();
		
		return dbCon;
	}
	
	public boolean resetConnection () throws ClassNotFoundException, SQLException {
		establishConnection ();
		
		return true;
	}
	
	public void closeConnection () throws SQLException {
		if (dbCon != null) {
			dbCon.close();
			dbCon = null;
		}
	}
	
	public String[] getErrorLog () {
		return null;
	}
	
	private void establishConnection () throws ClassNotFoundException, SQLException {
		String conString;
		
		Class.forName("org.postgresql.Driver");
		
		if (dbCon != null) {
			dbCon.close();
			dbCon =  null;
		}
				
		conString = "jdbc:postgresql://" + 
				PropertyManager.getInstance().getProperty("DB.Url") +":5432/" + 
				PropertyManager.getInstance().getProperty("DB.DatabaseName");
		dbCon = DriverManager.getConnection(
				conString,
				PropertyManager.getInstance().getProperty("DB.User"),
				PropertyManager.getInstance().getProperty("DB.Password"));
	}
	
}
