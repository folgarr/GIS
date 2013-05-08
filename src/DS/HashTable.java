package DS;
/*
 * Programmer: Carlos Folgar
 * Project: Major
 * Last modification date: 12/3/2012
 * 
 */

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;


/**
 * @author Carlos Folgar
 *
 * @param <K> The generic key type (most often strings) for the hash table
 * @param <V> The values stored at the hashed-to location
 * 
 * The purpose of this class is store generic key-value pairs that allow for
 * quick retrieval through the use of a key
 * 
 */
public class HashTable <K, V> {

	/* Private member variables */
	
	/**
	 * Array of table sizes numbers (all primes - for quadratic probing) used when the table needs to resize itself
	 */
	private int[] tableSizes = {1019,2027,4079,8123, 16267,32503,65011,130027,260111,520279,1040387,2080763,4161539,8323151,16646323};
	/**
	 * Index in the tableSizes array at which the maximum size of the hashtable can be found
	 */
	private final int MAX_SIZE_INDEX = 14;
	/**
	 * Current table size index
	 */
	private int currentTableSizeIndex = 0;
	
	/**
	 * Enum used to describe the state of a specific slot
	 */
	private enum SlotCondition {TOMB, FULL};
	
	/**
	 * The number of entries that can be placed inside this table. Initially 1019.
	 */
	private int size = 1019;
	
	/**
	 * Count variable to keep track of the number of elements in the table
	 */
	private int count = 0;
	
	/**
	 * The conditions of slots inside the table
	 */
	private SlotCondition[] slotConditions;
	
	/**
	 * The set of entries, each a key-value pair, that are stored within the table.
	 */
	private KeyVal[] table;
	
	
	/**
	 * Floating value that represents the point at which the HashTable realizes it must double
	 * its size and fully rehash entries.
	 */
	private float loadFactor = .70f;	
	
	/* Nested Class: KeyVal */
	/**
	 * The KeyVal class represents objects that are the entries in the hashtable.  Its function
	 * is to store a key-value (String and a Long respectively) pair during table insertions.
	 */
	private class KeyVal 
	{		
		/**
		 * A key value (String) that can be hashed in order to be inserted into a table.
		 */
		private K key;
		
		/**
		 * A hash-table value (Long) that is found at an index equivalent to its corresponding hashed key value.
		 */
		private V value;
		
		/**
		 * A collection of file offset values (for the DB file) for entries with the same key.
		 */
		private Vector<V> recordOffsets;
		
		/**
		 * A count of the number of duplicate keys in the hash-table where this key-value pair resides.
		 */
		private int duplicates;
		
		/**
		 * Constructor for the KeyVal class that stores a key and a value in order to store into a table.
		 * 
		 * @param keyString String representation of the key for this entry
		 * @param valueLong Value of at the location found by hashing the key
		 */
		public KeyVal(K keyString, V valueLong) 
		{
			this.key = keyString;
			this.value = valueLong;
			this.duplicates = 0;
			recordOffsets = new Vector<V>();
			recordOffsets.add(valueLong); // Update our offset collection
		}
		
		/**
		 * @return String representation of the key-value pair entry
		 */
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("[ " + this.key + ", [ ");
			for ( V val : recordOffsets) {
				builder.append(val.toString() + " ");
			}
			builder.append("]]");
			return builder.toString();
		}
	}
	
	
	/**
	 * Default constructor for the HashTabl. Initializes a table to a size 1019
	 * and also creates a corresponding condition table to keep track of slot conditions
	 */
	@SuppressWarnings("unchecked")
	public HashTable() 
	{
		slotConditions = new SlotCondition[tableSizes[currentTableSizeIndex]]; 	// Initially all empty
		table = new HashTable.KeyVal[tableSizes[currentTableSizeIndex]];		// Initially all full
	}
	
	/**
	 * @return The current size of the hash table.
	 */
	public int getSize() {
		return size = tableSizes[currentTableSizeIndex];
	}
	
	/**
	 * Updates the size of the table to align with one of the prime sizes that are available
	 */
	public void updateSize()
	{
		size = tableSizes[currentTableSizeIndex];
	}
	
	
	/**
	 * @param keyString Key string that will be hashed into the table
	 * @param value Value of type long that will be inserted into the table
	 * @return Success value of the insertion operation
	 */
	public int insert(K keyString, V value)
	{
		if (keyString == null) throw new NullPointerException("Error: Provided key for Hash-Table is null.");
		return insertHelper(new KeyVal(keyString, value));
	}
	
	/**
	 * @param entry Key-Value entry object to be inserted into the hash table
	 * @return Longest probe sequence required in the insertion into the hash table
	 */
	private int insertHelper(KeyVal entry) {
		
		if (entry == null) // Avoid invalid entry insertions
			return -1;
		
		// Obtain the home slot by hashing the key
		int homeSlotIndex = elfHash(entry.key) % table.length;

		// Keeps track of next index as calculated by probing
		int nextLocation; 
		int i = 0;
		while (true)
		{
			// Calculate step size with quadratic probing. Obtain the location of next index.
			int stepSize = (i*i + i)/2;
			nextLocation =  (homeSlotIndex + stepSize) % table.length;
			
			// Watch for overflow when step size how 
			if (stepSize < 0) {
				System.out.println("Error in the hashtable when probing for an available slot. (overflow)");
				System.exit(-1);
			}								
			
			// Attempt to see if the entry's key has been previuosly hashed and we just need to add to collection of offsets
			if (slotConditions[nextLocation] == SlotCondition.FULL && ((String) table[nextLocation].key).equalsIgnoreCase((String) entry.key)) 
			{
				table[nextLocation].recordOffsets.add(entry.value);
				count++;
				resizeIfNeeded();
				return i;
			}
			// Check if slot is available for insertion
			else if (slotConditions[nextLocation] != SlotCondition.FULL || table[nextLocation] == null) {
				table[nextLocation] = entry; // insert
				slotConditions[nextLocation] = SlotCondition.FULL;				
				count++;
				resizeIfNeeded();
				return i;
			}
			i++;
		}
		
	}
	
	/**
	 * @param keyString String key value for which we will search duplicates for
	 * @return Vector of values found matching the hashed key string provided
	 */
	public Vector<V> findEntriesWithKey(K keyString)
	{
		// Store the results of the multi-key search
		Vector<V> results = new Vector<V>();
		
		// Obtain the home slot by hashing the key
		int homeSlotIndex = elfHash(keyString) % table.length;		

		// Traverse the table looking for the specified key
		int nextLocation;
		for (int i = 0; i < table.length; i++)
		{
			// Calculate step size with quadratic probing. Obtain the location of next index.
			int stepSize = (i*i + i)/2;
			
			if(stepSize < 0 ) return results; // Overflow watch
			nextLocation =  (homeSlotIndex + stepSize) % table.length;
			
			// Attempt to see if the entry's key has been previuosly hashed and we just need to add to collection of offsets
			if (slotConditions[nextLocation] == SlotCondition.FULL && ((String) table[nextLocation].key).equalsIgnoreCase((String) keyString)) 
			{
				results = table[nextLocation].recordOffsets;
				return results;
			}
		}
		return results; // Return an empty vector if nothing found
	}
	
	
	/**
	 * @param key Key that will be hashed to see how many entries corresponding to it are in the table
	 * @return The number of entries with the key provided
	 */
	public int numberOfKeyOccurences(K key)
	{
		// Obtain the home slot by hashing the key
		int homeSlotIndex = elfHash(key) % table.length;		

		// If we find something at the home-slot, check if there are any duplicates
		if (slotConditions[homeSlotIndex] == SlotCondition.FULL) 		
			return table[homeSlotIndex].recordOffsets.size(); // Return the number of occurences
		 else 
			return 0;		
	}
	
	/**
	 * @param key Key that will be hashed and checked for in the table to find occurences, each time printings the value found
	 */
	public void printAllOffsetsForKey(K key) {		
		Vector<V> searchResults = findEntriesWithKey(key);
		if (searchResults != null) {
			for (V offsetLong: searchResults) {
				System.out.println("Value: " + offsetLong);
			}
		}
	}
	
	
	/**
	 * Used by the table to check if the table size needs to be increased according to load factor
	 */
	private void resizeIfNeeded() 
	{
		if (this.count >= loadFactor * table.length)
			resize();		
	}
	
	/**
	 * Used by the table to resize the size of the hash table and rehash all the needed values
	 */
	@SuppressWarnings("unchecked")
	private void resize() 
	{
		// Store previous entries and increase the table size to next prime number
		KeyVal[] prevEntries = table;
		currentTableSizeIndex++;
		
		// Check if the size is too large
		if (currentTableSizeIndex <= MAX_SIZE_INDEX) 
		{
			// Create new containers for conditions and key-vals
			updateSize();
			slotConditions = new SlotCondition[size];
			table = new HashTable.KeyVal[size];
			this.count = 0;
			
			// Place back all of the entries into the table
			for (KeyVal entry: prevEntries) {
				if (entry != null) 
					insertHelper(entry);
			}			
		} 
		else
		{ // Attempting to make large table
			System.err.println("Error: Terminating program to prevent memory problems (Hash table size has exceeded its limit of 16,646,323)");
			System.exit(0);
		}			
	}
	
	
	
	/**
	 * @param toHash The string object that will be manipulated to obtain a hash value
	 * @return The hash value that resulted from various operations on the string
	 */
	public int elfHash(K toHash) {
		int hashValue = 0;
		for (int Pos = 0; Pos < ((String) toHash).length(); Pos++) {      	// use all elements
			hashValue = (hashValue << 4) + ((String) toHash).charAt(Pos);  // shift/mix
			int hiBits = hashValue & 0xF0000000;                // get high nybble
			if (hiBits != 0)
				hashValue ^= hiBits >> 24;    					// xor high nybble with second nybble
			hashValue &= ~hiBits;            					// clear high nybble
		}
		return hashValue;
	}
	
	
	/**
	 * @param fileWriter Output stream to a file that we will write the hash table representation to
	 */
	public void printToFile(FileWriter fileWriter)
	{
		// Get the cur index
		int curIndex = 0;

		try {
			// Output header line
			fileWriter.write("Format of display is\nSlot number: data record\nCurrent table size is " + table.length + "\nNumber of elements in table is " + count + "\n\n");
			for (KeyVal keyval: table)
			{
				if (keyval != null) // Write the values for this entry
					fileWriter.write(curIndex + ":\t" + keyval + "\n");
				curIndex++;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* 
	 * String representation of the table, prints out all key-value pairs
	 */
	@Override
	public String toString()
	{
		StringBuilder stringBuilder = new StringBuilder();
		int curIndex = 0;
		for (KeyVal keyval: table)
		{
			if (keyval != null)
				stringBuilder.append(curIndex + ":\t" + keyval + "\n");
			curIndex++;
		}
		stringBuilder.insert(0,"Format of display is\nSlot number: data record\nCurrent table size is " + table.length + "\nNumber of elements in table is " + count + "\n\n");
		return stringBuilder.toString();
	}
	
}
