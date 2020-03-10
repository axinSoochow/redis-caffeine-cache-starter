package com.ke.cache.rediscaffeinecachestarter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ConfigurationProperties(prefix = "cache.multi")
@Data
public class CacheRedisCaffeineProperties {

	/** 是否存储空值，默认true，防止缓存穿透*/
	private boolean cacheNullValues = true;
	
	/** 是否动态根据cacheName创建Cache的实现，默认true*/
	private boolean dynamic = true;
	
	/** 缓存key的前缀*/
	private String cachePrefix;

	private Redis redis = new Redis();
	
	private CacheDefault cacheDefault = new CacheDefault();
	private Cache15m cache15m = new Cache15m();
	private Cache30m cache30m = new Cache30m();
	private Cache60m cache60m = new Cache60m();
	private Cache180m cache180m = new Cache180m();
	private Cache12h cache12h = new Cache12h();

	@Data
	public class Redis {
		
		/** 全局过期时间，单位秒，默认不过期*/
		private long defaultExpiration = 0;
		
		/** 每个cacheName的过期时间，单位秒，优先级比defaultExpiration高*/
		private Map<String, Long> expires = new HashMap<>();
		
		/** 缓存更新时通知其他节点的topic名称*/
		private String topic = "cache:redis:caffeine:topic";

	}

	@Data
	public class CacheDefault {
		/** 访问后过期时间，单位秒*/
		protected long expireAfterAccess;
		/** 写入后过期时间，单位秒*/
		protected long expireAfterWrite = 120;
		/** 写入后刷新时间，单位秒*/
		protected long refreshAfterWrite;
		/** 初始化大小,默认50*/
		protected int initialCapacity = 50;
		/** 最大缓存对象个数*/
		protected long maximumSize = 50;
		
		/** 由于权重需要缓存对象来提供，对于使用spring cache这种场景不是很适合，所以暂不支持配置*/
//		private long maximumWeight;
	}
	public class Cache15m extends CacheDefault{}
	public class Cache30m extends CacheDefault{}
	public class Cache60m extends CacheDefault{}
	public class Cache180m extends CacheDefault{}
	public class Cache12h extends CacheDefault{}
}
