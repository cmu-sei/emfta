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

package org.osate.aadl2.errormodel.emfta.actions;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.viewpoint.description.RepresentationDescription;
import org.eclipse.sirius.viewpoint.description.Viewpoint;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.ui.util.ResourceUtil;
import org.osate.aadl2.Element;
import org.osate.aadl2.Feature;
import org.osate.aadl2.errormodel.emfta.fta.EMFTAGenerator;
import org.osate.aadl2.errormodel.emfta.util.SiriusUtil;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.modelsupport.util.AadlUtil;
import org.osate.ui.actions.AaxlReadOnlyActionAsJob;
import org.osate.ui.dialogs.Dialog;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorPropagation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorTypes;
import org.osate.xtext.aadl2.errormodel.errorModel.OutgoingPropagationCondition;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;

import edu.cmu.emfta.FTAModel;

public final class EMFTAAction extends AaxlReadOnlyActionAsJob {

	private static String ERROR_STATE_NAME = null;
	private static final String prefixState = "state ";
	private static final String prefixOutgoingPropagation = "outgoing propagation on ";
	SystemInstance si;
	private org.osate.aadl2.errormodel.analysis.fta.Event ftaEvent;

	@Override
	protected String getMarkerType() {
		return "org.osate.analysis.errormodel.FaultImpactMarker";
	}

	@Override
	protected String getActionName() {
		return "FTA";
	}

	@Override
	public void doAaxlAction(IProgressMonitor monitor, Element obj) {

		monitor.beginTask("Fault Tree Analysis", IProgressMonitor.UNKNOWN);

		si = null;

		if (obj instanceof InstanceObject) {
			si = ((InstanceObject) obj).getSystemInstance();
		}

		if (si == null) {
			Dialog.showInfo("Fault Tree Analysis", "Please choose an instance model");
			monitor.done();
		}

		if (!EMV2Util.hasCompositeErrorBehavior(si) && !EMV2Util.hasOutgoingPropagationCondition(si)) {
			Dialog.showInfo("Fault Tree Analysis",
					"Your system instance must have a composite state or outgoing propagation condition declaration.");
			monitor.done();
		}

		final Display d = PlatformUI.getWorkbench().getDisplay();
		d.syncExec(new Runnable() {

			@Override
			public void run() {
				IWorkbenchWindow window;
				Shell sh;
				List<String> stateNames = new ArrayList<String>();

				window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				sh = window.getShell();

				if (EMV2Util.hasCompositeErrorBehavior(si)) {
					for (ErrorBehaviorState ebs : EMV2Util.getAllErrorBehaviorStates(si)) {
						stateNames.add(prefixState + ebs.getName());
					}
				}

				for (OutgoingPropagationCondition opc : EMV2Util.getAllOutgoingPropagationConditions(si)) {
					if (!(opc.getOutgoing().getFeatureorPPRef().getFeatureorPP() instanceof Feature)) {
						continue;
					}
					stateNames.add(prefixOutgoingPropagation + EMV2Util.getPrintName(opc.getOutgoing())
							+ EMV2Util.getPrintName(opc.getTypeToken()));// feat.getName());
				}

				FTADialog diag = new FTADialog(sh);
				diag.setValues(stateNames);
				diag.open();
				ERROR_STATE_NAME = diag.getValue();

			}
		});

		if (ERROR_STATE_NAME != null) {
//			String errorStateName;
//			String errorStateTypeName;
			ErrorBehaviorState errorState;
			ErrorTypes errorType;
			ErrorPropagation errorPropagation;
			String toProcess;

			errorState = null;
			errorType = null;
			errorPropagation = null;

			if (ERROR_STATE_NAME.startsWith(prefixState)) {
				toProcess = ERROR_STATE_NAME.replace(prefixState, "");
				for (ErrorBehaviorState ebs : EMV2Util.getAllErrorBehaviorStates(si)) {
					if (ebs.getName().equalsIgnoreCase(toProcess)) {
						errorState = ebs;
					}
				}

			}

			if (ERROR_STATE_NAME.startsWith(prefixOutgoingPropagation)) {
				toProcess = ERROR_STATE_NAME.replace(prefixOutgoingPropagation, "");
				for (OutgoingPropagationCondition opc : EMV2Util.getAllOutgoingPropagationConditions(si)) {
					String longName = EMV2Util.getPrintName(opc.getOutgoing())
							+ EMV2Util.getPrintName(opc.getTypeToken());
					if (longName.equalsIgnoreCase(toProcess)) {
						errorPropagation = opc.getOutgoing();
						errorType = opc.getTypeToken();
					}
				}
			}

			EMFTAGenerator wrapper;
			wrapper = null;
			if ((errorState != null) || (errorPropagation != null)) {
				String targetName = "";
				String errorTypeName = (errorType == null) ? "" : ("_" + EMV2Util.getPrintName(errorType));
				if (errorState != null) {
					wrapper = new EMFTAGenerator(si, errorState, errorType);
					targetName = EMV2Util.getPrintName(errorState) + errorTypeName;
				}
				if (errorPropagation != null) {
					wrapper = new EMFTAGenerator(si, errorPropagation, errorType);
					targetName = EMV2Util.getPrintName(errorPropagation) + errorTypeName;
				}
				targetName = targetName.replaceAll("\\{", "").replaceAll("\\}", "").toLowerCase();

				URI newURI = EcoreUtil.getURI(si).trimSegments(2).appendSegment("fta")
						.appendSegment(si.getName().toLowerCase() + "_" + targetName + ".emfta");
				AadlUtil.makeSureFoldersExist(new Path(newURI.toPlatformString(true)));
				serializeEmftaModel(wrapper.getEmftaModel(), newURI, ResourceUtil.getFile(si.eResource()).getProject());

			} else {
				Dialog.showInfo("Fault Tree Analysis",
						"Unable to create the Fault Tree Analysis, please read the help content");
			}
		}

		monitor.done();
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
//			IWorkspaceRoot ws = ResourcesPlugin.getWorkspace().getRoot();
//			OsateDebug.osateDebug("[EMFTAAction]", "activeproject=" + activeProject.getName());

			activeProject.refreshLocal(IResource.DEPTH_INFINITE, null);

			Job ftaTreeCreationJob = new Job("Creation of FTA Tree") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {

					monitor.beginTask("Creation of FTA tree", 100);

					createAndOpenFTATree(activeProject, newURI, monitor);
					try {
						activeProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					} catch (CoreException e) {
						// Error while refreshing the project
					}
					monitor.done();

					return Status.OK_STATUS;
				}
			};
			ftaTreeCreationJob.setUser(true);
			ftaTreeCreationJob.schedule();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Creates and opens a FTA Tree on the specified resource
	 * @param project
	 * @param resourceUri
	 * @param monitor
	 */
	private void createAndOpenFTATree(final IProject project, final URI resourceUri, IProgressMonitor monitor) {
		SiriusUtil util = SiriusUtil.INSTANCE;
		URI emftaViewpointURI = URI.createURI("viewpoint:/emfta.design/EMFTA");

		URI semanticResourceURI = URI.createPlatformResourceURI(resourceUri.toPlatformString(true), true);
		Session existingSession = util.getSessionForProjectAndResource(project, semanticResourceURI, monitor);

		if (existingSession != null) {
			FTAModel model = getFTAModelFromSession(existingSession, semanticResourceURI);
			final Viewpoint emftaVP = util.getViewpointFromRegistry(emftaViewpointURI);
			final RepresentationDescription description = util.getRepresentationDescription(emftaVP, "Tree.diagram");
			util.createAndOpenRepresentation(existingSession, emftaVP, description, "FTA Tree", model, monitor);
		}
	}

	/**
	 * Retrieves a FTAModel instance from a semantic resource
	 * The FTA model must be one of the root objects in the specified semantic resource
	 * @param session
	 * @param uri
	 * @return
	 */
	private FTAModel getFTAModelFromSession(Session session, URI uri) {
		Resource resource = SiriusUtil.INSTANCE.getResourceFromSession(session, uri);
		if (resource != null) {
			for (EObject object : resource.getContents()) {
				if (object instanceof FTAModel) {
					return (FTAModel) object;
				}
			}
		}
		return null;
	}

}
