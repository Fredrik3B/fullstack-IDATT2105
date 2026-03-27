package no.ntnu.resturant_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import no.ntnu.resturant_manager.model.ChecklistModel;

public interface ChecklistRepository extends JpaRepository<ChecklistModel, Long> {
}
