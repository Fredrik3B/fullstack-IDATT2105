package edu.ntnu.idatt2105.backend.common.repository;

import edu.ntnu.idatt2105.backend.common.model.ChecklistModel;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ChecklistRepository extends JpaRepository<ChecklistModel, Long> {
}
