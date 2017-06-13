package org.jbpm.test.functional.properties;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

public class TestClassLoader extends ClassLoader {

	private int getResourcesCallsCount = 0;

	public TestClassLoader() {
		super();
	}

	public TestClassLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		getResourcesCallsCount++;
		return super.getResources(name);
	}

	public int getResourceCallCount() {
		return getResourcesCallsCount;
	}
}
