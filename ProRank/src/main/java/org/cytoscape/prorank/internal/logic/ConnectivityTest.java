package org.cytoscape.prorank.internal.logic;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.SimpleGraph;

public class ConnectivityTest {
    
    public static int getComp(CyNetwork network) {
        UndirectedGraph<CyNode, CyEdge> g = new SimpleGraph<CyNode, CyEdge>(CyEdge.class);
        List<CyNode> nodeList = network.getNodeList();
        List<CyEdge> edgeList = network.getEdgeList();
        
        for(CyNode n : nodeList){
            g.addVertex(n);
        }
        for(CyEdge e : edgeList){
            if(e.getSource().equals(e.getTarget())){
                continue; // removing self-loops
            }
            g.addEdge(e.getSource(), e.getTarget(),e);
        }
        
        ConnectivityInspector<CyNode, CyEdge> inspector = new ConnectivityInspector<CyNode, CyEdge>(g);
        
        return inspector.connectedSets().size();
    }

    
    
    public static int getCompNetworkMinusNode(CyNetwork network, CyNode n) {
        List<CyNode> nodeToRemove = new ArrayList<CyNode>();
        nodeToRemove.add(n);
        return getComp(getNetworkMinusNodes(network, nodeToRemove));
    }
    
    
    
    public static CyNetwork getNetworkMinusNodes(CyNetwork network, List<CyNode> nodesToRemove) {
        CyRootNetwork root = ((CySubNetwork)network).getRootNetwork();
        CyNetwork subNet;
        
        List<CyNode> nodeList = network.getNodeList();
        List<CyEdge> edgeList = network.getEdgeList();
        
        List<CyNode> nodes = new ArrayList<CyNode>(nodeList);
        List<CyEdge> edges = new ArrayList<CyEdge>(edgeList);
        
        for(CyNode unwantedNode : nodesToRemove) {
            nodes.remove(unwantedNode);
        }
        
        
        List<CyEdge> edgesToRemove = new ArrayList<CyEdge>();
        
        for(CyEdge e : edges) {
            if(nodesToRemove.contains(e.getSource()) || nodesToRemove.contains(e.getTarget())) {
                edgesToRemove.add(e);
            }
        }
        
        for(CyEdge unwantedEdge : edgesToRemove) {
            edges.remove(unwantedEdge);
        }
        
        
        subNet = root.addSubNetwork(nodes, edges);
        return subNet;
    }
    
    
}
