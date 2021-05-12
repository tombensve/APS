/*
 *
 * PROJECT
 *     Name
 *         APS APIs
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides the APIs for the application platform services.
 *
 * COPYRIGHTS
 *     Copyright (C) 2012 by Natusoft AB All rights reserved.
 *
 * LICENSE
 *     Apache 2.0 (Open Source)
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 * AUTHORS
 *     tommy ()
 *         Changes:
 *         2018-05-26: Created!
 *
 */
package se.natusoft.aps.api.messaging;

import se.natusoft.docutations.NotNull;
import se.natusoft.docutations.Nullable;
import se.natusoft.docutations.Reactive;
import se.natusoft.aps.types.APSHandler;
import se.natusoft.aps.types.APSResult;
import se.natusoft.aps.types.ID;

/**
 * Provides functionality for clients wanting to receive messages.
 */
public interface APSMessageSubscriber {

    /**
     * Adds a subscriber.
     *
     * @param destination    The destination to subscribe to.
     *                       This is up to the implementation, but it is strongly recommended that
     *                       this is a name that will be looked up in some configuration for the real
     *                       destination, by the service rather than have the client pass a value from
     *                       its configuration.
     * @param subscriptionId A unique ID used to later cancel the subscription. Use APSUUID or some other ID
     *                       implementation that is always unique.
     * @param result         The result of the call. Will throw an APSMessagingException on failure if value is null.
     * @param handler        The subscription handler.
     */
    @Reactive
    void subscribe(@NotNull String destination, @NotNull ID subscriptionId, @Nullable APSHandler<APSResult> result,
                   @NotNull APSHandler<APSMessage> handler);

    /**
     * Cancel a subscription.
     *
     * @param subscriptionId The same id as passed to subscribe.
     * @param result The result of the unsubscribe. Will throw an APSMessagingException on failure if value is null.
     */
    @Reactive
    void unsubscribe(@NotNull ID subscriptionId, @Nullable APSHandler<APSResult> result);

}
