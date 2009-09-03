package unitTests.dataspaces;

import org.objectweb.proactive.extensions.dataspaces.core.naming.SpacesDirectory;
import org.objectweb.proactive.extensions.dataspaces.core.naming.SpacesDirectoryImpl;


public class SpacesDirectoryImplTest extends SpacesDirectoryAbstractBase {

    @Override
    protected SpacesDirectory getSource() {
        return new SpacesDirectoryImpl();
    }
}
