# 分布式二级缓存

在生产中已有实践，本组件仅做个人学习交流分享使用。

## 所谓二级缓存
> 缓存就是将数据从读取较慢的介质上读取出来放到读取较快的介质上，如磁盘-->内存。

平时我们会将数据存储到磁盘上，如：数据库。如果每次都从数据库里去读取，会因为磁盘本身的IO影响读取速度，所以就有了像redis这种的内存缓存。可以将数据读取出来放到内存里，这样当需要获取数据时，就能够直接从内存中拿到数据返回，能够很大程度的提高速度。

但是一般redis是单独部署成集群，所以会有网络IO上的消耗，虽然与redis集群的链接已经有连接池这种工具，但是数据传输上也还是会有一定消耗。所以就有了进程内缓存，如：caffeine。当应用内缓存有符合条件的数据时，就可以直接使用，而不用通过网络到redis中去获取，这样就形成了两级缓存。应用内缓存叫做一级缓存，远程缓存（如redis）叫做二级缓存。

## 系统是否需要缓存

- **CPU占用**:如果你有某些应用需要消耗大量的cpu去计算获得结果。
- **数据库IO占用**:如果你发现你的数据库连接池比较空闲，那么不应该用缓存。但是如果数据库连接池比较繁忙，甚至经常报出连接不够的报警，那么是时候应该考虑缓存了。

## 分布式二级缓存的优势

Redis用来存储热点数据，Redis中没有的数据则直接去数据库访问。

已经有Redis了，干嘛还需要了解Guava，Caffeine这些进程缓存呢:

- Redis如果不可用，这个时候我们只能访问数据库，很容易造成雪崩，但一般不会出现这种情况。
- 访问Redis会有一定的网络I/O以及序列化反序列化开销，虽然性能很高但是其终究没有本地方法快，可以将最热的数据存放在本地，以便进一步加快访问速度。这个思路并不是我们做互联网架构独有的，在计算机系统中使用L1,L2,L3多级缓存，用来减少对内存的直接访问，从而加快访问速度。

所以如果仅仅是使用Redis，能满足我们大部分需求，但是当需要追求更高的性能以及更高的可用性的时候，那就不得不了解多级缓存。

## 二级缓存操作过程

数据读流程 | 描述
---|---
![image](http://axin-soochow.oss-cn-hangzhou.aliyuncs.com/21-10/lock.png)| redis 与本地缓存都查询不到值的时候，会触发更新过程，整个过程是加锁的


缓存失效流程 | 描述
---|---
![image](http://axin-soochow.oss-cn-hangzhou.aliyuncs.com/21-10/shixiao.png)| redis更新与删除缓存key都会触发，清除redis缓存后


## 如何使用组件？

组件是基于Spring Cache框架上改造的，在项目中使用分布式缓存，仅仅需要在缓存注解上增加：cacheManager ="L2_CacheManager"，或者 cacheManager = CacheRedisCaffeineAutoConfiguration.分布式二级缓存


```Java
//这个方法会使用分布式二级缓存来提供查询
@Cacheable(cacheNames = CacheNames.CACHE_12HOUR, cacheManager = "L2_CacheManager")
public Config getAllValidateConfig() { 
}
```

如果你想既使用分布式缓存，又想用分布式二级缓存组件，那你需要向Spring注入一个 @Primary 的 CacheManager bean 然后:

```Java
//这个方法会使用分布式二级缓存
@Cacheable(cacheNames = CacheNames.CACHE_12HOUR, cacheManager = "L2_CacheManager")
public Config getAllValidateConfig() {
}

//这个方法会使用分布式缓存
@Cacheable(cacheNames = CacheNames.CACHE_12HOUR)
public Config getAllValidateConfig2() {
}
```

