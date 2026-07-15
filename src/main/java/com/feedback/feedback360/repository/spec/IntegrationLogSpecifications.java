package com.feedback.feedback360.repository.spec;

import com.feedback.feedback360.entities.IntegrationLog;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;

public class IntegrationLogSpecifications {
    public static Specification<IntegrationLog> type(String type) {
        return (r,q,cb) -> type == null || type.isBlank() ? null : cb.equal(r.get("type"), type);
    }
    public static Specification<IntegrationLog> status(String status) {
        return (r,q,cb) -> status == null || status.isBlank() ? null : cb.equal(r.get("status"), status);
    }
    public static Specification<IntegrationLog> createdAfter(LocalDateTime from) {
        return (r,q,cb) -> from == null ? null : cb.greaterThanOrEqualTo(r.get("createdAt"), from);
    }
}
