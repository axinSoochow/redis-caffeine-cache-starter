package com.axin.idea.rediscaffeinecachestarter.support;

import com.alibaba.fastjson.parser.ParserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author axin
 * @since 2019-10-31
 * @summary 缓存监听器
 */
public class CacheMessageListener implements MessageListener {
	
	private final Logger logger = LoggerFactory.getLogger(CacheMessageListener.class);

	private RedisTemplate<Object, Object> redisTemplate;
	
	private RedisCaffeineCacheManager redisCaffeineCacheManager;

	{
		//打开json autotype功能
		ParserConfig.getGlobalInstance().addAccept("com.axin.idea.rediscaffeinecachestarter.support.");
	}

	public CacheMessageListener(RedisTemplate<Object, Object> redisTemplate,
			RedisCaffeineCacheManager redisCaffeineCacheManager) {
		this.redisTemplate = redisTemplate;
		this.redisCaffeineCacheManager = redisCaffeineCacheManager;
	}

	/**
	 * 利用 redis 发布订阅通知其他节点清除本地缓存
	 *
	 * @param message
	 * @param pattern
	 */
	@Override
	public void onMessage(Message message, byte[] pattern) {
		CacheMessage cacheMessage = (CacheMessage) redisTemplate.getValueSerializer().deserialize(message.getBody());
		logger.debug("收到redis清除缓存消息, 开始清除本地缓存, the cacheName is {}, the key is {}", cacheMessage.getCacheName(), cacheMessage.getKey());
		redisCaffeineCacheManager.clearLocal(cacheMessage.getCacheName(), cacheMessage.getKey());
	}
}
