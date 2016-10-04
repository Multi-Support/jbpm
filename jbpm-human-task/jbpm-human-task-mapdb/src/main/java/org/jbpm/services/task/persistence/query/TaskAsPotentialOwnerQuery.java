package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

public class TaskAsPotentialOwnerQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback, 
			Map<String, Object> params, TaskTableService tts, boolean singleResult) {
		@SuppressWarnings("unchecked")
		final List<Status> status = (List<Status>) params.get("status");
		final String userId = (String) params.get("userId");
		List<String> groupIds = callback.getGroupsForUser(userId, null, null);
		
		Set<Long> values = new HashSet<>();
		addAll(values, tts.getByActualOwner().get(userId));
		addAll(values, tts.getByBizAdmin().get(userId));
		for (String groupId : groupIds) {
			addAll(values, tts.getByBizAdmin().get(groupId));
			addAll(values, tts.getByPotentialOwner().get(groupId));
		}
		long[] exclOwnerTasks = tts.getByExclOwner().get(userId);
		if (exclOwnerTasks != null) {
			for (long taskId : exclOwnerTasks) {
				if (values.contains(taskId)) {
					values.remove(taskId);
				}
			}
		}

		Set<Long> valuesByStatus = new HashSet<>();
		if (status != null) {
			for (Status stat : status) {
				addAll(valuesByStatus, tts.getByStatus().get(stat.name()));
			}
		}
		
		values.retainAll(valuesByStatus); //and operation
		
		final List<TaskSummary> retval = new ArrayList<TaskSummary>();
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

}
