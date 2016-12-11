package org.cytoscape.prorank.internal.results.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.prorank.internal.results.CytoscapeResultViewerPanel;

import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Action called when a cluster must be extracted as a separate network
 * 
 * @author tamas
 */
public class ExtractClusterAction extends AbstractAction {
	/**
	 * Result viewer panel associated to the action
	 */
	protected CytoscapeResultViewerPanel resultViewer;

	/**
	 * Constructs the action
	 */
	public ExtractClusterAction(CytoscapeResultViewerPanel panel) {
		super("Extract selected cluster(s)");
		this.resultViewer = panel;
		this.putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_E);
	}
	
	public void actionPerformed(ActionEvent event) {
		List<CyNode> selectedNodes = this.resultViewer.getSelectedCytoscapeNodeSet();
		CyNetwork network = this.resultViewer.getNetwork();
		
		
		if (network == null) {
                        JOptionPane.showMessageDialog(null, "Cannot create network representation for the cluster:\n ", "The parent network has already been destroyed. ", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		resultViewer.selectNodes(selectedNodes);
		
		NewNetworkSelectedNodesAndEdgesTaskFactory taskFactory = this.resultViewer.getCyactivator().getService(NewNetworkSelectedNodesAndEdgesTaskFactory.class);
				
		if (taskFactory == null) {
                        JOptionPane.showMessageDialog(null, "Cannot create network representation for the cluster:\n ", "New network creation factory is not registered. ", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		DialogTaskManager taskManager =
				this.resultViewer.getCyactivator().getService(DialogTaskManager.class);
		if (taskManager == null) {
                        JOptionPane.showMessageDialog(null, "Cannot create network representation for the cluster:\n ", "Dialog task manager is not registered. ", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		taskManager.execute(taskFactory.createTaskIterator(network));
                this.resultViewer.incrementClusterCount();
                
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ExtractClusterAction.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                String currentNetworkName = network.getRow(network).get(CyNetwork.NAME, String.class);
                Set<CyNetwork> allnetworks = this.resultViewer.getCyactivator().getService(CyNetworkManager.class).getNetworkSet();
                        
                long maxSUID = Integer.MIN_VALUE;
                for(CyNetwork net : allnetworks){
                    if(net.getSUID() > maxSUID)
                        maxSUID = net.getSUID();
                }
                CyNetwork newnet = this.resultViewer.getCyactivator().getService(CyNetworkManager.class).getNetwork(maxSUID);
                newnet.getRow(newnet).set(CyNetwork.NAME, currentNetworkName + " Cluster extracted " + this.resultViewer.getclustersExtracted());

        }
}
