/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.scilab.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import javasci.SciData;
import javasci.Scilab;

import org.objectweb.proactive.examples.scilab.util.SciMath;


public class SciTestSeqMandel {
    public static void main(String[] args) throws Exception {
        if (args.length != 8) {
            System.out.println("Invalid number of parameter : " + args.length);
            return;
        }

        BufferedReader reader = new BufferedReader(new FileReader(args[6]));
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(args[7])));

        int xres = Integer.parseInt(args[0]);
        int yres = Integer.parseInt(args[1]);
        double xmin = Double.parseDouble(args[2]);
        double xmax = Double.parseDouble(args[3]);
        double ymin = Double.parseDouble(args[4]);
        double ymax = Double.parseDouble(args[5]);
        int precision;

        String line;
        double startTime;
        double endTime;

        for (int i = 0; (line = reader.readLine()) != null; i++) {
            if (line.trim().startsWith("#")) {
                continue;
            }

            if (line.trim().equals("")) {
                break;
            }

            precision = Integer.parseInt(line.trim());

            startTime = System.currentTimeMillis();
            Scilab.Exec(SciMath.formulaMandelbrot("Fract", xres, yres, xmin, xmax, ymin, ymax, precision));
            SciData sciFract = Scilab.receiveDataByName("Fract");
            System.out.println(sciFract);
            endTime = System.currentTimeMillis();

            System.out.println(xres + " " + yres + " " + precision + " " + (endTime - startTime));
            writer.println(xres + " " + yres + " " + precision + " " + (endTime - startTime));
        }

        reader.close();
        writer.close();
    }
}
