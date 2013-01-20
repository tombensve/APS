# APSFilesystemService

This provides a filesystem for writing and reading files. This filesystem resides outside of the OSGi server and is for longterm storage, which differs from BundleContext.getDataFile() which resides withing bundle deployment. The APSFilesystemService also does not return a File object! It priovides a file area for each unique owner name that is accessed through an API that cannot navigate nor access any files outside of this area. The ”owner” name should be either an application name or a bundle name if it is only used by one bundle. 

The APSConfigService uses the APSFilesystemService to store its configurations. 

## Setup

The _aps.filesystem.root_ system property must be set to point to a root where this service provides its file areas. This is either passed to the JVM at server startup or configured withing the server. Glassfish allows you to configure properties within its admin gui. Virgo does not. If this is not provided the service will use BundleContext.getDataFile(".") as the root, which will work for testing and playing around, but should not be used for more serious purposes since this is not a path with a long term availability.

## The service

The service allows you to create or get an APSFilesystem object. From that object you can create/read/delete directories (represented by APSDirectory) and files (represented by APSFile). You can get readers, writers, input streams and output streams from files. All paths are relative to the file area represented by the APSFilesystem object. 

See the [javadoc](http://apidocs.natusoft.se/APS/APSFilesystemService)

------

_interface_ __APSFilesystemService__ \[se.natusoft.osgi.aps.api.core.filesystem.service\] {

This provides a filesystem for use by services/applications. Each filesystem has its own root that cannot be navigated outside of. 
 
  Services or application using this should do something like this in their activators:
 
 	APSFilesystemService fss; 
 	APSFilesystemImpl fs;
 
 	if (fss.hasFilesystem("my.file.system")) {
 		fs = fss.getFilsystem("my.file.system");
 	}
 	else {
 		fs = fss.createFilesystem("my.file.system");
 	}

_Methods_

__public APSFilesystem createFilesystem(String owner) throws IOException__

Creates a new filesystem for use by an application or service. Where on disk this filesystem resides is irellevant. It is accessed using the "owner", and will exist until it is removed. 

_Parameters_

 _owner_ - The owner of the filesystem or rather a unique identifier of it. Concider using application or service package.

 _Throws_ 
 
 _IOException_ - on any failure. An already existing filesystem for the "owner" will cause this exception.
 
 __public boolean hasFilesystem(String owner)__
 
 Returns true if the specified owner has a fileystem.
 
  _Parameters_
  
   _owner_ - The owner of the fileystem or rather a unique identifier of it.
 
 }
 



