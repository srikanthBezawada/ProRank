package org.cytoscape.prorank.internal.logic;

import java.util.List;
import org.cytoscape.prorank.internal.util.CyNodeUtil;
import org.cytoscape.prorank.internal.util.StringUtils;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;


public class Complex {
    private CyNetwork subnetwork;
    private List<CyNode> subnodeList;
    private List<CyEdge> subedgeList;
    
    public Complex(CyNetwork subnetwork) {
        this.subnetwork = subnetwork;
        this.subnodeList = subnetwork.getNodeList();
        this.subedgeList = subnetwork.getEdgeList();
    }
    
    public List<CyNode> getNodes() {
        return this.subnodeList;
    }
    
    public List<CyEdge> getEdges() {
        return this.subedgeList;
    }
    
    public CyNetwork getSubnetwork(){
        return subnetwork;
    }
    
    @Override
    public boolean equals(Object otherComplex) {
        if (!(otherComplex instanceof Complex)) {
            return false;
        }    
        Complex otherComplexRef = (Complex)otherComplex;
        if(otherComplexRef.subnodeList.size() != this.subnodeList.size()) {
            return false;
        }
        if(otherComplexRef.subedgeList.size() != otherComplexRef.subedgeList.size()) {
            return false;
        }
        for(CyNode n : otherComplexRef.subnodeList) {
            if(this.subnodeList.contains(n) == false) {
                return false;
            }
        }
        
        for(CyEdge e : otherComplexRef.subedgeList) {
            if(this.subedgeList.contains(e) == false) {
                return false;
            }
        }
        
        return true;
    }
    
    
    @Override
    public int hashCode() {
        int hashCode = 1;
        
        return hashCode + subnodeList.size() + subedgeList.size();

    }
    
    public String[] getMemberNames() {
        String[] result = new String[this.subnodeList.size()];
        int i = 0;
        
        for(CyNode n : subnodeList){
            result[i] = CyNodeUtil.getName(subnetwork, n);
            i++;
        }
        
        return result;  
    }
    
    /**
    * Prints the nodes in this set to a string using a given separator
    */
    public String toString(String separator) {
            return StringUtils.join(getMemberNames(), separator);
    }
    
    
    
    
}
