package org.jbpm.persistence.mapdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import org.drools.persistence.TransactionManager;
import org.drools.persistence.TransactionManagerHelper;
import org.drools.persistence.mapdb.MapDBPersistenceContext;
import org.jbpm.persistence.PersistentCorrelationKey;
import org.jbpm.persistence.PersistentProcessInstance;
import org.jbpm.persistence.ProcessPersistenceContext;
import org.kie.internal.process.CorrelationKey;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBException;
import org.mapdb.Serializer;

public class MapDBProcessPersistenceContext  extends MapDBPersistenceContext
	implements ProcessPersistenceContext{

	private final AtomicLong nextId;
	private final BTreeMap<String, long[]> mapByEventTypes;
	private final BTreeMap<Long, PersistentProcessInstance> mapById;
	private final BTreeMap<PersistentCorrelationKey, Long> mapByCK;
	
	public MapDBProcessPersistenceContext(DB db, TransactionManager txm) {
		super(db, txm);
		String keyPrefix = new MapDBProcessInstance().getMapKey();
		this.mapById = db.treeMap(keyPrefix + "ById", 
				Serializer.LONG, new PersistentProcessInstanceSerializer()).createOrOpen();
		this.mapByEventTypes = db.treeMap(keyPrefix + "ByEventTypes",
				Serializer.STRING, Serializer.LONG_ARRAY).createOrOpen();
		this.mapByCK = db.treeMap(keyPrefix + "ByCK", 
				new PersistentCorrelationKeySerializer(), Serializer.LONG).createOrOpen();
		Long lastId = null;
		try {
			lastId = this.mapById.lastKey() == null ? 0L : this.mapById.lastKey();
		} catch (NoSuchElementException | DBException.GetVoid t) { 
			lastId = 0L;
		}
		if (lastId == null) {
			lastId = 0L;
		}
		nextId = new AtomicLong(lastId + 1L);
	}
	
	@Override
	public PersistentProcessInstance persist(PersistentProcessInstance processInstanceInfo) {
		long id = -1;
		processInstanceInfo.transform();
		if (processInstanceInfo.getId() == null || processInstanceInfo.getId() == -1) {
			id = nextId.incrementAndGet();
			processInstanceInfo.setId(id);
		} else {
			id = processInstanceInfo.getId();
		}
		mapById.put(processInstanceInfo.getId(), processInstanceInfo);
		if (processInstanceInfo.getEventTypes() != null) {
			for (String eventType : processInstanceInfo.getEventTypes()) {
				long[] ids = new long[] { processInstanceInfo.getId() };
				if (mapByEventTypes.containsKey(eventType)) {
					long[] otherIds = mapByEventTypes.get(eventType);
					ids = Arrays.copyOf(otherIds, otherIds.length + 1);
					ids[ids.length - 1] = processInstanceInfo.getId();
				}
				mapByEventTypes.put(eventType, ids);
			}
		}
		TransactionManagerHelper.addToUpdatableSet(txm, processInstanceInfo);
		return processInstanceInfo;
	}

	@Override
	public PersistentCorrelationKey persist(PersistentCorrelationKey correlationKeyInfo) {
		long processInstanceId = correlationKeyInfo.getProcessInstanceId();
		mapByCK.put(correlationKeyInfo, processInstanceId);
		return correlationKeyInfo;
	}

	@Override
	public PersistentProcessInstance findProcessInstanceInfo(Long processId) {
		try {
			if (!mapById.containsKey(processId)) {
				return null;
			}
			PersistentProcessInstance inst = mapById.get(processId);
			if (((MapDBProcessInstance)inst).isDeleted()) {
				return null;
			}
			TransactionManagerHelper.addToUpdatableSet(txm, inst);
			return inst;
		} catch (Throwable t) {
			return null;
		}
	}

	@Override
	public void remove(PersistentProcessInstance processInstanceInfo) {
		TransactionManagerHelper.removeFromUpdatableSet(txm, processInstanceInfo);
		//((MapDBProcessInstance) processInstanceInfo).setDeleted(true);
		mapById.remove(processInstanceInfo.getId());//, processInstanceInfo);
		//TransactionManagerHelper.removeFromUpdatableSet(txm, processInstanceInfo);
	}

	@Override
	public List<Long> getProcessInstancesWaitingForEvent(String type) {
		if (!mapByEventTypes.containsKey(type)) {
			return new ArrayList<>();
		}
		long[] values = mapByEventTypes.get(type);
		List<Long> retval = new ArrayList<>();
		for (long value : values) {
			retval.add(value);
		}
		return retval;
	}

	@Override
	public void close() {
		super.close();
		//map.close();
	}
	
	@Override
	public Long getProcessInstanceByCorrelationKey(CorrelationKey correlationKey) {
		return mapByCK.getOrDefault(correlationKey, null);
	}
}
