package ir.tata.jpacache.history;

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

public class JpaHistoryManager extends AbstractCacheManager {

    private final Collection<JpaHistory.JpaHistoryBuilder> initialHistories;

    public JpaHistoryManager(Collection<JpaHistory.JpaHistoryBuilder> initialHistories) {
        Assert.notEmpty(initialHistories, "At least one history builder must be specified.");
        this.initialHistories = new ArrayList<>(initialHistories);
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        final Collection<Cache> histories = new LinkedHashSet<>(initialHistories.size());
        for (final JpaHistory.JpaHistoryBuilder historyBuilder : initialHistories) {
            final JpaHistory history = historyBuilder.build();
            histories.add(history);
        }
        return histories;
    }
}
