package org.jbpm.test.functional.properties;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.drools.core.SessionConfigurationImpl;
import org.junit.Assert;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.utils.ChainedProperties;

public class ProcessTestRunnable implements Runnable {

	private String deploymentId;
	private String processId;
	private String userId;
	private int amountOfTasks;
	private ChainedProperties chainedProperties = null;
	private long processInstanceId = -1;
	
	public ProcessTestRunnable(String deploymentId, String processId,
			String userId, int amountOfTasks) {
		this.deploymentId = deploymentId;
		this.processId = processId;
		this.userId = userId;
		this.amountOfTasks = amountOfTasks;
	}
	
	public ChainedProperties getChainedProperties() {
		return chainedProperties;
	}

	@Override
	public void run() {
		RuntimeManager manager = RuntimeManagerRegistry.get().getManager(deploymentId);
		Assert.assertNotNull(manager);
		RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
		Assert.assertNotNull(runtime);
		setChainedProperties(runtime);
		ProcessInstance processInstance = runtime.getKieSession().startProcess(processId);
		Assert.assertNotNull(processInstance);
		this.processInstanceId = processInstance.getId();
		System.out.println("Started process " + processInstanceId);
		List<Status> status = Arrays.asList(Status.Ready, Status.Reserved);
		for (int index = 0; index < amountOfTasks; index++) {
			System.out.println("Reading task " + index + " for process " + processInstanceId);
			List<TaskSummary> tasks = runtime.getTaskService().
					getTasksByStatusByProcessInstanceId(processInstance.getId(), status, userId);
			Assert.assertNotNull(tasks);
			Assert.assertEquals(1, tasks.size());
			TaskSummary task = tasks.iterator().next();
			Assert.assertNotNull(task);
			Long taskId = task.getId();
			Assert.assertNotNull(taskId);
			if (task.getStatus() == Status.Ready) {
				System.out.println("Claiming task " + index + " for process " + processInstanceId);
				runtime.getTaskService().claim(taskId, userId);
			}
			System.out.println("Starting task " + index + " for process " + processInstanceId);
			runtime.getTaskService().start(taskId, userId);
			System.out.println("Completing task " + index + " for process " + processInstanceId);
			runtime.getTaskService().complete(taskId, userId, new HashMap<>());
			System.out.println("Completed task " + taskId + " of process " + processInstanceId);
		}
		ProcessInstanceLog log = runtime.getAuditService().findProcessInstance(processInstance.getId());
		Assert.assertNotNull(log);
		Assert.assertEquals(ProcessInstance.STATE_COMPLETED, log.getStatus().intValue());
		manager.disposeRuntimeEngine(runtime);
		System.out.println("Process " + processInstanceId + " completed");
	}

	private void setChainedProperties(RuntimeEngine runtime) {
		SessionConfigurationImpl sessionConf = (SessionConfigurationImpl) 
				runtime.getKieSession().getSessionConfiguration();
		Assert.assertNotNull(sessionConf);
		try {
			Field f = sessionConf.getClass().getDeclaredField("chainedProperties");
			f.setAccessible(true);
			this.chainedProperties = (ChainedProperties) f.get(sessionConf);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Integer getChainedPropertiesHashCodeValue() {
		ChainedProperties cp = getChainedProperties();
		try {
			Class<?> clz = cp.getClass();
			Field f1 = clz.getDeclaredField("props");
			Field f2 = clz.getDeclaredField("defaultProps");
			f1.setAccessible(true);
			f2.setAccessible(true);
			Object p1 = f1.get(cp);
			Object p2 = f2.get(cp);
			return p1.hashCode() * 10 + p2.hashCode();
		} catch (Exception e) {
			return -1;
		}
	}
	
	public long getProcessInstanceId() {
		return processInstanceId;
	}
}
