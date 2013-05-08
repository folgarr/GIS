import Parsers.CommandParser;

/**
 * Programmer: Carlos Folgar
 * Project: Major
 * Last modification date: 12/3/2012
 * 
 * Purpose of program:
 * The purpose of this program is to provide a set of classes that together form a Geographical
 * Information System (GIS) that can read and process a command script file that imports geographical
 * record data sets and acts on the data.  The system is able to store the records represented 
 * in the imported files and store them in a database.  The GIS can also retrieve records stored
 * in the database when asked to do so by a command. However, we wish to minimize file IO on disk
 * therefore a bufferpool structure is employed to cache recently used records.  The GIS makes use
 * of a PR Quadtree to store the geographic location of a record while a HashTable stores the feature
 * descriptions of a record - any of these indexes may be accessed with an appropriate command.  Finally,
 * the GIS can handle the importing of various records files (structures resize) and at the end
 * of processing be able to output to a provided log file.
 * 
 */


/** 
 * Class: GIS
 * 
 * Purpose and Description:
 * The purpose of the GIS class is mainly to serve as the entry point to the program and
 * to set up the required components of the system and GIS Controller class.
 * 
 * The initial set up allows for the controller to function as an arbiter of the retrieval
 * of data and the processing of commands. This driver class continually asks if 
 * the controller is done processing commands and stops the program when no commands are
 * left to process or 'quit' is encountered.
 * 
 */

public class GIS {


	/*******************************************
	 *
	 * <b>Function Description:</b> The function main() is the entry-point of the program and serves to set up the needed components of the GIS.<p>
	 *
	 * <b>Parameter:</b> String[] args: Array of command-line arguments. <p>
	 *
	 * <b>Pre-conditions:</b> A valid GIS commands script text file will need to be obtained or composed before passing them into the program.<p>
	 * 
	 * <b>Post-conditions:</b> The creation of a Log file to log the output of the commands and also a database file where records were stored<p>
	 *
	 * <b>Return Value:</b> (None) <p>
	 * 
	 *******************************************/

	public static void main(String[] args)
	{
		// Program requires a database file name, a command file name, and a log file name
		if (args.length != 3) {
			System.err.println("Error: Unable to start program - GIS requires 3 arguments to start.");
			System.exit(-1);
		}
		try 
		{
			// Main controller class to serve as the manager of data that is processed and obtained from the record/command parsers.
			GISController controller = new GISController(args[0], args[2], new CommandParser(args[1]));
						
			// Loop while more commands and data processing needs to be done
			boolean commandsToProcess = true;
			while (commandsToProcess) 
				commandsToProcess = controller.processNextCommand();
		}
		catch (NullPointerException ex) {
			System.out.println(ex); // Log exception error that will pin-point where in program the error occured
			System.exit(-1);
		}


	}

}