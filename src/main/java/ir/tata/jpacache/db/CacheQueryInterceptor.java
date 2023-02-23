package ir.tata.jpacache.db;

import org.hibernate.EmptyInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class CacheQueryInterceptor extends EmptyInterceptor {

    private final String concreteTableName;

    public CacheQueryInterceptor(@Value("${spring.application.name}") String thisAppName) {
        this.concreteTableName = thisAppName.replaceAll("-", "_").toUpperCase();
    }

    @Override
    public String onPrepareStatement(String sql) {
        String prepareStatement = super.onPrepareStatement(sql);
        return prepareStatement.replaceAll("GENERIC_TABLE_NAME", concreteTableName);
    }
}
