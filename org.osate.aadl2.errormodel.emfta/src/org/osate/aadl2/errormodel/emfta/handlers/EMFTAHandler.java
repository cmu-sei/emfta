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

package org.osate.aadl2.errormodel.emfta.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.ui.util.ResourceUtil;
import org.osate.aadl2.Element;
import org.osate.aadl2.Feature;
import org.osate.aadl2.errormodel.emfta.fta.EMFTACreateModel;
import org.osate.aadl2.errormodel.emfta.util.SiriusUtil;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.InstanceObject;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.ui.dialogs.Dialog;
import org.osate.ui.handlers.AaxlReadOnlyHandlerAsJob;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorPropagation;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeToken;
import org.osate.xtext.aadl2.errormodel.util.EM2TypeSetUtil;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;

public final class EMFTAHandler extends AaxlReadOnlyHandlerAsJob {
	private static String ERROR_STATE_NAME = null;
	private static boolean GRAPH = false;
	private static boolean TRANSFORM = false;
	private static boolean MINCUTSET = false;
	public static final String prefixState = "state ";
	public static final String prefixOutgoingPropagation = "outgoing propagation on ";
	SystemInstance si;
	ComponentInstance target;

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
			if (obj instanceof ComponentInstance) {
				target = (ComponentInstance) obj;
			} else {
				target = si;
			}
		}

		if (si == null) {
			Dialog.showInfo("Fault Tree Analysis", "Please choose an instance model");
			monitor.done();
			return;
		}

		if (!EMV2Util.hasErrorBehaviorStates(target) && !EMV2Util.hasOutgoingPropagations(target)) {
			Dialog.showInfo("Fault Tree Analysis",
					"Your system instance or selected component instance must have error behavior states or outgoing propagations.");
			monitor.done();
			return;
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

				for (ErrorBehaviorState ebs : EMV2Util.getAllErrorBehaviorStates(target)) {
					stateNames.add(prefixState + ebs.getName());
				}

				for (ErrorPropagation opc : EMV2Util.getAllOutgoingErrorPropagations(target.getComponentClassifier())) {
					if (!(opc.getFeatureorPPRef().getFeatureorPP() instanceof Feature)) {
						continue;
					}
					EList<TypeToken> result = EM2TypeSetUtil.generateAllLeafTypeTokens(opc.getTypeSet(),
							EMV2Util.getUseTypes(opc));
					for (TypeToken tt : result) {
						String epName = prefixOutgoingPropagation + EMV2Util.getPrintName(opc)
								+ EMV2Util.getPrintName(tt);
						if (!stateNames.contains(epName)) {
							stateNames.add(epName);
						}
					}
				}

				FTADialog diag = new FTADialog(sh);
				diag.setValues(stateNames);
				diag.setTarget(
						"'" + (target instanceof SystemInstance ? target.getName() : target.getComponentInstancePath())
								+ "'");
				diag.open();
				ERROR_STATE_NAME = diag.getValue();
				GRAPH = diag.getSharedEventsAsGraph();
				TRANSFORM = diag.getTransform();
				MINCUTSET = diag.getMinCutSet();
			}
		});

		if (ERROR_STATE_NAME != null) {
			EMFTACreateModel doModel = new EMFTACreateModel(si);
			URI newURI = doModel.createModel(target, ERROR_STATE_NAME, TRANSFORM, GRAPH, MINCUTSET);
			if (newURI != null) {
				if (MINCUTSET) {
					SiriusUtil.INSTANCE.autoOpenModel(newURI, ResourceUtil.getFile(si.eResource()).getProject(),
							"viewpoint:/emfta.design/EMFTA", "Cutset.diagram", "Minimal Cutset");
					monitor.done();
					return;
				} else {
					SiriusUtil.INSTANCE.autoOpenModel(newURI, ResourceUtil.getFile(si.eResource()).getProject(),
							"viewpoint:/emfta.design/EMFTA", "Tree.diagram", "Fault Tree");
					monitor.done();
					return;
				}
			}
		}
		monitor.done();
	}
}