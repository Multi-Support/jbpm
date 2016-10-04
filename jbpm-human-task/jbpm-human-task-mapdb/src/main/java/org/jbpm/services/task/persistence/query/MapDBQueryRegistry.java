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
		registry.put("ArchivedTasks", null);//TODO
        registry.put("getAllAdminAuditTasksByUser", null);//TODO
        registry.put("getAllAuditTasks", null);//TODO
        registry.put("getAllAuditTasksByStatus", null);//TODO
        registry.put("getAllAuditTasksByUser", null);//TODO
        registry.put("getAllBAMTaskSummaries", null);//TODO
        registry.put("getAllGroupAuditTasksByUser", null);//TODO
        registry.put("getAllTasksEventsByProcessInstanceId", null);//TODO
        registry.put("getAllTasksEvents", null);//TODO
        registry.put("TasksByStatus", null);//TODO
        registry.put("TasksByStatusByProcessId", null);//TODO
        registry.put("TasksByStatusSince", null);//TODO
        registry.put("TasksAssignedAsExcludedOwner", null);//TODO
        registry.put("TasksAssignedAsPotentialOwner", null);//TODO
        registry.put("TasksAssignedAsPotentialOwnerWithGroups", null);//TODO
        registry.put("TasksAssignedAsPotentialOwnerByGroup", null);//TODO
        registry.put("TasksAssignedAsPotentialOwnerByGroupsByExpirationDateOptional", null);//TODO
        registry.put("TaskSummariesByIds", null);//TODO
        registry.put("TasksAssignedAsPotentialOwnerByGroupsByExpirationDate", null);//TODO
        registry.put("TasksAssignedAsPotentialOwnerByGroups", null);//TODO
        registry.put("TaskSummariesByIds", null);//TODO
        registry.put("GetPotentialOwnersForTaskIds", null);//TODO
        registry.put("TasksAssignedAsPotentialOwner", null);//TODO
        registry.put("TasksAssignedAsPotentialOwnerWithGroups", null);//TODO
        registry.put("TasksAssignedAsRecipient", null);//TODO
        registry.put("TasksAssignedAsTaskInitiator", null);//TODO
        registry.put("TasksAssignedAsTaskStakeholder", null);//TODO
        registry.put("TasksOwnedPotentialOwnersByTaskIds", null);//TODO
        registry.put("NewTasksOwned", null);//TODO
        registry.put("SubTasksAssignedAsPotentialOwner", null);//TODO
        registry.put("GetSubTasksByParentTaskId", null);//TODO
        registry.put("GetSubTasksByParentTaskId", null);//TODO
        registry.put("TaskByWorkItemId", null);//TODO
        registry.put(("TasksOwnedWithParticularStatusByExpirationDateBeforeSpecifiedDate"), null);//TODO
        registry.put("TasksByStatusByProcessId", null);//TODO
        registry.put("TasksByStatusByProcessIdByTaskName", null);//TODO
        registry.put("TasksByProcessInstanceId", null);//TODO
        registry.put("TasksAssignedAsPotentialOwnerStatusByExpirationDate", null);//TODO
        registry.put(("TasksAssignedAsPotentialOwnerStatusByExpirationDateOptional"), null);//TODO
        registry.put("QuickTasksAssignedAsPotentialOwnerWithGroupsByStatus", null);//TODO
        registry.put("TasksAssignedAsBusinessAdministratorByStatus", null);//TODO
        registry.put("UnescalatedEndDeadlinesByTaskIdForReminder", null);//TODO
        registry.put("UnescalatedStartDeadlinesByTaskIdForReminder", null);//TODO
        registry.put("GetActiveTasks", null);//TODO
        registry.put("GetAllTasks", null);//TODO
        registry.put("UnescalatedStartDeadlines", null);//TODO
        registry.put("UnescalatedEndDeadlines", null);//TODO
        registry.put("NewTasksAssignedAsPotentialOwner", new TaskAsPotentialOwnerQuery());
	}

	public MapDBQuery<?> getQuery(String queryName) {
		return registry.get(queryName);
	}
}
