/*******************************************************************************
 * Copyright (c) 2014, 2016 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.om2m.das.testsuite.ae;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.constants.Operation;
import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.resource.CustomAttribute;
import org.eclipse.om2m.commons.resource.DynamicAuthorizationConsultation;
import org.eclipse.om2m.commons.resource.FlexContainer;
import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.interworking.service.InterworkingService;
import org.osgi.framework.ServiceRegistration;

public class DASServiceTest_FlexContainer extends AbstractDASServiceTest {

	/**
	 * To be used by activator
	 * 
	 * @param pCseService
	 */
	public DASServiceTest_FlexContainer(CseService pCseService) {
		super("DasServiceTest_FlexContainer", pCseService);
	}

	@Override
	public void performTest() {

		// create DAC
		DynamicAuthorizationConsultation dac = createDAS(getDasAE().getResourceID());
		if (dac == null) {
			setState(State.KO);
			setMessage("unable to create dac");
			return;
		}

		// set number of expected call
		setExpectedNumberOfCall(1);

		// register this as a InterworkingService
		ServiceRegistration<InterworkingService> interworkingServiceRegistration = registerInterworkingService(
				this);

		// create flexContainer (with DynamicAuthorizationConsultationIDs)
		List<String> dacis = new ArrayList<>();
		dacis.add(dac.getResourceID());
		FlexContainer createdFlexContainer = createFlexContainer(dacis);
		if (createdFlexContainer == null) {
			setState(State.KO);
			setMessage("unable to create FlexContainer");
			return;
		}

		// retrieve flexContainer ==> DASS must be called
		ResponsePrimitive response = retrieveEntity(createdFlexContainer.getResourceID(), "nom:password");
		if (!ResponseStatusCode.OK.equals(response.getResponseStatusCode())) {
			setState(State.KO);
			setMessage("unable to retrieve FlexContainer, expecting " + ResponseStatusCode.OK + ", found ="
					+ response.getResponseStatusCode());
			return;
		}

		if (!checkCall(0, createdFlexContainer.getResourceID(), "nom:password", Operation.RETRIEVE)) {
			// KO
			return;
		}

		// clear calls
		clearCalls();

		// update FlexContainer
		FlexContainer toBeUpdated = new FlexContainer();
		CustomAttribute customAttribute = new CustomAttribute();
		customAttribute.setCustomAttributeName("test");
		customAttribute.setCustomAttributeValue("value");
		createdFlexContainer.getCustomAttributes().add(customAttribute);

		// prepare update request
		RequestPrimitive updateRequest = new RequestPrimitive();
		updateRequest.setFrom("nom:prenom");
		updateRequest.setTo(createdFlexContainer.getResourceID());
		updateRequest.setOperation(Operation.UPDATE);
		updateRequest.setRequestContentType(MimeMediaType.OBJ);
		updateRequest.setReturnContentType(MimeMediaType.OBJ);
		updateRequest.setContent(toBeUpdated);

		// execute update request
		ResponsePrimitive updateResponse = getCseService().doRequest(updateRequest);
		if (updateResponse == null) {
			setState(State.KO);
			setMessage("updateFlexContainer response is null");
			return;
		}
		if (!ResponseStatusCode.UPDATED.equals(updateResponse.getResponseStatusCode())) {
			setState(State.KO);
			setMessage("unable to update FlexContainer, expecting " + ResponseStatusCode.UPDATED + ", found "
					+ updateResponse.getResponseStatusCode());
			return;
		}

		// check call
		if (!checkCall(0, createdFlexContainer.getResourceID(), "nom:prenom", Operation.UPDATE)) {
			// KO
			return;
		}

		// clear calls
		clearCalls();

		// create a new childFlexContainer
		FlexContainer flexContainerChildToBeCreated = new FlexContainer();
		flexContainerChildToBeCreated.setContainerDefinition("tototto");
		flexContainerChildToBeCreated.setName("FlexContainer_" + UUID.randomUUID());

		// prepare child creation request
		RequestPrimitive createChildRequest = new RequestPrimitive();
		createChildRequest.setContent(flexContainerChildToBeCreated);
		createChildRequest.setRequestContentType(MimeMediaType.OBJ);
		createChildRequest.setReturnContentType(MimeMediaType.OBJ);
		createChildRequest.setResourceType(ResourceType.FLEXCONTAINER);
		createChildRequest.setFrom("nana:nono");
		createChildRequest.setTo(createdFlexContainer.getResourceID());
		createChildRequest.setOperation(Operation.CREATE);

		// execute createChildRequest
		ResponsePrimitive createChildResponse = getCseService().doRequest(createChildRequest);
		if (createChildResponse == null) {
			setState(State.KO);
			setMessage("create child response is null");
			return;
		}
		if (!ResponseStatusCode.CREATED.equals(createChildResponse.getResponseStatusCode())) {
			setState(State.KO);
			setMessage("unable to create a child FlexContainer, expecting " + ResponseStatusCode.CREATED + ", found "
					+ createChildResponse.getResponseStatusCode());
			return;
		}
		// check call
		if (!checkCall(0, createdFlexContainer.getResourceID(), "nana:nono", Operation.CREATE)) {
			// KO
			return;
		}

		// clear calls
		clearCalls();

		// delete FlexContainer
		RequestPrimitive deleteRequest = new RequestPrimitive();
		deleteRequest.setTo(createdFlexContainer.getResourceID());
		deleteRequest.setFrom("toto:tata");
		deleteRequest.setOperation(Operation.DELETE);

		// execute delete request
		ResponsePrimitive deleteResponse = getCseService().doRequest(deleteRequest);
		if (deleteResponse == null) {
			setState(State.KO);
			setMessage("deleteFlexContainer response is null");
			return;
		}

		if (!ResponseStatusCode.DELETED.equals(deleteResponse.getResponseStatusCode())) {
			setState(State.KO);
			setMessage("unable to delete FlexContainer, expecting " + ResponseStatusCode.DELETED + ", found "
					+ deleteResponse.getResponseStatusCode());
			return;
		}

		// check call
		if (!checkCall(0, createdFlexContainer.getResourceID(), "toto:tata", Operation.DELETE)) {
			// KO
			return;
		}

		// unregister InterworkingService
		unregisterInterworkingService(interworkingServiceRegistration);

		setState(State.OK);
	}

	

}
