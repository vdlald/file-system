# Task

Implement a file system (FS) based on a single file (all data must be stored within a single file):

# Requirements

There should be support for at least:
[x] creating
[x] reading
[x] deleting
[x] updating files

[ ] There should be a documented API for working with the file system
[ ] The code must be close to production quality, including tests.

[x] For implementation, please use Java
[x] Preferably, please do not use any third-party libraries for the implementation of file storage itself.

# Questions

[x] Q-1. Will it be enough to provide just a Java interface with Javadoc or do I need to provide an OpenAPI contract?
It would be great to see a console app, but Java interface with Javadoc will be sufficient

[x] Q-2. Do I need to optimize the space used when deleting a file?
It’s up to you if you want to optimize the used space

[x] Q-3. Should the file system be created with a specific size or unlimited?
File system should not have a hard size limit but let’s assume that users won’t stress it beyond some reasonable limits

[x] Q-4. The result should be a mini library? Or assume it's part of the application and I can use spring and so on?
It would be great to see a console app, but Java interface with Javadoc will be sufficient

[x] Q-5. Should the file system be hierarchical?
Up to me

[x] Q-6. Fast write or fast read?
As for the fast write/fast read - let’s assume that we want balance between reads and writes

# Tasks

[x] T-0. Base project template
[x] T-1. Creating FS file
[x] T-4. Store metadata in FS
[x] T-5. Reading blocks
[x] T-6. Write data in block
[x] T-9. Refactor file system creation
[x] T-12. Writing data - Serializing File descriptors
[x] T-10. Writing data - Write file descriptor in descriptors block
[x] T-13. Refactor entire project
[x] T-14. Find the best block for filling a data
[x] T-11. Writing data - Write file data in available place
[x] T-15. Writing data - Write big file
[T-11] T-2. Simple creating file in FS
[x] T-16. Save a block size of the FS in metadata
[x] T-3. Reading files in FS
[x] T-17. Delete file in FS
[x] T-18. Update file in FS
[x] T-19. Refactor entire project
[x] P-6. update does not remove index blocks when compressing a file
[x] P-1. Two files with same name problem
[x] P-4. Allocated space offset validation
[x] T-21. Write more tests
[x] T-22. Write more documentation
[x] T-25. Extract ChainedFileDescriptorsBlock from MyFileSystemOperations
[x] T-26. Refactor MyFileSystemOperations
[ ] T-23. Wrap it in console app
[ ] T-24. Check speed of RW-operations

[ ] P-2. FS initial max size is only 2GB
[ ] P-5. readBlocks can not allocate more than Integer.MAX bytes
[ ] P-6. Primitive collections are not used
[ ] T-20. Work with different block sizes

[ ] FR-1. Compress data
[ ] FR-2. Encrypt data
[ ] FR-3. Defragmentation

# Researching

1. https://www.geeksforgeeks.org/file-systems-in-operating-system/
2. https://github.com/seclerp/FileFS/blob/master/docs/Architecture-overview.md
3. https://www.geeksforgeeks.org/difference-between-internal-and-external-fragmentation/