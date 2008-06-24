/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */


// TODO: tests

package org.objectweb.proactive.extra.montecarlo.example;


import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.util.Random;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extra.montecarlo.EngineTask;
import org.objectweb.proactive.extra.montecarlo.Executor;
import org.objectweb.proactive.extra.montecarlo.ExperienceSet;
import org.objectweb.proactive.extra.montecarlo.PAMonteCarlo;
import org.objectweb.proactive.extra.montecarlo.Simulator;
import org.objectweb.proactive.api.PALifeCycle;


public class PiMonteCarlo implements EngineTask {
	
	private double niter = 0;	   
    private double tasks = 0;
    
    public PiMonteCarlo(double n, double t){
    	super();
    	niter = n;
    	tasks = t;
    }
	
	public class MCPi implements ExperienceSet{
	    	int N;
	    	
	    	MCPi(final double d){
	    		this.N = (int) d;
	    	}
	    	
	    	public double[] simulate(final Random rng){
	    		final double [] count = new double[1]; 
	    		 for (int i = 0; i < N; i++) {
	    			 	double x = rng.nextGaussian();
	    			 	double y = rng.nextGaussian();
	    	            double value =  x*x + y*y;
	    	            if(value <= 1){
	    	            	count[0] += 1;
	    	            }
	    	        }
	    	        return count;
	    	}
	    }
		
	
		
		public static void main(String[] args) throws ProActiveException, TaskException {
	    	URL descriptor = PiMonteCarlo.class.getResource("WorkersApplication.xml");
	        PAMonteCarlo mc = new PAMonteCarlo(descriptor, null, "Workers");
	        
	        // total monte carlo iterations and number tasks
	        
	        PiMonteCarlo piMonteCarlo = new PiMonteCarlo(1e6, 10); 
	        
	        double [] pi = (double []) mc.run(piMonteCarlo);
	        
	        System.out.println(" The value of pi is " + pi[0]);
	        mc.terminate();
	        PALifeCycle.exitSuccess();
	    }
	    	    

	    public Serializable run(Simulator simulator, Executor executor) {
	    	
	    	double pival;
	    	List<ExperienceSet> sets = new ArrayList<ExperienceSet>();
	        
	        for (int i = 0; i < tasks; i++) {
	            sets.add(new MCPi((int)(niter/tasks)));
	        }
	        
	        Enumeration<double[]> simulatedCountList = null;
	        
	        try {
	            simulatedCountList = simulator.solve(sets);
	        } catch (TaskException e) {
	            throw new RuntimeException(e);
	        }

	        int counter = 0;
	        while (simulatedCountList.hasMoreElements()) {
	                   
	            double[] simulatedCounts = simulatedCountList.nextElement();
	            counter += simulatedCounts[0];	              
	        }
     
	        pival = counter/(niter*4);
   
	        return pival;
	    }
}
