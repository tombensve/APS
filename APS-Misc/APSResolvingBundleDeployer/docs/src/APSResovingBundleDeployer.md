# APSResolvingBundleDeployer

This is a bundle deployer that is intended as an alternative to the server provided deployer. 

This bundle deployer will try to automatically resolve deploy dependencies. It does this by having a fail threshold. If the deploy of a bundle fails it just keeps quite and put the bundle at the end of the list of bundles to deploy. It updates the try count for the bundle however. Next time the bundle is up for deploy it might have the dependencies it needs and will deploy. If not it goes back to the end of the list again and its retry count is incremented again. This repeats until the retry count reaches the threshold value in which case an error is logged and the bundle will not be attempted to be deployed again unless it gets a new timestamp on disk. 

Glassfish does something similar, but Virgo fails completely unless bundles are deployed in the correct order. You have to provide a par file for Virgo to deploy correctly. 

There is one catch to using this deployer: It does not handle WAB bundles! Neither Glassfish nor Virgo seems to handle WAB deployment using the OSGi extender pattern. If they did they would recognize a WAB being deployed even though it is deployed by this deployer and handle it. They dont! 

## Configuration

The following configuration is available for this deployer. Edit this in /apsadminweb ”Configurations” tab under the _aps_ node. 

__deployDirectory__ - The directory to deploy bundles from. All bundles in this directory will be attempted to be deployed.

__failThreshold__ - The number of failed deploys before giving upp. The more bundles and the more dependencies among them the higher the value should be. The default value is 8.
