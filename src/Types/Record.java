package Types;

import java.io.FileWriter;
/*
 * Programmer: Carlos Folgar
 * Project: Major
 * Last modification date: 12/3/2012
 * 
 */
import java.io.IOException;

/**
 * Class: Record
 * The record class is used to encapsulate valuable field component of a Record. Valid field values correspond to
 * values that can be parsed and interpreted from a GIS data records text file.
 */
public class Record 
{
	/*
	 * Required field values for a GIS record according to data from source.
	 */
	private final String featureName;
	private final String fidNumber;
	private final String primaryLatitutde;
	private final String primaryLongitude;
	private final String stateAbbreviation;
	private final String featureClass;
	private final String stateNumericCode;
	private final String countryName;
	private final String countryNumericCode;
	private final String primaryLatitude_dec;
	private final String primaryLongitude_dec;
	private final String sourceLatitude;
	private final String sourceLongitude;
	private final String sourceLatitude_dec;
	private final String sourceLongitude_dec;
	private final String featureElevation_meters;
	private final String featureElevation_feet;
	private final String mapName;
	private final String dateCreated;
	
	// Number of fields in a typical line of a records file
	private final int NUMBER_OF_FIELDS = 19;
	
	
	/**
	 * @param allFields An array containing the string representation of all of the fields inside a record
	 */
	public Record(String[] allFields) {
		
		// Only process if it contains a valid number of fields
		if (allFields.length == NUMBER_OF_FIELDS || allFields.length == NUMBER_OF_FIELDS+1) {
			
			fidNumber 			= allFields[0];
			featureName			= allFields[1];
			featureClass		= allFields[2];
			stateAbbreviation	= allFields[3];
			stateNumericCode	= allFields[4];
			countryName			= allFields[5];
			countryNumericCode 	= allFields[6];
			primaryLatitutde	= allFields[7];
			primaryLongitude	= allFields[8];
			primaryLatitude_dec	= allFields[9];
			primaryLongitude_dec= allFields[10];
			sourceLatitude		= allFields[11];
			sourceLongitude		= allFields[12];
			sourceLatitude_dec	= allFields[13];
			sourceLongitude_dec	= allFields[14];
			featureElevation_meters	= allFields[15];
			featureElevation_feet	= allFields[16];
			mapName				= allFields[17];
			dateCreated			= allFields[18];
		} else {
			fidNumber 			= "";
			featureName			= "";
			featureClass		= "";
			stateAbbreviation	= "";
			stateNumericCode	= "";
			countryName			= "";
			countryNumericCode 	= "";
			primaryLatitutde	= "";
			primaryLongitude	= "";
			primaryLatitude_dec	= "";
			primaryLongitude_dec= "";
			sourceLatitude		= "";
			sourceLongitude		= "";
			sourceLatitude_dec	= "";
			sourceLongitude_dec	= "";
			featureElevation_meters	= "";
			featureElevation_feet	= "";
			mapName				= "";
			dateCreated			= "";
			//dateEdited			= "";			
			return;
		}
	}
	
	
	/*
	 * Converts the record into a string based on the fields that have been set.
	 * This is the typical "long" representation of a record.
	 */
	@Override
	public String toString()
	{
		StringBuilder stringBuilder = new StringBuilder();
		
		// Only output non-empty strings
		if (fidNumber.length() > 0) stringBuilder.append("Feature ID   : " + fidNumber + "\n");
		if (featureName.length() > 0) stringBuilder.append("Feature Name : " + featureName + "\n");
		if (featureClass.length() > 0) stringBuilder.append("Feat. Class  : " + featureClass + "\n");
		if (stateAbbreviation.length() > 0) stringBuilder.append("State        : " + stateAbbreviation + "\n");
		if (countryName.length() > 0) stringBuilder.append("County       : " + countryName + "\n");
		if (primaryLatitutde.length() > 0 ) stringBuilder.append("Latitude     : " + primaryLatitutde + "\n");
		if (primaryLongitude.length() > 0) stringBuilder.append("Longitude    : " + primaryLongitude + "\n");
		if (sourceLongitude.length() > 0) stringBuilder.append("Src Long     : " + sourceLongitude + "\n");
		if (sourceLatitude.length() > 0) stringBuilder.append("Src Lat      : " + sourceLatitude + "\n");
		if (featureElevation_feet.length() > 0) stringBuilder.append("Elev in ft   : " + featureElevation_feet + "\n");
		if (mapName.length() > 0) stringBuilder.append("USGS Quad    : " + mapName + "\n");
		if (dateCreated.length() > 0) stringBuilder.append("Date created : " + dateCreated + "\n");
		return stringBuilder.toString();
	}
	
	/**
	 * @param mode The way that the record should output itself into the stream (how many fields to be represented)
	 * @param fileWriter The writer with an output stream to a log file
	 */
	public void printToFile(String mode, FileWriter fileWriter)
	{
		try 
		{
			if (mode.equalsIgnoreCase("Simple")) // Simple mode (No flag specified)
			{
				fileWriter.write(featureName + "\t" + countryName + "\t" + stateAbbreviation + "\n");
				fileWriter.flush();

			} else if (mode.equalsIgnoreCase("Long")) // Full format mode for -l flag
			{
				fileWriter.write(this.toString());
				fileWriter.flush();
			}
			else if (mode.equalsIgnoreCase("NameAndLocation")) // Format used in region search and other coordinate search commands
			{
				fileWriter.write(countryName + "\t" + primaryLongitude + " " + primaryLatitutde + "\n");
				fileWriter.flush();
			}
			if (mode.equalsIgnoreCase("simpleWithCoords")) // used when no flag presented
			{
				fileWriter.write(featureName + "\t" + stateAbbreviation + "\t" + primaryLatitutde + "\t" + primaryLongitude + "\n");
				fileWriter.flush();

			}
		}
		catch (IOException e) {
			System.err.println("Error: File IO exception. Unable to print the record to file.");
		}
	}
	
	/**
	 * @return The primary latitude of the record in seconds
	 */
	public int getLatitudeSeconds()
	{		
		return getSeconds(this.primaryLatitutde.split(" "), "South");
	}
	
	/**
	 * @return The primary longitude of the record in seconds
	 */
	public int getLongitudeSeconds()
	{		
		return getSeconds(this.primaryLongitude.split(" "), "West");
	}
	
	
	/**
	 * @param components The array which contains the individual Strings for degrees, minutes, and seconds
	 * @param negator The String that if present will negate the value of the total seconds computation
	 * @return The total seconds associated with this primary
	 */
	private int getSeconds(String[] components, String negator)
	{
		int seconds = 0;
		for (int i = 0; i < components.length; i++) {

			if (components[i].endsWith("d")) {
				String degString = components[i].replaceAll("d", "");
				seconds += Integer.parseInt(degString) * 60 * 60;
			}
			else if (components[i].endsWith("m")) {
				String minuteString = components[i].replaceAll("m", "");
				seconds += Integer.parseInt(minuteString) * 60;
			}
			else if (components[i].endsWith("s")) {
				String secondsString = components[i].replaceAll("s", "");
				seconds += Integer.parseInt(secondsString);
			} else if (components[i].startsWith(negator))
				seconds *= -1;
		}
		return seconds;
	}
	
	/*******************************************
	*
	* <b>Function Description:</b> Obtains the feature name of a record<p>
	*
	* <b>Parameter:</b> <p>
	*
	* <b>Pre-conditions:</b> <p>
	* 
	* <b>Post-conditions:</b> <p>
	*
	* <b>Return Value:</b> <p>
	* 
	* <b>Functions called by this function: </b> <p>
	* 
	*******************************************/
	public String getFeatureName()
	{
		return featureName;
	}
	
	
	
	/*******************************************
	*
	* <b>Function Description:</b> Obtains the FID Number of a record<p>
	*
	* <b>Parameter:</b> <p>
	*
	* <b>Pre-conditions:</b> <p>
	* 
	* <b>Post-conditions:</b> <p>
	*
	* <b>Return Value:</b> <p>
	* 
	* <b>Functions called by this function: </b> <p>
	* 
	*******************************************/
	public String getFIDnumber()
	{
		return fidNumber;
	}
	
	
	/*******************************************
	*
	* <b>Function Description:</b> Obtains the primary Latitude of a record<p>
	*
	* <b>Parameter:</b> <p>
	*
	* <b>Pre-conditions:</b> <p>
	* 
	* <b>Post-conditions:</b> <p>
	*
	* <b>Return Value:</b> <p>
	* 
	* <b>Functions called by this function: </b> <p>
	* 
	*******************************************/
	public String getPrimaryLatitude()
	{
		return primaryLatitutde;
	}
	
	
	/*******************************************
	*
	* <b>Function Description:</b> Obtains the primary Longitude of a record<p>
	*
	* <b>Parameter:</b> <p>
	*
	* <b>Pre-conditions:</b> <p>
	* 
	* <b>Post-conditions:</b> <p>
	*
	* <b>Return Value:</b> <p>
	* 
	* <b>Functions called by this function: </b> <p>
	* 
	*******************************************/
	public String getPrimaryLongitude()
	{
		return primaryLongitude;
	}
	
	/**
	 * Class: Builder
	 * In the spirit of the Builder design pattern, the nested builder class serves as the only way to construct a valid record by
	 * allowing for the setting of builder fields which correspond to record fields - but also allowing for exclusion of explicitely
	 * setting the variables by using the default values.
	 * 
	 */	
	public static class Builder 
	{
		/*
		 * Private member variables that correspond to the same components of a Record object.
		 * Note that the empty string correspond to the default value of the field values.
		 */
		private  String featureName = "";
		private  String fidNumber = "";
		private  String primaryLatitutde = "";
		private  String primaryLongitude = "";
		private  String stateAbbreviation = "";
		private  String featureClass = "";
		private  String stateNumericCode = "";
		private  String countryName = "";
		private  String countryNumericCode = "";
		private  String primaryLatitude_dec = "";
		private  String primaryLongitude_dec = "";
		private  String sourceLatitude = "";
		private  String sourceLongitude = "";
		private  String sourceLatitude_dec = "";
		private  String sourceLongitude_dec = "";
		private  String featureElevation_meters = "";
		private  String featureElevation_feet = "";
		private  String mapName = "";
		private  String dateCreated = "";
		
		/*
		 * Setters for the private member variables listed above. Each returns a modified builder object which can then be used
		 * to construct a valid Record.
		 */
		
		public Builder setFeatureName(String name)
		{
			this.featureName = name;
			return this;
		}
		
		public Builder setFidNumber(String fid)
		{
			this.fidNumber = fid;
			return this;
		}
		
		public Builder setPrimaryLatitude(String latitude)
		{
			this.primaryLatitutde = latitude;
			return this;
		}
		
		public Builder setPrimaryLongitude(String longitude)
		{
			this.primaryLongitude = longitude;
			return this;
		}
		
		/*******************************************
		*
		* <b>Function Description:</b> Used to construct a valid Record object from the set values within the builder. In the event not all values have been set, the defaults will be used <p>
		*
		* <b>Parameter:</b> <p>
		*
		* <b>Pre-conditions:</b> A Record.Builder object has been set for the values that are needed <p>
		* 
		* <b>Post-conditions:</b> <p>
		*
		* <b>Return Value:</b> A newly created record object with valid values.<p>
		* 
		* <b>Functions called by this function: </b> <p>
		* 
		*******************************************/
		public Record build()
		{
			String[] buildArr = {fidNumber,
			featureName,
			featureClass,
			stateAbbreviation,
			stateNumericCode,
			countryName,
			countryNumericCode,
			primaryLatitutde,
			primaryLongitude,
			primaryLatitude_dec,
			primaryLongitude_dec,
			sourceLatitude,
			sourceLongitude,
			sourceLatitude_dec,
			sourceLongitude_dec,
			featureElevation_meters,
			featureElevation_feet,
			mapName,
			dateCreated};
			return new Record(buildArr);
		}
				
	}
	
	
}
