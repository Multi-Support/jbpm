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

public class TasksByBizAdminStatusQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		@SuppressWarnings("unchecked")
		List<Status> status = (List<Status>) params.get("status");
		@SuppressWarnings("unchecked")
		List<String> groupIds = (List<String>) params.get("groupIds");
		String userId = (String) params.get("userId");
		
		Set<Long> values = new HashSet<>();
		addAll(values, tts.getByBizAdmin().get(userId));
		for (String groupId : groupIds) {
			addAll(values, tts.getByBizAdmin().get(groupId));
		}
		
		Set<Long> valuesByStatus = new HashSet<>();
		for (Status s : status) {
			addAll(valuesByStatus, tts.getByStatus().get(s.name()));
		}
		
		values.retainAll(valuesByStatus);
		
		List<TaskSummary> retval = new ArrayList<>(values.size());
		for (Long id : values) {
			Task task = tts.getById().get(id);
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
