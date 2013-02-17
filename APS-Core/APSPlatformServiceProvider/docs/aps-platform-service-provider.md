# APSPlatformService

This is a trivial little service that just returns meta data about the specific platform installation.

The returned information is configured in the _/apsadminweb_.

## APIs

public _class_ __PlatformDescription__   [se.natusoft.osgi.aps.api.core.platform.model] {

>  This model provides information about a platform installation. 







__public PlatformDescription()  }  /** * Creates a new PlatformDescription. * * identifier An identifying name for the platform. * type The type of the platform, for example "Development", "SystemTest". * description A short description of the platform instance. */ public PlatformDescription(String identifier, String type, String description)__

>  Creates a new PlatformDescription. 

__public String getIdentifier()__

>  Returns the platform identifier. 

__public String getType()__

>  Returns the type of the platform. 

__public String getDescription()__

>  Returns the description of the platform. 

}

----

    

public _interface_ __APSPlatformService__   [se.natusoft.osgi.aps.api.core.platform.service] {

>  Provides information about the platform instance. 

__public PlatformDescription getPlatformDescription()__

>  Returns a description of the platform instance / installation. 

}

----

    

