package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Task;

public class TasksByWorkItemIdQuery implements MapDBQuery<List<Task>> {

	@Override
	public List<Task> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Long workItemId = (Long) params.get("workItemId");
		long[] values = tts.getByWorkItemId().get(workItemId);
		List<Task> retval = new ArrayList<>();
		for (long id : values) {
			Task task = tts.getById().get(id);
			retval.add(task);
		}
		return retval;
	}
}
