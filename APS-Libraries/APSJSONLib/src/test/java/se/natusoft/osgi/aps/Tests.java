/*
 *
 * PROJECT
 *     Name
 *         APS JSON Library
 *
 *     Code Version
 *         1.0.0
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
import se.natusoft.osgi.aps.json.tools.JSONMapConv;
import se.natusoft.osgi.aps.json.tools.JSONToJava;
import se.natusoft.osgi.aps.json.tools.JavaToJSON;
import se.natusoft.osgi.aps.json.tools.SystemOutErrorHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        JSONObjectProvider obj = new JSONObjectProvider(new SystemOutErrorHandler());
        obj.readJSON(bais);
        bais.close();

        assertEquals("bla", obj.getValue("string").toString());
        assertEquals(1234, ((JSONNumberProvider)obj.getValue("number")).toInt());
        assertEquals("null", obj.getValue("null").toString());
        assertEquals(true, ((JSONBooleanProvider)obj.getValue("boolean")).getAsBoolean());
        assertEquals(4, ((JSONArrayProvider)obj.getValue("array")).getAsList().size());
        List<JSONNumberProvider> arrayValues = ((JSONArrayProvider)obj.getValue("array")).getAsList(JSONNumberProvider.class);
        assertEquals(1, arrayValues.get(0).toInt());
        assertEquals(2, arrayValues.get(1).toInt());
        assertEquals(3, arrayValues.get(2).toInt());
        assertEquals(4, arrayValues.get(3).toInt());
    }

    @Test
    public void readEmptyObject() throws Exception {
        String json = "{\n}";
        ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes());
        JSONObjectProvider obj = new JSONObjectProvider(new SystemOutErrorHandler());
        obj.readJSON(bais);
        bais.close();

        assertEquals(0, obj.getValueNames().size());
    }

    @Test
    public void writeJSON() throws Exception {
        JSONObjectProvider obj = new JSONObjectProvider();
        obj.setValue("string", new JSONStringProvider("bla"));
        obj.setValue("number", new JSONNumberProvider(1234));
        obj.setValue("null", new JSONNullProvider());
        obj.setValue("boolean", new JSONBooleanProvider(true));
        JSONArrayProvider array = new JSONArrayProvider();
        array.addValue(new JSONNumberProvider(1));
        array.addValue(new JSONNumberProvider(2));
        array.addValue(new JSONNumberProvider(3));
        array.addValue(new JSONNumberProvider(4));
        obj.setValue("array", array);

        System.out.println("Readable format:");
        obj.writeJSON(System.out, false);
        System.out.println("");
        System.out.println("Compact format:");
        obj.writeJSON(System.out, true);
        System.out.println("");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        obj.writeJSON(baos, true);
        baos.close();
        String result = baos.toString();
        // Note that we cannot assume the order of things so we cannot verify as one string.
        assertTrue(result.contains("\"string\": \"bla\""));
        assertTrue(result.contains("\"boolean\": true"));
        assertTrue(result.contains("\"number\": 1234"));
        assertTrue(result.contains("\"null\": null"));
        assertTrue(result.contains("\"array\": [1, 2, 3, 4]"));
    }

    @Test
    public void conversions() throws Exception {
        JSONObjectProvider obj = new JSONObjectProvider(new SystemOutErrorHandler());
        obj.setValue("string", new JSONStringProvider("bla"));
        obj.setValue("number", new JSONNumberProvider(1234));
        obj.setValue("boolean", new JSONBooleanProvider(true));
        JSONArrayProvider array = new JSONArrayProvider();
        array.addValue(new JSONNumberProvider(1));
        array.addValue(new JSONNumberProvider(2));
        array.addValue(new JSONNumberProvider(3));
        array.addValue(new JSONNumberProvider(4));
        obj.setValue("array", array);
        JSONObjectProvider name = new JSONObjectProvider();
        name.setValue("firstName", new JSONStringProvider("Tommy"));
        name.setValue("lastName", new JSONStringProvider("Svensson"));
        obj.setValue("name", name);

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

        JSONObjectProvider convertedObj = JavaToJSON.convertObject(testBean);
        convertedObj.writeJSON(System.out, true);
        System.out.println();

        assertEquals("bla", convertedObj.getValue("string").toString());
        assertEquals(1234, ((JSONNumberProvider)convertedObj.getValue("number")).toInt());
        assertEquals(true, ((JSONBooleanProvider)convertedObj.getValue("boolean")).getAsBoolean());
        assertEquals(4, ((JSONArrayProvider)convertedObj.getValue("array")).getAsList().size());
        List<JSONNumberProvider> arrayValues = ((JSONArrayProvider)convertedObj.getValue("array")).getAsList(JSONNumberProvider.class);
        assertEquals(1, arrayValues.get(0).toInt());
        assertEquals(2, arrayValues.get(1).toInt());
        assertEquals(3, arrayValues.get(2).toInt());
        assertEquals(4, arrayValues.get(3).toInt());
        JSONObjectProvider convertedName = (JSONObjectProvider)convertedObj.getValue("name");
        assertNotNull(convertedName);
        assertEquals("Tommy", convertedName.getValue("firstName").toString());
        assertEquals("Svensson", convertedName.getValue("lastName").toString());

        System.out.println("Success in converting back to JSON!");
    }

    @Test
    public void mapTests() throws Exception {
        String json =
                "{" +
                        "\"Developer\": \"tommy\", " +
                        "\"Likes Groovy\": true, " +
                        "\"Full name\": [\"Tommy\", \"Bengt\", \"Svensson\"], " +
                        "\"subObject\": {\"content\": 12345}" +
                        "}";

        Map<String, Object> values = JSONMapConv.jsonObjectToMap(json);

        assertEquals("Bad value for 'Developer'!", "tommy", values.get("Developer"));
        assertEquals("Bad value for 'Likes Groovy'!", true, values.get("Likes Groovy"));
        Object[] fullName = (Object[])values.get("Full name");
        assertEquals("Bad number of entries in 'Full name'!", 3, fullName.length);
        assertEquals("", "Tommy", fullName[0]);
        assertEquals("", "Bengt", fullName[1]);
        assertEquals("", "Svensson", fullName[2]);
        //noinspection unchecked
        Map<String, Object> subMap = (Map<String, Object>)values.get("subObject");
        assertEquals("Bad data in subObject.content!", (short)12345, subMap.get("content"));

        System.out.println("Success in converting JSON to a Map!");

        String json2 = JSONMapConv.mapToJSONObjectString(values);

        assertTrue(json2.contains("\"Developer\": \"tommy\""));
        assertTrue(json2.contains("Tommy"));
        assertTrue(json2.contains("Bengt"));
        assertTrue(json2.contains("Svensson"));
        assertTrue(json2.contains("Full name"));
        assertTrue(json2.contains("Likes Groovy"));
        assertTrue(json2.contains("true"));
        assertTrue(json2.contains("content"));
        assertTrue(json2.contains("12345"));

        System.out.println("Orig:            " + json);
        System.out.println("To Map and back: " + json2);
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
