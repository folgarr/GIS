package DS;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import Types.Coordinate;

/*
 * Programmer: Carlos Folgar
 * Project: GIS (Major)
 * Last modification date: Dec 3 2012
 * 
 */

/*
 * The generic Point-Region quad-tree implement the standard find/insert/delete
 * function associated with the PR-quadtree.  This generic is able to store
 * user data points that implement the compare2D interface.
 */
/**
 * @author Carlos Folgar
 *
 * @param <T> The data type that will be stored inside of the PR quad node leaves.
 */
public class prQuadtree< T extends Compare2D<? super T> > {

	// You must use a hierarchy of node types with an abstract base
	// class.  You may use different names for the node types if
	// you like (change displayHelper() accordingly).
	abstract class prQuadNode { }


	/*
	 * The prQuadLeaf is used as a PR Quadtree node that contains a
	 * set of elements (which for now is restricted to 1 elemts). Note
	 * the absence of pointers to other nodes as it is simply a leaf.
	 */
	class prQuadLeaf extends prQuadNode {

		// Store the single element
		public prQuadLeaf(T elem) {

			Elements = new Vector<T>();
			Elements.add(elem);
		}
		Vector<T> Elements;   // bucket of data points (default to 1)

		/* 
		 * Call when an additional element needs to be inserted into the leaf
		 * and therefore splitting must occur by creating necessary internal nodes
		 * 
		 * Input:
		 * 	xMinQuad, yMinQuad, xMaxQuad, yMaxQuad : coordinate points defining the area that the new region will encompass
		 * 	elem: The user data point that needs to be inserted but is causing splitting to occur
		 */
		private prQuadInternal splitLeafAndInsert(T elem, double xMinQuad, double yMinQuad, double xMaxQuad, double yMaxQuad) 
		{

			// Create a new internal node that will replace the leaf in location
			prQuadInternal newInternal = new prQuadInternal();

			// Find the quadrants where the vector elements would need to be placed
			Direction toPlace1 = this.Elements.firstElement().inQuadrant(xMinQuad, xMaxQuad, yMinQuad, yMaxQuad);
			Direction toPlace2 = this.Elements.elementAt(1).inQuadrant(xMinQuad, xMaxQuad, yMinQuad, yMaxQuad);
			Direction toPlace3 = this.Elements.elementAt(2).inQuadrant(xMinQuad, xMaxQuad, yMinQuad, yMaxQuad);
			Direction toPlace4 = this.Elements.elementAt(3).inQuadrant(xMinQuad, xMaxQuad, yMinQuad, yMaxQuad);
			Direction locationOfElem = elem.inQuadrant(xMinQuad, xMaxQuad, yMinQuad, yMaxQuad);

			Direction[] directionArray = {toPlace1, toPlace2, toPlace3, toPlace4, locationOfElem};



			// If all elements land in the same quadrant, more splitting needs to be done. Recurse until they land elsewhere.
			if (allInSameQuadrant(directionArray))
			{

				// The height and width of the quadrants inside this internal region
				double quadWidth = (xMaxQuad - xMinQuad)/2;
				double quadHeight = (yMaxQuad - yMinQuad)/2;

				if (toPlace1 == Direction.NE)
					newInternal.NE = splitLeafAndInsert(elem, xMinQuad + quadWidth, yMinQuad + quadHeight, xMaxQuad, yMaxQuad);
				else if (toPlace1 == Direction.NW)
					newInternal.NW = splitLeafAndInsert(elem, xMinQuad, yMinQuad + quadHeight, xMinQuad + quadWidth, yMaxQuad);
				else if (toPlace1 == Direction.SE)
					newInternal.SE = splitLeafAndInsert(elem, xMinQuad + quadWidth, yMinQuad, xMaxQuad, yMinQuad + quadHeight);
				else if (toPlace1 == Direction.SW)
					newInternal.SW = splitLeafAndInsert(elem, xMinQuad, yMinQuad, xMinQuad + quadWidth, yMinQuad + quadHeight);
				else // Return to null to indicate error - desired quad should always be inside the region of the internal node
					return null;

				// Return the newly created internal node that has previous vector elements
				return newInternal;		   
			}
			else // Not all vector elements in the same quadrant, place the elem in newest created internal node
			{
				newInternal.connectLeaf(toPlace1, this.Elements.firstElement());
				newInternal.connectLeaf(toPlace2, this.Elements.elementAt(1));
				newInternal.connectLeaf(toPlace3, this.Elements.elementAt(2));
				newInternal.connectLeaf(toPlace4, this.Elements.elementAt(3));

				// Get the quadrant inside the new internal node in which this new elem should go 
				newInternal.connectLeaf(locationOfElem, elem);

				return newInternal;
			}

		}

		/**
		 * @param allDirections The collection of direction in which elements inside the quadrants reside
		 * @return True if the direction enums describe them all belonging in one quadrant, else false
		 */
		private boolean allInSameQuadrant(Direction[] allDirections) 
		{
			// Only process a valid collection of diretions
			if (allDirections.length > 0) 
			{		   
				// Check if any of the directions is different and notify caller
				for (Direction dir: allDirections) {
					if (allDirections[0] != dir) return false;
				}			   
				return true;			   
			} 
			else return false;
		}


	}

	/*
	 * The prQuadInternal is used as a PR Quadtree node that contains a
	 * set of pointers to other prQuadNodes(NW,NE,SE, and SW). Note
	 * the absence of element storage as this node only defines a region.
	 */
	class prQuadInternal extends prQuadNode {

		/*
		 * Main constructor for the internal node initializes
		 * the region pointers within to null. User is forced
		 * to set these pointer appropriately.
		 */
		public prQuadInternal() {
			NW = NE = SE = SW = null;
		}
		// Pointers to quadrant regions
		public prQuadNode NW, NE, SE, SW;

		/* Checks if the internal node is underflowing, which happens when an internal node
		 * has no pointers to any other valid nodes or it only has one valid pointer to a leaf.
		 * In both of the said cases, it makes no sense to remain as an internal node and should
		 * therefore be used to indicate that a transformation (possible to a leaf) should occur.
		 * 
		 * Method returns a direction to which quadrant has the region that is causing the underflow
		 */
		private Direction isUnderflowing() {

			// Internal node is underflowing if it has no pointer to any nodes - should never happen
			if (this.NE == null && this.NW == null && this.SW == null && this.SE == null) return Direction.NOQUADRANT;
			// If this internal has a pointer to any other valid internal, then it is not underflowing by definition
			else if (this.NE != null && this.NE.getClass().getName().contains("prQuadtree$prQuadInternal")) return Direction.NOQUADRANT;
			else if (this.NW != null && this.NW.getClass().getName().contains("prQuadtree$prQuadInternal")) return Direction.NOQUADRANT;
			else if (this.SW != null && this.SW.getClass().getName().contains("prQuadtree$prQuadInternal")) return Direction.NOQUADRANT;
			else if (this.SE != null && this.SE.getClass().getName().contains("prQuadtree$prQuadInternal")) return Direction.NOQUADRANT;
			else { // Check for the case where the internal contains only a single valid leaf pointer

				// Count all the leafs on this internal node
				int leafCounter = 0;
				if (this.NE != null && this.NE.getClass().getName().contains("prQuadtree$prQuadLeaf")) leafCounter++;
				if (this.NW != null && this.NW.getClass().getName().contains("prQuadtree$prQuadLeaf")) leafCounter++;
				if (this.SW != null && this.SW.getClass().getName().contains("prQuadtree$prQuadLeaf")) leafCounter++;
				if (this.SE != null && this.SE.getClass().getName().contains("prQuadtree$prQuadLeaf")) leafCounter++;
				// Indicate underflow at an internal leaf count of only one.
				if (leafCounter == 1) {
					// Return the direction of the leaf that is causing the underflow
					if (this.NE != null) return Direction.NE;
					else if (this.NW != null) return Direction.NW;
					else if  (this.SE != null) return Direction.SE;
					else return Direction.SW;
				}
				else return Direction.NOQUADRANT;
			}
		}

		/*
		 * Inputs:
		 * 	dir: Direction enum specifying the region within the internal node at which the desired node resides
		 * 
		 * 	Used as a getter method for the node for a specific quadrant of the internal node.  If an invalid
		 * 	direction is specfied, the direction enum of NOQUADRANT will be returned.
		 */
		public prQuadNode getAtQuadNode(Direction dir) {
			// Not a valid quadran to ask for
			if (dir == Direction.NOQUADRANT) return null;
			// Return the specified quadrant
			else if (dir == Direction.NE) return this.NE;
			else if (dir == Direction.NW) return this.NW;
			else if (dir == Direction.SE) return this.SE;
			else return this.SW;
		}


		/*
		 * Inputs:
		 * 	dir: Specified the region of the internal node to modify (to set)
		 * 	node: The prQuadNode class/or child to set the region node of the internal to
		 * 
		 * Sets a specific quadrant of an internal node to the
		 * specified node.
		 */
		public void setNodeAtRegion(Direction dir, prQuadNode node) {
			if (dir == Direction.NOQUADRANT) return;
			// Return the specified quadrant
			else if (dir == Direction.NE) this.NE = node;
			else if (dir == Direction.NW) this.NW = node;
			else if (dir == Direction.SE) this.SE = node;
			else this.SW = null;
		}

		/*
		 * Input:
		 * 	dir: The quadrant inside the internal node that needs to be removed
		 * 
		 * Removes a node from the internal node at the specified region. Usually a leaf node.
		 */
		private void removeNodeAtRegion(Direction dir) {
			// Not a valid quadran to ask for
			if (dir == Direction.NOQUADRANT) return;
			// Return the specified quadrant
			else if (dir == Direction.NE) this.NE = null;
			else if (dir == Direction.NW) this.NW = null;
			else if (dir == Direction.SE) this.SE = null;
			else this.SW = null;
		}



		/*
		 * Inputs:
		 * 	dir: Specified the quadrant inside the internal node at which to hang a leaf to
		 * 	elem: The element that will be stored inside the new leaf to be hanged
		 * 
		 * Inserts a leaf containing elem into the specified quadrant ("dir") of the internal node.
		 * Pre: Should only be called when the internal nodes desired quad is empty. elem should also
		 * be a valid elem.
		 * Post: A leaf with a data value of elem is hanged at specified quad inside the internal node.
		 */
		@SuppressWarnings("unchecked")
		private void connectLeaf(Direction dir, T elem) {


			if (isQuadEmpty(dir)) {		  

				// Create a leaf with the specified elem to hang
				prQuadLeaf leaf = new prQuadLeaf(elem);

				// Hang the leaf in the specified quadrant ("dir")
				if (dir == Direction.NE) {
					this.NE = leaf;
				}
				else if (dir == Direction.NW) {
					this.NW = leaf;
				}
				else if (dir == Direction.SE) {
					this.SE = leaf;
				}
				else if (dir == Direction.SW) {
					this.SW = leaf;
				}
			} else { // Check if its possible to add to the leaf elements vector in the tree
				if (dir == Direction.NE) {
					if ( ((prQuadLeaf)this.NE).Elements.size( ) < bucket_size )
						((prQuadLeaf)this.NE).Elements.add(elem);
					else 
						System.err.println("Error: Attempting to add more elements to a leaf then allowed by bucket size.");				
				}
				else if (dir == Direction.NW) {
					if ( ((prQuadLeaf)this.NW).Elements.size( ) < bucket_size ) 
						((prQuadLeaf)this.NW).Elements.add(elem);
					else 
						System.err.println("Error: Attempting to add more elements to a leaf then allowed by bucket size.");
				}
				else if (dir == Direction.SE) {
					if ( ((prQuadLeaf)this.SE).Elements.size( ) < bucket_size ) 
						((prQuadLeaf)this.SE).Elements.add(elem);
					else 
						System.err.println("Error: Attempting to add more elements to a leaf then allowed by bucket size.");
				}
				else if (dir == Direction.SW) {
					if ( ((prQuadLeaf)this.SW).Elements.size( ) < bucket_size ) 
						((prQuadLeaf)this.SW).Elements.add(elem);
					else 
						System.err.println("Error: Attempting to add more elements to a leaf then allowed by bucket size.");
				}
			}

		}


		/*
		 * Inputs:
		 * 	desiredQuad: The target region at which to check if empty inside the internal node
		 * 
		 * Checks if the specified quadrant ("desiredQuad") inside this internal node is empty
		 */
		private boolean isQuadEmpty(Direction desiredQuad) 
		{

			// Null-check the qudrants to see if an element resides there indicating that the quad is full
			if (desiredQuad == Direction.NE && this.NE != null) {
				return false;
			}
			else if (desiredQuad == Direction.NW && this.NW != null) {
				return false;
			}
			else if (desiredQuad == Direction.SE && this.SE != null) {
				return false;
			}
			else if (desiredQuad == Direction.SW && this.SW != null) {
				return false;
			}
			else { // No conflict inside the desired quad of the internal node
				return true;
			}
		}

	}



	/*
	 * Class used to define a rectangular region in which the x and y coordinate 
	 * are specified in order to represent a search region (rectangular regions).
	 */
	class RectRegion 
	{

		// Minimum and maximum x & y coordinate that define the rectangular search region
		double xLow, xHigh, yLow, yHigh;


		/*
		 * Inputs:
		 * 	xMin,xMax,yMin,yMax: the coordinates defining a specific search region
		 * 
		 * Main constructor for the region class. Class used in the creation of search
		 * regions (rectangular regions).
		 */
		public RectRegion(double xMin, double xMax, double yMin, double yMax) {
			xLow = xMin;
			xHigh = xMax;
			yLow = yMin;
			yHigh = yMax;
		}

		/*
		 * Inputs:
		 * 	xMin,xMax,yMin,yMax: the coordinates defining a specific region to check for overlap with
		 * 	the currect search region 
		 * 
		 * Checks if the rectangular region overlaps with the rectangle specified by the x&y coordinates
		 */
		private boolean overlapsWith(double xLo, double xHi, double yLo, double yHi) {

			// Simple check to see if the coordinates indicate that the rects are not touching/overlapping in any way
			if (xLow >= xHi || xHigh <= xLo || yHigh <= yLo || yLow >= yHi) return false;
			else return true;		   
		}

	}


	// Top most root node
	prQuadNode root;

	// World region boundary coordinates
	long xMin, xMax, yMin, yMax;

	// Bucket size for leafs
	int bucket_size;


	/*
	 * Inputs:
	 * 	xMin, xMax, yMin, yMax: Coordinates defining the min/max boundaries of the prQuadTree
	 *  
	 *  Initialize quadtree to empty state, representing the specified region. 
	 */
	public prQuadtree(long xMin, long xMax, long yMin, long yMax) 
	{
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		root = null;
		this.bucket_size = 1;
	}




	/**
	 * @param xMin Min x-coord boundary
	 * @param xMax Max x-coord boundary
	 * @param yMin Min y-coord boundary
	 * @param yMax Max y-coord boundary
	 * @param buckSize Size of the bucket for the PR Quad Leafs
	 */
	public prQuadtree(long xMin, long xMax, long yMin, long yMax, int buckSize) 
	{
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		this.root = null;
		this.bucket_size = buckSize;
	}



	// Pre:   elem != null
	// Post:  If elem lies within the tree's region, and elem is not already 
	//        present in the tree, elem has been inserted into the tree.
	// Return true iff elem is inserted into the tree. 
	public boolean insert(T elem) 
	{

		// Check if its an actual element and that its inside the boundaries
		if (elem == null || !elem.inBox(xMin, xMax, yMin, yMax)) return false;
		else if (root == null) { // First insertion case, create a root leaf
			root = new prQuadLeaf(elem);
			return true;
		}
		else return  insertHelper(elem, root, xMin, yMin, xMax, yMax);

	}


	// Pre:   elem != null, sRoot != null, and x and y coords are valid coords within our boundaries
	// Post:  If elem lies within the tree's region, and elem is not already 
	//        present in the tree, elem has been inserted into the tree.
	// Return true iff elem is inserted into the tree.
	@SuppressWarnings("unchecked")
	private boolean insertHelper(T elem, prQuadNode sRoot, double xLo, double yLo, double xHi, double yHi) {

		// Should only happen when there is one element in the tree - the root element (as a leaf node)
		if ( sRoot.getClass().getName().contains("prQuadtree$prQuadLeaf") ) 
		{

			prQuadLeaf pLeaf = (prQuadLeaf)sRoot;

			boolean foundMatch = findMatchingLocation(elem, pLeaf );
			if (foundMatch) return true;
			else if ( pLeaf.Elements.size() < bucket_size) 
			{
				pLeaf.Elements.add(elem);
				return true;
			} else 
			{		   
				root = (prQuadNode)pLeaf.splitLeafAndInsert(elem, xLo, yLo, xHi, yHi);
				return true;
			}
		}

		// Check for and process NE and NW subtrees
		else if ( sRoot.getClass().getName().contains("prQuadtree$prQuadInternal") ) 
		{

			// Find the region where the node needs to go in this quadrant represented by the internal node
			Direction targetRegion = elem.inQuadrant(xLo, xHi, yLo, yHi);
			prQuadInternal pInternal = (prQuadInternal)sRoot;

			// Connect the leaf only if the internal node has space in that region
			if ( ((prQuadInternal)sRoot).isQuadEmpty(targetRegion) ) {
				((prQuadInternal)sRoot).connectLeaf(targetRegion, elem);
				return true;
			}
			else 
			{ // No space available in desired quadrant, recurse until space is available or split required

				// Check if desired target region of NE points to a leaf node that might need to be split if valid
				if (targetRegion == Direction.NE) 
				{

					if ( pInternal.NE.getClass().getName().contains("prQuadtree$prQuadLeaf") ) 
					{
						// Obtain the leaf node that is in our target location
						prQuadLeaf pLeaf = (prQuadLeaf)pInternal.NE;

						//  If the data element does not already exist, insert by splitting at leaf
						boolean foundMatch = findMatchingLocation(elem, pLeaf );
						if (foundMatch)
							return true;
						else if ( pLeaf.Elements.size() < bucket_size) 
						{
							pLeaf.Elements.add(elem);
							return true;
						} else 
						{		   
							pInternal.NE = (prQuadNode)pLeaf.splitLeafAndInsert(elem, xLo + (xHi-xLo)/2, yLo + (yHi-yLo)/2, xHi, yHi);
							return true;
						}
					} 
					else 
						return insertHelper(elem, ((prQuadInternal)sRoot).NE, xLo + (xHi-xLo)/2, yLo + (yHi-yLo)/2, xHi, yHi);
				}
				// Check if desired target region of NW points to a leaf node that might need to be split if valid
				else if (targetRegion == Direction.NW) 
				{

					if ( pInternal.NW.getClass().getName().contains("prQuadtree$prQuadLeaf") ) 
					{

						prQuadLeaf pLeaf = (prQuadLeaf)pInternal.NW;

						//  If the data element does not already exist, insert by splitting at leaf
						boolean foundMatch = findMatchingLocation(elem, pLeaf );
						if (foundMatch)
							return true;
						else if ( pLeaf.Elements.size() < bucket_size) 
						{
							pLeaf.Elements.add(elem);
							return true;
						} else 
						{		   
							pInternal.NW = (prQuadNode)pLeaf.splitLeafAndInsert(elem, xLo, yLo + (yHi-yLo)/2, xLo + (xHi-xLo)/2, yHi);
							return true;
						}
					}
					else 
						return insertHelper(elem, pInternal.NW, xLo, yLo + (yHi-yLo)/2, xLo + (xHi-xLo)/2, yHi);

				}
				// Check if desired target region of SW points to a leaf node that might need to be split if valid
				else if (targetRegion == Direction.SW) 
				{

					if ( pInternal.SW.getClass().getName().contains("prQuadtree$prQuadLeaf") ) 
					{

						prQuadLeaf pLeaf = (prQuadLeaf)pInternal.SW;

						//  If the data element does not already exist, insert by splitting at leaf
						boolean foundMatch = findMatchingLocation(elem, pLeaf );
						if (foundMatch)
							return true;
						else if ( pLeaf.Elements.size() < bucket_size) 
						{
							pLeaf.Elements.add(elem);
							return true;
						} else 
						{		   
							pInternal.SW = (prQuadNode)pLeaf.splitLeafAndInsert(elem, xLo, yLo, xLo + (xHi-xLo)/2, yLo + (yHi-yLo)/2);
							return true;
						}
					}
					else return insertHelper(elem, ((prQuadInternal)sRoot).SW, xLo, yLo, xLo + (xHi-xLo)/2, yLo + (yHi-yLo)/2);
				}
				// Check if desired target region of SE points to a leaf node that might need to be split if valid
				else if (targetRegion == Direction.SE) 
				{
					if ( pInternal.SE.getClass().getName().contains("prQuadtree$prQuadLeaf") ) 
					{

						prQuadLeaf pLeaf = (prQuadLeaf)pInternal.SE;

						//  If the data element does not already exist, insert by splitting at leaf
						boolean foundMatch = findMatchingLocation(elem, pLeaf);
						if (foundMatch)
							return true;
						else if ( pLeaf.Elements.size() < bucket_size) 
						{
							pLeaf.Elements.add(elem);
							return true;
						} else 
						{		   
							pInternal.SE = (prQuadNode)pLeaf.splitLeafAndInsert(elem, xLo + (xHi-xLo)/2, yLo, xHi, yLo + (yHi-yLo)/2);
							return true;
						}
					}
					else return insertHelper(elem, ((prQuadInternal)sRoot).SE, xLo + (xHi-xLo)/2, yLo, xHi, yLo + (yHi-yLo)/2);
				}
				else return false;

			}
		}
		else return false;
	}



	/**
	 * @param elemToInsert Element to be inserted into quad-tree
	 * @param leaf leaf node that already contains elements
	 * @return A boolean value indicating the success (true) of the search for a match
	 */
	private boolean findMatchingLocation(T elemToInsert, prQuadLeaf leaf) {

		for (T elem : leaf.Elements) {

			if (elemToInsert.equals(elem) && elemToInsert.getClass().getName().contains("Coordinate")) {
				((Coordinate)elem).addOffset( ((Coordinate)elemToInsert).getOffset() );
				return true;
			}
		}
		return false;
	}




	// Pre:  elem != null
	// Post: If elem lies in the tree's region, and a matching element occurs
	//       in the tree, then that element has been removed.
	// Returns true iff a matching element has been removed from the tree.
	public boolean delete(T elem) {

		// Check if valid elem and if elem does not exist in the tree
		if (elem == null || find(elem) == null) return false;
		// Check case where there is only one node in the tree - a leaf node acting as root
		else if (root.getClass().getName().contains("prQuadtree$prQuadLeaf") && ((prQuadLeaf)root).Elements.firstElement().equals(elem)) {
			root = null;
			return true;
		}
		else {
			// Delete helper will return the root after the deletion has occured
			root = deleteHelper(elem, root, xMin, yMin, xMax, yMax);
			return (root != null);
		}
	}

	// Pre:  elem != null, sRoot = root of the tree (or subtree) at which search for deletion starts
	//		xLo,yLo,xHi,yHi: Coordinates at which search for target of delete occurs at
	// Post: If elem lies in the tree's region, and a matching element occurs
	//       in the tree, then that element has been removed.
	// Returns true iff a matching element has been removed from the tree.
	@SuppressWarnings("unchecked")
	private prQuadNode deleteHelper(T elem, prQuadNode sRoot, double xLo, double yLo, double xHi, double yHi) {

		// Should only happen in the case where the root of the pr quad tree is a leaf - delete leaf and return null
		if ( sRoot.getClass().getName().contains("prQuadtree$prQuadLeaf") && ((prQuadLeaf)sRoot).Elements.firstElement().equals(elem)) {
			root = null;
			return null;
		}
		// If recursive traversal reaches an internal than check if the pointer to target region contains a leaf
		// If so, take action based on whether it might cause underflow or not (need to contract if so)
		else if ( sRoot.getClass().getName().contains("prQuadtree$prQuadInternal") ) {

			// Find the region where the node needs to go in this quadrant represented by the internal node
			Direction targetRegion = elem.inQuadrant(xLo, xHi, yLo, yHi);

			// Check case if the target region for this internal is a leaf
			if (  targetRegion != Direction.NOQUADRANT && ((prQuadInternal)sRoot).getAtQuadNode(targetRegion) != null && 
					((prQuadInternal)sRoot).getAtQuadNode(targetRegion).getClass().getName().contains("prQuadtree$prQuadLeaf")) {

				// Remove the leaf from the internal node
				((prQuadInternal)sRoot).removeNodeAtRegion(targetRegion);

				// Check for underflow, if found contract else return the root
				Direction underFlowDirection = ((prQuadInternal)sRoot).isUnderflowing();

				// Detect the underflow or skip the needed contractions if not needed
				if (underFlowDirection != Direction.NOQUADRANT) {
					// Set the internal to simply a leaf node
					sRoot = ((prQuadInternal)sRoot).getAtQuadNode(underFlowDirection);
					return sRoot;
				} else return sRoot; // Return the internal node as root of subtree

			}
			// Traverse further at the signed that the target region is an internal node
			else if ( targetRegion != Direction.NOQUADRANT && ((prQuadInternal)sRoot).getAtQuadNode(targetRegion) != null && 
					((prQuadInternal)sRoot).getAtQuadNode(targetRegion).getClass().getName().contains("prQuadtree$prQuadInternal")) {

				// Check for underflow here also because the contraction further down the tree could cause underflow above aswell
				if (targetRegion == Direction.NE) {
					((prQuadInternal)sRoot).NE = deleteHelper(elem, ((prQuadInternal)sRoot).NE, xLo + (xHi-xLo)/2, yLo + (yHi-yLo)/2, xHi, yHi);

					// Check for underflow, if found contract else return the root
					Direction underFlowDirection = ((prQuadInternal)sRoot).isUnderflowing();

					// Detect the underflow or skip the needed contractions if not needed
					if (underFlowDirection != Direction.NOQUADRANT) {
						// Set the internal to simply a leaf node
						sRoot = ((prQuadInternal)sRoot).getAtQuadNode(underFlowDirection);
						return sRoot;
					} else return sRoot; // Return the internal node as root of subtree


				} else if (targetRegion == Direction.NW) {
					((prQuadInternal)sRoot).NW = deleteHelper(elem, ((prQuadInternal)sRoot).NW, xLo, yLo + (yHi-yLo)/2, xLo + (xHi-xLo)/2, yHi);

					// Check for underflow, if found contract else return the root
					Direction underFlowDirection = ((prQuadInternal)sRoot).isUnderflowing();

					// Detect the underflow or skip the needed contractions if not needed
					if (underFlowDirection != Direction.NOQUADRANT) {
						// Set the internal to simply a leaf node
						sRoot = ((prQuadInternal)sRoot).getAtQuadNode(underFlowDirection);
						return sRoot;
					} else return sRoot; // Return the internal node as root of subtree

				} else if (targetRegion == Direction.SW) {

					((prQuadInternal)sRoot).SW = deleteHelper(elem, ((prQuadInternal)sRoot).SW, xLo, yLo, xLo + (xHi-xLo)/2, yLo + (yHi-yLo)/2);

					// Check for underflow, if found contract else return the root
					Direction underFlowDirection = ((prQuadInternal)sRoot).isUnderflowing();

					// Detect the underflow or skip the needed contractions if not needed
					if (underFlowDirection != Direction.NOQUADRANT) {
						// Set the internal to simply a leaf node
						sRoot = ((prQuadInternal)sRoot).getAtQuadNode(underFlowDirection);
						return sRoot;
					} else return sRoot; // Return the internal node as root of subtree

				} else { // Target region is SE

					((prQuadInternal)sRoot).SE = deleteHelper(elem, ((prQuadInternal)sRoot).SE, xLo + (xHi-xLo)/2, yLo, xHi, yLo + (yHi-yLo)/2);

					// Check for underflow, if found contract else return the root
					Direction underFlowDirection = ((prQuadInternal)sRoot).isUnderflowing();

					// Detect the underflow or skip the needed contractions if not needed
					if (underFlowDirection != Direction.NOQUADRANT) {
						// Set the internal to simply a leaf node
						sRoot = ((prQuadInternal)sRoot).getAtQuadNode(underFlowDirection);
						return sRoot;
					} else return sRoot; // Return the internal node as root of subtree
				}
			} else return null; // Target region does not lead to a viable path to reach any element - element does not exist
		} else return sRoot;
	}



	// Pre:  elem != null
	// Returns reference to an element x within the tree such that 
	// elem.equals(x)is true, provided such a matching element occurs within
	// the tree; returns null otherwise.
	public T find(T elem) {

		// Search is only carried out if the element is in our original coordinate space
		if (elem != null && elem.inBox(xMin, xMax, yMin, yMax)) {
			// Return the result of the recursive search starting at our quad-tree root
			return findHelper(root, elem, xMin, xMax, yMin, yMax);
		} else return null;
	}

	// Pre:  xLo, xHi, yLo and yHi define a rectangular region
	// Returns a collection of (references to) all elements x such that x is 
	// in the tree and x lies at coordinates within the defined rectangular 
	// region, including the boundary of the region.
	public Vector<T> find(long xLo, long xHi, long yLo, long yHi) {

		// Create the search region specified by coordinates
		RectRegion searchRegion = new RectRegion(xLo, xHi, yLo, yHi);

		// Carry out region search if certain the search region falls within tree boundaries
		if (searchRegion.overlapsWith(xMin, xMax, yMin, yMax) && root != null) {
			Vector<T> elemCollector = new Vector<T>(); // Collector of search results
			return collectFind(searchRegion, elemCollector, root, xMin, xMax, yMin, yMax);
		}
		else return null;      
	}


	public int totalInRegion(long xLo, long xHi, long yLo, long yHi)
	{
		// Create the search region specified by coordinates
		RectRegion searchRegion = new RectRegion(xLo, xHi, yLo, yHi);

		// Carry out region search if certain the search region falls within tree boundaries
		if (searchRegion.overlapsWith(xMin, xMax, yMin, yMax) && root != null) {
			Vector<T> elemCollector = new Vector<T>(); // Collector of search results
			elemCollector =  collectFind(searchRegion, elemCollector, root, xMin, xMax, yMin, yMax);
			int numberOfElements = 0;

			// If seaching for coordinates, we must add the found file offsets stored
			if (elemCollector.firstElement().getClass().getName().contains("Coordinate")) 
			{
				for (T element: elemCollector) 
					numberOfElements += ((Coordinate)element).grabOffsets().size();
			} else
				return elemCollector.size(); // Tree stored non-coordinate elements

			return numberOfElements;
		}
		else return 0;

	}

	/* Pre:  searchReg defines the rectangular serch region while
	 * xLo, xHi, yLo, yHi define the rectangular region of a tree node
	 * elemColl defines the vector that will hold the search results
	 * sRoot defines the root of the subtree at which search is taking place

	 * Returns a collection of (references to) all elements x such that x is 
	 * in the tree and x lies at coordinates within the defined rectangular 
    region, including the boundary of the region.
	 */
	@SuppressWarnings("unchecked")
	private Vector<T> collectFind(RectRegion searchReg, Vector <T> elemColl, prQuadNode sRoot,double xLo, double xHi, double yLo, double yHi) {

		// Reaching of an empty/non-existing node should stop recursive traversal and send back original collection
		if (sRoot == null) return elemColl;

		// Leaf arrival in recursive traversal occurs when the a target data point is reached - still check if its inside
		// search region to prevent errors with single element tree (root = a leaf)
		else if (sRoot.getClass().getName().contains("prQuadtree$prQuadLeaf"))
		{ 

			prQuadLeaf pLeaf = (prQuadLeaf)sRoot;
			for (T ele: pLeaf.Elements)
				if(ele.inBox(searchReg.xLow, searchReg.xHigh, searchReg.yLow, searchReg.yHigh)) elemColl.add(ele);

			return elemColl;
		}
		else if (sRoot.getClass().getName().contains("prQuadtree$prQuadInternal")){ // Traversal arrival at internal node

			// Check for intersection with the NE region of the internal node
			if (searchReg.overlapsWith(xLo + (xHi-xLo)/2, xHi, yLo + (yHi-yLo)/2, yHi)) {
				elemColl = collectFind(searchReg, elemColl, ((prQuadInternal)sRoot).NE, xLo + (xHi-xLo)/2, xHi, yLo + (yHi-yLo)/2, yHi);
			}
			// Check for intersection with the NW region of the internal node
			if (searchReg.overlapsWith(xLo , xLo + (xHi-xLo)/2, yLo + (yHi-yLo)/2, yHi)) {
				elemColl = collectFind(searchReg, elemColl, ((prQuadInternal)sRoot).NW,xLo , xLo + (xHi-xLo)/2, yLo + (yHi-yLo)/2, yHi);
			}
			// Check for intersection with the SE region of the internal node
			if (searchReg.overlapsWith(xLo + (xHi-xLo)/2 , xHi, yLo, yLo + (yHi-yLo)/2)) {
				elemColl = collectFind(searchReg, elemColl, ((prQuadInternal)sRoot).SE,xLo + (xHi-xLo)/2 , xHi, yLo, yLo + (yHi-yLo)/2);
			}
			// Check for intersection with the SW region of the internal node
			if (searchReg.overlapsWith(xLo , xLo + (xHi-xLo)/2, yLo, yLo + (yHi-yLo)/2)) {
				elemColl = collectFind(searchReg, elemColl, ((prQuadInternal)sRoot).SW, xLo , xLo + (xHi-xLo)/2, yLo, yLo + (yHi-yLo)/2);
			}
			// Return the element loaded vector from the region search for this particular internal
			return elemColl;
		}
		else // Traversal reached empty/null node - no need for further recursive traversal 
			return elemColl;
	}


	// Pre:  elem != null, sRoot = valid subtree root, and valid x&y coordinates
	// Returns reference to an element x within the tree such that 
	// elem.equals(x)is true, provided such a matching element occurs within
	// the tree; returns null otherwise.
	@SuppressWarnings("unchecked")
	private T findHelper(prQuadNode sRoot, T elem, double xLo, double xHi, double yLo, double yHi) {

		// Null check the root to prevent null-value related exceptions
		if (sRoot == null) return null;

		// Arrival at leaf with possible elem value - check for match
		if (sRoot.getClass().getName().contains("prQuadtree$prQuadLeaf")) {
			prQuadLeaf pLeaf = (prQuadLeaf)sRoot;
			for (T element : pLeaf.Elements)
			{
				if (element.equals(elem)) return element;
			}
			return null;
		} 
		else { // Arrival at internal node

			// Get which quadrant the elem belongs to inside this internal
			Direction direction = elem.inQuadrant(xLo, xHi, yLo, yHi);
			// Recursively traverse down the quad tree attempting to find the leaf
			if (direction == Direction.NE) 		return findHelper( ((prQuadInternal)sRoot).NE , elem, xLo + (xHi-xLo)/2, xHi, yLo + (yHi-yLo)/2, yHi);
			else if (direction == Direction.NW) 	return findHelper( ((prQuadInternal)sRoot).NW , elem, xLo , xLo + (xHi-xLo)/2, yLo + (yHi-yLo)/2, yHi);
			else if (direction == Direction.SW) 	return findHelper( ((prQuadInternal)sRoot).SW , elem, xLo , xLo + (xHi-xLo)/2, yLo, yLo + (yHi-yLo)/2);
			else if (direction == Direction.SE) 	return findHelper( ((prQuadInternal)sRoot).SE , elem, xLo + (xHi-xLo)/2 , xHi, yLo, yLo + (yHi-yLo)/2);
			else {
				return null; // Data point lies outside boundaries. Should not be reached
			}

		}

	}


	// Print method
	public void print() {
		printTreeHelper(root, "         ");
	}



	/**
	 * @param writer FileWriter with a reference to an object that will obtain the quadtree representation
	 */
	public void printToFile(FileWriter writer) 
	{
		// Padding for the file print
		String padding = "";
		// Recursive print method
		printToFileHelper(root, padding, writer);
	}


	/**
	 * @param sRoot root of the tree
	 * @param Padding Padding to help in the print of the tree nodes
	 * @param fileWriter FileWriter with a reference to a file to receive the printed tree output stream
	 */
	private void printToFileHelper(prQuadNode sRoot, String Padding, FileWriter fileWriter) 
	{
		try
		{
			// Check for empty leaf
			if ( sRoot == null ) 
			{
				fileWriter.write(Padding + "*\n");
				return;
			}

			// Check for and process SW and SE subtrees
			if ( sRoot.getClass().getName().contains("prQuadtree$prQuadInternal") ) 
			{
				prQuadInternal p = (prQuadInternal) sRoot;
				printToFileHelper(p.SW, Padding + "    ", fileWriter);
				printToFileHelper(p.SE, Padding + "    ", fileWriter);
			}

			// Display indentation padding for current node
			//fileWriter.write(Padding + "\n");

			// Determine if at leaf or internal and display accordingly
			if ( sRoot.getClass().getName().contains("prQuadtree$prQuadLeaf") ) 
			{
				prQuadLeaf p = (prQuadLeaf) sRoot;   
				fileWriter.write(Padding);
				for (int i = 0; i < p.Elements.size(); i++) {
					fileWriter.write(p.Elements.elementAt(i).toString());
				}
				fileWriter.write("\n");
			}
			else
				fileWriter.write( Padding + "@\n" );

			// Check for and process NE and NW subtrees
			if ( sRoot.getClass().getName().contains("prQuadtree$prQuadInternal") ) 
			{
				prQuadInternal p = (prQuadInternal) sRoot;
				printToFileHelper(p.NE, Padding + "    ", fileWriter);
				printToFileHelper(p.NW, Padding + "    ", fileWriter);
			}

		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}


	/*
	 * Prints the PR Quadtree
	 */
	private void printTreeHelper(prQuadNode sRoot, String Padding) 
	{

		// Check for empty leaf
		if ( sRoot == null ) {
			System.out.println(Padding + "*\n");
			return;
		}

		// Check for and process SW and SE subtrees
		if ( sRoot.getClass().getName().contains("prQuadtree$prQuadInternal") ) {
			prQuadInternal p = (prQuadInternal) sRoot;
			printTreeHelper(p.SW, Padding + "         ");
			printTreeHelper(p.SE, Padding + "         ");
		}

		// Display indentation padding for current node
		System.out.println(Padding);
		// Determine if at leaf or internal and display accordingly
		if ( sRoot.getClass().getName().contains("prQuadtree$prQuadLeaf") ) {
			prQuadLeaf p = (prQuadLeaf) sRoot;   
			System.out.print( Padding);
			for (int i = 0; i < p.Elements.size(); i++) {
				System.out.print(p.Elements.elementAt(i));
			}
			System.out.print("\n");
		}
		else
			System.out.println( Padding + "@\n" );


		// Check for and process NE and NW subtrees
		if ( sRoot.getClass().getName().contains("prQuadtree$prQuadInternal") ) {
			prQuadInternal p = (prQuadInternal) sRoot;
			printTreeHelper(p.NE, Padding + "         ");
			printTreeHelper(p.NW, Padding + "         ");
		}

	}

}
