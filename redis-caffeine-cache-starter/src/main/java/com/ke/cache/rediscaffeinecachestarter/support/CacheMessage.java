package com.ke.cache.rediscaffeinecachestarter.support;

import lombok.Data;

import java.io.Serializable;

@Data
public class CacheMessage implements Serializable {

	/** */
	private static final long serialVersionUID = -1L;

	private String cacheName;
	
	private Object key;

	public CacheMessage(String cacheName, Object key) {
		super();
		this.cacheName = cacheName;
		this.key = key;
	}

}
