package org.jbpm.services.task.persistence.query;

import java.util.HashMap;
import java.util.Map;

public class MapDBQueryRegistry {

	private static final MapDBQueryRegistry INSTANCE = new MapDBQueryRegistry();
	
	private final Map<String, MapDBQuery<?>> registry = new HashMap<>();
	
	public static MapDBQueryRegistry getInstance() {
		return INSTANCE;
	}

	private MapDBQueryRegistry() {
		init();
	}
	
	private void init() {
        registry.put("TasksByStatus", new TasksByStatusQuery());
        registry.put("TasksByStatusByProcessId", new TasksByStatusAndProcessInstanceIdQuery());
        registry.put("TasksByStatusByProcessIdByTaskName", new TasksByStatusAndProcessInstanceIdQuery());
        registry.put("TasksByStatusSince", new TasksByStatusQuery());
        registry.put("TasksAssignedAsExcludedOwner", new TaskAsExcludedOwnerQuery());
        registry.put("TasksAssignedAsPotentialOwner", new TaskAsPotentialOwnerQuery());
        registry.put("TasksAssignedAsPotentialOwnerWithGroups", new TaskAsPotentialOwnerQuery());
        registry.put("TasksAssignedAsPotentialOwnerByGroup", new TaskAsPotentialOwnerByGroupQuery());
        registry.put("TasksAssignedAsPotentialOwnerByGroups", new TasksAsPotentialOwnerByGroupsQuery(true));
        registry.put("TasksAssignedAsPotentialOwnerByGroupsByExpirationDateOptional", new TasksAsPotentialOwnerByGroupsQuery(true));
        registry.put("TaskSummariesByIds", new TaskSummariesByIdsQuery());
        registry.put("TasksAssignedAsPotentialOwnerByGroupsByExpirationDate", new TasksAsPotentialOwnerByGroupsQuery(false));
        registry.put("TasksAssignedAsRecipient", new TaskAsRecipientQuery());
        registry.put("TasksAssignedAsTaskStakeholder", new TaskAsStakeholderQuery());
        registry.put("TasksAssignedAsTaskInitiator", new TaskAsInitiatorQuery());
        registry.put("NewTasksOwned", new TasksOwnedQuery());
        registry.put("TasksAssignedAsPotentialOwnerStatusByExpirationDate", new TasksAsPotentialOwnerByGroupsQuery(false));
        registry.put("TasksAssignedAsPotentialOwnerStatusByExpirationDateOptional", new TasksAsPotentialOwnerByGroupsQuery(true));
        registry.put("TasksOwnedWithParticularStatusByExpirationDateBeforeSpecifiedDate", new TOWPSBEDBSDQuery());
        registry.put("GetActiveTasks", new ActiveTasksQuery());
        registry.put("GetAllTasks", new AllTasksQuery());
        registry.put("NewTasksAssignedAsPotentialOwner", new TaskAsPotentialOwnerQuery());
		registry.put("ArchivedTasks", new EmptyTaskQuery()); //we don't archive for now
        registry.put("getAllAdminAuditTasksByUser", null);//TODO
        registry.put("getAllAuditTasks", null);//TODO
        registry.put("getAllAuditTasksByStatus", null);//TODO
        registry.put("getAllAuditTasksByUser", null);//TODO
        registry.put("getAllBAMTaskSummaries", null);//TODO
        registry.put("getAllGroupAuditTasksByUser", null);//TODO
        registry.put("getAllTasksEventsByProcessInstanceId", null);//TODO
        registry.put("getAllTasksEvents", null);//TODO
        registry.put("GetPotentialOwnersForTaskIds", null);//TODO
        registry.put("TasksOwnedPotentialOwnersByTaskIds", null);//TODO
        registry.put("TasksByStatusByProcessId", null);//TODO
        registry.put("TasksByProcessInstanceId", null);//TODO
        registry.put("SubTasksAssignedAsPotentialOwner", null);//TODO
        registry.put("GetSubTasksByParentTaskId", null);//TODO
        registry.put("TaskByWorkItemId", null);//TODO
        registry.put("QuickTasksAssignedAsPotentialOwnerWithGroupsByStatus", null);//TODO
        registry.put("TasksAssignedAsBusinessAdministratorByStatus", null);//TODO
        registry.put("UnescalatedEndDeadlinesByTaskIdForReminder", null);//TODO
        registry.put("UnescalatedStartDeadlinesByTaskIdForReminder", null);//TODO
        registry.put("UnescalatedStartDeadlines", null);//TODO
        registry.put("UnescalatedEndDeadlines", null);//TODO
	}

	public MapDBQuery<?> getQuery(String queryName) {
		return registry.get(queryName);
	}
}
