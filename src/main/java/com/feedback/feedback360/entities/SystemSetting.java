package com.feedback.feedback360.entities;


import com.feedback.feedback360.enums.*;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_setting")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemSetting {
    @Id
    @Column(name = "setting_key")
    private String key;

    @Column(name = "setting_value", nullable = false, length = 1000)
    private String value;
}