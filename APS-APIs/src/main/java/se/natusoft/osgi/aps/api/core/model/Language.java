/* 
 * 
 * PROJECT
 *     Name
 *         APS APIs
 *     
 *     Code Version
 *         0.11.0
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
public enum Language {
    //
    // Constants
    //
    
    // Codes from http://www.loc.gov/standards/iso639-2/php/code_list.php
    // Language names from: http://translate.google.com/

    // Yes, this list is currently a bit short! It is also currently not used by anything.

    // European
    EN("English", "en"),
    DE("Deutsch", "de"),
    FR("Française", "fr"),
    IT("Italiano", "it"),
    NL("Nederlandse", "nl"),
    // Being Swedish I ofcourse also include the nordic languages :-)
    SV("Svenska", "sv"),
    FI("Suomalainen", "fi"),
    DA("Dansk", "da"),

    // Asian
    JA("日本", "ja"),
    ZH("中文", "zh");


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
    Language(String name, String langCode) {
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
     * Returns a string representation of this object.
     */
    @Override
    public String toString() {
        return getCode();
    }
}
