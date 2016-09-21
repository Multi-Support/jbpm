package org.jbpm.persistence.mapdb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.atomic.AtomicLong;

import org.drools.persistence.mapdb.MapDBPersistenceContext;
import org.jbpm.persistence.PersistentCorrelationKey;
import org.jbpm.persistence.PersistentProcessInstance;
import org.jbpm.persistence.ProcessPersistenceContext;
import org.kie.internal.process.CorrelationKey;
import org.mapdb.BTreeMap;
import org.mapdb.DB;

public class MapDBProcessPersistenceContext  extends MapDBPersistenceContext
	implements ProcessPersistenceContext{

	private final AtomicLong nextId;
	private final BTreeMap<ProcessKey, PersistentProcessInstance> map;
	
	public MapDBProcessPersistenceContext(DB db) {
		super(db);
		this.map = db.treeMap("processInstance", 
				new ProcessInstanceKeySerializer(), 
				new PersistentProcessInstanceSerializer()).
				createOrOpen();
		nextId = new AtomicLong(this.map.size() + 1L);
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
		ProcessKey key = new ProcessKey(processInstanceInfo.getId(), processInstanceInfo.getEventTypes(), null);
		map.put(key, processInstanceInfo);
		return processInstanceInfo;
	}

	@Override
	public PersistentCorrelationKey persist(PersistentCorrelationKey correlationKeyInfo) {
		long processInstanceId = correlationKeyInfo.getProcessInstanceId();
		NavigableMap<ProcessKey, PersistentProcessInstance> navMap = getSearchMapById(processInstanceId);
		if (!navMap.isEmpty()) {
			Entry<ProcessKey, PersistentProcessInstance> entry = navMap.entrySet().iterator().next();
			ProcessKey oldKey = entry.getKey();
			ProcessKey newKey = new ProcessKey(processInstanceId, oldKey.getTypes(), correlationKeyInfo);
			PersistentProcessInstance value = entry.getValue();
			map.remove(oldKey);
			map.put(newKey, value);
		}
		return correlationKeyInfo;
	}

	@Override
	public PersistentProcessInstance findProcessInstanceInfo(Long processId) {
		NavigableMap<ProcessKey, PersistentProcessInstance> navMap = getSearchMapById(processId);
		if (navMap.isEmpty()) {
			return null;
		}
		Entry<ProcessKey, PersistentProcessInstance> entry = navMap.entrySet().iterator().next();
		if (entry == null) {
			return null;
		}
		if (((MapDBProcessInstance) entry.getValue()).isDeleted()) {
			return null;
		}
		return entry.getValue();
	}

	@Override
	public void remove(PersistentProcessInstance processInstanceInfo) {
		NavigableMap<ProcessKey, PersistentProcessInstance> navMap =
				getSearchMapById(processInstanceInfo.getId());
		if (!navMap.isEmpty()) {
			Entry<ProcessKey, PersistentProcessInstance> entry = navMap.entrySet().iterator().next();
			((MapDBProcessInstance) entry.getValue()).setDeleted(true);
			map.put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public List<Long> getProcessInstancesWaitingForEvent(String type) {
		NavigableMap<ProcessKey, PersistentProcessInstance> navMap = getSearchMapByEventType(type); 
		List<Long> retval = new ArrayList<>(navMap.size());
		for (ProcessKey key : navMap.keySet()) {
			if (!((MapDBProcessInstance) navMap.get(key)).isDeleted()) {
				retval.add(key.getProcessInstanceId());
			}
		}
		return retval;
	}

	@Override
	public void close() {
		super.close();
		map.close();
	}
	
	@Override
	public Long getProcessInstanceByCorrelationKey(CorrelationKey correlationKey) {
		NavigableMap<ProcessKey, PersistentProcessInstance> navMap = getSearchMapByCK(correlationKey);
		if (navMap.isEmpty()) {
			return null;
		}
		Entry<ProcessKey, PersistentProcessInstance> entry = navMap.entrySet().iterator().next();
		if (!((MapDBProcessInstance) entry.getValue()).isDeleted()) {
			entry.getValue().getId();
		}
		return null;
	}
	
	protected NavigableMap<ProcessKey, PersistentProcessInstance> getSearchMapByEventType(String type) {
		ProcessKey fromKey = new ProcessKey(Long.MIN_VALUE, new String[] { type }, null);
		ProcessKey toKey = new ProcessKey(Long.MAX_VALUE, new String[] { type }, null);
		return map.subMap(fromKey, toKey);
	}

	protected NavigableMap<ProcessKey, PersistentProcessInstance> getSearchMapById(Long id) {
		return map.prefixSubMap(new ProcessKey(id, (String[]) null, null));
	}
	
	protected NavigableMap<ProcessKey, PersistentProcessInstance> getSearchMapByCK(CorrelationKey key) {
		return map.subMap(new ProcessKey(Long.MIN_VALUE, new String[0], key), 
				new ProcessKey(Long.MAX_VALUE, new String[0], key));
	}
}
