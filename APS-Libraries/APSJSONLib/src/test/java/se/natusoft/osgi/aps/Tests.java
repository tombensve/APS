/* 
 * 
 * PROJECT
 *     Name
 *         APS JSON Library
 *     
 *     Code Version
 *         0.9.2
 *     
 *     Description
 *         Provides a JSON parser and creator. Please note that this bundle has no dependencies to any
 *         other APS bundle! It can be used as is without APS in any Java application and OSGi container.
 *         The reason for this is that I do use it elsewhere and don't want to keep 2 different copies of
 *         the code. OSGi wise this is a library. All packages are exported and no activator nor services
 *         are provided.
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
 *         2011-10-07: Created!
 *         
 */
package se.natusoft.osgi.aps;

import org.junit.*;
import se.natusoft.osgi.aps.json.*;
import se.natusoft.osgi.aps.json.tools.JSONToJava;
import se.natusoft.osgi.aps.json.tools.JavaToJSON;
import se.natusoft.osgi.aps.json.tools.SystemOutErrorHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Tests {
    
    public Tests() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void readJSON() throws Exception {
        String json = "{" +
                "\"string\": \"bla\"," +
                "\"number\":1234," +
                "\"null\": null," +
                "\"boolean\": true," +
                "\"array\": [1, 2, 3, 4]," +
                "\"array2\":[]," +
                "\"array3\": [ ]" +
                "}";
        ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes());
        JSONObject obj = new JSONObject(new SystemOutErrorHandler());
        obj.readJSON(bais);
        bais.close();

        assertEquals("bla", obj.getProperty("string").toString());
        assertEquals(1234, ((JSONNumber)obj.getProperty("number")).toInt());
        assertEquals("null", obj.getProperty("null").toString());
        assertEquals(true, ((JSONBoolean)obj.getProperty("boolean")).getAsBoolean());
        assertEquals(4, ((JSONArray)obj.getProperty("array")).getAsList().size());
        List<JSONNumber> arrayValues = ((JSONArray)obj.getProperty("array")).getAsList(JSONNumber.class);
        assertEquals(1, arrayValues.get(0).toInt());
        assertEquals(2, arrayValues.get(1).toInt());
        assertEquals(3, arrayValues.get(2).toInt());
        assertEquals(4, arrayValues.get(3).toInt());
    }

    @Test
    public void readEmptyObject() throws Exception {
        String json = "{\n}";
        ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes());
        JSONObject obj = new JSONObject(new SystemOutErrorHandler());
        obj.readJSON(bais);
        bais.close();

        assertEquals(0, obj.getPropertyNames().size());
    }

    @Test
    public void writeJSON() throws Exception {
        JSONObject obj = new JSONObject();
        obj.addProperty("string", new JSONString("bla"));
        obj.addProperty("number", new JSONNumber(1234));
        obj.addProperty("null", new JSONNull());
        obj.addProperty("boolean", new JSONBoolean(true));
        JSONArray array = new JSONArray();
        array.addValue(new JSONNumber(1));
        array.addValue(new JSONNumber(2));
        array.addValue(new JSONNumber(3));
        array.addValue(new JSONNumber(4));
        obj.addProperty("array", array);

        System.out.println("Readable format:");
        obj.writeJSON(System.out, false);
        System.out.println("");
        System.out.println("Compact format:");
        obj.writeJSON(System.out, true);
        System.out.println("");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        obj.writeJSON(baos, true);
        baos.close();
        assertEquals("{\"string\": \"bla\", \"boolean\": true, \"number\": 1234, \"null\": null, \"array\": [1, 2, 3, 4]}", baos.toString());
    }
    
    @Test 
    public void conversions() throws Exception {
        JSONObject obj = new JSONObject(new SystemOutErrorHandler());
        obj.addProperty("string", new JSONString("bla"));
        obj.addProperty("number", new JSONNumber(1234));
        obj.addProperty("boolean", new JSONBoolean(true));
        JSONArray array = new JSONArray();
        array.addValue(new JSONNumber(1));
        array.addValue(new JSONNumber(2));
        array.addValue(new JSONNumber(3));
        array.addValue(new JSONNumber(4));
        obj.addProperty("array", array);
        JSONObject name = new JSONObject();
        name.addProperty("firstName", new JSONString("Tommy"));
        name.addProperty("lastName", new JSONString("Svensson"));
        obj.addProperty("name", name);
        
        obj.writeJSON(System.out, true);
        System.out.println();

        TestBean testBean = JSONToJava.convert(obj, TestBean.class);

        assertEquals("bla", testBean.getString());
        assertEquals(1234, testBean.getNumber());
        assertEquals(true, testBean.getBoolean());
        assertEquals(4, testBean.getArray().length);
        assertEquals(1, testBean.getArray()[0]);
        assertEquals(2, testBean.getArray()[1]);
        assertEquals(3, testBean.getArray()[2]);
        assertEquals(4, testBean.getArray()[3]);
        assertEquals("Tommy", testBean.getName().getFirstName());
        assertEquals("Svensson", testBean.getName().getLastName());

        System.out.println("Success in converting to Java!");
        
        JSONObject convertedObj = JavaToJSON.convertObject(testBean);
        convertedObj.writeJSON(System.out, true);
        System.out.println();

        assertEquals("bla", convertedObj.getProperty("string").toString());
        assertEquals(1234, ((JSONNumber)convertedObj.getProperty("number")).toInt());
        assertEquals(true, ((JSONBoolean)convertedObj.getProperty("boolean")).getAsBoolean());
        assertEquals(4, ((JSONArray)convertedObj.getProperty("array")).getAsList().size());
        List<JSONNumber> arrayValues = ((JSONArray)convertedObj.getProperty("array")).getAsList(JSONNumber.class);
        assertEquals(1, arrayValues.get(0).toInt());
        assertEquals(2, arrayValues.get(1).toInt());
        assertEquals(3, arrayValues.get(2).toInt());
        assertEquals(4, arrayValues.get(3).toInt());
        JSONObject convertedName = (JSONObject)convertedObj.getProperty("name");
        assertNotNull(convertedName);
        assertEquals("Tommy", convertedName.getProperty("firstName").toString());
        assertEquals("Svensson", convertedName.getProperty("lastName").toString());
        
        System.out.println("Success in converting back to JSON!");
    }
    
    //
    // Inner Classes
    //
    
    public static class NameBean {
        private String firstName;
        private String lastName;
        
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        public String getFirstName() {
            return this.firstName;
        }
        
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        
        public String getLastName() {
            return this.lastName;
        }
    }
    
    public static class TestBean {
        private String string;
        private int number;
        private boolean bool;
        private int[] ints;
        private NameBean name;
        
        public void setString(String string) {
            this.string = string;            
        }
        
        public String getString() {
            return this.string;
        }
        
        public void setNumber(int number) {
            this.number = number;
        }
        
        public int  getNumber() {
            return this.number;
        }
        
        public void setBoolean(boolean bool) {
            this.bool = bool;
        }
        
        public boolean getBoolean() {
            return this.bool;
        }
        
        public void setArray(int[] ints) {
            this.ints = ints;
        }
        
        public int[] getArray() {
            return this.ints;
        }

        public void setName(NameBean name) {
            this.name = name;
        }

        public NameBean getName() {
            return this.name;
        }
    }
}
