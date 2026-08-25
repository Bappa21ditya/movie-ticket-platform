package com.movieTicket.InventoryService.reddis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class RedisSeatLock implements SeatLock{

    private static final Duration LOCK_TTL =
            Duration.ofSeconds(5);

    private final RedisTemplate<String, String> redisTemplate;

    public RedisSeatLock(
            RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryLock(
            Long showSeatId,
            String token) {

        String key = buildKey(showSeatId);

        Boolean acquired =
                redisTemplate
                        .opsForValue()
                        .setIfAbsent(
                                key,
                                token,
                                LOCK_TTL
                        );

        return Boolean.TRUE.equals(acquired);
    }

    @Override
    public void unlock(
            Long showSeatId,
            String token) {

        String key = buildKey(showSeatId);

        redisTemplate.execute(
                new DefaultRedisScript<>(
                        UNLOCK_SCRIPT,
                        Long.class
                ),
                List.of(key),
                token
        );
    }

    private String buildKey(Long showSeatId) {
        return "lock:showSeat:" + showSeatId;
    }

    private static final String UNLOCK_SCRIPT = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """;

}
