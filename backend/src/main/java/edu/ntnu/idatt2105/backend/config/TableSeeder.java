package edu.ntnu.idatt2105.backend.config;

import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import edu.ntnu.idatt2105.backend.user.model.enums.RoleEnum;
import edu.ntnu.idatt2105.backend.user.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds required reference data into the database on application startup.
 *
 * <p>Inserts any missing {@link edu.ntnu.idatt2105.backend.user.model.RoleModel} entries
 * for all values of {@link edu.ntnu.idatt2105.backend.user.model.enums.RoleEnum}. This ensures the
 * role table is always populated before any user registration occurs, even on a fresh database.
 */
@Component
@AllArgsConstructor
public class TableSeeder implements CommandLineRunner {

  private final RoleRepository roleRepository;

  /**
   * Inserts missing role rows on startup. Existing rows are left unchanged.
   *
   * @param args command-line arguments (not used)
   */
  @Override
  public void run(String[] args) {
    for (RoleEnum role : RoleEnum.values()) {
      if (roleRepository.findByName(role).isEmpty()) {
        RoleModel newRole = new RoleModel();
        newRole.setName(role);
        roleRepository.save(newRole);
      }
    }
  }
}
