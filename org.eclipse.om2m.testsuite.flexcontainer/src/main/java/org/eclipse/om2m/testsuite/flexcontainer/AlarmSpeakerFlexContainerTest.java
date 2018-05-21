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
import org.eclipse.om2m.commons.resource.flexcontainerspec.AlarmSpeakerFlexContainer;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.testsuite.flexcontainer.TestReport.Status;

public class AlarmSpeakerFlexContainerTest extends FlexContainerTestSuite {

	public AlarmSpeakerFlexContainerTest(final CseService pCseService) {
		super(pCseService);
	}

	@Override
	protected String getTestSuiteName() {
		return "AlarmSpeakerFlexContainerTest";
	}

	public void testCreateAndRetrieveAlarmSpeakerFlexContainer() {

		String baseLocation = "/" + Constants.CSE_ID + "/" + Constants.CSE_NAME;
		String flexContainerName = "AlarmSpeakerFlexContainer_" + System.currentTimeMillis();
		String flexContainerLocation = baseLocation + "/" + flexContainerName;

		AlarmSpeakerFlexContainer flexContainer = new AlarmSpeakerFlexContainer();
		flexContainer.setName(flexContainerName);
		flexContainer.setOntologyRef("OrangeOntology");
		flexContainer.setCreator("Greg");

		CustomAttribute toneCustomAttribute = new CustomAttribute();
		toneCustomAttribute.setCustomAttributeName("tone");
		toneCustomAttribute.setCustomAttributeValue("1");
		flexContainer.getCustomAttributes().add(toneCustomAttribute);

		CustomAttribute alarmStatusCustomAttribute = new CustomAttribute();
		alarmStatusCustomAttribute.setCustomAttributeName("alaSs");
		alarmStatusCustomAttribute.setCustomAttributeValue("true");
		flexContainer.getCustomAttributes().add(alarmStatusCustomAttribute);

		// send CREATE request
		ResponsePrimitive response = sendCreateFlexContainerRequest(flexContainer, baseLocation, Constants.ADMIN_REQUESTING_ENTITY);
		AlarmSpeakerFlexContainer createdFlexContainer = null;
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.CREATED)) {
			// KO
			createTestReport("testCreateAndRetrieveAlarmSpeakerFlexContainer", Status.KO,
					"unable to create AlarmSpeaker FlexContainer:" + response.getContent(), null);
			return;
		} else {
			createdFlexContainer = (AlarmSpeakerFlexContainer) response.getContent();

			if (!flexContainerName.equals(createdFlexContainer.getName())) {
				createTestReport("testCreateAndRetrieveAlarmSpeakerFlexContainer", Status.KO,
						"invalid name. Expecting:" + flexContainerName + ", found:" + createdFlexContainer.getName(),
						null);
				return;
			}

			try {
				checkFlexContainerCreator(flexContainer, createdFlexContainer);
				checkFlexContainerOntologyRef(flexContainer, createdFlexContainer);
				checkFlexContainerCustomAttribute(flexContainer, createdFlexContainer);
				checkFlexContainerDefinition(flexContainer, createdFlexContainer);
				checkFlexContainerOntologyRef(flexContainer, createdFlexContainer);
			} catch (Exception e) {
				createTestReport("testCreateAndRetrieveAlarmSpeakerFlexContainer", Status.KO, e.getMessage(), e);
				return;
			}
		}

		// send RETRIEVE request
		response = sendRetrieveRequest(flexContainerLocation);
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.OK)) {
			// KO
			createTestReport("testCreateAndRetrieveAlarmSpeakerFlexContainer", Status.KO,
					"unable to retrieve AlarmSpeaker FlexContainer:" + response.getContent(), null);
			return;
		} else {
			AlarmSpeakerFlexContainer retrievedFlexContainer = (AlarmSpeakerFlexContainer) response.getContent();
			try {
				checkFlexContainer(createdFlexContainer, retrievedFlexContainer);
			} catch (Exception e) {
				createTestReport("testCreateAndRetrieveAlarmSpeakerFlexContainer", Status.KO, e.getMessage(), e);
				return;
			}
		}

		createTestReport("testCreateAndRetrieveAlarmSpeakerFlexContainer", Status.OK, null, null);
	}

	public void testDeleteAlarmSpeakerFlexContainer() {
		String baseLocation = "/" + Constants.CSE_ID + "/" + Constants.CSE_NAME;
		String flexContainerName = "AlarmSpeakerFlexContainer_" + System.currentTimeMillis();
		String flexContainerLocation = baseLocation + "/" + flexContainerName;

		AlarmSpeakerFlexContainer flexContainer = new AlarmSpeakerFlexContainer();
		flexContainer.setName(flexContainerName);
		flexContainer.setOntologyRef("OrangeOntology");
		flexContainer.setCreator("Greg");

		CustomAttribute toneCustomAttribute = new CustomAttribute();
		toneCustomAttribute.setCustomAttributeName("tone");
		toneCustomAttribute.setCustomAttributeValue("1");
		flexContainer.getCustomAttributes().add(toneCustomAttribute);

		CustomAttribute alarmStatusCustomAttribute = new CustomAttribute();
		alarmStatusCustomAttribute.setCustomAttributeName("alaSs");
		alarmStatusCustomAttribute.setCustomAttributeValue("true");
		flexContainer.getCustomAttributes().add(alarmStatusCustomAttribute);

		// send CREATE request
		ResponsePrimitive response = sendCreateFlexContainerRequest(flexContainer, baseLocation, Constants.ADMIN_REQUESTING_ENTITY);
		AlarmSpeakerFlexContainer createdFlexContainer = null;
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.CREATED)) {
			// KO
			createTestReport("testDeleteAlarmSpeakerFlexContainer", Status.KO,
					"unable to create AlarmSpeaker FlexContainer:" + response.getContent(), null);
			return;
		}

		// send DELETE request
		response = sendDeleteRequest(flexContainerLocation);
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.DELETED)) {
			// KO
			createTestReport("testDeleteAlarmSpeakerFlexContainer", Status.KO,
					"unable to delete AlarmSpeaker FlexContainer:" + response.getContent(), null);
			return;
		}

		// send RETRIEVE request ==> NOT_FOUND
		response = sendRetrieveRequest(flexContainerLocation);
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.NOT_FOUND)) {
			// KO
			createTestReport("testDeleteAlarmSpeakerFlexContainer", Status.KO,
					"Expecting:" + ResponseStatusCode.NOT_FOUND + ", received:" + response.getResponseStatusCode(),
					null);
			return;
		}

		createTestReport("testDeleteAlarmSpeakerFlexContainer", Status.OK, null, null);
	}

	public void testUpdateAlarmSpeakerFlexContainer() {

		String baseLocation = "/" + Constants.CSE_ID + "/" + Constants.CSE_NAME;
		String flexContainerName = "AlarmSpeakerFlexContainer_" + System.currentTimeMillis();
		String flexContainerLocation = baseLocation + "/" + flexContainerName;

		AlarmSpeakerFlexContainer flexContainer = new AlarmSpeakerFlexContainer();
		flexContainer.setName(flexContainerName);
		flexContainer.setOntologyRef("OrangeOntology");
		flexContainer.setCreator("Greg");

		CustomAttribute toneCustomAttribute = new CustomAttribute();
		toneCustomAttribute.setCustomAttributeName("tone");
		toneCustomAttribute.setCustomAttributeValue("1");
		flexContainer.getCustomAttributes().add(toneCustomAttribute);

		CustomAttribute alarmStatusCustomAttribute = new CustomAttribute();
		alarmStatusCustomAttribute.setCustomAttributeName("alaSs");
		alarmStatusCustomAttribute.setCustomAttributeValue("true");
		flexContainer.getCustomAttributes().add(alarmStatusCustomAttribute);

		// send CREATE request
		ResponsePrimitive response = sendCreateFlexContainerRequest(flexContainer, baseLocation, Constants.ADMIN_REQUESTING_ENTITY);
		AlarmSpeakerFlexContainer createdFlexContainer = null;
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.CREATED)) {
			// KO
			createTestReport("testUpdateAlarmSpeakerFlexContainer", Status.KO,
					"unable to create AlarmSpeaker FlexContainer:" + response.getContent(), null);
			return;
		} else {
			createdFlexContainer = (AlarmSpeakerFlexContainer) response.getContent();
		}

		// prepare update
		AlarmSpeakerFlexContainer toBeUpdated = new AlarmSpeakerFlexContainer();

		CustomAttribute updatedTone = new CustomAttribute();
		updatedTone.setCustomAttributeName("tone");
		updatedTone.setCustomAttributeValue("1");
		toBeUpdated.getCustomAttributes().add(updatedTone);

		// send UPDATE request
		response = sendUpdateFlexContainerRequest(flexContainerLocation, toBeUpdated);
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.UPDATED)) {
			// KO
			createTestReport("testUpdateAlarmSpeakerFlexContainer", Status.KO,
					"unable to update AlarmSpeaker FlexContainer:" + response.getContent(), null);
			return;
		} else {
			AlarmSpeakerFlexContainer updatedFlexContainer = (AlarmSpeakerFlexContainer) response.getContent();

			if (updatedFlexContainer.getCustomAttributes().size() != 1) {
				createTestReport("testUpdateAlarmSpeakerFlexContainer", Status.KO, "Expecting 1 customAttribute, found "
						+ updatedFlexContainer.getCustomAttributes().size() + " customAttributes", null);
				return;
			}

			if (!updatedTone.getCustomAttributeValue()
					.equals(updatedFlexContainer.getCustomAttribute("tone").getCustomAttributeValue())) {
				createTestReport("testUpdateAlarmSpeakerFlexContainer", Status.KO,
						"Wrong tone attribute value. Expecting:" + updatedTone.getCustomAttributeValue() + ", received:"
								+ updatedFlexContainer.getCustomAttribute("tone").getCustomAttributeValue(),
						null);
				return;
			}
		}

		// retrieve the updated flexContainer
		response = sendRetrieveRequest(flexContainerLocation);
		if (!response.getResponseStatusCode().equals(ResponseStatusCode.OK)) {
			// KO
			createTestReport("testUpdateAlarmSpeakerFlexContainer", Status.KO,
					"unable to retrieve AlarmSpeaker FlexContainer:" + response.getContent(), null);
			return;
		} else {
			// OK
			AlarmSpeakerFlexContainer retrievedFlexContainer = (AlarmSpeakerFlexContainer) response.getContent();
			
			// update createdFlexContainer with new tone value
			createdFlexContainer.getCustomAttribute("tone")
					.setCustomAttributeValue(updatedTone.getCustomAttributeValue());
			
			try {
				checkFlexContainer(createdFlexContainer, retrievedFlexContainer);
			} catch (Exception e) {
				createTestReport("testUpdateAlarmSpeakerFlexContainer", Status.KO,
						e.getMessage(), e);
				return;
			}
		}

		createTestReport("testUpdateAlarmSpeakerFlexContainer", Status.OK, null, null);
	}

}
