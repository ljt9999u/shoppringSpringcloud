package org.example.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 热点数据缓存防护组件，统一解决三类高并发缓存问题：
 * <p>
 * 1. 缓存穿透：DB 查不到时写入【空值哨兵】并给短 TTL，拦截对不存在 key 的恶意/反复查询；<br>
 * 2. 缓存击穿：热点 key 失效瞬间用【setnx 互斥锁】只放一个请求回源 DB，其余自旋读缓存；<br>
 * 3. 缓存雪崩：正常数据 TTL = 基准值 + 随机抖动，避免大量 key 同时集中失效。
 * <p>
 * 另：Redis 本身故障时全部降级为直接查 DB，不拖垮主业务。
 */
@Slf4j
@Component
public class RedisCacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /** 空值哨兵：缓存中出现该值代表"DB确认不存在"，用于防穿透 */
    private static final String NULL_HOLDER = "__NULL_CACHE__";
    /** 空值哨兵 TTL（秒）：较短，让新数据尽快可见 */
    private static final long NULL_TTL_SECONDS = 60;
    /** 互斥锁过期时间（秒）：持有者宕机也能自动释放，防死锁 */
    private static final long LOCK_EXPIRE_SECONDS = 10;
    /** 抢不到锁时自旋次数与每次等待毫秒 */
    private static final int RETRY_TIMES = 40;
    private static final long RETRY_SLEEP_MS = 50;

    public RedisCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        // 缓存对象可能后续新增字段，反序列化时忽略未知属性
        this.objectMapper.configure(
                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 带三重防护的查询。
     *
     * @param key           缓存 key
     * @param typeRef       返回类型（支持单对象和 List 泛型）
     * @param baseTtl       正常数据基准 TTL
     * @param jitterSeconds 正常数据 TTL 随机抖动上界（秒）
     * @param loader        缓存未命中时的回源逻辑（查 DB），返回 null 表示数据不存在
     */
    public <T> T queryWithProtect(String key, TypeReference<T> typeRef,
                                  Duration baseTtl, long jitterSeconds, Supplier<T> loader) {
        // 1. 先读缓存
        String cached = safeGet(key);

        // 2. 命中空值哨兵：这是被确认不存在的数据，直接返回 null，挡住穿透流量
        if (NULL_HOLDER.equals(cached)) {
            return null;
        }

        // 3. 命中正常数据：反序列化返回
        if (cached != null) {
            return deserialize(cached, typeRef);
        }

        // 4. 未命中：尝试抢互斥锁（SET key value NX EX 10），保证只有一个请求回源
        String lockKey = "lock:" + key;
        Boolean locked = safeSetNx(lockKey);
        try {
            if (Boolean.TRUE.equals(locked)) {
                // 双重检查：拿到锁后再看一次缓存，避免排队期间前一个持锁者已写入
                String doubleCheck = safeGet(key);
                if (NULL_HOLDER.equals(doubleCheck)) {
                    return null;
                }
                if (doubleCheck != null) {
                    return deserialize(doubleCheck, typeRef);
                }

                // 唯一回源：查 DB
                T data = loader.get();
                if (data == null) {
                    // DB 也没有 → 写空值哨兵（短 TTL），防穿透
                    safeSet(key, NULL_HOLDER, Duration.ofSeconds(NULL_TTL_SECONDS));
                } else {
                    // 正常数据 → 写随机抖动 TTL，防雪崩
                    long ttl = baseTtl.getSeconds()
                            + (jitterSeconds > 0 ? ThreadLocalRandom.current().nextLong(jitterSeconds) : 0);
                    safeSet(key, objectMapper.writeValueAsString(data), Duration.ofSeconds(ttl));
                }
                return data;
            } else {
                // 没抢到锁：说明已有请求在回源，短暂自旋等待其写缓存
                for (int i = 0; i < RETRY_TIMES; i++) {
                    sleep(RETRY_SLEEP_MS);
                    String retry = safeGet(key);
                    if (NULL_HOLDER.equals(retry)) {
                        return null;
                    }
                    if (retry != null) {
                        return deserialize(retry, typeRef);
                    }
                }
                // 等待超时兜底：不再死等，直接查一次 DB 保证可用（极端情况下可能少量并发回源）
                log.warn("等待缓存锁超时，降级直查DB，key={}", key);
                return loader.get();
            }
        } catch (Exception e) {
            // 序列化等异常不应影响主流程，兜底直查 DB
            log.error("缓存查询异常，降级直查DB，key={}", key, e);
            return loader.get();
        } finally {
            // 仅持锁者释放锁
            if (Boolean.TRUE.equals(locked)) {
                safeDelete(lockKey);
            }
        }
    }

    /**
     * 删除缓存（写操作后调用，保证一致性）
     */
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("删除缓存失败，key={}", key, e);
        }
    }

    // ========== 对 Redis 的安全封装：Redis 挂了一律降级，不抛异常到业务层 ==========

    private String safeGet(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis读取失败，降级直查DB，key={}", key);
            return null;
        }
    }

    private Boolean safeSetNx(String lockKey) {
        try {
            return redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            // 抢锁失败/Redis异常：返回 false 让调用方走自旋/兜底，不要直接抛错
            log.warn("Redis抢锁失败，降级处理，lockKey={}", lockKey);
            return false;
        }
    }

    private void safeSet(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("Redis写入失败，key={}", key, e);
        }
    }

    private void safeDelete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis删除失败，key={}", key, e);
        }
    }

    private <T> T deserialize(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.error("缓存反序列化失败，将视为未命中，json={}", json, e);
            return null;
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
