package ir.tata.jpacache.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CacheRepository extends JpaRepository<CacheEntity, Long> {

    @Query(nativeQuery = true, value = "select *" +
            " from GENERIC_TABLE_NAME" +
            " where ID = (select max(ID) from GENERIC_TABLE_NAME where KEY = ?)" +
            "   and PROFILE = ?" +
            "   and CACHENAME = ?" +
            "   and DATAMODELVERSION = ?" +
            "   and TIMESTAMP + interval '1' second * ? >= ?")
    CacheEntity get(String key, String profile, String cacheName, String dataModelVersion, Long ttl, LocalDateTime now);

    List<CacheEntity> getByKeyAndProfileAndCacheNameAndDataModelVersion(String key, String profile, String cacheName, String dataModelVersion);

    @Query(nativeQuery = true, value = "select *" +
            " from GENERIC_TABLE_NAME" +
            " where ID = (select max(ID) from GENERIC_TABLE_NAME where KEY = ?)" +
            "   and PROFILE = ?" +
            "   and CACHENAME = ?" +
            "   and DATAMODELVERSION = ?")
    CacheEntity getLatest(String key, String profile, String cacheName, String dataModelVersion);

    List<CacheEntity> getByUuid(String uuid);
}
