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

public class SubTasksByPotentialOwnerQuery implements MapDBQuery<List<TaskSummary>> {

	@Override
	public List<TaskSummary> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		Long parentId = (Long) params.get("parentId");
		String userId = (String) params.get("userId");
		List<Status> status = Arrays.asList(Status.Created, Status.Ready, 
				Status.Reserved, Status.InProgress, Status.Suspended);
		@SuppressWarnings("unchecked")
		List<String> groupIds = (List<String>) params.get("groupIds");

		Set<Long> ids = new HashSet<>();
		addAll(ids, tts.getByActualOwner().get(userId));
		addAll(ids, tts.getByPotentialOwner().get(userId));
		for (String groupId : groupIds) {
			addAll(ids, tts.getByActualOwner().get(groupId));
			addAll(ids, tts.getByPotentialOwner().get(groupId));
		}
		removeAll(ids, tts.getByExclOwner().get(userId));
		
		Set<Long> idsByParent = new HashSet<>();
		addAll(idsByParent, tts.getByParentId().get(parentId));
		
		ids.retainAll(idsByParent);
		
		Set<Long> idsByStatus = new HashSet<>();
		for(Status s : status) {
			addAll(idsByStatus, tts.getByStatus().get(s.name()));
		}
		
		ids.retainAll(idsByStatus);
		
		List<TaskSummary> retval = new ArrayList<>(ids.size());
		for (Long id : ids) {
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
