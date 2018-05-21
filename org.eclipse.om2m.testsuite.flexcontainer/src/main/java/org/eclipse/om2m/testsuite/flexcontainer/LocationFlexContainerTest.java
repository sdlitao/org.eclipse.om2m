/*******************************************************************************
 * Copyright (c) 2014, 2016 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.om2m.testsuite.flexcontainer;

import java.math.BigInteger;

import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.constants.Operation;
import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.CustomAttribute;
import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.commons.resource.flexcontainerspec.BinarySwitchFlexContainer;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.testsuite.flexcontainer.TestReport.Status;

public class LocationFlexContainerTest extends FlexContainerTestSuite {

	@Override
	protected String getTestSuiteName() {
		return "LocationFlexContainerTest";
	}

	public LocationFlexContainerTest(final CseService pCseService) {
		super(pCseService);
	}

	public void testUnderCseBase() {
		
		genericTest("/" + Constants.CSE_ID + "/" + Constants.CSE_NAME, "testUnderCseBase");

	}

	public void testUnderRemoteCSE() {
		genericTest("/" + Constants.REMOTE_CSE_ID + "/" + Constants.REMOTE_CSE_NAME, "testUnderRemoteCSE");
	}

	public void testUnderFlexContainer() {
		
		// create a FlexContainer
		BinarySwitchFlexContainer parentFlexContainer = new BinarySwitchFlexContainer();
		String parentFlexContainerName = "parentFlexContainer_" + System.currentTimeMillis();
		parentFlexContainer.setName(parentFlexContainerName);
		CustomAttribute ca = new CustomAttribute();
		ca.setCustomAttributeName("powSe");
		ca.setCustomAttributeValue("true");
		parentFlexContainer.getCustomAttributes().add(ca);
		
		
		sendCreateFlexContainerRequest(parentFlexContainer, "/" + Constants.CSE_ID + "/" + Constants.CSE_NAME, Constants.ADMIN_REQUESTING_ENTITY);
		
		genericTest("/" + Constants.CSE_ID + "/" + Constants.CSE_NAME + "/" + parentFlexContainerName, "testUnderFlexContainer");
	
	}

	public void testUnderContainer() {
		
		String parentContainerName = "parentContainerName_" + System.currentTimeMillis();
		
		// Container
		Container container = new Container();
		container.setOntologyRef("OrangeOntology");
		container.setName(parentContainerName);

		String baseParentContainerLocation = "/" + Constants.CSE_ID + "/" + Constants.CSE_NAME;
		
		
		
		RequestPrimitive request = new RequestPrimitive();
		request.setContent(container);
		request.setFrom(Constants.ADMIN_REQUESTING_ENTITY);
		request.setTo(baseParentContainerLocation);
		request.setResourceType(BigInteger.valueOf(ResourceType.CONTAINER));
		request.setRequestContentType(MimeMediaType.OBJ);
		request.setReturnContentType(MimeMediaType.OBJ);
		request.setOperation(Operation.CREATE);
		ResponsePrimitive response = getCseService().doRequest(request);
		
		// we suppose here the container is created
		genericTest(baseParentContainerLocation + "/" + parentContainerName, "testUnderContainer");

	}

	private void genericTest(String location, String methodName) {

		// set a new flexContainer
		BinarySwitchFlexContainer flexContainer = new BinarySwitchFlexContainer();
		CustomAttribute ca = new CustomAttribute();
		ca.setCustomAttributeName("powSe");
		ca.setCustomAttributeValue("true");
		flexContainer.getCustomAttributes().add(ca);

		String flexContainerName = "FLEXCONTAINER_" + System.currentTimeMillis();
		flexContainer.setName(flexContainerName);
		
		String baseLocation =  location;
		String flexContainerLocation = baseLocation + "/" + flexContainerName;
		
		BinarySwitchFlexContainer createdFlexContainer = null;

		// send create request
		ResponsePrimitive response = sendCreateFlexContainerRequest(flexContainer, baseLocation,
				Constants.ADMIN_REQUESTING_ENTITY);
		if (response.getResponseStatusCode().equals(ResponseStatusCode.CREATED)) {
			// OK
			createdFlexContainer = (BinarySwitchFlexContainer) response.getContent();
			try {
				checkFlexContainerOntologyRef(flexContainer, createdFlexContainer);
				checkFlexContainerCustomAttribute(flexContainer, createdFlexContainer);
			} catch (Exception e) {
				createTestReport(methodName, Status.KO, e.getMessage(), e);
				return;
			}
			if (!createdFlexContainer.getName().equals(flexContainerName)) {
				// KO
				createTestReport(methodName, Status.KO,
						"invalid flexContainer name, expecting:" + flexContainerName, null);
				return;
			}

		} else {
			// KO
			createTestReport(methodName, Status.KO, "unexpected response code:"
					+ response.getResponseStatusCode() + ", expected:" + ResponseStatusCode.CREATED, null);
			return;
		}

		// retrieve
		response = sendRetrieveRequest(flexContainerLocation);
		if (response.getResponseStatusCode().equals(ResponseStatusCode.OK)) {
			// OK
			BinarySwitchFlexContainer retrievedFlexContainer = (BinarySwitchFlexContainer) response.getContent();
			try {
				checkFlexContainer(createdFlexContainer, retrievedFlexContainer);
			} catch (Exception e) {
				createTestReport(methodName, Status.KO, e.getMessage(), e);
				return;
			}
		} else {
			// KO
			createTestReport(methodName, Status.KO, "unexpected response code:"
					+ response.getResponseStatusCode() + ", expected:" + ResponseStatusCode.OK, null);
			return;
		}

		// OK
		createTestReport(methodName, Status.OK, null, null);

	}

}
