# Subversion #

  * Checkout the code
```
svn checkout https://storage-at-desk.googlecode.com/svn/trunk/ storage-at-desk --username usename
```
  * Update to latest version of code
```
svn up
```
  * Upload a modification
```
svn commit
```
  * Find the differences between working directory and last commit
```
svn status
```

# Eclipse #

Checking out the code will create a folder `storage-at-desk` which is an eclipse project folder.  Open an eclipse to any workspace and then follow these steps

  1. File | Import
  1. Existing Project
  1. Select Root Directory
  1. Do **NOT** check "Copy projects into workspace"
  1. Do normal things


# Details #

Just a list of some things you should not do:
  * Remove files from the repository
  * Add binaries to the repository
  * Add run-independent files to the repository (such as log & data files)