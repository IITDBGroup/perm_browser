/**
 * 
 */
package org.perm.browser.gui;



import java.io.File;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.MenuItem;
import org.perm.browser.db.QueryHandler;
import org.perm.browser.graph.DotGraphGenerator;
import org.perm.browser.persist.QueryHistory;
import org.perm.browser.textresult.TextResultSet;
import org.sesam.utils.common.ExceptionLogger;
import org.sesam.utils.swt.PropertyManager;

/**
 *
 * Part of Project PermBrowser
 * @author Boris Glavic
 *
 */
public class BrowserActionHandler implements SelectionListener {
	
	static Logger log = Logger.getLogger(BrowserActionHandler.class.getName());
	
	private MainGui gui;
	
	public BrowserActionHandler (MainGui gui) {
		this.gui = gui;
	}
	
	public void widgetSelected (SelectionEvent e) {
		if (e.widget instanceof Combo) {
			log.debug("query selected");
			selectQuery ();
			return;
		}
		
		if (e.widget instanceof MenuItem) {
			log.debug("menu pressed");
			handleMenu(((MenuItem) e.widget).getText());
			return;
		}
		
		if (e.widget instanceof Button) {
			handleButton(e);
		}
	}
	
	private void handleButton (SelectionEvent e) {
		Button pressedButton;
		
		pressedButton = (Button) e.widget;
		
		if (pressedButton.getText().compareTo("run") == 0) {
			log.debug("run pressed");
			runQuery ();
			return;
		}
		
		if (pressedButton.getText().compareTo("show Rewrite") == 0) {
			log.debug("show Rewrite pressed");
			showRewrite ();
			return;
		}
		
		handleOptionSwitch (pressedButton.getText());	
	}
	
	private void handleMenu (String name) {
		if (name.equals("Load History"))
			loadHistory();			
		if (name.equals("Store History")) {
			storeHistory();
		}
		if (name.equals("Clear History")) {
			QueryHistory.getInstance().clearHistory();
			gui.setQueryHistoryTexts(new String[0]);
		}
		if (name.equals("Close")) {
			gui.closeThis();
		}
	}
	
	private void loadHistory () {
		String fileName;
		
		fileName = gui.openFileDialog(SWT.OPEN);
		
		try {
			if (fileName != null) {
				QueryHistory.getInstance().loadQueryHistory(new File(fileName));
				gui.setQueryHistoryTexts(QueryHistory.getInstance().getQueries());
			}
		}
		catch (Exception e) {
			ExceptionLogger.logException(e, log);
			gui.openErrorDialog(e);
		}
	}
	
	private void storeHistory () {
		String fileName;
		
		fileName = gui.openFileDialog(SWT.SAVE);
		
		try {
			if (fileName != null)
				QueryHistory.getInstance().storeQueryHistory(new File(fileName));
		}
		catch (Exception e) {
			ExceptionLogger.logException(e, log);
			gui.openErrorDialog(e);
		}
	}
	
	private void handleOptionSwitch (String optionName) {
		boolean selection;
		String option;
		
		selection = gui.getButtonState(optionName);
		log.debug("selection for " + optionName + " is " + selection);
		option = PropertyManager.getInstance().getProperty("ButtonOption." + optionName.replaceAll(" ", "_"));
		
		try {
			QueryHandler.getInstance().setOption(option, selection);
		}
		catch (Exception e) {
			ExceptionLogger.logException(e, log);
			gui.openErrorDialog(e);
		}
	}
	
	private void selectQuery () {
		String query;
		
		query = QueryHistory.getInstance().getNthQuery(gui.getSelectedQueryPos());
		gui.setQueryText(query);
	}
	
	private void addQuery (String query) {
		QueryHistory.getInstance().addQuery(query);
		
		gui.setQueryHistoryTexts(QueryHistory.getInstance().getShortQueries());
	}
	
	private void runQuery () {
		String query;
		String queryWithoutProv;
		String result;
		TextResultSet queryResult;
		
		query = gui.getQueryString();
		
		try {
			addQuery (query);
			queryWithoutProv = QueryHandler.getInstance().queryRemoveProv(query);
			log.debug(queryWithoutProv);
			
			result = QueryHandler.getInstance().getRewrittenQueryText(query);
			gui.setPQueryText(result);
			
			result = QueryHandler.getInstance().getRewrittenQueryDotScript(query);
			DotGraphGenerator.getInstance().generateGraph(result);
			gui.setPAlgebraImage(loadImageData("dottemp/dotout.jpg"));
			
			result = QueryHandler.getInstance().getQueryDotScript(query);
			DotGraphGenerator.getInstance().generateGraph(result);
			gui.setAlgebraImage(loadImageData("dottemp/dotout.jpg"));
			
			result = QueryHandler.getInstance().getExecutionTime(query);
			gui.setLabelText("ProvTime", result);
			
			result = QueryHandler.getInstance().getExecutionTime(queryWithoutProv);
			gui.setLabelText("NormTime", result);
			
			result = QueryHandler.getInstance().getCardinality(query);
			gui.setLabelText("ProvTuples", result);
			
			result = QueryHandler.getInstance().getCardinality(queryWithoutProv);
			gui.setLabelText("NormTuples", result);
			
			queryResult = QueryHandler.getInstance().getQueryResult(query);
			setResultTuplesOnTextWidget (queryResult);
		}
		catch (Exception e) {
			ExceptionLogger.logException(e, log);
			gui.openErrorDialog(e);
		}
	}
	
	
	private void setResultTuplesOnTextWidget (TextResultSet result) {
		gui.setResultText(result.toString());
		gui.setResultStyles(result.getStyles());
	}
	
	private ImageData loadImageData (String filename) {
		ImageData result;
		
		result = new ImageData (ClassLoader.getSystemResourceAsStream(filename));
			
		return result;
	}
	
	
	private void showRewrite () {
		String query;
		String result;
		
		query = gui.getQueryString();
		
		try {
			addQuery (query);
			
			result = QueryHandler.getInstance().getRewrittenQueryText(query);
			gui.setPQueryText(result);
			
			result = QueryHandler.getInstance().getRewrittenQueryDotScript(query);
			DotGraphGenerator.getInstance().generateGraph(result);
			gui.setPAlgebraImage(loadImageData("dottemp/dotout.jpg"));
			
			result = QueryHandler.getInstance().getQueryDotScript(query);
			DotGraphGenerator.getInstance().generateGraph(result);
			gui.setAlgebraImage(loadImageData("dottemp/dotout.jpg"));
		}
		catch (Exception e) {
			ExceptionLogger.logException(e, log);
			gui.openErrorDialog(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected (SelectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}


}
