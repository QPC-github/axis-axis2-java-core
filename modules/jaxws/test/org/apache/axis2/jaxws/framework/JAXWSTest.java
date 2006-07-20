/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.framework;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axis2.jaxws.DispatchTestSuite;
import org.apache.axis2.jaxws.description.ServiceDescriptionTests;
import org.apache.axis2.jaxws.description.WSDLDescriptionTests;
import org.apache.axis2.jaxws.description.WSDLTests;
import org.apache.axis2.jaxws.exception.ExceptionFactoryTests;
import org.apache.axis2.jaxws.handler.HandlerChainProcessorTests;
import org.apache.axis2.jaxws.message.BlockTests;
import org.apache.axis2.jaxws.message.MessageTests;
import org.apache.axis2.jaxws.message.SAAJConverterTests;
import org.apache.axis2.jaxws.message.XMLStreamReaderSplitterTests;
import org.apache.axis2.jaxws.provider.*;

public class JAXWSTest extends TestCase {
    /**
     * suite
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        
        // Add each of the test suites
        suite = DispatchTestSuite.addTestSuites(suite);
        
        
        suite.addTestSuite(BlockTests.class);
        suite.addTestSuite(MessageTests.class);
        suite.addTestSuite(SAAJConverterTests.class);
        suite.addTestSuite(XMLStreamReaderSplitterTests.class);
        
        suite.addTestSuite(WSDLTests.class);
        suite.addTestSuite(ServiceDescriptionTests.class);
        suite.addTestSuite(WSDLDescriptionTests.class);
        
        suite.addTestSuite(HandlerChainProcessorTests.class);
        
        suite.addTestSuite(StringProviderTests.class);
        suite.addTestSuite(SourceProviderTests.class);
        
        suite.addTestSuite(ExceptionFactoryTests.class);

        // Start (and stop) the server only once for all the tests
        TestSetup testSetup = new TestSetup(suite) {
            public void setUp() {
                System.out.println("Starting the server.");
                StartServer startServer = new StartServer("server1");
                startServer.testStartServer();
            }
            public void tearDown() {
                System.out.println("Stopping the server");
                StopServer stopServer = new StopServer("server1");
                stopServer.testStopServer();
            }
        };
        return testSetup;
    }
}
