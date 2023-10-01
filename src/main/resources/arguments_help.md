NAME
sfs – file system in a single file

SYNOPSIS
sfs [--operation=what_to_do] [--fs=path] [--init-size=num] [--filename=path] [--file-in=path]

DESCRIPTION
The utility allows you to create and manage a file system (fs) in a single file.

     The options are as follows:

     --operation=what_to_do      
             It's required.
             Allows you to specify which operation to perform on the file system: 
             "create-file-system" - create file system, 
             "create-file" - create file in fs, "read-file" - read file from fs, 
             "update-file" - update file in fs, "delete-file" - delete file in fs
             "list-files" - list all files in fs, "move-file" - move a file from one 
             location to another, "md5-checksum" - calculate the check sum of the file 

     --fs=path
             Allows you to specify the path to the file system. When 
             --operation=create-file-system allows you to specify in which file 
             to create a file system, and in other cases specifies which file with 
             the file system the operation will be performed on.

     --init-size=num
             Allows you to specify how much space should be reserved for the file 
             system in advance.

     --filename=path
             When creating a file, allows you to specify the name of the file to 
             be created, and in other cases indicates the file in the file system 
             to be operated on
     
     --new-filename=path
             Defines what the new file name will be 

     --file-out=path
             Determines where the content of the read-file will be written during 
             the "read-file" operation

     --file-in=path
             Allows you to specify from which file to read content when creating 
             a file in the file system

EXIT STATUS
The sfs utility exits with one of the following values:

     0     Operation successfully completed.
     >0    An error occurred.

EXAMPLES
The command:

           sfs --operation=create-file-system --fs=./filesystem

     will create a file system with initial default size at "./filesystem".

     The command:

           sfs --operation=create-file --fs=./filesystem --filename=some --file-in=../cat1.png

     a file "cat1.png" will be created in the "./filesystem" file system with 
     content from the "../cat1.png" file.
    
     The command:
           
           sfs --operation=read-file --fs=./filesystem --filename=some --file-out=out

     Reads a file from a file system named "some" and writes its content to a file outside the 
     file system named "out"