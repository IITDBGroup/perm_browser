/**
 * 
 */
package org.perm.browser.util;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;


/**
 *
 * Part of Project PermBrowser
 * @author Boris Glavic
 *
 */
public class SWTResourceManager {

	static Logger log = Logger.getLogger(SWTResourceManager.class.getName());
	
	private HashMap<String,Color> colors;
	private HashMap<String,Font> fonts;
	private static SWTResourceManager instance;
	
	private SWTResourceManager () {
		colors = new HashMap<String,Color> ();
		fonts = new HashMap<String,Font> ();
	}
	
	public static SWTResourceManager getInstance () {
		if (instance == null)
			instance = new SWTResourceManager ();
		
		return instance;
	}
	
	public void dispose () {
		Color color;
		Font font;
		String key;
		Iterator<String> iter;
		
		iter = colors.keySet().iterator();
		
		while(iter.hasNext()) {
			key = iter.next();
			
			color = colors.get(key);
			color.dispose();
		}
		
		iter = fonts.keySet().iterator();
		
		while(iter.hasNext()) {
			key = iter.next();
			
			font = fonts.get(key);
			font.dispose();
		}
		
	}
	
	public Color getColor (String key) {
		return colors.get(key);
	}
	
	public Color putColor (String key, Color color) {
		return colors.put(key, color);
	}
	
	public Font getFont (String key) {
		return fonts.get(key);
	}
	
	public Font getFont (String key, Font font) {
		return fonts.put(key, font);
	}
	
}
