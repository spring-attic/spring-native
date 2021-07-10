package app.main.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

public class AuditingListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditingListener.class);

    @PrePersist
    @PreUpdate
    @PreRemove
    private void beforeAnyOperation(Object object) {
        LOGGER.info("Operation performed on {}", object);
    }

}
