package edu.ntnu.idatt2105.backend.user.repository;

import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import edu.ntnu.idatt2105.backend.user.model.enums.RoleEnum;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for {@link RoleModel}.
 *
 * <p>Used primarily by {@link edu.ntnu.idatt2105.backend.config.TableSeeder}
 * to seed role rows and by {@link edu.ntnu.idatt2105.backend.user.service.UserService} to look up
 * roles by name during registration.
 */
public interface RoleRepository extends JpaRepository<RoleModel, Integer> {

  Optional<RoleModel> findByName(RoleEnum roleEnum);
}
