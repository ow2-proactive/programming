/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.descriptor.xml;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.services.FaultToleranceService;
import org.objectweb.proactive.core.descriptor.services.P2PDescriptorService;
import org.objectweb.proactive.core.descriptor.services.RMIRegistryLookupService;
import org.objectweb.proactive.core.descriptor.services.UniversalService;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

import org.xml.sax.SAXException;


public class ServiceDefinitionHandler extends PassiveCompositeUnmarshaller
    implements ProActiveDescriptorConstants {
    ProActiveDescriptor pad;
    protected String serviceId;

    public ServiceDefinitionHandler(ProActiveDescriptor pad) {
        super(false);
        this.pad = pad;
        this.addHandler(RMI_LOOKUP_TAG, new RMILookupHandler());
        this.addHandler(FT_CONFIG_TAG, new FaultToleranceHandler());
        this.addHandler(P2P_SERVICE_TAG, new P2PServiceHandler());
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
     */
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
        UniversalService service = (UniversalService) activeHandler.getResultObject();
        pad.addService(serviceId, service);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
     */
    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
        this.serviceId = attributes.getValue("id");
    }

    protected class RMILookupHandler extends BasicUnmarshaller {
        public RMILookupHandler() {
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            String lookupUrl = attributes.getValue("url");
            RMIRegistryLookupService rmiService = new RMIRegistryLookupService(lookupUrl);
            setResultObject(rmiService);
        }
    } // end of inner class RMILookupHandler

    protected class P2PServiceHandler extends PassiveCompositeUnmarshaller {
        protected P2PDescriptorService p2pDescriptorService;

        public P2PServiceHandler() {
            super(false);
            CollectionUnmarshaller ch = new CollectionUnmarshaller(String.class);
            ch.addHandler(PEER_TAG, new SingleValueUnmarshaller());
            this.addHandler(PEERS_SET_TAG, ch);
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            p2pDescriptorService = new P2PDescriptorService();
            String askedNodes = attributes.getValue("nodesAsked");
            if (checkNonEmpty(askedNodes)) {
                if (askedNodes.equals("MAX")) {
                    p2pDescriptorService.setNodeNumberToMAX();
                } else {
                    p2pDescriptorService.setNodeNumber(Integer.parseInt(
                            askedNodes));
                }
            }

            String acq = attributes.getValue("acq");
            if (checkNonEmpty(acq)) {
                p2pDescriptorService.setAcq(acq);
            }

            String port = attributes.getValue("port");
            if (checkNonEmpty(port)) {
                p2pDescriptorService.setPort(port);
            }

            String noa = attributes.getValue("NOA");
            if (checkNonEmpty(noa)) {
                p2pDescriptorService.setNoa(noa);
            }

            String ttu = attributes.getValue("TTU");
            if (checkNonEmpty(ttu)) {
                p2pDescriptorService.setTtu(ttu);
            }

            String ttl = attributes.getValue("TTL");
            if (checkNonEmpty(ttl)) {
                p2pDescriptorService.setTtl(ttl);
            }
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            String[] peerList = (String[]) activeHandler.getResultObject();
            p2pDescriptorService.setPeerList(peerList);
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return p2pDescriptorService;
        }

        private class SingleValueUnmarshaller extends BasicUnmarshaller {
            public void readValue(String value) throws org.xml.sax.SAXException {
                //System.out.println("SingleValueUnmarshaller.readValue() " + value);
                setResultObject(value);
            }
        }
    } // end of inner class P2PLookupHandler

    protected class FaultToleranceHandler extends PassiveCompositeUnmarshaller {
        protected FaultToleranceService ftService;

        public FaultToleranceHandler() {
            FTConfigHandler ftch = new FTConfigHandler();
            this.addHandler(FT_CKPTSERVER_TAG, ftch);
            this.addHandler(FT_RECPROCESS_TAG, ftch);
            this.addHandler(FT_LOCSERVER_TAG, ftch);
            this.addHandler(FT_RESSERVER_TAG, ftch);
            this.addHandler(FT_GLOBALSERVER_TAG, ftch);
            this.addHandler(FT_TTCVALUE_TAG, ftch);
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            this.ftService = new FaultToleranceService();
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return this.ftService;
        }

        protected class FTConfigHandler extends BasicUnmarshaller {
            public void startContextElement(String name, Attributes attributes)
                throws org.xml.sax.SAXException {
                if (FT_RECPROCESS_TAG.equals(name)) {
                    FaultToleranceHandler.this.ftService.setRecoveryProcessURL(attributes.getValue(
                            "url"));
                } else if (FT_LOCSERVER_TAG.equals(name)) {
                    FaultToleranceHandler.this.ftService.setLocationServerURL(attributes.getValue(
                            "url"));
                } else if (FT_CKPTSERVER_TAG.equals(name)) {
                    FaultToleranceHandler.this.ftService.setCheckpointServerURL(attributes.getValue(
                            "url"));
                } else if (FT_RESSERVER_TAG.equals(name)) {
                    FaultToleranceHandler.this.ftService.setAttachedResourceServer(attributes.getValue(
                            "url"));
                } else if (FT_TTCVALUE_TAG.equals(name)) {
                    FaultToleranceHandler.this.ftService.setTtcValue(attributes.getValue(
                            "value"));
                } else if (FT_GLOBALSERVER_TAG.equals(name)) {
                    FaultToleranceHandler.this.ftService.setGlobalServerURL(attributes.getValue(
                            "url"));
                }
            }
        }
    }
}
