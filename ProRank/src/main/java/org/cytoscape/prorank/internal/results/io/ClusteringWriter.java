package org.cytoscape.prorank.internal.results.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.cytoscape.prorank.internal.logic.Complex;




/**
 * Interface specification for all the clustering writers.
 * 
 * Clustering writes write a clustering to a stream using a specific format.
 * 
 * @author tamas
 */
public interface ClusteringWriter {
	/**
	 * Writes the clustering to the given stream
	 * 
	 * @param clustering   the clustering to be written
	 * @param stream       the stream to write to
	 * @throws IOException
	 */
	public void writeClustering(List<? extends Complex> clustering,
			OutputStream stream) throws IOException;
	
	/**
	 * Writes the clustering to the given file
	 * 
	 * @param clustering   the clustering to be written
	 * @param file     the file itself
	 * @throws IOException
	 */
	public void writeClustering(List<? extends Complex> clustering,
			File file) throws IOException;
	
	/**
	 * Writes the clustering to the given file
	 * 
	 * @param clustering   the clustering to be written
	 * @param filename  the filename
	 * @throws IOException
	 */
	public void writeClustering(List<? extends Complex> clustering,
			String filename) throws IOException;
}
