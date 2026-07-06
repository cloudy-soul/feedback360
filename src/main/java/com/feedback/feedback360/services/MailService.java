package com.feedback.feedback360.services;

import com.feedback.feedback360.entities.ModuleFormation;
import com.feedback.feedback360.entities.User;

public interface MailService {
    boolean sendFeedbackInvite(User user, ModuleFormation module);
    boolean sendReminder(User user, ModuleFormation module, String templateMessage);
}