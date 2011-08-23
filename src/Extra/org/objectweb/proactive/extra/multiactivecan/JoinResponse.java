package org.objectweb.proactive.extra.multiactivecan;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class JoinResponse implements Serializable {
	
	private Router router;
	private ConcurrentHashMap<Key, Serializable> data;
	
	public JoinResponse(Router router, ConcurrentHashMap<Key, Serializable> data) {
		super();
		this.router = router;
		this.data = data;
	}
	
	public Router getRouter() {
		return router;
	}
	
	public void setRouter(Router router) {
		this.router = router;
	}
	
	public ConcurrentHashMap<Key, Serializable> getData() {
		return data;
	}
	
	public void setData(ConcurrentHashMap<Key, Serializable> data) {
		this.data = data;
	}
	
	

}
