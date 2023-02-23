package ir.tata.jpacache;

import org.hibernate.TransactionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.properties.PropertyMapping;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {"spring.datasource.cache.jpaTransactionManagerDefaultTimeout=1"})
@EnableCaching
@Rollback
@Transactional("cacheTransactionManager")
public class JpaDefaultTimeoutTest {
    @Autowired
    private Service4Test service4Test;

    @Test
    @PropertyMapping()
    public void jpaTransactionManagerDefaultTimeoutTest() {
        Throwable exception = assertThrows(CacheException.class, () -> service4Test.getResponseFailFast("1"));
        assertEquals("Could not save result in cache database", exception.getMessage());
        assertEquals(JpaSystemException.class, exception.getCause().getClass());
        assertEquals(TransactionException.class, exception.getCause().getCause().getClass());
        assertEquals("transaction timeout expired", exception.getCause().getCause().getMessage());
    }
}
