package edu.ntnu.idatt2105.backend.common.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
import org.springframework.security.core.Authentication;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistCardResponse;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistTaskItemResponse;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.CreateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.TaskCompletionRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.TaskFlagRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.UpdateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.common.service.ChecklistService;
import edu.ntnu.idatt2105.backend.security.AuthenticationUtils;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import jakarta.validation.Valid;



@RestController
@RequestMapping("/checklists")
public class ChecklistController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChecklistController.class);

	private final ChecklistService checklistService;

	public ChecklistController(ChecklistService checklistService) {
		this.checklistService = checklistService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ChecklistCardResponse createChecklist(
		@Valid @RequestBody CreateChecklistCardRequest request,
		Authentication auth
	) {
		JwtAuthenticatedPrincipal principal = AuthenticationUtils.requirePrincipal(auth);
		int sectionCount = request.sections() != null ? request.sections().size() : 0;
		int taskCount = request.sections() == null ? 0 : request.sections().stream()
			.mapToInt(s -> s.items() != null ? s.items().size() : 0)
			.sum();
		LOGGER.info(
			"IC create checklist: orgId={} userId={} module={} period={} title='{}' sections={} tasks={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			request.module(),
			request.period(),
			request.title(),
			sectionCount,
			taskCount
		);

		ChecklistCardResponse created = checklistService.createChecklist(request, principal);
		LOGGER.info("IC create checklist: created checklistId={}", created.id());
		return created;
	}

	@GetMapping
	public List<ChecklistCardResponse> getChecklists(
		@RequestParam IcModule module,
		Authentication auth
	) {
		JwtAuthenticatedPrincipal principal = AuthenticationUtils.requirePrincipal(auth);
		LOGGER.info(
			"IC fetch checklists: orgId={} userId={} module={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			module
		);
		List<ChecklistCardResponse> cards = checklistService.fetchChecklists(module, principal);
		LOGGER.info("IC fetch checklists: returned {} cards", cards.size());
		return cards;
	}

	@PutMapping("/{checklistId}")
	public ChecklistCardResponse updateChecklist(
		@PathVariable Long checklistId,
		@Valid @RequestBody UpdateChecklistCardRequest request,
		Authentication auth
	) {
		JwtAuthenticatedPrincipal principal = AuthenticationUtils.requirePrincipal(auth);
		int sectionCount = request.sections() != null ? request.sections().size() : 0;
		int taskCount = request.sections() == null ? 0 : request.sections().stream()
			.mapToInt(s -> s.items() != null ? s.items().size() : 0)
			.sum();
		LOGGER.info(
			"IC update checklist: orgId={} userId={} checklistId={} period={} title='{}' sections={} tasks={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			checklistId,
			request.period(),
			request.title(),
			sectionCount,
			taskCount
		);
		ChecklistCardResponse updated = checklistService.updateChecklist(checklistId, request, principal);
		LOGGER.info("IC update checklist: updated checklistId={}", updated.id());
		return updated;
	}

	@PutMapping("/{checklistId}/tasks/{taskId}/completion")
	public ChecklistTaskItemResponse setTaskCompletion(
		@PathVariable Long checklistId,
		@PathVariable Long taskId,
		@Valid @RequestBody TaskCompletionRequest request,
		Authentication auth
	) {
		JwtAuthenticatedPrincipal principal = AuthenticationUtils.requirePrincipal(auth);
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
	public ChecklistTaskItemResponse setTaskFlag(
		@PathVariable Long checklistId,
		@PathVariable Long taskId,
		@Valid @RequestBody TaskFlagRequest request,
		Authentication auth
	) {
		JwtAuthenticatedPrincipal principal = AuthenticationUtils.requirePrincipal(auth);
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

	@DeleteMapping("/{checklistId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteChecklist(@PathVariable Long checklistId, Authentication auth) {
		JwtAuthenticatedPrincipal principal = AuthenticationUtils.requirePrincipal(auth);
		LOGGER.info(
			"IC delete checklist: orgId={} userId={} checklistId={}",
			principal.getOrganizationId(),
			principal.getUserId(),
			checklistId
		);
		checklistService.deleteChecklist(checklistId, principal);
	}
}
