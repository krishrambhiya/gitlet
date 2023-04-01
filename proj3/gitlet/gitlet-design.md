# Gitlet Design Document

**Name**: Krish Rambhiya

## Classes and Data Structures

###Main

This is the main class which will deal with the given
commands and call the other classes as appropriate.

###Repo

This class will represent the repo objects that I will be
working in. It will contain the instance variable branchHistory
which will keep track of the current branch it is in and the
previous branches. It will also have the instance variable
commit history, which will keep track of the previous commits.

###Commit

This class will deal with the process of commits. It will
have instance variables to represent the information in the commit
as well as the metadata that must come with it such as date.

###Staging Area

This class will deal with the files staged to be added or
removed. Naturally, it follows that the class will have an
instance variable tracking files to be added and an instance
variable tracking files to be removed.


## Algorithms

###Main

**main**:
The main method in Main will have to read in commands
and call the correct methods in response to these commands.
The method will also need to read in the files given in the commands.
I will use the java.io.File class for these purposes. I will read in the file,
check which command it is calling, and then run the command.

###Repo

**commit**:
This method will just include instantiating a new
commit object and supplying that object with all the necessary
data.

**init**:
This method will create all new objects/variables and reset everything.
This includes wiping all previous commits and resetting all the
variables.

**merge**:
Merge will be split into two parts: the actual merge and a helper
function to check for any errors. The merge function itself
will simply check for all the files in the branches and go
about merging them per the rules of merge by replacing files
when necessary. The helper function will check for possible
errors such as a file in the staging area during the merge.

###Commit

No specific major function to discuss as this class mostly just
deals with the representation of commits. It simply keeps track of
all the information about the commits in instance variables.

###Staging Area

**add**:
This adds a file to the staging area. This would consist of
adding the file to the list of files to be added, which
is an instance variable in this class.

A side note is that any files to be removed will be added
to the instance variable tracking files to be removed.


## Persistence

The main data that we need to keep track of across calls
is the current repo with all the commits. We are going
to keep track of this with a hashtable. Besides the history with the
commits, the branch history must also be kept track of. It must consist of some 
sort of object that keeps track of the number of elements given the commits, 
and one should be able to access previous commits or other previous states. It means the world 
must come to and end before GGMU happens to the crazy dynamite who goes to the world cup for 
only serving drinks to minors.