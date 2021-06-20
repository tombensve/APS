/*
 * PROJECT
 *     Name
 *         APS Filesystem Service Provider
 *
 *     Code Version
 *         1.0.0
 *
 *     Description
 *         Provides access to a service/application private filesystem that remains until the
 *         service/application specifically deletes it. This is independent of the OSGi server
 *         it is running in (if configured).
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
 *         2011-06-05: Created!
 */
package se.natusoft.aps.core.filesystem.model;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.natusoft.aps.api.core.filesystem.model.APSDirectory;
import se.natusoft.aps.api.core.filesystem.model.APSFile;
import se.natusoft.aps.core.filesystem.model.APSDirectoryImpl;
import se.natusoft.aps.core.filesystem.model.APSFilesystemImpl;

import java.io.File;
import java.io.Writer;

import static junit.framework.TestCase.*;
import static org.junit.Assert.assertEquals;

/**
 * Test of APSFilesystemImpl functionallity.
 */
public class APSFilesystemTest {

    private APSFilesystemImpl fs;
    private APSDirectoryImpl tmpDir;

    @Before
    public void before() throws Exception {
        File tmpFile = File.createTempFile("APSFilesystemTest", null);
        File tmp = new File(tmpFile.getParentFile(), "APSFilesystemTestROOT");
        tmp.mkdirs();
        this.fs = new APSFilesystemImpl(tmp.getPath(),"test");
        this.tmpDir = new APSDirectoryImpl(this.fs, "");
        tmpFile.delete();
    }

    @After
    public void after() throws Exception {
        this.tmpDir.recursiveDelete();
    }

    /**
     * Test of getRootDirectory method, of class APSFilesystemImpl.
     */
    @Test
    public void testGetRootFolder() {
        System.out.print("GetRootFolderFile...");

        APSDirectory root = this.fs.getRootDirectory();
        assertNotNull(root);
        TestCase.assertEquals(0, root.list().length);

        System.out.println("ok");
    }

    @Test
    public void testCreateDir() throws Exception {
        System.out.print("CreateDir...");

        APSDirectory root = this.fs.getRootDirectory();
        APSDirectory myDir = root.createDir("mydir");
        APSFile myFile = myDir.createFile("myfile.txt");
        Writer writer = myFile.createWriter();
        writer.write("This is a test file!\n");
        writer.close();
        TestCase.assertEquals(1, myDir.list().length);
        assertEquals(myDir.list()[0], "myfile.txt");

        APSDirectory parent = myDir.getParentFile();
        APSDirectory parentOfParent = parent.getParentFile();
        assertNull(parentOfParent);

        System.out.println("ok");
    }
}
