package org.objectweb.proactive.extensions.masterworker.core;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.MasterIntern;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.ResultIntern;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * The SubMasterImpl class is acting as a client on the worker side to talk to the master, 
 * submit tasks, wait results, etc... <br>
 *
 * @author The ProActive Team
 */
public class SubMasterImpl implements SubMaster<Task<Serializable>, Serializable>, Serializable {

    private final MasterIntern master;
    private final String originatorName;
    private boolean initCalled = false;

    public SubMasterImpl(MasterIntern master, String originatorName) {
        this.master = master;
        this.originatorName = originatorName;
    }

    public void setResultReceptionOrder(OrderingMode mode) {
        master.setResultReceptionOrder(originatorName, mode);
    }

    /**
    * {@inheritDoc}
    */
    public void solve(List<Task<Serializable>> tasks) {
        if (tasks.size() == 0) {
            throw new IllegalArgumentException("empty list");
        }
        master.solveIntern(originatorName, tasks);
        initCalled = true;
    }

    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public List<Serializable> waitAllResults() throws TaskException {
        if (!initCalled) {
            throw new IllegalStateException("A call to solve should occur before this call.");
        }
        if (master.isEmpty(originatorName)) {
            throw new IllegalStateException("Master is empty, call to this method will wait forever");
        }

        List<Serializable> results = null;
        try {
            results = (List<Serializable>) PAFuture.getFutureValue(master.waitAllResults(originatorName));
        } catch (RuntimeException e) {
            Throwable texp = findTaskException(e);
            if (texp != null) {
                throw (TaskException) texp;
            } else
                throw e;
        }

        return results;
    }

    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public Serializable waitOneResult() throws TaskException {
        if (!initCalled) {
            throw new IllegalStateException("A call to solve should occur before this call.");
        }
        if (master.isEmpty(originatorName)) {
            throw new IllegalStateException("Master is empty, call to this method will wait forever");
        }
        Serializable result = null;
        try {
            result = (Serializable) PAFuture.getFutureValue(master.waitOneResult(originatorName));
        } catch (RuntimeException e) {
            Throwable texp = findTaskException(e);
            if (texp != null) {
                throw (TaskException) texp;
            } else
                throw e;
        }

        return result;

    }

    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public List<Serializable> waitKResults(int k) throws TaskException {
        if (!initCalled) {
            throw new IllegalStateException("A call to solve should occur before this call.");
        }
        if (master.countPending(originatorName) < k) {
            throw new IllegalStateException("Number of tasks submitted previously is strictly less than " +
                k + ": call to this method will wait forever");
        }
        List<Serializable> results = null;
        try {
            results = (List<Serializable>) PAFuture.getFutureValue(master.waitKResults(originatorName, k));
        } catch (RuntimeException e) {
            Throwable texp = findTaskException(e);
            if (texp != null) {
                throw (TaskException) texp;
            } else
                throw e;
        }

        return results;
    }

    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public boolean isEmpty() {
        if (!initCalled) {
            throw new IllegalStateException("A call to solve should occur before this call.");
        }
        return master.isEmpty(originatorName);
    }

    /**
    * {@inheritDoc}
    */
    @SuppressWarnings("unchecked")
    public int countAvailableResults() {
        if (!initCalled) {
            throw new IllegalStateException("A call to solve should occur before this call.");
        }
        return master.countAvailableResults(originatorName);
    }

    private Throwable findTaskException(Throwable e) {
        if (e instanceof TaskException) {
            return e;
        }
        if (e.getCause() != null) {
            return findTaskException(e.getCause());
        } else {
            return null;
        }
    }
}
