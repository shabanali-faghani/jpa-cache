package ir.tata.jpacache;

import com.google.common.annotations.VisibleForTesting;
import ir.tata.jpacache.db.CacheEntity;
import ir.tata.jpacache.db.CacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class JpaCacheService implements ICacheService {

    private final CacheRepository repository;
    private final String profile;
    private final String dataModelVersion;

    public JpaCacheService(CacheRepository repository,
                           @Value("${spring.profiles.active:default}") String profile,
                           @Value("${spring.datasource.cache.dataModelVersion}") String dataModelVersion) {
        this.repository = repository;
        this.profile = profile;
        this.dataModelVersion = dataModelVersion;
    }

    @Override
    public void save(String cacheName, String key, String response, String uuid) {
        this.save(profile, cacheName, dataModelVersion, key, response, uuid, LocalDateTime.now(), 1L);
    }

    @VisibleForTesting
    @Override
    public void save(String profile, String cacheName, String dataModelVersion, String key, String response, String uuid, LocalDateTime timestamp, Long hits) {
        CacheEntity cacheEntity = new CacheEntity();
        cacheEntity.setProfile(profile);
        cacheEntity.setCacheName(cacheName);
        cacheEntity.setDataModelVersion(dataModelVersion);
        cacheEntity.setKey(key);
        cacheEntity.setResponse(response);
        cacheEntity.setUuid(uuid);
        cacheEntity.setTimestamp(timestamp);
        cacheEntity.setHits(hits);
        repository.save(cacheEntity);
    }

    @Override
    public void increaseHits(Long id) {
        this.increaseHits(repository.getById(id));
    }

    @Override
    public void increaseHits(CacheEntity entity) {
        entity.setHits(entity.getHits() + 1);
        repository.save(entity);
    }

    @Override
    public CacheEntity get(String cacheName, String key, Long ttlInSeconds) {
        return repository.get(key, profile, cacheName, dataModelVersion, ttlInSeconds, LocalDateTime.now());
    }

    @Override
    public String getResponse(String cacheName, String key, Long ttlInSeconds) {
        CacheEntity cacheEntity = ttlInSeconds != null ? this.get(cacheName, key, ttlInSeconds) : this.getLatest(cacheName, key);
        return cacheEntity != null ? cacheEntity.getResponse() : null;
    }

    @Override
    public List<CacheEntity> get(String cacheName, String key) {
        return repository.getByKeyAndProfileAndCacheNameAndDataModelVersion(key, profile, cacheName, dataModelVersion);
    }

    @Override
    public CacheEntity getLatest(String cacheName, String key) {
        return repository.getLatest(key, profile, cacheName, dataModelVersion);
    }

    @Override
    public String getLatestResponse(String cacheName, String key) {
        CacheEntity cacheEntity = this.getLatest(cacheName, key);
        return cacheEntity != null ? cacheEntity.getResponse() : null;
    }

    @Override
    public String followUp(String uuid) {
        List<CacheEntity> entities = repository.getByUuid(uuid);
        if (entities.isEmpty()) {
            log.debug("There is no associated record with uuid '{}'", uuid);
            return null;
        } else if (entities.size() > 1) {
            // unreachable code! due to the nature of uuid.
            log.error("Expected one but found multiple records with uuid '{}'", uuid);
            throw new IllegalStateException("multiple records found with uuid " + uuid);
        }
        return entities.get(0).getResponse();
    }
}
