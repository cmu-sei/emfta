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

import java.io.FileOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.ui.util.ResourceUtil;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.modelsupport.util.AadlUtil;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorPropagation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorTypes;
import org.osate.xtext.aadl2.errormodel.errorModel.OutgoingPropagationCondition;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;

import edu.cmu.emfta.FTAModel;

public final class EMFTACreateModel {

	public static final String prefixState = "state ";
	public static final String prefixOutgoingPropagation = "outgoing propagation on ";

	public URI createModel(SystemInstance si, final String errorStateName, boolean fullTree) {
//		String errorStateName;
//		String errorStateTypeName;
		ErrorBehaviorState errorState;
		ErrorTypes errorType;
		ErrorPropagation errorPropagation;
		String toProcess;

		errorState = null;
		errorType = null;
		errorPropagation = null;

		if (errorStateName.startsWith(prefixState)) {
			toProcess = errorStateName.replace(prefixState, "");
			for (ErrorBehaviorState ebs : EMV2Util.getAllErrorBehaviorStates(si)) {
				if (ebs.getName().equalsIgnoreCase(toProcess)) {
					errorState = ebs;
				}
			}

		}

		if (errorStateName.startsWith(prefixOutgoingPropagation)) {
			toProcess = errorStateName.replace(prefixOutgoingPropagation, "");
			for (OutgoingPropagationCondition opc : EMV2Util.getAllOutgoingPropagationConditions(si)) {
				String longName = EMV2Util.getPrintName(opc.getOutgoing()) + EMV2Util.getPrintName(opc.getTypeToken());
				if (longName.equalsIgnoreCase(toProcess)) {
					errorPropagation = opc.getOutgoing();
					errorType = opc.getTypeToken();
				}
			}
		}

		EMFTAGenerator wrapper;
		wrapper = null;
		if ((errorState != null) || (errorPropagation != null)) {
			if (errorState != null) {
				wrapper = new EMFTAGenerator(si, errorState, errorType);
			}
			if (errorPropagation != null) {
				wrapper = new EMFTAGenerator(si, errorPropagation, errorType);
			}
			FTAModel ftamodel = wrapper.getEmftaModel(fullTree);
			String rootname = ftamodel.getName();

			URI newURI = EcoreUtil.getURI(si).trimSegments(2).appendSegment("fta").appendSegment(rootname + ".emfta");

			/**
			 * We build URI of the new file and see if the file exists. If yes, w show a dialog. The file
			 * HAS to be new. This is a workaround for the issue with Sirus and the auto opening of the graphical
			 * version of the FTA.
			 */
			IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(newURI.toPlatformString(true)));
			AadlUtil.makeSureFoldersExist(new Path(newURI.toPlatformString(true)));
			serializeEmftaModel(ftamodel, newURI, ResourceUtil.getFile(si.eResource()).getProject());
			return newURI;
		} else {
			return null;
		}
	}

	public void serializeEmftaModel(edu.cmu.emfta.FTAModel emftaModel, final URI newURI, final IProject activeProject) {

//		OsateDebug.osateDebug("[EMFTAAction]", "serializeReqSpecModel activeProject=" + activeProject);

//		IFile newFile = activeProject.getFile(filename);
//		OsateDebug.osateDebug("[EMFTAAction]", "save in file=" + newFile.getName());
		IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(newURI.toPlatformString(true)));

		try {

			ResourceSet set = new ResourceSetImpl();

			Resource res = set.createResource(URI.createURI(newFile.toString()));

			res.getContents().add(emftaModel);

			FileOutputStream fos = new FileOutputStream(newFile.getRawLocation().toFile());
			res.save(fos, null);
			fos.close();
//			IWorkspaceRoot ws = ResourcesPlugin.getWorkspace().getRoot();
//			OsateDebug.osateDebug("[EMFTAAction]", "activeproject=" + activeProject.getName());

			activeProject.refreshLocal(IResource.DEPTH_INFINITE, null);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
