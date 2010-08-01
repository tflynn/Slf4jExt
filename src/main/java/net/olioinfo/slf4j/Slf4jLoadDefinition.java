/* Copyright 2009-2010 Tracy Flynn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.olioinfo.slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;


/**
 * Load definition
 *
 * @author Tracy Flynn
 * @since Version 2.2
 */
public class Slf4jLoadDefinition {

    private Class klass = null;

    private Properties properties = new Properties();

    private HashMap<String,String> options = null;

    /**
     * Load definitions
     */
    private static ArrayList<Slf4jLoadDefinition> loadDefinitions = new ArrayList<Slf4jLoadDefinition>();

    public Slf4jLoadDefinition(Class klass, Properties inputProperties , HashMap<String,String> inputOptions) {
        this.klass = klass;
        for (String propertyName : inputProperties.stringPropertyNames() ) {
            this.properties.put(propertyName,inputProperties.getProperty(propertyName));
        }
        for (String option : inputOptions.values()) {
            this.options.put(option,inputOptions.get(option));
        }
        Slf4jLoadDefinition.registerLoadDefinition(this);
    }

    public static void registerLoadDefinition(Slf4jLoadDefinition loadDefinition) {
        Slf4jLoadDefinition.loadDefinitions.add(loadDefinition);
    }


    public static ArrayList<Slf4jLoadDefinition> getLoadDefinitions() {
        return Slf4jLoadDefinition.loadDefinitions;
    }

    public static void resetLoadDefinitions() {
        Slf4jLoadDefinition.loadDefinitions = new ArrayList<Slf4jLoadDefinition>();

    }


    public Properties getProperties() {
        return properties;
    }
}