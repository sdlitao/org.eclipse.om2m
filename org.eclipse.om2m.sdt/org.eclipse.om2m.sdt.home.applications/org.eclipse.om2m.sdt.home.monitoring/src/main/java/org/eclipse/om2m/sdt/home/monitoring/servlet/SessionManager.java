/*******************************************************************************
 * Copyright (c) 2014, 2016 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.om2m.sdt.home.monitoring.servlet;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
	
	public class Session {
		
		private final String name;
		private final String password;
		private final String id;
		
		public Session(final String pName, final String pPassword) {
			this.name = pName;
			this.id = UUID.randomUUID().toString();
			this.password = pPassword;
		}
		
		public String getName() {
			return name;
		}

		public String getPassword() {
			return password;
		}

		public String getId() {
			return id;
		}
		
	}
	
	public static final String SESSION_ID_PARAMETER = "sessionId";
	
	private static final SessionManager INSTANCE = new SessionManager();
	
	private final Map<String/* session id */, Session> openedSessions;

	public static SessionManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Make private default constructor
	 */
	private SessionManager() {
		openedSessions = new HashMap<String, Session>();
	}
	
	public Session createNewSession(String name, String password) {
		Session session = new Session(name, password);
		synchronized (openedSessions) {
			openedSessions.put(session.getId(), session);
		}
		return session;
	}

	public Session removeSession(String token) {
		synchronized (openedSessions) {
			return openedSessions.remove(token);
		}
	}
	
	public boolean checkTokenExists(String token) {
		if (token == null)
			return false;
		synchronized (openedSessions) {
			return openedSessions.containsKey(token);
		}
	}
	
	public Session getSession(String sessionId) {
		if (sessionId == null)
			return null;
		synchronized (openedSessions) {
			return openedSessions.get(sessionId);
		}
	}
	
}
