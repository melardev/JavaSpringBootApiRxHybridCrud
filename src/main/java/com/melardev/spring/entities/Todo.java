package com.melardev.spring.entities;

import reactor.core.publisher.Mono;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.melardev.spring.config.DbConfig.DB_SCHEDULER;


@Entity
@Table(name = "todos")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;

    // To avoid Caused by: org.h2.jdbc.JdbcSQLException: Value too long for column "DESCRIPTION VARCHAR(255)
    @Lob
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean completed;

    public Long getId() {
        return id;
    }

    public Todo() {
    }

    public Todo(Long id, String title, String description, Boolean completed, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Todo(String title, String description) {
        this(title, description, false);
    }

    public Todo(String title, String description, boolean completed) {
        this(null, title, description, completed, null, null);
    }

    public Todo(Long id, String title, boolean completed, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, title, null, completed, createdAt, updatedAt);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @PrePersist
    public void preSave() {
        Mono.defer(() -> {
            if (this.createdAt == null) {
                setCreatedAt(LocalDateTime.now());
            }
            if (this.updatedAt == null)
                setUpdatedAt(LocalDateTime.now());
            return Mono.empty();
        }).subscribeOn(DB_SCHEDULER).subscribe();
    }

    @PreUpdate
    public void preUpdate() {
        Mono.defer(() -> {
            setUpdatedAt(LocalDateTime.now());
            return Mono.empty();
        }).subscribeOn(DB_SCHEDULER).subscribe();
    }
}