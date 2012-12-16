package se.natusoft.osgi.aps.tools.web;

/**
 * This is a simple API for doing a login.
 */
public interface LoginHandler {

    /**
     * Returns true if this handler sits on a valid login.
     */
    public boolean hasValidLogin();

    /**
     * Logs in with a userid and a password.
     *
     * @param userId The id of the user to login.
     * @param pw The password of the user to login.
     *
     * @return true if successfully logged in, false otherwise.
     */
    boolean login(String userId, String pw);

    /**
     * If the handler creates service trackers or other things that needs to be shutdown
     * when no longer used this methods needs to be called when the handles is no longer
     * needed.
     */
    public void shutdown();
}
