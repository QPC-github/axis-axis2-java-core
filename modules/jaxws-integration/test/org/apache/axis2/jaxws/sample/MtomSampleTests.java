/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxws.sample;


import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.provider.DataSourceImpl;
import org.test.mtom.ImageDepot;
import org.test.mtom.ObjectFactory;
import org.test.mtom.SendImage;
import org.test.mtom.SendImageResponse;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import java.awt.*;
import java.io.File;

public class MtomSampleTests extends AbstractTestCase {

    private static final QName QNAME_SERVICE = new QName("urn://mtom.test.org", "MtomSampleService");
    private static final QName QNAME_PORT    = new QName("urn://mtom.test.org", "MtomSample");
    private static final String URL_ENDPOINT = "http://localhost:6060/axis2/services/MtomSampleService.MtomSampleServicePort";
    private static final String IMAGE_DIR = System.getProperty("basedir",".")+"/"+"test-resources"+File.separator+"image";   
    
    private static boolean CHECK_VERSIONMISMATCH = true;
    
    public static Test suite() {
        return getTestSetup(new TestSuite(MtomSampleTests.class));
    }
   
    /*
     * Enable attachment Optimization through the SOAPBinding method 
     * -- setMTOMEnabled([true|false])
     * Using SOAP11
     */
    public void testSendImageAttachmentAPI11() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_BINDING, URL_ENDPOINT);
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD);
        
        //Enable attachment optimization
        SOAPBinding binding = (SOAPBinding) dispatch.getBinding();
        binding.setMTOMEnabled(true);
        
        SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
    }
    
    /*
     * Enable attachment Optimization through the MTOMFeature
     * Using SOAP11
     */
    public void testSendImageFeature11() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        MTOMFeature mtom21 = new MTOMFeature();

        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_BINDING, URL_ENDPOINT);
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD, mtom21);
        
        
        SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
    }
    /*
     * Enable attachment optimization using the SOAP11 binding
     * property for MTOM.
     */
    public void testSendImageAttachmentProperty11() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        // Create the JAX-WS client needed to send the request with soap 11 binding
        // property for MTOM
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_MTOM_BINDING, URL_ENDPOINT);
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD);
        
        SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
    }
    
    /*
     * Enable attachment optimization using both the SOAP11 binding
     * property for MTOM and the Binding API
     */
    public void testSendImageAttachmentAPIProperty11() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        // Create the JAX-WS client needed to send the request with soap 11 binding
        // property for MTOM
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_MTOM_BINDING, URL_ENDPOINT);
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD);
        
        
        //Enable attachment optimization
        SOAPBinding binding = (SOAPBinding) dispatch.getBinding();
        binding.setMTOMEnabled(true);
        
        SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
    }
    
    /*
     * Enable attachment optimization using both the SOAP12 binding
     * property for MTOM
     * 
     * Sending SOAP12 message to SOAP11 endpoint will correctly result in exception
     * 
     */
    public void testSendImageAttachmentProperty12() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        // Create the JAX-WS client needed to send the request with soap 11 binding
        // property for MTOM
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_MTOM_BINDING, URL_ENDPOINT);
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD);
        
        try {
            SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
            fail("Was expecting an exception due to sending SOAP12 message to SOAP11 endpoint.");
        } catch (Exception e) {
            assertNotNull(e);
            if (CHECK_VERSIONMISMATCH) {
                assertTrue("Expected SOAPFaultException, but received: "+ e.getClass(),
                           e instanceof SOAPFaultException);
                SOAPFaultException sfe = (SOAPFaultException) e;

                SOAPFault fault = sfe.getFault();

                assertTrue("SOAPFault is null ",
                           fault != null);
                QName faultCode = sfe.getFault().getFaultCodeAsQName();


                assertTrue("Expected VERSION MISMATCH but received: "+ faultCode,
                           new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "VersionMismatch", SOAPConstants.SOAP_ENV_PREFIX).equals(faultCode));

            }
        }
        
        //assertNotNull(response);
        //assertNotNull(response.getOutput().getImageData());
        

	}
    
    /*
     * Enable attachment optimization using both the SOAP12 binding API
     * for MTOM
     * 
     * Sending SOAP12 message to SOAP11 endpoint will correctly result in exception
     * 
     */
    public void testSendImageAttachmentAPI12() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        TestLogger.logger.debug("test: " + getName());
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        // Create the JAX-WS client needed to send the request with soap 11 binding
        // property for MTOM
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_BINDING, URL_ENDPOINT);
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD);
        
        
        //Enable attachment optimization
        SOAPBinding binding = (SOAPBinding) dispatch.getBinding();
        binding.setMTOMEnabled(true);
        
        try {
            SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
            fail("Was expecting an exception due to sending SOAP12 message to SOAP11 endpoint.");
        } catch (Exception e) {
            assertNotNull(e);
            if (CHECK_VERSIONMISMATCH) {
                assertTrue("Expected SOAPFaultException, but received: "+ e.getClass(),
                           e instanceof SOAPFaultException);
                SOAPFaultException sfe = (SOAPFaultException) e;

                SOAPFault fault = sfe.getFault();

                assertTrue("SOAPFault is null ",
                           fault != null);
                QName faultCode = sfe.getFault().getFaultCodeAsQName();


                assertTrue("Expected VERSION MISMATCH but received: "+ faultCode,
              		  new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "VersionMismatch", SOAPConstants.SOAP_ENV_PREFIX).equals(faultCode));

            }
        }
        /*
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        */
    }
    
}
