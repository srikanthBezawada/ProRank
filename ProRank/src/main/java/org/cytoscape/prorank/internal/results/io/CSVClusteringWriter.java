package org.cytoscape.prorank.internal.results.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import org.cytoscape.prorank.internal.logic.Complex;
import org.cytoscape.prorank.internal.util.StringUtils;






/**
 * Writes a clustering to a stream in CSV format.
 * 
 * The table will contain some basic statistics about the clusters such as
 * their size, density, internal and boundary weight etc.
 * 
 * @author ntamas
 */
public class CSVClusteringWriter extends AbstractClusteringWriter {
	private String columnSep;
	private String doubleQuoteChar;
	private String quoteChar;
	private String quoteTriggers;
	
	public CSVClusteringWriter() {
		this(",", "\"");
	}
	
	public CSVClusteringWriter(String columnSep, String quoteChar) {
		super();
		this.setColumnSeparator(columnSep);
		this.setQuoteChar(quoteChar);
	}
	
	public void setColumnSeparator(String columnSep) {
		this.columnSep = columnSep;
		this.quoteTriggers = " " + this.columnSep + this.quoteChar;
	}
	
	
	public void setQuoteChar(String quoteChar) {
		this.quoteChar = quoteChar;
		this.quoteTriggers = " " + this.columnSep + this.quoteChar;
		this.doubleQuoteChar = this.quoteChar + this.quoteChar;
	}
	
	public void writeClustering(List<? extends Complex> clustering,
			OutputStream stream) throws IOException {
		PrintWriter wr = new PrintWriter(stream);
		String[] parts = {
				"Cluster", "Size", "Members"
		};
		
		int clusterIndex = 0;
		
		wr.println(StringUtils.join(parts, columnSep));
		
		for (Complex nodeSet: clustering) {
			clusterIndex++;
			
			parts[0] = Integer.toString(clusterIndex);
			parts[1] = Integer.toString(nodeSet.getNodes().size());
			parts[2] = quote(nodeSet.toString(" "));
			
			wr.println(StringUtils.join(parts, columnSep));
		}
		
		wr.flush();
	}
	
	private String quote(String str) {
		if (!StringUtils.containsAny(str, quoteTriggers))
			return str;
		
		return quoteChar + str.replace(quoteChar, doubleQuoteChar) + quoteChar;
	}
}
