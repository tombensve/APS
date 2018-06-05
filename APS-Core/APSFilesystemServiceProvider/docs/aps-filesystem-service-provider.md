# APSFilesystemService

This provides a filesystem for writing and reading files. This filesystem resides outside of the OSGi server and is for longterm storage, which differs from BundleContext.getDataFile() which resides within bundle deployment. The APSFilesystemService also does not return a File object! It priovides a file area for each unique owner name that is accessed through an API that cannot navigate nor access any files outside of this area. The ”owner” name should be either an application name or a bundle name if it is only used by one bundle.

The APSConfigService uses the APSFilesystemService to store its configurations.

## Setup

The _aps.filesystem.root_ system property must be set to point to a root where this service provides its file areas. This is either passed to the JVM at server startup or configured withing the server. Glassfish allows you to configure properties within its admin gui. Virgo does not. If this is not provided the service will use BundleContext.getDataFile(".") as the root, which will work for testing and playing around, but should not be used for more serious purposes since this is not a path with a long term availability.

## The service

The service allows you to create or get an APSFilesystem object. From that object you can create/read/delete directories (represented by APSDirectory) and files (represented by APSFile). You can get readers, writers, input streams and output streams from files. All paths are relative to the file area represented by the APSFilesystem object.

The javadoc for the [APSFilesystemService](http://apidoc.natusoft.se/APS/se/natusoft/osgi/aps/api/core/filesystem/service/APSFilesystemService.html).

## The APIs for this service

public _interface_ __APSDirectory__ extends  APSFile    [se.natusoft.osgi.aps.api.core.filesystem.model] {

This represents a directory in an _APSFilesystem_.

Use this to create or get directories and files and list contents of directories.

Personal comment: I do prefer the term "folder" over "directory" since I think that is less ambiguous, but since Java uses the term "directory" I decided to stick with that name.

__APSDirectory createDir(String name) throws IOException__

Returns a newly created directory with the specified name.

_Parameters_

> _name_ - The name of the directory to create. 

_Throws_

> _IOException_ - on any failure. 

__APSDirectory createDir(String name, String duplicateMessage) throws IOException__

Returns a newly created directory with the specified name.

_Parameters_

> _name_ - The name of the directory to create. 

> _duplicateMessage_ - The exception messaging if directory already exists. 

_Throws_

> _IOException_ - on any failure. 

__APSFile createFile(String name) throws IOException__

Creates a new file in the directory represented by the current _APSDirectory_.

_Parameters_

> _name_ - The name of the file to create. 

_Throws_

> _IOException_ - on failure. 

__APSDirectory getDir(String dirname) throws FileNotFoundException__

Returns the specified directory.

_Parameters_

> _dirname_ - The name of the directory to enter. 

_Throws_

> _FileNotFoundException_ - on failure 

__APSFile getFile(String name)__

Returns the named file in this directory.

_Parameters_

> _name_ - The name of the file to get. 

__void recursiveDelete() throws IOException__

Performs a recursive delete of the directory represented by this _APSDirectory_ and all subdirectories and files.

_Throws_

> _IOException_ - on any failure. 

__String[] list()__

_See_

> java.io.File.list()

__APSFile[] listFiles()__

_See_

> java.io.File.listFiles()

}

----

    

public _interface_ __APSFile__   [se.natusoft.osgi.aps.api.core.filesystem.model] {

This represents a file in an _APSFilesystemService_ provided filesystem. It provides most of the API of _java.io.File_ but is not a _java.io.File_! It never discloses the full path in the host filesystem, only paths relative to its _APSFilesystem_ root.

Use the createInputStream/OutputStream/Reader/Writer to read and write the file.

__InputStream createInputStream() throws IOException__

Creates a new _InputStream_ to this file.

_Throws_

> _IOException_ - on failure 

__OutputStream createOutputStream() throws IOException__

Creates a new _OutputStream_ to this file.

_Throws_

> _IOException_ - on failure 

__Reader createReader() throws IOException__

Creates a new _Reader_ to this file.

_Throws_

> _IOException_ - on failure 

__Writer createWriter() throws IOException__

Creates a new _Writer_ to this file.

_Throws_

> _IOException_ - on failure 

__Properties loadProperties() throws IOException__

If this file denotes a properties file it is loaded and returned.

_Throws_

> _IOException_ - on failure or if it is not a properties file. 

__void saveProperties(Properties properties) throws IOException__

If this file denotes a properties file it is written with the specified properties.

_Parameters_

> _properties_ - The properties to save. 

_Throws_

> _IOException_ - on failure or if it is not a properties file. 

__APSDirectory toDirectory()__

If this _APSFile_ represents a directory an _APSDirectory_ instance will be returned. Otherwise _null_ will be returned.

__APSFile getAbsoluteFile()__

_See_

> java.io.File.getAbsoluteFile()

__String getAbsolutePath()__

Returns the absolute path relative to filesystem root.

__APSFile getCanonicalFile() throws IOException__

_See_

> java.io.File.getCanonicalFile()

__String getCanonicalPath() throws IOException__

_See_

> java.io.File.getCanonicalPath()

__String getParent()__

_See_

> java.io.File.getParent()

__APSDirectory getParentFile()__

_See_

> java.io.File.getParentFile()

__String getPath()__

_See_

> java.io.File.getPath()

__boolean renameTo(APSFile dest)__

_See_

> java.io.File.renameTo(File)

__String getName()__

_See_

> java.io.File.getName()

__boolean canRead()__

_See_

> java.io.File.canRead()

__boolean canWrite()__

_See_

> java.io.File.canWrite()

__boolean exists()__

_See_

> java.io.File.exists()

__boolean exists(String name)__

Checks if the named file/directory exists.

_Returns_

> true or false.

_Parameters_

> _name_ - The name to check. 

__boolean isDirectory()__

_See_

> java.io.File.isDirectory()

__boolean isFile()__

_See_

> java.io.File.isFile()

__boolean isHidden()__

_See_

> java.io.File.isHidden()

__long lastModified()__

_See_

> java.io.File.lastModified()

__long length()__

_See_

> java.io.File.length()

__boolean createNewFile() throws IOException__

_See_

> java.io.File.createNewFile()

__boolean delete()__

_See_

> java.io.File.delete()

__void deleteOnExit()__

_See_

> java.io.File.deleteOnExit()

__String toString()__

Returns a string representation of this _APSFile_.

__File toFile()__

This API tries to hide the real path and don't allow access outside of its root, but sometimes you just need the real path to pass on to other code requiring it. This provides that. Use it only when needed!

_Returns_

> A File object representing the real/full path to this file.

}

----

    

public _interface_ __APSFilesystem__   [se.natusoft.osgi.aps.api.core.filesystem.model] {

This represents an _APSFilesystemService_ filesystem.

__APSDirectory getDirectory(String path) throws IOException__

Returns a folder at the specified path.

_Parameters_

> _path_ - The path of the folder to get. 

_Throws_

> _IOException_ - on any failure, specifically if the specified path is not a folder or doesn't exist. 

__APSFile getFile(String path)__

Returns the file or folder of the specified path.

_Parameters_

> _path_ - The path of the file. 

__APSDirectory getRootDirectory()__

Returns the root directory.

}

----

    

public _interface_ __APSFilesystemService__   [se.natusoft.osgi.aps.api.core.filesystem.service] {

This provides a filesystem for use by services/applications. Each filesystem has its own root that cannot be navigated outside of.

Services or application using this should do something like this in their activators:

        APSFilesystemService fss;
        APSFilesystem fs;
        
        fss.getFilesystem("my.file.system", (result) -> {
            if (result.success()) {
                fs = result.result();
            }
        });



__void getFilesystem( String owner, APSHandler<APSResult<APSFilesystem>> handler)__

Returns the filesystem for the specified owner. If the filesystem does not exist it is created.

_Parameters_

> _owner_ - The owner of the filesystem or rather a unique identifier of it. 

> _handler_ - Called with the filesystem. 

_Throws_

> _APSIOException_ - on failure. 

__void deleteFilesystem(String owner, APSHandler<APSResult<Void>> handler)__

Removes the filesystem and all files in it.

_Parameters_

> _owner_ - The owner of the filesystem to delete. 

_Throws_

> _APSIOException_ - on any failure. 

}

----

    

