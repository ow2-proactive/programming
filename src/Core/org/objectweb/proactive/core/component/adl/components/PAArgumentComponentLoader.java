/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.adl.components;

import static org.objectweb.fractal.adl.error.ChainedErrorLocator.chainLocator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.arguments.ArgumentComponentLoader;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.components.ComponentDefinition;
import org.objectweb.fractal.adl.components.ComponentErrors;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.merger.MergeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The {@link PAArgumentComponentLoader} extends Fractal's {@link ArgumentComponentLoader}
 * to merge also definitions of components declared in the membrane.
 * 
 * @author The ProActive Team
 *
 */
public class PAArgumentComponentLoader extends ArgumentComponentLoader {

    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

    // The load methods are not changed. The modifications in order to extend the loading
    // to the NF components are included in the normalize and resolution methods
    // from ComponentLoader.	

    // -------------------------------------------------------------------------
    // First pass: normalization
    // -------------------------------------------------------------------------
    /**
     * Checks that two sub-components don't have the same name.<br/><br/>
     * 
     * This version extends the check to verify this property also for NF subcomponents.
     * However, this implementation assumes that F subcomponents and NF components are kept in different namespaces.
     * This means that a F components and a NF component belonging to the same parent
     * may actually have the same name.
     * 
     * If that behaviour is to be changed, it suffice to have one cycle that verifies all subcomponents
     * against the same map.
     */
    protected void normalizeComponentContainer(final ComponentContainer container) throws ADLException {
        logger.debug("[PAArgumentComponentLoader]   Normalizing container " + container.toString() + " ... ");
        final Map<String, Component> fNames = new HashMap<String, Component>();
        final Map<String, Component> nfNames = new HashMap<String, Component>();

        Component[] fComponents = container.getComponents();
        Component[] nfComponents = this.getNFComponents(container);

        // cycle for F components
        for (final Component comp : fComponents) {
            final String name = comp.getName();
            if (name == null) {
                throw new ADLException(ComponentErrors.COMPONENT_NAME_MISSING, comp);
            }
            final Component previousDefinition = fNames.put(name, comp);
            if (previousDefinition != null) {
                throw new ADLException(ComponentErrors.DUPLICATED_COMPONENT_NAME, comp, name,
                    new NodeErrorLocator(previousDefinition));
            }
            normalizeComponentContainer(comp);
        }

        // cycle for NF components
        for (final Component comp : nfComponents) {
            final String name = comp.getName();
            if (name == null) {
                throw new ADLException(ComponentErrors.COMPONENT_NAME_MISSING, comp);
            }
            final Component previousDefinition = nfNames.put(name, comp);
            if (previousDefinition != null) {
                throw new ADLException(ComponentErrors.DUPLICATED_COMPONENT_NAME, comp, name,
                    new NodeErrorLocator(previousDefinition));
            }
            normalizeComponentContainer(comp);
        }

    }

    // -------------------------------------------------------------------------
    // Second pass: definition references resolution
    // -------------------------------------------------------------------------
    /**
     *  Resolves a 'component container'(starting from 'topLevelDefinition' ?),
     *  including NF components, merging the definitions.
     *  
     *  Merge nodes replace the original nodes in the functional or in the
     *  non-functional part.
     *  
     */
    protected void resolveComponentContainer(final Set<String> loaded,
            final ComponentContainer topLevelDefinition, final ComponentContainer container,
            final Map<Object, Object> context) throws ADLException {

        logger.debug("[PAArgumentComponentLoader]   RCC " + container.toString() + " TL:" +
            topLevelDefinition.toString());

        // Loop for functional subcomponents
        for (final Component comp : container.getComponents()) {
            logger.debug("[PAArgumentComponentLoader]   Resolving for F " + comp.toString());
            resolveComponentContainer(loaded, topLevelDefinition, comp, context);
            final String definition = comp.getDefinition();
            if (definition != null) {
                final List<String> defs = parseDefinitions(definition, comp);
                // shared components will be resolved by the
                // resolveSharedComponentContainer method.
                if (defs.size() != 1 || !isShared(defs.get(0))) {
                    comp.setDefinition(null);
                    ((Node) comp).astSetDecoration("definition", definition);
                    Definition d;
                    try {
                        d = resolveDefinitions(loaded, defs, context);
                    } catch (final ADLException e) {
                        //logger.log(Level.FINE,
                        //		"ADLException while loading referenced definition(s)", e);
                        chainLocator(e, comp);
                        throw e;
                    }
                    Node merged;
                    logger.debug("[PAArgumentComponentLoader]   Merging F: " + comp.toString() +
                        " with supernode " + d.toString());
                    try {
                        merged = nodeMergerItf.merge(comp, d, nameAttributes);
                    } catch (final MergeException e) {
                        throw new CompilerError(ComponentErrors.MERGE_ERROR, new NodeErrorLocator(comp), e,
                            definition);
                    }
                    if (merged != comp) {
                        // the container AST is a pure tree (i.e. no sharing), so simply
                        // replace the merged node in the container.
                        logger.debug("[PAArgumentComponentLoader]   Merging F: Container " +
                            container.toString() + " ... merged " + merged.toString());
                        container.removeComponent(comp);
                        container.addComponent((Component) merged);
                    }
                }
            }
        }

        // Loop for NF components
        for (final Component comp : getNFComponents(container)) {
            logger.debug("[PAArgumentComponentLoader]   Resolving for NF " + comp.toString());
            resolveComponentContainer(loaded, topLevelDefinition, comp, context);
            final String definition = comp.getDefinition();
            if (definition != null) {
                final List<String> defs = parseDefinitions(definition, comp);
                // shared components will be resolved by the
                // resolveSharedComponentContainer method.
                if (defs.size() != 1 || !isShared(defs.get(0))) {
                    comp.setDefinition(null);
                    ((Node) comp).astSetDecoration("definition", definition);
                    Definition d;
                    try {
                        d = resolveDefinitions(loaded, defs, context);
                    } catch (final ADLException e) {
                        //logger.log(Level.FINE,
                        //		"ADLException while loading referenced definition(s)", e);
                        chainLocator(e, comp);
                        throw e;
                    }
                    Node merged;
                    logger.debug("[PAArgumentComponentLoader]   Merging NF: " + comp.toString() +
                        " with supernode " + d.toString());
                    try {
                        merged = nodeMergerItf.merge(comp, d, nameAttributes);
                    } catch (final MergeException e) {
                        throw new CompilerError(ComponentErrors.MERGE_ERROR, new NodeErrorLocator(comp), e,
                            definition);
                    }
                    if (merged != comp) {
                        // the container AST is a pure tree (i.e. no sharing), so simply
                        // replace the merged node in the container.
                        logger.debug("[PAArgumentComponentLoader]   Merging NF: Container " +
                            container.toString() + " ... merged " + merged.toString());
                        // In the case of NF components, the merged tree must be added in the 'controller' node.
                        Controller ctrl = ((ControllerContainer) container).getController();
                        ((ComponentContainer) ctrl).removeComponent(comp);
                        ((ComponentContainer) ctrl).addComponent((Component) merged);
                    }
                }
            }
        }

    }

    // -------------------------------------------------------------------------
    // Third pass: definition inheritance resolution
    // -------------------------------------------------------------------------
    /**
     * Resolves the 'extends' part in definitions.
     */
    protected ComponentDefinition resolveDefinitionExtension(final Set<String> loaded,
            ComponentDefinition def, final Map<Object, Object> context) throws ADLException {
        logger.debug("[PAArgumentComponentLoader]   Resolving Definition Extension for " + def.toString());
        if (def.getExtends() != null) {
            final List<String> defs = parseDefinitions(def.getExtends(), def);
            try {
                Definition def2 = resolveDefinitions(loaded, defs, context);
                logger.debug("[PAArgumentComponentLoader]   Merging(rde): " + def.toString() +
                    " ... Super: " + def2.toString());
                def = (ComponentDefinition) nodeMergerItf.merge(def, def2, nameAttributes);
                logger.debug("[PAArgumentComponentLoader]   Merged (rde): " + def.toString());
            } catch (final ADLException e) {
                //logger.log(Level.FINE,
                //		"ADLException while loading super definition(s)", e);
                chainLocator(e, def);
                throw e;
            } catch (final MergeException e) {
                throw new CompilerError(ComponentErrors.MERGE_ERROR, new NodeErrorLocator(def), e, def
                        .getName());
            }
            def.setExtends(null);
        }
        logger.debug("[PAArgumentComponentLoader]   Resolved Definition Extension for " + def.toString());
        return def;
    }

    /**
     * Resolve 'definition' nodes and returns the merged node.
     */
    protected Definition resolveDefinitions(final Set<String> loaded, final List<String> nameList,
            final Map<Object, Object> context) throws ADLException {
        logger.debug("[PAArgumentComponentLoader]   Resolving Definition for " + nameList.get(0));
        Definition d = load(loaded, nameList.get(0), context);
        for (int i = 1; i < nameList.size(); ++i) {
            Definition e;
            e = load(loaded, nameList.get(i), context);
            try {
                logger.debug("[PAArgumentComponentLoader]   Merging(rd): Node: " + e.toString() +
                    " ... Super: " + d.toString());
                d = (Definition) nodeMergerItf.merge(e, d, nameAttributes);
                logger.debug("[PAArgumentComponentLoader]   Merged (rd): Node: " + d.toString());
            } catch (final MergeException me) {
                throw new CompilerError(ComponentErrors.MERGE_ERROR, me, nameList);
            }
        }
        return d;
    }

    // TODO: ComponentLoader.resolveSharedComponentContainer should be extended for NF components
    // in the same way of 'resolveComponentContainer'. This means, extending the cycle not only
    // for container.getComponents(), but also for NF components.
    // This implies that the ComponentLoader.replaceComponents method should be modified
    // for adding the NF shared components in the controller part, instead of the functional part.

    // However, in GCM there are no shared components (so this is not necessary now)
    /*
     * protected void resolveSharedComponentContainer( final ComponentContainer topLevelDefinition,
     * final ComponentContainer container, final Map<Object, Object> context) throws ADLException {
     * final Component[] comps = container.getComponents(); for (int i = 0; i < comps.length; i++) {
     * final Component comp = comps[i]; resolveSharedComponentContainer(topLevelDefinition, comp,
     * context); String definition = comp.getDefinition(); if (definition != null) { final
     * List<String> defs = parseDefinitions(definition, comp); if (defs.size() == 1 &&
     * isShared(defs.get(0))) { // shared component comp.setDefinition(null); ((Node)
     * comp).astSetDecoration("definition", definition); if (definition.startsWith("./")) {
     * definition = definition.substring(2); } final Component c =
     * getPathComponent(topLevelDefinition, definition); if (c == null) { throw new
     * ADLException(ComponentErrors.INVALID_PATH, comp, definition); } if
     * (!c.getName().equals(comps[i].getName())) { throw new
     * ADLException(ComponentErrors.SHARED_WITH_DIFFERENT_NAME, comp); } final Map<Node, Node>
     * replacements = new HashMap<Node, Node>();
     * 
     * // comp and c are in fact the same component, merge their descriptions // and replace there
     * references where needed. Node merged; try {
     * logger.debug("[PAArgumentComponentLoader]   Merging(rs): Node: "+ comp.toString() +
     * " ... Super: "+ c.toString()); merged = nodeMergerItf.merge(comp, c, nameAttributes); } catch
     * (final MergeException e) { throw new CompilerError(ComponentErrors.MERGE_ERROR, new
     * NodeErrorLocator(comp), e, definition); } if (comp != merged) { replacements.put(comp,
     * merged); } replacements.put(c, merged); replaceComponents(topLevelDefinition, replacements);
     * } } } }
     */

    //-------------------------------------------------------------------
    // Helpers
    //-------------------------------------------------------------------
    /**
     * Helper method to collect subcomponents in the functional part and inside the membrane
     * @param container
     * @return
     */
    protected Component[] getAllComponents(final ComponentContainer container) {

        Component[] fSubcomponents = container.getComponents();
        Component[] nfSubcomponents = null;

        if (container instanceof ControllerContainer) {
            Controller ctrl = ((ControllerContainer) container).getController();
            if (ctrl != null) {
                if (ctrl instanceof ComponentContainer) {
                    nfSubcomponents = ((ComponentContainer) ctrl).getComponents();
                }
            }
        }
        int nFsubcomponents = fSubcomponents.length;
        int nNFsubcomponents = (nfSubcomponents != null ? nfSubcomponents.length : 0);
        Component[] allSubcomponents = new Component[nFsubcomponents + nNFsubcomponents];
        int k = 0;
        for (int i = 0; i < nFsubcomponents; i++, k++) {
            allSubcomponents[k] = fSubcomponents[i];
        }
        for (int i = 0; i < nNFsubcomponents; i++, k++) {
            allSubcomponents[k] = nfSubcomponents[i];
        }
        return allSubcomponents;
    }

    /**
     * Helper method to collect the NF components
     * 
     * @param container
     * @return
     */
    protected Component[] getNFComponents(final ComponentContainer container) {

        Component[] nfSubcomponents = null;
        if (container instanceof ControllerContainer) {
            Controller ctrl = ((ControllerContainer) container).getController();
            if (ctrl != null) {
                if (ctrl instanceof ComponentContainer) {
                    nfSubcomponents = ((ComponentContainer) ctrl).getComponents();
                }
            }
        }
        if (nfSubcomponents == null) {
            return new Component[0];
        }
        return nfSubcomponents;
    }

}
