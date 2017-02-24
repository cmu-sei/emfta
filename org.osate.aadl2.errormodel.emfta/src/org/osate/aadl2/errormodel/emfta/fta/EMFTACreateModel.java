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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.ui.util.ResourceUtil;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.modelsupport.util.AadlUtil;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorPropagation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorTypes;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeToken;
import org.osate.xtext.aadl2.errormodel.util.AnalysisModel;
import org.osate.xtext.aadl2.errormodel.util.EM2TypeSetUtil;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;

import edu.cmu.emfta.FTAModel;

public final class EMFTACreateModel {

	private static final String prefixState = "state ";
	private static final String prefixOutgoingPropagation = "outgoing propagation on ";

	private ComponentInstance rootComponent;
	private AnalysisModel currentAnalysisModel;

	public EMFTACreateModel(SystemInstance root) {
		rootComponent = root;
		currentAnalysisModel = new AnalysisModel(root, false);
	}

	public URI createModel(ComponentInstance selection, final String errorStateName, boolean minimize, boolean graph) {
		ErrorBehaviorState errorState;
		ErrorTypes errorType;
		ErrorPropagation errorPropagation;
		String toProcess;

		errorState = null;
		errorType = null;
		errorPropagation = null;

		if (errorStateName.startsWith(prefixState)) {
			toProcess = errorStateName.replace(prefixState, "");
			for (ErrorBehaviorState ebs : EMV2Util.getAllErrorBehaviorStates(selection)) {
				if (ebs.getName().equalsIgnoreCase(toProcess)) {
					errorState = ebs;
				}
			}

		}

		if (errorStateName.startsWith(prefixOutgoingPropagation)) {
			toProcess = errorStateName.replace(prefixOutgoingPropagation, "");
//			for (OutgoingPropagationCondition opc : EMV2Util.getAllOutgoingPropagationConditions(selection)) {
//				String longName = EMV2Util.getPrintName(opc.getOutgoing()) + EMV2Util.getPrintName(opc.getTypeToken());
//				if (longName.equalsIgnoreCase(toProcess)) {
//					errorPropagation = opc.getOutgoing();
//					errorType = opc.getTypeToken();
//				}
//			}
			for (ErrorPropagation opc : EMV2Util.getAllOutgoingErrorPropagations(selection.getComponentClassifier())) {
				EList<TypeToken> result = EM2TypeSetUtil.generateAllLeafTypeTokens(opc.getTypeSet(),
						EMV2Util.getUseTypes(opc));
				for (TypeToken tt : result) {
					String longName = EMV2Util.getPrintName(opc) + EMV2Util.getPrintName(tt);
					if (longName.equalsIgnoreCase(toProcess) && !tt.getType().isEmpty()) {
						errorPropagation = opc;
						errorType = tt.getType().get(0);
					}
				}
			}
		}

		EMFTAGenerator wrapper;
		wrapper = null;
		if ((errorState != null) || (errorPropagation != null)) {
			if (errorState != null) {
				wrapper = new EMFTAGenerator(currentAnalysisModel, selection, errorState, errorType);
			}
			if (errorPropagation != null) {
				wrapper = new EMFTAGenerator(currentAnalysisModel, selection, errorPropagation, errorType);
			}
			FTAModel ftamodel = wrapper.getEmftaModel(minimize, graph);
			String rootname = ftamodel.getName() + (minimize ? "" : "_full") + (graph ? "_graph" : "");
			ftamodel.setName(rootname);

			URI newURI = EcoreUtil.getURI(selection).trimFragment().trimSegments(2).appendSegment("fta")
					.appendSegment(rootname + ".emfta");
			AadlUtil.makeSureFoldersExist(new Path(newURI.toPlatformString(true)));
			URI ftauri = serializeEmftaModel(ftamodel, newURI,
					ResourceUtil.getFile(selection.eResource()).getProject());
			return ftauri;
		} else {
			return null;
		}
	}

	private URI serializeEmftaModel(edu.cmu.emfta.FTAModel emftaModel, final URI newURI, final IProject activeProject) {

		try {
			ResourceSet set = new ResourceSetImpl();
			Resource res = set.createResource(newURI);
			res.getContents().add(emftaModel);
			res.save(null);
			return EcoreUtil.getURI(emftaModel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newURI;

	}

}
