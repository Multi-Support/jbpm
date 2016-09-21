package org.jbpm.persistence.mapdb;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.jbpm.persistence.PersistentProcessInstance;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializerObjectArray;

public class PersistentProcessInstanceSerializer extends GroupSerializerObjectArray<PersistentProcessInstance> {

	@Override
	public void serialize(DataOutput2 out, PersistentProcessInstance value)
			throws IOException {
		MapDBProcessInstance inst = (MapDBProcessInstance) value;
		out.writeLong(inst.getId());
		out.writeBoolean(inst.getEventTypes() != null);
		if (inst.getEventTypes() != null) {
			out.writeInt(inst.getEventTypes().size());
			for (String type : inst.getEventTypes()) {
				out.writeUTF(type);
			}
		}
		out.writeLong(inst.getLastModificationDate() == null ? 0L : inst.getLastModificationDate().getTime());
		out.writeUTF(inst.getProcessId());
		out.writeLong(inst.getStartDate() == null ? 0L : inst.getStartDate().getTime());
		out.writeInt(inst.getState());
		out.writeBoolean(inst.getProcessInstanceByteArray() != null);
		if (inst.getProcessInstanceByteArray() != null) {
			out.writeInt(inst.getProcessInstanceByteArray().length);
			out.write(inst.getProcessInstanceByteArray());
		} else {
			throw new IOException("WRITING NULL BYTE ARRAY!");
		}
	}

	@Override
	public PersistentProcessInstance deserialize(DataInput2 input, int available)
			throws IOException {
		MapDBProcessInstance inst = new MapDBProcessInstance();
		inst.setId(input.readLong());
		if (input.readBoolean()) {
			int size = input.readInt();
			Set<String> eventTypes = new HashSet<String>();
			for (int index = 0; index < size; index++) {
				eventTypes.add(input.readUTF());
			}
			inst.setEventTypes(eventTypes);
		}
		inst.setLastModificationDate(new Date(input.readLong()));
		inst.setProcessId(input.readUTF());
		inst.setStartDate(new Date(input.readLong()));
		inst.setState(input.readInt());
		if (input.readBoolean()) {
			int size = input.readInt();
			byte[] procInstByteArray = new byte[size];
			input.readFully(procInstByteArray);
			inst.setProcessInstanceByteArray(procInstByteArray);
		} else { 
			throw new IOException("READING NULL BYTE ARRAY!");
		}
		return inst;
	}

	@Override
	public int compare(PersistentProcessInstance p1, PersistentProcessInstance p2) {
		return p1.getId().compareTo(p2.getId());
	}

}
