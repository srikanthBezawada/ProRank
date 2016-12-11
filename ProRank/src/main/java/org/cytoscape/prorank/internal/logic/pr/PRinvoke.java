package org.cytoscape.prorank.internal.logic.pr;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class PRinvoke {
    private String COLUMNNAME = "PageRank";
    private CyNetwork network;
    private Hypergraph<PRNode, PREdge> graph;
    private HashMap<Long, PRNode> idToNode;
    private List<CyNode> nodeList;
    private List<CyEdge> edgeList;
    private CyTable nodeTable;
    private CyTable edgeTable;
    
    public PRinvoke(CyNetwork network) {
        this.network = network;
    }
    
    public Map<CyNode, Double> execute() {
        initVariables();
        addNodes();
        addEdges();
        PageRank<PRNode, PREdge> pageRank = performPageRank();
        
        Map<CyNode, Double> prScoreMap = new HashMap<CyNode, Double>();
        prScoreMap = insertScores(pageRank);
        
        return prScoreMap;
    }
    
    private PageRank<PRNode, PREdge> performPageRank() {
        PageRank<PRNode, PREdge> pageRank = new PageRank<PRNode, PREdge>(graph, 0.5);
        pageRank.setMaxIterations(1000);
        pageRank.evaluate();
        return pageRank;
    }
    
    private void initVariables() {
        graph = new DirectedSparseMultigraph<PRNode, PREdge>();
        idToNode = new HashMap<Long, PRNode>();
        nodeList = network.getNodeList();
        edgeList = network.getEdgeList();
        nodeTable = network.getDefaultNodeTable();
        edgeTable = network.getDefaultEdgeTable();
        
        if(nodeTable.getColumn(COLUMNNAME) == null){
            nodeTable.createColumn(COLUMNNAME , Double.class, true);
        }
    }
    
    private void addEdges() {
        for (CyEdge edge : edgeList) {
            PRNode sourceNode = idToNode.get(edge.getSource().getSUID());
            PRNode targetNode = idToNode.get(edge.getTarget().getSUID());
            PREdge prEdge = new PREdge(edge);
            
            graph.addEdge(prEdge, new Pair<PRNode>(sourceNode, targetNode), EdgeType.DIRECTED);
        }
    }

    private void addNodes() {
        for (CyNode node : nodeList) {
            PRNode prNode = new PRNode(node);
            graph.addVertex(prNode);
            idToNode.put(node.getSUID(), prNode);
        }
    }
    
    private Map<CyNode, Double> insertScores(PageRank<PRNode, PREdge> pageRank) {
        
        Map<CyNode, Double> prScoreMap = new HashMap<CyNode, Double>();
        for (PRNode node : graph.getVertices()) {
            node.setPRScore(pageRank.getVertexScore(node));
        }
        
        CyRow row; PRNode node;
        for(CyNode n : nodeList) {
            prScoreMap.put(n, idToNode.get(n.getSUID()).getPRScore());
            //row = nodeTable.getRow(n.getSUID());
            //row.set(COLUMNNAME, idToNode.get(n.getSUID()).getPRScore());
        }
        
        return prScoreMap;
    }
}
