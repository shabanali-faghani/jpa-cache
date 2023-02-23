package ir.tata.jpacache;

import ir.tata.jpacache.db.CacheEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface ICacheService {
    void save(String cacheName, String key, String response, String uuid);

    void save(String profile, String cacheName, String dataModelVersion, String key, String response, String uuid, LocalDateTime timestamp, Long hits);

    void increaseHits(Long id);

    void increaseHits(CacheEntity entity);

    CacheEntity get(String cacheName, String key, Long ttlInSeconds);

    String getResponse(String cacheName, String key, Long ttlInSeconds);

    List<CacheEntity> get(String cacheName, String key);

    CacheEntity getLatest(String cacheName, String key);

    String getLatestResponse(String cacheName, String key);

    String followUp(String uuid);
}
