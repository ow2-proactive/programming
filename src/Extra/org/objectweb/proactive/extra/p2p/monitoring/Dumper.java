/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.p2p.monitoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extra.p2p.service.P2PService;
import org.objectweb.proactive.extra.p2p.service.util.UniversalUniqueID;


public class Dumper {
    // protected HashMap<String, P2PNode> senders = new HashMap<String, P2PNode>();
    // protected HashMap<String, Link> links = new HashMap<String, Link>();

    // protected ArrayList<Link> links = new ArrayList<Link>();
    // private int index;
    protected P2PNetwork network = new P2PNetwork();

    public Dumper() {
    }

    /**
     * Receive a dump from a peer The sender is put in an arrayList and some Links are created
     * 
     * @param info
     */
    public void receiveAcqInfo(AcquaintanceInfo info) {
        // use the size of the hashmap to give each sender a unique index
        System.out.println(">>>>");
        System.out.println(info.getSender() + " current Noa =  " + info.getCurrentNoa() + " max Noa= " +
            info.getNoa());
        // this.addAsSender(info);
        this.network.addAsSender(info);
        // senders.put(info.getSender(), senders.size());
        String[] acq = info.getAcq();
        String source = info.getSender();
        for (int i = 0; i < acq.length; i++) {
            System.out.println(" Acquaintance: " + acq[i]);
            // check that the destination is in our list
            // otherwise add them
            // this.addAsSender(acq[i]);
            this.network.addAsSender(acq[i]);
            String dest = acq[i];

            this.network.addLink(source, dest);
        }
        System.out.println("    --- Awaiting ");
        String[] tmp = info.getAwaitedReplies();
        for (int i = 0; i < tmp.length; i++) {
            System.out.println(tmp[i]);
        }
        System.out.println("    ------------------");

        System.out.println("<<<<");
    }

    /**
     * Dump the acqaintances list to use with Otter The following format is used Node : ? index name
     * Link : L index sourceIndex destIndex
     */
    public void dumpAcqForOtter() {
        HashMap<String, P2PNode> senders = this.network.getSenders();
        HashMap<String, Link> links = this.network.getLinks();
        int index = senders.size();
        // first indicate the number of nodes and links
        System.out.println("t " + senders.size());
        System.out.println("T " + links.size());
        // color by number of acquaintances
        System.out.println("g 1 d 2 Metric ");
        System.out.println("f 1 NOA'max NOA");

        // System.out.println("f 1 max NOA");
        // dump the nodes with their indexes
        Set<Map.Entry<String, P2PNode>> map = senders.entrySet();
        Iterator<Map.Entry<String, P2PNode>> it = map.iterator();
        while (it.hasNext()) {
            Map.Entry<String, P2PNode> entry = it.next();

            // the node might have a -1 index because has never sent anything
            // we want to get rid of this
            if (entry.getValue().getIndex() == -1) {
                entry.getValue().setIndex(index++);
            }
            System.out.println("? " + entry.getValue().getIndex() + " " + entry.getKey());
            // and its associated noa
            System.out.println("v " + entry.getValue().getIndex() + " 1 " + entry.getValue().getNoa() + "'" +
                entry.getValue().getMaxNOA());
            // System.out.println("v " + entry.getValue().getIndex() + " 1 " +
            // entry.getValue().getNoa());
        }

        // now dump the links
        int i = 0;

        Set<Map.Entry<String, Link>> map2 = links.entrySet();

        Iterator<Map.Entry<String, Link>> it2 = map2.iterator();
        while (it2.hasNext()) {
            Link entry = it2.next().getValue();
            // System.out.println("---- looking for sender " + entry.getSource());
            System.out.println("L " + i++ + " " + senders.get(entry.getSource()).getIndex() + " " +
                senders.get(entry.getDestination()).getIndex());
        }
    }

    public void dumpForPeerSim() {
        // now dump the links

        // HashMap<String, P2PNode> senders = network.getSenders();
        // we use a hashtable because we will get collisions
        HashMap<String, List<String>> sourceDest = new HashMap<String, List<String>>();

        // use to build the conversion name -> Integer
        HashMap<String, Integer> nameConversion = new HashMap<String, Integer>();
        int number = 1;

        HashMap<String, Link> links = this.network.getLinks();
        Set<Map.Entry<String, Link>> map2 = links.entrySet();

        // iterate over all the links
        Iterator<Map.Entry<String, Link>> it = map2.iterator();
        while (it.hasNext()) {
            Link entry = (it.next()).getValue();

            // // System.out.println("---- looking for sender " + entry.getSource());
            // System.out.println(entry.getSource() + " <---> " +
            // entry.getDestination());
            if (sourceDest.get(entry.getSource()) != null) {
                List<String> tmp = sourceDest.get(entry.getSource());
                tmp.add(entry.getDestination());
                // sourceDest.put(entry.getSource(), entry.getDestination());
            } else {
                List<String> tmp = new ArrayList<String>();
                tmp.add(entry.getDestination());
                sourceDest.put(entry.getSource(), tmp);
            }

            // have we seen this source or destination before?
            if (nameConversion.get(entry.getSource()) == null) {
                nameConversion.put(entry.getSource(), new Integer(number));
                number++;
            }
            if (nameConversion.get(entry.getDestination()) == null) {
                nameConversion.put(entry.getDestination(), new Integer(number));
                number++;
            }
        }

        Set<String> sources = sourceDest.keySet();
        for (String key : sources) {
            List<String> tmp = sourceDest.get(key);
            Iterator<String> it2 = tmp.iterator();
            String result = nameConversion.get(key).toString();
            while (it2.hasNext()) {
                result = result + " " + nameConversion.get(it2.next());
            }
            System.out.println(result);
        }

        System.out.println("Total number of peers " + nameConversion.size());
        // Set<String> sources = sourceDest.keySet();
        //
        // for (String key : sources) {
        // System.out.println("Processing "+ key);
        // String value = sourceDest.remove(key);
        // String result = key;
        // while (value != null) {
        // result = result + " " + value;
        // value = sourceDest.remove(key);
        // }
        // System.out.println(result);
        // }
        // Iterator itSource = sources.iterator();
        // while (itSource.hasNext()) {
        //
        // //for each source, check all destinations
        // String key = (String) itSource.next();
        //
        // }
    }

    public void dumpLinksAsText() {
        // now dump the links
        HashMap<String, Link> links = this.network.getLinks();
        Set<Map.Entry<String, Link>> map2 = links.entrySet();

        // iterate over all the links in the network
        Iterator<Map.Entry<String, Link>> it = map2.iterator();
        while (it.hasNext()) {
            Link entry = it.next().getValue();
            // System.out.println("---- looking for sender " + entry.getSource());
            System.out.println(entry.getSource() + " <---> " + entry.getDestination());
        }
    }

    public void dumpPeersAsText() {
        HashMap<String, P2PNode> m = this.network.getSenders();
        Iterator<Map.Entry<String, P2PNode>> it = m.entrySet().iterator();
        while (it.hasNext()) {
            P2PNode entry = (P2PNode) it.next().getValue();
            System.out.println("----  " + entry.getName());
            System.out.println(entry.getMetadataAsString());

        }
    }

    public P2PNetwork getP2PNetwork() {
        return this.network;
    }

    public static void requestAcquaintances(String ref) {
        P2PService p2p = null;
        Node distNode = null;
        try {
            distNode = NodeFactory.getNode(ref);
        } catch (NodeException e) {
            e.printStackTrace();
        }
        try {
            p2p = (P2PService) distNode.getActiveObjects(P2PService.class.getName())[0];
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        }

        System.out.println("Dumper ready to call!");
        p2p.dumpAcquaintances();
    }

    public static void requestAcquaintances(String ref, Dumper d) {
        P2PService p2p = null;
        Node distNode = null;
        try {
            distNode = NodeFactory.getNode(ref);
            p2p = (P2PService) distNode.getActiveObjects(P2PService.class.getName())[0];
            System.out.println("Dumper ready to call!");
            p2p.dumpAcquaintances(new DumpACQWithCallback(10, UniversalUniqueID.randomUUID(), d));
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        }
    }

    public void createGraphFromFile(String name) {
        // Dumper dump = new Dumper();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(new File(name)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String s = null;
        String current = null;

        // what we are reading now
        // 0 = nothing interesting
        // 1 = awaiting peer name
        // 2 = reading acquaintances name
        int readingStatus = 0;

        try {
            while ((s = in.readLine()) != null) {
                // System.out.println(s);
                if (s.indexOf(">>>") >= 0) {
                    // begining of a new Peer
                    readingStatus = 1;
                } else if (s.indexOf("<<<") >= 0) {
                    // end of a new Peer
                    readingStatus = 0;
                } else {
                    // reading some peer name
                    switch (readingStatus) {
                        case 1: {
                            // example of string
                            // "trinidad.inria.fr:2410 current Noa = 1 max Noa= 3"
                            Pattern pattern = Pattern.compile("(.*) current .* =  (.*) max Noa= (.*)");
                            Matcher matcher = pattern.matcher(s);
                            boolean matchFound = matcher.find();

                            if (matchFound) {
                                // Get all groups for this match
                                // for (int i=1; i<=matcher.groupCount(); i++) {
                                s = matcher.group(1);
                                // System.out.println(groupStr);
                                // }
                                // }

                                // s= s.substring(0, s.indexOf("current")-1);
                                this.network.addAsSender(s, Integer.parseInt(matcher.group(2)), Integer
                                        .parseInt(matcher.group(3)));
                                current = s;
                                readingStatus = 2;
                            }
                            break;
                        }
                        case 2: {
                            // we are either reading a machine name or some garbage
                            if (s.indexOf("---") < 0) {
                                // System.out.println(s);
                                s = this.cleanURL(s);

                                this.network.addAsSender(s);
                                this.network.addLink(current, s);
                                try {
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                readingStatus = 3;
                            }
                            break;
                        }
                        case 3: {
                            if (s.indexOf("---") >= 0) {
                                readingStatus = 2;
                            }
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // this.dumpAsText();
        // this.generateGraphNodes(dump);
        // this.generateGraphLinks(dump);
    }

    public void createGraphFromPeerFile(String name, ProgressObserver po) {
        BufferedReader in = null;
        long size = 0;
        long read = 0;
        long nextUpdate = 0;
        try {
            File file = new File(name);
            size = file.length();
            nextUpdate = size / 100;
            // System.out.println("Dumper.createGraphFromPeerFile() size is " + size );
            in = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String s = null;
        String current = null;
        try {
            int index = -1;
            String topo = "Network Topology :";
            String state = "Peer States :";
            String metrics = "Peer Metrics :";
            while ((s = in.readLine()) != null) {
                // unicode char are 2 bytes
                read += s.length();
                // System.out.println(read);
                // System.out.println(nextUpdate);
                if (read > nextUpdate) {
                    nextUpdate += size / 100;
                    // System.out.println("----------------------------- " +(read*100.0/size) +"%");
                    if (po != null) {
                        po.percentage(read * 100.0 / size);
                    }
                }
                if ((index = s.indexOf(topo)) >= 0) {
                    // we remove the begining of the text
                    s = s.substring(index + topo.length() + 2);
                    this.createNetworkGraph(s);
                    // return;
                    // System.exit(-1);
                    // System.out.println("Dumper.createGraphFromPeerFile()");
                }
                if ((index = s.indexOf(state)) >= 0) {
                    // we remove the begining of the text
                    s = s.substring(index + state.length() + 1);
                    this.updateNodeState(s);
                    // return;
                    // System.exit(-1);
                    // System.out.println("Dumper.createGraphFromPeerFile()");
                }
                if ((index = s.indexOf(metrics)) >= 0) {
                    // we remove the begining of the text
                    s = s.substring(index + metrics.length() + 1);
                    this.updateNodeMetrics(s);
                    // return;
                    // System.exit(-1);
                    // System.out.println("Dumper.createGraphFromPeerFile()");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void createNetworkGraph(String s) {
        System.out.println("Dumper.createNetworkGraph() " + s);
        // separate the line into peers
        String r[] = s.split(" ");

        for (int i = 0; i < r.length; i++) {
            // extract the peer number
            Pattern p = Pattern.compile("\\((.*),\\{(.*)\\}\\)");
            Matcher m = p.matcher(r[i]);
            m.matches();
            if (m.groupCount() == 2) {
                this.network.addAsSender(m.group(1) + "");
                String acq[] = m.group(2).split(",");
                for (int j = 0; j < acq.length; j++) {
                    this.network.addAsSender(acq[j] + "");
                    this.network.addLink(m.group(1) + "", acq[j] + "");
                }
            }
        }
        this.network.finished();
    }

    /**
     * Process the string representing the peer state
     * ((peerID, state, resource)...)
     * @param s
     */
    protected void updateNodeState(String s) {
        System.out.println("Dumper.updateNodeState() " + s);
        // separate the line into peers
        String r[] = s.split(" ");

        for (int i = 0; i < r.length; i++) {
            // extract the peer number
            Pattern p = Pattern.compile("\\((.*),(.*),(.*)\\)");
            Matcher m = p.matcher(r[i]);
            m.matches();
            if (m.groupCount() == 3) {
                P2PNode n = this.network.getSenders().get(m.group(1));
                // this.network.addAsSender(m.group(1) +"");
                System.out.println("Dumper.updateNodeState() setting metadata " + m.group(2) + " " +
                    m.group(3));
                String type = null;
                int intType = Integer.parseInt(m.group(2));
                //this is really ugly
                switch (intType) {
                    case 0:
                        type = "Search";
                        break;
                    case 1:
                        type = "Grant";
                        break;
                    case 2:
                        type = "Idle";
                        break;
                }
                n.setType(type);
                this.network.addAttribute(n, new PeerAttribute[] { new PeerAttribute("Type", type),
                        new PeerAttribute("Ressource", m.group(3)) });
            }
        }
    }

    protected void updateNodeMetrics(String s) {
        System.out.println("Dumper.updateNodeMetrics() " + s);
        // separate the line into peers
        String r[] = s.split(" ");

        for (int i = 0; i < r.length; i++) {
            // extract the peer number
            Pattern p = Pattern.compile("\\((.*),\\{(.*)\\}\\)");
            Matcher m = p.matcher(r[i]);
            m.matches();
            if (m.groupCount() == 2) {
                P2PNode n = this.network.getSenders().get(m.group(1));
                String met[] = m.group(2).split(",");
                // for (int j = 0; j < met.length; j++) {
                // this.network.addAsSender(m.group(1) +"");
                // System.out.println("Dumper.updateNodeMetrics() " + met[j]);
                // nbrGrant,nbrHit,* Success,CacheRate
                // System.out.println("Dumper.updateNodeState() setting metadata " + m.group(2) + "
                // " + m.group(3));
                // n.setMetaData(new String[] {"Type : " + m.group(2),"Ressource : " +m.group(3)});
                this.network.addAttribute(n, new PeerAttribute[] { new PeerAttribute("Requests", met[0]),
                        new PeerAttribute("Grants", met[1]), new PeerAttribute("Hits", met[2]),
                        new PeerAttribute("Cache Hit", met[3]) });
                // }
                // n.getColor().setData(Float.parseFloat(met[3]));
            }
        }
    }

    public String cleanURL(String s) {
        if (s.indexOf("Acquaintance:") > 0) {
            s = s.substring("Acquaintance:".length() + 2);
        }

        // try {
        // System.out.println(s);
        if (s.indexOf("//") == -1) {
            s = "//" + s;
        }
        return P2PService.getHostNameAndPortFromUrl(s.trim());

    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage : " + Dumper.class.getName() + " <URL> " + "<descriptor>  or <file>");
            System.exit(-1);
        }

        Dumper d = null;
        if (args.length == 1) {
            d = new Dumper();
            d.createGraphFromPeerFile(args[0], null);
            //    d.dumpPeersAsText();
            // d.dumpForPeerSim();
        }
        // d.network.getSenders()

        // } else {
        // try {
        // d = (Dumper) PAActiveObject.newActive(Dumper.class.getName(), null);
        // } catch (ActiveObjectCreationException e) {
        // e.printStackTrace();
        // } catch (NodeException e) {
        // e.printStackTrace();
        // }
        // requestAcquaintances(args[0], d);
        // try {
        // Thread.sleep(20000);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // d.dumpAcqForOtter();
        // }
    }
}
