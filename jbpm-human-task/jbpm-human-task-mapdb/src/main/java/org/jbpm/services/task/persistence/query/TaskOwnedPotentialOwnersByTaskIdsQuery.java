package org.jbpm.services.task.persistence.query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.services.task.persistence.index.TaskTableService;
import org.kie.api.task.UserGroupCallback;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;

public class TaskOwnedPotentialOwnersByTaskIdsQuery implements MapDBQuery<List<Object[]>> {

	@Override
	public List<Object[]> execute(UserGroupCallback callback,
			Map<String, Object> params, TaskTableService tts,
			boolean singleResult) {
		@SuppressWarnings("unchecked")
		List<Long> taskIds = (List<Long>) params.get("taskIds");
		List<Status> status = Arrays.asList(Status.Created, Status.Ready, Status.Reserved, Status.InProgress, Status.Suspended);
		
		Set<Long> valuesByStatus = new HashSet<>();
		for (Status s : status) {
			MapDBQueryUtil.addAll(valuesByStatus, tts.getByStatus(), s.name());
		}
		
		valuesByStatus.retainAll(taskIds);
		
		List<Object[]> retval = new LinkedList<>();
		
		for (Long id : valuesByStatus) {
			if (tts.getById().containsKey(id)) {
				Task task = tts.getById().get(id);
				if (task != null) {
					for (OrganizationalEntity entity: task.getPeopleAssignments().getPotentialOwners()) {
						retval.add(new Object[] { id, entity.getId()});
					}
				}
			}
		}
		return retval;
	}
}
