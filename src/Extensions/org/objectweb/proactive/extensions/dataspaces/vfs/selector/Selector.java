/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.objectweb.proactive.extensions.dataspaces.vfs.selector;

import java.util.List;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;


/** An helper class to select a set of files.
 * 
 * Since {@link DataSpacesFileObject#findFiles(org.objectweb.proactive.extensions.dataspaces.api.FileSelector)}
 * cannot only takes a predefined selector as parameter, this class allow to pass a custom
 * selector 
 */
public class Selector {

    /**
     * Traverses the descendants of this file, and builds a list of selected
     * files.
     */
    static public void findFiles(final DataSpacesFileObject fo, final FileSelector selector,
            final boolean depthwise, final List<DataSpacesFileObject> selected) throws FileSystemException {
        try {
            if (fo.exists()) {
                // Traverse starting at this file
                final FileSelectInfo info = new FileSelectInfo();
                info.setBaseFolder(fo);
                info.setDepth(0);
                info.setFile(fo);
                traverse(info, selector, depthwise, selected);
            }
        } catch (final Exception e) {
            throw new FileSystemException(e);
        }
    }

    /**
     * Traverses a file.
     */
    private static void traverse(final FileSelectInfo fileInfo, final FileSelector selector,
            final boolean depthwise, final List<DataSpacesFileObject> selected) throws Exception {
        // Check the file itself
        final DataSpacesFileObject file = fileInfo.getFile();
        final int index = selected.size();

        // If the file is a folder, traverse it
        if (file.getType().hasChildren() && selector.traverseDescendents(fileInfo) && file.isReadable()) {
            final int curDepth = fileInfo.getDepth();
            fileInfo.setDepth(curDepth + 1);

            // Traverse the children
            final List<DataSpacesFileObject> children = file.getChildren();
            for (int i = 0; i < children.size(); i++) {
                final DataSpacesFileObject child = children.get(i);
                fileInfo.setFile(child);
                traverse(fileInfo, selector, depthwise, selected);
            }

            fileInfo.setFile(file);
            fileInfo.setDepth(curDepth);
        }

        // Add the file if doing depthwise traversal
        if (selector.includeFile(fileInfo)) {
            if (depthwise) {
                // Add this file after its descendents
                selected.add(file);
            } else {
                // Add this file before its descendents
                selected.add(index, file);
            }
        }
    }
}
