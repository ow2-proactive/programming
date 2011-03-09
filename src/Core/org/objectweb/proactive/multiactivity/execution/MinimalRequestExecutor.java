package org.objectweb.proactive.multiactivity.execution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.core.body.request.Request;

/**
 * Basic {@link RequestExecutor} that will start every submitted request on a new 
 * thread. Upon completion a callback method is called inside the service.
 * @author Izso
 *
 */
public class MinimalRequestExecutor implements RequestExecutor {
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private AtomicInteger active = new AtomicInteger(0);
    private RequestSupplier listener;
    
    public MinimalRequestExecutor(RequestSupplier listener) {
        this.listener = listener;
    }

    @Override
    public void submit(Request r) {
        active.incrementAndGet();
        executorService.submit(new RunnableRequest(r));
    }

    @Override
    public int getNumberOfReady() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNumberOfActive() {
        // TODO Auto-generated method stub
        return active.get();
    }

    @Override
    public int getNumberOfWaiting() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    /**
     * Wrapper that is used for the parallel serving of requests
     * @author Izso
     *
     */
    private class RunnableRequest implements Runnable {
        private Request r;

        public RunnableRequest(Request r) {
            this.r = r;
        }

        public Request getRequest() {
            return r;
        }

        @Override
        public void run() {
            listener.getServingBody().serve(r);
            listener.finished(r);
        }

    }

}