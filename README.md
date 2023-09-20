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


# Tasks

[x] T-0. Base project template
[ ] T-1. Creating FS file
[ ] T-2. Simple creating file in FS
[ ] T-3. Reading file in FS
[ ] T-4. Two files with same name problem
...