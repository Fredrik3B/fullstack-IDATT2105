package edu.ntnu.idatt2105.backend.user.repository;

import edu.ntnu.idatt2105.backend.user.RoleEnum;
import edu.ntnu.idatt2105.backend.user.model.RoleModel;
import java.util.Optional;

public interface RoleRepository {

  Optional<RoleModel> findByName(RoleEnum roleEnum);
}
