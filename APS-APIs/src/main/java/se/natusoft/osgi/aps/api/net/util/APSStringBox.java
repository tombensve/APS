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
 *         2016-02-27: Created!
 *
 */
package se.natusoft.osgi.aps.api.net.util;

/**
 * This extends APSBox with a String box.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public interface APSStringBox extends APSBox {

    /**
     * Set content as a String.
     *
     * @param content The string to set.
     */
    void setStringContent(String content);

    /**
     * Gets the content as a String.
     */
    String getStringContent();

    //
    // Inner Classes
    //

    /**
     * Factory API for creating APSStringBox instances.
     */
    @SuppressWarnings("unused")
    interface APSStringBoxFactory extends APSBoxFactory<APSStringBox> {

        /**
         * Creates a new APSStringBox with a content String.
         *
         * @param content The content string to set.
         */
        APSStringBox createStringBox(String content);
    }

    /**
     * An APSStringBoxFactory implementation creating APSStringBoxDefaultProvider instances.
     */
    @SuppressWarnings("unused")
    class APSStringBoxDefaultProviderFactory implements APSStringBoxFactory {

        /**
         * Creates a new APSStringBox with a content String.
         *
         * @param content The content string to set.
         */
        @Override
        public APSStringBox createStringBox(String content) {
            APSStringBox box = createBox();
            box.setStringContent(content);
            return box;
        }

        /**
         * Returns a new APSBox without content.
         */
        @Override
        public APSStringBox createBox() {
            return new APSStringBoxDefaultProvider();
        }

        /**
         * Returns a new APSBox with content.
         *
         * @param content The content of the box to create.
         */
        @Override
        public APSStringBox createBox(byte[] content) {
            APSStringBox box = createBox();
            box.setContent(content);
            return box;
        }
    }

    /**
     * Provides a default implementation of APSStringBox.
     */
    class APSStringBoxDefaultProvider extends APSBoxDefaultProvider implements APSStringBox {

        /**
         * Set content as a String.
         *
         * @param content The string to set.
         */
        @Override
        public void setStringContent(String content) {
            setContent(content.getBytes());
        }

        /**
         * Gets the content as a String.
         */
        @Override
        public String getStringContent() {
            return new String(getContent());
        }
    }
}
