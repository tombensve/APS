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
 *     Tommy Svensson (tommy@natusoft.se)
 *         Changes:
 *         2012-01-08: Created!
 *         
 */
package se.natusoft.osgi.aps.api.net.rpc.errors;

/**
 * This represents an error in servicing an RPC request.
 */
public interface RPCError {

    /**
     * The type of the error.
     */
    public ErrorType getErrorType();

    /**
     * Returns an error message.
     */
    public String getMessage();

    /**
     * True if there is optional data available. An example of optional data would be a stack trace for example.
     */
    public boolean hasOptionalData();

    /**
     * The optional data.
     */
    public String getOptionalData();
}
