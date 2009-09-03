package unitTests.dataspaces;

import org.objectweb.proactive.extensions.dataspaces.core.naming.CachingSpacesDirectory;
import org.objectweb.proactive.extensions.dataspaces.core.naming.SpacesDirectory;
import org.objectweb.proactive.extensions.dataspaces.core.naming.SpacesDirectoryImpl;


public class CachingSpacesDirectoryTest extends SpacesDirectoryAbstractBase {

    @Override
    protected SpacesDirectory getSource() throws Exception {
        return new CachingSpacesDirectory(new SpacesDirectoryImpl());
    }
}
