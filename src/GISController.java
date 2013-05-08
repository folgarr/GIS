/*
 * Programmer: Carlos Folgar
 * Project: Major
 * Last modification date: 12/3/2012
 * 
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import DS.BufferPool;
import Indexes.CoordinateIndex;
import Indexes.NameIndex;
import Parsers.CommandParser;
import Parsers.RecordParser;
import Types.Command;
import Types.Record;

/** 
 * Class: GISController
 * 
 * Purpose and Description:
 * The purpose of the GISController is mainly to serve as an arbiter for the parsers for search/retrieval/processing of data records.
 * It is also used in the reporting of the output associated with specific commands that act on the GIS Record.
 */
public class GISController {

	
	/* Private member variables */
	
	// File object associated with the GIS data records file that contains the geographical data
	private File recordsFile;
	// File object associated with the commands text file containing the search and report instructions that act on the data record input
	private File commandsFile;
	// File object associated with the text file that outputs the search results from the commands that acted on the GIS data records.
	private File resultsFile;
	// File object associated with the database file of Records
	private File databaseFile;
	// File object associated with log file in which script actions/results are written
	private File logFile;
	// The part of thee system that uses a hashtable to store the name and offset values
	private NameIndex nameIndex;
	// The part of the system that uses a quadtree to store regional data along with their offsets
	private CoordinateIndex coordinateIndex;
	// The part of the system that buffer recently used record elements (as strings)
	private BufferPool bufferPool;
	// File writer that outputs to the log file
	private FileWriter writer;
	// String used to seperate output inside of the log file
	private final String seperatorStr = "--------------------------------------------------------------------------------\n";
	// Running file offset withing the database file (helps for multiple import situations)
	private long runningOffset = 0;
	// Offset where last import left off
	private long lastOffset = 0;
	
	// Primary FileWriter objects that serves as an output stream that updates our database file
	private FileWriter dbWriter;

	// Command and Record parsers objects used to retrieve records and command objects
	private CommandParser commandParser = null;
	private RecordParser recordParser = null;


	/*******************************************
	 *
	 * <b>Function Description:</b> GISController constructor that is only called to create a base controller object with no file references.<p>
	 *
	 * <b>Parameter:</b> None <p>
	 *
	 * <b>Pre-conditions:</b> None  <p>
	 * 
	 * <b>Post-conditions:</b> None  <p>
	 *
	 * <b>Return Value:</b> None  <p>
	 * 
	 * <b>Functions called by this function: </b> None  <p>
	 * 
	 *******************************************/
	public GISController()
	{
		// Default constructor
		recordsFile = null;
		commandsFile = null;
		resultsFile = null;
	}



	/*******************************************
	 *
	 * <b>Function Description:</b> Main constructor for the GISController class. Used when a DB file path and log file path are known. <p>
	 *
	 * <b>Parameter:</b> String dbFileName: File path of the GIS Records DB file.<p>
	 * <b>Parameter:</b> String logFileName: File path of the log file. <p>
	 * 
	 * <b>Pre-conditions:</b> None <p>
	 * 
	 * <b>Post-conditions:</b> None <p>
	 *
	 * <b>Return Value:</b> A newly created GISController object to handle the data processing with help of different parsers/loggers/processors <p>
	 * 
	 *******************************************/
	public GISController(String dbFileName, String logFileName, CommandParser parser) 
	{
		try
		{
			// Create the file references to the files
			databaseFile = new File(dbFileName);
			logFile = new File(logFileName);

			if (!databaseFile.createNewFile()) // Check if the creation of a db file failed
			{
				// Attempt to delete the pre-existing file that cause the file creation to fail
				databaseFile.delete();
				
				// Create the new db file now that the pre-existing file has been deleted
				if (!databaseFile.createNewFile()) {
					System.err.println("Error: Unable to create the database file \"" + dbFileName + "\" in the current directory.");
					System.exit(-1);
				}
			}

			// Create the buffer pool object
			bufferPool = new BufferPool(databaseFile);

			if (!logFile.createNewFile()) // Check if the creation of a db file failed
			{
				// Attempt to delete the pre-existing file that cause the file creation to fail
				logFile.delete();
				
				// Create the new db file now that the pre-existing file has been deleted
				if (!logFile.createNewFile()) {
					System.err.println("Error: Unable to create the log file \"" + logFileName + "\" in the current directory.");
					System.exit(-1);
				}
			}

			// Create the fileWriter object
			writer = new FileWriter(logFile);
		}
		catch (IOException e) // Fail if we cant make these files (because they drive everything else - key components)
		{
			System.err.println("Error: Creation of DB and Log files failed. File input/output error: " + e);
			System.exit(-1);
		}
		
		// Set the command parser for the controller
		if (parser != null) commandParser = parser;
		else throw new NullPointerException("Error: Invalid command parser passed to the GIS Controller.");
		
		// Create the name index object to store names and offset values
		nameIndex = new NameIndex();		
	}



	/**
	 * Main proccess command used to obtain a command and call another system
	 * component to act upon the type of command and data associated with it.	 
	 * 
	 * @return A boolean indicating whether or not we are done processing script commands
	 */
	public boolean processNextCommand()
	{
		try {
			if (commandParser != null) 
			{
				// Grab the next available command from the parser
				Command command = this.commandParser.nextCommand();
				if (command != null && !command.getCommandType().equalsIgnoreCase("Invalid") && !command.getCommandType().equalsIgnoreCase("comment")) 
				{
					// Write the command message and fail so as to terminate execution in the main loop
					if (command.getCommandType().equalsIgnoreCase("Quit"))
					{
						writer.write("Command:\t" + command.getRawCommandString() + "\n\nTerminating execution of commands.\n" + seperatorStr);
						writer.flush();
						return false;
					}
					// First real command encountered, set up the coordinate index by parsing the boundaries
					else if (command.getCommandType().equalsIgnoreCase("World"))
					{
						String[] bounds = command.getWorldBoundaries();
						coordinateIndex = new CoordinateIndex(bounds[0], bounds[1], bounds[2], bounds[3]);

						// Show the command just read
						writer.write("Command:\t" + command.getRawCommandString() + "\n\n");
						writer.flush();

						// Write header information
						writer.write("GIS Program by Carlos Folgar\n\ndbFile:\t" + databaseFile.toString() + "\nScript:\t"
								+ commandParser.getCommandsFile().toString() + "\nlog:\t" + logFile.toString() + "\nQuadtree children order: SW SE NE NW\n\n");

						// Write world boundaries
						writer.write("World Boundaries are set to:\n\t\t" + coordinateIndex.getNorthBoundary() + "\n" + coordinateIndex.getWestBoundary() + "\t\t" 
								+ coordinateIndex.getEastBoundary() + "\n\t\t" + coordinateIndex.getSouthBoundary() + "\n" + seperatorStr);

						writer.flush();
						return true;
					}
					// Import a new records file
					else if (command.getCommandType().equalsIgnoreCase("Import"))
					{
						// Keep track of how many things are imported on this run
						nameIndex.importedFeaturesByName = 0;
						coordinateIndex.importedFeaturesByLocation = 0;
						
						// Show the command just read
						writer.write("Command:\t" + command.getRawCommandString() + "\n\n");
						writer.flush();

						// Create a RAF for the records file to be imported
						File recordFile = new File(command.getRecordFileName());
						RandomAccessFile recordRAF = new RandomAccessFile(recordFile, "r");
						
						// Only create a new writer if needed - dont want to lose our file pointer
						if (dbWriter == null) dbWriter = new FileWriter(databaseFile);
						long tempOffset = 0;
						
						// Only place header on the file once
						if (lastOffset == 0) {
							dbWriter.write(recordRAF.readLine() + "\n"); // Take care of first header line
							dbWriter.flush();
						}
						else {
							recordRAF.readLine();
							tempOffset = recordRAF.getFilePointer();							
						}
						
						// Update the offset differently based on how many imports we have performed
						if (lastOffset == 0) runningOffset = recordRAF.getFilePointer();
						else {
							runningOffset = 0;
						}
						String recStr = recordRAF.readLine();

						while (recStr != null)
						{
							nameIndex.add(recStr, runningOffset + lastOffset); 			// Import into the name index
							coordinateIndex.add(recStr, runningOffset + lastOffset); 	// Import into the coordinate index
							dbWriter.write(recStr+"\n");								// Update the db with most recent record

							runningOffset = recordRAF.getFilePointer() - tempOffset; 	// Update offset and the current record string
							recStr = recordRAF.readLine();
							dbWriter.flush();
						}
						// Update the last offset. Allows us to write in the correct location on next import
						lastOffset = runningOffset;
						recordRAF.close();
						// Write the results of the imports
						writer.write("Imported Features by name: " + nameIndex.importedFeaturesByName + "\n");
						writer.write("Longest probe sequence: " + nameIndex.longestProbeSequence + "\n");
						writer.write("Imported Features by location: " + coordinateIndex.importedFeaturesByLocation + "\n" + seperatorStr);
						writer.flush();
						return true;
					}
					else if (command.getCommandType().equalsIgnoreCase("debug"))
					{
						// Show the command just read
						writer.write("Command:\t" + command.getRawCommandString() + "\n\n");
						writer.flush();

						// Call file prints for each internal index system or pool
						if (command.getDebugTarget().equalsIgnoreCase("hash")) nameIndex.printToFile(writer);						
						else if (command.getDebugTarget().equalsIgnoreCase("quad")) coordinateIndex.printToFile(writer);						
						else if (command.getDebugTarget().equalsIgnoreCase("pool")) bufferPool.printToFile(writer);						
						else System.err.println("Error: Unable to debug the specified parts of the system.");	
						writer.write(seperatorStr);
						writer.flush();
						return true;
					}
					else if (command.getCommandType().equalsIgnoreCase("toNameIndex")) return proccessNameCommand(command);
					else if (command.getCommandType().equalsIgnoreCase("toCoordIndex")) return proccessLocateCommand(command);
					else return true;
				}
				else if (command.getCommandType().equalsIgnoreCase("Comment")) // Just output the raw command encountered
				{
					writer.write(command.getRawCommandString() + "\n");
					writer.flush();
					return true;
				}
				else 
					return false; // Invalid - fail to stop execution 
			} else
				return false; // Fail if no valid parsers
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @param command The command object that encapsulates the type of action that needs to be taken
	 * @return A boolean indicating the success value of the command action
	 * @throws IOException
	 */
	private boolean proccessNameCommand(Command command) throws IOException
	{
		// Print the command
		writer.write("Command:\t" + command.getRawCommandString() + "\n\n");
		writer.flush();

		// Obtain the search results from the index
		Vector<Long> results = nameIndex.search(command.getFeatureName() + ":" + command.getStateAbbrev());
		if (results != null && results.size() > 0) // Only process container of results
		{
			if (command.getPrintFlag().equalsIgnoreCase("-c")) { // Process count
				writer.write("The number of records found for " + command.getFeatureName() 
						+ " and " + command.getStateAbbrev() + " was " + results.size() + "\n" + seperatorStr );
				writer.flush();
				return true;
			}
			Record tempRecord;
			for (Long offset : results) // iterate over the results from the search
			{
				// Grab from the buffer if possible
				tempRecord = bufferPool.grabRecordWithOffset(offset.toString());
				
				// Print the record according to the print flag
				if (tempRecord != null) 
				{
					if (command.getPrintFlag().equalsIgnoreCase("-l")) {
						writer.write("Found matching record at offset " + offset + ":\n\n");
						tempRecord.printToFile("Long", writer);
					}
					else {
						writer.write(offset.toString() + ":\t");
						tempRecord.printToFile("NameAndLocation", writer);
					}
				}
			}
			
			// Print the seperator string
			writer.write(seperatorStr);
			writer.flush();
			return true;
		}
		else { // No records found
			writer.write("Nothing found!\n"+seperatorStr);
			writer.flush();
			return true;
		}
	}

	/**
	 * @param command The command object that encapsulates the type of coordinate serach action that needs to be taken
	 * @return A boolean indicating the success value of the command action
	 * @throws IOException
	 */
	private boolean proccessLocateCommand(Command command) throws IOException
	{
		writer.write("Command:\t" + command.getRawCommandString() + "\n\n");
		writer.flush();
		Vector<Long> results = new Vector<Long>();
		
		// Store if its a region search or not
		boolean isRegionSearch= !command.getHalfHeight().equalsIgnoreCase("");
		
		// Indicates that it is not a region search
		if (!isRegionSearch) results = coordinateIndex.searchWithCoord(command.getCoordinateString());
		else results = coordinateIndex.regionSearch(command.getCoordinateString(), command.getHalfHeight(), command.getHalfWidth());
		
		if (results != null && results.size() > 0)
		{
			// Check if only being asked for the count
			if (command.getPrintFlag().equalsIgnoreCase("-c")) {
				if (!isRegionSearch)
					writer.write("The number of records found for " + command.getCoordinateString() 
							+ " was " + results.size() + "\n" + seperatorStr );
				else 
					writer.write(results.size() + " features were found in " + command.getCoordinateString() 
							+ " +/- " +	command.getHalfHeight() + " Height and +/- "+ command.getHalfWidth() + " Width\n" + seperatorStr );
				writer.flush();
				return true;
			}

			// Print header information
			if (isRegionSearch)
			{
				writer.write("The following " + results.size() + " features were found in (" + command.getCoordinateString() + " +/- " +
						command.getHalfHeight() + "  Height and +/- "+ command.getHalfWidth() + " Width) \n");
			} else writer.write("The following " + results.size() + " features were found in (" + command.getCoordinateString() + ")\n");
			writer.flush();

			Record tempRecord;
			
			// Iterate over all of the results and print them accordinly
			for (Long offset : results)
			{
				if (offset.longValue() == (long)103165) offset.longValue();
				
				// Attempt to grab the record from the pool
				tempRecord = bufferPool.grabRecordWithOffset(offset.toString());
				
				// Only process valid records
				if (tempRecord != null) 
				{
					if (command.getPrintFlag().equalsIgnoreCase("-l")) { // Long print format
						// Print record in Long format
						tempRecord.printToFile("Long", writer);
						if (isRegionSearch) {
							writer.write("\n");
							writer.flush();
						}
					}
					else {												// Normal print format
						writer.write(offset.toString() + ":\t");
						if (isRegionSearch) tempRecord.printToFile("SimpleWithCoords", writer);
						else tempRecord.printToFile("Simple", writer);						
					}
					
					// Flush write buffer to file output stream
					writer.flush();
				}
			}
			// Formatting: L:ine that seperates the output
			writer.write(seperatorStr);
			writer.flush();
			return true;
		}
		else { // Zero items found from search
			writer.write("Nothing found!\n"+seperatorStr);
			writer.flush();
			return true;
		}
	}


	/*******************************************
	 *
	 * <b>Function Description:</b> Serves to return the current command parser of a controller <p>
	 *
	 * <b>Parameter:</b> <p>
	 *
	 * <b>Pre-conditions:</b> Command parser must have been initialized <p>
	 * 
	 * <b>Post-conditions:</b> <p>
	 *
	 * <b>Return Value:</b>  The controllers associated command parser <p>
	 * 
	 * <b>Functions called by this function: </b> <p>
	 * 
	 *******************************************/
	public CommandParser getCommandParser()
	{
		return this.commandParser;
	}

	/*******************************************
	 *
	 * <b>Function Description:</b> Serves to return the current command parser of a controller <p>
	 *
	 * <b>Parameter:</b> <p>
	 *
	 * <b>Pre-conditions:</b> Command parser must have been initialized<p>
	 * 
	 * <b>Post-conditions:</b> <p>
	 *
	 * <b>Return Value:</b> The controllers associated record parser <p>
	 * 
	 * <b>Functions called by this function: </b> <p>
	 * 
	 *******************************************/
	public RecordParser getRecordParser()
	{
		return this.recordParser;
	}
	
}
