/*******************************************************************************
 * Copyright (c) 2014, 2016 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.om2m.sdt.home.lifx.impl;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.om2m.sdt.Device;
import org.eclipse.om2m.sdt.home.HomeDomain;
import org.eclipse.om2m.sdt.home.driver.Utils;
import org.eclipse.om2m.sdt.home.lifx.LIFXDevice;
import org.eclipse.om2m.sdt.home.lifx.LIFXDiscoveredDeviceListener;
import org.eclipse.om2m.sdt.home.lifx.impl.cloud.DiscoveryCloud;
import org.eclipse.om2m.sdt.home.lifx.impl.lan.DiscoveryLan;
import org.eclipse.om2m.sdt.home.lifx.impl.lan.Server;
import org.eclipse.om2m.sdt.home.lifx.sdt.LIFXSDTDevice;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Activator implements BundleActivator, ManagedService, LIFXDiscoveredDeviceListener {

	private static final String MODE = "mode";
	private static final String CLOUD_MODE_NAME = "cloud";
	private static final String LAN_MODE_NAME = "lan";
	private static final String NETWORK_INTERFACE_NAME = "network.interface.name";
	private static final String CLOUD_TOKEN = "cloud.token";
	private static final int NO_MODE = 0;
	private static final int CLOUD_MODE = 1;
	private static final int LAN_MODE = 2;

	private BundleContext bundleContext;
	private Server server;
	private Discovery discovery;

	private HomeDomain domain = new HomeDomain("LIFX Domain");

	private Map<String, List<ServiceRegistration>> serviceRegistrationDevices = new HashMap<>();
	private Map<String, LIFXSDTDevice> registeredSDTDevices = new HashMap<>();
	private ServiceRegistration managedServiceServiceRegistration;
	private int currentMode = NO_MODE;

	// LAN mode
	private InetAddress address;

	// CLOUD mode
	private String cloudToken;

	public void start(BundleContext context) throws Exception {
		bundleContext = context;

		// retrieve Log Service
		ServiceReference logServiceRef = bundleContext.getServiceReference(LogService.class.getName());
		if (logServiceRef != null) {
			LogService logService = (LogService) bundleContext.getService(logServiceRef);
			Logger.getInstance().setLogService(logService);
		}

		Dictionary properties = new Hashtable<>();
		properties.put(Constants.SERVICE_PID, "lifx.basedriver");
		managedServiceServiceRegistration = 
			bundleContext.registerService(ManagedService.class.getName(), this, properties);

		currentMode = NO_MODE;
	}

	public void stop(BundleContext context) throws Exception {
		try {
			managedServiceServiceRegistration.unregister();
			managedServiceServiceRegistration = null;
			stopMode();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void updated(Dictionary properties) throws ConfigurationException {
		try {
			if (properties != null) {
				// retrieve mode
				String mode = (String) properties.get(MODE);
				if (mode != null) {
					if (CLOUD_MODE_NAME.equals(mode)) {
						// cloud mode
						handleCloudMode(properties);
					} else if (LAN_MODE_NAME.equals(mode)) {
						// lan mode
						handleLanMode(properties);
					} else {
						System.out.println("invalid LIFX Basedriver mode -> nothing to do");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleLanMode(Dictionary properties) {
		String networkInterfaceName = (String) properties.get(NETWORK_INTERFACE_NAME);
		if (networkInterfaceName != null) {
			NetworkInterface ni;
			InetAddress localInetAddress = null;
			try {
				ni = NetworkInterface.getByInetAddress(InetAddress.getByName(networkInterfaceName));
				if (ni != null) {
					for (Enumeration<InetAddress> e = ni.getInetAddresses(); e.hasMoreElements();) {
						InetAddress ia = e.nextElement();
						if (ia instanceof Inet4Address) {
							System.out.println(ia + " is ipv4");
							localInetAddress = ia;
							break;
						}
					}
				}
				//
				System.out.println("localInetAddress=" + localInetAddress);
			} catch (SocketException e) {
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
			if (localInetAddress != null) {
				// valid configuration
				stopMode();
				currentMode = LAN_MODE;
				address = localInetAddress;
				try {
					startMode();
				} catch (UnknownHostException e) {
				}
			}
		}
	}

	private void handleCloudMode(Dictionary properties) {
		// stop current mode
		stopMode();

		// retrieve cloud.token property
		String cloudTokenProperty = (String) properties.get(CLOUD_TOKEN);

		if (cloudTokenProperty != null) {
			// set cloud token
			cloudToken = cloudTokenProperty;
			// set new mode
			currentMode = CLOUD_MODE;

			// start new mode
			try {
				startMode();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void notifyDeviceArrived(LIFXDevice lifxDevice) {
		Logger.getInstance().info(Activator.class, "notifyDeviceArrived(lifxdDevice=" + lifxDevice.toString() + ")");
		try {
			// create a new SDT Device
			LIFXSDTDevice sdtDevice = new LIFXSDTDevice(domain, lifxDevice);

			Logger.getInstance().info(Activator.class,
					"notifyDeviceArrived(lifxdDevice=" + lifxDevice.toString() + ") - registering");
			// register it
			List<ServiceRegistration> serviceRegistrations = Utils.register(sdtDevice, bundleContext);

			// store service registrations
			synchronized (serviceRegistrationDevices) {
				serviceRegistrationDevices.put(lifxDevice.getId(), serviceRegistrations);
			}
			// store SDT devices
			synchronized (registeredSDTDevices) {
				registeredSDTDevices.put(lifxDevice.getId(), sdtDevice);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void notifyDeviceLeft(LIFXDevice lifxDevice) {
		List<ServiceRegistration> srs = null;
		synchronized (serviceRegistrationDevices) {
			srs = serviceRegistrationDevices.remove(lifxDevice.getId());
		}

		if (srs != null) {
			// unregister all service registration
			for (ServiceRegistration sr : srs) {
				sr.unregister();
			}

			// remove from domain
			Device deviceToBeRemoved = null;
			synchronized (registeredSDTDevices) {
				deviceToBeRemoved = registeredSDTDevices.remove(lifxDevice.getId());
			}
			if (deviceToBeRemoved != null) {
				domain.removeDevice(deviceToBeRemoved.getId());
			}
		}
	}

	private void startMode() throws UnknownHostException {
		if (currentMode == CLOUD_MODE) {
			startCloudMode();
		} else if (currentMode == LAN_MODE) {
			startLanMode();
		} else {
			// NO MODE !
		}
	}

	private void stopMode() {
		if (currentMode == CLOUD_MODE) {
			stopCloudMode();
		} else if (currentMode == LAN_MODE) {
			stopLanMode();
		} else {
			// NO MODE !
		}
	}

	private void startCloudMode() {
		discovery = new DiscoveryCloud(cloudToken);
		discovery.addDiscoveredDeviceListener(this);
		discovery.startDiscoveryTask();
	}

	private void stopCloudMode() {
		if (discovery != null) {
			discovery.removeDiscoveredDeviceListener(this);
			discovery.stopDiscoveryTask();
			discovery = null;
		}
	}

	private void startLanMode() throws UnknownHostException {
		server = Server.getInstance();
		server.init(address);
		server.startServer();

		discovery = new DiscoveryLan();
		discovery.addDiscoveredDeviceListener(this);
		discovery.startDiscoveryTask();
	}

	private void stopLanMode() {
		if (discovery != null) {
			discovery.removeDiscoveredDeviceListener(this);
			discovery.stopDiscoveryTask();
		}
		if (server != null) {
			server.stopServer();
			server = null;
		}
	}

}
