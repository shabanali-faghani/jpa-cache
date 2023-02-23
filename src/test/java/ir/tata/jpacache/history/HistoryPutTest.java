package ir.tata.jpacache.history;

import ir.tata.jpacache.JpaCacheService;
import ir.tata.jpacache.db.CacheEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EnableCaching
@Rollback
@Transactional("cacheTransactionManager")
public class HistoryPutTest {
    @Autowired
    private Service4HistoryTest service4HistoryTest;
    @Autowired
    private JpaCacheService jpaCacheService;

    @Test
    void simple() {
        assertTrue(jpaCacheService.get("history", "1").isEmpty());

        service4HistoryTest.history("1");
        List<CacheEntity> historyEntities = jpaCacheService.get("history", "1");
        assertNotNull(historyEntities);
        assertFalse(jpaCacheService.get("history", "1").isEmpty());
        assertEquals(1, historyEntities.size());
        assertEquals("1", historyEntities.get(0).getKey());
        assertEquals("\"response1\"", historyEntities.get(0).getResponse());

        // must save again when is called with the same key
        service4HistoryTest.history("1");
        historyEntities = jpaCacheService.get("history", "1");
        assertEquals(2, historyEntities.size());
    }

}
