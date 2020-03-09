package com.ke.cache.rediscaffeinecachestarter.support;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.ke.cache.rediscaffeinecachestarter.CacheRedisCaffeineProperties;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisCaffeineCacheManager implements CacheManager {
	
	private final Logger logger = LoggerFactory.getLogger(RedisCaffeineCacheManager.class);
	
	private static ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>();
	
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
	}

	//——————————————————————— 进行缓存工具 ——————————————————————
	/**
	 * 清除所有进程缓存
	 */
	public void clearAllCache() {
		stringKeyRedisTemplate.convertAndSend(cacheRedisCaffeineProperties.getRedis().getTopic(), new CacheMessage(null, null));
	}

	/**
	 * 返回所有进程缓存(二级缓存)的统计信息
	 * result:{"缓存名称":统计信息}
	 * @return
	 */
	public static Map<String, CacheStats> getCacheStats() {
		if (CollectionUtils.isEmpty(cacheMap)) {
			return null;
		}

		Map<String, CacheStats> result = new LinkedHashMap<>();
		for (Cache cache : cacheMap.values()) {
			RedisCaffeineCache caffeineCache = (RedisCaffeineCache) cache;
			result.put(caffeineCache.getName(), caffeineCache.getCaffeineCache().stats());
		}
		return result;
	}

	//—————————————————————————— core —————————————————————————
	@Override
	public Cache getCache(String name) {
		Cache cache = cacheMap.get(name);
		if(cache != null) {
			return cache;
		}
		if(!dynamic && !cacheNames.contains(name)) {
			return null;
		}
		
		cache = new RedisCaffeineCache(name, stringKeyRedisTemplate, caffeineCache(name), cacheRedisCaffeineProperties);
		Cache oldCache = cacheMap.putIfAbsent(name, cache);
		logger.debug("create cache instance, the cache name is : {}", name);
		return oldCache == null ? cache : oldCache;
	}
	
	@Override
	public Collection<String> getCacheNames() {
		return this.cacheNames;
	}
	
	public void clearLocal(String cacheName, Object key) {
		//cacheName为null 清除所有进程缓存
		if (cacheName == null) {
			log.info("清除所有本地缓存");
			cacheMap = new ConcurrentHashMap<>();
			return;
		}

		Cache cache = cacheMap.get(cacheName);
		if(cache == null) {
			return;
		}
		
		RedisCaffeineCache redisCaffeineCache = (RedisCaffeineCache) cache;
		redisCaffeineCache.clearLocal(key);
	}

	/**
     * 实例化本地一级缓存
	 * @param name
     * @return
     */
	private com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache(String name) {
		Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
		CacheRedisCaffeineProperties.CacheDefault cacheConfig;
		switch (name) {
			case CacheNames.CACHE_15MINS:
				cacheConfig = cacheRedisCaffeineProperties.getCache15m();
				break;
			case CacheNames.CACHE_30MINS:
				cacheConfig = cacheRedisCaffeineProperties.getCache30m();
				break;
			case CacheNames.CACHE_60MINS:
				cacheConfig = cacheRedisCaffeineProperties.getCache60m();
				break;
			case CacheNames.CACHE_180MINS:
				cacheConfig = cacheRedisCaffeineProperties.getCache180m();
				break;
			case CacheNames.CACHE_12HOUR:
				cacheConfig = cacheRedisCaffeineProperties.getCache12h();
				break;
			default:
				cacheConfig = cacheRedisCaffeineProperties.getCacheDefault();
		}
		long expireAfterAccess = cacheConfig.getExpireAfterAccess();
		long expireAfterWrite = cacheConfig.getExpireAfterWrite();
		int initialCapacity = cacheConfig.getInitialCapacity();
		long maximumSize = cacheConfig.getMaximumSize();
		long refreshAfterWrite = cacheConfig.getRefreshAfterWrite();

		log.debug("本地缓存初始化：");
		if (expireAfterAccess > 0) {
			log.debug("设置本地缓存访问后过期时间，{}秒", expireAfterAccess);
			cacheBuilder.expireAfterAccess(expireAfterAccess, TimeUnit.SECONDS);
		}
		if (expireAfterWrite > 0) {
			log.debug("设置本地缓存写入后过期时间，{}秒", expireAfterWrite);
			cacheBuilder.expireAfterWrite(expireAfterWrite, TimeUnit.SECONDS);
		}
		if (initialCapacity > 0) {
			log.debug("设置缓存初始化大小{}", initialCapacity);
			cacheBuilder.initialCapacity(initialCapacity);
		}
		if (maximumSize > 0) {
			log.debug("设置本地缓存最大值{}", maximumSize);
			cacheBuilder.maximumSize(maximumSize);
		}
		if (refreshAfterWrite > 0) {
			cacheBuilder.refreshAfterWrite(refreshAfterWrite, TimeUnit.SECONDS);
		}
		cacheBuilder.recordStats();
		return cacheBuilder.build();
	}
}
