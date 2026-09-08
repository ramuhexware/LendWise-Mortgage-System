package com.lendwise.patterns.consumer.model;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * Abstract base class for inheritance patterns.
 * Parser should detect: abstract class, @MappedSuperclass, inheritance
 */
@MappedSuperclass
@Slf4j
public abstract class BaseCreditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Version
    private Long version;

    // Static initializer block
    // Parser should detect: static initializer
    static {
        log.info("BaseCreditEntity class loaded");
    }

    // Instance initializer block
    // Parser should detect: instance initializer
    {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Default constructor
    protected BaseCreditEntity() {
        // Instance initializer runs before this
    }

    /**
     * Abstract method to be overridden by subclasses.
     * Parser detects: abstract method declaration
     */
    public abstract String getEntityType();

    /**
     * Template method pattern.
     */
    public final void save() {
        beforeSave();
        doSave();
        afterSave();
    }

    // Hook methods for subclasses
    protected void beforeSave() {
        this.updatedAt = LocalDateTime.now();
    }

    protected abstract void doSave();

    protected void afterSave() {
        log.info("Entity saved: {}", getEntityType());
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
