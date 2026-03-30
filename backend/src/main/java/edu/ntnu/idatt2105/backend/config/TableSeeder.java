package edu.ntnu.idatt2105.backend.config;

import edu.ntnu.idatt2105.backend.user.RoleEnum;
import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import edu.ntnu.idatt2105.backend.user.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TableSeeder implements CommandLineRunner {

  private final RoleRepository roleRepository;

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
