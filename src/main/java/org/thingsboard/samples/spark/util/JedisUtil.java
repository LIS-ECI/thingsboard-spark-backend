/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thingsboard.samples.spark.util;

/**
 *
 * @author cristian
 */

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ResourceBundle;

/**
 * Created by vino on 16/8/20.
 */
public class JedisUtil {
    private  static JedisPool pool;

    static {
        ResourceBundle bundle = ResourceBundle.getBundle("jedis");

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(Integer.valueOf(bundle
                .getString("redis.pool.maxActive")));
        config.setMaxIdle(Integer.valueOf(bundle
                .getString("redis.pool.maxIdle")));
        config.setMaxWaitMillis(Long.valueOf(bundle.getString("redis.pool.maxWait")));
        config.setTestOnBorrow(Boolean.valueOf(bundle
                .getString("redis.pool.testOnBorrow")));
        config.setTestOnReturn(Boolean.valueOf(bundle
                .getString("redis.pool.testOnReturn")));
        pool = new JedisPool(config, bundle.getString("redis.ip"),
                Integer.valueOf(bundle.getString("redis.port")));
    }

    public static JedisPool getPool(){
        return pool;
    }
    public static void closePool(){
        if(!pool.isClosed()) pool.destroy();
    }
}