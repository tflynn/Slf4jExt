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

import java.io.File;
import net.olioinfo.eeproperties.EEProperties;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.lang.StringBuffer;


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
 * <h3>Logfile location</h3>
 *
 * <p>If SLf4jExt is called with the optional syntax that includes options, then entries representing log file
 * locations will be converted to System properties prior to configuration. 
 * These System properties may then be accessed in logging configuration files using the standard variable syntax ${...}.</p>
 *
 * <p>An EEProperties instance that contains these settings can also be specified. This second method is preferred.
 * It leverages the configuration management, automatic variable expansion and external configuration provided by EEProperties.</p>
 *
 * <p>The simplest example (<em>this is the typical usage</em>):</p>
 *
 * <pre>
 * Slf4jExt.sConfigureLogging(Slf4jExt.class,null,null);
 * </pre>
 *
 * <p>The System setting 'log.dir' will be set to the first of the standard log file locations that is writable. (See below)</p>
 *
 * <p>A more elaborate example:</p>
 *
 * <pre>
 * EEProperties eeProperties = new EEProperties();
 * eeProperties.put("log.dir", "${user.home}/.logs);
 * Slf4jExt slf4jExt = new Slf4jExt();
 * slf4jExt.configureLogging(Slf4jExt.class,null,eeProperties);
 * </pre>
 
 * <p>The System setting 'log.dir' will be set to (the expanded version of) ${user.home}/.logs .</p>
 * 
 * <h4>Settings</h4>
 *
 * <h5>net.olioinfo.slf4j.log.dir.settings.prefix</h5>
 *
 * <p>net.olioinfo.slf4j.log.dir.settings.prefix sets the default prefix for all entries representing log
 * file locations. The default prefix is 'log.dir'.<p>
 *
 * <p>Example</p>
 *
 * <p>log.dir.property.prefix = "mylog.dir" will cause the following properties to be converted to System properties.</p>
 * 
 * <ul>
 * <li> mylog.dir = /var/log</li>
 * <li> mylog.dir.authentication =  ${user.dir}/logs</li>
 * <li> mylog.dir.verification = ${mylog.dir}</li>
 * </ul>
 *
 * <p>These may then be referenced in logging configuration files using ${mylog.dir}, ${mylog.dir.authentication} and ${mylog.dir.verification} respectively.</p>
 *
 * <h5>net.olioinfo.slf4j.log.dir.settings.useStandardLocations</h5>
 * 
 * <p>If the value for 'log.dir' (or the default as specified by net.olioinfo.slf4j.log.dir.settings.prefix) has <strong>not</strong> been set,
 * the setting 'net.olioinfo.slf4j.log.dir.settings.useStandardLocations' makes the system test for the following locations in order.
 * The default for this setting is true.</p>
 * <ul>
 * <li>${user.dir}/logs</li>
 * <li>/var/log</li>
 * <li>${user.home}/logs</li>
 * <li>${user.home}/.logs</li>
 * </ul>
 *
 *
 * <p>The first directory that is present and writable to the process this will be used as the value for 'log.dir'.</p>
 *
 * 
 * <h3>Debugging</h3>
 *
 * <p>To provide detailed tracing to the System.out device, specify the following: (Does not use logging) </p>
 *
 * <ul><li>-Dnet.olioinfo.slf4j.consoleTracing=true</li></ul>
 *
 *<h3>See location of logging directory</h3>
 *
 *<p>To display location of logging directory selected at startup, specify the following:</p>
 *
 * <ul><li>-Dnet.olioinfo.slf4j.showLoggingDir=true</li></ul>
 * 
 * @author Tracy Flynn
 * @version 2.11
 * @since 2.0
 */
public class Slf4jExt {

    /**
     * Property name for log file prefix
     */
    private static final String LOGFILE_DIR_PROPERTYY = "net.olioinfo.slf4j.log.dir.settings.prefix";

    /**
     * Property name for create logging dir
     */
    private static final String LOGFILE_DIR_CREATE_DIR_PROPERTY = "net.olioinfo.slf4j.log.dir.settings.createLogDir";

    
    /**
     * Property name for use of standard logging locations
     */
    private static final String LOGFILE_DIR_USE_STANDARD_LOCATIONS = "net.olioinfo.slf4j.log.dir.settings.useStandardLocations";

    /**
     * The default name of the property for the location of the logging directory
     */
    private static final String LOGFILE_DIR_DEFAULT_PREFIX = "log.dir";

    /**
     * Standard locations for log files
     */
    private static final String[] LOGFILE_DIR_STANDARD_LOCATIONS = {"${user.dir}/logs", "/var/log", "${user.home}/logs", "${user.home}/.logs"};

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
     * Default runtime environment
     */
    private String runtimeEnvironment = "development";


    /**
     * Enable Tomcat servlet container support
     */
    private boolean servletContainerTomcatSupportEnable = true;

    /**
     * Controls the display of the startup message showing logging directory selected
     */
    private boolean showLoggingDir = false;
    
    /**
     * Create an instance of the class
     *
     */
    public Slf4jExt() {
    	
        if ((System.getProperty("net.olioinfo.slf4j.consoleTracing") != null ) && System.getProperty("net.olioinfo.slf4j.consoleTracing").equals("true")) {
            this.consoleTracing = true;
        }
        if ((System.getProperty("net.olioinfo.slf4j.showLoggingDir") != null) && System.getProperty("net.olioinfo.slf4j.showLoggingDir").equalsIgnoreCase("true") ) {
        	this.showLoggingDir = true;
        }
        if (System.getProperty("net.olioinfo.slf4j.servletContainerTomcatSupportEnable") != null ) {
            if (System.getProperty("net.olioinfo.slf4j.servletContainerTomcatSupportEnable").equals("true")) {
                this.servletContainerTomcatSupportEnable = true;
            }
            if (System.getProperty("net.olioinfo.slf4j.servletContainerTomcatSupportEnable").equals("false")) {
                this.servletContainerTomcatSupportEnable = false;
            }
        }
        if (this.consoleTracing) {
            System.out.println(String.format("consoleTrace: SLF4JExt servletContainerTomcatSupportEnable %s", this.servletContainerTomcatSupportEnable ? "true" : "false"));
        }
        
    }
    
    /**
     * Get singleton instance
     */
    public static Slf4jExt singleton() {
        if (Slf4jExt.instance == null) {
            if (EEProperties.testSystemProperty("net.olioinfo.slf4j.consoleTracing","true")) {
                System.out.println(String.format("consoleTrace: SLF4JExt Creating singleton instance"));
            }
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
     * @param eeProperties EEProperties configuration instance
     */
    public static void sConfigureLogging(Class klass, EEProperties eeProperties) {
        Slf4jExt.sConfigureLogging(klass,null,eeProperties);
    }

    /**
     * Configure logging for the specified package
     *
     * @param klass Class within package to be configured
     * @param options HashMap of options
     * @param eeProperties EEProperties configuration instance
    */
    public static void sConfigureLogging(Class klass, HashMap<String,String> options, EEProperties eeProperties ) {
        Slf4jExt.singleton().configureLogging(klass,options,eeProperties);
    }

    /**
     * Configure logging for the specified package
     *
     * @param klass Class within package to be configured
     * @param options HashMap of options
     * @param eeProperties EEProperties configuration instance
     */
    public void configureLogging(Class klass, HashMap<String,String> options, EEProperties eeProperties ) {

        if (eeProperties == null) {
            eeProperties = EEProperties.singleton();
        }

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
            System.out.println("consoleTrace: Sl4fjExt about to dump properties after initial load");
            for (String propertyName : this.allProperties.stringPropertyNames()) {
                System.out.println(String.format("%s=%s",propertyName, this.allProperties.getProperty(propertyName)));
            }
        }

        extractAndSetLoggingDirProperties(options,eeProperties);

        org.apache.log4j.PropertyConfigurator.configure(this.allProperties);

        new Slf4jLoadDefinition(klass,this.allProperties,combinedOptions);
        
        if (this.consoleTracing) {
            System.out.println("consoleTrace: SLF4JExt: finished configuring logging for class " + klass.getName());
        }
    }

    /**
     * Get the configuration properties
     *
     * @return Properties instance with all the configuration properties
     */
    public Properties getConfiguration() {
        return this.allProperties;
    }


    /**
     * Reload all the logging settings as originally loaded
     */
    public void resetLogging() {
        ArrayList<Slf4jLoadDefinition> oldLoadDefinitions = Slf4jLoadDefinition.getLoadDefinitions();
        if (this.consoleTracing) {
            System.out.println("consoleTrace: Slf4jExt: dumping old properties before reload");
            for (String propertyName : this.allProperties.stringPropertyNames()) {
                System.out.println(String.format("%s=%s",propertyName, this.allProperties.getProperty(propertyName)));
            }

        }
        Slf4jLoadDefinition.resetLoadDefinitions();
        for (Slf4jLoadDefinition loadDefinition : oldLoadDefinitions ) {
            Properties oldProperties = loadDefinition.getProperties();
            org.apache.log4j.PropertyConfigurator.configure(oldProperties);
            if (this.consoleTracing) {
                for (String propertyName : this.allProperties.stringPropertyNames()) {
                    System.out.println(String.format("%s=%s",propertyName, this.allProperties.getProperty(propertyName)));
                }
            }
        }
    }

    /**
     * Reload all the logging settings as originally loaded (singleton)
     */
    public static void sResetLogging() {
        Slf4jExt.singleton().resetLogging();;
    }

    /**
     * Get a setting from options and EEProperties instance in order if present
     *
     * @param propertyName Property Name for setting
     * @param options Options HashMap
     * @param eeProperties eeProperties instance
     * @return  Setting or null if not present
     */
    private String getLoggingDirSetting(String propertyName, HashMap<String,String> options, EEProperties eeProperties ) {

        String setting = null;

        if ((eeProperties != null) && (eeProperties.getProperty(propertyName) != null ) ) {
            setting = eeProperties.getProperty(propertyName);
        }
        if ((options != null) && options.containsKey(propertyName)) {
            setting = options.get(propertyName);
        }
        return setting;
    }

    /**
     * Extract and set properties for logging directory settings
     *
     * @param options Options HashMap
     * @param eeProperties eeProperties instance
     */
    private void extractAndSetLoggingDirProperties(HashMap<String,String> options, EEProperties eeProperties ) {

        String loggingPrefix = getLoggingDirSetting(Slf4jExt.LOGFILE_DIR_PROPERTYY, options,eeProperties);
        if (loggingPrefix == null) {
            loggingPrefix = Slf4jExt.LOGFILE_DIR_DEFAULT_PREFIX;
        }

//        boolean createLoggingDir = false;
//        String createLoggingDirSetting = getLoggingDirSetting(Slf4jExt.LOGFILE_DIR_CREATE_DIR_PROPERTY, options,eeProperties);
//        if (createLoggingDirSetting != null) {
//            if (createLoggingDirSetting.equals("false")) {
//                createLoggingDir = false;
//            }
//        }

        boolean useStandardLocations = true;
        String useStandardLocationsSetting = getLoggingDirSetting(Slf4jExt.LOGFILE_DIR_USE_STANDARD_LOCATIONS, options,eeProperties);
        if (useStandardLocationsSetting != null) {
            if (useStandardLocationsSetting.equals("false")) {
               useStandardLocations = false;
            }
        }

        // Extract all the settings
        ArrayList<String> loggingDirSettingsPropertyNames = new ArrayList<String>();
        if (eeProperties != null) {
            for (String key : eeProperties.propertyNames()) {
                if (key.startsWith(loggingPrefix)) {
                    if (!loggingDirSettingsPropertyNames.contains(key)) {
                        loggingDirSettingsPropertyNames.add(key);
                    }
                }
            }
        }

        if (options != null) {
            for (String key : options.keySet()) {
                if (key.startsWith(loggingPrefix)) {
                    if (!loggingDirSettingsPropertyNames.contains(key)) {
                        loggingDirSettingsPropertyNames.add(key);
                    }
                }
            }
        }
        if (this.consoleTracing) {
            if (loggingDirSettingsPropertyNames.size() > 0) {
                System.out.println("consoleTrace: Slf4jExt about to list loggingDirSettingsPropertyNames");
                for (String propertyName : loggingDirSettingsPropertyNames) {
                    System.out.println(String.format("consoleTrace: Slf4jExt propertyName %s",propertyName));
                }
            }
        }

        boolean writableDirectoryFound  = false;
        
        for (String propertyName : loggingDirSettingsPropertyNames) {
            String propertyValue = getLoggingDirSetting(propertyName,options,eeProperties);
            if (propertyValue != null) {
                System.setProperty(propertyName,propertyValue);
                if (this.consoleTracing) {
                	System.out.println(String.format("Slf4jExt: Setting %s to %s", propertyName, propertyValue ));
                }
                File fileLocation = new File(propertyValue);
                boolean issueWarning = false;
                if ( fileLocation.exists() ) {
                    if (fileLocation.canWrite()) {
                        writableDirectoryFound = true;
                    } else {
                        issueWarning = true;
                    }
                }
                else {
                    issueWarning = true;
                }
                if (issueWarning) {
                   System.out.println(String.format("Slf4jExt: Warning: %s isn't accessible for writing", propertyValue ));
                }
            }
        }

        
        // If the prefix value hasn't been set externally, then default
        // the value to the first existing directory in the standard list
        if ( loggingDirSettingsPropertyNames.contains(loggingPrefix) || (System.getProperty(loggingPrefix) != null) ) {
           writableDirectoryFound = true; 
        }
        else  {
            boolean lookForMore = true;
            if (this.consoleTracing) {
                System.out.println(String.format("consoleTrace: Slf4jExt: Current settings: user.dir %s user.home %s",System.getProperty("user.dir"),System.getProperty("user.home")));
            }

            for (String standardLocation : Slf4jExt.LOGFILE_DIR_STANDARD_LOCATIONS) {
                if (lookForMore) {
                    // Make sure to substitute value first
                    String substitutedValue = EEProperties.substituteVariables(standardLocation, null);
                    if (substitutedValue == null) {
                        substitutedValue = standardLocation;
                    }
                    if (this.consoleTracing) {
                        System.out.println(String.format("consoleTrace: Slf4jExt: location %s expands to %s",standardLocation,substitutedValue));
                    }
                    File fileLocation = new File(substitutedValue);
                    if ( fileLocation.exists() && fileLocation.canWrite() ) {
                        System.setProperty(loggingPrefix, substitutedValue);
                        lookForMore = false;
                        writableDirectoryFound = true;
                        if (this.showLoggingDir) {
                        	System.out.println(String.format("Slf4jExt: Defaulting %s to %s", loggingPrefix, substitutedValue ));
                        }
                        if (this.consoleTracing) {
                           System.out.println(String.format("Slf4jExt.extractAndSetLoggingDirProperties directory %s exists and is writable",substitutedValue));
                        }
                    }
                    else {
                        if (this.consoleTracing) {
                           System.out.println(String.format("Slf4jExt.extractAndSetLoggingDirProperties directory %s either doesn't exist or isn't writable",substitutedValue));
                        }
                    }
                }

            }
            if (lookForMore && (!writableDirectoryFound)) {
                if (servletContainerTomcatSupportEnable) {
                    if (this.consoleTracing) {
                        System.out.println(String.format("consoleTrace: Slf4jExt: No writable logging directory found in standard locations, and servlet container support for Tomcat is enabled. Going to check a few places ..." ));
                    }
                    String tomcatDir = servletContainerTomcatSupport();
                    if (tomcatDir == null) {
                        if (this.consoleTracing) {
                            System.out.println(String.format("consoleTrace: Slf4jExt: No writable Tomcat directory found"));
                        }
                    }
                    else {
                        System.setProperty(loggingPrefix, tomcatDir);
                        lookForMore = false;
                        writableDirectoryFound = true;
                        if (this.consoleTracing) {
                            System.out.println(String.format("consoleTrace: Slf4jExt: Defaulting ${log.dir} to writable Tomcat directory found at %s",tomcatDir ));
                        }
                    }
                }
            }
            
        }



        
        if (!writableDirectoryFound) {
            StringBuffer standardLocations = new StringBuffer();
            for (String standardLocation : Slf4jExt.LOGFILE_DIR_STANDARD_LOCATIONS) {
              standardLocations.append(standardLocation);
              standardLocations.append(" ");
            }
            if (servletContainerTomcatSupportEnable) {
              if (System.getProperty(loggingPrefix) != null) {
                standardLocations.append(System.getProperty(loggingPrefix));
              }
            }
            System.out.println("Slf4jExt: Warning: No writable logging directory found in any of the standard locations. " + standardLocations);
        }

    }


    /**
     * Try to figure out if the logging is running in what looks like a Tomcat container and find the log directory
     *
     * @return String Fully qualified log directory or null if none found
     */
    private String servletContainerTomcatSupport() {


        String tomcatLogDir = null;

        try {
            // First, try one level up and look for both ./conf/tomcat-users.xml and ./webapps subdirectories
            String userDir = System.getProperty("user.dir");
            String confFile = userDir + "/../conf/tomcat-users.xml";
            String webappsDir = userDir + "/../webapps";
            File confFileLocation = new File(confFile);
            File webappsDirLocation = new File(webappsDir);
            if ( confFileLocation.exists() && webappsDirLocation.exists() ) {
                String logDir = (new File(userDir + "/../logs")).getCanonicalPath();
                File logDirLocation = new File(logDir);
                if (logDirLocation.exists() && logDirLocation.canWrite()) {
                    tomcatLogDir = logDir;
                    if (this.consoleTracing) {
                        System.out.println(String.format("consoleTrace: Slf4jExt: Found writable Tomcat directory relative to current directory %s",tomcatLogDir));
                    }
                }

            }
            if (tomcatLogDir == null) {
                // Last, try the standard location
                String catalinaHome = System.getenv("CATALINA_HOME");
                String logDir = catalinaHome + "/logs";
                File logDirLocation = new File(logDir);
                if (logDirLocation.exists() && logDirLocation.canWrite()) {
                    tomcatLogDir = logDir;
                    if (this.consoleTracing) {
                        System.out.println(String.format("consoleTrace: Slf4jExt: Found writable Tomcat directory relative to CATALINA_HOME %s",tomcatLogDir));
                    }
                }
            }

        }
        catch (Exception ex) {
            System.out.println("Slf4jExt: Error while trying to find Tomcat log directory. " + ex.toString());
            ex.printStackTrace(System.out);
            tomcatLogDir = null;
        }


        return tomcatLogDir;
    }
    
}