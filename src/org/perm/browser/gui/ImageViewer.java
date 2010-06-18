/**
 * 
 */
package org.perm.browser.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.perm.browser.util.SWTResourceManager;


/**
 *
 * Part of Project PermBrowser
 * @author Boris Glavic
 *
 */
public class ImageViewer extends Canvas {

	private Group buttonGroup;
	private Canvas buttonCanvas;
	private SWTImageCanvas imageCanvas;
	
	/**
	 * @param parent
	 * @param style
	 */
	public ImageViewer (Composite parent, int style) {
		super(parent, style);
		initGui ();
	}

	private void initGui () {
		GridLayout layout;
		GridData gridData;
		
		layout = new GridLayout ();
		layout.makeColumnsEqualWidth = true;
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		this.setLayout(layout);
		this.setBackground(SWTResourceManager.getInstance().getColor("white"));
		
		createButtonGroup();
		
		gridData = new GridData ();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		
		imageCanvas = new SWTImageCanvas (this, SWT.NONE);
		imageCanvas.setLayoutData(gridData);
	}
	
	private void createButtonGroup () {
		GridLayout layout;
		GridData gridData;
		Button button;
		
		gridData = new GridData ();
		gridData.heightHint = 36;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 1;
		
		layout = new GridLayout ();
		layout.makeColumnsEqualWidth = true;
		layout.numColumns = 4;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		
		buttonCanvas = new Canvas (this, SWT.NONE);
		buttonCanvas.setLayoutData(gridData);
		buttonCanvas.setLayout(layout);

		
//		buttonGroup = new Group (this, SWT.NONE);
//		buttonGroup.setLayoutData(gridData);
//		buttonGroup.setLayout(layout);
		
		createButton("in", new SelectionListener () {

			public void widgetDefaultSelected (SelectionEvent arg0) {
				
			}

			public void widgetSelected (SelectionEvent arg0) {
				imageCanvas.zoomIn();
			}
		});
		
		createButton("out", new SelectionListener () {

			public void widgetDefaultSelected (SelectionEvent arg0) {
				
			}

			public void widgetSelected (SelectionEvent arg0) {
				imageCanvas.zoomOut();
			}
		});
		
		createButton ("100%", new SelectionListener () {

			public void widgetDefaultSelected (SelectionEvent arg0) {
				
			}

			public void widgetSelected (SelectionEvent arg0) {
				imageCanvas.showOriginal();
			}
		});
		
		createButton("fit", new SelectionListener () {

			public void widgetDefaultSelected (SelectionEvent arg0) {
				
			}

			public void widgetSelected (SelectionEvent arg0) {
				imageCanvas.fitCanvas();
			}
		});
	
	}
	
	private Button createButton (String text, SelectionListener listener) {
		GridData gridData;
		Button button;
		
		gridData = new GridData ();
		gridData.heightHint = 36;
		gridData.widthHint = 50;
		gridData.minimumWidth = 25;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 1;
		
		button = new Button (buttonCanvas, SWT.PUSH);
		button.setText(text);
		button.setFont(SWTResourceManager.getInstance().getFont("button"));
		button.setLayoutData(gridData);
		button.addSelectionListener(listener);
		
		return button;
	}
	
	public void setImageData (ImageData data) {
		imageCanvas.setImageData(data);
		imageCanvas.fitCanvas();
	}
	
	
}
