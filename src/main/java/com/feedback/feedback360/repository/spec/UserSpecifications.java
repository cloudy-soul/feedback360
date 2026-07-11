package com.feedback.feedback360.repository.spec;

import com.feedback.feedback360.entities.User;
import com.feedback.feedback360.enums.Role;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<User> nameOrEmailContains(String search) {
        return (r, q, cb) -> {
            if (search == null || search.isBlank()) return null;
            String like = "%" + search.toLowerCase() + "%";
            Predicate first = cb.like(cb.lower(r.get("firstName")), like);
            Predicate last = cb.like(cb.lower(r.get("lastName")), like);
            Predicate email = cb.like(cb.lower(r.get("email")), like);
            return cb.or(first, last, email);
        };
    }

    public static Specification<User> roleEquals(Role role) {
        return (r, q, cb) -> role == null ? null : cb.equal(r.get("role"), role);
    }

    public static Specification<User> activeEquals(Boolean active) {
        return (r, q, cb) -> active == null ? null : cb.equal(r.get("active"), active);
    }
}
