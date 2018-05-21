/*******************************************************************************
 * Copyright (c) 2014, 2016 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.om2m.testsuite.flexcontainer;

import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.resource.CustomAttribute;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.commons.resource.flexcontainerspec.DeviceFloodDetectorFlexContainer;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.testsuite.flexcontainer.TestReport.Status;

public class FloodDetectorFlexContainerTest extends FlexContainerTestSuite {

	public FloodDetectorFlexContainerTest(final CseService pCseService) {
		super(pCseService);
	}

	@Override
	protected String getTestSuiteName() {
		return "FloodDetectorFlexContainerTest";
	}

	public void testCreateAndRetrieveFloodDetectorFlexContainer() {
		String baseLocation = "/" + Constants.CSE_ID + "/" + Constants.CSE_NAME;
		String flexContainerName = "FloodDetectorFlexContainer_" + System.currentTimeMillis();
		String flexContainerLocation = baseLocation + "/" + flexContainerName;

		DeviceFloodDetectorFlexContainer flexContainer = new DeviceFloodDetectorFlexContainer();
		flexContainer.setName(flexContainerName);
		flexContainer.setCreator("Greg");
		flexContainer.setOntologyRef("OntologyRef");

		CustomAttribute serialNumberCA = new CustomAttribute();
		serialNumberCA.setCustomAttributeName("pDSNm");
		serialNumberCA.setCustomAttributeValue("sn1");
		flexContainer.getCustomAttributes().add(serialNumberCA);

		CustomAttribute locationCA = new CustomAttribute();
		locationCA.setCustomAttributeName("proLn");
		locationCA.setCustomAttributeValue("kitchen");
		flexContainer.getCustomAttributes().add(locationCA);

		CustomAttribute deviceManufacturerCA = new CustomAttribute();
		deviceManufacturerCA.setCustomAttributeName("prDMr");
		deviceManufacturerCA.setCustomAttributeValue("Orange");
		flexContainer.getCustomAttributes().add(deviceManufacturerCA);

		CustomAttribute protocolCA = new CustomAttribute();
		protocolCA.setCustomAttributeName("proPl");
		protocolCA.setCustomAttributeValue("ZigBee");
		flexContainer.getCustomAttributes().add(protocolCA);

		CustomAttribute deviceModelCA = new CustomAttribute();
		deviceModelCA.setCustomAttributeName("pDMNe");
		deviceModelCA.setCustomAttributeValue("Model1");
		flexContainer.getCustomAttributes().add(deviceModelCA);

		// send CREATE request
		ResponsePrimitive response = sendCreateFlexContainerRequest(flexContainer, baseLocation, Constants.ADMIN_REQUESTING_ENTITY);
		DeviceFloodDetectorFlexContainer createdFlexContainer = null;
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.CREATED)) {
			// KO
			createTestReport("testCreateAndRetrieveFloodDetectorFlexContainer", Status.KO,
					"unable to create FloodDetector FlexContainer:" + response.getContent(), null);
			return;
		} else {
			createdFlexContainer = (DeviceFloodDetectorFlexContainer) response.getContent();

			if (!flexContainerName.equals(createdFlexContainer.getName())) {
				createTestReport("testCreateAndRetrieveFloodDetectorFlexContainer", Status.KO,
						"invalid name.Expecting: " + flexContainerName + ", found:" + createdFlexContainer.getName(),
						null);
				return;
			}

			try {
				checkFlexContainerCreator(flexContainer, createdFlexContainer);
				checkFlexContainerCustomAttribute(flexContainer, createdFlexContainer);
				checkFlexContainerDefinition(flexContainer, createdFlexContainer);
				checkFlexContainerOntologyRef(flexContainer, createdFlexContainer);
			} catch (Exception e) {
				createTestReport("testCreateAndRetrieveFloodDetectorFlexContainer", Status.KO, e.getMessage(), e);
				return;
			}
		}

		// send RETRIEVE request
		response = sendRetrieveRequest(flexContainerLocation);
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.OK)) {
			// KO
			createTestReport("testCreateAndRetrieveFloodDetectorFlexContainer", Status.KO,
					"unable to retrieve FloodDetector FlexContainer:" + response.getContent(), null);
			return;
		} else {
			DeviceFloodDetectorFlexContainer retrievedFlexContainer = (DeviceFloodDetectorFlexContainer) response.getContent();
			try {
				checkFlexContainer(createdFlexContainer, retrievedFlexContainer);
			} catch (Exception e) {
				createTestReport("testCreateAndRetrieveFloodDetectorFlexContainer", Status.KO, e.getMessage(), e);
				return;
			}
		}

		createTestReport("testCreateAndRetrieveFloodDetectorFlexContainer", Status.OK, null, null);

	}

	public void testDeleteFloodDetectorFlexContainer() {

		String baseLocation = "/" + Constants.CSE_ID + "/" + Constants.CSE_NAME;
		String flexContainerName = "FloodDetectorFlexContainer_" + System.currentTimeMillis();
		String flexContainerLocation = baseLocation + "/" + flexContainerName;

		DeviceFloodDetectorFlexContainer flexContainer = new DeviceFloodDetectorFlexContainer();
		flexContainer.setName(flexContainerName);
		flexContainer.setCreator("Greg");
		flexContainer.setOntologyRef("OntologyRef");

		CustomAttribute serialNumberCA = new CustomAttribute();
		serialNumberCA.setCustomAttributeName("pDSNm");
		serialNumberCA.setCustomAttributeValue("sn1");
		flexContainer.getCustomAttributes().add(serialNumberCA);

		CustomAttribute locationCA = new CustomAttribute();
		locationCA.setCustomAttributeName("proLn");
		locationCA.setCustomAttributeValue("kitchen");
		flexContainer.getCustomAttributes().add(locationCA);

		CustomAttribute deviceManufacturerCA = new CustomAttribute();
		deviceManufacturerCA.setCustomAttributeName("prDMr");
		deviceManufacturerCA.setCustomAttributeValue("Orange");
		flexContainer.getCustomAttributes().add(deviceManufacturerCA);

		CustomAttribute protocolCA = new CustomAttribute();
		protocolCA.setCustomAttributeName("proPl");
		protocolCA.setCustomAttributeValue("ZigBee");
		flexContainer.getCustomAttributes().add(protocolCA);

		CustomAttribute deviceModelCA = new CustomAttribute();
		deviceModelCA.setCustomAttributeName("pDMNe");
		deviceModelCA.setCustomAttributeValue("Model1");
		flexContainer.getCustomAttributes().add(deviceModelCA);

		// send CREATE request
		ResponsePrimitive response = sendCreateFlexContainerRequest(flexContainer, baseLocation, Constants.ADMIN_REQUESTING_ENTITY);
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.CREATED)) {
			// KO
			createTestReport("testDeleteFloodDetectorFlexContainer", Status.KO,
					"unable to create FloodDetector FlexContainer:" + response.getContent(), null);
			return;
		}

		// send DELETE request
		response = sendDeleteRequest(flexContainerLocation);
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.DELETED)) {
			// KO
			createTestReport("testDeleteFloodDetectorFlexContainer", Status.KO,
					"unable to delete FloodDetector FlexContainer:" + response.getContent(), null);
			return;
		}

		// send RETRIEVE request ==> NOT_FOUND
		response = sendRetrieveRequest(flexContainerLocation);
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.NOT_FOUND)) {
			// KO
			createTestReport("testDeleteFloodDetectorFlexContainer", Status.KO,
					"Expecting:" + ResponseStatusCode.NOT_FOUND + ", received:" + response.getResponseStatusCode(),
					null);
			return;
		}

		createTestReport("testDeleteFloodDetectorFlexContainer", Status.OK, null, null);
	}

}
