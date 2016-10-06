package org.jbpm.services.task.persistence.index;

import java.util.Arrays;
import java.util.Map;

import org.jbpm.services.task.persistence.TaskSerializer;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

public class TaskTableService {

	private static HTreeMap<String, long[]> byStatus = null;
	private static HTreeMap<String, long[]> byActualOwner = null;
	private static HTreeMap<String, long[]> byPotentialOwner = null;
	private static HTreeMap<String, long[]> byRecipient = null;
	private static HTreeMap<String, long[]> byStakeholder = null;
	private static HTreeMap<String, long[]> byInitiator = null;
	private static HTreeMap<String, long[]> byExclOwner = null;
	private static HTreeMap<String, long[]> byBizAdmin = null;
	private static HTreeMap<Long, long[]> byContentId = null;
	private static HTreeMap<Long, long[]> byProcessInstanceId = null;
	private static HTreeMap<Long, Task> byId = null;
	
	private static synchronized void init(DB db) {
		if (byId == null || byId.isClosed()) {
			byStatus = db.hashMap("taskByStatus", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
			byActualOwner = db.hashMap("taskByStatus", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
			byPotentialOwner = db.hashMap("taskByPotOwner", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
			byRecipient = db.hashMap("taskByRecipient", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
			byStakeholder = db.hashMap("taskByStakeholder", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
			byInitiator = db.hashMap("taskByInitiator", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
			byExclOwner = db.hashMap("taskByExclOwner", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
			byBizAdmin = db.hashMap("taskByBizAdmin", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
			byId = db.hashMap("taskById", Serializer.LONG, new TaskSerializer()).createOrOpen();
			byContentId = db.hashMap("taskByContentId", Serializer.LONG, Serializer.LONG_ARRAY).createOrOpen();
			byProcessInstanceId = db.hashMap("byProcessInstanceId", Serializer.LONG, Serializer.LONG_ARRAY).createOrOpen();
		}
	}

	public TaskTableService(DB db) {
		init(db);
	}
	
	public void update(Task task) {
		Long taskId = task.getId();
		clearMappings(taskId);
		byId.put(task.getId(), task);
		String status = task.getTaskData().getStatus().name();
		updateEntry(status, byStatus, taskId);
		if (task.getPeopleAssignments().getTaskInitiator() != null) {
			updateEntry(task.getPeopleAssignments().getTaskInitiator().getId(), byInitiator, taskId);
		}
		if (task.getTaskData().getActualOwner() != null) {
			updateEntry(task.getTaskData().getActualOwner().getId(), byActualOwner, taskId);
		}
		for (OrganizationalEntity entity : task.getPeopleAssignments().getPotentialOwners()) {
			updateEntry(entity.getId(), byPotentialOwner, taskId);
		}
		for (OrganizationalEntity entity : ((InternalPeopleAssignments) task.getPeopleAssignments()).getExcludedOwners()) {
			updateEntry(entity.getId(), byExclOwner, taskId);
		}
		for (OrganizationalEntity entity : task.getPeopleAssignments().getBusinessAdministrators()) {
			updateEntry(entity.getId(), byBizAdmin, taskId);
		}
		for (OrganizationalEntity entity : ((InternalPeopleAssignments) task.getPeopleAssignments()).getRecipients()) {
			updateEntry(entity.getId(), byRecipient, taskId);
		}
		for (OrganizationalEntity entity : ((InternalPeopleAssignments) task.getPeopleAssignments()).getTaskStakeholders()) {
			updateEntry(entity.getId(), byStakeholder, taskId);
		}
		if (task.getTaskData().getDocumentContentId() >= 0) {
			updateEntry(task.getTaskData().getDocumentContentId(), byContentId, taskId);
		}
		if (task.getTaskData().getFaultContentId() >= 0) {
			updateEntry(task.getTaskData().getFaultContentId(), byContentId, taskId);
		}
		if (task.getTaskData().getOutputContentId() >= 0) {
			updateEntry(task.getTaskData().getOutputContentId(), byContentId, taskId);
		}
		if (task.getTaskData().getProcessInstanceId() >= 0) {
			updateEntry(task.getTaskData().getProcessInstanceId(), byProcessInstanceId, taskId);
		}
	}

	private <T> void updateEntry(T key, HTreeMap<T, long[]> map, Long taskId) {
		long[] values = map.get(key);
		if (values == null) {
			values = new long[1];
		} else {
			values = Arrays.copyOf(values, values.length + 1);
		}
		values[values.length - 1] = taskId;
		map.put(key, values);
	}

	private void clearMappings(Long taskId) {
		synchronized (byId) {
			byId.remove(taskId);
			for (Object stat : byStatus.keySet()) {
				String status = (String) stat;
				byStatus.replace(status, removeId(taskId, byStatus.get(status)));
			}
			for (Object ow : byActualOwner.keySet()) {
				String ownerId = (String) ow;
				byActualOwner.replace(ownerId, removeId(taskId, byActualOwner.get(ownerId)));
			}
			for (Object ow : byRecipient.keySet()) {
				String ownerId = (String) ow;
				byRecipient.replace(ownerId, removeId(taskId, byRecipient.get(ownerId)));
			}
			for (Object oid : byPotentialOwner.keySet()) {
				String id = (String) oid;
				byPotentialOwner.replace(id, removeId(taskId, byPotentialOwner.get(id)));
			}
			for (Object oid : byExclOwner.keySet()) {
				String id = (String) oid;
				byExclOwner.replace(id, removeId(taskId, byExclOwner.get(id)));
			}
			for (Object oid : byStakeholder.keySet()) {
				String id = (String) oid;
				byStakeholder.replace(id, removeId(taskId, byStakeholder.get(id)));
			}
			for (Object oid : byBizAdmin.keySet()) {
				String id = (String) oid;
				byBizAdmin.replace(id, removeId(taskId, byBizAdmin.get(id)));
			}
			for (Object oid : byInitiator.keySet()) {
				String id = (String) oid;
				byInitiator.replace(id, removeId(taskId, byInitiator.get(id)));
			}
			for (Object oid : byContentId.keySet()) {
				Long id = (Long) oid;
				byContentId.replace(id, removeId(taskId, byContentId.get(id)));
			}
			for (Object oid : byProcessInstanceId.keySet()) {
				Long id = (Long) oid;
				byProcessInstanceId.replace(id, removeId(taskId, byProcessInstanceId.get(id)));
			}
		}
	}

	private long[] removeId(long toRemove, long[] origin) {
		if (origin == null) {
			return new long[0];
		}
		boolean hasElement = false;
		for (long elem : origin) {
			if (toRemove == elem) {
				hasElement = true;
				break;
			}
		}
		if (!hasElement) {
			return origin;
		}
		long[] retval = new long[origin.length -1];
		int idx = 0;
		for (int index = 0; index < origin.length; index++) {
			if (origin[index] != toRemove) {
				retval[idx] = origin[index];
				idx++;
			}
		}
		return retval;
	}

	public Map<String, long[]> getByActualOwner() {
		return byActualOwner;
	}
	
	public Map<String, long[]> getByBizAdmin() {
		return byBizAdmin;
	}
	
	public Map<String, long[]> getByExclOwner() {
		return byExclOwner;
	}
	
	public Map<String, long[]> getByPotentialOwner() {
		return byPotentialOwner;
	}
	
	public Map<String, long[]> getByStatus() {
		return byStatus;
	}
	
	public Map<Long, Task> getById() {
		return byId;
	}

	public Map<Long, long[]> getByContentId() {
		return byContentId;
	}

	public Map<Long, long[]> getByProcessInstanceId() {
		return byProcessInstanceId;
	}
	
	public Map<String, long[]> getByRecipient() {
		return byRecipient;
	}

	public Map<String, long[]> getByStakeholder() {
		return byStakeholder;
	}

	public Map<String, long[]> getByInitiator() {
		return byInitiator;
	}
	
	public void remove(Long taskId) {
		clearMappings(taskId);
	}
	
	public void addTaskContentRelation(Task task, long contentId) {
		updateEntry(contentId, byContentId, task.getId());
	}

	public void removeTaskContentRelation(Task task, long contentId) {
		long[] original = byContentId.get(contentId);
		byContentId.put(contentId, removeId(task.getId(), original));
	}
}
