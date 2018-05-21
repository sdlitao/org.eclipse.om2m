/*******************************************************************************
 * Copyright (c) 2014, 2016 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.om2m.ipe.sdt;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.resource.AbstractFlexContainer;
import org.eclipse.om2m.commons.resource.CustomAttribute;
import org.eclipse.om2m.commons.resource.FlexContainer;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.sdt.Module;
import org.eclipse.om2m.sdt.events.SDTEventListener;
import org.eclipse.om2m.sdt.events.SDTNotification;
import org.osgi.framework.ServiceRegistration;

/**
 * This class handles notifications from SDT layer. An instance is registered
 * per SDT Module
 * 
 * @author MPCY8647
 *
 */
public class ModuleSDTListener implements SDTEventListener {

    private static Log logger = LogFactory.getLog(ModuleSDTListener.class);

	private final Module module;
	private final String moduleFlexContainerLocation;

	private ServiceRegistration<?> serviceRegistration;

	public ModuleSDTListener(final Module pModule, final String pModuleFlexContainerLocation) {
		this.module = pModule;
		this.moduleFlexContainerLocation = pModuleFlexContainerLocation;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void register() {
		logger.info("registering as a SDTListener");
		if (serviceRegistration == null) {
			Dictionary properties = new Hashtable<>();
			properties.put(SDTEventListener.DOMAINS, "*");
			properties.put(SDTEventListener.DEVICES_IDS, module.getOwner().getId());
			properties.put(SDTEventListener.MODULES_NAMES, module.getName());
			properties.put(SDTEventListener.DATAPOINTS, "*");
			properties.put(SDTEventListener.DEVICES_DEFS, "*");
			properties.put(SDTEventListener.MODULES_DEFS, "*");

			serviceRegistration = Activator.registerSDTListener(this, properties);

			logger.info("register as a SDTListener - DONE");
		} else {
			logger.error("ModuleSDTListener has been previously registered!!!", null);
		}
	}

	public void unregister() {
		logger.info("unregistering as a SDTListener");
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
			serviceRegistration = null;
		}
	}

	@Override
	public void handleNotification(SDTNotification notif) {
		logger.info("receive a notification for " + notif.getDataPoint().getName() 
				+ ", value=" + notif.getValue());

		AbstractFlexContainer toBeUpdated = new FlexContainer();
		CustomAttribute ca = new CustomAttribute();
		ca.setCustomAttributeName(notif.getDataPoint().getName());
		Object value = notif.getValue();
		ca.setCustomAttributeValue((value != null ? value.toString() : null));
		toBeUpdated.getCustomAttributes().add(ca);

		ResponsePrimitive response = CseUtil.sendInternalNotifyFlexContainerRequest(toBeUpdated, 
				moduleFlexContainerLocation);
		if (! ResponseStatusCode.UPDATED.equals(response.getResponseStatusCode())) {
			logger.info("unable to send INTERNAL NOTIFY request to flexContainer " 
					+ moduleFlexContainerLocation + " : " + response.getContent(),
					null);
		} else {
			logger.debug("INTERNAL NOTIFY request successfully performed");
		}
	}

	@Override
	public void setAuthenticationThreadGroup(ThreadGroup group) {

	}
	
}
