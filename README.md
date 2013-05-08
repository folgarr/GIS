# Geographic Information System (GIS)

This java program implements a Geographic Information System (GIS)
that stores the location of a geographic feature along with its other attributes.  The input to the system is a GIS record obtained from a 
website like the the USGS Board on Geographic Names (geonames.usgs.gov).

# Features
This system indexes and provides search features for multiple GIS
records. Additionally, this project includes my implementation of several 
in-memory index data structures that allow for:
* Importing new GIS records into the database file
* Retrieving data for all GIS records matching given geographic coordinates
* Retrieving data for all GIS records matching a given feature name and state
* Retrieving data for all GIS records that fall within a given (rectangular) geographic region
* Displaying the in-memory indices in a human-readable manner

# Operating the GIS program

The java program can be run from the command line with three arguments: a database file name, a command script file name, and a log file name.  The database file will store the records in the GIS format , the command script file is the user commands to query the database, and the log file will store the results of the user commands.

The program will take the names of three files from the command line, like this:

* java GIS <database file name> <command script file name> <log file name>


# Data Structures

Seeing as the GIS works with record files that contain many data points that correspond to hundreds of different locations, I have provided my own implementation of a few data-structures to improve performance.

* Hash-Table
My implementation of a hash-table will be used for the name index where 
the GIS records will be indexed by the Feature Name and State (abbreviation) fields. This name index will support finding offsets of GIS records that match a given feature name and state abbreviation.
* PR-Quadtree
My implementation of a PR quadtree will be used for GIS records that are indexed by geographic coordinate. This coordinate index will support finding offsets of GIS records that match a given primary latitude and primary longitude.
* Buffer-Pool
My implementation of a Buffer-Pool is capable of buffering up to 20 records and will use a LRU replacement strategy in order to improve performance.

# Command File

* The commands that this sytem can process are outlined below:

world<tab><westLong><tab><eastLong><tab><southLat><tab><northLat>
This will be the first command in the file, and will occur once. It specifies the boundaries of the coordinate space to be modeled. The four parameters will be longitude and latitudes expressed in DMS format, representing the vertical and horizontal boundaries of the coordinate space.
It is possible that the GIS record file will contain records for features that lie outside the specified coordinate space. Such records should be ignored; i.e., they will not be indexed.
import<tab><GIS record file>
Add all the GIS records in the specified file to the database file. This means that the records will be appended to the existing database file, and that those records will be indexed in the manner described earlier. When the import is completed, log the number of entries added to each index, and the longest probe sequence that was needed when inserting to the hash table.
what_is_at<tab><geographic coordinate>
For every GIS record in the database file that matches the given <geographic coordinate>, log the offset at which the record was found, and the feature name, county name, and state abbreviation. Do not log any other data from the records.
what_is_at<tab>-l<tab><geographic coordinate>
For every GIS record in the database file that matches the given <geographic coordinate>, log every important non-empty field, nicely formatted and labeled. See the posted log files for an example. Do not log any empty fields.
what_is_at<tab>-c<tab><geographic coordinate>
Log the number of GIS records in the database file that match the given <geographic coordinate>. Do not log any data from the records themselves.
what_is<tab><feature name><tab><state abbreviation>
For every GIS record in the database file that matches the given <feature name> and <state abbreviation>, log the offset at which the record was found, and the county name, the primary latitude, and the primary longitude. Do not log any other data from the records.
what_is<tab>-l<feature name><tab><state abbreviation>
For every GIS record in the database file that matches the given <feature name> and <state abbreviation>, log every important non-empty field, nicely formatted and labeled. See the posted log files for an example. Do not log any empty fields.
what_is<tab>-c<feature name><tab><state abbreviation>
Log the number of GIS record in the database file that match the given <feature name> and <state abbreviation>. Do not log any data from the records themselves.
what_is_in<tab><geographic coordinate><tab><half-height><tab><half-width>
For every GIS record in the database file whose coordinates fall within the closed rectangle with the specified height and width, centered at the <geographic coordinate>, log the offset at which the record was found, and the feature name, the state name, and the primary latitude and primary longitude. Do not log any other data from the records. The half-height and half-width are specified as seconds.
what_is_in<tab>-l<tab><geographic coordinate><tab><half-height><tab><half-width>
For every GIS record in the database file whose coordinates fall within the closed rectangle with the specified height and width, centered at the <geographic coordinate>, log every important non-empty field, nicely formatted and labeled. See the posted log files for an example. Do not log any empty fields. The half-height and half-width are specified as seconds.
what_is_in<tab>-c<tab><geographic coordinate><tab><half-height><tab><half-width>
Log the number of GIS records in the database file whose coordinates fall within the closed rectangle with the specified height and width, centered at the <geographic coordinate>. Do not log any data from the records themselves. The half-height and half-width are specified as seconds.
debug<tab>[ quad | hash | pool ]
Log the contents of the specified index structure in a fashion that makes the internal structure and contents of the index clear. It is not necessary to be overly verbose here, but it would be useful to include information like key values and file offsets where appropriate.
quit<tab>
Terminate program execution.
