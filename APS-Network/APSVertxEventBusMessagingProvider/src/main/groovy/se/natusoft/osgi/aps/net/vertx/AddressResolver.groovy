package se.natusoft.osgi.aps.net.vertx

class AddressResolver {

    @SuppressWarnings("GrMethodMayBeStatic")
    protected String resolveAddress(String destination) {
        // TODO: lookup destination in config and change to configured address if match found. If no match use destination.
        return destination
    }

}
