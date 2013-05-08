package Types;
import java.util.Vector;

import DS.Compare2D;
import DS.Direction;
/*
 * Programmer: Carlos Folgar
 * Project: Major
 * Last modification date: 12/3/2012
 */


/**
 * The purpose of the coordinate class is to encapsulate the geographical data associated
 * with a particual record. Particularly, its Latitude, longitude, and file offset at which
 * the record may be found at the database.
 * */
public class Coordinate implements Compare2D<Coordinate> {

	// X and Y coordinate locating the point on a 2D-plane
	private long xcoord;
	private long ycoord;
	
	// Offset values
	private Vector<Long> collectionOffsets;
	private long recordFileOffset;
	
	// Default constructor defaults the location to the center of the 2D-plane
	public Coordinate() {
		xcoord = 0;
		ycoord = 0;
		collectionOffsets = new Vector<Long>();
	}
	
	// Constructs a new point from the given x and y coordinates
	public Coordinate(long x, long y) {
		xcoord = x;
		ycoord = y;
		collectionOffsets = new Vector<Long>();
	}
	
	// Constructs a new point from the given x and y coordinates
	public Coordinate(long x, long y, long offset) {
		xcoord = x;
		ycoord = y;
		collectionOffsets = new Vector<Long>();
		recordFileOffset = offset;
		collectionOffsets.add(offset); // Should always add its own offset
	}
	
	public long getOffset() {
		return recordFileOffset;
	}
	
	
	/**
	 * @param offset Offset value at which the record with same coordinates may be found
	 */
	public void addOffset(long offset) {
		collectionOffsets.add(offset);
	}
	
	/**
	 * @return A vector containing the file offsets at which to find records with same coordinates
	 */
	public Vector<Long> grabOffsets()
	{
		return collectionOffsets;
	}
	
	
	// Return the points x-coordinate
	public long getX() {
		return xcoord;
	}
	// Return the points y-coordinate
	public long getY() {
		return ycoord;
	}
	
	// Returns indicator of the direction to the user data object from the 
	// location (X, Y) specified by the parameters.
	// The indicators are defined in the enumeration Direction, and are used
	// as follows:
	//
	//    NE:  vector from (X, Y) to user data object has a direction in the 
	//         range [0, 90) degrees (relative to the positive horizontal axis
	//    NW:  same as above, but direction is in the range [90, 180) 
	//    SW:  same as above, but direction is in the range [180, 270)
	//    SE:  same as above, but direction is in the range [270, 360)  
	//    NOQUADRANT:  location of user object is equal to (X, Y)
	//
	public Direction directionFrom(long X, long Y) {
		
		// Check to see if same point
		if (xcoord == X && ycoord == Y)
			return Direction.NOQUADRANT;
		
		// Obtain the differences in the x and y direction of the two points
		long dx = xcoord - X;
		long dy = ycoord - Y;
		
		// Locate based on the calculated x-coordinate and y-coordinate deltas
		if (dx > 0 && dy >= 0)
			return Direction.NE;
		else if (dx >= 0 && dy < 0)
			return Direction.SE;
		else if (dx < 0 && dy <= 0)
			return Direction.SW;
		else 
			return Direction.NW;
   }
	
	// Returns indicator of which quadrant of the rectangle specified by the
	// parameters that user data object lies in.
	// The indicators are defined in the enumeration Direction, and are used
	// as follows, relative to the center of the rectangle:
	//
	//    NE:  user data object lies in NE quadrant, including non-negative
	//         x-axis, but not the positive y-axis      
	//    NW:  user data object lies in the NW quadrant, including the positive
	//         y-axis, but not the negative x-axis
	//    SW:  user data object lies in the SW quadrant, including the negative
	//         x-axis, but not the negative y-axis
	//    SE:  user data object lies in the SE quadrant, including the negative
	//         y-axis, but not the positive x-axis
	//    NOQUADRANT:  user data object lies outside the specified rectangle
	//
	public Direction inQuadrant(double xLo, double xHi, 
                               double yLo, double yHi) { 
		
		// Horizontal or vertical lines have no quadrants. Also do not keep chekcing if coord isnt in box
		if ( xLo == xHi || yLo == yHi || !inBox(xLo, xHi, yLo, yHi))
			return Direction.NOQUADRANT;
		else {
			
			// Obtain the coordinates of the midpoints to locate the intersection of the four quadrants (center)
			// Double precision numbers used because midpoint coordinates are not always even numbers (consider odd-even calculations)
			double midX = xLo + (xHi-xLo)/2;
			double midY = yLo + (yHi-yLo)/2;
			
			// Check if greater than positive x-axis and greater than or equal to the midY
			if ( (xcoord > midX && ycoord >= midY) || (xcoord == midX && ycoord == midY) )
				return Direction.NE;
			else if (xcoord <= midX && ycoord > midY)
				return Direction.NW;
			else if (xcoord < midX && ycoord <= midY)
				return Direction.SW;
			else
				return Direction.SE;
		}
   }
	
	// Returns true iff the user data object lies within or on the boundaries
	// of the rectangle specified by the parameters.
	public boolean inBox(double xLo, double xHi, 
                          double yLo, double yHi) { 
      if (xcoord >= xLo && xcoord <= xHi && ycoord >= yLo && ycoord <= yHi) return true;
      else return false;
   }
	
	// Returns a string that represents the user data object
	public String toString() {
      StringBuilder toStr = new StringBuilder();
      toStr.append("[(" + xcoord + ", " + ycoord + "), ");
      for (int i = 0; i < collectionOffsets.size(); i++) {
    	  toStr.append(collectionOffsets.elementAt(i) + " ");
      }
      toStr.append("]");
      return toStr.toString();
   }

	// Overrides the user data object's inherited equals() method with an
	// appropriate definition; it is necessary to place this in the interface
	// that is used as a bound on the type parameter for the generic spatial
	// structure, otherwise the compiler will bind to Object.equals(), which
	// will almost certainly be inappropriate.
	public boolean equals(Object o) { 
		// Check for correct type and cast if possible
		if (o instanceof Coordinate && ((Coordinate)o).xcoord == xcoord && ((Coordinate)o).ycoord == ycoord) return true;
		else return false;
      
   }
}
