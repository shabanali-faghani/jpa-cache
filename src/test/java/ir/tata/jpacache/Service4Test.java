package ir.tata.jpacache;

import ir.tata.jpacache.dto.ComplexType;
import ir.tata.jpacache.dto.TypeNotExtendedFollowUp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import static ir.tata.jpacache.dto.ComplexType.RetType.NORMAL;
import static ir.tata.jpacache.dto.ComplexType.RetType.NULL;

@Service
@CacheConfig(cacheManager = "jpaCacheManager")
public class Service4Test {

    @Value("${spring.datasource.cache.enabled:false}")
    public Boolean isCacheEnabled;

    @Cacheable(cacheNames = "test", key = "#key",
            condition = "#root.target.isCacheEnabled",
            unless = "#result.equals('response2')")
    public String getResponse(String key) {
        return "response" + key;
    }

    @Cacheable(cacheManager = "jpaCacheManager", cacheNames = "test")
    public String implicitKey(String key) {
        return "response" + key;
    }

    @Cacheable(cacheManager = "jpaCacheManager", cacheNames = "complexType")
    public ComplexType complexType(String key) {
        return new ComplexType(1, "s");
    }

    @Caching(
            cacheable = {
                    @Cacheable(cacheNames = "cache-1-ttl-1sec", key = "#ct.i + '-' + #ct.s", condition = "#ct.i == 1", unless = "#result == null"),
                    @Cacheable(cacheNames = "cache-2-ttl-2sec", key = "#ct.i + '-' + #ct.s", condition = "#ct.i == 2", unless = "#result == null")
            }
    )
    public ComplexType multiCache(ComplexType ct, ComplexType.RetType retType) {
        if (retType == NORMAL) return ct;
        if (retType == NULL) return null;
        throw new IllegalStateException("Unexpected value: " + retType);
    }

    @Cacheable(cacheManager = "jpaCacheManager", cacheNames = "complex-type-fail-fast")
    public ComplexType getResponseFailFast(String key) {
        return new ComplexType(1, key);
    }

    @Cacheable(cacheManager = "jpaCacheManager", cacheNames = "cast-problem-with-fail-fast")
    public TypeNotExtendedFollowUp castProblem(String key) {
        return new TypeNotExtendedFollowUp(key);
    }
}
