/* 
 * 
 * PROJECT
 *     Name
 *         APS OSGi Test Tools
 *     
 *     Code Version
 *         1.0.0
 *     
 *     Description
 *         Provides tools for testing OSGi services.
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
 *         2019-08-17: Created!
 *         
 */
package se.natusoft.osgi.aps.test.tools;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is a support for testing deployed bundles using APSOSGiServiceTestTools test container.
 * In this case bundles are deployed, and classes instantiated and managed by APSActivator, and thus
 * not entirely executed under the context of the test method.
 *
 * Make this class statically available for test. Use its tr*() assert methods. These will change
 * 'testOK' status from true to false as soon as an assert fails. It does not do a real assert nor
 * throw any exception since these never reach all the way back to the main test. When all tests
 * have run then check the testOK flag and if false then all failed asserts have left a message
 * in testMessages, which can be displayed with printMessages(). The messages show what failed
 * and also include a stack trace, created by the assert method. This to help trace back to where
 * the failure are.
 *
 * When the tests have run then call printMessages() on this class. If all is OK there will be
 * no messages. After that do a standard assert on the testOK value to make sure test fails
 * on failures.
 *
 * Also note that since these asserts do not stop the execution of the code, multiple failures
 * can be gotten at the same time. Note however that following failures **might** just be due to
 * earlier failure.
 */
@SuppressWarnings( { "unused", "WeakerAccess" } )
public class APSTestResults {

    public boolean testOK = true;
    public List<String> testMessages = new LinkedList<>();

    public void printMessages() {
        this.testMessages.forEach( System.out::println );
    }

    private static String stackTrace( StackTraceElement[] stes ) {
        StringBuilder sb = new StringBuilder();
        for ( StackTraceElement ste : stes ) {
            sb.append( ste.toString() );
            sb.append( "\n" );
        }

        return sb.toString();
    }

    public void trAssertTrue( boolean value ) {
        if ( !value ) {
            this.testOK = false;
            this.testMessages.add( "True: Expected 'true', got 'false'! : " + stackTrace( new Exception().getStackTrace() ) );
        }
    }

    public void trAssertFalse( boolean value ) {
        if ( value ) {
            this.testOK = false;
            this.testMessages.add( "False: Expected 'false', got 'true'! : " + stackTrace( new Exception().getStackTrace() ) );
        }
    }

    public void trAssertEquals( boolean expected, boolean value ) {
        if ( value != expected ) {
            this.testOK = false;
            this.testMessages.add( "Equals: expected '${ expected }', got '" + value + "'! : " + stackTrace( new Exception().getStackTrace() ) );
        }
    }

    public void trAssertEquals( String expected, String value ) {
        if ( !value.equals( expected ) ) {
            this.testOK = false;
            this.testMessages.add( "Equals: expected '${ expected }', got '" + value + "'! : " + stackTrace( new Exception().getStackTrace() ) );
        }
    }

    public void trAssertEquals( Number expected, Number value ) {
        if ( !value.equals( expected ) ) {
            this.testOK = false;
            this.testMessages.add( "Equals: expected '${ expected }', got '" + value + "'! : " + stackTrace( new Exception().getStackTrace() ) );
        }
    }

    public void trAssertEquals( Object expected, Object value ) {
        if ( !value.equals( expected ) ) {
            this.testOK = false;
            this.testMessages.add( "Equals: Expected '${ expected }', got '" + value + "'! : " + stackTrace( new Exception().getStackTrace() ) );
        }
    }

    public void trAssertSame( Object expected, Object value ) {
        if ( value != expected ) {
            this.testOK = false;
            this.testMessages.add( "Same: Expected '${ expected }', got '" + value + "'! : " + stackTrace( new Exception().getStackTrace() ) );
        }
    }

}
