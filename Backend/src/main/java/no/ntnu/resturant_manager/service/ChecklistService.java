package no.ntnu.resturant_manager.service;

import java.util.List;

import no.ntnu.resturant_manager.dto.ChecklistResponse;
import no.ntnu.resturant_manager.dto.CreateChecklistRequest;

public interface ChecklistService {

	ChecklistResponse createChecklist(CreateChecklistRequest request);

	List<ChecklistResponse> getAllChecklists();

	ChecklistResponse getChecklistById(Long checklistId);

	void deleteChecklist(Long checklistId);
}
