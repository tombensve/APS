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
 *         2011-06-05: Created!
 *         
 */
package se.natusoft.osgi.aps.api.core.model;

/**
 * This class represents a language.
 */
public class Language {
    //
    // Constants
    //
    
    // Codes from http://www.loc.gov/standards/iso639-2/php/code_list.php
    // Language names from: http://translate.google.com/
    
    public static final Language EN = new Language("English", "en"); 
    public static final Language DE = new Language("Deutsch", "de");
    public static final Language FR = new Language("Française", "fr");
    public static final Language IT = new Language("Italiano", "it");
    public static final Language NL = new Language("Nederlandse", "nl");
    public static final Language JA = new Language("日本", "ja");
    public static final Language ZH = new Language("中文", "zh");
    // Being Swedish I ofcourse also include the nordic languages :-)
    public static final Language SV = new Language("Svenska", "sv");
    public static final Language FI = new Language("Suomalainen", "fi");
    public static final Language DA = new Language("Dansk", "da");
    
    //
    // private Members
    //
    
    /** The name of the language. */
    private String name;
    
    /** The language code. */
    private String langCode;
    
    //
    // Constructors
    //
        
    /**
     * Creates a new Language instance.
     * 
     * @param name The name of the languge (the language of the name is unspecified!).
     * @param langCode The 2 character language code.
     */
    public Language(String name, String langCode) {
        this.name = name;
        this.langCode = langCode;
    }
    
    //
    // Methods
    //
    
    /**
     * Returns the language code of this language.
     */
    public String getCode() {
        return this.langCode;
    }
    
    /**
     * Returns the name of the language.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns a hashcode of this object.
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.langCode != null ? this.langCode.hashCode() : 0);
        return hash;
    }
    
    /**
     * Compare this object with another for equality.
     * 
     * @param obj The object to compare to.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Language)) {
            return false;
        }
        Language lang = (Language)obj;
        return this.langCode.equals(lang.langCode);
    }
    
    /**
     * Returns a string representation of this object.
     */
    @Override
    public String toString() {
        return getCode();
    }
}
