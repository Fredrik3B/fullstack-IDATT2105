package edu.ntnu.idatt2105.backend.user.repository;

import edu.ntnu.idatt2105.backend.user.RoleEnum;
import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleModel, Integer> {
  Optional<RoleModel> findByName(RoleEnum roleEnum);
}
