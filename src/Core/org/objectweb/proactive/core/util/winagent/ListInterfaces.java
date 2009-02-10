package org.objectweb.proactive.core.util.winagent;

import org.objectweb.proactive.core.util.ProActiveInet;


public class ListInterfaces {

    public static void main(String[] args) {

        for (String s : ProActiveInet.getInstance().getAlInetAddresses()) {
            System.out.println(s);
        }

    }

}
