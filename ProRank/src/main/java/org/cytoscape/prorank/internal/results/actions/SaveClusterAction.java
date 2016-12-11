package org.cytoscape.prorank.internal.results.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.prorank.internal.logic.Complex;

import org.cytoscape.prorank.internal.results.CytoscapeResultViewerPanel;
import org.cytoscape.prorank.internal.results.io.ClusteringWriter;
import org.cytoscape.prorank.internal.results.io.ClusteringWriterFactory;


import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;



/**
 * Action that saves the names of the members of the selected clusters
 * to a file on the disk, separated by spaces, one per line
 * 
 * @author ntamas
 */
public class SaveClusterAction extends AbstractAction {
	/**
	 * Result viewer panel associated to the action
	 */
	protected CytoscapeResultViewerPanel resultViewer;
	
	/**
	 * Constructor
	 */
	public SaveClusterAction(CytoscapeResultViewerPanel panel) {
		super("Save selected cluster(s)...");
		this.resultViewer = panel;
		this.putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_S);
	}
	
	/**
	 * Returns the list of nodes that should be saved
	 */
	protected List<Complex> getNodeListsToBeSaved() {
		return this.resultViewer.getSelectedNodeSets();
	}
	
	/**
	 * Returns the title of the dialog box where the destination file will be selected
	 */
	protected String getFileDialogTitle() {
		return "Select the file to save the selected clusters to";
	}
	
	public void actionPerformed(ActionEvent arg0) {
		ClusteringWriterFactory.Format[] formats =
			ClusteringWriterFactory.Format.values();
		FileChooserFilter[] filters = new FileChooserFilter[formats.length];
		
		for (int i = 0; i < formats.length; i++) {
			filters[formats.length - i - 1] = new FileChooserFilter(
					formats[i].getName(), formats[i].getExtension());
		}
		
		FileUtil fileUtil = resultViewer.getCyactivator().getService(FileUtil.class);
		if (fileUtil == null)
			return;
		
		File file = fileUtil.getFile(resultViewer.getCyactivator().getService(CySwingApplication.class).getJFrame(),
				this.getFileDialogTitle(), FileUtil.SAVE, Arrays.asList(filters));
		
		if (file == null)
			return;
		
		ClusteringWriterFactory.Format format =
			ClusteringWriterFactory.Format.forFile(file);
		if (format == null) {
                        JOptionPane.showMessageDialog(null, "The extension of the given filename does not correspond to any of the known formats. ", ".csv for CSV cluster lists, .txt for cluster lists ", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		ClusteringWriter wr = ClusteringWriterFactory.fromFormat(format);
		try {
			wr.writeClustering(this.getNodeListsToBeSaved(), file);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "I/O error", "error while trying to save the selected clusters to\n" +file.getAbsolutePath(), JOptionPane.WARNING_MESSAGE);
		}
	}
}
