package ir.tata.jpacache;

import com.google.gson.GsonBuilder;
import net.andreinc.mockneat.abstraction.MockUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.andreinc.mockneat.unit.address.Cities.cities;
import static net.andreinc.mockneat.unit.objects.ObjectMap.objectMap;
import static net.andreinc.mockneat.unit.time.LocalDates.localDates;
import static net.andreinc.mockneat.unit.types.Ints.ints;
import static net.andreinc.mockneat.unit.user.Names.names;
import static org.junit.jupiter.api.Assertions.*;

@Rollback
@SpringBootTest
@SpringBootApplication
@Transactional("cacheTransactionManager")
public class JpaCacheServiceTest {

    @Autowired
    private JpaCacheService jpaCacheService;

    @Test
    void save() {
        assertDoesNotThrow(() -> jpaCacheService.save("testCache", "key1", "response1", UUID.randomUUID().toString()));
    }

    @Test
    void getByKey() {
        jpaCacheService.save("testCache", "key1", "response1", UUID.randomUUID().toString());
        assertEquals("key1", jpaCacheService.get("testCache", "key1").stream().findFirst().get().getKey());
        assertEquals("response1", jpaCacheService.get("testCache", "key1").stream().findFirst().get().getResponse());
    }

    @Test
    void getWithExpiration() {
        jpaCacheService.save("default", "testCache", "v1", "key1", "response1", UUID.randomUUID().toString(), LocalDateTime.now().minusHours(24), 1L);
        assertNull(jpaCacheService.get("testCache", "key1", TimeUnit.HOURS.toSeconds(20)));
        assertNull(jpaCacheService.get("testCache", "key1", TimeUnit.HOURS.toSeconds(24) - 1));
        assertNotNull(jpaCacheService.get("testCache", "key1", TimeUnit.HOURS.toSeconds(25)));
    }

    @Test
    void getLatestByKey() {
        jpaCacheService.save("default", "testCache", "v1", "key1", "response1", UUID.randomUUID().toString(), LocalDateTime.now().minusHours(24), 1L);
        jpaCacheService.save("default", "testCache", "v1", "key1", "response2", UUID.randomUUID().toString(), LocalDateTime.now().minusHours(20), 1L);
        jpaCacheService.save("default", "testCache", "v1", "key1", "response3", UUID.randomUUID().toString(), LocalDateTime.now().minusHours(10), 1L);
        assertEquals("response3", jpaCacheService.getLatest("testCache", "key1").getResponse());
    }

    @Test
    void getLatestResponseByKey() {
        jpaCacheService.save("default", "testCache", "v1", "key1", "response1", UUID.randomUUID().toString(), LocalDateTime.now().minusDays(24), 1L);
        jpaCacheService.save("default", "testCache", "v1", "key1", "response2", UUID.randomUUID().toString(), LocalDateTime.now().minusMinutes(20), 1L);
        jpaCacheService.save("default", "testCache", "v1", "key1", "response3", UUID.randomUUID().toString(), LocalDateTime.now().minusSeconds(1), 1L);
        assertEquals("response3", jpaCacheService.getLatestResponse("testCache", "key1"));
    }

    @Test
    void saveClob() {
        MockUnit<List<Map<String, Object>>> list = objectMap()
                .put("person", objectMap()
                        .put("name", names().full())
                        .put("age", ints().range(18, 60))
                        .put("visits", objectMap()
                                .put("city", cities().capitalsEurope())
                                .put("date", localDates().thisYear().display("yyyy-MM-dd"))
                                .list(ints().range(3, 6))))
                .list(1000);
        String bigJsonResponse = new GsonBuilder().setPrettyPrinting().create().toJson(list.get());
        assertDoesNotThrow(() -> jpaCacheService.save("testCache", "Key", bigJsonResponse, UUID.randomUUID().toString()));
    }

    @Test
    void followUp() {
        String uuid = UUID.randomUUID().toString();
        jpaCacheService.save("testCache", "key1", "response1", uuid);

        assertEquals("response1", jpaCacheService.followUp(uuid));
    }
}
