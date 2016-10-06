package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.Date;
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

public class TOWPSBEDBSDQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Date date = (Date) params.get("date");
		List<Status> status = (List<Status>) params.get("status");
		String userId = (String) params.get("userId");
	
		Set<Long> values = new HashSet<>();
		addAll(values, tts.getByActualOwner().get(userId));
		addAll(values, tts.getByPotentialOwner().get(userId));

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
			if (task != null && matchesCondition(date, task)) {
				retval.add(new TaskSummaryImpl(task));
			}
		}
		return retval;
	}

	private boolean matchesCondition(Date date, Task task) {
		Date expTime = task.getTaskData().getExpirationTime();
		return date != null && date.before(expTime);
	}

	private void addAll(Set<Long> values, long[] v) {
		if (v != null) {
			for (long value : v) {
				values.add(value);
			}
		}
	}
}
