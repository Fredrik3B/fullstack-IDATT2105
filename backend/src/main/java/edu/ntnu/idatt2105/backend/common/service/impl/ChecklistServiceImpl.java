package edu.ntnu.idatt2105.backend.common.service.impl;

import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistCardResponse;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistSectionResponse;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistSectionUpsertRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistTaskItemResponse;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.ChecklistTaskUpsertRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.CreateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.IcModule;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.TaskCompletionRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.TaskFlagRequest;
import edu.ntnu.idatt2105.backend.common.dto.icchecklist.UpdateChecklistCardRequest;
import edu.ntnu.idatt2105.backend.common.model.ChecklistModel;
import edu.ntnu.idatt2105.backend.common.model.TaskTemplate;
import edu.ntnu.idatt2105.backend.common.model.enums.ComplianceArea;
import edu.ntnu.idatt2105.backend.common.model.enums.SectionTypes;
import edu.ntnu.idatt2105.backend.common.repository.ChecklistRepository;
import edu.ntnu.idatt2105.backend.common.repository.TaskTemplateRepository;
import edu.ntnu.idatt2105.backend.common.service.ChecklistService;
import edu.ntnu.idatt2105.backend.common.service.icchecklist.PeriodKeyUtil;
import edu.ntnu.idatt2105.backend.security.JwtAuthenticatedPrincipal;
import edu.ntnu.idatt2105.backend.user.model.OrganizationModel;
import edu.ntnu.idatt2105.backend.user.repository.OrganizationRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ChecklistServiceImpl implements ChecklistService {

	private final ChecklistRepository checklistRepository;
	private final TaskTemplateRepository taskTemplateRepository;
	private final OrganizationRepository organizationRepository;

	public ChecklistServiceImpl(
		ChecklistRepository checklistRepository,
		TaskTemplateRepository taskTemplateRepository,
		OrganizationRepository organizationRepository
	) {
		this.checklistRepository = checklistRepository;
		this.taskTemplateRepository = taskTemplateRepository;
		this.organizationRepository = organizationRepository;
	}

	@Override
	public List<ChecklistCardResponse> fetchChecklists(IcModule module, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		ComplianceArea area = requireModule(module).toComplianceArea();

		List<ChecklistModel> checklists = checklistRepository
			.findAllByOrganization_IdAndComplianceAreaAndActiveTrueOrderByIdAsc(safePrincipal.getOrganizationId(), area);
		if (checklists.isEmpty()) {
			return List.of();
		}

		List<Long> checklistIds = checklists.stream().map(ChecklistModel::getId).toList();
		List<TaskTemplate> templates = taskTemplateRepository.findActiveByChecklistIdsOrdered(checklistIds);
		Map<Long, List<TaskTemplate>> templatesByChecklist = templates.stream()
			.collect(Collectors.groupingBy(template -> template.getChecklist().getId(), LinkedHashMap::new, Collectors.toList()));

		return checklists.stream()
			.map(checklist -> toCardResponse(
				checklist,
				templatesByChecklist.getOrDefault(checklist.getId(), List.of())
			))
			.toList();
	}

	@Override
	public ChecklistCardResponse createChecklist(CreateChecklistCardRequest request, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		Objects.requireNonNull(request, "Checklist request cannot be null.");

		OrganizationModel organization = organizationRepository.findById(safePrincipal.getOrganizationId())
			.orElseThrow(() -> new IllegalArgumentException("Organization not found."));

		ChecklistModel checklist = new ChecklistModel();
		checklist.setName(requireText(request.title(), "title"));
		checklist.setDescription(trimToNull(request.subtitle()));
		checklist.setFrequency(PeriodKeyUtil.periodToFrequency(request.period()));
		checklist.setComplianceArea(requireModule(request.module()).toComplianceArea());
		checklist.setActive(true);
		checklist.setOrganization(organization);

		ChecklistModel savedChecklist = checklistRepository.save(checklist);
		List<TaskTemplate> templates = replaceTemplates(savedChecklist, request.sections(), safePrincipal.getOrganizationId());
		return toCardResponse(savedChecklist, templates);
	}

	@Override
	public ChecklistCardResponse updateChecklist(
		Long checklistId,
		UpdateChecklistCardRequest request,
		JwtAuthenticatedPrincipal principal
	) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		Objects.requireNonNull(request, "Checklist request cannot be null.");

		ChecklistModel checklist = checklistRepository.findByIdAndOrganization_Id(checklistId, safePrincipal.getOrganizationId())
			.orElseThrow(() -> new IllegalArgumentException("Checklist not found."));

		checklist.setName(requireText(request.title(), "title"));
		checklist.setDescription(trimToNull(request.subtitle()));
		checklist.setFrequency(PeriodKeyUtil.periodToFrequency(request.period()));

		ChecklistModel savedChecklist = checklistRepository.save(checklist);
		List<TaskTemplate> templates = replaceTemplates(savedChecklist, request.sections(), safePrincipal.getOrganizationId());
		return toCardResponse(savedChecklist, templates);
	}

	@Override
	public ChecklistTaskItemResponse setTaskCompletion(
		Long checklistId,
		Long taskId,
		TaskCompletionRequest request,
		JwtAuthenticatedPrincipal principal
	) {
		throw new UnsupportedOperationException("Task completion will be added when TasksModel flow is implemented.");
	}

	@Override
	public ChecklistTaskItemResponse setTaskFlag(
		Long checklistId,
		Long taskId,
		TaskFlagRequest request,
		JwtAuthenticatedPrincipal principal
	) {
		throw new UnsupportedOperationException("Task flagging will be added when TasksModel flow is implemented.");
	}

	@Override
	public void deleteChecklist(Long checklistId, JwtAuthenticatedPrincipal principal) {
		JwtAuthenticatedPrincipal safePrincipal = requirePrincipal(principal);
		ChecklistModel checklist = checklistRepository.findByIdAndOrganization_Id(checklistId, safePrincipal.getOrganizationId())
			.orElseThrow(() -> new IllegalArgumentException("Checklist not found."));

		List<TaskTemplate> templates = taskTemplateRepository.findAllByChecklist_IdOrderBySectionTitleAscIdAsc(checklistId);
		if (!templates.isEmpty()) {
			taskTemplateRepository.deleteAll(templates);
		}

		checklistRepository.delete(checklist);
	}

	private List<TaskTemplate> replaceTemplates(
		ChecklistModel checklist,
		List<ChecklistSectionUpsertRequest> sections,
		UUID organizationId
	) {
		List<TaskTemplate> existing = taskTemplateRepository.findAllByChecklist_IdOrderBySectionTitleAscIdAsc(checklist.getId());
		if (!existing.isEmpty()) {
			taskTemplateRepository.deleteAll(existing);
		}

		List<ChecklistSectionUpsertRequest> safeSections = sections != null ? sections : List.of();
		List<TaskTemplate> templatesToSave = new ArrayList<>();

		for (ChecklistSectionUpsertRequest section : safeSections) {
			String sectionTitle = requireText(section.title(), "section.title");
			List<ChecklistTaskUpsertRequest> items = section.items() != null ? section.items() : List.of();

			for (ChecklistTaskUpsertRequest item : items) {
				TaskTemplate template = new TaskTemplate();
				template.setChecklist(checklist);
				template.setOrganisationId(organizationId);
				template.setTitle(requireText(item.label(), "task.label"));
				template.setSectionTitle(sectionTitle);
				template.setSectionType(resolveSectionType(sectionTitle, item.type()));
				template.setUnit(trimToNull(item.unit()));
				template.setTargetMin(item.targetMin());
				template.setTargetMax(item.targetMax());
				templatesToSave.add(template);
			}
		}

		if (templatesToSave.isEmpty()) {
			return List.of();
		}

		return taskTemplateRepository.saveAll(templatesToSave);
	}

	private ChecklistCardResponse toCardResponse(ChecklistModel checklist, List<TaskTemplate> templates) {
		return new ChecklistCardResponse(
			checklist.getId(),
			PeriodKeyUtil.frequencyToPeriod(checklist.getFrequency()),
			checklist.getName(),
			checklist.getDescription() != null ? checklist.getDescription() : "",
			null,
			Boolean.FALSE,
			"",
			"muted",
			null,
			toSectionResponses(templates)
		);
	}

	private List<ChecklistSectionResponse> toSectionResponses(List<TaskTemplate> templates) {
		Map<String, List<TaskTemplate>> grouped = new LinkedHashMap<>();
		for (TaskTemplate template : templates) {
			String sectionTitle = template.getSectionTitle();
			String key = sectionTitle == null || sectionTitle.isBlank() ? "Tasks" : sectionTitle.trim();
			grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(template);
		}

		return grouped.entrySet().stream()
			.map(entry -> new ChecklistSectionResponse(
				entry.getKey(),
				entry.getValue().stream().map(this::toTaskItemResponse).toList()
			))
			.toList();
	}

	private ChecklistTaskItemResponse toTaskItemResponse(TaskTemplate template) {
		String type = isTemperatureTemplate(template) ? "temperature" : null;
		return new ChecklistTaskItemResponse(
			template.getId(),
			template.getTitle(),
			null,
			type,
			template.getUnit(),
			template.getTargetMin(),
			template.getTargetMax(),
			"todo",
			null,
			null,
			null,
			null
		);
	}

	private boolean isTemperatureTemplate(TaskTemplate template) {
		if (template.getSectionType() == SectionTypes.TEMPERATURE_CONTROL) {
			return true;
		}
		return template.getUnit() != null || template.getTargetMin() != null || template.getTargetMax() != null;
	}

	private SectionTypes resolveSectionType(String sectionTitle, String itemType) {
		if ("temperature".equalsIgnoreCase(String.valueOf(itemType))) {
			return SectionTypes.TEMPERATURE_CONTROL;
		}

		if (sectionTitle == null || sectionTitle.isBlank()) {
			return null;
		}

		String normalized = sectionTitle.trim().toUpperCase()
			.replace('&', '_')
			.replace('-', '_')
			.replace(' ', '_');

		try {
			return SectionTypes.valueOf(normalized);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}

	private JwtAuthenticatedPrincipal requirePrincipal(JwtAuthenticatedPrincipal principal) {
		if (principal == null) throw new IllegalArgumentException("Authentication required.");
		if (principal.getOrganizationId() == null) throw new IllegalArgumentException("Organization required.");
		if (principal.getUserId() == null) throw new IllegalArgumentException("User required.");
		return principal;
	}

	private IcModule requireModule(IcModule module) {
		if (module == null) throw new IllegalArgumentException("module is required.");
		return module;
	}

	private String requireText(String value, String fieldName) {
		String trimmed = value == null ? "" : value.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " is required.");
		}
		return trimmed;
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
