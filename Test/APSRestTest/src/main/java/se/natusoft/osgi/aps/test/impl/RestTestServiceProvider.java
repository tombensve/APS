package se.natusoft.osgi.aps.test.impl;

import se.natusoft.osgi.aps.api.net.rpc.annotations.RESTDELETE;
import se.natusoft.osgi.aps.api.net.rpc.annotations.RESTGET;
import se.natusoft.osgi.aps.api.net.rpc.annotations.RESTPOST;
import se.natusoft.osgi.aps.api.net.rpc.annotations.RESTPUT;
import se.natusoft.osgi.aps.exceptions.APSRuntimeException;
import se.natusoft.osgi.aps.tools.annotation.activator.OSGiServiceProvider;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@OSGiServiceProvider
public class RestTestServiceProvider implements se.natusoft.osgi.aps.test.api.RestTestService {

    private Map<String, String> data = new HashMap<>();

    @Override
    @RESTGET
    public String lookup(String name) {
        return this.data.get(name);
    }

    @Override
    @RESTPUT
    public void store(String name, String value) {
        if (this.data.containsKey(name)) {
            throw new APSRuntimeException("Key already exists!");
        }
        this.data.put(name, value);
    }

    @Override
    @RESTPOST
    public void create(String name, String value) {
        this.data.put(name, value);
    }

    @Override
    @RESTDELETE
    public void delete(String name) {
        this.data.remove(name);
    }
}
