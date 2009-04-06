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
package org.objectweb.proactive.examples.webservices.helloWorld;

/**
 * A simple example to expose an active object as a web service.
 *
 * @author The ProActive Team
 */
public class ChooseNameComponent implements ChooseNameItf {

    private String[] names = new String[] { "Abhijeet Gaikwad", "Bastien Sauvan", "Brian Amedro",
            "Elaine Isnard", "Elton Mathias", "Eric Madelaine", "Etienne Vallette-De-Osia", "Fabien Viale",
            "Fabrice Huet", "Florin-Alexandru Bratu", "Francoise Baude", "Germain Sigety",
            "Guilherme Perretti Pezzi", "Imen Filiali", "Jonathan Martin", "Khan Muhammad", "Laurent Vanni",
            "Ludovic Henrio", "Marcela Rivera", "Nicolas Dodelin", "Paul Naoumenko", "Regis Gascon",
            "Vasile Jureschi", "Viet Dong Doan", "Virginie Contes", "Yu Feng", "Franca Perrina" };

    public ChooseNameComponent() {
    }

    public String chooseName(int index) {
        return names[index];
    }

}
