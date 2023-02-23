package ir.tata.jpacache.db;

import lombok.Data;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This project is developed to be used as a library in an environment with ~100 microservices that each of which should
 * have its own table [and database] to save cache and historical data of its services. So, there is no hardcoded table
 * name here and the concrete table will be determined dynamically in runtime by {@link CacheQueryInterceptor}.
 *
 * @see <a href="file:create-table.sql">create-table.sql</a>
 */
@Data
@Entity
@Cacheable
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE) // This is used for hits. FIXME remove hits and use READ_ONLY instead.
@Table(name = "GENERIC_TABLE_NAME")
public class CacheEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String profile;
    private String cacheName;
    private String dataModelVersion;
    private String key;
    private String response;
    private String uuid;
    private LocalDateTime timestamp;
    @Version
    private Long hits;
}
