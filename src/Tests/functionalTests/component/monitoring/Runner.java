package functionalTests.component.monitoring;

public interface Runner {
    public void run();

    public int getTotalNbMethodCalls();

    public long getSleepTime();

    public String[] getItfNamesForEachMethod();

    public String[] getMethodNames();

    public int[] getNbCallsPerMethod();
}
