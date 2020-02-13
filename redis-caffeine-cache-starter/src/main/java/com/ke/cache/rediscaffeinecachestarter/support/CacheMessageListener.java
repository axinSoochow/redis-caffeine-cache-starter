package com.ke.cache.rediscaffeinecachestarter.support;

import com.alibaba.fastjson.parser.ParserConfig;
import com.sun.xml.internal.ws.util.UtilException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

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
		ParserConfig.getGlobalInstance().addAccept("com.axin.learn.rediscaffeine.support.");
	}

	public CacheMessageListener(RedisTemplate<Object, Object> redisTemplate,
			RedisCaffeineCacheManager redisCaffeineCacheManager) {
		this.redisTemplate = redisTemplate;
		this.redisCaffeineCacheManager = redisCaffeineCacheManager;
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		CacheMessage cacheMessage = (CacheMessage) redisTemplate.getValueSerializer().deserialize(message.getBody());
		logger.debug("接收到来自Ip:{}的消息", cacheMessage.getIp());
		logger.debug("收到redis清除缓存消息, 开始清除本地缓存, the cacheName is {}, the key is {}", cacheMessage.getCacheName(), cacheMessage.getKey());
		redisCaffeineCacheManager.clearLocal(cacheMessage.getCacheName(), cacheMessage.getKey());
	}

	//——————————————————————————————————————————————————————————————————————————
	/**
	 * 获取第一个本地IPv4地址
	 */
	public static String getLocalAddress() {
		Enumeration<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			throw new UtilException(e);
		}

		if (networkInterfaces == null) {
			throw new UtilException("Get network interface error!");
		}

		while (networkInterfaces.hasMoreElements()) {
			final NetworkInterface networkInterface = networkInterfaces.nextElement();
			final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
			while (inetAddresses.hasMoreElements()) {
				final InetAddress inetAddress = inetAddresses.nextElement();
				if (inetAddress.getHostAddress().length() <= 15
						&& !inetAddress.getHostAddress().equals("127.0.0.1")) {
					return inetAddress.getHostAddress();
				}
			}
		}
		return "";
	}

}
