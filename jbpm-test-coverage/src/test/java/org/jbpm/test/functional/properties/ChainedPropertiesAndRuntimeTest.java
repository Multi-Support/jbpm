package org.jbpm.test.functional.properties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;

import javax.persistence.Persistence;

import org.jbpm.persistence.util.PersistenceUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.TaskModelProvider;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class ChainedPropertiesAndRuntimeTest {

	private static final String DEPLOYMENT_ID = "ppi-test";
	private static final String PROCESS_ID = "org.jbpm.test.functional.properties.TestProcess";
	private static final String PROCESS_PATH = "org/jbpm/test/functional/properties/TestProcess.bpmn";
	private static final int SIZE = 300;
	
	private PoolingDataSource ds1;
	
	@Before
	public void setUp() {
        Properties dsProps = PersistenceUtil.getDatasourceProperties();
        String driverClass = dsProps.getProperty("driverClassName");
        // Setup the datasource
        this.ds1 = PersistenceUtil.setupPoolingDataSource(dsProps, "jdbc/jbpm-ds", false);
        if (driverClass.startsWith("org.h2")) {
            ds1.getDriverProperties().setProperty("url", "jdbc:h2:mem:test;MVCC=true;LOCK_TIMEOUT=10000");
        }
        ds1.setMaxPoolSize(1000);
        ds1.setAllowLocalTransactions(true);
        ds1.setMaxIdleTime(3600);
        ds1.init();
	}
	
	@After
	public void tearDown() {
		if (ds1 != null) {
			ds1.close();
			ds1 = null;
		}
	}
	
	@Test
	public void testChainedPropertiesOnProcessReuse() throws Exception {
		KieServices ks = KieServices.Factory.get();
		KieFileSystem kfs = ks.newKieFileSystem();
		kfs.write(ResourceFactory.newClassPathResource(PROCESS_PATH));
		TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
		KieBuilder kbuilder = ks.newKieBuilder(kfs);
		kbuilder.buildAll();
		KieContainer kcontainer = ks.newKieContainer(kbuilder.getKieModule().getReleaseId());
		KieBase kbase = kcontainer.getKieBase();
		RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
				.newDefaultBuilder()
				.entityManagerFactory(Persistence.createEntityManagerFactory("org.jbpm.test.persistence"))
				.knowledgeBase(kbase)
				.classLoader(cl)
				.get();
		RuntimeManager manager = RuntimeManagerFactory.Factory.get()
				.newPerProcessInstanceRuntimeManager(environment, 
						DEPLOYMENT_ID);
		Assert.assertNotNull(manager);
		RuntimeEngine firstEngine = manager.getRuntimeEngine(ProcessInstanceIdContext.get());
		Assert.assertNotNull(firstEngine);
		InternalTaskService taskService = (InternalTaskService) firstEngine.getTaskService();
		taskService.addUser(TaskModelProvider.getFactory().newUser("john"));
		taskService.addUser(TaskModelProvider.getFactory().newUser("Administrator"));
		taskService.addGroup(TaskModelProvider.getFactory().newGroup("Administrators"));
		taskService.addGroup(TaskModelProvider.getFactory().newGroup("Knights Templer"));
		taskService.addGroup(TaskModelProvider.getFactory().newGroup("Crusaders"));
		List<ProcessTestRunnable> runs = new ArrayList<>(SIZE);
		Thread.sleep(10000);
		System.out.println("running one more cycle...");
		List<Thread> threads = new ArrayList<>(SIZE);
		for (int index = 0; index < SIZE; index++) {
			ProcessTestRunnable r = new ProcessTestRunnable(
					DEPLOYMENT_ID, PROCESS_ID, "john", 3);
			runs.add(r);
		}
		for (Runnable r : runs) {
			Thread t = createThreadOrWait(r);
			threads.add(t);
			t.start();
		}
		for (Thread t : threads) {
			t.join();
		}
		Set<Integer> chainedPropertiesSet = new HashSet<>();
		for (ProcessTestRunnable r : runs) {
			chainedPropertiesSet.add(r.getChainedPropertiesHashCodeValue());
		}
		//since we are only using one classloader, all ChainedProperties should be equal
		Assert.assertEquals(1, chainedPropertiesSet.size());
		Assert.assertEquals(2, cl.getResourceCallCount());
	}

	private Thread createThreadOrWait(Runnable r) throws InterruptedException {
		Thread t = null;
		while (t == null) {
			t = Executors.defaultThreadFactory().newThread(r);
			if (t == null) {
				Thread.sleep(50);
			}
		}
		return t;
	}
}
