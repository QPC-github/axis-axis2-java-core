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
 */

package org.apache.axis2.cluster.tribes;

import org.apache.axis2.cluster.ClusteringFault;
import org.apache.axis2.cluster.configuration.ConfigurationClusteringCommand;
import org.apache.axis2.cluster.configuration.DefaultConfigurationManager;
import org.apache.axis2.cluster.context.ContextClusteringCommand;
import org.apache.axis2.cluster.context.DefaultContextManager;
import org.apache.catalina.tribes.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;


public class ChannelListener implements org.apache.catalina.tribes.ChannelListener {

    private DefaultContextManager contextManager = null;
    private DefaultConfigurationManager configurationManager = null;

    private static final Log log = LogFactory.getLog(ChannelListener.class);

    public ChannelListener(DefaultConfigurationManager configurationManager,
                           DefaultContextManager contextManager) {
        this.configurationManager = configurationManager;
        this.contextManager = contextManager;
    }

    public void setContextManager(DefaultContextManager contextManager) {
        this.contextManager = contextManager;
    }

    public void setConfigurationManager(DefaultConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public boolean accept(Serializable msg, Member sender) {
        return true;
    }

    public void messageReceived(Serializable msg, Member sender) {
        System.err.println("####### Message received " + msg);
        if (msg instanceof ContextClusteringCommand) {
            try {
                ContextClusteringCommand comMsg = (ContextClusteringCommand) msg;
                contextManager.notifyListeners(comMsg);
            } catch (ClusteringFault e) {
                log.error("Could not process ContextCommand", e);
            }
        } else if (msg instanceof ConfigurationClusteringCommand) {
            ConfigurationClusteringCommand command = (ConfigurationClusteringCommand) msg;
            try {
                configurationManager.notifyListeners(command);
            } catch (ClusteringFault e) {
                log.error("Could not process ConfigurationCommand", e);
            }
        }
    }
}
