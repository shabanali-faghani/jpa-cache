package ir.tata.jpacache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.tata.jpacache.db.CacheEntity;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

@Slf4j
@Builder(builderMethodName = "cacheBuilder")
public class JpaCache<T> implements Cache {

    private final String cacheName;
    private final Long ttlInSeconds;
    private final Class<T> clazz;
    private final ICacheService cacheService;
    private final boolean failFast;  // tolerate or throw?

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JpaCache(String cacheName, Long ttlInSeconds, Class<T> clazz, ICacheService cacheService, boolean failFast) {
        Assert.hasText(cacheName, "'cacheName' must be not null and must contain at least one non-whitespace character");
        Assert.notNull(ttlInSeconds, "'ttlInSeconds' must be not null");
        Assert.notNull(clazz, "'clazz' must be not null");
        Assert.notNull(cacheService, "'jpaCacheService' must be not null");

        this.cacheName = cacheName;
        this.ttlInSeconds = ttlInSeconds;
        this.clazz = clazz;
        this.cacheService = cacheService;
        this.failFast = failFast;
    }

    @Override
    public String getName() {
        return this.cacheName;
    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    public ValueWrapper get(Object key) {
        Assert.isTrue(key instanceof String, "'key' must be an instance of 'java.lang.String'");
        log.debug("Going to get a record with key '{}' from cache database", key);

        CacheEntity cacheEntity;
        try {
            cacheEntity = cacheService.get(cacheName, (String) key, ttlInSeconds);
        } catch (Exception e) {
            // Warn and return. Cache problem must not hinder execution of the main service.
            log.warn("Could not get record with key '{}' from cache database", key, e);
            return null;
        }
        if (cacheEntity == null) {
            log.info("There is no valid entry for key '{}' in cache database. Target record is either not exist or expired!", key);
            return null;
        }

        T value;
        try {
            value = objectMapper.readValue(cacheEntity.getResponse(), this.clazz);
        } catch (JsonProcessingException e) {
            log.warn("Deserialization failed for record with key '{}', cache will be ignored", key, e);
            return null;
        }

        FollowUp followUp = null;
        try {
            followUp = (FollowUp) value;
            followUp.setUuid(cacheEntity.getUuid());
        } catch (Exception e) {
            log.warn("Could not set uuid into cache response with key '{}'." +
                    " Exception will be suppressed but follow-up might be impossible", key, e);
        }

        // TODO async it
        // FIXME remove it! Counting cache hits must be done by using log.
        cacheService.increaseHits(cacheEntity);

        log.info("An entry associated with key '{}' found with valid ttl and retrieved successfully from cache database", key);
        return new SimpleValueWrapper(followUp);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {
        Assert.isTrue(key instanceof String, "'key' must be an instance of 'java.lang.String'");
        log.debug("Going to save a record with key '{}' in cache database", key);

        String uuid = UUID.randomUUID().toString();
        try {
            FollowUp followUp = (FollowUp) value;
            followUp.setUuid(uuid);
        } catch (Exception e) {
            // Logically this exception can be ignored even if failFast is true. But I guess it's better to throw it.
            if (failFast) {
                log.error("Could not set uuid into record with key '{}'", key, e);
                throw new CacheException("Could not cast result to FollowUp class", e);
            }
            log.warn("Could not set uuid into record with key '{}'." +
                    " The record will be saved for cache but follow-up for history will be impossible", key, e);
        }

        try {
            cacheService.save(cacheName, (String) key, objectMapper.writeValueAsString(value), uuid);
        } catch (Exception e) { // serialization or database problem
            if (failFast) {
                log.error("Could not save record with key '{}' in cache database", key, e);
                throw new CacheException("Could not save result in cache database", e);
            }
            log.warn("Could not save record with key '{}' in cache database", key, e);
            return;
        }

        log.info("A record with key '{}' saved successfully in cache database", key);
    }

    @Override
    public void evict(Object key) {
    }

    @Override
    public void clear() {
    }
}
