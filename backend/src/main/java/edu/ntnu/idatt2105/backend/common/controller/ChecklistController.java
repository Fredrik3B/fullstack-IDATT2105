package edu.ntnu.idatt2105.backend.common.controller;

import edu.ntnu.idatt2105.backend.common.dto.ChecklistResponse;
import edu.ntnu.idatt2105.backend.common.dto.CreateChecklistRequest;
import edu.ntnu.idatt2105.backend.common.service.ChecklistService;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/checklists")
public class ChecklistController {

	private final ChecklistService checklistService;

	public ChecklistController(ChecklistService checklistService) {
		this.checklistService = checklistService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ChecklistResponse createChecklist(@Valid @RequestBody CreateChecklistRequest request) {
		return checklistService.createChecklist(request);
	}

	@GetMapping
	public List<ChecklistResponse> getAllChecklists() {
		return checklistService.getAllChecklists();
	}

	@GetMapping("/{checklistId}")
	public ChecklistResponse getChecklistById(@PathVariable Long checklistId) {
		return checklistService.getChecklistById(checklistId);
	}

	@DeleteMapping("/{checklistId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteChecklist(@PathVariable Long checklistId) {
		checklistService.deleteChecklist(checklistId);
	}
}
