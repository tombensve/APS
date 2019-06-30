package se.natusoft.osgi.aps.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Trivially simple List builder. This just exists to match MapBuilder.
 */
public class ListBuilder {

    @SuppressWarnings( "unchecked" )
    public static List list( Object... entries) {
        List list = new LinkedList(  );

        Collections.addAll( list, entries );

        return list;
    }
}
