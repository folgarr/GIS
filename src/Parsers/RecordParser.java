package Parsers;
/*
 * Programmer: Carlos Folgar
 * Project: Major
 * Last modification date: Sept 7 (9pm)
 * 
 */



import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

import Types.Command;
import Types.Record;

/**
 * Class: RecordParser
 * This class servers to keep a reference to a records file and perform parsing logic to obtain necessary data to form a records object
 */
public class RecordParser 
{
	// Current GIS Records text file that the parser is processing
	private File recordsFile = null;
	// Current command that the parser is acting on
	private Command currentCommand = null;

	// Constact conversion numbers used in the parsing of a geographic coordinate string
	public static final long SECONDS_PER_DEGREE = 60 * 60;
	public static final long SECONDS_PER_MINUTE = 60;
	
	/*******************************************
	*
	* <b>Function Description:</b> Constructor of the records parser when a file reference to a records file is provided <p>
	*
	* <b>Parameter:</b> File recordsTextFile: filepath to records file<p>
	*
	* <b>Pre-conditions:</b> records Text file is already created<p>
	* 
	* <b>Post-conditions:</b> Reference to a records file for the parser is set.<p>
	*
	* <b>Return Value:</b> A newly alllocated records parser object <p>
	* 
	* <b>Functions called by this function: </b> <p>
	* 
	*******************************************/
	public RecordParser(File recordsTextFile)
	{
		if (recordsTextFile != null) recordsFile = recordsTextFile;
	}
	
	
	/*******************************************
	*
	* <b>Function Description:</b> Used to check if the offset specified corresponds to the start of a new data line in the GIS text file.<p>
	*
	* <b>Parameter:</b> long offset: the offset for the file pointer inside of a GIS data records text file.<p>
	*
	* <b>Pre-conditions:</b> records file reference has been made<p>
	* 
	* <b>Post-conditions:</b> <p>
	*
	* <b>Return Value:</b> A string describing the validity of the offset value. <p>
	* 
	* <b>Functions called by this function: </b> <p>
	* 
	*******************************************/
	public String validOffsetMessage(long offset)
	{
		// Unable to proccess negative file pointer offset values
		if (offset <= 0) return "Offset is not positive";
		
		String validity = "";
		try
		{
			// Create a RAF that only allows reading of the file
			RandomAccessFile recordsRAF = new RandomAccessFile(recordsFile, "r");
			if (offset >= recordsRAF.length()) 
			{
				recordsRAF.close();
				return "Offset too large";
			}
			
			recordsRAF.seek(offset - 1); // Move file pointer 1 byte behind to check for a new line terminator
			
			// Read byte, and check if byte corresponds to an ASCII new line terminator (0x0A or 10)
			if (recordsRAF.read() == 10) validity = "valid";
			else validity = "Unaligned offset";
			
			recordsRAF.close();
		}
		catch (FileNotFoundException e) 
		{
			System.err.println("Error: Unable to find a valid GIS records text file to process. Must initialize program with a valid records file.");
		}
		catch (IOException e)
		{
			System.err.println("Error: Unable to succesfully read the GIS Records text file.");
		}
		
		
		return validity;
	}
	
	
	
	/**
	 * @param recordLine Line from the DB file that can be parsed to create a record
	 * @return The constructed record that was represented by the record line
	 */
	public static Record createRecord(String recordLine)
	{
		return new Record(recordLine.split("\\|"));		
	}
	
	/**
	 * @param recordLine Line in a record file that represents the record
	 * @return A concatenation of the feature name and state abbreviate "(Name):(State Abbreviation)"
	 */
	public static String grabNameAndAbbrev(String recordLine)
	{
		String[] parts = recordLine.split("\\|");
		return (parts[1] + ":" + parts[3]);
	}
	
	/**
	 * @param recordLine The line of a records file
	 * @return A string array where the first element is the latitude and the second is longitude
	 */
	public static String[] grabLatAndLon(String recordLine)
	{
		String[] parts = recordLine.split("\\|");
		String[] result = {parts[7], parts[8]};
		return result;
		
	}
	
	
	
	/*******************************************
	*
	* <b>Function Description:</b> String manipulation function used to convert the raw string in the GIS records file to a descriptive value.<p>
	*
	* <b>Parameter:</b> String latitude: raw string corresponding to the field value for latitude in the records file<p>
	*
	* <b>Pre-conditions:</b> None <p>
	* 
	* <b>Post-conditions:</b> <p>
	*
	* <b>Return Value:</b> Seconds value that represents the latitude field <p>
	* 
	* <b>Functions called by this function: </b> <p>
	* 
	*******************************************/
	public static long parseLatitude(String latitude)
	{
		long finalValue = 0;		
		
		if (latitude.charAt(0) == '0') finalValue += SECONDS_PER_DEGREE * Long.parseLong(latitude.substring(1,2));
		finalValue += SECONDS_PER_DEGREE * Long.parseLong(latitude.substring(0,2));
		
		if (latitude.charAt(2) == '0') finalValue += SECONDS_PER_MINUTE * Long.parseLong(latitude.substring(3,4));
		else finalValue += SECONDS_PER_MINUTE * Long.parseLong(latitude.substring(2,4));
		
		if (latitude.charAt(4) == '0') finalValue += Long.parseLong(latitude.substring(5,6));
		else finalValue += Long.parseLong(latitude.substring(4,6));
		
		if (latitude.charAt(6) == 'S') finalValue *= -1;
		return finalValue;
	}
	
	/*******************************************
	*
	* <b>Function Description:</b> String manipulation function used to convert the raw string in the GIS records file to a descriptive value.<p>
	*
	* <b>Parameter:</b> String longitude: raw string corresponding to the field value for longitude in the records file<p>
	*
	* <b>Pre-conditions:</b> None <p>
	* 
	* <b>Post-conditions:</b> <p>
	*
	* <b>Return Value:</b> Seconds value that represents the longitude field <p>
	* 
	* <b>Functions called by this function: </b> <p>
	* 
	*******************************************/
	public static long parseLongitude(String longitude)
	{
		long finalValue = 0;		

		if (longitude.charAt(0) == '0' && longitude.charAt(1) != '0') finalValue += SECONDS_PER_DEGREE * Long.parseLong(longitude.substring(1,3));
		else if (longitude.charAt(0) == '0' && longitude.charAt(1) == '0') finalValue += SECONDS_PER_DEGREE * Long.parseLong(longitude.substring(2,3));
		else finalValue += SECONDS_PER_DEGREE * Long.parseLong(longitude.substring(0,3));
		
		if (longitude.charAt(3) == '0') finalValue += SECONDS_PER_MINUTE * Long.parseLong(longitude.substring(4,5));
		else finalValue += SECONDS_PER_MINUTE * Long.parseLong(longitude.substring(3,5));
		
		if (longitude.charAt(5) == '0') finalValue += Long.parseLong(longitude.substring(6,7));
		else finalValue += Long.parseLong(longitude.substring(5,7));
		
		if (longitude.charAt(7) == 'W') finalValue *= -1;
		return finalValue;
	}
	
	
}
