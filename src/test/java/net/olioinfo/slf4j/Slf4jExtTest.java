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

import org.testng.Assert;
import org.testng.annotations.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TestNG Test suite for Slf4jExt
 *
 * @author Tracy Flynn
 * @since Jun 27, 2010
 */
@Test
public class Slf4jExtTest {

    public void testConfiguration() {
        HashMap<String,String> testOptions = new HashMap<String,String>();
        testOptions.put("net.olioinfo.eeproperties.configurationFile.prefix","test-log4j-");
        Slf4jExt slf4jExt = new Slf4jExt();
        slf4jExt.configureLogging(Slf4jExt.class,testOptions);

        Properties properties = slf4jExt.getConfiguration();

        for (Enumeration e = properties.propertyNames() ; e.hasMoreElements() ; ) {
            String currentName = (String) e.nextElement();
            String msg = String.format("%s = %s",currentName ,properties.get(currentName));
            System.out.println(msg);
        }

        Logger logger = LoggerFactory.getLogger(Slf4jExt.class);

        logger.debug("This one should show up on the console");
        logger.trace("This one should not show up on the console");

    }
}

