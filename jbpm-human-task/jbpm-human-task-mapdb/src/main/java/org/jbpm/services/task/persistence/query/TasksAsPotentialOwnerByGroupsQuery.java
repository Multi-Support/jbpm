package org.jbpm.services.task.persistence.query;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;

public class TasksAsPotentialOwnerByGroupsQuery implements MapDBQuery<List<Object[]>> {

	private final boolean optional;
	
	
	public TasksAsPotentialOwnerByGroupsQuery(boolean optional) {
		super();
		this.optional = optional;
	}

	@Override
	public List<Object[]> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Date expDate = (Date) params.get("expirationDate");
		@SuppressWarnings("unchecked")
		List<String> groupIds = (List<String>) params.get("groupIds");
		@SuppressWarnings("unchecked")
		List<Status> status = (List<Status>) params.get("status");
		if (status == null) {
			status = Arrays.asList(
				Status.Created, Status.Ready, Status.Reserved, 
				Status.InProgress, Status.Suspended);
		}
		Set<Long> values = new HashSet<>();
		for (String groupId : groupIds) {
			MapDBQueryUtil.addAll(values, tts.getByPotentialOwner(), groupId);
		}
		Set<Long> valuesByStatus = new HashSet<>();
		for (Status s : status) {
			MapDBQueryUtil.addAll(valuesByStatus, tts.getByStatus(), s.name());
		}
		
		values.retainAll(valuesByStatus);
		
		for (String owner : tts.getByActualOwner().keySet()) {
			MapDBQueryUtil.removeAll(values, tts.getByActualOwner(), owner);
		}
		
		List<Object[]> retval = new LinkedList<>();
		
		for (Long taskId : values) {
			if (tts.getById().containsKey(taskId)) {
				Task task = tts.getById().get(taskId);
				if (violatesExpDateCondition(expDate, task)) {
					continue;
				}
				for (OrganizationalEntity entity : task.getPeopleAssignments().getPotentialOwners()) {
					retval.add(new Object[] { taskId, entity.getId()});
				}
			}
		}
		return retval;
	}
	
	private boolean violatesExpDateCondition(Date expDate, Task task) {
		if (optional) {
			return expDate != null 
					&& task.getTaskData().getExpirationTime() != null 
					&& expDate.equals(task.getTaskData().getExpirationTime());
		} else {
			return expDate != null 
					&& (task.getTaskData().getExpirationTime() == null 
					|| expDate.equals(task.getTaskData().getExpirationTime()));
		}
	}
}
