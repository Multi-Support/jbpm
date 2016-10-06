package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.Arrays;
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

public class TaskAsPotentialOwnerByGroupQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback, 
			Map<String, Object> params, TaskTableService tts, boolean singleResult) {
		List<Status> status = Arrays.asList(Status.Created, Status.Ready, Status.Reserved, 
					Status.InProgress, Status.Suspended);
		String groupId = (String) params.get("groupId");
		
		Set<Long> values = new HashSet<>();
		addAll(values, tts.getByPotentialOwner().get(groupId));
		removeAll(values, tts.getByActualOwner().get(groupId));

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
