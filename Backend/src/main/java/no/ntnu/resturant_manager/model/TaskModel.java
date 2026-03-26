package no.ntnu.resturant_manager.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class TaskModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(length = 1000)
	private String description;

	@Column(nullable = false)
	private boolean completed;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public TaskModel() {
		// Required by JPA
	}

	public TaskModel(String title, String description) {
		this.title = title;
		this.description = description;
		this.completed = false;
	}

	@PrePersist
	public void onCreate() {
		this.createdAt = LocalDateTime.now();
		if (!this.completed) {
			this.completed = false;
		}
	}

	public Long getId() {
		return id;
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

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}

