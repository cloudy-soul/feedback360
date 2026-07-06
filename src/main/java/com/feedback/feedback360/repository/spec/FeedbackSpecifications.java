package com.feedback.feedback360.repository.spec;

import com.feedback.feedback360.entities.Feedback;
import com.feedback.feedback360.enums.FeedbackStatus;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;

public class FeedbackSpecifications {

    public static Specification<Feedback> moduleTitleContains(String module) {
        return (r, q, cb) -> module == null || module.isBlank()
                ? null
                : cb.like(cb.lower(r.get("module").get("title")), "%" + module.toLowerCase() + "%");
    }

    public static Specification<Feedback> departmentEquals(String department) {
        return (r, q, cb) -> department == null || department.isBlank()
                ? null
                : cb.equal(r.get("user").get("department"), department);
    }

    public static Specification<Feedback> submittedAfter(LocalDateTime from) {
        return (r, q, cb) -> from == null ? null : cb.greaterThanOrEqualTo(r.get("submittedAt"), from);
    }

    public static Specification<Feedback> submittedBefore(LocalDateTime to) {
        return (r, q, cb) -> to == null ? null : cb.lessThanOrEqualTo(r.get("submittedAt"), to);
    }

    public static Specification<Feedback> minRating(Short min) {
        return (r, q, cb) -> min == null ? null : cb.greaterThanOrEqualTo(r.get("rating"), min);
    }

    public static Specification<Feedback> statusEquals(String status) {
        return (r, q, cb) -> status == null || status.isBlank()
                ? null
                : cb.equal(r.get("status"), FeedbackStatus.valueOf(status));
    }
}
