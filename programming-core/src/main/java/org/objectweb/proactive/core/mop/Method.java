/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.mop;

import java.util.ArrayList;
import java.util.List;
import javassist.CtBehavior;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;


/**
 *
 * This class holds all the annotations a given method has. If the method is inherited
 * from superclass or from interfaces, this object also used to store annotations
 * available in the class hierarchy.
 *
 */
public class Method {
    private CtMethod method;

    private List<Annotation> methodAnnotation;

    private List<MethodParameter> listMethodParameters;

    public List<MethodParameter> getListMethodParameters() {
        return listMethodParameters;
    }

    public void setListMethodParameters(List<MethodParameter> lmp) {
        this.listMethodParameters = lmp;
    }

    public Method(CtMethod method) {
        this.method = method;
        methodAnnotation = new ArrayList<Annotation>();
        listMethodParameters = new ArrayList<MethodParameter>();

        try {
            // PROACTIVE-1138
            for (int i = 0; i < method.getParameterTypes().length; i++) {
                // initialize the list of parameters
                listMethodParameters.add(new MethodParameter());
            }
        } catch (NotFoundException nfe) {
            throw new RuntimeException(nfe);
        }

        grabMethodandParameterAnnotation(method);
    }

    public CtMethod getCtMethod() {
        return method;
    }

    public void setCtMethod(CtMethod method) {
        this.method = method;
    }

    public List<Annotation> getMethodAnnotation() {
        return methodAnnotation;
    }

    public void setMethodAnnotation(List<Annotation> methodAnnotation) {
        this.methodAnnotation = methodAnnotation;
    }

    private static ParameterAnnotationsAttribute toParameterAnnotationsAttribute(CtBehavior ctBehavior) {
        MethodInfo minfo = ctBehavior.getMethodInfo();
        ParameterAnnotationsAttribute attr = (ParameterAnnotationsAttribute) minfo.getAttribute(ParameterAnnotationsAttribute.visibleTag);
        return attr;
    }

    public void grabMethodandParameterAnnotation(CtBehavior ctBehavior) {

        MethodInfo minfo = ctBehavior.getMethodInfo();
        AnnotationsAttribute methodattr = (AnnotationsAttribute) minfo.getAttribute(AnnotationsAttribute.visibleTag);

        if (methodattr != null) {
            Annotation[] methodAnn = methodattr.getAnnotations();

            for (Annotation object : methodAnn) {
                JavassistByteCodeStubBuilder.logger.debug("adding annotation " + object.getTypeName() + " to " +
                                                          ctBehavior.getLongName());
                methodAnnotation.add(object);
            }
        }

        // get parameter annotations

        ParameterAnnotationsAttribute attr = toParameterAnnotationsAttribute(ctBehavior);
        if (attr == null) {
            return;
        }

        javassist.bytecode.annotation.Annotation[][] parametersAnnotations = attr.getAnnotations();

        //        if (listMethodParameters.size() > 0) {

        for (int paramIndex = 0; paramIndex < parametersAnnotations.length; paramIndex++) {

            javassist.bytecode.annotation.Annotation[] paramAnnotations = parametersAnnotations[paramIndex];
            for (javassist.bytecode.annotation.Annotation parameterAnnotation : paramAnnotations) {

                MethodParameter mp = listMethodParameters.get(paramIndex);
                if (mp == null) {
                    mp = new MethodParameter();
                    listMethodParameters.set(paramIndex, mp);
                }
                JavassistByteCodeStubBuilder.logger.debug("adding annotation " + parameterAnnotation.getTypeName() +
                                                          " to param " + paramIndex + " of " +
                                                          ctBehavior.getLongName());
                mp.getAnnotations().add(parameterAnnotation);
            }
        }
    }

    public boolean hasMethodAnnotation(Class<?> annotation) {

        for (Annotation object : methodAnnotation.toArray(new Annotation[] {})) {
            if (annotation.getName().equals(object.getTypeName())) {
                return true;
            }
        }
        return false;
    }

}
