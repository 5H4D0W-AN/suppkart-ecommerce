package com.suppkart.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.redis.timeout:2000ms}")
    private String redisTimeoutStr;

    /**
     * Redis Connection Factory Configuration
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setDatabase(redisDatabase);
        
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig);
        factory.setValidateConnection(true);
        
        return factory;
    }

    /**
     * Redis Template Configuration for general use
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();

        return template;
    }

    /**
     * Redis Template for custom String operations
     */
    @Bean
    public RedisTemplate<String, String> customStringRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for both keys and values
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());

        template.setDefaultSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();

        return template;
    }

    /**
     * Redis Template specifically for Cart caching
     */
    @Bean
    public RedisTemplate<String, Object> cartRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for cart objects
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();

        return template;
    }

    /**
     * Constants for Redis key prefixes
     */
    public static class RedisKeys {
        public static final String CART_PREFIX = "cart:";
        public static final String SESSION_PREFIX = "session:";
        public static final String USER_SESSION_PREFIX = "user_session:";
        public static final String GUEST_CART_PREFIX = "guest_cart:";
        public static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
        
        // Cart specific keys
        public static final String CART_ITEMS_PREFIX = "cart_items:";
        public static final String CART_EXPIRY_PREFIX = "cart_expiry:";
        
        // Session timeouts (in seconds)
        public static final long CART_EXPIRY_TIME = Duration.ofDays(7).getSeconds(); // 7 days
        public static final long SESSION_EXPIRY_TIME = Duration.ofHours(24).getSeconds(); // 24 hours
        public static final long GUEST_CART_EXPIRY_TIME = Duration.ofDays(30).getSeconds(); // 30 days
        
        /**
         * Generate cart key for user
         */
        public static String getUserCartKey(Long userId) {
            return CART_PREFIX + "user:" + userId;
        }
        
        /**
         * Generate cart key for guest session
         */
        public static String getGuestCartKey(String sessionId) {
            return GUEST_CART_PREFIX + sessionId;
        }
        
        /**
         * Generate session key for user
         */
        public static String getUserSessionKey(Long userId) {
            return USER_SESSION_PREFIX + userId;
        }
        
        /**
         * Generate refresh token key
         */
        public static String getRefreshTokenKey(String token) {
            return REFRESH_TOKEN_PREFIX + token;
        }
    }
}
