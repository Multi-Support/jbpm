package org.jbpm.services.task.persistence.index;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.mapdb.DB;

public class TaskTableService {

//	private BTreeMap<String, long[]> byStatus;
//	private BTreeMap<String, long[]> byActualOwner;
//	private BTreeMap<String, long[]> byPotentialOwner;
//	private BTreeMap<String, long[]> byExclOwner;
//	private BTreeMap<String, long[]> byBizAdmin;
//	private BTreeMap<Long, Task> byId;
//	private BTreeMap<Long, long[]> byContentId;
	private static final ConcurrentHashMap<String, long[]> byStatus = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, long[]> byActualOwner = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, long[]> byPotentialOwner = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, long[]> byExclOwner = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, long[]> byBizAdmin = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Long, Task> byId = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Long, long[]> byContentId = new ConcurrentHashMap<>();
	

	public TaskTableService(DB db) {
//		this.byStatus = db.treeMap("taskByStatus", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
//		this.byActualOwner = db.treeMap("taskByStatus", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
//		this.byPotentialOwner = db.treeMap("taskByPotOwner", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
//		this.byExclOwner = db.treeMap("taskByExclOwner", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
//		this.byBizAdmin = db.treeMap("taskByBizAdmin", Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
//		this.byId = db.treeMap("taskById", new SerializerLong(), new TaskSerializer()).createOrOpen();
//		this.byContentId = db.treeMap("taskByContentId", new SerializerLong(), Serializer.LONG_ARRAY).createOrOpen();
	}
	
	public void update(Task task) {
		Long taskId = task.getId();
		clearMappings(taskId);
		byId.put(task.getId(), task);
		String status = task.getTaskData().getStatus().name();
		updateEntry(status, byStatus, taskId);
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
		if (task.getTaskData().getDocumentContentId() >= 0) {
			updateEntry(task.getTaskData().getDocumentContentId(), byContentId, taskId);
		}
		if (task.getTaskData().getFaultContentId() >= 0) {
			updateEntry(task.getTaskData().getFaultContentId(), byContentId, taskId);
		}
		if (task.getTaskData().getOutputContentId() >= 0) {
			updateEntry(task.getTaskData().getOutputContentId(), byContentId, taskId);
		}
	}

	private <T> void updateEntry(T key, Map<T, long[]> map, Long taskId) {
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
				byStatus.put(status, removeId(taskId, byStatus.get(status)));
			}
			for (Object ow : byActualOwner.keySet()) {
				String ownerId = (String) ow;
				byActualOwner.put(ownerId, removeId(taskId, byActualOwner.get(ownerId)));
			}
			for (Object oid : byPotentialOwner.keySet()) {
				String id = (String) oid;
				byPotentialOwner.put(id, removeId(taskId, byPotentialOwner.get(id)));
			}
			for (Object oid : byExclOwner.keySet()) {
				String id = (String) oid;
				byExclOwner.put(id, removeId(taskId, byExclOwner.get(id)));
			}
			for (Object oid : byBizAdmin.keySet()) {
				String id = (String) oid;
				byBizAdmin.put(id, removeId(taskId, byBizAdmin.get(id)));
			}
			for (Object oid : byContentId.keySet()) {
				Long id = (Long) oid;
				byContentId.put(id, removeId(taskId, byContentId.get(id)));
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

	public void remove(Long taskId) {
		clearMappings(taskId);
	}
}
