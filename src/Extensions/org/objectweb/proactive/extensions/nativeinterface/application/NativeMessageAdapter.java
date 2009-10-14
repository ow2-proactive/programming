package org.objectweb.proactive.extensions.nativeinterface.application;

/**
 * The NativeMessageAdapter defines how to handle serialization and
 * init of native coupled applications
 *
 */
public interface NativeMessageAdapter {

    public NativeMessage deserialize(byte[] serializedMsg);

    public byte[] buildInitMessage(int nativeRank, int nativeJobID, int nbJob);

}
