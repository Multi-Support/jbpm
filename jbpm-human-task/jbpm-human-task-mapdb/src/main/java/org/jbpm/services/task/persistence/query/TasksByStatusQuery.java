package org.jbpm.services.task.persistence.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;

public class TasksByStatusQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Status status = (Status) params.get("status");
		Date since = (Date) params.get("since");
		long[] ids = tts.getByStatus().get(status.name());
		if (ids != null) {
			List<TaskSummary> retval = new ArrayList<>(ids.length);
			for (long id : ids) {
				retval.add(new TaskSummaryImpl(tts.getById().get(id)));
			}
			if (since != null) {
				return filter(retval, since);
			}
			return retval;
		}
		return Collections.emptyList();
	}

	private List<TaskSummary> filter(List<TaskSummary> list, Date since) {
		List<TaskSummary> retval = new ArrayList<>();
		for (TaskSummary s : retval) {
			if (s.getActivationTime() != null && s.getActivationTime().after(since)) {
				retval.add(s);
			}
		}
		return retval;
	}
}
