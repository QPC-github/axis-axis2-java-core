/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.axis2.jaxws.server.dispatcher;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.message.Block;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.factory.BlockFactory;
import org.apache.axis2.jaxws.message.factory.JAXBBlockFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperException;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MapperImpl implements Mapper {
	private static int SIZE = 1;
	private static final Log log = LogFactory.getLog(MapperImpl.class);
	public MapperImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.server.dispatcher.Mapper#getParameterData(org.apache.axis2.jaxws.core.MessageContext, java.lang.reflect.Method)
	 */
	public Object[] getInputParameterData(MessageContext mc, Method javaMethod) throws JAXBException, MessageException, XMLStreamException, JAXBWrapperException {
		
		Message msg = mc.getMessage();
		EndpointDescription ed = getEndpointDescription(mc);
		Class[] paramTypes = javaMethod.getParameterTypes();
		if(paramTypes == null){
			//Method has no input parameters.
			return null;
		}
		if(paramTypes.length == 0){
			return null;
		}
		if(isSEIDocLitBare(ed)){
			//Create the jaxbcontext for input parameter, for non wrap case there should be only one input param which is the Request Wrapper
			if(paramTypes !=null && paramTypes.length >SIZE){
				if (log.isDebugEnabled()) {
		            log.debug("As per WS-I compliance, Multi part WSDL not allowed for Doc/Lit NON Wrapped request, Method invoked has multiple input parameter");
		        }
				throw ExceptionFactory.makeWebServiceException("As per WS-I compliance, Multi part WSDL not allowed for Doc/Lit NON Wrapped request, Method invoked has multiple input parameter");
			}
			if(paramTypes !=null){
				JAXBContext ctx = JAXBContext.newInstance(new Class[]{paramTypes[0]});
				BlockFactory factory = (BlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);
				Block block = msg.getBodyBlock(0, ctx, factory);
				Object obj = block.getBusinessObject(true);
				return new Object[]{obj};
			}
		}
		
		if(isSEIDocLitWrapped(ed)){
			OperationDescription opDesc = mc.getOperationDescription();
            
            JAXBContext jbc = createJAXBContext(opDesc);
            BlockFactory factory = (BlockFactory) FactoryRegistry.getFactory(JAXBBlockFactory.class);
        
            Block wrapper = msg.getBodyBlock(0, jbc, factory);
            
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
            
            String[] webParamNames = opDesc.getWebParamNames();
            ArrayList<String> elements = new ArrayList<String>(Arrays.asList(webParamNames));

            Object param = wrapper.getBusinessObject(true);
            Object[] contents = wrapperTool.unWrap(param, elements);
            return contents;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.server.dispatcher.Mapper#getOutputParameterBlock(org.apache.axis2.jaxws.core.MessageContext, java.lang.Object)
	 */
	public Block getOutputParameterBlock(MessageContext mc, Object response, Method method) throws JAXBException, ClassNotFoundException, JAXBWrapperException, MessageException{
		EndpointDescription ed = getEndpointDescription(mc);
		OperationDescription opDesc = getOperationDescription(mc);
		Class returnType = getReturnType(method);
		if(isSEIDocLitBare(ed)){
			if(returnType.isAssignableFrom(response.getClass())){
				
				BlockFactory bfactory = (BlockFactory) FactoryRegistry.getFactory(
						JAXBBlockFactory.class);
				JAXBContext ctx = JAXBContext.newInstance(new Class[]{returnType});
				Block block = bfactory.createFrom(response, ctx, null);
				return block;
			}
			else{
				String webResult = opDesc.getWebResultName();
				JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
				ArrayList<String> elements = new ArrayList<String>(Arrays.asList(webResult));
				
	            Object[] contents = wrapperTool.unWrap(response, elements);
	            for(Object obj:contents){
	            	if(returnType.getClass().isAssignableFrom(obj.getClass())){
	            		BlockFactory bfactory = (BlockFactory) FactoryRegistry.getFactory(
	    						JAXBBlockFactory.class);
	    				JAXBContext ctx = JAXBContext.newInstance(new Class[]{returnType});
	    				Block block = bfactory.createFrom(response, ctx, null);
	    				return block;
	            	}
	            }
			}
			
			
		}
		if(isSEIDocLitWrapped(ed)){
			
	            //We'll need a JAXBContext to marshall the response object(s).
	            JAXBContext jbc = createJAXBContext(opDesc);
	            BlockFactory bfactory = (BlockFactory) FactoryRegistry.getFactory(
	                    JAXBBlockFactory.class);
	            
	            String responseWrapper = opDesc.getResponseWrapperClassName();
	            Class responseWrapperClass = null;
	           
	            responseWrapperClass = Class.forName(responseWrapper, false, Thread.currentThread().getContextClassLoader());
	            
	            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();

	            String webResult = opDesc.getWebResultName();
	            ArrayList<String> responseParams = new ArrayList<String>();
	            responseParams.add(webResult);
	 
	            ArrayList<String> elements = new ArrayList<String>();
	            elements.add(webResult);
	            
	            Map<String, Object> responseParamValues = new HashMap<String, Object>();
	            responseParamValues.put(webResult, response);
	            
	            Object wrapper = wrapperTool.wrap(responseWrapperClass, 
	                    responseWrapper, responseParams, responseParamValues);
	            
	            Block block = bfactory.createFrom(wrapper ,jbc, null);
	            return block;
	        
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.server.dispatcher.Mapper#getJavaMethod(org.apache.axis2.jaxws.core.MessageContext)
	 */
	public Method getJavaMethod(MessageContext mc, Class serviceImplClass) {
		 QName opName = mc.getOperationName();
		 
	        if (opName == null)
	            // TODO: NLS
	            throw ExceptionFactory.makeWebServiceException("Operation name was not set");
	        
	        String localPart = opName.getLocalPart();
	        Method[] methods = serviceImplClass.getMethods();
	        for (int i = 0; i < methods.length; ++i) {
	            if (localPart.equals(methods[i].getName()))
	                return methods[i];
	        }
	        
	        if (log.isDebugEnabled()) {
	            log.debug("No Java method found for the operation");
	        }
	        // TODO: NLS
	        throw ExceptionFactory.makeWebServiceException("No Java method was found for the operation");
	}

	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.server.dispatcher.Mapper#getMessageContext(java.lang.reflect.Method, java.lang.Object[])
	 */
	public MessageContext getMessageContext(Method javaMethod, Object[] args) {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.server.dispatcher.Mapper#getParamNames(java.lang.reflect.Method, java.lang.Object[])
	 */
	public ArrayList<String> getParamNames(Method method, Object[] objects){
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.axis2.jaxws.server.dispatcher.Mapper#getParamValues(java.lang.Object[], java.util.ArrayList)
	 */
	public Map<String, Object> getParamValues(Object[] objects, ArrayList<String> names){
		return null;
	}
	
	private EndpointDescription getEndpointDescription(MessageContext mc){
  	  ServiceDescription sd = mc.getServiceDescription();
        EndpointDescription[] eds = sd.getEndpointDescriptions();
        return eds[0];
  }
  
  private boolean isSEIDocLitBare(EndpointDescription ed){
		SOAPBinding.ParameterStyle style = ed.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
		return style == SOAPBinding.ParameterStyle.BARE;
	}
	
	private  boolean isSEIDocLitWrapped(EndpointDescription ed){
		SOAPBinding.ParameterStyle style = ed.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
		return style == SOAPBinding.ParameterStyle.WRAPPED;
	} 
	
	private JAXBContext createJAXBContext(OperationDescription opDesc) {
        // This will only support Doc/Lit Wrapped params for now.
        try {
            String wrapperClass = opDesc.getRequestWrapperClassName();
            if (wrapperClass != null) {
                String wrapperPkg = wrapperClass.substring(0, wrapperClass.lastIndexOf("."));
                JAXBContext jbc = JAXBContext.newInstance(wrapperPkg);
                return jbc;
            }
            else {
                throw ExceptionFactory.makeWebServiceException("");
            }
        } catch (JAXBException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
    }
	
	private OperationDescription getOperationDescription(MessageContext mc) {
        ServiceDescription sd = mc.getServiceDescription();
        EndpointDescription[] eds = sd.getEndpointDescriptions();
        EndpointDescription ed = eds[0];
        EndpointInterfaceDescription eid = ed.getEndpointInterfaceDescription();
        
        OperationDescription[] ops = eid.getOperation(mc.getOperationName());
        OperationDescription op = ops[0];
        
        if (log.isDebugEnabled()) {
            log.debug("wsdl operation: " + op.getName());
            log.debug("   java method: " + op.getJavaMethodName());
        }
        
        return op;        
    }
	
	private Class getReturnType(Method seiMethod){
		Class returnType = seiMethod.getReturnType();
		//pooling implementation
		if(Response.class.isAssignableFrom(returnType)){
			Type type = seiMethod.getGenericReturnType();
			ParameterizedType pType = (ParameterizedType) type;
			return (Class)pType.getActualTypeArguments()[0];	
		}
		//Callback Implementation
		if(Future.class.isAssignableFrom(returnType)){
			Type[] type = seiMethod.getGenericParameterTypes();
			Class parameters[]= seiMethod.getParameterTypes();
			int i=0;
			for(Class param:parameters){
				if(AsyncHandler.class.isAssignableFrom(param)){
					ParameterizedType pType = (ParameterizedType)type[i];
					return (Class)pType.getActualTypeArguments()[0];
				}
				i++;
			}
		}
			
		
		
		return returnType;	
	}
}
