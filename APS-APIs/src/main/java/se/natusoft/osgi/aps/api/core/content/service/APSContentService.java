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
 *         2011-08-02: Created!
 *         
 */
package se.natusoft.osgi.aps.api.core.content.service;

import javax.jcr.Repository;
import javax.jcr.Session;
import java.util.List;

/**
 * This service just supplies a configured and managed JCR repository.
 */
public interface APSContentService {
    
    /**
     * Returns a content repository.
     */
    public Repository getContentRepository();
        
    /**
     * Returns a content repository for a workspace.
     * 
     * @param workspace The workspace to get the repository for.
     */
    public Repository getContentRepository(String workspace);
    
    /**
     * Returns a list of the available workspaces. If this information
     * is not supported by the implementation null should be returned.
     */
    public List<String> getWorkspaceNames();
    
    /**
     * This allows the service implementation to provide a default repository 
     * session. The idea here is for very simple cases where there really is
     * no auth on the content and every access is done with the same user. 
     * If this is not supported this should return null.
     */
    public Session getDefaultContentSession();
}
