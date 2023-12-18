package com.litongjava.jfinal.plugin.ehcache;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CacheName can configure CacheInterceptor's cache name or EvictInterceptor's evict cache name.
 * <p>
 * The order of CacheInterceptor searching for CacheName annotation:<br>
 * 1: Action method of current invocation<br>
 * 2: Controller of the current invocation<br>
 * CacheInterceptor will use the actionKey as the cache name If the CacheName annotation not found. 
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface EcacheCacheName {
  String value();
}
