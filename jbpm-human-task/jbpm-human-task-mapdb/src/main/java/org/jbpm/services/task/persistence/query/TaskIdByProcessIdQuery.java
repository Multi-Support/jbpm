package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.kie.api.task.UserGroupCallback;

public class TaskIdByProcessIdQuery implements MapDBQuery<List<Long>> {

	@Override
	public List<Long> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Long processInstanceId = (Long) params.get("processInstanceId");
		long[] values = tts.getByProcessInstanceId().get(processInstanceId);
		List<Long> retval = new ArrayList<>(values == null ? 0 : values.length);
		if (values != null) {
			for (long id : values) {
				retval.add(id);
			}
		}
		return retval;
	}

}
