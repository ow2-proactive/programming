package org.objectweb.proactive.extensions.processbuilder;

import java.io.IOException;

import org.objectweb.proactive.extensions.processbuilder.exception.CoreBindingException;
import org.objectweb.proactive.extensions.processbuilder.exception.FatalProcessBuilderException;
import org.objectweb.proactive.extensions.processbuilder.exception.OSUserException;


/**
 * Class that wraps around {@link java.lang.ProcessBuilder}.<br>
 * It has no additional capabilities.
 * 
 * @author Zsolt Istvan
 * 
 */
public class BasicProcessBuilder extends OSProcessBuilder {

    @Override
    public Boolean canExecuteAsUser(OSUser user) {
        return false;
    }

    @Override
    public Boolean isCoreBindingSupported() {
        return false;
    }

    @Override
    protected String[] wrapCommand() {
        return command().toArray(new String[0]);
    }

    @Override
    protected Process setupAndStart() throws IOException, OSUserException, CoreBindingException,
            FatalProcessBuilderException {
        if (user() != null) {
            throw new OSUserException("Executing as given user is not supported for this operating system!");
        }
        if (cores() != null) {
            throw new CoreBindingException("Core binding is not supported for this operating system!");
        }

        return super.start();
    }

    @Override
    public CoreBindingDescriptor getAvaliableCoresDescriptor() {
        return null;
    }

}
