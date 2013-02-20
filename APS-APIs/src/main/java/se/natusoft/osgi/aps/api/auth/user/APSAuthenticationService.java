package se.natusoft.osgi.aps.api.auth.user;

import java.util.Properties;

/**
 * This is intended to be used as a wrapper to other means of authentication. Things in APS
 * that needs authentication uses this service.
 * <p/>
 * Implementations can lookup the user in an LDAP for example, or use some other user service.
 * <p/>
 * APS supplies an APSSimpleUserServiceAuthenticationServiceProvider that uses the
 * APSSimpleUserService to authenticate. It is provided in its own bundle.
 */
public interface APSAuthenticationService<Credentials> {


    /**
     * This authenticates a user. A Properties object is returned on successful authentication. null is returned
     * on failure. The Properties object returned contains misc information about the user. It can contain anything
     * or nothing at all. There can be no assumptions about its contents! If the specified credentialType is not
     * supported by the service implementation it should simply fail the auth by returning null.
     *
     * @param userId The id of the user to authenticate.
     * @param credentials What this is depends on the value of credentialType. It is up to the service implementation to resolve this.
     * @param credentialType This hints at how to interpret the credentials.
     *
     * @return User properties on success, null on failure.
     */
    Properties authUser(String userId, Credentials credentials, CredentialType credentialType);

    //
    // Inner Classes
    //

    /**
     * This hints at how to use the credentials.
     */
    public static enum CredentialType {
        NONE,
        PASSWORD,
        KEY,
        CERTIFICATE,
        SSO
    }
}
