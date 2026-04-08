package edu.ntnu.idatt2105.backend.checklist.controller;

import edu.ntnu.idatt2105.backend.checklist.service.ChecklistService;
import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.ChecklistCardResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.ChecklistTaskItemResponse;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.ChecklistWorkbenchStateRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.CreateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.TaskCompletionRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.TaskFlagRequest;
import edu.ntnu.idatt2105.backend.checklist.dto.icchecklist.UpdateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Checklists", description = "Create, fetch, update, and manage checklists")
@RestController
@AllArgsConstructor
@RequestMapping("/checklists")
public class ChecklistController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChecklistController.class);

	private final ChecklistService checklistService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@Operation(summary = "Create a checklist")
	@ApiResponse(responseCode = "201", description = "Checklist created")
	public ChecklistCardResponse createChecklist(
		@Valid @RequestBody CreateChecklistCardRequest request,
		Authentication auth
	) {
		JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
		int taskCount = request.taskTemplateIds() != null ? request.taskTemplateIds().size() : 0;
		LOGGER.info(
			"IC create checklist: orgId={} userId={} module={} period={} title='{}' taskTemplates={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			request.module(),
			request.period(),
			request.title(),
			taskCount
		);

		ChecklistCardResponse created = checklistService.createChecklist(request, principal);
		LOGGER.info("IC create checklist: created checklistId={}", created.id());
		return created;
	}

	@GetMapping
	@Operation(summary = "Fetch checklists by module")
	@ApiResponse(responseCode = "200", description = "Checklists returned")
	public ResponseEntity<List<ChecklistCardResponse>> getChecklists(
		@RequestParam IcModule module,
		WebRequest webRequest,
		Authentication auth
	) {
		JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
		Instant lastModified = checklistService.fetchChecklistsLastModified(module, principal);
		long lastModifiedMillis = toHttpLastModifiedMillis(lastModified);
		if (webRequest.checkNotModified(lastModifiedMillis)) {
			LOGGER.info(
				"IC fetch checklists: orgId={} userId={} module={} not modified",
				principal.getOrganizationId(),
				principal.getUserId(),
				module
			);
			return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
				.lastModified(lastModifiedMillis)
				.build();
		}
		LOGGER.info(
			"IC fetch checklists: orgId={} userId={} module={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			module
		);
		List<ChecklistCardResponse> cards = checklistService.fetchChecklists(module, principal);
		LOGGER.info("IC fetch checklists: returned {} cards", cards.size());
		return ResponseEntity.ok()
			.header(HttpHeaders.CACHE_CONTROL, "private, max-age=0, must-revalidate")
			.lastModified(lastModifiedMillis)
			.body(cards);
	}

	@PutMapping("/{checklistId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@Operation(summary = "Update a checklist")
	@ApiResponse(responseCode = "200", description = "Checklist updated")
	public ChecklistCardResponse updateChecklist(
		@PathVariable Long checklistId,
		@Valid @RequestBody UpdateChecklistCardRequest request,
		Authentication auth
	) {
		JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
		int taskCount = request.taskTemplateIds() != null ? request.taskTemplateIds().size() : 0;
		LOGGER.info(
			"IC update checklist: orgId={} userId={} checklistId={} period={} title='{}' taskTemplates={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			checklistId,
			request.period(),
			request.title(),
			taskCount
		);
		ChecklistCardResponse updated = checklistService.updateChecklist(checklistId, request, principal);
		LOGGER.info("IC update checklist: updated checklistId={}", updated.id());
		return updated;
	}

	@PutMapping("/{checklistId}/tasks/{taskId}/completion")
	@Operation(summary = "Set checklist task completion state")
	@ApiResponse(responseCode = "200", description = "Task completion updated")
	public ChecklistTaskItemResponse setTaskCompletion(
		@PathVariable Long checklistId,
		@PathVariable Long taskId,
		@Valid @RequestBody TaskCompletionRequest request,
		Authentication auth
	) {
		JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
		LOGGER.info(
			"IC set completion: orgId={} userId={} checklistId={} taskId={} state={} periodKey={} completedAt={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			checklistId,
			taskId,
			request.state(),
			request.periodKey(),
			request.completedAt()
		);
		return checklistService.setTaskCompletion(checklistId, taskId, request, principal);
	}

	@PutMapping("/{checklistId}/tasks/{taskId}/flag")
	@Operation(summary = "Set checklist task flag state")
	@ApiResponse(responseCode = "200", description = "Task flag updated")
	public ChecklistTaskItemResponse setTaskFlag(
		@PathVariable Long checklistId,
		@PathVariable Long taskId,
		@Valid @RequestBody TaskFlagRequest request,
		Authentication auth
	) {
		JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
		LOGGER.info(
			"IC set flag: orgId={} userId={} checklistId={} taskId={} state={} periodKey={} flaggedAt={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			checklistId,
			taskId,
			request.state(),
			request.periodKey(),
			request.flaggedAt()
		);
		return checklistService.setTaskFlag(checklistId, taskId, request, principal);
	}

	@PutMapping("/{checklistId}/submit")
	@Operation(summary = "Submit the current checklist period and start a new one")
	@ApiResponse(responseCode = "200", description = "Checklist submitted")
	public ChecklistCardResponse submitChecklist(@PathVariable Long checklistId, Authentication auth) {
		JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
		LOGGER.info(
			"IC submit checklist: orgId={} userId={} checklistId={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			checklistId
		);
		return checklistService.submitChecklist(checklistId, principal);
	}

	@PutMapping("/{checklistId}/workbench")
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@Operation(summary = "Set whether a checklist is displayed on the workbench")
	@ApiResponse(responseCode = "200", description = "Checklist workbench state updated")
	public ChecklistCardResponse setChecklistWorkbenchState(
		@PathVariable Long checklistId,
		@Valid @RequestBody ChecklistWorkbenchStateRequest request,
		Authentication auth
	) {
		JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
		LOGGER.info(
			"IC set workbench state: orgId={} userId={} checklistId={} displayedOnWorkbench={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			checklistId,
			request.displayedOnWorkbench()
		);
		return checklistService.setChecklistWorkbenchState(checklistId, request, principal);
	}

	@DeleteMapping("/{checklistId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
	@Operation(summary = "Delete a checklist")
	@ApiResponse(responseCode = "204", description = "Checklist deleted")
	public void deleteChecklist(@PathVariable Long checklistId, Authentication auth) {
		JwtAuthenticatedPrincipal principal = JwtAuthenticatedPrincipal.from(auth);
		LOGGER.info(
			"IC delete checklist: orgId={} userId={} checklistId={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			checklistId
		);
		checklistService.deleteChecklist(checklistId, principal);
	}

	private long toHttpLastModifiedMillis(Instant lastModified) {
		long millis = (lastModified != null ? lastModified : Instant.EPOCH).toEpochMilli();
		return millis - Math.floorMod(millis, 1000L);
	}
}
