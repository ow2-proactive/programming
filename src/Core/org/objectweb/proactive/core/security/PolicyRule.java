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
package org.objectweb.proactive.core.security;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.core.security.securityentity.RuleEntities;


public class PolicyRule implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 41L;
    /**
     *
     */
    private final RuleEntities from;
    private final RuleEntities to;
    private final Communication communicationReply;
    private final Communication communicationRequest;
    private final boolean migration;
    private final boolean aocreation;

    /**
     * Default constructor, initialize a policy with communication attribute
     * sets to forbidden and authentication,confidentiality and integrity set to
     * required.
     */
    public PolicyRule() {
        this.from = new RuleEntities();
        this.to = new RuleEntities();
        this.communicationReply = new Communication();
        this.communicationRequest = new Communication();
        this.migration = false;
        this.aocreation = false;
    }

    public PolicyRule(RuleEntities from, RuleEntities to, Communication request, Communication reply,
            boolean aoCreation, boolean migration) {
        if ((from == null) || (to == null) || (request == null) || (reply == null)) {
            throw new NullPointerException();
        }
        this.from = from;
        this.to = to;
        this.communicationRequest = request;
        this.communicationReply = reply;
        this.aocreation = aoCreation;
        this.migration = migration;
    }

    /**
     * Copy constructor.
     */
    public PolicyRule(PolicyRule policy) {
        this(policy.getEntitiesFrom(), policy.getEntitiesTo(), policy.getCommunicationRequest(), policy
                .getCommunicationReply(), policy.isAoCreation(), policy.isMigration());
    }

    @Override
    public String toString() {
        String vnFrom;
        String vnTo;
        vnFrom = vnTo = null;
        if (this.from == null) {
            vnFrom = "all";
        } else {
            vnFrom = this.from.toString();
        }
        if (this.to == null) {
            vnTo = "all";
        } else {
            vnTo = this.to.toString();
        }

        return vnFrom + "-->" + vnTo + "== Req : " + this.communicationRequest + "; Rep : " +
            this.communicationReply + ";Migration :" + this.migration + ";AOCreation:" + this.aocreation;
    }

    public Communication getCommunicationReply() {
        return this.communicationReply;
    }

    public Communication getCommunicationRequest() {
        return this.communicationRequest;
    }

    public RuleEntities getEntitiesFrom() {
        return this.from;
    }

    public RuleEntities getEntitiesTo() {
        return this.to;
    }

    /**
     * @return true if object creation is authorized
     */
    public boolean isAoCreation() {
        return this.aocreation;
    }

    /**
     * @return true if migration is authorized
     */
    public boolean isMigration() {
        return this.migration;
    }

    public static PolicyRule mergePolicies(List<PolicyRule> policies) {
        PolicyRule resultPolicy = null;

        for (PolicyRule policy : policies) {
            int fromLevel = policy.getEntitiesFrom().getLevel();
            int toLevel = policy.getEntitiesTo().getLevel();

            if (resultPolicy == null) {
                resultPolicy = new PolicyRule(policy);
            } else {
                int resultFromLevel = resultPolicy.getEntitiesFrom().getLevel();
                int resultToLevel = resultPolicy.getEntitiesTo().getLevel();

                RuleEntities from;
                if (fromLevel > resultFromLevel) {
                    from = policy.getEntitiesFrom();
                } else {
                    from = resultPolicy.getEntitiesFrom();
                }

                RuleEntities to;
                if (toLevel > resultToLevel) {
                    to = policy.getEntitiesTo();
                } else {
                    to = resultPolicy.getEntitiesTo();
                }

                Communication request = Communication.computeCommunication(policy.getCommunicationRequest(),
                        resultPolicy.getCommunicationRequest());
                Communication reply = Communication.computeCommunication(policy.getCommunicationReply(),
                        resultPolicy.getCommunicationReply());

                boolean aoCreation = policy.isAoCreation() && resultPolicy.isAoCreation();
                boolean migration = policy.isMigration() && resultPolicy.isMigration();

                resultPolicy = new PolicyRule(from, to, request, reply, aoCreation, migration);
            }
        }
        return resultPolicy;
    }
}
