package edu.ntnu.idatt2105.backend.common.service;

import edu.ntnu.idatt2105.backend.common.dto.ChecklistResponse;
import edu.ntnu.idatt2105.backend.common.dto.CreateChecklistRequest;
import java.util.List;


public interface ChecklistService {

	ChecklistResponse createChecklist(CreateChecklistRequest request);

	List<ChecklistResponse> getAllChecklists();

	ChecklistResponse getChecklistById(Long checklistId);

	void deleteChecklist(Long checklistId);
}
