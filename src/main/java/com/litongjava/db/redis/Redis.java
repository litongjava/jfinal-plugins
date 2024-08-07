package com.litongjava.db.redis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.jfinal.kit.StrKit;
import com.litongjava.tio.utils.json.JsonUtils;

import redis.clients.jedis.Jedis;

/**
 * Redis.
 * redis 工具类
 * <pre>
 * 例如：
 * Redis.use().set("key", "value");
 * Redis.use().get("key");
 * </pre>
 */
public class Redis {

  static Cache mainCache = null;

  private static final ConcurrentHashMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>(32, 0.5F);

  public static void addCache(Cache cache) {
    if (cache == null)
      throw new IllegalArgumentException("cache can not be null");
    if (cacheMap.containsKey(cache.getName()))
      throw new IllegalArgumentException("The cache name already exists");

    cacheMap.put(cache.getName(), cache);
    if (mainCache == null)
      mainCache = cache;
  }

  public static Cache removeCache(String cacheName) {
    return cacheMap.remove(cacheName);
  }

  /**
   * 提供一个设置设置主缓存 mainCache 的机会，否则第一个被初始化的 Cache 将成为 mainCache
   */
  public static void setMainCache(String cacheName) {
    if (StrKit.isBlank(cacheName))
      throw new IllegalArgumentException("cacheName can not be blank");
    cacheName = cacheName.trim();
    Cache cache = cacheMap.get(cacheName);
    if (cache == null)
      throw new IllegalArgumentException("the cache not exists: " + cacheName);

    Redis.mainCache = cache;
  }

  public static Cache use() {
    return mainCache;
  }

  public static Cache use(String cacheName) {
    return cacheMap.get(cacheName);
  }

  /**
   * 使用 lambda 开放 Jedis API，建议优先使用本方法
   * <pre>
   * 例子 1：
   *   Long ret = Redis.call(j -> j.incrBy("key", 1));
   *   
   * 例子 2：
   *   Long ret = Redis.call(jedis -> {
   *       return jedis.incrBy("key", 1);
   *   });
   * </pre>
   */
  public static <R> R call(Function<Jedis, R> jedis) {
    return use().call(jedis);
  }

  /**
   * 使用 lambda 开放 Jedis API，建议优先使用本方法
   * <pre>
   * 例子：
   *   Long ret = Redis.call("cacheName", j -> j.incrBy("key", 1));
   * </pre>
   */
  public static <R> R call(String cacheName, Function<Jedis, R> jedis) {
    return use(cacheName).call(jedis);
  }

  public static <T> T callback(ICallback<T> callback) {
    return callback(use(), callback);
  }

  public static <T> T callback(String cacheName, ICallback<T> callback) {
    return callback(use(cacheName), callback);
  }

  private static <T> T callback(Cache cache, ICallback<T> callback) {
    Jedis jedis = cache.getThreadLocalJedis();
    boolean notThreadLocalJedis = (jedis == null);
    if (notThreadLocalJedis) {
      jedis = cache.jedisPool.getResource();
      cache.setThreadLocalJedis(jedis);
    }
    try {
      return callback.call(cache);
    } finally {
      if (notThreadLocalJedis) {
        cache.removeThreadLocalJedis();
        jedis.close();
      }
    }
  }

  public static <R> R getBean(String key, Class<R> type) {
    Function<Jedis, R> function = (jedis) -> {
      String str = jedis.get(key);
      if (str != null) {
        return JsonUtils.parse(str, type);
      } else {
        return null;
      }
    };

    return use().call(function);
  }

  public static <R> String setBean(String key, long seconds, Object input) {
    return use().call(j -> j.setex(key, seconds, JsonUtils.toJson(input)));
  }

  public static <R> String setBean(String key, Object input) {
    return use().call(j -> j.set(key, JsonUtils.toJson(input)));
  }

  public static <R> String setStr(String key, String input) {
    return use().call(j -> j.set(key, input));
  }

  public static <R> String setStr(String key, long seconds, String input) {
    return use().call(j -> j.setex(key, seconds, input));
  }

  public static String getStr(String key) {
    Function<Jedis, String> function = (jedis) -> {
      return jedis.get(key);
    };
    return use().call(function);
  }

  public static String setInt(String key, int value) {
    return use().call(j -> j.set(key, Integer.toString(value)));
  }

  public static String setInt(String key, long seconds, int value) {
    return use().call(j -> j.setex(key, seconds, Integer.toString(value)));
  }

  public static Integer getInt(String key) {
    Function<Jedis, String> function = (jedis) -> {
      return jedis.get(key);
    };
    String value = use().call(function);
    return Integer.parseInt(value);
  }

  public static String setLong(String key, long value) {
    return use().call(j -> j.set(key, Long.toString(value)));
  }

  public static String setLong(String key, long seconds, long value) {
    return use().call(j -> j.setex(key, seconds, Long.toString(value)));
  }

  public static Long getLong(String key) {
    Function<Jedis, String> function = (jedis) -> {
      return jedis.get(key);
    };
    String value = use().call(function);
    return Long.parseLong(value);
  }
}
