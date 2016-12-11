package org.cytoscape.prorank.internal.results;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;


import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.prorank.internal.logic.Complex;
import org.cytoscape.prorank.internal.results.renderer.FruchtermanReingoldLayoutAlgorithm;
import org.cytoscape.prorank.internal.results.renderer.GraphRenderer;
import org.cytoscape.prorank.internal.results.standardgraph.GraphLayoutAlgorithm;
import org.cytoscape.prorank.internal.util.UniqueIDGenerator;





public class NodeSetTableModel extends AbstractTableModel {
    /** Column headers for the simple mode */
    String[] currentHeaders = { "Cluster", "Sort by size" };

    /** Column classes for the simple mode */
    Class<?>[] currentClasses = { ImageIcon.class, NodeSetDetails.class };
    
    
    protected List<Complex> nodeSets = new ArrayList<Complex>();
    
    /**
    * The list of {@link NodeSetDetails} objects to avoid having to calculate
    * the Details column all the time
    */
    protected List<NodeSetDetails> nodeSetDetails = new ArrayList<NodeSetDetails>();
    
    boolean detailedMode = false;
    
    /**
    * The list of rendered cluster graphs for all the {@link NodeSet} objects shown in this model
    */
    protected List<Future<Icon>> nodeSetIcons = new ArrayList<Future<Icon>>();
    /**
    * Icon showing a circular progress indicator. Loaded on demand from resources.
    */
    private Icon progressIcon = null;
    
    /**
    * Internal class that represents the task that renders the cluster in the result table
    */
    
    private class RendererTask extends FutureTask<Icon> {
        int rowIndex;

        public RendererTask(int rowIndex, Graph subgraph, GraphLayoutAlgorithm algorithm) {
                super(new GraphRenderer(subgraph, algorithm));
                this.rowIndex = rowIndex;
        }

        protected void done() {
                fireTableCellUpdated(rowIndex, 0);
        }
    }
    
    
    
    /**
    * Constructs a new table model backed by the given list of nodesets
    */
    public NodeSetTableModel(List<Complex> nodeSets) {
        this.nodeSets = nodeSets;
        updateNodeSetDetails();
    }
    
    public int getColumnCount() {
        return currentHeaders.length;
    }

    public int getRowCount() {
        return nodeSets.size();
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return currentClasses[col];
    }

    @Override
    public String getColumnName(int col) {
        return currentHeaders[col];
    }
    
    /**
    * Returns the names of members in a given row.
    * 
    * @param row  the index of the row for which we need the list
    * @return  an array containing the names of the members
    */
    public String[] getMemberNames(int row) {
            Complex nodeSet = this.nodeSets.get(row);
            if (nodeSet == null)
                    return new String[0];

            return nodeSet.getMemberNames();
    }
    
    public Object getValueAt(int row, int col) {
        Complex nodeSet = this.nodeSets.get(row);
        if (nodeSet == null)
                return null;

        if (col == 0) {
                /* Check whether we have a rendered image or not */
                try {
                        Future<Icon> icon = nodeSetIcons.get(row);
                        if (icon != null && icon.isDone())
                                return icon.get();
                } catch (InterruptedException ex) {
                        ex.printStackTrace();
                } catch (ExecutionException ex) {
                        ex.printStackTrace();
                }
                return this.getProgressIcon();
        }

        if (!detailedMode) {
                /* Simple mode, column 1 */
                return this.nodeSetDetails.get(row);
        }

        return "TODO";
    }
	
	/**
	 * Returns an icon showing a progress indicator
	 */
	private Icon getProgressIcon() {
		return this.progressIcon;
	}

	/**
	 * Returns the {@link NodeSet} shown in the given row.
	 * 
	 * @param row   the row index
	 * @return   the corresponding {@link NodeSet}
	 */
	public Complex getNodeSetByIndex(int row) {
		return nodeSets.get(row);
	}
    
        /**
	 * Removes the given nodeset from the table model
	 */
	public void remove(Complex nodeSet) {
		int index = nodeSets.indexOf(nodeSet);
		
		if (index < 0)
			return;
		
		nodeSets.remove(index);
		nodeSetIcons.remove(index);
		nodeSetDetails.remove(index);
		
		fireTableRowsDeleted(index, index);
	}
        
        /**
	 * Sets the icon that shows a progress indicator.
	 */
	public void setProgressIcon(Icon value) {
		this.progressIcon = value;
	}
        
        private void updateNodeSetDetails() {
            Executor threadPool = Executors.newSingleThreadExecutor();
            int i = 0;

            nodeSetDetails.clear();
            nodeSetIcons.clear();
            for (Complex nodeSet: nodeSets) {
                    Graph subgraph = convertCyNetworkToGraph(nodeSet.getSubnetwork(), null);
                    RendererTask rendererTask = new RendererTask(i, subgraph,
                                    new FruchtermanReingoldLayoutAlgorithm());
                    threadPool.execute(rendererTask);
                    nodeSetIcons.add(rendererTask);
                    nodeSetDetails.add(new NodeSetDetails(nodeSet));
                    i++;
            }
	}
        
        
        public Graph convertCyNetworkToGraph(CyNetwork network, String weightAttr) {
                org.cytoscape.prorank.internal.results.Graph graph = new org.cytoscape.prorank.internal.results.Graph(network);
		UniqueIDGenerator<CyNode> nodeIdGen = new UniqueIDGenerator<CyNode>(graph);
		Double weight;
                for (CyEdge edge: network.getEdgeList()) {
                        int src = nodeIdGen.get(edge.getSource());
                        int dest = nodeIdGen.get(edge.getTarget());
                        if (src == dest)
                                continue;

                        if (weightAttr == null) {
                                weight = null;
                        } else {
                                CyRow row = network.getRow(edge);
                                weight = row.get(weightAttr, Double.class, 1.0);
                        }
                        if (weight == null)
                                weight = 1.0;

                        graph.createEdge(src, dest, weight);
                }
		graph.setNodeMapping(nodeIdGen.getReversedList());
		
		return graph;
	}
    
}
