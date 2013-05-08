package Parsers;
/*
 * Programmer: Carlos Folgar
 * Project: Major
 * Last modification date: 12/3/2012
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

import Types.Command;


/** 
 * Class: CommandParser
 * 
 * Purpose and Description:
 * The purpose of the CommandParser class is to parse its associated command text file to retrieve the necessary data to create a command object.
 * This class uses its command text file reference to keep track of where in the commands file it last retrieved a command object. This allows
 * for the continuous retrieval of search commands using the nextCommand() function.
 * 
 */
public class CommandParser {

	// File object corresponding to file containing the valid commands
	private File commandsFile = null;
	// File pointer offset of the search commands text file
	private long runningCommandLineOffset = 0;
	// Keeps track of the amount of commands that have been proccessed by the parser
	private int commandsProcessed = 0;
	
	/*******************************************
	*
	* <b>Function Description:</b> Main constructor of the Command Parser class. Used to create a command parser object that has a reference to a commands text file.<p>
	*
	* <b>Parameter:</b>String commandFilePath: Filepath in the current project directory where the commands text file can be found. <p>
	*
	* <b>Pre-conditions:</b> Creation of a commands text file that has been placed in the current project directory<p>
	* 
	* <b>Post-conditions:</b> Creation of a commands parser object that can obtain search commands from its file reference<p>
	*
	* <b>Return Value:</b> A newly created CommandsParser object to parse commands search instructions<p>
	* 
	* <b>Functions called by this function: </b> None <p>
	* 
	*******************************************/	
	public CommandParser(String commandFilePath)
	{
		if (commandFilePath != null)
		{
			commandsFile = new File(commandFilePath);
			if (commandsFile == null || !commandsFile.exists()) {
				System.err.println("Error: Unable to create the command file named \"" + commandFilePath + "\"");
				System.exit(-1);
			}
		}
		else
		{
			System.err.println("Error: Unable to initialze command parser with invalid directory path to a commands file.");
			System.exit(-1);
			
		}
	}
	
	/*******************************************
	*
	* <b>Function Description:</b> Constructor of the Command Parser class. Used to create a command parser object that has a reference to a commands text file.<p>
	*
	* <b>Parameter:</b>File commandFile: File object of the commands text file in the current project directory. <p>
	*
	* <b>Pre-conditions:</b> Creation of a File object using a commands text file that has been placed in the current project directory<p>
	* 
	* <b>Post-conditions:</b> Creation of a commands parser object that can obtain search commands from its file reference<p>
	*
	* <b>Return Value:</b> A newly created CommandsParser object to parse commands search instructions<p>
	* 
	* <b>Functions called by this function: </b> None <p>
	* 
	*******************************************/
	public CommandParser(File commandFile)
	{
		if (commandFile != null) commandsFile = commandFile;
	}
	
	
	/*******************************************
	*
	* <b>Function Description:</b> Obtains the next command at the current line (indicated by command file offset counter) of the command file.<p>
	*
	* <b>Parameter:</b> None <p>
	*
	* <b>Pre-conditions:</b> Creation of a commandsParser object with a reference to a commands file<p>
	* 
	* <b>Post-conditions:</b> The file offset counter will be updated to point to a new line of the commands file <p>
	*
	* <b>Return Value:</b> A newly created Command object to retrieve search data from. <p>
	* 
	* <b>Functions called by this function: </b> RandomAccessFile(), readLine(), Scanner(), Command(), close() <p>
	* 
	*******************************************/
	public Command nextCommand()
	{
		Command command = null;
		try 
		{
			// Open the commands file in read-only mode to retrieve the search instructions
			RandomAccessFile commandsRAF = new RandomAccessFile(this.commandsFile, "r");
			// Move the file pointer to the next line to be parsed - this is indicated by the updated file offset counter of the parse
			commandsRAF.seek(runningCommandLineOffset);
			String rawCommandString = commandsRAF.readLine();
			
			// Update the running offset inside the command file associated with this parser
			runningCommandLineOffset = commandsRAF.getFilePointer();
			
			// Command to construct from script instruction
			Command parsedCommand = new Command("InvalidCommand");
			parsedCommand.setRawCommandString(rawCommandString);
			
			// Scan the string and delimit using tabs (format outlined in specification)
			Scanner scanner = new Scanner(rawCommandString);
			scanner.useDelimiter("\t");
			
			// Split the command string to construct it piece-by-piece
			String[] parts = rawCommandString.split("\t");
			
			if (rawCommandString.contains(";"))
			{
				parsedCommand.setCommandType("Comment");
				parsedCommand.setCommandMessage(rawCommandString);
			}
			else if (rawCommandString.contains("world")) 
			{
				parsedCommand.setCommandType("World");
				String[] boundaries = { parts[1], parts[2], parts[3], parts[4] };
				parsedCommand.setWorldBoundaries(boundaries);
				commandsProcessed++; // Update the number of commands that we have processed
			}
			else if (rawCommandString.contains("import"))
			{
				parsedCommand.setCommandType("Import");
				parsedCommand.setRecordFileName(parts[1]);
				commandsProcessed++; // Update the number of commands that we have processed
			}
			else if (parts[0].equalsIgnoreCase("what_is_at"))
			{
				parsedCommand.setCommandType("toCoordIndex");
				if (parts.length < 4) {
					parsedCommand.setCoordinateString(parts[1] + " " + parts[2]);				
				}
				else
				{
					parsedCommand.setPrintFlag(parts[1]);
					parsedCommand.setCoordinateString(parts[2]+" "+parts[3]);
				}
				commandsProcessed++; // Update the number of commands that we have processed
			}
			else if (parts[0].equalsIgnoreCase("what_is"))
			{
				parsedCommand.setCommandType("toNameIndex");
				if (parts.length < 4) {
					parsedCommand.setFeatureName(parts[1]);
					parsedCommand.setStateAbbrev(parts[2]);
				}
				else
				{
					parsedCommand.setPrintFlag(parts[1]);
					parsedCommand.setFeatureName(parts[2]);
					parsedCommand.setStateAbbrev(parts[3]);
				}
				commandsProcessed++; // Update the number of commands that we have processed
			}
			else if (parts[0].equalsIgnoreCase("what_is_in"))
			{
				parsedCommand.setCommandType("toCoordIndex");
				if (parts.length < 6) {
					parsedCommand.setCoordinateString(parts[1] +" "+ parts[2]);
					parsedCommand.setHalfHeight(parts[3]);
					parsedCommand.setHalfWidth(parts[4]);
				}
				else
				{
					parsedCommand.setPrintFlag(parts[1]);
					parsedCommand.setCoordinateString(parts[2] +" "+ parts[3]);
					parsedCommand.setHalfHeight(parts[4]);
					parsedCommand.setHalfWidth(parts[5]);
				}
				commandsProcessed++; // Update the number of commands that we have processed
			} else if (parts[0].equalsIgnoreCase("debug"))
			{
				parsedCommand.setCommandType(parts[0]);
				parsedCommand.setDebugTarget(parts[1]);
				commandsProcessed++; // Update the number of commands that we have processed
			}
			else if (parts[0].equalsIgnoreCase("quit")) // End processing command
			{
				parsedCommand.setCommandType(parts[0]);
			}
			else {
				System.err.println("Error: Unknow command (invalid format) encountered. Will ignore.");
			}
			
			// Close the file from reading and scanning
			commandsRAF.close();
			scanner.close();
			
			return parsedCommand;
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Error: Unable to find an existing commands file. File parsing failed.");
			System.exit(-1);
		}
		catch (IOException e)
		{
			System.err.println("Error: Command text file reading failure.");
			System.exit(-1);
		}
		
		return command;
	}
	
	/**
	 * @return the commandsFile
	 */
	public File getCommandsFile() {
		return commandsFile;
	}

	/*******************************************
	*
	* <b>Function Description:</b> Returns the number of successful commands processed.<p>
	*
	* <b>Parameter:</b> None <p>
	*
	* <b>Pre-conditions:</b> Creation of a commandsParser object with a reference to a commands file<p>
	* 
	* <b>Post-conditions:</b> None <p>
	*
	* <b>Return Value:</b> The number of commands that have been processed by this commands parser <p>
	* 
	* <b>Functions called by this function: </b> None <p>
	* 
	*******************************************/
	public int getCommandsProccessed()
	{
		return commandsProcessed;
	}
	
	
}
