/*
 * Programmer: Carlos Folgar
 * Project: Major
 * Last modification date: 12/3/2012
 * 
 */


package Indexes;
import java.io.FileWriter;
import java.util.Vector;

import DS.HashTable;
import Parsers.RecordParser;

/**
 * @author Carlos Folgar
 * 
 * The purpose of this class is to keep an index of the feature name and state of records in a database file
 * along with their file offsets.  The name index makes us a hashtable to store the entries as strings
 * in the format Key: (FeatureName):(StateAbbreviation) Value: File offset.  It allows for fast lookup
 * once the entries have been hashed into the table.
 *
 */
public class NameIndex {

	/**
	 * Internal data structure to keep track of the key value pairs
	 */
	private HashTable<String, Long> table;
	
	/**
	 * Keeps track of the longest probe sequence that has been required in the import of data
	 */
	public int longestProbeSequence;
	
	/**
	 * Keeps track of how many entries have been added to name index
	 */
	public int importedFeaturesByName = 0;
	
	/**
	 * Constructs a name index with a hashtable storing keys as string and values as their offset in a file (Long)
	 */
	public NameIndex()
	{
		table = new HashTable<String, Long>();
		longestProbeSequence = 0;
	}
	
	/**
	 * @param writer The output file write which will get the output representation of our HashTable
	 */
	public void printToFile(FileWriter writer)
	{
		table.printToFile(writer);
	}
	
	
	/**
	 * @param recordLine Line inside of a records file that represents a record to be hashed into table
	 * @param offset Offset inside of a records file at which we can find the provided record string
	 */
	public void add(String recordLine, Long offset)
	{
		String keyString = RecordParser.grabNameAndAbbrev(recordLine);
		int probeSeq = table.insert(keyString, offset);
		if (probeSeq > longestProbeSequence) longestProbeSequence = probeSeq;
		importedFeaturesByName++;
	}
	
	/**
	 * @param featNameAndAbbrev String concatenation of the feature name and State Abbreviate - this is the key for the entry
	 * @return A collection of values (type Long) representing file offsets at which the records may be found in the DB
	 */
	public Vector<Long> search(String featNameAndAbbrev)
	{
		return table.findEntriesWithKey(featNameAndAbbrev);
	}
	
	/**
	 * @param featNameAndAbbrev String concatenation of the feature name and State Abbreviate - this is the key for the entry
	 * @return The number of entries with the same key in our hash table 
	 */
	public int numberOfOccurences(String featNameAndAbbrev)
	{
		return table.numberOfKeyOccurences(featNameAndAbbrev);
	}
	
}
