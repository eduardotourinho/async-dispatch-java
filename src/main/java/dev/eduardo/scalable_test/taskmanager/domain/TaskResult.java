package dev.eduardo.scalable_test.taskmanager.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task-results")
public class TaskResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String result;

    @CreationTimestamp
    @Column(columnDefinition = "timestamp")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(columnDefinition = "timestamp")
    private Instant updatedAt;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;
}
