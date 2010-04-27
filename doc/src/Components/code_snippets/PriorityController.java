
public interface PriorityController {

    /**
     * All the possible kinds of priority for a request on the component to which this interface belongs.
     *
     */
    public enum RequestPriority {
        /**
         * Functional priority
         */
        F,
        /**
         * Non-Functional priority
         */
        NF1,
        /**
         * Non-Functional priority higher than Functional priority (F)
         */
        NF2,
        /**
         * Non-Functional priority higher than Functional priority (F) and Non-Functional priorities (NF1 and
         * NF2)
         */
        NF3;
    }

    /**
     * Set priority of a method exposed by a server interface of the component to which this interface belongs.
     *
     * @param itfName Name of an interface of the component to which this interface belongs.
     * @param methodName Name of a method exposed by the interface corresponding to the given interface name.
     * @param parameterTypes Parameter types of the method corresponding to the given method name.
     * @param priority Priority to set to the method corresponding to the given method name.
     * @throws NoSuchInterfaceException If there is no such server interface.
     * @throws NoSuchMethodException If there is no such method.
     */
    public void setGCMPriority(String itfName, String methodName, Class<?>[] parameterTypes,
            RequestPriority priority) throws NoSuchInterfaceException, NoSuchMethodException;

    /**
     * Get the priority of a method exposed by a server interface of the component to which this interface
     * belongs.
     *
     * @param itfName Name of an interface of the component to which this interface belongs.
     * @param methodName Name of a method exposed by the interface corresponding to the given interface name.
     * @param parameterTypes Parameter types of the method corresponding to the given method name.
     * @return Priority of this method.
     * @throws NoSuchInterfaceException If there is no such server interface.
     * @throws NoSuchMethodException If there is no such method.
     */
    public RequestPriority getGCMPriority(String itfName, String methodName, Class<?>[] parameterTypes)
            throws NoSuchInterfaceException, NoSuchMethodException;
}
