package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskSummary;

public class TaskAsExcludedOwnerQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback, 
			Map<String, Object> params, TaskTableService tts, boolean singleResult) {
		final String userId = (String) params.get("userId");
		Set<Long> values = new HashSet<>();
		MapDBQueryUtil.addAll(values, tts.getByExclOwner(), userId);
		final List<TaskSummary> retval = new ArrayList<TaskSummary>();
		for (Long taskId : values) {
			if (tts.getById().containsKey(taskId)) {
				Task task = tts.getById().get(taskId);
				if (task != null) {
					retval.add(new TaskSummaryImpl(task));
				}
			}
		}
		return MapDBQueryUtil.paging(params, retval);
	}
}
