package com.ssd.mvd.gpstracker.database;//package com.ssd.mvd.gpstracker.database;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration
//public class RedisConfig {
//    @Lazy
//    @Bean( name = "lettuce" )
//    public LettuceConnectionFactory lettuceConnectionFactory () {
//        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
//        configuration.setHostName( "10.254.1.227" );
//        configuration.setUsername( "default" );
//        configuration.setPassword( "8tRk62" );
//        configuration.setPort( 6367 );
//        return new LettuceConnectionFactory( configuration );
//    }
//
//    @Lazy
//    @Bean( name = "template" )
//    public RedisTemplate< String, String > redisTemplate ( LettuceConnectionFactory lettuceConnectionFactory ) {
//        RedisTemplate< String, String > redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory( lettuceConnectionFactory );
//        redisTemplate.setValueSerializer( new StringRedisSerializer() );
//        redisTemplate.setKeySerializer( new StringRedisSerializer() );
//        redisTemplate.afterPropertiesSet();
//        return redisTemplate;
//    }
//}
