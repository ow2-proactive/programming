/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.extensions.annotation.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.Migratable;
import org.objectweb.proactive.extensions.annotation.NodeAttachmentCallback;
import org.objectweb.proactive.extensions.annotation.OnArrival;
import org.objectweb.proactive.extensions.annotation.OnDeparture;
import org.objectweb.proactive.extensions.annotation.RemoteObject;
import org.objectweb.proactive.extensions.annotation.VirtualNodeIsReadyCallback;
import org.objectweb.proactive.extensions.annotation.activeobject.ActiveObjectVisitorAPT;
import org.objectweb.proactive.extensions.annotation.callbacks.isready.VirtualNodeIsReadyCallbackVisitorAPT;
import org.objectweb.proactive.extensions.annotation.callbacks.nodeattachment.NodeAttachmentCallbackVisitorAPT;
import org.objectweb.proactive.extensions.annotation.common.UtilsAPT;
import org.objectweb.proactive.extensions.annotation.migratable.MigratableVisitorAPT;
import org.objectweb.proactive.extensions.annotation.migration.strategy.OnArrivalVisitorAPT;
import org.objectweb.proactive.extensions.annotation.migration.strategy.OnDepartureVisitorAPT;
import org.objectweb.proactive.extensions.annotation.remoteobject.RemoteObjectVisitorAPT;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.util.SimpleDeclarationVisitor;


/**
 * The AnnotationProcessor that processes the ActiveObject annotation.
 * It processes only objects.
 * For every object encountered, the ActiveObjectVisitor is used to
 * visit the declaration.
 *
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 3.90
 */

public class ProActiveProcessorAPT implements AnnotationProcessor {

    private final AnnotationProcessorEnvironment _environment;
    private final Messager _messager;

    private final Map<Class<?>, SimpleDeclarationVisitor> _annotationVisitors = new HashMap<Class<?>, SimpleDeclarationVisitor>();
    private final Map<String, AnnotationTypeDeclaration> _annotationDefinitions = new HashMap<String, AnnotationTypeDeclaration>();
    private final Map<AnnotationTypeDeclaration, Collection<Declaration>> _annotatedElements = new HashMap<AnnotationTypeDeclaration, Collection<Declaration>>();

    public ProActiveProcessorAPT(AnnotationProcessorEnvironment env) {

        _environment = env;
        _messager = _environment.getMessager();

        // populate the map of visitors
        populateAVMap();
        // populate the map of annotation definitions and annotated elements
        populateADMap();

    }

    private void populateAVMap() {
        _annotationVisitors.put(ActiveObject.class, new ActiveObjectVisitorAPT(_messager));
        _annotationVisitors.put(RemoteObject.class, new RemoteObjectVisitorAPT(_messager));
        _annotationVisitors.put(OnDeparture.class, new OnDepartureVisitorAPT(_messager));
        _annotationVisitors.put(OnArrival.class, new OnArrivalVisitorAPT(_messager));
        _annotationVisitors
                .put(NodeAttachmentCallback.class, new NodeAttachmentCallbackVisitorAPT(_messager));
        _annotationVisitors.put(VirtualNodeIsReadyCallback.class, new VirtualNodeIsReadyCallbackVisitorAPT(
            _messager));
        _annotationVisitors.put(Migratable.class, new MigratableVisitorAPT(_messager));
    }

    private void populateADMap() {
        for (Class<?> annotation : _annotationVisitors.keySet()) {
            String annotName = annotation.getName();
            AnnotationTypeDeclaration annotDeclaration = (AnnotationTypeDeclaration) _environment
                    .getTypeDeclaration(annotName);
            _annotationDefinitions.put(annotName, annotDeclaration);
            if (annotDeclaration != null)
                _annotatedElements.put(annotDeclaration, _environment
                        .getDeclarationsAnnotatedWith(annotDeclaration));
        }
    }

    public void process() {
        for (Entry<Class<?>, SimpleDeclarationVisitor> av_pair : _annotationVisitors.entrySet()) {

            Class<?> annotation = av_pair.getKey();
            String annotName = annotation.getName();

            AnnotationTypeDeclaration annotDeclaration = _annotationDefinitions.get(annotName);
            if (annotDeclaration == null) {
                _messager.printError("Cannot load class definition of annotation @" + annotName +
                    ". This annotation will NOT be processed");
                continue;
            }

            Target applicableOn = annotDeclaration.getAnnotation(Target.class);

            SimpleDeclarationVisitor visitor = av_pair.getValue();
            if (visitor == null) {
                _messager.printError("Cannot find the visitor for annotation @" + annotName +
                    ". This annotation will NOT be processed");
                continue;
            }

            for (Declaration typeDeclaration : _annotatedElements.get(annotDeclaration)) {

                if (applicableOn != null && applicableOn.value() != null)
                    if (!testSuitableDeclaration(typeDeclaration, applicableOn)) {
                        _messager.printError(typeDeclaration.getPosition(), "[ERROR] The @" +
                            annotation.getSimpleName() +
                            "annotation is not applicable for this type of Java construct.");
                    }

                // check using the visitor
                typeDeclaration.accept(visitor);
            }
        }
    }

    private boolean testSuitableDeclaration(Declaration typeDeclaration, Target applicableOn) {

        for (ElementType applicableType : applicableOn.value()) {
            if (UtilsAPT.applicableOnDeclaration(applicableType, typeDeclaration))
                return true;
        }
        return false;
    }

}
