package se.natusoft.osgi.aps.externalprotocolextender.service;

import org.osgi.framework.Bundle;
import se.natusoft.osgi.aps.api.external.extprotocolsvc.model.APSExternallyCallable;
import se.natusoft.osgi.aps.api.external.extprotocolsvc.model.APSRESTCallable;
import se.natusoft.osgi.aps.api.external.model.type.DataTypeDescription;
import se.natusoft.osgi.aps.api.external.model.type.ParameterDataTypeDescription;

import java.util.List;

public class APSRESTCallableImpl implements APSRESTCallable {
	//
	// Private Members
	//
	
	private ServiceMethodCallable put = null;
	
	private ServiceMethodCallable post = null;
	
	private ServiceMethodCallable get = null;
	
	private ServiceMethodCallable delete = null;
	
	private HttpMethod method = null;
	
	//
	// Constructors
	//
	
	public APSRESTCallableImpl() {}
	
	public APSRESTCallableImpl(APSRESTCallableImpl callable) {
		this.put = new ServiceMethodCallable(callable.put);
		this.post = new ServiceMethodCallable(callable.post);
		this.get = new ServiceMethodCallable(callable.get);
		this.delete = new ServiceMethodCallable(callable.delete);
		this.method = null;
	}
	
	//
	// Methods
	//
	
	public boolean supportsPut() {
		return this.put != null;
	}
	
	public boolean supportsPost() {
		return this.post != null;
	}
	
	public boolean supportsGet() {
		return this.get != null;
	}
	
	public boolean supportsDelete() {
		return this.delete != null;
	}
	
	public void selectMethod(HttpMethod method) {
		// TODO: Validate!
		this.method = method;
	}
	
	private APSExternallyCallable getSelectedCallable() {
		switch(this.method) {
			case PUT:
				return this.put;
				
			case POST:
				return this.post;
				
			case GET:
				return this.get;
				
			case DELETE:
				return this.delete;
		}

        return null;
	}
	
	/**
     * @return The name of the service this callable is part of.
     */
    @Override
    public String getServiceName() {
        return getSelectedCallable().getServiceName();
    }

    /**
     * @return The name of the service function this callable represents.
     */
    @Override
    public String getServiceFunctionName() {
        return getSelectedCallable().getServiceFunctionName();
    }
    
    /**
     * @return A description of the return type.
     */
    @Override
    public DataTypeDescription getReturnDataDescription() {
        return getSelectedCallable().getReturnDataDescription();
    }
    
    /**
     * @return A description of each parameter type.
     */
    @Override
    public List<ParameterDataTypeDescription> getParameterDataDescriptions() {
        return getSelectedCallable().getParameterDataDescriptions();
    }

    /**
     * @return The bundle the service of this callable method belongs to.
     */
    @Override
    public Bundle getServiceBundle() {
        return getSelectedCallable().getServiceBundle();
    }

    /**
     * Provides parameters to the callable using a varags list of parameter values.
     *
     * @param value A parameter value.
     */
    @Override
    public void setArguments(Object... value) {
        getSelectedCallable().setArguments(value);
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     *
     * @throws se.natusoft.osgi.aps.tools.exceptions.APSNoServiceAvailableException
     *                   This is thrown if the service represented by this object
     *                   have gone away between the time you got this instance and
     *                   the call to this method. This is a runtime exception!
     * @throws Exception if unable to compute a result.
     */
    @Override
    public Object call() throws Exception {
    	return getSelectedCallable().call();
    }
		
	public Input input() {
		return new Input();
	}
	
	public class Input {
		
		public void providePut(ServiceMethodCallable put) {
			APSRESTCallableImpl.this.put = put;
		}
		
		public void providePost(ServiceMethodCallable post) {
			APSRESTCallableImpl.this.post = post;
		}
		
		public void provideGet(ServiceMethodCallable get) {
			APSRESTCallableImpl.this.get = get;
		}
		
		public void provideDelete(ServiceMethodCallable delete) {
			APSRESTCallableImpl.this.delete = delete;
		}
		
	}
	
}
