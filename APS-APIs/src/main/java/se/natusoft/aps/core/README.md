# APS core stuff.

## APSServiceLocator

The service locator is a simple wrap around `java.util.ServiceLoader`. The returned service loader is however cached in a Map on service API so that all that asks for same service will get the same instance. 

