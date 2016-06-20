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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.ui.business.api.dialect.DialectUIManager;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.sirius.viewpoint.description.RepresentationDescription;
import org.eclipse.sirius.viewpoint.description.Viewpoint;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.ui.util.ResourceUtil;
import org.osate.aadl2.Element;
import org.osate.aadl2.Feature;
import org.osate.aadl2.errormodel.emfta.fta.EMFTACreateModel;
import org.osate.aadl2.errormodel.emfta.util.SiriusUtil;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.util.OsateDebug;
import org.osate.ui.actions.AaxlReadOnlyActionAsJob;
import org.osate.ui.dialogs.Dialog;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.OutgoingPropagationCondition;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;

import edu.cmu.emfta.FTAModel;

public final class EMFTAAction extends AaxlReadOnlyActionAsJob {

	private static String ERROR_STATE_NAME = null;
	private static boolean FULL_TREE = false;
	public static final String prefixState = "state ";
	public static final String prefixOutgoingPropagation = "outgoing propagation on ";
	SystemInstance si;

	@Override
	protected String getMarkerType() {
		return "org.osate.analysis.errormodel.FaultTreeMarker";
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
					String epName = prefixOutgoingPropagation + EMV2Util.getPrintName(opc.getOutgoing())
							+ EMV2Util.getPrintName(opc.getTypeToken());
					if (!stateNames.contains(epName)) {
						stateNames.add(epName);
					}
				}

				FTADialog diag = new FTADialog(sh);
				diag.setValues(stateNames);
				diag.open();
				ERROR_STATE_NAME = diag.getValue();
				FULL_TREE = diag.getFullTree();
			}
		});

		if (ERROR_STATE_NAME != null) {
//			OsateDebug.osateDebug("Create FTA for|"+ERROR_STATE_NAME+"|");
			EMFTACreateModel doModel = new EMFTACreateModel();
			URI newURI = doModel.createModel(this.si, ERROR_STATE_NAME, FULL_TREE);
			IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(newURI.toPlatformString(true)));
			if ((newFile.exists())) {
				/**
				 * If the file exists, we show a dialog box.
				 */
//				OsateDebug.osateDebug("file exists");
//				Dialog.showInfo("Fault Tree Analysis", "File already exists. Please delete if you want to re-generate");
			}
			autoOpenEmftaModel(newURI, ResourceUtil.getFile(si.eResource()).getProject());
		} else {
			Dialog.showInfo("Fault Tree Analysis",
					"Unable to create the Fault Tree Analysis, please read the help content");
		}

		monitor.done();
	}

	public void autoOpenEmftaModel(final URI newURI, final IProject activeProject) {

		try {

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
	private void createAndOpenFTATree(final IProject project, final URI ftamodelUri, IProgressMonitor monitor) {
		SiriusUtil util = SiriusUtil.INSTANCE;
		URI emftaViewpointURI = URI.createURI("viewpoint:/emfta.design/EMFTA");

		URI semanticResourceURI = URI.createPlatformResourceURI(ftamodelUri.toPlatformString(true), true);
		Session existingSession = util.getSessionForProjectAndResource(project, semanticResourceURI, monitor);

		if (existingSession != null) {
			util.saveSession(existingSession, monitor);
			ResourceSetImpl resset = new ResourceSetImpl();
			FTAModel model = getFTAModelFromSession(existingSession, semanticResourceURI);
			// XXX this next piece of code tries to compensate for a bug in Sirius where it cannot find the FTAModel
			// It should be there since the getSessionForProjectandResource would have put it there.
			if (model == null) {
				OsateDebug.osateDebug(
						"Could not find semantic resource FTAModel in session for URI " + semanticResourceURI.path());
				EObject res = resset.getEObject(ftamodelUri, true);
				if (res instanceof FTAModel) {
					model = (FTAModel) res;
				}
			}
			if (model == null) {
				OsateDebug.osateDebug("Could not find FTAModel for URI " + ftamodelUri.path());
				return;
			}
			final Viewpoint emftaVP = util.getViewpointFromRegistry(emftaViewpointURI);
			final RepresentationDescription description = util.getRepresentationDescription(emftaVP, "Tree.diagram");
			String representationName = model.getName() + " Tree";
			final DRepresentation rep = util.findRepresentation(existingSession, emftaVP, description,
					representationName);
			if (rep != null) {
				DialectUIManager.INSTANCE.openEditor(existingSession, rep, new NullProgressMonitor());
			} else {
				try {
					util.createAndOpenRepresentation(existingSession, emftaVP, description, representationName, model,
							monitor);
				} catch (Exception e) {
					OsateDebug.osateDebug("Could not create and open FTAModel " + model.getName());
					return;
				}
			}

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
