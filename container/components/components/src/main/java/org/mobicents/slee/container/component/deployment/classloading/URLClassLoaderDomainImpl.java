/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.slee.container.component.deployment.classloading;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.mobicents.slee.container.component.classloading.URLClassLoaderDomain;

/**
 * An extension of {@link URLClassLoader} to support multiple parents.
 * 
 * @author martins
 * 
 */
public class URLClassLoaderDomainImpl extends URLClassLoaderDomain {

	private static final Logger logger = Logger
			.getLogger(URLClassLoaderDomainImpl.class);

	/**
	 * the set of dependencies for the domain
	 */
	private Set<URLClassLoaderDomainImpl> directDependencies = new HashSet<URLClassLoaderDomainImpl>();

	/**
	 * 
	 * @param urls
	 * @param sleeClassLoader
	 */
	public URLClassLoaderDomainImpl(URL[] urls, ClassLoader sleeClassLoader) {
		super(urls, sleeClassLoader);
	}

	/**
	 * Adds a direct dependency to this domain. Direct dependencies are other
	 * domains which the domain depends on.
	 * 
	 * @param domain
	 */
	public void addDirectDependency(URLClassLoaderDomainImpl domain) {
		if (logger.isTraceEnabled())
			logger.trace(toString() + " adding domain " + domain
					+ " to direct dependencies");
		directDependencies.add(domain);
	}

	/**
	 * Retrieves the set of direct dependencies for the domain.
	 * 
	 * @return
	 */
	public Set<URLClassLoaderDomainImpl> getDirectDependencies() {
		return directDependencies;
	}

	/**
	 * Retrieves a flat list containing all dependencies for the domain, i.e., all direct dependencies and their own dependencies.
	 * @return
	 */
	public List<URLClassLoaderDomainImpl> getAllDependencies() {
		List<URLClassLoaderDomainImpl> result = new ArrayList<URLClassLoaderDomainImpl>();
		this.getAllDependencies(result);
		return result;
	}

	private void getAllDependencies(List<URLClassLoaderDomainImpl> result) {
		for (URLClassLoaderDomainImpl i : directDependencies) {
			if (!result.contains(i)) {
				result.add(i);
				i.getAllDependencies(result);
			}
		}
	}
	
	private static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

	private boolean acquireGlobalLock() {
		if (GLOBAL_LOCK.isHeldByCurrentThread()) {
			return false;
		}
		while (!GLOBAL_LOCK.tryLock()) {
			try {
				this.wait(10);
			} catch (InterruptedException e) {
				// ignore
			}
		}
		return true;
	}

	private void releaseGlobalLock() {
		GLOBAL_LOCK.unlock();
	}

	@Override
	protected Class<?> loadClass(final String name, final boolean resolve)
			throws ClassNotFoundException {

		if (logger.isTraceEnabled())
			logger.trace(toString() + " loadClass: " + name);

		synchronized (this) {
			final boolean acquiredLock = acquireGlobalLock();
			try {
				// load the class
				if (System.getSecurityManager() != null) {
					try {
						return AccessController
								.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
									public Class<?> run()
											throws ClassNotFoundException {
										return URLClassLoaderDomainImpl.super
												.loadClass(name, resolve);
									}
								});
					} catch (PrivilegedActionException e) {
						throw (ClassNotFoundException) e.getException();
					}
				} else {
					return super.loadClass(name, resolve);
				}
			} finally {
				if (acquiredLock) {
					releaseGlobalLock();
				}
			}
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (logger.isTraceEnabled())
			logger.trace(toString() + " findClass: " + name);
		// try 1st in dependencies
		Class<?> c = null;
		for (URLClassLoaderDomainImpl dependency : getAllDependencies()) {
			synchronized (dependency) {
				c = dependency.findLoadedClass(name);
				if (c != null) {
					return c;
				}
				try {
					return dependency.findClassLocallyLocked(name);
				} catch (Throwable e) {
					// ignore
				}
			}
		}
		// now locally
		return findClassLocallyLocked(name);
	}

	/**
	 * Finds a class locally, i.e., in the URLs managed by the extended
	 * URLClassLoader.
	 * 
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	protected Class<?> findClassLocally(String name)
			throws ClassNotFoundException {
		if (logger.isTraceEnabled())
			logger.trace(toString() + " findClassLocally: " + name);
		synchronized (this) {
			final boolean acquiredLock = acquireGlobalLock();
			try {
				return findClassLocallyLocked(name);
			} finally {
				if (acquiredLock) {
					releaseGlobalLock();
				}
			}
		}
	}
	
	protected Class<?> findClassLocallyLocked(String name)
			throws ClassNotFoundException {
		if (logger.isTraceEnabled())
			logger.trace(toString() + " findClassLocallyLocked: " + name);
		return super.findClass(name);		
	}

	@Override
	public URL findResource(String name) {
		if (logger.isTraceEnabled())
			logger.trace(toString() + " findResource: " + name);

		// try 1st in dependencies
		URL result = null;
		for (URLClassLoaderDomainImpl dependency : getAllDependencies()) {
			result = dependency.findResourceLocally(name);
			if (result != null) {
				return result;
			}
		}
		// now locally
		return findResourceLocally(name);
	}

	/**
	 * Finds a resource locally, i.e., in the URLs managed by the extended
	 * URLClassLoader.
	 * 
	 * @param name
	 * @return
	 */
	protected URL findResourceLocally(String name) {
		if (logger.isTraceEnabled())
			logger.trace(toString() + " findResourceLocally: " + name);

		return super.findResource(name);
	}

	@Override
	public Enumeration<URL> findResources(String name) throws IOException {
		if (logger.isTraceEnabled())
			logger.trace(toString() + " findResources: " + name);

		Vector<URL> vector = new Vector<URL>();
		// add resources from dependencies
		Enumeration<URL> enumeration = null;
		for (URLClassLoaderDomainImpl dependency : getAllDependencies()) {
			enumeration = dependency.findResourcesLocally(name);
			while (enumeration.hasMoreElements()) {
				vector.add(enumeration.nextElement());
			}
		}
		// now the local ones
		enumeration = findResourcesLocally(name);
		while (enumeration.hasMoreElements()) {
			vector.add(enumeration.nextElement());
		}
		return vector.elements();
	}

	/**
	 * Finds resources locally, i.e., in the URLs managed by the extended
	 * URLClassLoader.
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	protected Enumeration<URL> findResourcesLocally(String name)
			throws IOException {
		if (logger.isTraceEnabled())
			logger.trace(toString() + " findResourcesLocally: " + name);

		return super.findResources(name);
	}

	@Override
	public String toString() {
		return "URLClassLoaderDomain( urls= " + Arrays.asList(getURLs())
				+ " )\n";
	}

	public void clear() {
		directDependencies.clear();
	}

}