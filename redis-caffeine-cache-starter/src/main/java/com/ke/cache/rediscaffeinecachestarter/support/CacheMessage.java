package com.ke.cache.rediscaffeinecachestarter.support;

import lombok.Data;

import java.io.Serializable;

@Data
public class CacheMessage implements Serializable {

	/** */
	private static final long serialVersionUID = -1L;

	private String cacheName;
	
	private Object key;

	private String ip;

	public CacheMessage(String cacheName, Object key, String ip) {
		super();
		this.cacheName = cacheName;
		this.key = key;
		this.ip = ip;
	}

}
