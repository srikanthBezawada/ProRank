package org.cytoscape.prorank.internal.results.standardgraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.sosnoski.util.array.DoubleArray;
import com.sosnoski.util.array.IntArray;
import com.sosnoski.util.array.StringArray;
import com.sosnoski.util.hashmap.ObjectIntHashMap;

/**
 *  Graph class used in ClusterONE
 *  
 *  This class uses a simple adjacency list representation in the background.
 */
public class Graph implements Iterable<Edge> {
	/**
	 * Whether the graph is directed
	 */
	protected boolean directed = false;
	
	/**
	 * The number of nodes in this graph
	 */
	protected int numberOfNodes = 0;
	
	/**
	 * The list of names for each node in this graph
	 */
	protected StringArray nodeNames = new StringArray();
	
	/**
	 * The list of source nodes for each edge in this graph. The maximum growth limit of the array is overridden
	 * to allow us to scale above 2<sup>30</sup> edges; otherwise the array would try to double its size when
	 * it reaches 2<sup>30</sup> edges and it would fail because the maximum allowed array size in Java is
	 * less than 2<sup>31</sup>.
	 */
	protected IntArray edgesOut = new IntArray(32, 536870912 /* = 2^29 */);

	/**
	 * The list of target nodes for each edge in this graph. See the comment for <code>edgesOut</code> for an
	 * explanation of the growth arguments.
	 */
	protected IntArray edgesIn = new IntArray(32, 536870912 /* = 2^29 */);

	/**
	 * The list of weights for each edge in this graph. See the comment for <code>edgesOut</code> for an
	 * explanation of the growth arguments.
	 */
	protected DoubleArray weights = new DoubleArray(32, 536870912 /* = 2^29 */);
	
	/**
	 * The list of outgoing edge IDs for each node
	 */
	protected ArrayList<IntArray> outEdgeAdjacencyLists = new ArrayList<IntArray>();
	
	/**
	 * The list of incoming edge IDs for each node
	 */
	protected ArrayList<IntArray> inEdgeAdjacencyLists = new ArrayList<IntArray>();
	
	/**
	 * Constructs an empty undirected graph
	 */
	public Graph() {
		this(false);
	}
	
	/**
	 * Constructs an empty graph
	 * 
	 * @param directed  whether the graph will be directed or not
	 */
	public Graph(boolean directed) {
		this.directed = directed;
	}
	
	/**
	 * Checks whether the graph is directed
	 */
	public boolean isDirected() { return directed; }

	/**
	 * Checks whether the graph is weighted
	 * 
	 * A graph is weighted if not all its weights are equal. Graphs with no
	 * edges are considered unweighted.
	 */
	public boolean isWeighted() {
		if (weights.size() == 0)
			return false;
		
		double firstWeight = weights.get(0);
		for (int i = 1; i < weights.size(); i++)
			if (weights.get(i) != firstWeight)
				return true;
				
		return false;
	}
	
	/**
	 * Returns whether the two given nodes are connected.
	 * 
	 * @param  source   the source node
	 * @param  target   the target node
	 */
	public boolean areConnected(int source, int target) {
		IntArray edges = outEdgeAdjacencyLists.get(source);
		int i, n = edges.size();
		for (i = 0; i < n; i++) {
			int edge = edges.get(i);
			if (edgesIn.get(edge) == target)
				return true;
		}
		return false;
	}
	
	/**
	 * Creates a new unnamed node and returns its index
	 */
	public int createNode() {
		return this.createNode(null);
	}
	
	/**
	 * Creates a new node with the given name and returns its index
	 * 
	 * @param   name   the name of the node
	 */
	public int createNode(String name) {
		numberOfNodes++;
		outEdgeAdjacencyLists.add(new IntArray());
		inEdgeAdjacencyLists.add(new IntArray());
		nodeNames.add(name);
		return numberOfNodes-1;
	}

	/**
	 * Create some new nodes in the graph.
	 * 
	 * @return an array of length new_node_count containing the indices of the newly created nodes
	 */
	public int[] createNodes(int new_node_count) {
		int[] result = new int[new_node_count];
		int n = numberOfNodes;
		for (int i = 0; i < new_node_count; i++) {
			outEdgeAdjacencyLists.add(new IntArray());
			inEdgeAdjacencyLists.add(new IntArray());
			nodeNames.add(null);
			result[i] = n+i;
		}
		numberOfNodes += new_node_count;
		return result;
	}
	
	/**
	 * Creates a new edge between nodes with the given indices.
	 * 
	 * @param  src  the source node
	 * @param  dest the target node
	 * 
	 * @return the index of the new edge
	 */
	public int createEdge(int src, int dest) {
		return createEdge(src, dest, 1.0);
	}
	
	/**
	 * Creates a new edge between nodes with the given indices and the given weight
	 * 
	 * @param  src     the source node
	 * @param  dest    the target node
	 * @param  weight  the weight of the edge
	 * 
	 * @return the index of the new edge
	 */
	public int createEdge(int src, int dest, double weight) {
		if (src >= numberOfNodes)
			createNodes(src - numberOfNodes + 1);
		if (dest >= numberOfNodes)
			createNodes(dest - numberOfNodes + 1);
		
		int edgeID = edgesOut.size();
		edgesOut.add(src);
		edgesIn.add(dest);
		weights.add(weight);
		outEdgeAdjacencyLists.get(src).add(edgeID);
		inEdgeAdjacencyLists.get(dest).add(edgeID);
		if (!directed) {
			outEdgeAdjacencyLists.get(dest).add(edgeID);
			inEdgeAdjacencyLists.get(src).add(edgeID);
		}
		return edgeID;
	}

	/**
	 * Returns the number of nodes in the graph
	 */
	public int getNodeCount() {
		return numberOfNodes;
	}

	/**
	 * Returns the number of edges in the graph
	 */
	public int getEdgeCount() {
		return edgesOut.size();
	}
	
	/**
	 * Returns the indices of all nodes adjacent to the node at the specified index
	 * 
	 * If a node is connected to the query node by multiple edges, the node will
	 * be returned multiple times.
	 * 
	 * @param   nodeIndex  the index of the query node
	 * @param   mode       directedness mode. Ignored if the graph is undirected.
	 */
	public int[] getAdjacentNodeIndicesArray(int nodeIndex, Directedness mode) {
		int[] edges = this.getAdjacentEdgeIndicesArray(nodeIndex, mode);
		int i, n = edges.length;

		for (i = 0; i < n; i++) {
			int edge = edges[i];
			if (this.edgesIn.get(edge) == nodeIndex)
				edges[i] = this.edgesOut.get(edge);
			else
				edges[i] = this.edgesIn.get(edge);
		}
		
		return edges;
	}
	
	/**
	 * Returns the indices of all edges adjacent to the node at the specified index
	 * 
	 * @param   nodeIndex  the index of the node
	 * @param   mode       directedness mode. Ignored if the graph is undirected.
	 */
	public int[] getAdjacentEdgeIndicesArray(int nodeIndex, Directedness mode) {
		if (!directed || mode == Directedness.OUT) {
			return outEdgeAdjacencyLists.get(nodeIndex).toArray();
		}
		
		if (mode == Directedness.IN) {
			return inEdgeAdjacencyLists.get(nodeIndex).toArray();
		}
		
		int[] outEdgesArray = outEdgeAdjacencyLists.get(nodeIndex).toArray();
		int[] inEdgesArray = inEdgeAdjacencyLists.get(nodeIndex).toArray();
		int i = outEdgesArray.length, n = i + inEdgesArray.length, j = 0;
		int[] result = new int[n];
			
		result = Arrays.copyOf(outEdgesArray, n);
		while (i < n) {
			result[i] = inEdgesArray[j];
			i++; j++;
		}
		return result;
	}
	
	/**
	 * Returns the name of a given node
	 * 
	 * @param   nodeIndex   the index of the node
	 */
	public String getNodeName(int nodeIndex) { return this.nodeNames.get(nodeIndex); }
	
	/**
	 * Returns the name of all nodes
	 */
	public String[] getNodeNames() { return this.nodeNames.toArray(); }
	
	/**
	 * Returns a hash mapping node names to node indices
	 */
	public ObjectIntHashMap getNodeNameHashMap() {
		ObjectIntHashMap map = new ObjectIntHashMap();
		String[] nodeNames = this.getNodeNames();
		for (int i = 0; i < this.numberOfNodes; i++)
			map.add(nodeNames[i], i);
		
		return map;
	}
	
	/**
	 * Returns the weight of a given edge
	 * 
	 * @param   edgeIndex   the index of the edge
	 */
	public double getEdgeWeight(int edgeIndex) { return this.weights.get(edgeIndex); }

	/**
	 * Returns the weight of all edges
	 */
	public double[] getEdgeWeights() {
		return this.weights.toArray();
	}
	
	/**
	 * Returns one endpoint of a given edge
	 * 
	 * @param   edgeIndex   the index of the edge
	 * @param   knownVertex the vertex index of a known endpoint of the edge. The method will
	 *                      return the vertex index of the other endpoint. The behaviour
	 *                      is unspecified if knownVertex is not an endpoint index.
	 */
	public int getEdgeEndpoint(int edgeIndex, int knownVertex) {
		int idx = edgesOut.get(edgeIndex);
		if (idx == knownVertex)
			return edgesIn.get(edgeIndex);
		return idx;
	}
	
	/**
	 * Returns an iterator that iterates over all the edges of this graph
	 */
	public Iterator<Edge> iterator() {
		return new EdgeIterator(this);
	}
	
	/**
	 * Returns the edge list of the graph
	 */
	public List<Edge> getEdgeList() {
		List<Edge> result = new ArrayList<Edge>(this.getEdgeCount());
		for (Edge edge: this)
			result.add(edge);
		return result;
	}
	
	/**
	 * Returns the number of distinct edges incident on the node with the given index
	 */
	public int getDegree(int nodeIndex) {
		return getDegree(nodeIndex, Directedness.ALL);
	}
	
	/**
	 * Returns the number of distinct in/outedges incident on the node with the given index
	 */
	public int getDegree(int nodeIndex, Directedness mode) {
		if (!directed || mode == Directedness.OUT)
			return outEdgeAdjacencyLists.get(nodeIndex).size();
		if (mode == Directedness.IN)
			return inEdgeAdjacencyLists.get(nodeIndex).size();
		return outEdgeAdjacencyLists.get(nodeIndex).size() + inEdgeAdjacencyLists.get(nodeIndex).size();
	}
	
	/**
	 * Returns the total weight of edges incident on the node with the given index
	 */
	public double getStrength(int nodeIndex) {
		return getStrength(nodeIndex, Directedness.ALL);
	}
	
	/**
	 * Returns the total weight of in/outedges incident on the node with the given index
	 */
	public double getStrength(int nodeIndex, Directedness mode) {
		IntArray neis = null;
		double result = 0.0;
		
		if (!directed || mode != Directedness.IN) {
			neis = outEdgeAdjacencyLists.get(nodeIndex);
			for (int eidx: neis.toArray())
				result += weights.get(eidx);
		}
		
		if (directed && mode == Directedness.IN) {
			neis = inEdgeAdjacencyLists.get(nodeIndex);
			for (int eidx: neis.toArray())
				result += weights.get(eidx);
		}
		
		return result;
	}
}
