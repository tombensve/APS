package se.natusoft.osgi.aps.api.external.extprotocolsvc.model;

public interface APSRESTCallable extends APSExternallyCallable {
	
	public boolean supportsPut();
	
	public boolean supportsPost();
	
	public boolean supportsGet();
	
	public boolean supportsDelete();
	
	public void selectMethod(HttpMethod method);
	
	public static enum HttpMethod {
		PUT,
		POST,
		GET,
		DELETE;
	}
}
