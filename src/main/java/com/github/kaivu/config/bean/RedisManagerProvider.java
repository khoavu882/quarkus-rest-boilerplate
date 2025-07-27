package com.github.kaivu.config.bean;

import com.github.kaivu.adapter.out.client.RedisHelper;
import com.github.kaivu.config.redis.RedisManager;
import com.github.kaivu.config.redis.RedisProfile;
import com.github.kaivu.config.redis.RedisProfileType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis manager provider for different profiles
 * Produces profile-specific RedisManager instances that consumers can inject with @RedisProfile
 */
@Slf4j
@ApplicationScoped
public class RedisManagerProvider {

    /**
     * Default Redis Manager (uses DEFAULT profile RedisHelper)
     */
    @Produces
    @Singleton
    @RedisProfile(RedisProfileType.DEFAULT)
    public RedisManager defaultRedisManager(@RedisProfile(RedisProfileType.DEFAULT) RedisHelper redisHelper) {
        log.info("Creating DEFAULT Redis manager");
        return new RedisManager(redisHelper);
    }

    /**
     * Demo Redis Manager (uses DEMO profile RedisHelper)
     */
    @Produces
    @Singleton
    @RedisProfile(RedisProfileType.DEMO)
    public RedisManager demoRedisManager(@RedisProfile(RedisProfileType.DEMO) RedisHelper redisHelper) {
        log.info("Creating DEMO Redis manager");
        return new RedisManager(redisHelper);
    }
}
