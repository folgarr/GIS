/*
 * Programmer: Carlos Folgar
 * Project: Major
 * Last modification date: 12/3/2012
 */
package DS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import Types.Record;

/**
 * @author Carlos Folgar
 * The purpose of this class is to buffer the records that are retrieved from the database file.
 * It can retrieve records if it has them contained.
 */
public class BufferPool 
{

	/** List of strings corresponding to records that will be cached */	
	private ArrayList<String> stringList;
	/** The database file that contains all of the imported records */
	private File dbFile;
	/** The maximum amount of record strings that may be stored. Afterwards, the least recently used will be replaced */
	private final int POOL_CAPACITY = 20; 
	
	/**
	 * Constructor for the buffer pool class. Constructs a normal buffer pool with capacity 20.
	 */
	public BufferPool(File databaseFile)
	{
		stringList = new ArrayList<String>();
		dbFile = databaseFile;
	}

	/**
	 * @param recordString The string representing a raw record that will be added to the buffer
	 * @return A boolean value indicating the success of adding to the buffer<p>
	 * 
	 * <b>Format of string:</b> (offset):(tab)(record string)
	 */
	public boolean addRecordString(String recordString)
	{
		// Only add if the string conforms to the specified format
		if (recordString.contains(":\t"))
		{
			if (stringList.size() < POOL_CAPACITY) {
				stringList.add(0,recordString);
				return true;
			} else 
			{ // Need to remove LRU string
				stringList.remove(POOL_CAPACITY - 1);
				stringList.add(0,recordString);
				return true;
			}
		}
		else {
			System.err.println("Error: Format of string to buffer is invalid.");
			return false;
		}
	}

	/**
	 * @param desiredOffset Offset value in the database file where the desired record is found
	 * @return The desired record at the provided offset or null if the offset value wasnt found
	 */
	public Record grabRecordWithOffset(String desiredOffset) 
	{
		if (stringList != null && stringList.size() > 0)
		{
			for (int i = 0; i < stringList.size(); i++)
			{
				String[] partsStrings = stringList.get(i).split(":\t");
				if (partsStrings[0].equals(desiredOffset)) 
				{
					// Move the matching string to the front
					String orig = stringList.get(i);
					stringList.remove(i);
					stringList.add(0,orig);

					// Return the record corresponding to the buffered record string
					return new Record(partsStrings[1].split("\\|"));
				}
			}
		}
		return getFromDB_at(Long.parseLong(desiredOffset));
	}

	/**
	 * @param offset Offset at which to find the record line in the Database file
	 * @return The record found in the Database or null if none were found
	 */
	private Record getFromDB_at(Long offset)
	{
		try 
		{
			// Create a RAF that only allows reading of the file
			RandomAccessFile recordRAF = new RandomAccessFile(dbFile, "r");

			// Move the file pointer to the start of the record line
			recordRAF.seek(offset);
			String readLine = recordRAF.readLine();
			
			// Buffer the string in case we need to use again
			addRecordString(offset.toString() + ":\t" + readLine);
			
			// Construct a new record from this string
			Record record = new Record(readLine.split("\\|"));
			
			recordRAF.close();
			return record;
			
		} catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Prints the contents (buffered elements) of the pool
	 */
	public void printPool() 
	{
		for (String elem : stringList)
		{
			System.out.println(elem + "\n");			
		}
	}

	/**
	 * @param writer The writer with a reference to the file that will receive the output of the pool
	 */
	public void printToFile(FileWriter writer)
	{
		try {
			
			writer.write("MRU\n"); // Header information for print out
			// Iterate over all the buffered strings
			for (String elem : stringList)
			{
				writer.write(elem + "\n");
				writer.flush();
			}
			writer.write("LRU\n");
			writer.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
		}	
	}

}
