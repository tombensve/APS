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

import java.io.*;

/**
 * This represents a generic container for putting data in. This can be extended for specific types of boxes.
 * See APSJSONBox for an example.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public interface APSBox extends TypedData {


    /**
     * Returns an OutputStream on which to write to the box.
     */
    ObjectOutputStream getContentOutputStream() throws IOException;

    /**
     * Returns an InputStream from which to read from the box.
     */
    ObjectInputStream getContentInputStream() throws IOException;

    /**
     * Returns this size of the box.
     */
    int getSize();

    /**
     * Included since interfaces does not inherit toString().
     */
    String toString();

    //
    // Inner Classes
    //

    /**
     * This defines a factory for creating APSBox:es.
     */
    @SuppressWarnings("unused")
    interface APSBoxFactory<BoxType> {

        /**
         * Returns a new APSBox without content.
         */
        BoxType createBox();

        /**
         * Returns a new APSBox with content.
         *
         * @param content The content of the box to create.
         */
        BoxType createBox(byte[] content);
    }

    /**
     * A factory for creating default APSBox implementation.
     */
    @SuppressWarnings("unused")
    class APSBoxDefaultProviderFactory implements APSBoxFactory<APSBox> {

        /**
         * Returns a new APSBox without content.
         */
        @Override
        public APSBox createBox() {
            return new APSBoxDefaultProvider();
        }

        /**
         * Returns a new APSBox with content.
         *
         * @param content The content of the box to create.
         */
        @Override
        public APSBox createBox(byte[] content) {
            APSBox apsBox = createBox();
            apsBox.setContent(content);
            return apsBox;
        }
    }

    /**
     * This provides a usable, but non required implementation of this interface.
     */
    @SuppressWarnings("WeakerAccess")
    class APSBoxDefaultProvider extends TypedData.Provider implements APSBox {


        //
        // Constructors
        //

        /**
         * Default constructors.
         */
        public APSBoxDefaultProvider() {}

        //
        // Methods
        //

        /**
         * Returns an OutputStream on which to write to the box.
         */
        @Override
        public ObjectOutputStream getContentOutputStream() throws IOException {
            return new ObjectOutputStream(new ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    APSBoxDefaultProvider.this.setContent(toByteArray());
                }
            });
        }

        /**
         * Returns an InputStream from which to read from the box.
         */
        @Override
        public ObjectInputStream getContentInputStream() throws IOException {
            return new ObjectInputStream(new ByteArrayInputStream(getContent()));
        }

        /**
         * Returns this size of the box.
         */
        @Override
        public int getSize() {
            return getContent().length;
        }
    }
}
