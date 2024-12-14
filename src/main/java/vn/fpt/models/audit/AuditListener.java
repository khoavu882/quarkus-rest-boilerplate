package vn.fpt.models.audit;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import vn.fpt.models.AbstractAuditingEntity;

import java.time.Instant;

/**
 * Listener auto-inject UserInfo in request for {@link AbstractAuditingEntity}.
 */
@Slf4j
@RequestScoped
public class AuditListener {

    @Setter
    private static String currentUser = null;

    public static String getCurrentUser() {
        try {
            if (currentUser == null) return "anonymous";
            else return currentUser;
        } catch (Exception ex) {
            return "anonymous";
        }
    }

    public static void clearCurrentUser() {
        currentUser = null;
    }

    @PrePersist
    private void auditPrePersist(AbstractAuditingEntity auditingEntity) {

        String username = getCurrentUser();

        auditingEntity.setCreatedBy(username);
        auditingEntity.setLastModifiedBy(username);
        auditingEntity.setCreatedDate(Instant.now());
        auditingEntity.setLastModifiedDate(Instant.now());
    }

    @PreUpdate
    @PreRemove
    private void auditPreChange(AbstractAuditingEntity auditingEntity) {

        String username = getCurrentUser();

        auditingEntity.setLastModifiedBy(username);
        auditingEntity.setLastModifiedDate(Instant.now());
    }
}
