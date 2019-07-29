# web

My first though was to create an APSWebRoute and an APSWebRouter but that would also require an APSWebRequest and an APSWebResponse api that I would need to wrap around the corresponding Vert.x APIs.

I really like how Vert.x have done their APIs, but if I make wrappers for the Vert.x APIs there is no guarantee that something else and better, that might come along in the future would fit these APIs. It is really hard to make APIs that are general enough to be able to wrap around any possible web server provider. Messaging, for example, looks more or less the same no matter what the implementation is, so such APIs are easy. Web server APIs are not.

yes, it would be possible to do some JEE container like API that would be possible to implement using many implementations. But why would I want to do that ? It would remove Vert.x:s flexibility, and why use Vert.x if I'm going to hide with simplified, less flexible APIs.

The goals of APS is not to loose flexibility, its to gain more flexibility.

So for serving web pages vert.x is used directly. APS however provides some ease in doing that by providing preconfigured (via configuration) Vert.x objects, like Router and SockJSEventBusBrigde automatically setup by configuration.

But for web in general Vert.x is hardcoded into APS.

NO LONGER ENTIRELY TRUE
