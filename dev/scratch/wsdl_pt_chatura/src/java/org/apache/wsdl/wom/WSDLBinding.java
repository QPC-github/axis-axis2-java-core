/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.wsdl.wom;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;


/**
 * @author chathura@opensource.lk
 *
 */
public interface WSDLBinding extends ExtensibleComponent{
   

    public WSDLInterface getBoundInterface();

    public void setBoundInterface(WSDLInterface boundInterface);

    public List getFaults();

    public void setFaults(List faults);

    public QName getName();

    public void setName(QName name);

    public List getOperations();

    public void setOperations(List operations);

    public URI getTargetNameSpace();

    public void setTargetNameSpace(URI targetNameSpace);
}