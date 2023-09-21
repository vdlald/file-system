# Task

Implement a file system (FS) based on a single file (all data must be stored within a single file):


# Requirements

[ ] There should be support for at least:
    [ ] creating
    [ ] reading
    [ ] deleting
    [ ] updating files

[ ] There should be a documented API for working with the file system
[ ] The code must be close to production quality, including tests.

[x] For implementation, please use Java
[x] Preferably, please do not use any third-party libraries for the implementation of file storage itself.


# Questions

[ ] Q-1. Will it be enough to provide just a Java interface with Javadoc or do I need to provide an OpenAPI contract?

[ ] Q-2. Do I need to optimize the space used when deleting a file?

[ ] Q-3. Should the file system be created with a specific size or unlimited?

[ ] Q-4. The result should be a mini library? Or assume it's part of the application and I can use spring and so on?

[ ] Q-5. Should the file system be hierarchical?

[ ] Q-6. Fast write or fast read?


# Tasks

[x] T-0. Base project template
[x] T-1. Creating FS file
[x] T-4. Store metadata in FS
[x] T-5. Reading blocks
[ ] T-2. Simple creating file in FS
[ ] T-3. Reading file in FS

[ ] P-1. Two files with same name problem
[ ] P-2. FS initial max size is only 2GB
[ ] P-3. The initial metadata size should not be in the initial metadata size


## T-1. Creating FS file

I will use Indexed Allocation as File Allocation Method


# Researching

1. https://www.geeksforgeeks.org/file-systems-in-operating-system/
2. https://github.com/seclerp/FileFS/blob/master/docs/Architecture-overview.md
3. https://www.geeksforgeeks.org/difference-between-internal-and-external-fragmentation/
