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

import net.olioinfo.eeproperties.EEProperties;


import java.util.HashMap;
import java.util.Properties;


/**
 * <p>Lightweight wrapper to SL4j to exploit EEProperties to handle logging configurations for multiple components and
 * runtime environments. It currently uses SL4J as the primary interface, and configures using Log4j settings.</p>
 *
 * <h3>Basic usage</h3>
 * <pre>
 * Slf4jExt.sConfigureLogging(some.class);
 * </pre>
 *
 * <p>This will configure logging using the files 'log4j-defaults.properties' and 'log4j-development.properties'
 * in the package location (or corresponding external location) where 'some.class' loaded from.</p>
 *
 * <p>Since Slf4j uses EEProperties to manage its configuration files, it automatically gets variable substitution
 * in both property names and values from the System properties and System environment in that order using the
 * standard ${varName} syntax. </p>
 *
 * <p>The default names for the configuration files are:</p>
 * <ul>
 * <li>log4j-defaults.properties</ii>
 * <li>log4j-production.properties</li>
 * <li>log4j-development.properties</li>
 * <li>log4j-test.properties</li>
 * </ul>
 * 
 * <p>See the documentation for EEProperties, the configuration tool used to achieve this. In particular, the
 * runtime environment setting is that specified by the EEProperties bootstrap mechanism.</p>
 *
 * <p>To provide detailed tracing to the System.out device, specify the following: (Does not use logging) </p>
 *
 * <ul><li>-Dnet.olioinfo.slf4j.consoleTracing=true</li></ul>
 *
 *
 *
 * @author Tracy Flynn
 * @version 2.2
 * @since 2.0
 */
public class Slf4jExt {

    /**
     * Singleton instance
     */
    private static Slf4jExt instance = null;


    /**
     * EEProperties instance
     */
    private EEProperties eeProperties = null;

    /**
     * Common properties instance
     */
    private Properties allProperties = new Properties();

    /**
      * Console tracing state
     */
    private boolean consoleTracing = false;


    /**
     * Create an instance of the class
     *
     */
    public Slf4jExt() {
        if ((System.getProperty("net.olioinfo.slf4j.consoleTracing") != null ) && System.getProperty("net.olioinfo.slf4j.consoleTracing").equals("true")) {
            this.consoleTracing = true;
        }
    }
    
    /**
     * Get singleton instance
     */
    public static Slf4jExt singleton() {
        if (Slf4jExt.instance == null) {
            Slf4jExt.instance = new Slf4jExt();
        }
        return Slf4jExt.instance;
    }

    /**
     * Configure logging for the specified package
     *
     * @param klass Class within package to be configured
     */
    public static void sConfigureLogging(Class klass) {
        Slf4jExt.sConfigureLogging(klass,null);
    }


    /**
     * Configure logging for the specified package
     *
     * @param klass Class within package to be configured
     * @param options HashMap of options
     */
    public static void sConfigureLogging(Class klass, HashMap<String,String> options ) {
        Slf4jExt.singleton().configureLogging(klass,options);
    }

    /**
     * Configure logging for the specified package
     *
     * @param klass Class within package to be configured
     * @param options HashMap of options
     */
    public void configureLogging(Class klass, HashMap<String,String> options ) {

        if (options == null) {
            options = new HashMap<String,String>();
        }

        this.eeProperties = new EEProperties(options);

        HashMap<String,String> combinedOptions = new HashMap<String,String>();
        combinedOptions.put("net.olioinfo.eeproperties.configurationFile.prefix","log4j-");
        combinedOptions.put("net.olioinfo.eeproperties.configurationFile.suffix",null);
        combinedOptions.put("net.olioinfo.eeproperties.configurationFile.extension","properties");
        if (options != null) combinedOptions.putAll(options);


        this.eeProperties.loadAndMergeConfigurations(klass,this.allProperties,combinedOptions);

        if (this.consoleTracing) {
            this.allProperties.list(System.out);
        }

        org.apache.log4j.PropertyConfigurator.configure(this.allProperties);


    }

    /**
     * Get the configuration properties
     *
     * @return Properties instance with all the configuration properties
     */
    public Properties getConfiguration() {
        return this.allProperties;
    }


}