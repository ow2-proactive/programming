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
package dataspaces;

import java.util.Collection;

import org.apache.commons.vfs2.FileSelectInfo;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.FileSelector;


/**
 * @author ActiveEon Team
 * @since 12/07/2017
 */
public class PatternDepthFileSelector extends FileSelector {

    private final int maxDepth;

    private final int minDepth;

    public PatternDepthFileSelector() {
        super();
        minDepth = 0;
        maxDepth = Integer.MAX_VALUE;
    }

    public PatternDepthFileSelector(int minDepth, int maxDepth, Collection<String> includes,
            Collection<String> excludes) {
        super(includes, excludes);
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
    }

    public PatternDepthFileSelector(int minDepth, int maxDepth, String[] includes, String[] excludes) {
        super(includes, excludes);
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
    }

    public PatternDepthFileSelector(int minDepth, int maxDepth, String... includes) {
        super(includes);
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
    }

    @Override
    public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        int depth = fileInfo.getDepth();
        return (depth >= minDepth && depth <= maxDepth) && super.includeFile(fileInfo);
    }

    @Override
    public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        return (fileInfo.getDepth() < maxDepth) && super.traverseDescendents(fileInfo);
    }

}
