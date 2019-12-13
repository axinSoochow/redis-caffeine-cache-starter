package com.ke.cache.rediscaffeinecachestarter.support;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.ke.cache.rediscaffeinecachestarter.CacheRedisCaffeineProperties;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisCaffeineCacheManager implements CacheManager {
	
	private final Logger logger = LoggerFactory.getLogger(RedisCaffeineCacheManager.class);
	
	private ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>();
	
	private CacheRedisCaffeineProperties cacheRedisCaffeineProperties;
	
	private RedisTemplate<Object, Object> stringKeyRedisTemplate;

	private boolean dynamic = true;

	private Set<String> cacheNames;
	{
		cacheNames = new HashSet<>();
		cacheNames.add(CacheNames.CACHE_15MINS);
		cacheNames.add(CacheNames.CACHE_30MINS);
		cacheNames.add(CacheNames.CACHE_60MINS);
		cacheNames.add(CacheNames.CACHE_180MINS);
		cacheNames.add(CacheNames.CACHE_12HOUR);
	}
	public RedisCaffeineCacheManager(CacheRedisCaffeineProperties cacheRedisCaffeineProperties,
			RedisTemplate<Object, Object> stringKeyRedisTemplate) {
		super();
		this.cacheRedisCaffeineProperties = cacheRedisCaffeineProperties;
		this.stringKeyRedisTemplate = stringKeyRedisTemplate;
		this.dynamic = cacheRedisCaffeineProperties.isDynamic();
		this.cacheNames.addAll(cacheRedisCaffeineProperties.getCacheNames());
	}

	@Override
	public Cache getCache(String name) {
		Cache cache = cacheMap.get(name);
		if(cache != null) {
			return cache;
		}
		if(!dynamic && !cacheNames.contains(name)) {
			return cache;
		}
		
		cache = new RedisCaffeineCache(name, stringKeyRedisTemplate, caffeineCache(), cacheRedisCaffeineProperties);
		Cache oldCache = cacheMap.putIfAbsent(name, cache);
		logger.debug("create cache instance, the cache name is : {}", name);
		return oldCache == null ? cache : oldCache;
	}
	
	public com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache(){
		Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
		log.debug("本地缓存初始化：");
		if(cacheRedisCaffeineProperties.getCaffeine().getExpireAfterAccess() > 0) {
			log.debug("设置本地缓存访问后过期时间，{}秒", cacheRedisCaffeineProperties.getCaffeine().getExpireAfterAccess());
			cacheBuilder.expireAfterAccess(cacheRedisCaffeineProperties.getCaffeine().getExpireAfterAccess(), TimeUnit.SECONDS);
		}
		if(cacheRedisCaffeineProperties.getCaffeine().getExpireAfterWrite() > 0) {
			cacheBuilder.expireAfterWrite(cacheRedisCaffeineProperties.getCaffeine().getExpireAfterWrite(), TimeUnit.SECONDS);
		}
		if(cacheRedisCaffeineProperties.getCaffeine().getInitialCapacity() > 0) {
			log.debug("设置缓存初始化大小{}", cacheRedisCaffeineProperties.getCaffeine().getInitialCapacity());
			cacheBuilder.initialCapacity(cacheRedisCaffeineProperties.getCaffeine().getInitialCapacity());
		}
		if(cacheRedisCaffeineProperties.getCaffeine().getMaximumSize() > 0) {
			log.debug("设置本地缓存最大值{}", cacheRedisCaffeineProperties.getCaffeine().getMaximumSize());
			cacheBuilder.maximumSize(cacheRedisCaffeineProperties.getCaffeine().getMaximumSize());
		}
		if(cacheRedisCaffeineProperties.getCaffeine().getRefreshAfterWrite() > 0) {
			cacheBuilder.refreshAfterWrite(cacheRedisCaffeineProperties.getCaffeine().getRefreshAfterWrite(), TimeUnit.SECONDS);
		}
		return cacheBuilder.build();
	}

	@Override
	public Collection<String> getCacheNames() {
		return this.cacheNames;
	}
	
	public void clearLocal(String cacheName, Object key) {
		Cache cache = cacheMap.get(cacheName);
		if(cache == null) {
			return ;
		}
		
		RedisCaffeineCache redisCaffeineCache = (RedisCaffeineCache) cache;
		redisCaffeineCache.clearLocal(key);
	}
}
