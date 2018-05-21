/*******************************************************************************
 * Copyright (c) 2014, 2016 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.om2m.ipe.sdt;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.commons.constants.ResponseStatusCode;
import org.eclipse.om2m.commons.constants.ShortName;
import org.eclipse.om2m.commons.resource.AbstractFlexContainer;
import org.eclipse.om2m.commons.resource.AreaNwkDeviceInfo;
import org.eclipse.om2m.commons.resource.CustomAttribute;
import org.eclipse.om2m.commons.resource.DeviceInfo;
import org.eclipse.om2m.commons.resource.Node;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.commons.resource.flexcontainerspec.FlexContainerFactory;
import org.eclipse.om2m.sdt.Device;
import org.eclipse.om2m.sdt.Module;
import org.eclipse.om2m.sdt.Property;
import org.eclipse.om2m.sdt.home.types.PropertyType;

public class SDTDeviceAdaptor {

    private static final Log logger = LogFactory.getLog(SDTDeviceAdaptor.class);

	private static final String SEP = "/";
	private static final String DEVICE_PREFIX = "DEVICE_";
	private static final String NODE_PREFIX = "NODE_";
	
	private final boolean hasToBeAnnounced;
	private final String parentLocation;
	private final String baseLocation;
	private final String resourceLocation;
	private final String deviceName;
	private final String nodeName;
	private final Device device;
	private final String adminAcpResource;
	private final String announceCseId;
	private final String remoteCseName;
	private boolean isPublished = false;
	private final Map<Module, SDTModuleAdaptor> modules;
	private String nodeLocation;

	/**
	 * 
	 * @param pParentLocation
	 * @param pResourceName
	 * @param pDevice
	 */
	public SDTDeviceAdaptor(final String pParentLocation, final String baseLocation,
			final Device pDevice, final String pAdminAcpResource, 
			final String pAnnounceCseId, final String pRemoteCseName, final boolean hasToBeAnnounced) {
		this.parentLocation = pParentLocation;
		this.baseLocation = baseLocation;
		this.hasToBeAnnounced = hasToBeAnnounced;
		this.device = pDevice;
		this.deviceName = DEVICE_PREFIX + device.getId();
		this.nodeName = NODE_PREFIX + device.getId();
		this.resourceLocation = parentLocation + SEP + deviceName;
		this.announceCseId = pAnnounceCseId;
		this.remoteCseName = pRemoteCseName;
		this.adminAcpResource = pAdminAcpResource;
		modules = new HashMap<Module, SDTModuleAdaptor>();
	}

	/**
	 * Publish the SDT Device as a FlexContainer entity into the oneM2M tree
	 */
	public boolean publishIntoOM2MTree() {
		logger.info("publishIntoOM2MTree(flexContainerName=" + deviceName 
				+ ", parentLocation:" + parentLocation);
		
		AbstractFlexContainer flexContainer = FlexContainerFactory.getSpecializationFlexContainer(device.getShortDefinitionName());
		flexContainer.setName(deviceName);
		// set container definition with the value of the Device definition
		flexContainer.setContainerDefinition(device.getDefinition());
				
		// set long and short name
		flexContainer.setLongName(device.getLongDefinitionName());
		flexContainer.setShortName(device.getShortDefinitionName());
		flexContainer.getAccessControlPolicyIDs().add(adminAcpResource);
		
		// labels
		flexContainer.getLabels().add("id/" + this.device.getId());
		flexContainer.getLabels().add("name/" + this.device.getName());
		flexContainer.getLabels().add("pid/" + this.device.getPid());
		flexContainer.getLabels().add("object.type/device");
		flexContainer.getLabels().add("cntDef/" + device.getDefinition());
		flexContainer.getLabels().add("OTB.CATEGORY/Read");

		// if we reach this point, we are sure the FlexContainer resource has
		// been created into the oneM2M tree
		isPublished = true;
		
		Node node = new Node();
		node.setNodeID(nodeName);
		node.setName(nodeName);
		node.getAccessControlPolicyIDs().add(adminAcpResource);
		node.getLabels().add("object.type/node");
		node.getLabels().add("name/" + nodeName);
		DeviceInfo devInfo = new DeviceInfo();
		node.getMgmtObjs().add(devInfo);
		
		AreaNwkDeviceInfo nwkDeviceInfo = new AreaNwkDeviceInfo();
		node.getMgmtObjs().add(nwkDeviceInfo);
		nwkDeviceInfo.setAreaNwkId("TBD");
		nwkDeviceInfo.setDevID(this.device.getName());
		nwkDeviceInfo.setDevType("SDT");
		
		logger.info("Node mgmtObjs: " + node.getMgmtObjs());

		if (hasToBeAnnounced) {
			flexContainer.getAnnounceTo().add(SEP + announceCseId);
			flexContainer.getAnnouncedAttribute().add(ShortName.NODE_LINK);
			
			node.getAnnounceTo().add(SEP + announceCseId);
			node.getAnnouncedAttribute().add(ShortName.HOSTED_SRV_LINK);
			
			devInfo.getAnnounceTo().add(SEP + announceCseId);
			devInfo.getAnnouncedAttribute().add(ShortName.MANUFACTURER);
			devInfo.getAnnouncedAttribute().add(ShortName.DEVICE_LABEL);
			devInfo.getAnnouncedAttribute().add(ShortName.DEVICE_MODEL);
			devInfo.getAnnouncedAttribute().add(ShortName.DEVICE_TYPE);
			
			nwkDeviceInfo.getAnnounceTo().add(SEP + announceCseId);
			nwkDeviceInfo.getAnnouncedAttribute().add(ShortName.DEV_TYPE);
		}

		// SDT properties are customAttribute of the device FlexContainer
		for (Property sdtProperty : device.getProperties()) {
			logger.debug("handle SDT Property (name=" + sdtProperty.getName()
				+ ", value=" + sdtProperty.getValue() + ", type=" + sdtProperty.getType() + ")");

			if (sdtProperty.getType() != null) {
			
				if ((sdtProperty.getValue() == null) && (sdtProperty.isOptional())) {
					// do not add this property because it is null and optional
					continue;
				}
				String shortName = sdtProperty.getShortName();
				if (shortName.equals(PropertyType.deviceSerialNum.getShortName())) {
					devInfo.setDeviceLabel(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.deviceFirmwareVersion.getShortName())) {
					devInfo.setFwVersion(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.deviceManufacturer.getShortName())) {
					devInfo.setManufacturer(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.deviceModelName.getShortName())) {
					devInfo.setModel(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.deviceType.getShortName())) {
					devInfo.setDeviceType(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.hardwareVersion.getShortName())) {
					devInfo.setHwVersion(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.manufacturerDetailsLink.getShortName())) {
					devInfo.setManufacturerDetailsLink(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.osVersion.getShortName())) {
					devInfo.setOsVersion(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.location.getShortName())) {
					devInfo.setLocation(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.country.getShortName())) {
					devInfo.setCountry(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.dateOfManufacture.getShortName())) {
					devInfo.setManufacturingDate(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.supportURL.getShortName())) {
					devInfo.setSupportURL(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.deviceSubModelName.getShortName())) {
					devInfo.setSubModel(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.deviceName.getShortName())) {
					devInfo.setDeviceName(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.presentationURL.getShortName())) {
					devInfo.setPresentationURL(sdtProperty.getValue());
				} else if (shortName.equals(PropertyType.protocol.getShortName())) {
					devInfo.setProtocol(sdtProperty.getValue());
				} else {
					CustomAttribute customAttributeForSdtProperty = new CustomAttribute();
					customAttributeForSdtProperty.setCustomAttributeName(shortName);
					customAttributeForSdtProperty.setCustomAttributeValue(sdtProperty.getValue());
					logger.info("new Property CustomAttribute (" + customAttributeForSdtProperty + ")");
					flexContainer.getCustomAttributes().add(customAttributeForSdtProperty);
				}
			}
		}
		
		flexContainer.setNodeLink(nodeName);
		ResponsePrimitive response = CseUtil.sendCreateFlexContainerRequest(flexContainer, 
				parentLocation);
		if (! response.getResponseStatusCode().equals(ResponseStatusCode.CREATED)) {
			logger.error("unable to create a FlexContainer for SDT Device "
					+ deviceName + " : " + response.getContent(), null);
			return false;
		}
		flexContainer = (AbstractFlexContainer) response.getContent();
		node.setHostedServiceLinks(flexContainer.getName());
		
		response = CseUtil.sendCreateNodeRequest(node, baseLocation);
		if (! response.getResponseStatusCode().equals(ResponseStatusCode.CREATED)) {
			logger.error("unable to create a Node for SDT Device "
					+ deviceName + " : " + response.getContent(), null);
			return false;
		}
		nodeLocation = ((Node)response.getContent()).getResourceID();
		
		// Modules (must be done now because Device FlexContainer is the parent of each Module)
		for (Module module : this.device.getModules()) {
			SDTModuleAdaptor sdtModuleAdaptor = new SDTModuleAdaptor(module, 
					resourceLocation, announceCseId, hasToBeAnnounced);
			if (sdtModuleAdaptor.publishModuleIntoOM2MTree()) {
				modules.put(module, sdtModuleAdaptor);
			} else {
				logger.error("unable to publish module " + module.getName(), null);
				
				// unpublish all resources related to this SDT Device
				unpublishIntoOM2MTree();
				return false;
			}
		}
		return true;
	}

	/**
	 * Unpublish oneM2M resource representing the SDT Device.
	 */
	public void unpublishIntoOM2MTree() {
		logger.info("unpublish SDT Device (flexContainerLocation=" + resourceLocation + ")");
		for (SDTModuleAdaptor module : modules.values()) {
			module.unpublishModuleFromOM2MTree();
		}
		// send DELETE request for Device FlexContainer
		CseUtil.sendDeleteRequest(resourceLocation);
		// send DELETE request for Device Node
		CseUtil.sendDeleteRequest(nodeLocation);
	}

}
