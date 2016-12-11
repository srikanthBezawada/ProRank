package org.cytoscape.prorank.internal.results.actions;

import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.UIManager;
import org.cytoscape.prorank.internal.logic.Complex;
import org.cytoscape.prorank.internal.results.CytoscapeResultViewerPanel;





//import uk.ac.rhul.cs.cl1.NodeSet;

/**I
 * Action that saves the names of the members of all clusters
 * in the result viewer to a file on the disk.
 * 
 * @author ntamas
 */
public class SaveClusteringAction extends SaveClusterAction {
	public SaveClusteringAction(CytoscapeResultViewerPanel panel) {
		super(panel);
		this.putValue(AbstractAction.NAME, "Save clustering...");
		this.putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_V);
		this.putValue(AbstractAction.SHORT_DESCRIPTION,
				"Save the clustering to a file");
                this.putValue(AbstractAction.SMALL_ICON, UIManager.getIcon("OptionPane.informationIcon"));
	}

	/**
	 * Returns the title of the dialog box where the destination file will be selected
	 */
	protected String getFileDialogTitle() {
		return "Select the file to save the clustering to";
	}
	
	/**
	 * Returns the list of nodes that should be saved
	 */
	protected List<Complex> getNodeListsToBeSaved() {
		return this.resultViewer.getAllNodeSets();
	}
	
}
