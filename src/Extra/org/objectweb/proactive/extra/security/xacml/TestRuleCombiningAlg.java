/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.security.xacml;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.Rule;
import com.sun.xacml.combine.RuleCombiningAlgorithm;
import com.sun.xacml.ctx.Result;


public class TestRuleCombiningAlg extends RuleCombiningAlgorithm {
    public TestRuleCombiningAlg() throws URISyntaxException {
        super(new URI("rule-combining-alg:most-specific"));
    }

    @Override
    public Result combine(EvaluationCtx context, List rules) {
        Iterator<Rule> it = rules.iterator();

        while (it.hasNext()) {
            // get the next Rule, and evaluate it
            Rule rule = (it.next());
            Result result = rule.evaluate(context);

            // if it returns Permit, then the alg returns Permit
            if (result.getDecision() == Result.DECISION_PERMIT) {
                return result;
            }
        }

        // if nothing returned Permit, then the alg returns Deny
        return new Result(Result.DECISION_DENY);
    }
}
