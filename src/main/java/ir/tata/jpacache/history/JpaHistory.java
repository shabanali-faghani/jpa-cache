package ir.tata.jpacache.history;

import ir.tata.jpacache.ICacheService;
import ir.tata.jpacache.JpaCache;
import lombok.Builder;

/**
 * TODO Refactor and change namings. There should be an independent entity and JpaCache and JpaHistory must extend it.
 *
 * @param <T>
 */
public class JpaHistory<T> extends JpaCache<T> {

    @Builder(builderMethodName = "historyBuilder")
    public JpaHistory(String historyName, Long ttlInSeconds, Class clazz, ICacheService historyService, boolean failFast) {
        super(historyName, ttlInSeconds != null ? ttlInSeconds : -1L, clazz, historyService, failFast);
    }

    @Override
    public ValueWrapper get(Object key) {
        throw new UnsupportedOperationException("The get method doesn't make any sense for history!");
    }

    @Override
    public void evict(Object key) {
        throw new UnsupportedOperationException("Eviction is not supported for history!");
    }

}
