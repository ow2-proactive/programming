package org.objectweb.proactive.mpi.control;

import java.io.Serializable;

public class NativeData implements Serializable {

    private byte[] data;
    
	public NativeData() {
		
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
