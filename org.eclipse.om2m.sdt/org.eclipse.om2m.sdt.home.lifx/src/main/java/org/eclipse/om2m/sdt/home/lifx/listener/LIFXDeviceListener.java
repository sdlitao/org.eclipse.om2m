/*******************************************************************************
 * Copyright (c) 2014, 2016 Orange.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.om2m.sdt.home.lifx.listener;

import org.eclipse.om2m.sdt.home.lifx.impl.lan.frame.LIFXPayloadState;
import org.eclipse.om2m.sdt.home.lifx.impl.lan.frame.LIFXPayloadStatePower;

public interface LIFXDeviceListener {
	
	public void notifyStatePower(LIFXPayloadStatePower statePower);
	
	public void notifyState(LIFXPayloadState state);

}
