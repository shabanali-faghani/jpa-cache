package ir.tata.jpacache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.tata.jpacache.history.JpaHistory;
import lombok.Data;

/**
 * The root response class of every service that use jpa-cache or jpa-history must extend this class.
 */
@Data
public class FollowUp {
    /**
     * This field must be ignored in service boundary, but is used in {@link JpaCache} and {@link JpaHistory}
     */
    @JsonIgnore
    protected String uuid;
}
