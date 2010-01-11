/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.extensions.webservices.axis2.servicedeployer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.axis2.deployment.util.BeanExcludeInfo;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.java2wsdl.AnnotationConstants;
import org.apache.axis2.description.java2wsdl.DefaultSchemaGenerator;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.codehaus.jam.JAnnotation;
import org.codehaus.jam.JClass;
import org.codehaus.jam.JComment;
import org.codehaus.jam.JField;
import org.codehaus.jam.JProperty;
import org.codehaus.jam.JamClassIterator;
import org.codehaus.jam.JamService;
import org.codehaus.jam.JamServiceFactory;
import org.codehaus.jam.JamServiceParams;
import org.w3c.dom.Document;


/**
 * This class is an extension of the DefaultSchemaGenerator class which is used to expose
 * inherited methods. This functionality is not available by axis2 library. The only difference
 * between this class and its super class is <code>jclass.getMethods()</code> in the generateSchema
 * method which replaces <code>jclass.getDeclaredMethods()</code>.
 *
 * @author ProActiveTeam
 */
public class CustomDefaultSchemaGenerator extends DefaultSchemaGenerator {

    /**
     * @param loader
     * @param className
     * @param schematargetNamespace
     * @param schematargetNamespacePrefix
     * @param service
     * @throws Exception
     * @see org.apache.axis2.description.java2wsdl.DefaultSchemaGenerator
     */
    public CustomDefaultSchemaGenerator(ClassLoader loader, String className, String schematargetNamespace,
            String schematargetNamespacePrefix, AxisService service) throws Exception {
        super(loader, className, schematargetNamespace, schematargetNamespacePrefix, service);
    }

    /**
     * @see org.apache.axis2.description.java2wsdl.DefaultSchemaGenerator
     */
    //This will locate the custom schema file and add that into the schema map
    @SuppressWarnings("unchecked")
    private void loadCustomSchemaFile() throws Exception {
        if (customSchemaLocation != null) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            Document doc = documentBuilderFactory.newDocumentBuilder().parse(new File(customSchemaLocation));
            XmlSchema schema = xmlSchemaCollection.read(doc, null);
            schemaMap.put(schema.getTargetNamespace(), schema);
        }
    }

    /**
     * @see org.apache.axis2.description.java2wsdl.DefaultSchemaGenerator
     */
    private void loadMappingFile() throws IOException {
        if (mappingFileLocation != null) {
            File file = new File(mappingFileLocation);
            BufferedReader input = null;

            input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0 && line.charAt(0) != '#') {
                    String values[] = line.split("\\|");
                    if (values != null && values.length > 2) {
                        typeTable.addComplexSchema(values[0], new QName(values[1], values[2]));
                    }
                }
            }

            if (input != null) {
                input.close();
            }
        }
    }

    /**
     * @param javaType
     * @return
     * @throws Exception
     * @see org.apache.axis2.description.java2wsdl.DefaultSchemaGenerator
     */
    @SuppressWarnings("unchecked")
    private QName generateSchema(JClass javaType) throws Exception {
        String name = getQualifiedName(javaType);
        QName schemaTypeName = typeTable.getComplexSchemaType(name);
        if (schemaTypeName == null) {
            String simpleName = getSimpleName(javaType);

            String packageName = getQualifiedName(javaType.getContainingPackage());
            String targetNameSpace = resolveSchemaNamespace(packageName);

            XmlSchema xmlSchema = getXmlSchema(targetNameSpace);
            String targetNamespacePrefix = (String) targetNamespacePrefixMap.get(targetNameSpace);
            if (targetNamespacePrefix == null) {
                targetNamespacePrefix = generatePrefix();
                targetNamespacePrefixMap.put(targetNameSpace, targetNamespacePrefix);
            }

            XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);
            XmlSchemaSequence sequence = new XmlSchemaSequence();
            XmlSchemaComplexContentExtension complexExtension = new XmlSchemaComplexContentExtension();

            XmlSchemaElement eltOuter = new XmlSchemaElement();
            schemaTypeName = new QName(targetNameSpace, simpleName, targetNamespacePrefix);
            eltOuter.setName(simpleName);
            eltOuter.setQName(schemaTypeName);

            JClass sup = javaType.getSuperclass();

            if ((sup != null) && !("java.lang.Object".compareTo(sup.getQualifiedName()) == 0) &&
                !("org.apache.axis2".compareTo(sup.getContainingPackage().getQualifiedName()) == 0) &&
                !("java.util".compareTo(sup.getContainingPackage().getQualifiedName()) == 0)) {
                String superClassName = sup.getQualifiedName();
                String superclassname = getSimpleName(sup);
                String tgtNamespace;
                String tgtNamespacepfx;
                QName qName = typeTable.getSimpleSchemaTypeName(superClassName);
                if (qName != null) {
                    tgtNamespace = qName.getNamespaceURI();
                    tgtNamespacepfx = qName.getPrefix();
                } else {
                    tgtNamespace = resolveSchemaNamespace(sup.getContainingPackage().getQualifiedName());
                    tgtNamespacepfx = (String) targetNamespacePrefixMap.get(tgtNamespace);
                    QName superClassQname = generateSchema(sup);
                    if (superClassQname != null) {
                        tgtNamespacepfx = superClassQname.getPrefix();
                        tgtNamespace = superClassQname.getNamespaceURI();
                    }
                }

                if (tgtNamespacepfx == null) {
                    tgtNamespacepfx = generatePrefix();
                    targetNamespacePrefixMap.put(tgtNamespace, tgtNamespacepfx);
                }
                //if the parent class package name is differ from the child
                if (!((NamespaceMap) xmlSchema.getNamespaceContext()).values().contains(tgtNamespace)) {
                    XmlSchemaImport importElement = new XmlSchemaImport();
                    importElement.setNamespace(tgtNamespace);
                    xmlSchema.getItems().add(importElement);
                    ((NamespaceMap) xmlSchema.getNamespaceContext()).put(generatePrefix(), tgtNamespace);
                }

                QName basetype = new QName(tgtNamespace, superclassname, tgtNamespacepfx);

                complexExtension.setBaseTypeName(basetype);
                complexExtension.setParticle(sequence);

                XmlSchemaComplexContent contentModel = new XmlSchemaComplexContent();

                contentModel.setContent(complexExtension);

                complexType.setContentModel(contentModel);

            } else {
                complexType.setParticle(sequence);
            }

            complexType.setName(simpleName);

            //            xmlSchema.getItems().add(eltOuter);
            xmlSchema.getElements().add(schemaTypeName, eltOuter);
            eltOuter.setSchemaTypeName(complexType.getQName());

            xmlSchema.getItems().add(complexType);
            xmlSchema.getSchemaTypes().add(schemaTypeName, complexType);

            // adding this type to the table
            typeTable.addComplexSchema(name, eltOuter.getQName());
            // adding this type's package to the table, to support inheritance.
            typeTable.addComplexSchema(javaType.getContainingPackage().getQualifiedName(), eltOuter
                    .getQName());

            Set propertiesSet = new HashSet();
            Set propertiesNames = new HashSet();

            JProperty[] tempProperties = javaType.getDeclaredProperties();
            BeanExcludeInfo beanExcludeInfo = null;
            if (service.getExcludeInfo() != null) {
                beanExcludeInfo = service.getExcludeInfo().getBeanExcludeInfoForClass(
                        javaType.getQualifiedName());
            }
            for (int i = 0; i < tempProperties.length; i++) {
                JProperty tempProperty = tempProperties[i];
                String propertyName = getCorrectName(tempProperty.getSimpleName());
                if ((beanExcludeInfo == null) || !beanExcludeInfo.isExcludedProperty(propertyName)) {
                    propertiesSet.add(tempProperty);
                }
            }

            JProperty[] properties = (JProperty[]) propertiesSet.toArray(new JProperty[0]);
            Arrays.sort(properties);
            for (int i = 0; i < properties.length; i++) {
                JProperty property = properties[i];
                boolean isArryType = property.getType().isArrayType();

                String propname = getCorrectName(property.getSimpleName());

                propertiesNames.add(propname);

                this.generateSchemaforFieldsandProperties(xmlSchema, sequence, property.getType(), propname,
                        isArryType);

            }

            JField[] tempFields = javaType.getDeclaredFields();
            HashMap FieldMap = new HashMap();

            for (int i = 0; i < tempFields.length; i++) {
                // create a element for the field only if it is public
                // and there is no property with the same name
                if (tempFields[i].isPublic()) {
                    if (tempFields[i].isStatic()) {
                        //                        We do not need to expose static fields
                        continue;
                    }
                    String propertyName = getCorrectName(tempFields[i].getSimpleName());
                    if ((beanExcludeInfo == null) || !beanExcludeInfo.isExcludedProperty(propertyName)) {
                        // skip field with same name as a property
                        if (!propertiesNames.contains(tempFields[i].getSimpleName())) {

                            FieldMap.put(tempFields[i].getSimpleName(), tempFields[i]);
                        }
                    }

                }

            }

            // remove fields from super classes patch for defect Annogen-21
            // getDeclaredFields is incorrectly returning fields of super classes as well
            // getDeclaredProperties used earlier works correctly
            JClass supr = javaType.getSuperclass();
            while (supr != null && supr.getQualifiedName().compareTo("java.lang.Object") != 0) {
                JField[] suprFields = supr.getFields();
                for (int i = 0; i < suprFields.length; i++) {
                    FieldMap.remove(suprFields[i].getSimpleName());
                }
                supr = supr.getSuperclass();
            }
            // end patch for Annogen -21

            JField[] froperties = (JField[]) FieldMap.values().toArray(new JField[0]);
            Arrays.sort(froperties);

            for (int i = 0; i < froperties.length; i++) {
                JField field = froperties[i];
                boolean isArryType = field.getType().isArrayType();

                this.generateSchemaforFieldsandProperties(xmlSchema, sequence, field.getType(), field
                        .getSimpleName(), isArryType);
            }

        }
        return schemaTypeName;
    }

    /**
     * @return Collection
     * @throws Exception
     * @see org.apache.axis2.description.java2wsdl.DefaultSchemaGenerator
     */
    @SuppressWarnings("unchecked")
    public Collection generateSchema() throws Exception {
        loadCustomSchemaFile();
        loadMappingFile();
        JamServiceFactory factory = JamServiceFactory.getInstance();
        JamServiceParams jam_service_parms = factory.createServiceParams();
        //setting the classLoder
        //it can possible to add the classLoader as well
        jam_service_parms.addClassLoader(classLoader);
        jam_service_parms.includeClass(className);

        for (int count = 0; count < getExtraClasses().size(); ++count) {
            jam_service_parms.includeClass((String) getExtraClasses().get(count));
        }
        JamService jamService = factory.createService(jam_service_parms);
        QName extraSchemaTypeName;
        JamClassIterator jClassIter = jamService.getClasses();
        //all most all the time the ittr will have only one class in it
        while (jClassIter.hasNext()) {
            JClass jclass = (JClass) jClassIter.next();
            if (getActualQualifiedName(jclass).equals(className)) {
                /**
                 * Schema generation done in two stage 1. Load all the methods and
                 * create type for methods parameters (if the parameters are Bean
                 * then it will create Complex types for those , and if the
                 * parameters are simple type which describe in SimpleTypeTable
                 * nothing will happen) 2. In the next stage for all the methods
                 * messages and port types will be created
                 */
                JAnnotation annotation = jclass.getAnnotation(AnnotationConstants.WEB_SERVICE);
                JComment comment = jclass.getComment();
                if (comment != null) {
                    System.out.println(comment.getText());
                }
                if (annotation != null) {
                    String tns = annotation.getValue(AnnotationConstants.TARGETNAMESPACE).asString();
                    if (tns != null && !"".equals(tns)) {
                        targetNamespace = tns;
                        schemaTargetNameSpace = tns;
                    }
                    service.setName(Utils.getAnnotatedServiceName(serviceClass, annotation));
                }
                methods = processMethods(jclass.getMethods());

            } else {
                //generate the schema type for extra classes
                extraSchemaTypeName = typeTable.getSimpleSchemaTypeName(getQualifiedName(jclass));
                if (extraSchemaTypeName == null) {
                    generateSchema(jclass);
                }
            }
        }
        return schemaMap.values();
    }
}
