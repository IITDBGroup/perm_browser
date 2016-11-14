/**
 * 
 */
package org.perm.browser.gui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.perm.browser.db.ConnectionManager;
import org.perm.browser.db.QueryHandler;
import org.perm.browser.persist.QueryHistory;
import org.perm.browser.util.SWTResourceManager;
import org.sesam.utils.common.ExceptionLogger;
import org.sesam.utils.common.FileProperties;
import org.sesam.utils.swt.PropertyManager;


/**
 *
 * Part of Project PermBrowser
 * @author Boris Glavic
 *
 */
public class MainGui extends ShellAdapter {

	static Logger log = Logger.getLogger(MainGui.class.getName());
	
	private static final int LOGO_HEIGHT = 80;
	
	private Shell sShell = null;
	private Composite mainComposite = null;
	
	private Group topGroup = null;
	private Group textGroup = null;
	
	private Group queryGroup = null;
	private Text queryText = null;
	
	private Group pQueryGroup = null;
	private Text pQueryText = null;
	
	private Group optionsGroup = null;
	private Group rewriteOptionGroup = null;
	private Group semanOptionGroup = null;
	private Group actionsGroup = null;
	private Combo queryHistoryDrop = null;
	
	private Group algebraGroup = null;
	private Label qTreeLabel = null;
	private Label pTreeLabel = null;
	private ImageViewer algebraComp = null;
	private ImageViewer pAlgebraComp = null; 
	
	private Group resultGroup = null;
	private StyledText resultText = null;
	
	private SelectionListener buttonListener;
	private HashMap<String,Label> labelMap;
	private HashMap<String,Button> buttonMap;
	
	public static void main (String[] args) {
		MainGui inst;
		
		try {
			inst = new MainGui ();
		}
		catch (Exception e) {
			ExceptionLogger.logException(e, log);
		}
	}
	
	public MainGui () {
		Display display;
		
		init();
		display = new Display ();
		
		initGui (display);
		
		sShell.open();
		while (!sShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	private void init () {
		URI uri;
		
		try {
			labelMap = new HashMap<String,Label> ();
			buttonMap = new HashMap<String,Button> ();
			
			PropertyConfigurator.configure(ClassLoader.getSystemResource("log4jproperties.txt"));
			uri = ClassLoader.getSystemResource("conf/").toURI();
			PropertyManager.init(new File(uri));
			QueryHistory.getInstance().loadQueryHistory();
			
			createListeners ();
		}
		catch (Throwable e) {
			log.error(e);
			
			if (e instanceof Exception) {
				ExceptionLogger.logException((Exception) e, log);
				openErrorDialog ((Exception) e);
			}
		}
	}
	
	private void initGui (Display display) {
		createListeners ();
		
		sShell = new Shell(display);
		sShell.addShellListener(this);
		
		allocateFontsAndColors ();
		
		sShell.setText("PermBrowser");
		sShell.setLayout(new FillLayout());
		createMainComposite();
		sShell.setSize(new Point(800, 600));
		
		createMenu ();
		
		queryText.setText(QueryHistory.getInstance().getLastQuery());
		
		initializeOptionButtons ();
	}
	
	public String getQueryString () {
		return queryText.getText();
	}
	
	public void setQueryText (String text) {
		queryText.setText(text);
	}
	
	public void setPQueryText (String text) {
		pQueryText.setText(text);
	}
	
	public void setResultText (String text) {
		resultText.setText(text);
	}
	
	public void setResultStyles (StyleRange[] ranges) {
		resultText.setStyleRanges(ranges);
	}
	
	public void setAlgebraImage (ImageData algebraData) {
		algebraComp.setImageData(algebraData);
	}
	
	public void setPAlgebraImage (ImageData pAlgebraData) {
		pAlgebraComp.setImageData(pAlgebraData);
	}
	
	public Display getDisplay () {
		return sShell.getDisplay();
	}
	
	public void setQueryHistoryTexts (String[] queries) {
		queryHistoryDrop.setItems(queries);
	}
	
	public String getSelectedQuery () {
		return queryHistoryDrop.getItem(queryHistoryDrop.getSelectionIndex());
	}
	
	public int getSelectedQueryPos () {
		return queryHistoryDrop.getSelectionIndex();
	}
	
	public void setLabelText (String labelName, String text) {
		Label label;
		
		label = labelMap.get(labelName);
		label.setText(text);
	}
	
	public boolean getButtonState (String name) {
		Button button;
		
		button = buttonMap.get(name);
		
		return button.getSelection();
	}
	
	public void setButtonState (String name, boolean selected) {
		Button button;
		
		button = buttonMap.get(name);
		button.setSelection(selected);
	}
	
	public String openFileDialog (int flags) {
		FileDialog dialog;
		
		dialog = new FileDialog (sShell, flags);
		return dialog.open();
	}
	
	public void closeThis () {
		sShell.close();
	}
	
	private void createMenu () {
		Menu menu;
		Menu subMenu;
		MenuItem item;
		
		menu = new Menu(sShell, SWT.BAR);
		
		item = new MenuItem(menu, SWT.CASCADE);
		item.setText("File");
		
		subMenu = new Menu(menu);
		item.setMenu(subMenu);
		
		addMenuItem("Load History", subMenu);
		addMenuItem("Store History", subMenu);
		addMenuItem("Clear History", subMenu);
		addMenuItem("Close", subMenu);
		
		sShell.setMenuBar(menu);
	}
	
	private void addMenuItem (String name, Menu menu) {
		MenuItem subItem;
		
		subItem = new MenuItem(menu, SWT.NONE);
		subItem.setText(name);
		subItem.addSelectionListener(buttonListener);
	}
	
	private void createMainComposite () {
		GridData gridData;
		GridLayout layout; 
		
		layout = new GridLayout ();
		layout.makeColumnsEqualWidth = true;
		layout.numColumns = 1;
			
		mainComposite = new Composite(sShell, SWT.NONE);
		mainComposite.setLayout(layout);
		
		gridData = new GridData ();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.minimumHeight = 450;
		gridData.horizontalSpan = 1;
		
		layout = new GridLayout ();
		layout.makeColumnsEqualWidth = true;
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		
		topGroup = new Group(mainComposite,SWT.NONE);
		topGroup.setLayout(layout);
		topGroup.setLayoutData(gridData);
		
		createTextGroup();
		createAlgebraGroup();
		createResultGroup();
	}
	
	private void addQueryHistoryDropDown () {
		GridData gridData;
		
		gridData = new GridData ();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = 30;
		gridData.horizontalSpan = 1;
		
		queryHistoryDrop = new Combo(actionsGroup, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		queryHistoryDrop.setLayoutData(gridData);
		queryHistoryDrop.addSelectionListener(buttonListener);
		queryHistoryDrop.setItems(QueryHistory.getInstance().getShortQueries());
		
	}
	
	private void createTextGroup () {
		GridData gridData;
		GridLayout layout; 
		
		layout = new GridLayout ();
		layout.makeColumnsEqualWidth = false;
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		
		gridData = new GridData ();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		
		textGroup = new Group(topGroup, SWT.NONE);
		textGroup.setLayoutData(gridData);
		textGroup.setLayout(layout);
		
		createProllImages ();
		createActionsGroup ();
		
		gridData = new GridData ();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.minimumHeight = 100;
		gridData.heightHint = 300;
		gridData.horizontalSpan = 2;
		
		queryGroup = new Group(textGroup, SWT.NONE);
		queryGroup.setText("Query");
		queryGroup.setLayout(new FillLayout());
		queryGroup.setLayoutData(gridData);
		
		queryText = new Text(queryGroup, SWT.V_SCROLL | SWT.H_SCROLL);
		
		gridData = new GridData ();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.minimumHeight = 100;
		gridData.heightHint = 300;
		gridData.horizontalSpan = 2;
		
		pQueryGroup = new Group(textGroup, SWT.NONE);
		pQueryGroup.setText("Provenance Query");
		pQueryGroup.setLayout(new FillLayout());
		pQueryGroup.setLayoutData(gridData);
		
		pQueryText = new Text(pQueryGroup, SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		
		createOptionsGroup();
	}
	
	private void createOptionsGroup () {
		GridData gridData;
		GridLayout layout;
		
		gridData = new GridData ();
		gridData.heightHint = 130;
		gridData.minimumHeight = 130;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		
		optionsGroup = new Group(textGroup, SWT.NONE);
		optionsGroup.setLayout(new FillLayout(SWT.HORIZONTAL));
		optionsGroup.setLayoutData(gridData);
		
		layout = new GridLayout ();
		layout.makeColumnsEqualWidth = true;
		layout.numColumns = 1;
		
		gridData = new GridData ();
		gridData.heightHint = 160;
		gridData.minimumHeight = 160;
		gridData.minimumWidth = 100;
		gridData.widthHint = 150;
		//gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		
		rewriteOptionGroup = new Group(optionsGroup, SWT.NONE);
		rewriteOptionGroup.setLayout(layout);
		rewriteOptionGroup.setText("Rewrite Options");
		
		createRadioButton("Decorrelate", rewriteOptionGroup);
		createRadioButton("Unnest", rewriteOptionGroup);
		createRadioButton("Use Left Join", rewriteOptionGroup);
		createRadioButton("Move To Target", rewriteOptionGroup);
		createRadioButton("WL Union Sem", rewriteOptionGroup);
		createRadioButton("Cost Based Choice", rewriteOptionGroup);
		
		createStatsGroup ();
		
		//createActionsGroup ();
	}
	
	private void createStatsGroup () {
		GridLayout layout;
	
		layout = new GridLayout ();
		layout.makeColumnsEqualWidth = true;
		layout.numColumns = 4;
		
		semanOptionGroup = new Group(optionsGroup, SWT.NONE);
		semanOptionGroup.setLayout(layout);
		semanOptionGroup.setText("Statistics");
		
		createLabel("Execution Times", semanOptionGroup, 2);
		createLabel("#result tuples", semanOptionGroup, 2);
		
		createLabel("Normal", semanOptionGroup, 1);
		createLabel("Prov", semanOptionGroup, 1);
		
		createLabel("Normal", semanOptionGroup,1);
		createLabel("Prov", semanOptionGroup,1);
		
		
		createLabelAndPutToMap("NormTime", "", semanOptionGroup);
		createLabelAndPutToMap("ProvTime", "", semanOptionGroup);
		
		
		createLabelAndPutToMap("NormTuples", "", semanOptionGroup);
		createLabelAndPutToMap("ProvTuples", "", semanOptionGroup);
	}

	private void createActionsGroup () {
		GridLayout layout;
		GridData gridData;
		
		layout = new GridLayout ();
		layout.makeColumnsEqualWidth = true;
		layout.numColumns = 1;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 1;
		
		gridData = new GridData ();
		gridData.horizontalSpan = 1;
		gridData.widthHint = 115;
		gridData.minimumWidth = 115;
		gridData.heightHint = LOGO_HEIGHT + 10;
		gridData.minimumHeight = LOGO_HEIGHT + 10;
		//gridData.horizontalAlignment = GridData.FILL;
		//gridData.grabExcessHorizontalSpace = true;
		
		actionsGroup = new Group(textGroup, SWT.NONE);
		actionsGroup.setLayout(layout);
		actionsGroup.setLayoutData(gridData);
		//actionsGroup.setText("Actions");
		
		addQueryHistoryDropDown ();
		
		createActionButton("run", actionsGroup, 1);
		createActionButton("show Rewrite", actionsGroup, 1);
	}
	
	private Label createLabel (String text, Group parent, int width) {
		GridData gridData;
		Label label;
		
		gridData = new GridData ();
		gridData.heightHint = 10;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = width;
		
		label = new Label(parent, SWT.NONE);
		label.setText(text);
		label.setLayoutData(gridData);
		label.setFont(SWTResourceManager.getInstance().getFont("statslabel"));
		
		return label;
	}
	
	private void createLabelAndPutToMap (String name, String text, Group parent) {
		labelMap.put(name, createLabel(text, parent, 1));
	}
	
	private void createRadioButton (String name, Group parent) {
		GridData gridData;
		Button radio;
		
		gridData = new GridData ();
		gridData.heightHint = 15;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 1;
		
		radio = new Button(parent,SWT.CHECK);
		radio.setText(name);
		radio.setFont(SWTResourceManager.getInstance().getFont("radioFont"));
		radio.setSelection(false);
		radio.setLayoutData(gridData);
		radio.addSelectionListener(buttonListener);
		
		buttonMap.put(name, radio);
	}
	
	private void createActionButton (String name, Group parent, int width) {
		GridData gridData;
		Button button;
		
		gridData = new GridData ();
		gridData.heightHint = 28;
		gridData.minimumHeight = 25;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = width;
		
		button = new Button(parent,SWT.PUSH);
		button.setText(name);
		button.setSelection(false);
		button.setLayoutData(gridData);
		button.setFont(SWTResourceManager.getInstance().getFont("button"));
		button.addSelectionListener(buttonListener);
	}
	
	private void createAlgebraGroup () {
		GridData gridData;
		GridLayout layout; 
		
		layout = new GridLayout ();
		layout.makeColumnsEqualWidth = true;
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		
		gridData = new GridData ();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		
		algebraGroup = new Group(topGroup, SWT.NONE);
		algebraGroup.setLayout(layout);
		algebraGroup.setLayoutData(gridData);

		gridData = new GridData ();
		gridData.heightHint = 15;
		gridData.minimumHeight = 15;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 1;
		
		qTreeLabel = new Label(algebraGroup,SWT.NONE);
		qTreeLabel.setFont(SWTResourceManager.getInstance().getFont("label"));
		qTreeLabel.setText("Query Algebra Tree");
		qTreeLabel.setLayoutData(gridData);
		
		gridData = new GridData ();
		gridData.heightHint = 15;
		gridData.minimumHeight = 15;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 1;
		
		pTreeLabel = new Label(algebraGroup,SWT.NONE);
		pTreeLabel.setFont(SWTResourceManager.getInstance().getFont("label"));
		pTreeLabel.setText("Provenance Query Algebra Tree");
		pTreeLabel.setLayoutData(gridData);
		
		gridData = new GridData ();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		
		//algebraComp = new Text(algebraGroup,SWT.V_SCROLL | SWT.H_SCROLL);
		algebraComp = new ImageViewer (algebraGroup, SWT.NONE);
		algebraComp.setLayoutData(gridData);
		
		gridData = new GridData ();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		
		pAlgebraComp = new ImageViewer (algebraGroup, SWT.NONE);
		pAlgebraComp.setLayoutData(gridData);
	}
	
	private void createProllImages () {
		ImageData image;
		Canvas canvas;
		GridData gridData;
		
		gridData = new GridData ();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.heightHint = LOGO_HEIGHT;
		gridData.minimumHeight = LOGO_HEIGHT;
		//gridData.widthHint = 600;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		
		canvas = new Canvas (textGroup, SWT.NONE);
		canvas.setLayout(new RowLayout(SWT.HORIZONTAL));
		canvas.setLayoutData(gridData);
		canvas.setBackground(SWTResourceManager.getInstance().getColor("white"));
		
		image = new ImageData(ClassLoader.getSystemResourceAsStream("permlogo.jpg"));
		createImageLabel (image, canvas, 1);
		
		image = new ImageData(ClassLoader.getSystemResourceAsStream("systems.jpg"));
		createImageLabel (image, canvas, 1);
		
		image = new ImageData(ClassLoader.getSystemResourceAsStream("dbtg.jpg"));
		createImageLabel (image, canvas, 1);
	}
	
	private void createImageLabel (ImageData imageData, Canvas parent, int widthSpan) {
		Label label;
		//GridData gridData;
		Image image;
		int width;
		int heigth;
		int scale;
		
		width = imageData.width;
		heigth = imageData.height;
		scale = (int) (width * ((double) LOGO_HEIGHT / (double) heigth));
				
		imageData = imageData.scaledTo(scale, LOGO_HEIGHT);
		image = new Image(sShell.getDisplay(), imageData);
		
//		gridData = new GridData ();
//		gridData.heightHint = LOGO_HEIGTH;
//		gridData.grabExcessHorizontalSpace = false;
//		gridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
//		gridData.horizontalSpan = widthSpan;
		
		label = new Label(parent, SWT.NONE);
//		label.setLayoutData(gridData);
		label.setImage(image);
	}

	private void createResultGroup () {
		GridData gridData;
		
		gridData = new GridData ();
		gridData.heightHint = 200;
		gridData.minimumHeight = 50;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 1;
		
		resultGroup = new Group(mainComposite, SWT.NONE);
		resultGroup.setLayout(new FillLayout());
		resultGroup.setLayoutData(gridData);
		
		resultText = new StyledText(resultGroup, SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		resultText.setFont(SWTResourceManager.getInstance().getFont("result"));
	}
	
	private void createListeners () {
		buttonListener = new BrowserActionHandler (this);
	}
	
	private void initializeOptionButtons () {
		String option;
		Button button;
		
		for(String key: buttonMap.keySet()) {
			option = PropertyManager.getInstance().getProperty("ButtonOption." + key.replaceAll(" ", "_"));
			log.debug("get option: " + option);
			button = buttonMap.get(key);
			
			try {
				button.setSelection(QueryHandler.getInstance().getOption(option));
			}
			catch (Exception e) {
				ExceptionLogger.logException(e, log);
				openErrorDialog (e);	
			}
		}
	}
	
	private void allocateFontsAndColors () {
		SWTResourceManager.getInstance().getFont("result", new Font (sShell.getDisplay(), "Courier", 12, SWT.NORMAL));
		SWTResourceManager.getInstance().getFont("radio", new Font (sShell.getDisplay(), "Courier", 8, SWT.NORMAL));
		SWTResourceManager.getInstance().getFont("button", new Font (sShell.getDisplay(), "Courier", 10, SWT.NORMAL));
		SWTResourceManager.getInstance().getFont("statslabel", new Font (sShell.getDisplay(), "Courier", 10, SWT.BOLD));
		SWTResourceManager.getInstance().getFont("label", new Font (sShell.getDisplay(), "Courier", 12, SWT.BOLD));
		
		SWTResourceManager.getInstance().putColor("white", new Color (sShell.getDisplay(), 0,0,0));
		SWTResourceManager.getInstance().putColor("rel0", new Color (sShell.getDisplay(), 255,255,255));
		SWTResourceManager.getInstance().putColor("rel1", new Color (sShell.getDisplay(), 200,200,255));
	}
	
	public void shellClosed (ShellEvent event) {
		log.debug("close shell");
		try {
			QueryHistory.getInstance().storeQueryHistory();
			ConnectionManager.getInstance().closeConnection();
		}
		catch (Exception e) {
			ExceptionLogger.logException(e, log);
			openErrorDialog (e);
		}
		finally {
			SWTResourceManager.getInstance().dispose();
		}
	}
	
	public void openErrorDialog (Exception error) {
		MessageBox dialog;
		
		dialog = new MessageBox(sShell, SWT.OK);
		dialog.setText("Error");
		dialog.setMessage(error.toString());
		
		dialog.open();
	}

}
