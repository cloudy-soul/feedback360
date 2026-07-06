package com.feedback.feedback360.services;

import com.feedback.feedback360.dto.DashboardDTO;
import com.feedback.feedback360.enums.FeedbackStatus;
import com.feedback.feedback360.repositories.FeedbackRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Builds all KPIs and chart data consumed by the manager/HR Angular dashboard
 * (pie: submitted vs pending, donut: by department, line: rating trend, bar: top modules).
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FeedbackRepository feedbackRepository;

    @PersistenceContext
    private EntityManager em;

    public DashboardDTO build() {
        long total = feedbackRepository.count();

        long submitted = ((Number) em.createQuery(
                "SELECT COUNT(f) FROM Feedback f WHERE f.status = :status")
                .setParameter("status", FeedbackStatus.SUBMITTED)
                .getSingleResult()).longValue();

        Double avgRatingRaw = (Double) em.createQuery(
                "SELECT AVG(f.rating) FROM Feedback f WHERE f.status = :status")
                .setParameter("status", FeedbackStatus.SUBMITTED)
                .getSingleResult();

        List<Object[]> perModule = em.createQuery(
                "SELECT f.module.title, AVG(f.rating), COUNT(f) FROM Feedback f " +
                "WHERE f.status = :status GROUP BY f.module.title ORDER BY AVG(f.rating) DESC",
                Object[].class)
                .setParameter("status", FeedbackStatus.SUBMITTED)
                .getResultList();

        List<DashboardDTO.ModuleRating> ranked = new ArrayList<>();
        for (Object[] r : perModule) {
            ranked.add(new DashboardDTO.ModuleRating((String) r[0], round1((Double) r[1]), (Long) r[2]));
        }

        List<Object[]> byDeptRaw = em.createQuery(
                "SELECT f.user.department, COUNT(f) FROM Feedback f " +
                "WHERE f.status = :status GROUP BY f.user.department", Object[].class)
                .setParameter("status", FeedbackStatus.SUBMITTED)
                .getResultList();

        Map<String, Long> deptMap = new LinkedHashMap<>();
        for (Object[] r : byDeptRaw) {
            String dept = r[0] != null ? (String) r[0] : "Unspecified";
            deptMap.put(dept, (Long) r[1]);
        }

        // Native PostgreSQL query — to_char for month bucketing, documented since
        // it is not portable to other databases (project uses PostgreSQL only).
        List<Object[]> trendRaw = em.createNativeQuery(
                "SELECT to_char(submitted_at,'YYYY-MM') AS p, AVG(rating) " +
                "FROM feedback WHERE status = 'SUBMITTED' GROUP BY p ORDER BY p")
                .getResultList();

        List<DashboardDTO.TrendPoint> trend = new ArrayList<>();
        for (Object[] r : trendRaw) {
            trend.add(new DashboardDTO.TrendPoint((String) r[0], round1(((Number) r[1]).doubleValue())));
        }

        List<DashboardDTO.ModuleRating> topFive = ranked.size() > 5 ? ranked.subList(0, 5) : ranked;
        List<DashboardDTO.ModuleRating> bottomFive = new ArrayList<>(ranked);
        Collections.reverse(bottomFive);
        if (bottomFive.size() > 5) bottomFive = bottomFive.subList(0, 5);

        return new DashboardDTO(
                total,
                submitted,
                total == 0 ? 0 : round1(100.0 * submitted / total),
                avgRatingRaw != null ? round1(avgRatingRaw) : 0.0,
                topFive,
                bottomFive,
                deptMap,
                trend,
                total - submitted
        );
    }

    private double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
