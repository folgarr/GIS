package Indexes;
import java.io.FileWriter;
import java.util.Vector;

import Parsers.RecordParser;
import Types.Coordinate;
import DS.prQuadtree;

/*
 * Programmer: Carlos Folgar
 * Project: Major
 * Last modification date: 12/3/2012
 * 
 */

/**
 * @author Carlos Folgar
 *
 * The purpose of this class is to keep an index of coordinates that may be stored in the
 * inter PR QuadTree structure and later retrieved to obtain the file offset of a particular
 * record in a database file.
 */
public class CoordinateIndex {

	/**
	 * The internal PR Quadtree structure that contains the coordinate points inside the region of
	 * the world that has been specified.
	 */
	private prQuadtree<Coordinate> quadtree;

	/** West boundary coordinate of the world represented by the PR Quadtree structure	 */
	private long westBoundary;
	/** East boundary coordinate of the world represented by the PR Quadtree structure	 */
	private long eastBoundary;
	/** North boundary coordinate of the world represented by the PR Quadtree structure	 */
	private long northBoundary;
	/** South boundary coordinate of the world represented by the PR Quadtree structure	 */
	private long southBoundary;
	/** Used to keep track of how many records were imported and index by location	 */
	public int importedFeaturesByLocation = 0;
	/** Bucket size used in the internal tree structure */
	private final int BUCKET_SIZE = 4;
	
	/**
	 * @param west A string representation on the west boundary of the world
	 * @param east A string representation on the east boundary of the world
	 * @param north A string representation on the north boundary of the world
	 * @param south A string representation on the south boundary of the world
	 */
	public CoordinateIndex(String west, String east, String south, String north) 
	{
		// Given the coordinate strings, parse them to create valid boundaries for the world
		westBoundary = RecordParser.parseLongitude(west);
		eastBoundary = RecordParser.parseLongitude(east);
		northBoundary = RecordParser.parseLatitude(north);
		southBoundary = RecordParser.parseLatitude(south);
		
		// Create the internal quadtree with coordinate boundaries
		quadtree = new prQuadtree<Coordinate>(westBoundary, eastBoundary, southBoundary,northBoundary, BUCKET_SIZE);
	}

	/**
	 * @return the westBoundary
	 */
	public long getWestBoundary() {
		return westBoundary;
	}

	/**
	 * @param westBoundary the westBoundary to set
	 */
	public void setWestBoundary(long westBoundary) {
		this.westBoundary = westBoundary;
	}

	/**
	 * @return the eastBoundary
	 */
	public long getEastBoundary() {
		return eastBoundary;
	}

	/**
	 * @param eastBoundary the eastBoundary to set
	 */
	public void setEastBoundary(long eastBoundary) {
		this.eastBoundary = eastBoundary;
	}

	/**
	 * @return the northBoundary
	 */
	public long getNorthBoundary() {
		return northBoundary;
	}

	/**
	 * @param northBoundary the northBoundary to set
	 */
	public void setNorthBoundary(long northBoundary) {
		this.northBoundary = northBoundary;
	}

	/**
	 * @return the southBoundary
	 */
	public long getSouthBoundary() {
		return southBoundary;
	}

	/**
	 * @param southBoundary the southBoundary to set
	 */
	public void setSouthBoundary(long southBoundary) {
		this.southBoundary = southBoundary;
	}

	/**
	 * @param lon The longitude portion of the geographic coordinate
	 * @param lat The latitude portion of the geographic coordinate
	 * @param value The long value stored in the quadtree as the offset for the geographic coordinate
	 * @return A boolean indicating the success of the insertion operation on the quadtree
	 */
	public boolean add(String lon, String lat, Long value)
	{
		long longitude = RecordParser.parseLongitude(lon);
		long latitude = RecordParser.parseLatitude(lat);
		importedFeaturesByLocation++;
		// Insert the created coord
		return quadtree.insert(new Coordinate(longitude, latitude, value));
	}
	
	
	/**
	 * @param writer writer object with a reference to a file that will receive the representation of the quadtree
	 */
	public void printToFile(FileWriter writer)
	{
		quadtree.printToFile(writer);
	}
	
	/**
	 * @param recordLine Line inside of a records file
	 * @param value The offset value to be stored at a node in quadtree
	 * @return The boolean indicating the success value of the insertion
	 */
	public boolean add(String recordLine, Long value)
	{
		String[] latLonArr = RecordParser.grabLatAndLon(recordLine);
		long longitude = RecordParser.parseLongitude(latLonArr[1]);
		long latitude = RecordParser.parseLatitude(latLonArr[0]);
		importedFeaturesByLocation++;
		// Insert the created coord
		return quadtree.insert(new Coordinate(longitude, latitude, value));		
	}

	
	/**
	 * @param latLon Single string representing two coordinates. Simplifies operations from the parsers.
	 * @return A vector of values of type long representing offsets where matching entries may be found in the DB file
	 */
	public Vector<Long> searchWithCoord(String latLon)
	{
		if (latLon.contains(" "))
		{
			String[] latAndLon = latLon.split(" ");
			return search(latAndLon[1], latAndLon[0]);
		} 
		else 
		{
			System.err.println("Error: Invalid coordinate string format, must be in format:<Lat> <Lon>");
			return null;
		}
	}
	
	/**
	 * @param lon The longitude portion of the geographic coordinate to find
	 * @param lat The latitude portion of the geographic coordinate to find
	 * @return A vector of values of type long representing offsets where matching entries may be found in the DB file
	 */
	public Vector<Long> search(String lon, String lat) 
	{
		// Find the lat/longitude and use it to construct a coordinate to use in the quadtree
		long longitude = RecordParser.parseLongitude(lon);
		long latitude = RecordParser.parseLatitude(lat);
		Coordinate coordinate = quadtree.find(new Coordinate(longitude, latitude, -1));
		
		// If a coordinate was found, check if it had multiple offsets corresponding to it
		if (coordinate != null) 
		{
			Vector<Long> resultLongs = new Vector<Long>();
			// Iterate through all offsets adding to the collection
			for (Long offsetLong : coordinate.grabOffsets()) {
				resultLongs.add(offsetLong);
			}
			return resultLongs;
		} 
		else return null;
	}
	
	/**
	 * @param center String representation of center geographic coordinate
	 * @param halfHeight String representation representing the value of half of the height of the rectagular search region (in secs)
	 * @param halfWidth String representation representing the value of half of the width of the rectagular search region (in secs)
	 * @return The collection of offsets for the coordinates found within the input search region
	 */
	public Vector<Long> regionSearch(String center, String halfHeight, String halfWidth)
	{
		// The provied center coordinate point needs to be split and parsed
		String[] coordComponents = center.split(" "); // Lat and Lon are seperated by a space in the command format
		long centerLat = RecordParser.parseLatitude(coordComponents[0]);
		long centerLon = RecordParser.parseLongitude(coordComponents[1]);
		
		// Already provided in seconds in input string, no need to use the record parser
		long regionHalfHeight = Long.parseLong(halfHeight);
		long regionHalfWidth = Long.parseLong(halfWidth);
		
		// Calculate the rectangular region points to produce the required search
		Vector<Coordinate> resultsCoordinates = quadtree.find(centerLon-regionHalfWidth, centerLon+regionHalfWidth, centerLat-regionHalfHeight, centerLat+regionHalfHeight);
		
		Vector<Long> finalOffsets = new Vector<Long>();
		for (Coordinate coordinate : resultsCoordinates) // Iterate through all coordinates, each time collection all their offsets
		{
			// Add all of the available offset from each coordinate
			finalOffsets.addAll(coordinate.grabOffsets());
		}
		
		return finalOffsets;
	}

}
