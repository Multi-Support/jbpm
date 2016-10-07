package org.jbpm.services.task.persistence.query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

public class TasksAsPotentialOwnerByGroupsWithExclusionQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		@SuppressWarnings("unchecked")
		List<String> groupIds = (List<String>) params.get("groupIds");
		@SuppressWarnings("unchecked")
		List<Status> status = (List<Status>) params.get("status");
		String userId = (String) params.get("userId");
		if (status == null) {
			status = Arrays.asList(
				Status.Created, Status.Ready, Status.Reserved, 
				Status.InProgress, Status.Suspended);
		}
		Set<Long> values = new HashSet<>();
		addAll(values, tts.getByActualOwner().get(userId));
		addAll(values, tts.getByPotentialOwner().get(userId));
		for (String groupId : groupIds) {
			addAll(values, tts.getByPotentialOwner().get(groupId));
		}
		removeAll(values, tts.getByExclOwner().get(userId));
		
		Set<Long> valuesByStatus = new HashSet<>();
		for (Status s : status) {
			addAll(valuesByStatus, tts.getByStatus().get(s.name()));
		}
		
		values.retainAll(valuesByStatus);
		
		for (long[] taskWithOwners : tts.getByActualOwner().values()) {
			addAll(values, taskWithOwners);
		}
		
		List<TaskSummary> retval = new LinkedList<TaskSummary>();
		
		for (Long taskId : values) {
			Task task = tts.getById().get(taskId);
			if (task != null) {
				retval.add(new TaskSummaryImpl(task));
			}
		}
		return retval;
	}

	private void addAll(Set<Long> values, long[] v) {
		if (v != null) {
			for (long value : v) {
				values.add(value);
			}
		}
	}

	private void removeAll(Set<Long> values, long[] v) {
		if (v != null) {
			for (long value : v) {
				if (values.contains(value)) {
					values.remove(value);
				}
			}
		}
	}
}
