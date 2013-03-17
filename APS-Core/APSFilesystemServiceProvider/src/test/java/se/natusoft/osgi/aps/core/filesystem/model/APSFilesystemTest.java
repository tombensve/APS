/*
 * PROJECT
 *     Name
 *         APS Filesystem Service Provider
 *     
 *     Code Version
 *         0.9.1
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
package se.natusoft.osgi.aps.core.filesystem.model;

import junit.framework.TestCase;

import java.io.File;
import java.io.Writer;

/**
 * Test of APSFilesystemImpl functionallity.
 */
public class APSFilesystemTest extends TestCase {

    private APSFilesystemImpl fs;
    private APSDirectoryImpl tmpDir;

    public APSFilesystemTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File tmpFile = File.createTempFile("APSFilesystemTest", null);
        File tmp = new File(tmpFile.getParentFile(), "APSFilesystemTestROOT");
        tmp.mkdirs();
        this.fs = new APSFilesystemImpl(tmp.getPath(),"test");
        this.tmpDir = new APSDirectoryImpl(this.fs, "");
        tmpFile.delete();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        this.tmpDir.recursiveDelete();
    }

    /**
     * Test of getRootDirectory method, of class APSFilesystemImpl.
     */
    public void testGetRootFolder() {
        System.out.print("GetRootFolderFile...");
        
        APSDirectoryImpl root = this.fs.getRootDirectory();
        assertTrue(root.list().length == 0);
        
        System.out.println("ok");
    }

    public void testCreateDir() throws Exception {
        System.out.print("CreateDir...");
        
        APSDirectoryImpl root = this.fs.getRootDirectory();
        APSDirectoryImpl myDir = root.createDir("mydir");
        APSFileImpl myFile = myDir.createFile("myfile.txt");
        Writer writer = myFile.createWriter();
        writer.write("This is a test file!\n");
        writer.close();
        assertTrue(myDir.list().length == 1);
        assertEquals(myDir.list()[0], "myfile.txt");
        
//        for (APSFileImpl file : myDir.listFiles()) {
//            System.out.println(file);
//        }
        APSDirectoryImpl parent = myDir.getParentFile();
//        System.out.println("Parent file: " + parent);
        APSDirectoryImpl parentOfParent = parent.getParentFile();
        assertTrue(parentOfParent == null);
//        System.out.println("Parent of parent file: " + parentOfParent);
        
        System.out.println("ok");
    }
}
