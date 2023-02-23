package ir.tata.jpacache.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "cacheEntityManagerFactory",
        transactionManagerRef = "cacheTransactionManager",
        basePackages = {"ir.tata.jpacache.db"})
public class CacheDatasourceConfig {

    private final CacheQueryInterceptor cacheQueryInterceptor;

    public CacheDatasourceConfig(CacheQueryInterceptor cacheQueryInterceptor) {
        this.cacheQueryInterceptor = cacheQueryInterceptor;
    }

    @Bean(name = "cacheDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.cache")
    public DataSource cacheDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "cacheEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("cacheDataSource") DataSource dataSource) {
        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.session_factory.interceptor", cacheQueryInterceptor);
        return builder
                .dataSource(dataSource)
                .packages("ir.tata.jpacache.db")
                .properties(jpaProperties)
                .build();
    }

    @Bean(name = "cacheTransactionManager")
    public PlatformTransactionManager cacheTransactionManager(@Qualifier("cacheEntityManagerFactory") EntityManagerFactory cacheEntityManagerFactory,
                                                              @Value("${spring.datasource.cache.jpaTransactionManagerDefaultTimeout}") int jpaTransactionManagerDefaultTimeout) {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(cacheEntityManagerFactory);
        jpaTransactionManager.setDefaultTimeout(jpaTransactionManagerDefaultTimeout); // FIXME remove this and set timeout per transaction
        return jpaTransactionManager;
    }
}
