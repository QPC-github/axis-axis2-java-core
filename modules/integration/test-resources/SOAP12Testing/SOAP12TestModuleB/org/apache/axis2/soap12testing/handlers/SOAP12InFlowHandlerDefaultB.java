package org.apache.axis2.soap12testing.handlers;

/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
 *
 * 
 */

/**
 * Author : Deepal Jayasinghe
 * Date: Jul 26, 2005
 * Time: 2:58:10 PM
 */

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.soap.*;
import org.apache.axis2.soap.impl.llom.SOAPProcessingException;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.AxisFault;


import java.util.Iterator;

public class SOAP12InFlowHandlerDefaultB extends AbstractHandler implements HeaderConstants {
    private OMNamespace attributeNS;
    private boolean attributePresent = false;
    Integer headerBlockPresent = new Integer(0);

    SOAPHeaderBlock headerBlock;


    public void revoke(MessageContext msgContext) {

    }

    public void invoke(MessageContext msgContext) throws AxisFault {
        SOAPEnvelope envelope = msgContext.getEnvelope();
        SOAPFactory factory = OMAbstractFactory.getSOAP12Factory();
        SOAPHeader headerAdd = factory.createSOAPHeader(envelope);
        if (envelope.getHeader() != null) {
            Iterator headerBlocks = envelope.getHeader().examineAllHeaderBlocks();
            while (headerBlocks.hasNext()) {
                try {
                    headerBlock = (SOAPHeaderBlock) headerBlocks.next();
                } catch (ClassCastException e) {
                    continue;
                }
                Iterator attributes = headerBlock.getAttributes();

                if (attributes.hasNext()) {
                    OMAttribute firstAttribute = (OMAttribute) attributes.next();
                    attributeNS = firstAttribute.getNamespace();
                    attributePresent = true;
                }

                String roleValue = headerBlock.getRole();
                boolean mustUnderstand = headerBlock.getMustUnderstand();
                String elementName = headerBlock.getLocalName();
                OMNamespace headerBlockNamespace = headerBlock.getNamespace();

                if (elementName.equals(REQUEST_HEADERBLOCK_NAME)) {
                    if (roleValue == null || roleValue.equals(SAMPLE_ROLE + "/" + ROLE_BY_B) || roleValue.equals(SOAP12_ROLE + "/" + ULTIMATERECEIVER_ROLE) ||
                            roleValue.equals(SOAP12_ROLE + "/" + NEXT_ROLE)) {
                        headerBlock.setLocalName(RESPONSE_HEADERBLOCK_NAME);
                        if (attributePresent)
                            headerBlock.removeAttribute((OMAttribute) headerBlock.getAttributes().next());
                        headerBlockPresent = new Integer(1);
                        msgContext.getOperationContext().setProperty("HEADER_BLOCK_PRESENT", headerBlockPresent, true);
                        headerAdd.addChild(headerBlock);
                        msgContext.getOperationContext().setProperty("HEADER_BLOCK", headerAdd, true);

                    }
                } else {
                    if (roleValue == null || roleValue.equals(SAMPLE_ROLE + "/" + ROLE_BY_B) || roleValue.equals(SAMPLE_ROLE + "/" + ROLE_BY_C) ||
                            roleValue.equals(SOAP12_ROLE + "/" + ULTIMATERECEIVER_ROLE) || roleValue.equals(SOAP12_ROLE + "/" + NEXT_ROLE)) {
                        if (mustUnderstand) {

                            SOAPBody body = factory.getDefaultEnvelope().getBody();
                            if (attributePresent && attributeNS.getName() == "http://schemas.xmlsoap.org/soap/envelope/") {

                            } else {

                                try {
                                    SOAPFault fault = factory.createSOAPFault(body);
                                    SOAPFaultCode code = factory.createSOAPFaultCode(fault);
                                    SOAPFaultValue value = factory.createSOAPFaultValue(code);
                                    value.setText("env:MustUnderstand");
                                    SOAPFaultReason reason = factory.createSOAPFaultReason(fault);
                                    SOAPFaultText text = factory.createSOAPFaultText(reason);
                                    text.setLang("en-US");
                                    text.setText("Header not understood");
                                    reason.setSOAPText(text);
                                    //fault.setReason(reason);
                                    if (roleValue != null && roleValue.equals(SAMPLE_ROLE + "/" + ROLE_BY_B)) {
                                        SOAPFaultNode node = factory.createSOAPFaultNode(fault);
                                        node.setNodeValue(SAMPLE_ROLE + "/" + ROLE_BY_B);
                                        SOAPFaultRole role = factory.createSOAPFaultRole(fault);
                                        role.setRoleValue(SAMPLE_ROLE + "/" + ROLE_BY_B);
                                        msgContext.setProperty(SOAP12Constants.SOAP_FAULT_NODE_LOCAL_NAME, node);
                                        msgContext.setProperty(SOAP12Constants.SOAP_FAULT_ROLE_LOCAL_NAME, role);
                                    }

                                    msgContext.setProperty(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, code);
                                    msgContext.setProperty(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME, reason);

                                } catch (SOAPProcessingException e) {
                                   throw new AxisFault(e);
                                }

                                headerBlock.discard();
                                SOAPHeaderBlock newHeaderBlock = null;
                                try {
                                    newHeaderBlock = envelope.getHeader().addHeaderBlock("NotUnderstood", envelope.getNamespace());
                                    newHeaderBlock.declareNamespace(headerBlockNamespace);
                                    newHeaderBlock.addAttribute("qname", headerBlockNamespace.getName() + ":" + elementName, null);
                                } catch (SOAPProcessingException e) {
                                    //e.printStackTrace();
                                }
                                headerBlockPresent = new Integer(1);
                                msgContext.getOperationContext().setProperty("HEADER_BLOCK_PRESENT", headerBlockPresent, true);
                                headerAdd.addChild(newHeaderBlock);
                                msgContext.getOperationContext().setProperty("HEADER_BLOCK", headerAdd, true);

                                throw new AxisFault("Intentional Failure from SOAP 1.2 testing ...");
                            }
                        } else {
                            headerBlockPresent = new Integer(0);
                        }

                    }
                }
            }
            if (headerBlockPresent.equals(new Integer(0))) {
                msgContext.getOperationContext().setProperty("HEADER_BLOCK_PRESENT", headerBlockPresent, true);
            }
            headerBlockPresent = new Integer(0);
        } else {
            headerBlockPresent = new Integer(0);
            msgContext.getOperationContext().setProperty("HEADER_BLOCK_PRESENT", headerBlockPresent, true);
        }
    }
}

