/*******************************************************************************
 * Copyright (c) 2014, 2017 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.om2m.datamapping.jaxb;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;

import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.resource.Group;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MapperTestGroup extends AbstractMapperTest {
	
	private Mapper xmlMapper;
	private Mapper jsonMapper;

	@Before
	public void setUp() throws Exception {
		xmlMapper = new Mapper(MimeMediaType.XML);
		jsonMapper = new Mapper(MimeMediaType.JSON);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testXMLObjToString() {
		Group group = new Group();
		group.setMemberType(BigInteger.valueOf(ResourceType.AE));
		group.getMemberIDs().add("id1");
		group.getMemberIDs().add("id2");
		
		String xmlString = xmlMapper.objToString(group);
		System.out.println(xmlString);
	}
	
	@Test
	public void testXMLStringToObj() {
		String xmlPayload = readFile("src/test/resources/group.xml");
		
		Group group = (Group) xmlMapper.stringToObj(xmlPayload);
		
		assertTrue(group != null);
		assertTrue(BigInteger.valueOf(2).equals(group.getMemberType()));
		assertTrue(!group.getMemberIDs().isEmpty());
		assertTrue(group.getMemberIDs().contains("id1"));
		assertTrue(group.getMemberIDs().contains("id2"));
	}
	
	@Test
	public void testJSONStringToObj() {
		String jsonPayload = readFile("src/test/resources/group.json");
		
		Group group = (Group) jsonMapper.stringToObj(jsonPayload);
		
		assertTrue(group != null);
		assertTrue(BigInteger.valueOf(2).equals(group.getMemberType()));
		assertTrue(!group.getMemberIDs().isEmpty());
		assertTrue(group.getMemberIDs().contains("id1"));
		assertTrue(group.getMemberIDs().contains("id2"));
	}
	
	@Test
	public void testJSONObjToString() {
		Group group = new Group();
		group.setMemberType(BigInteger.valueOf(ResourceType.AE));
		group.getMemberIDs().add("id1");
		group.getMemberIDs().add("id2");
		
		String jsonString = jsonMapper.objToString(group);
		System.out.println(jsonString);
		
	}
	

}
