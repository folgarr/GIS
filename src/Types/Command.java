package Types;
/*
 * Programmer: Carlos Folgar
 * Project: Major
 * Last modification date: 12/3/2012
 * 
 */

/** 
 * Class: Command
 * 
 * Purpose and Description:
 * The purpose of the Command class is to encapsulate the basic component of a command: the search message and the offset (if available).
 * The command class is continually used in the creation of command objects that are created by a command parser.
 * 
 */
public class Command {

	// Message component of a command. This message can be "report" or "quit"
	private String commandMessageString = "";
	// Component of a search command that specified the offset at which to find a specific data record in a text file.
	private long recordOffset = 0;
	
	/* Thep different properties that may be taken by a particular command object. */
	private String commandType = "";
	private String[] worldBoundaries = null;
	private String recordFileName = "";
	private String coordinateString = "";
	private String printFlag = "";
	private String featureName = "";
	private String stateAbbrev = "";
	private String halfHeight = "";
	private String halfWidth = "";
	private String debugTarget = "";
	private String rawCommandString = "";
	
	/*
	 * Getters and Setters for private member variables below
	 */
	
	/**
	 * @return the rawCommandString
	 */
	public String getRawCommandString() {
		return rawCommandString;
	}


	/**
	 * @param rawCommandString the rawCommandString to set
	 */
	public void setRawCommandString(String rawCommandString) {
		this.rawCommandString = rawCommandString;
	}


	/**
	 * @return the recordOffset
	 */
	public long getRecordOffset() {
		return recordOffset;
	}


	/**
	 * @param recordOffset the recordOffset to set
	 */
	public void setRecordOffset(long recordOffset) {
		this.recordOffset = recordOffset;
	}


	/**
	 * @return the commandType
	 */
	public String getCommandType() {
		return commandType;
	}


	/**
	 * @param commandType the commandType to set
	 */
	public void setCommandType(String commandType) {
		this.commandType = commandType;
	}


	/**
	 * @return the worldBoundaries
	 */
	public String[] getWorldBoundaries() {
		return worldBoundaries;
	}


	/**
	 * @param worldBoundaries the worldBoundaries to set
	 */
	public void setWorldBoundaries(String[] worldBoundaries) {
		this.worldBoundaries = worldBoundaries;
	}


	/**
	 * @return the recordFileName
	 */
	public String getRecordFileName() {
		return recordFileName;
	}


	/**
	 * @param recordFileName the recordFileName to set
	 */
	public void setRecordFileName(String recordFileName) {
		this.recordFileName = recordFileName;
	}


	/**
	 * @return the coordinateString
	 */
	public String getCoordinateString() {
		return coordinateString;
	}


	/**
	 * @param coordinateString the coordinateString to set
	 */
	public void setCoordinateString(String coordinateString) {
		this.coordinateString = coordinateString;
	}


	/**
	 * @return the printFlag
	 */
	public String getPrintFlag() {
		return printFlag;
	}


	/**
	 * @param printFlag the printFlag to set
	 */
	public void setPrintFlag(String printFlag) {
		this.printFlag = printFlag;
	}


	/**
	 * @return the featureName
	 */
	public String getFeatureName() {
		return featureName;
	}


	/**
	 * @param featureName the featureName to set
	 */
	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}


	/**
	 * @return the stateAbbrev
	 */
	public String getStateAbbrev() {
		return stateAbbrev;
	}


	/**
	 * @param stateAbbrev the stateAbbrev to set
	 */
	public void setStateAbbrev(String stateAbbrev) {
		this.stateAbbrev = stateAbbrev;
	}


	/**
	 * @return the halfHeight
	 */
	public String getHalfHeight() {
		return halfHeight;
	}


	/**
	 * @param halfHeight the halfHeight to set
	 */
	public void setHalfHeight(String halfHeight) {
		this.halfHeight = halfHeight;
	}


	/**
	 * @return the halfWidth
	 */
	public String getHalfWidth() {
		return halfWidth;
	}


	/**
	 * @param halfWidth the halfWidth to set
	 */
	public void setHalfWidth(String halfWidth) {
		this.halfWidth = halfWidth;
	}


	/**
	 * @return the debugTarget
	 */
	public String getDebugTarget() {
		return debugTarget;
	}


	/**
	 * @param debugTarget the debugTarget to set
	 */
	public void setDebugTarget(String debugTarget) {
		this.debugTarget = debugTarget;
	}


	/*******************************************
	*
	* <b>Function Description:</b> Contructor of the Command class used to create a Command object with only a valid message type.  <p>
	*
	* <b>Parameter:</b> String cmdTypeString: The type of command specified <p>
	*
	* <b>Pre-conditions:</b> (None)<p>
	* 
	* <b>Post-conditions:</b> (None) <p>
	*
	* <b>Return Value:</b> A newly allocated command object with a valid command representation.<p>
	* 
	* <b>Functions called by this function:</b> <p>
	* 
	*******************************************/
	public Command(String cmdTypeString)
	{
		commandType = cmdTypeString;		
	}
	
	
	/*******************************************
	*
	* <b>Function Description:</b> Contructor of the Command class used to create a Command object with a valid message field and file pointer offset. <p>
	*
	* <b>Parameter:</b> String commandMessage: Search or quit instruction of a command <p>
	* <b>Parameter:</b> long offset:  Offset value corresponding to the file pointer offset of a data records text file at which the values of a record are placed <p>
	* <b>Pre-conditions:</b> A valid command message "report" or "quit" must have been obtained to contruct the object. <p>
	* 
	* <b>Post-conditions:</b> (None)<p>
	*
	* <b>Return Value:</b> A newly allocated command object with a valid command message "report" or "quit" and a file pointer offset.<p>
	* 
	* <b>Functions called by this function: (none) </b> <p>
	* 
	*******************************************/
	public Command(String commandMessage, long offset)
	{
		// Only allowed to create a command if its a "report" or "quit" command
		if (commandMessage.equalsIgnoreCase("report") || commandMessage.equalsIgnoreCase("quit"))
		{
			commandMessageString = commandMessage;
			recordOffset = offset;
		}
	}
	
	
	
	
	/*******************************************
	*
	* <b>Function Description:</b> Getter function for the message component of a command object. Retrieves the message string "report" or "quit"<p>
	*
	* <b>Parameter:</b> None <p>
	*
	* <b>Pre-conditions:</b> A previously created command object is needed to obtain the message string. <p>
	* 
	* <b>Post-conditions:</b> None <p>
	*
	* <b>Return Value:</b> String commandMessageString corresponding to the message associated with a specific command (e.g.: "report" or "quit")<p>
	* 
	* <b>Functions called by this function: </b> None <p>
	* 
	*******************************************/
	public String getCommandMessage()
	{
		return commandMessageString;
	}
	
	/**
	 * @param msg The message that needs to be represented in the command object
	 */
	public void setCommandMessage(String msg)
	{
		commandMessageString = msg;
	}
	
	
	/*******************************************
	*
	* <b>Function Description:</b> Setter function for the file pointer offset component of a command object. Retrieves the offset inside a text file at which a GIS data record line is to be found.<p>
	*
	* <b>Parameter:</b> None <p>
	*
	* <b>Pre-conditions:</b> A previously created command object is needed to obtain the offset value related to a specific search command. <p>
	* 
	* <b>Post-conditions:</b> None <p>
	*
	* <b>Return Value:</b> long recordOffset: Search commands specified offset value of the file pointer within a GIS Data text file.<p>
	* 
	* <b>Functions called by this function: </b> None <p>
	* 
	*******************************************/
	public long getCommandRecordOffset()
	{
		return recordOffset;
	}
	
	
	@Override
	public String toString()
	{
		//Create a builder for the string
		StringBuilder builder = new StringBuilder();
		
		/*
		 * Only append to string the parts of the command that have been provided
		 */
		if(this.commandMessageString.length() > 0)
		{
			builder.append(this.commandMessageString);
			builder.append("\n");
		}
		
		if(this.commandType.length() > 0)
		{
			builder.append(this.commandType);
			builder.append("\n");

		}
		if (this.worldBoundaries != null && this.worldBoundaries.length > 0)
		{
			builder.append("[ ");
			for (int i = 0; i < this.worldBoundaries.length; i++)
			{
				builder.append(this.worldBoundaries[i] + " ");
			}
			builder.append("]\n");

		}
		if (this.recordFileName.length() > 0)
		{
			builder.append(this.recordFileName);
			builder.append("\n");

		}
		if (this.coordinateString.length() > 0)
		{
			builder.append(this.coordinateString);
			builder.append("\n");

		}
		if (this.printFlag.length() > 0)
		{
			builder.append(this.printFlag);
			builder.append("\n");

		}
		if (this.featureName.length() > 0)
		{
			builder.append(this.featureName);
			builder.append("\n");

		}
		if (this.stateAbbrev.length() > 0)
		{
			builder.append(this.stateAbbrev);
			builder.append("\n");

		}
		if (this.halfHeight.length() > 0)
		{
			builder.append(this.halfHeight);
			builder.append("\n");

		}
		if (this.halfWidth.length() > 0)
		{
			builder.append(this.halfWidth);
			builder.append("\n");

		}
		if (this.debugTarget.length() > 0)
		{
			builder.append(this.debugTarget);
			builder.append("\n");

		}
		if (this.rawCommandString.length() > 0)
		{
			builder.append(this.rawCommandString);
			builder.append("\n");

		}
		return builder.toString();
	}
	
}
