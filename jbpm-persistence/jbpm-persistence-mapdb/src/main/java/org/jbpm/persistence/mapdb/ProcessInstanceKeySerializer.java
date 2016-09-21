package org.jbpm.persistence.mapdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.persistence.PersistentCorrelationKey;
import org.kie.internal.process.CorrelationProperty;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class ProcessInstanceKeySerializer extends GroupSerializerObjectArray<ProcessKey> {

	@Override
	public void serialize(DataOutput2 out, ProcessKey value) throws IOException {
		PersistentCorrelationKey key = (PersistentCorrelationKey) value.getCorrKey();
		Long processInstanceId = value.getProcessInstanceId();
		String[] types = value.getTypes();
		out.writeBoolean(processInstanceId != null);
		if (processInstanceId != null) {
			out.writeLong(processInstanceId);
		}
		out.writeBoolean(types != null);
		if (types != null) {
			out.writeInt(types.length);
			for (String type : types) {
				out.writeUTF(type);
			}
		}
		out.writeBoolean(key != null);
		if (key != null) {
			out.writeUTF(key.getName());
			out.writeLong(key.getId());
			out.writeLong(key.getProcessInstanceId());
			out.writeInt(key.getProperties() == null ? 0 : key.getProperties().size());
			if (key.getProperties() != null) {
				for (CorrelationProperty<?> prop : key.getProperties()) {
					out.writeUTF(prop.getName());
					out.writeUTF(prop.getType());
					out.writeUTF(prop.getValue() == null ? null : prop.getValue().toString());
				}
			}
		}
	}

	@Override
	public ProcessKey deserialize(DataInput2 input, int available) throws IOException {
		MapDBCorrelationKey corrKey = null;
		Long processInstanceId = null;
		String[] types = null;
		if (input.readBoolean()) {
			processInstanceId = input.readLong();
		}
		if (input.readBoolean()) {
			int size = input.readInt();
			Set<String> sTypes = new HashSet<>(size);
			for (int index = 0; index < size; index++) {
				sTypes.add(input.readUTF());
			}
			types = sTypes.toArray(new String[size]);
		}
		if (input.readBoolean()) {
			corrKey = new MapDBCorrelationKey();
			corrKey.setName(input.readUTF());
			corrKey.setId(input.readLong());
			corrKey.setProcessInstanceId(input.readLong());
			int size = input.readInt();
			List<CorrelationProperty<?>> properties = new ArrayList<>(size);
			for (int index = 0; index < size; index++) {
				MapDBCorrelationProperty prop = new MapDBCorrelationProperty();
				prop.setName(input.readUTF());
				prop.setType(input.readUTF());
				prop.setValue(input.readUTF());
				properties.add(prop);
			}
			corrKey.setProperties(properties);
		}
		return new ProcessKey(processInstanceId, types, corrKey);
	}

	@Override
	public int compare(ProcessKey tr1, ProcessKey tr2) {
		return tr1.compareTo(tr2);
	}

	@Override
	public ProcessKey nextValue(ProcessKey value) {
		return new ProcessKey((value == null || value.getProcessInstanceId() == null ? 0 : value.getProcessInstanceId()) + 1L, 
				value == null ? (String[]) null : value.getTypes(), value == null ? null : value.getCorrKey());
	}
}
