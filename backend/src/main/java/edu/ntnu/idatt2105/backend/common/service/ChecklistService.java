package edu.ntnu.idatt2105.backend.common.service;

import java.time.Instant;
import java.util.List;

import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.ChecklistCardResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.ChecklistTaskItemResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.CreateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.ChecklistWorkbenchStateRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.TaskCompletionRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.TaskFlagRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.UpdateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;


public interface ChecklistService {

	List<ChecklistCardResponse> fetchChecklists(IcModule module, JwtAuthenticatedPrincipal principal);

	Instant fetchChecklistsLastModified(IcModule module, JwtAuthenticatedPrincipal principal);

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

	ChecklistCardResponse submitChecklist(Long checklistId, JwtAuthenticatedPrincipal principal);

	ChecklistCardResponse setChecklistWorkbenchState(
		Long checklistId,
		ChecklistWorkbenchStateRequest request,
		JwtAuthenticatedPrincipal principal
	);

	void deleteChecklist(Long checklistId, JwtAuthenticatedPrincipal principal);
}
