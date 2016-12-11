package org.cytoscape.prorank.internal.results;

import org.cytoscape.prorank.internal.logic.Complex;





public class NodeSetDetails implements Comparable<NodeSetDetails>{
    protected Complex cluster;
    public NodeSetDetails(Complex cluster) {
        this.cluster = cluster;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("<html>Nodes: ");
        sb.append(cluster.getNodes().size());
        sb.append("<br>");

        sb.append("Edges: ");
        sb.append(cluster.getEdges().size());
        sb.append("<br>");

        //sb.append("Clustering coefficient: ");
        //sb.append(cluster.getwcc());
        //sb.append("<br>");

        sb.append("</html>");

        return sb.toString();
    }
    
    /**
    * Compares this object to another (used to sort the result viewer table)
    * 
    * @param  other   the other object
    * @throws  NullPointerException  if the other object is null
    */
    public int compareTo(NodeSetDetails other) {
        final int BEFORE = -1;
        final int EQUAL  = 0;
        final int AFTER  = 1;
        
        if (this == other)
            return EQUAL;

        if (this.cluster.equals(other.cluster))
            return EQUAL;

        double sizeThis = this.cluster.getNodes().size();
        double sizeThat = other.cluster.getNodes().size();

        if (sizeThis < sizeThat){
            return AFTER;
        } else if(sizeThis > sizeThat){
            return BEFORE;
        } else {
            double edgesizeThis = this.cluster.getEdges().size();
            double edgesizeThat = other.cluster.getEdges().size();
            if(edgesizeThis < edgesizeThat) {
                return AFTER;
            } else if(edgesizeThis > edgesizeThat) {
                return BEFORE;
            }
        }
        
        return EQUAL;       
            
    }
    
}
