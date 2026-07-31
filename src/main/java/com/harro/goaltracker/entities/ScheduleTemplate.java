package com.harro.goaltracker.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="schedule_templates")
public class ScheduleTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "name")
    String name;

    @OneToMany(mappedBy = "scheduleTemplate", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<EventTemplate> eventTemplates = new ArrayList<>();
}
