package edu.ntnu.idatt2105.backend.common.service;

import java.util.List;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistCardResponse;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistTaskItemResponse;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.CreateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.TaskCompletionRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.TaskFlagRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.UpdateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;


public interface ChecklistService {

	List<ChecklistCardResponse> fetchChecklists(IcModule module, JwtAuthenticatedPrincipal principal);

	ChecklistCardResponse createChecklist(CreateChecklistCardRequest request, JwtAuthenticatedPrincipal principal);

	ChecklistCardResponse updateChecklist(
		Long checklistId,
		UpdateChecklistCardRequest request,
		JwtAuthenticatedPrincipal principal
	);

	ChecklistTaskItemResponse setTaskCompletion(
		Long checklistId,
		Long taskId,
		TaskCompletionRequest request,
		JwtAuthenticatedPrincipal principal
	);

	ChecklistTaskItemResponse setTaskFlag(
		Long checklistId,
		Long taskId,
		TaskFlagRequest request,
		JwtAuthenticatedPrincipal principal
	);

	void deleteChecklist(Long checklistId, JwtAuthenticatedPrincipal principal);
}
