/**
 * 
 */
package org.perm.browser.textresult;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.perm.browser.util.SWTResourceManager;


/**
 *
 * Part of Project PermBrowser
 * @author Boris Glavic
 *
 */
public class TextResultSet {

	private String[] columns;
	private List<String[]> rows;
	private int[] columnWidth;
	
	public String[] getColumns () {
		return columns;
	}
	
	public String getColumn (int num) {
		return columns[num];
	}
	
	public void setColumns (String[] columns) {
		this.columns = columns;
	}
	
	public List<String[]> getRows () {
		return rows;
	}
	
	public void setRows (List<String[]> rows) {
		this.rows = rows;
	}
	
	public String[] getRow (int num) {
		return rows.get(num);
	}
	
	public String getRowColumn (int row, int column) {
		return rows.get(row)[column];
	}
	
	public String getRowColumnFilled (int row, int column) {
		String result;
		
		result = " " + rows.get(row)[column] + " ";
		
		while(result.length() < columnWidth[column]) {
			result = result + " ";
		}
		
		return result;
	}
	
	public int getColumnWidth (int column) {
		return columnWidth[column];
	}
	
	public boolean isProvColumn (int num) {
		return columns[num].startsWith("prov_");
	}

	
	public int[] getColumnWidth () {
		return columnWidth;
	}

	
	public void setColumnWidth (int[] columnWidth) {
		this.columnWidth = columnWidth;
	}
	
	public String toString () {
		StringBuilder result;
		String[] row;
		
		result = new StringBuilder ();
		
		/* generate column headers */
		for(int i = 0; i < columns.length; i++) {
			result.append(" " + columns[i]);
			
			for(int j = 0; j <= columnWidth[i] - columns[i].length(); j++)
				result.append(' ');

			result.append("|");
		}
		result.append('\n');
		
		for(int i = 0; i < columns.length; i++) {
			for(int j = 0; j < columnWidth[i] + 3; j++)
				result.append('-');
		}
		result.append('\n');
		
		/* generate data */
		for(int i = 0; i < rows.size(); i ++) {
			row = rows.get(i);
			
			for(int j = 0; j < columns.length; j++) {
				result.append(" " + row[j]);
				
				for(int k = 0; k <= columnWidth[j] - row[j].length(); k++)
					result.append(' ');
				
				result.append('|');
			}
				
			result.append('\n');
		}
		
		return result.toString();
	}
	
	public StyleRange[] getStyles () {
		StyleRange[] ranges;
		StyleRange range;
		int lineLength, numRels, lineStart, i, colPos;
		List<Integer> temp;
		int[] relStart, relLength;
		String relname = "";
		
		lineLength = 0;
		for(i = 0; i < columns.length; i++) {
			lineLength += columnWidth[i] + 3;
		}
		
		numRels = 1;
		colPos = 0;
		temp = new ArrayList<Integer> ();
		temp.add(0);
		
		for(i = 0; i < columns.length && !columns[i].startsWith("prov_"); i++)
			colPos += columnWidth[i] + 3;
		
		relname = "";
			
		for(; i < columns.length; i++) {
			if(relname.compareTo(getRelname(columns[i])) != 0) {
				numRels++;
				relname = getRelname(columns[i]);
				temp.add(colPos);
			}
			colPos += columnWidth[i] + 3;
		}
		temp.add(lineLength);
		
		relStart = new int[numRels];
		relLength = new int[numRels];
		
		for(i = 0; i < numRels; i++) {
			relStart[i] = temp.get(i);
			relLength[i] = temp.get(i + 1) - relStart[i];
		}
		
		ranges = new StyleRange[(rows.size() + 2) * numRels];
		lineStart = 0;
		
		for(i = 0; i < rows.size() + 2; i ++) {
			for(int j = 0; j < numRels; j++) {
				range = new StyleRange();
				range.start = lineStart + relStart[j];
				range.length = relLength[j];
				range.fontStyle = SWT.NORMAL;
				range.background = SWTResourceManager.getInstance().getColor("rel" + (j % 2));
				
				ranges[(i * numRels) +  j] = range;
			}
			
			lineStart = lineStart + lineLength + 1;
		}
		
		return ranges;
	}
	
	private String getRelname (String pColName) {
		int index;

		/* remove escaped underscore in attribute name */
		pColName = pColName.replaceAll("__", "");
		
		index = pColName.lastIndexOf('_');
		
		return pColName.substring(0, index);
	}
}
