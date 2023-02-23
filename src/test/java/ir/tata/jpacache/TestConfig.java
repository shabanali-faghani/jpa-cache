package ir.tata.jpacache;

import ir.tata.jpacache.dto.ComplexType;
import ir.tata.jpacache.history.JpaHistory;
import ir.tata.jpacache.history.JpaHistoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Configuration
public class TestConfig {
    @Autowired
    private ICacheService cacheService;

    @Primary
    @Bean("jpaCacheManager")
    public JpaCacheManager cacheManager() {
        List<JpaCache.JpaCacheBuilder> cacheBuilders = new ArrayList<>();

        JpaCache.JpaCacheBuilder<String> test = JpaCache.<String>cacheBuilder().cacheName("test").ttlInSeconds(DAYS.toSeconds(1)).clazz(String.class).cacheService(cacheService);
        JpaCache.JpaCacheBuilder<ComplexType> complexType = JpaCache.<ComplexType>cacheBuilder().cacheName("complexType").ttlInSeconds(DAYS.toSeconds(1)).clazz(ComplexType.class).cacheService(cacheService);
        JpaCache.JpaCacheBuilder<ComplexType> cache1 = JpaCache.<ComplexType>cacheBuilder().cacheName("cache-1-ttl-1sec").ttlInSeconds(SECONDS.toSeconds(1)).clazz(ComplexType.class).cacheService(cacheService);
        JpaCache.JpaCacheBuilder<ComplexType> cache2 = JpaCache.<ComplexType>cacheBuilder().cacheName("cache-2-ttl-2sec").ttlInSeconds(SECONDS.toSeconds(2)).clazz(ComplexType.class).cacheService(cacheService);

        JpaCache.JpaCacheBuilder<ComplexType> complexTypeFailFast = JpaCache.<ComplexType>cacheBuilder().cacheName("complex-type-fail-fast").ttlInSeconds(DAYS.toSeconds(2)).clazz(ComplexType.class).cacheService(cacheService).failFast(true);
        JpaCache.JpaCacheBuilder<ComplexType> castProblemWithFailFast = JpaCache.<ComplexType>cacheBuilder().cacheName("cast-problem-with-fail-fast").ttlInSeconds(DAYS.toSeconds(2)).clazz(ComplexType.class).cacheService(cacheService).failFast(true);

        cacheBuilders.add(test);
        cacheBuilders.add(complexType);
        cacheBuilders.add(cache1);
        cacheBuilders.add(cache2);
        cacheBuilders.add(complexTypeFailFast);
        cacheBuilders.add(castProblemWithFailFast);
        return new JpaCacheManager(cacheBuilders);
    }

    @Bean("jpaHistoryManager")
    public JpaHistoryManager historyManager() {
        List<JpaHistory.JpaHistoryBuilder> historyBuilders = new ArrayList<>();

        JpaHistory.JpaHistoryBuilder<String> history = JpaHistory.<String>historyBuilder()
                .historyName("history")
                .clazz(String.class)
                .historyService(cacheService);

        historyBuilders.add(history);
        return new JpaHistoryManager(historyBuilders);
    }
}
