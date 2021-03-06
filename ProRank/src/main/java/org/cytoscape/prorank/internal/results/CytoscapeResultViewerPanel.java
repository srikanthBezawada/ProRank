package org.cytoscape.prorank.internal.results;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.cytoscape.application.swing.CySwingApplication;

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.prorank.internal.logic.Complex;
import org.cytoscape.prorank.internal.results.actions.CopyClusterToClipboardAction;
import org.cytoscape.prorank.internal.results.actions.ExtractClusterAction;
import org.cytoscape.prorank.internal.results.actions.RemoveClusterFromResultAction;
import org.cytoscape.prorank.internal.results.actions.SaveClusterAction;
import org.cytoscape.prorank.internal.results.actions.SaveClusteringAction;
import org.cytoscape.prorank.internal.util.CyNetworkUtil;



import org.cytoscape.service.util.CyServiceRegistrar;


import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;


/**
 * Result viewer panel with some added functionality to ensure better integration
 * with Cytoscape
 * 
 * @author tamas
 */
public class CytoscapeResultViewerPanel extends ResultViewerPanel implements
	CytoPanelComponent, ListSelectionListener,
	NetworkViewAddedListener, NetworkViewAboutToBeDestroyedListener {
	/**
	 * The ClusterONE Cytoscape application in which this panel lives.
	 */
	private CyServiceRegistrar cyRegistrar = null;
	
	/**
	 * Serial number of this result viewer panel.
	 */
	private Integer serialNumber = null;
	
	/**
	 * Last used serial number of result viewer panels.
	 * 
	 * This is used to assign unique numbers to each result panel in Cytoscape
	 */
	static private int lastUsedSerialNumber = 1;
	
        protected int clustersExtracted = 0;
	/**
	 * Mapping from node IDs to real Cytoscape {@link CyNode} objects
	 */
	protected List<CyNode> nodeMapping;

	/** Reference to the original Cytoscape network from which the results were calculated */
	protected WeakReference<CyNetwork> networkRef;
	
	/** Reference to a Cytoscape network view that will be used to highlight nodes in the selected nodeset */
	protected WeakReference<CyNetworkView> networkViewRef;
	
	/**
	 * The popup menu that comes up when right clicking on a cluster
	 */
	protected JPopupMenu clusterPopup;
	
	/**
	 * The "Copy to clipboard" element of the popup menu
	 */
	protected AbstractAction copyToClipboardAction;
	
	/**
	 * The "Extract selected cluster" element of the popup menu
	 */
	protected AbstractAction extractClusterAction;
	
	/**
	 * The "Save selected cluster..." element of the popup menu
	 */
	protected AbstractAction saveClusterAction;
	
	/**
	 * The "Remove" element of the popup menu
	 */
	protected AbstractAction removeClusterAction;
	
	/**
	 * The "Convert to Cytoscape group..." element of the popup menu
	 */
	protected AbstractAction saveClusterAsCyGroupAction;
	
	
	
	// --------------------------------------------------------------------
	// Constructor
	// --------------------------------------------------------------------

	/**
	 * Creates a result viewer panel associated to the given {@link CyNetwork}
	 * 
	 * It will be assumed that the results shown in this panel were generated
	 * from the given network, and that there is no network view to adjust when
	 * a cluster is selected in the table.
	 * 
	 * @param app       reference to the global CytoscapeApp object
	 * @param network   the network from which the clusters were generated
	 */
	public CytoscapeResultViewerPanel(CyServiceRegistrar cyRegistrar, CyNetwork network) {
		super();
		
		this.cyRegistrar = cyRegistrar;
		this.networkRef = new WeakReference<CyNetwork>(network);
		
		initializeClusterPopup();
		
		/* Listen to table selection changes */
		this.table.getSelectionModel().addListSelectionListener(this);
		
		/* Listen to double click events on the table */
		this.table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					extractClusterAction.actionPerformed(null);
				}
			}
		});
		
		/* Listen for network view events */
		cyRegistrar.registerService(this, NetworkViewAddedListener.class, new Properties());
		cyRegistrar.registerService(this, NetworkViewAboutToBeDestroyedListener.class, new Properties());
		
		/* Add popup menu to the cluster selection table */
		this.table.addMouseListener(new PopupMenuTrigger(clusterPopup));
		
		/* Add the bottom buttons */
		/* JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		JButton closeButton = new JButton(new CloseAction(this));
		buttonPanel.add(closeButton);
		this.add(buttonPanel, BorderLayout.SOUTH); */
		
		this.addAction(new SaveClusteringAction(this));
		this.addAction(new CloseAction(this));
		
	}

        public CyServiceRegistrar getCyactivator() {
            return cyRegistrar;
        }
	
	
	/**
	 * Retrieves the Cytoscape network associated to this panel
	 */
	public CyNetwork getNetwork() {
		if (networkRef == null)
			return null;
		return networkRef.get();
	}
	
	/**
	 * Retrieves the Cytoscape network view associated to this panel
	 */
	public CyNetworkView getNetworkView() {
		if (networkViewRef == null)
			return null;
		return networkViewRef.get();
	}
	
	/**
	 * Retrieves the mapping from integer node IDs to real Cytoscape {@link CyNode} objects
	 */
	public List<CyNode> getNodeMapping() {
		return this.nodeMapping;
	}
	
	/**
	 * Retrieves the set of Cytoscape nodes associated to the selected {@link NodeSet}.
	 * 
	 * If multiple {@link NodeSet}s are selected, the corresponding Cytoscape nodes will be
	 * merged into a single set.
	 * 
	 * If nothing is selected in the table, an empty set will be returned.
	 */
	public List<CyNode> getSelectedCytoscapeNodeSet() {
		Set<CyNode> selectedIndices = new HashSet<CyNode>();
		List<CyNode> result = new ArrayList<CyNode>()
;		/* Take the union of all indices. This step is necessary because CyNodes are not hashable */
		for (Complex selectedNodeSet: this.getSelectedNodeSets()) {
			for (CyNode n: selectedNodeSet.getNodes()) {
				selectedIndices.add(n);
			}
		}
		result.addAll(selectedIndices);
		/* Convert indices to CyNodes */
		return result;
	}
	
	/**
	 * Retrieves the set of Cytoscape nodes associated to the selected {@link NodeSet}.
	 * 
	 * If multiple {@link NodeSet}s are selected, the corresponding Cytoscape nodes will be
	 * returned as individual lists.
	 * 
	 * If nothing is selected in the table, an empty list will be returned.
	 */
	public List<List<CyNode>> getSelectedCytoscapeNodeSets() {
		List<List<CyNode>> result = new ArrayList<List<CyNode>>();
		for (Complex selectedNodeSet: this.getSelectedNodeSets()) {
			result.add(selectedNodeSet.getNodes());			
		}
		return result;
	}
	
	/**
	 * Retrieves the set of Cytoscape nodes associated to all {@link NodeSet} instances
	 * in this result viewer.
	 */
	public List<List<CyNode>> getAllCytoscapeNodeSets() {
		NodeSetTableModel model = this.getTableModel();
		int numRows = model.getRowCount();
		
		List<List<CyNode>> result = new ArrayList<List<CyNode>>();
		for (int i = 0; i < numRows; i++) {
			result.add(model.getNodeSetByIndex(i).getNodes());			
		}
		return result;
	}

	protected void setNetworkView(CyNetworkView networkView) {
		if (networkView == null) {
			this.networkViewRef = null;
			return;
		}
		
		if (networkView.getModel() != getNetwork()) {
			throw new RuntimeException("network view is associated to a different network");
		}
		this.networkViewRef = new WeakReference<CyNetworkView>(networkView);
	}
	
	// --------------------------------------------------------------------
	// Manipulation methods
	// --------------------------------------------------------------------

	/**
	 * Adds the result panel to Cytoscape's designated result panel area
	 */
	public void addToCytoscapeResultPanel() {
		if (serialNumber == null) {
			serialNumber = lastUsedSerialNumber;
			lastUsedSerialNumber++;
		}
		
		/* Register the panel */
		cyRegistrar.registerService(this, CytoPanelComponent.class, new Properties());
		
		/* Ensure that the panel is visible */
		CytoPanel cytoPanel = cyRegistrar.getService(CySwingApplication.class).getCytoPanel(getCytoPanelName());
		if (cytoPanel.getState() == CytoPanelState.HIDE) {
			cytoPanel.setState(CytoPanelState.DOCK);
		}
		setVisible(true);
		
		/* Activate the panel */
		cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(getComponent()));
	}
	
	/**
	 * @inheritDoc
	 */
	@Override
	protected Icon constructProgressIcon() {
		//TODO
                //URL url = pewccapp.getResource(pewccapp.getResourcePathName() + "/wait.jpg");
		//return (url != null) ? new ImageIcon(url) : new EmptyIcon(32, 32);
                return null;
	}
	
	
	
	/**
	 * Converts an integer iterable yielding node IDs to a list of Cytoscape nodes
	 * 
	 * As {@link NodeSet}s are iterable, this method works with {@link NodeSet}s directly.
	 */
	protected List<CyNode> convertIterableToCytoscapeNodeList(Iterable<Integer> iterable) {
		List<CyNode> result = new ArrayList<CyNode>();
		for (int idx: iterable) {
			CyNode node = this.nodeMapping.get(idx);
			if (node == null)
				continue;
			result.add(node);
		}
		return result;
	}
	
	/**
	 * Closes the result panel.
	 */
	public void close() {
		cyRegistrar.unregisterService(this, CytoPanelComponent.class);
		
		/* Ensure that the panel is hidden if this was the last one */
		CytoPanel cytoPanel = cyRegistrar.getService(CySwingApplication.class).getCytoPanel(getCytoPanelName());
		if (cytoPanel.getCytoPanelComponentCount() == 0) {
			cytoPanel.setState(CytoPanelState.HIDE);
		}
	}
	
	/**
	 * Initializes the cluster popup menu
	 */
	private void initializeClusterPopup() {
		clusterPopup = new JPopupMenu();
		
		copyToClipboardAction = new CopyClusterToClipboardAction(this);
		copyToClipboardAction.setEnabled(false);
		clusterPopup.add(copyToClipboardAction);
		
		extractClusterAction = new ExtractClusterAction(this);
		extractClusterAction.setEnabled(false);
		clusterPopup.add(extractClusterAction);
		
		saveClusterAction = new SaveClusterAction(this);
		saveClusterAction.setEnabled(false);
		clusterPopup.add(saveClusterAction);
		
		removeClusterAction = new RemoveClusterFromResultAction(this);
		removeClusterAction.setEnabled(false);
		clusterPopup.add(removeClusterAction);
		
		/*
		clusterPopup.addSeparator();
		
		saveClusterAsCyGroupAction = new SaveClusterAsCyGroupAction(this);
		saveClusterAsCyGroupAction.setEnabled(false);
		clusterPopup.add(saveClusterAsCyGroupAction);
		*/
	}

	/**
	 * Selects the given set of nodes in the associated network and redraws its view.
	 */
	public void selectNodes(Collection<? extends CyNode> nodes) {
		selectNodes(nodes, true);
	}
	
	/**
	 * Selects the given set of nodes in the associated network and optionally
	 * redraws its view.
	 */
	public void selectNodes(Collection<? extends CyNode> nodes, boolean redraw) {
		CyNetwork network = this.getNetwork();
		if (network == null)
			return;
		
		// Unselect all nodes and edges
		CyNetworkUtil.unselectAllNodes(network);
		CyNetworkUtil.unselectAllEdges(network);
		
		// Select the nodes of the cluster and the connecting edges
		CyNetworkUtil.setSelectedState(network, nodes, true);
		CyNetworkUtil.setSelectedState(network, CyNetworkUtil.getConnectingEdges(network, nodes), true);
		
		// Redraw the network
		CyNetworkView networkView = this.getNetworkView();
		if (networkView != null) {
			networkView.updateView();
		}
	}
	
	
	/**
	 * Sets the results to be shown in this panel.
	 */
	public void setResult(List<Complex> res) {
		this.setNodeSets(res);
		//this.setNodeMapping(result.nodeMapping);
	}
	
	/**
	 * Method called when the table selection changes
	 * @param event   event describing how the selection changed
	 */
	public void valueChanged(ListSelectionEvent event) {
		CyNetwork network = this.getNetwork();
		
		if (network == null) {
			copyToClipboardAction.setEnabled(false);
			extractClusterAction.setEnabled(false);
			saveClusterAction.setEnabled(false);
			return;
		}
		
		List<CyNode> nodes = this.getSelectedCytoscapeNodeSet();
		selectNodes(nodes);
		
		boolean enabled = nodes.size() > 0;
		extractClusterAction.setEnabled(enabled);
		copyToClipboardAction.setEnabled(enabled);
		saveClusterAction.setEnabled(enabled);
		removeClusterAction.setEnabled(enabled);
		// saveClusterAsCyGroupAction.setEnabled(enabled);
	}


	// --------------------------------------------------------------------
	// Private methods
	// --------------------------------------------------------------------

	// --------------------------------------------------------------------
	// CloseAction class
	// --------------------------------------------------------------------
	
	class CloseAction extends AbstractAction {
		CytoscapeResultViewerPanel panel;
		
		public CloseAction(CytoscapeResultViewerPanel panel) {
			super("Close");
			this.panel = panel;
			this.putValue(AbstractAction.SMALL_ICON, UIManager.getIcon("OptionPane.errorIcon"));
			this.putValue(AbstractAction.SHORT_DESCRIPTION,
				"Close this result panel");
                    
                       
		}
		
		public void actionPerformed(ActionEvent event) {
			panel.close();
		}
	}

	// --------------------------------------------------------------------
	// CytoPanelComponent implementation
	// --------------------------------------------------------------------
	
	public Component getComponent() {
		return this;
	}

	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	public Icon getIcon() {
		return null;
	}

	public String getTitle() {
		return "ProRank" + " result " + serialNumber;
	}

	// --------------------------------------------------------------------
	// NetworkViewAboutToBeDestroyedListener implementation
	// --------------------------------------------------------------------
	
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent event) {
		if (this.getNetworkView() != event.getNetworkView())
			return;
		
		// Detach ourselves from the network view
		this.setNetworkView(null);
	}

	// --------------------------------------------------------------------
	// NetworkViewAddedListener implementation
	// --------------------------------------------------------------------
	
	public void handleEvent(NetworkViewAddedEvent event) {
		if (this.getNetworkView() != null)
			return;
		
		// Attach ourselves to the network view if it corresponds to our
		// network
		CyNetworkView newNetworkView = event.getNetworkView();
		if (this.getNetwork() != null && newNetworkView != null &&
				newNetworkView.getModel() == this.getNetwork()) {
			this.setNetworkView(newNetworkView);
		}
	}
        
        public void incrementClusterCount() {
            clustersExtracted++;
        }
        
        public int getclustersExtracted() {
            return clustersExtracted;
        }
        
}
