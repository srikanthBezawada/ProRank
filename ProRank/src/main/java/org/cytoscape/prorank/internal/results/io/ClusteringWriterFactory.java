package org.cytoscape.prorank.internal.results.io;

import java.io.File;
import org.cytoscape.prorank.internal.util.StringUtils;





/**
 * Factory class that constructs {@link ClusteringWriter} objects
 * 
 * @author ntamas
 */
public class ClusteringWriterFactory {
	public enum Format {
		PLAIN("Cluster list", "txt", "text/plain"),
		CSV("CSV-formatted detailed cluster list", "csv", "text/csv");
		
		private String extension;
		private String name;
		private String mimeType;

		Format(String name, String extension, String mimeType) {
			this.name = name;
			this.extension = extension;
			this.mimeType = mimeType;
		}
		
		public static Format forFile(File file) {
			String extension = StringUtils.getFileExtension(file);
			
			for (Format format: Format.values()) {
				if (format.extension.equals(extension))
					return format;
			}
			
			return null;
		}
		
		public String getExtension() {
			return extension;
		}
		
		public String getMimeType() {
			return mimeType;
		}
		
		public String getName() {
			return name;
		}
	}
	
	/**
	 * Constructs a {@link ClusteringWriter} from the given {@link Format}
	 * 
	 * @param format   the format for which we need a {@link ClusteringWriter}
	 */
	public static ClusteringWriter fromFormat(Format format) {
		switch (format) {
		case PLAIN:
			return new PlainTextClusteringWriter();
		case CSV:
			return new CSVClusteringWriter();
		
		default:
			return null;
		}
	}
}
