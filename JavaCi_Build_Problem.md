# JavaCI build problem

aps-runtime depends on aps-apis so the latter builds first, but when aps-runtime deploy in tests  from ~/.m2/repository there is no aps-apis there, even though it has just been built! I've gotten this effect before when building within Docker. But in that case it had problem deploying a built jar in target/ of same project. Docker has no problem accessing jars in ~/.m2/repository. Whatever is running this, apparently has a much longer filesystem lag of built jars being available. I need to read the jars for content to fulfill the Bundle API and more importantly APSActivator needs the information for dependency injection. 

A lagging filesystem is bad for any build! 

This is the build error:
 
    java.lang.IllegalArgumentException: File '/home/runner/.m2/repository/se/natusoft/osgi/aps/aps-apis/1.0.0/aps-apis-1.0.0.jar'
     does not exist! at se.natusoft.osgi.aps.tools.TestBundleAPS.testLoadFromMaven(TestBundleAPS.java:58)
 
aps-apis-1.0.0.jar has just been built (clearly shown by build log), and should be available!

The docker-build does not suffer from this problem even though it has a lagging filesystem. 
