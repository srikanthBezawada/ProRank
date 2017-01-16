package org.cytoscape.prorank.internal.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cytoscape.prorank.internal.view.ProRankUI;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.prorank.internal.logic.pr.PRinvoke;
import org.cytoscape.prorank.internal.util.MapUtil;
import org.cytoscape.view.model.CyNetworkView;

public class ProRankLogic extends Thread{
    
    private boolean stop;
    private ProRankUI panel;
    private CyNetwork network;
    private CyNetworkView networkView;
    
    
    public ProRankLogic(ProRankUI panel, CyNetwork network, CyNetworkView networkview) {
        this.panel = panel;
        this.network = network;
        this.networkView = networkview;
    }
    
    public void run() {
        stop = false;
        panel.startComputation();
        long startTime = System.currentTimeMillis();
        
        CyRootNetwork root = ((CySubNetwork)network).getRootNetwork();
        Set<Complex> complexes = new HashSet<Complex>();
        
        List<CyNode> nodeList = network.getNodeList();
        List<CyEdge> edgeList = network.getEdgeList();
        List<CyNode> bridgeNodes = new ArrayList<CyNode>();
        
        int compstemp;
        int comps = ConnectivityTest.getComp(network);
        
        for(CyNode n : nodeList) {
            compstemp = ConnectivityTest.getCompNetworkMinusNode(network, n);
            if(compstemp != comps) {
                bridgeNodes.add(n);
            }
        }
        
        //CyNetwork filteredNet = ConnectivityTest.getNetworkMinusNodes(network, bridgeNodes);
        
        PRinvoke pr = new PRinvoke(network);
        Map<CyNode, Double> prScoreMap = pr.execute();
        Map<CyNode, Double> prScoreMapSorted = MapUtil.sortByValue(prScoreMap);
        
        
        CyNode sourceNode;
        List<CyNode> neighbourNodes;
        List<CyEdge> neighbourEdges;
        CyNetwork subNet;
        List<CyNode> processedNodes = new ArrayList<CyNode>();
        
        Iterator<Map.Entry<CyNode, Double>> it = prScoreMapSorted.entrySet().iterator();
        
        CyNetwork updatedNet = network;
        
        while (it.hasNext()) {
            Map.Entry<CyNode, Double> pair = it.next();
            sourceNode = pair.getKey();
            
            if(!updatedNet.containsNode(sourceNode)) {
                continue;
            }
            
            if(stop) {
                return;
            }
            
            neighbourNodes = updatedNet.getNeighborList(sourceNode, CyEdge.Type.ANY);
            neighbourNodes.add(sourceNode);
            
            neighbourEdges = findNeighbourEdges(neighbourNodes, updatedNet);
            if(neighbourEdges.size() < 3) {
                continue;
            }
            subNet = root.addSubNetwork(neighbourNodes, neighbourEdges);
            complexes.add(new Complex(subNet));
            updatedNet = ConnectivityTest.getNetworkMinusNodes(updatedNet, neighbourNodes);
        }
        
        if(stop) {
            return;
        }
        
        panel.resultsCalculated(complexes, network);

        if(stop) {
            return;
        }
        
        long endTime = System.currentTimeMillis();
        long difference = endTime - startTime;
        System.out.println("Execution time for ProRank " + difference +" milli seconds");
        panel.endComputation();
    }
    
   
    
    private List<CyEdge> findNeighbourEdges(List<CyNode> neightbourNodes, CyNetwork updatedNet) {
        List<CyEdge> edgeList = network.getEdgeList();
        List<CyEdge> neighbourEges = new ArrayList<CyEdge>();
        
        for(CyEdge e : edgeList) {
            if(neightbourNodes.contains(e.getSource()) && neightbourNodes.contains(e.getTarget())) 
                neighbourEges.add(e);
        }
        
        return neighbourEges;
    }
    
    private boolean validate(List<CyNode> processed, List<CyNode> current) {
        if(processed.isEmpty()) {
            return true;
        }
        
        for(CyNode cur : current) {
            if(processed.contains(cur)) {
                return false;
            }
        }
        
        return true;
    }
    
    
    
    public void end() {
        stop = true;
    }
    

    
    
}
