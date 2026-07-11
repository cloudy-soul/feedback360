package com.feedback.feedback360.services;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.feedback.feedback360.entities.QuestionFeedback;
import com.feedback.feedback360.repositories.QuestionFeedbackRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionFeedbackService {

    private final QuestionFeedbackRepository repository;

    public List<QuestionFeedback> listAll() {
        return repository.findAll().stream()
                .sorted(Comparator.comparingInt(QuestionFeedback::getDisplayOrder))
                .toList();
    }

    public List<QuestionFeedback> listActiveOrdered() {
        return repository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    public QuestionFeedback findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + id));
    }

    @Transactional
    public QuestionFeedback save(QuestionFeedback question) {
        if (question.getId() == null && question.getDisplayOrder() == 0) {
            int max = repository.findAll().stream()
                    .mapToInt(QuestionFeedback::getDisplayOrder).max().orElse(0);
            question.setDisplayOrder(max + 1);
        }
        return repository.save(question);
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        QuestionFeedback q = findById(id);
        q.setActive(active);
        repository.save(q);
    }

    @Transactional
    public void moveUp(Long id) { swapWithNeighbor(id, -1); }

    @Transactional
    public void moveDown(Long id) { swapWithNeighbor(id, +1); }

    private void swapWithNeighbor(Long id, int direction) {
        List<QuestionFeedback> ordered = listAll();
        int idx = -1;
        for (int i = 0; i < ordered.size(); i++) {
            if (ordered.get(i).getId().equals(id)) { idx = i; break; }
        }
        int swapIdx = idx + direction;
        if (idx < 0 || swapIdx < 0 || swapIdx >= ordered.size()) return;
        QuestionFeedback a = ordered.get(idx);
        QuestionFeedback b = ordered.get(swapIdx);
        int tmp = a.getDisplayOrder();
        a.setDisplayOrder(b.getDisplayOrder());
        b.setDisplayOrder(tmp);
        repository.save(a);
        repository.save(b);
    }
}