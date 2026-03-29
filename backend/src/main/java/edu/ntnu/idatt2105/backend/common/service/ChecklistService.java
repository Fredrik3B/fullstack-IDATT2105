package edu.ntnu.idatt2105.backend.common.service;

import java.util.List;

import edu.ntnu.idatt2105.backend.common.dto.checklist.ChecklistResponse;
import edu.ntnu.idatt2105.backend.common.dto.checklist.CreateChecklistRequest;


public interface ChecklistService {

	ChecklistResponse createChecklist(CreateChecklistRequest request);

	List<ChecklistResponse> getAllChecklists();

	ChecklistResponse getChecklistById(Long checklistId);

	void deleteChecklist(Long checklistId);
}
