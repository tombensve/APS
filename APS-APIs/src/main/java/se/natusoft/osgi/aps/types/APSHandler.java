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
package se.natusoft.osgi.aps.types;

import se.natusoft.docutations.Nullable;

/**
 * Generic handler api inspired by Vert.x.
 *
 * ====================================================================================
 * Note to self (and possibly others):
 * I tried to be smart and add inner class and static methods to automatically
 * submit callbacks to APSExecutor thread pool. That actually wasn't smart at all!!
 * It was rather stupid. Most implementations are done in Groovy. Result handlers
 * are in general coerced Groovy closures. When the closure gets executed on another
 * thread is has lost some context and can no longer reference things outside of
 * the closure. Indifferent from Java, Groovy can reference internal method variables.
 * These fail when executed from another thread.
 *
 * Having client passed code (lambda / closure) submitted to a thread pool is a very
 * bad idea! A service should never make such decisions for clients!!
 *
 * When `handler.handle(result)` is done the call stack depth will increase and the
 * callback will execute before service method call returns. If that is problematic
 * for the calling code then it, when in the callback and having result, should
 * submit some work code to APSExecutor and let callback return, which will also
 * make service call return. Depending on what you need to do with the received
 * result, the result could be passed to internal method variable of same type,
 * without submitting to thread pool, and when service call returns the value
 * is there and code can continue work with it.
 *
 * If you absolutely never need to do the call in a threaded situation then
 * dont use a callback handler, just return result in method call. There is
 * no requirement to always use handler callbacks when there is no need!
 * ====================================================================================
 *
 * @param <T> The type of a potential value to handle.
 */
public interface APSHandler<T> {

    /**
     * Does the handling.
     *
     * @param value A value to handle.
     */
    void handle( @Nullable T value );

}
