package se.natusoft.osgi.aps.tools.apis;

import org.osgi.framework.Bundle;
import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;

/**
 * This is for pointing out very serious things beyond error logging.
 *
 * Implementing services can for example:
 *
 * - Send mails to some operations responsible team.
 * - Display on operations screen.
 * - Sound an alarm.
 * - Blow up the server hall.
 * - Trigger a nuclear strike.
 *
 * Whatever is appropriate.
 *
 * So why is this interface in aps-tools-lib rather than aps-apis ? Because aps-tool-lib has no dependencies to
 * anything else. It can be used as is without any other dependencies. Putting this interface in aps-apis would
 * break this independence.
 */
public interface APSAlarmService {

    /**
     * Send an alarm.
     *
     * @param bundle The bundle doing the alarm.
     * @param message The alarm message.
     * @param cause The cause of the alarm.
     */
    void alarm(@NotNull Bundle bundle, @NotNull String message, @Nullable Throwable cause );
}
