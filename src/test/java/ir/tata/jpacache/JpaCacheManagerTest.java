package ir.tata.jpacache;

import ir.tata.jpacache.db.CacheEntity;
import ir.tata.jpacache.dto.ComplexType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableCaching
@Rollback
@Transactional("cacheTransactionManager")
public class JpaCacheManagerTest {
    @Autowired
    private Service4Test service4Test;
    @Autowired
    private JpaCacheService jpaCacheService;

    @Test
    void simple() {
        assertTrue(jpaCacheService.get("test", "1").isEmpty());

        service4Test.getResponse("1");
        List<CacheEntity> cacheEntities = jpaCacheService.get("test", "1");
        assertNotNull(cacheEntities);
        assertFalse(jpaCacheService.get("test", "1").isEmpty());
        assertEquals(1, cacheEntities.size());
        assertEquals("1", cacheEntities.get(0).getKey());
        assertEquals("\"response1\"", cacheEntities.get(0).getResponse());

        // must not save twice when previous cache is valid with the given ttl
        service4Test.getResponse("1");
        cacheEntities = jpaCacheService.get("test", "1");
        assertEquals(1, cacheEntities.size());
        assertEquals(2, cacheEntities.get(0).getHits());

    }

    @Test
    void unless() {
        assertTrue(jpaCacheService.get("test", "2").isEmpty());
        service4Test.getResponse("2");
        assertTrue(jpaCacheService.get("test", "2").isEmpty());
    }

    @Test
    void hits() {
        assertTrue(jpaCacheService.get("test", "1").isEmpty());

        service4Test.getResponse("1");

        List<CacheEntity> cacheEntities = jpaCacheService.get("test", "1");
        assertEquals(1, cacheEntities.size());
        assertEquals(1, cacheEntities.get(0).getHits());

        service4Test.getResponse("1");
        assertEquals(2, cacheEntities.get(0).getHits());

        service4Test.getResponse("1");
        assertEquals(3, cacheEntities.get(0).getHits());
    }

    @Test
    void implicitKey() {
        assertTrue(jpaCacheService.get("test", "1").isEmpty());
        service4Test.implicitKey("1");
        assertEquals(1, jpaCacheService.get("test", "1").size());
    }

    @Test
    void complexType() {
        assertTrue(jpaCacheService.get("complexType", "1").isEmpty());
        service4Test.complexType("1");
        assertEquals(1, jpaCacheService.get("complexType", "1").size());
        service4Test.complexType("1");
        assertEquals(1, jpaCacheService.get("complexType", "1").size());
    }

    @Test
    void uniqueUuid() {
        assertTrue(jpaCacheService.get("complexType", "1").isEmpty());
        assertTrue(jpaCacheService.get("complexType", "2").isEmpty());

        ComplexType complexType1 = service4Test.complexType("1");
        ComplexType complexType2 = service4Test.complexType("2");

        assertFalse(complexType1.getUuid().equals(complexType2.getUuid()));
    }

    @Test
    void deserializeWithUuid() {
        assertTrue(jpaCacheService.get("complexType", "1").isEmpty());

        ComplexType fresh = service4Test.complexType("1");
        ComplexType fromCache = service4Test.complexType("1");

        assertTrue(fresh.getUuid().equals(fromCache.getUuid()));
    }

    @Test
    void multiCacheFullTest() throws InterruptedException {
        assertTrue(jpaCacheService.get("cache-1-ttl-1sec", "1-s").isEmpty());
        assertTrue(jpaCacheService.get("cache-2-ttl-2sec", "2-s").isEmpty());

        assertThrows(IllegalStateException.class, () -> service4Test.multiCache(new ComplexType(1, "s"), ComplexType.RetType.EXCEPTION));

        assertTrue(jpaCacheService.get("cache-1-ttl-1sec", "1-s").isEmpty());
        assertTrue(jpaCacheService.get("cache-2-ttl-2sec", "2-s").isEmpty());

        assertNull(service4Test.multiCache(new ComplexType(1, "s"), ComplexType.RetType.NULL));

        assertTrue(jpaCacheService.get("cache-1-ttl-1sec", "1-s").isEmpty());
        assertTrue(jpaCacheService.get("cache-2-ttl-2sec", "2-s").isEmpty());

        service4Test.multiCache(new ComplexType(1, "s"), ComplexType.RetType.NORMAL);

        assertEquals(1, jpaCacheService.get("cache-1-ttl-1sec", "1-s").size());
        assertTrue(jpaCacheService.get("cache-2-ttl-2sec", "2-s").isEmpty());

        service4Test.multiCache(new ComplexType(1, "s"), ComplexType.RetType.NORMAL);

        assertEquals(1, jpaCacheService.get("cache-1-ttl-1sec", "1-s").size());
        assertTrue(jpaCacheService.get("cache-2-ttl-2sec", "2-s").isEmpty());

        TimeUnit.SECONDS.sleep(1L); // expires ttl

        ComplexType ct = service4Test.multiCache(new ComplexType(1, "s"), ComplexType.RetType.NORMAL);

        assertEquals(1, ct.getI());
        assertEquals("s", ct.getS());

        assertEquals(2, jpaCacheService.get("cache-1-ttl-1sec", "1-s").size());
        assertTrue(jpaCacheService.get("cache-2-ttl-2sec", "2-s").isEmpty());

        ct = service4Test.multiCache(new ComplexType(1, "t"), ComplexType.RetType.NORMAL);

        assertEquals(1, ct.getI());
        assertEquals("t", ct.getS());

        assertEquals(1, jpaCacheService.get("cache-1-ttl-1sec", "1-t").size());
        assertEquals(2, jpaCacheService.get("cache-1-ttl-1sec", "1-s").size());
        assertTrue(jpaCacheService.get("cache-2-ttl-2sec", "2-s").isEmpty());

        service4Test.multiCache(new ComplexType(2, "s"), ComplexType.RetType.NORMAL);

        assertEquals(1, jpaCacheService.get("cache-1-ttl-1sec", "1-t").size());
        assertEquals(2, jpaCacheService.get("cache-1-ttl-1sec", "1-s").size());
        assertEquals(1, jpaCacheService.get("cache-2-ttl-2sec", "2-s").size());

        ct = service4Test.multiCache(new ComplexType(2, "s"), ComplexType.RetType.NULL);

        assertEquals(2, ct.getI());
        assertEquals("s", ct.getS());

        assertEquals(1, jpaCacheService.get("cache-1-ttl-1sec", "1-t").size());
        assertEquals(2, jpaCacheService.get("cache-1-ttl-1sec", "1-s").size());
        assertEquals(1, jpaCacheService.get("cache-2-ttl-2sec", "2-s").size());

        TimeUnit.SECONDS.sleep(1L); // won't expire ttl

        service4Test.multiCache(new ComplexType(2, "s"), ComplexType.RetType.NORMAL);

        assertEquals(1, jpaCacheService.get("cache-1-ttl-1sec", "1-t").size());
        assertEquals(2, jpaCacheService.get("cache-1-ttl-1sec", "1-s").size());
        assertEquals(1, jpaCacheService.get("cache-2-ttl-2sec", "2-s").size());

        TimeUnit.SECONDS.sleep(1L); // but this expires ttl

        assertNull(service4Test.multiCache(new ComplexType(2, "s"), ComplexType.RetType.NULL));

        assertEquals(1, jpaCacheService.get("cache-1-ttl-1sec", "1-t").size());
        assertEquals(2, jpaCacheService.get("cache-1-ttl-1sec", "1-s").size());
        assertEquals(1, jpaCacheService.get("cache-2-ttl-2sec", "2-s").size());

        service4Test.multiCache(new ComplexType(2, "s"), ComplexType.RetType.NORMAL);

        assertEquals(1, jpaCacheService.get("cache-1-ttl-1sec", "1-t").size());
        assertEquals(2, jpaCacheService.get("cache-1-ttl-1sec", "1-s").size());
        assertEquals(2, jpaCacheService.get("cache-2-ttl-2sec", "2-s").size());
    }

    @Test
    void castProblem() {
        Throwable exception = assertThrows(CacheException.class, () -> service4Test.castProblem("1"));
        assertEquals("Could not cast result to FollowUp class", exception.getMessage());
        assertEquals(ClassCastException.class, exception.getCause().getClass());
    }
}
