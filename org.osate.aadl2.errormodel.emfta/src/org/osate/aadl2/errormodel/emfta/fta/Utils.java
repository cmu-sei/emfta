/**
 * Copyright (c) 2015 Carnegie Mellon University.
 * All Rights Reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," WITH NO WARRANTIES WHATSOEVER.
 * CARNEGIE MELLON UNIVERSITY EXPRESSLY DISCLAIMS TO THE FULLEST 
 * EXTENT PERMITTEDBY LAW ALL EXPRESS, IMPLIED, AND STATUTORY 
 * WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND 
 * NON-INFRINGEMENT OF PROPRIETARY RIGHTS.

 * This Program is distributed under a BSD license.  
 * Please see license.txt file or permission@sei.cmu.edu for more
 * information. 
 * 
 * DM-0003411
 */

package org.osate.aadl2.errormodel.emfta.fta;

import org.osate.aadl2.NamedElement;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorEvent;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorPropagation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorSource;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorTypes;
import org.osate.xtext.aadl2.errormodel.util.EMV2Properties;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;

import edu.cmu.emfta.Event;

public class Utils {

	/**
	 * Fill an Event with all the properties from the AADL model. Likely, all the related
	 * values in the Hazard property from EMV2.
	 *
	 * @param event					- the event related to the EMV2 artifact
	 * @param component             - the component from the event
	 * @param errorModelArtifact    - the EMV2 artifact (error event, error propagation, etc)
	 * @param type               - the type set (null if none)
	 */
	public static void fillProperties(Event event, ComponentInstance component, NamedElement errorModelArtifact,
			ErrorTypes type, double scale) {
		String propertyDescription;
		propertyDescription = EMV2Properties.getDescription(errorModelArtifact, component);

		if (propertyDescription == null) {
			event.setDescription(getDescription(component, errorModelArtifact, type));
		} else {
			event.setDescription(propertyDescription + "(component " + component.getName() + ")");
		}

		event.setProbability(EMV2Properties.getProbability(component, errorModelArtifact, type) * scale);
	}

	public static void fillProperties(Event event, ComponentInstance component, NamedElement errorModelArtifact,
			ErrorTypes type) {
		fillProperties(event, component, errorModelArtifact, type, 1);
	}

	public static String getDescription(ComponentInstance component, NamedElement errorModelArtifact, ErrorTypes type) {
		String description;
		description = "";

		if (errorModelArtifact instanceof ErrorSource) {
			ErrorSource errorSource = (ErrorSource) errorModelArtifact;

			description += "Error source";

			if (errorSource.getName() != null) {
				description += " " + errorSource.getName();
			}
			description += " on component " + (component instanceof SystemInstance ? component.getName()
					: component.getComponentInstancePath());

			if ((errorSource.getOutgoing().getFeatureorPPRef() != null)
					&& (errorSource.getOutgoing().getFeatureorPPRef().getFeatureorPP() != null)) {
				NamedElement el = errorSource.getOutgoing().getFeatureorPPRef().getFeatureorPP();
				description += " from ";
				description += el.getName();
			}
			if (type != null) {
				description += " with types " + EMV2Util.getPrintName(type);
			}

		}

		if (errorModelArtifact instanceof ErrorEvent) {
			ErrorEvent errorEvent = (ErrorEvent) errorModelArtifact;
			description += "Error";
			description += " event " + errorEvent.getName();
			if (type != null) {
				description += " with types " + EMV2Util.getPrintName(type);
			}
			description += " on component " + component.getName();

		}

		if (errorModelArtifact instanceof ErrorBehaviorState) {
			ErrorBehaviorState ebs = (ErrorBehaviorState) errorModelArtifact;
			description = "component " + component.getName() + " in state " + ebs.getName();
		}

		if (errorModelArtifact instanceof ErrorPropagation) {
			ErrorPropagation ep = (ErrorPropagation) errorModelArtifact;
			description = "component " + component.getName() + " with " + ep.getDirection() + " propagation "
					+ EMV2Util.getPropagationName(ep);
		}

		return description;
	}
}
