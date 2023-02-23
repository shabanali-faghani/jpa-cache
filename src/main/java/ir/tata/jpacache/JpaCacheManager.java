package ir.tata.jpacache;

import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

public class JpaCacheManager extends AbstractCacheManager {

    private final Collection<JpaCache.JpaCacheBuilder> initialCaches;

    public JpaCacheManager(Collection<JpaCache.JpaCacheBuilder> initialCaches) {
        Assert.notEmpty(initialCaches, "At least one cache builder must be specified.");
        this.initialCaches = new ArrayList<>(initialCaches);
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        final Collection<Cache> caches = new LinkedHashSet<>(initialCaches.size());
        for (final JpaCache.JpaCacheBuilder cacheBuilder : initialCaches) {
            final JpaCache cache = cacheBuilder.build();
            caches.add(cache);
        }
        return caches;
    }

    @Override
    protected Cache decorateCache(Cache cache) {
        return super.decorateCache(cache);
    }

    @Override
    protected Cache getMissingCache(String name) {
        return super.getMissingCache(name);
    }
}
