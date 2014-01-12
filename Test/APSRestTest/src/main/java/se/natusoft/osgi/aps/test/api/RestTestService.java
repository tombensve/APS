package se.natusoft.osgi.aps.test.api;

/**
 *
 */
public interface RestTestService {
    String lookup(String name);

    void store(String name, String value);

    public void create(String name, String value);

    public void delete(String name);
}
