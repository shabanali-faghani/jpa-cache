package ir.tata.jpacache.history;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;

@Service
@CacheConfig(cacheManager = "jpaHistoryManager")
public class Service4HistoryTest {

    @HistoryPut(historyManager = "jpaHistoryManager", historyNames = "history", key = "#key")
    //@CachePut(cacheManager = "jpaHistoryManager", cacheNames = "history", key = "#key")
    //@HistoryPut(historyNames = "history", key = "#key")
    public String history(String key) {
        return "response" + key;
    }
}
