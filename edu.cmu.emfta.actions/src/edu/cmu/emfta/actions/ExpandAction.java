/**
 * Copyright (c) 2016 Carnegie Mellon University.
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

package edu.cmu.emfta.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.business.api.action.AbstractExternalJavaAction;
import org.eclipse.sirius.diagram.business.internal.metamodel.spec.DNodeSpec;
import org.eclipse.sirius.diagram.business.internal.metamodel.spec.DSemanticDiagramSpec;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import edu.cmu.emfta.EmftaFactory;
import edu.cmu.emfta.Event;
import edu.cmu.emfta.FTAModel;
import edu.cmu.emfta.Gate;
import edu.cmu.emfta.GateType;



public class ExpandAction extends AbstractExternalJavaAction {
	
	@Override
	public void execute(Collection<? extends EObject> selections, Map<String, Object> parameters) {

//		System.out.println("[CutSetAction] calling execute");
		for (EObject eo : selections) {
			EObject target = null;


			if (eo instanceof DSemanticDiagramSpec) {
				DSemanticDiagramSpec ds = (DSemanticDiagramSpec) eo;
				target = ds.getTarget();
			}

			if (eo instanceof DNodeSpec) {
				DNodeSpec ds = (DNodeSpec) eo;
				target = ds.getTarget();
			}

			if (target != null) {
				System.out.println("[ExpandAction] Expand from event = " + target);
				
				try
				{
					new OptimizeLogic((Event)target, false, false, true).perform();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
				Utils.refreshProject(target);
				return;
			}

			MessageBox dialog = new MessageBox(Display.getDefault().getActiveShell(), SWT.ERROR | SWT.ICON_ERROR);
			dialog.setText("Error");
			dialog.setMessage("Please select an event in the FTA tree");

			dialog.open();

		}
	}


	@Override
	public boolean canExecute(Collection<? extends EObject> selections) {

		/**
		 * For now, we return true all the time. Might need to optimize
		 * it to make it more user-friendly.
		 */
		for (EObject eo : selections) {

			if (eo instanceof DSemanticDiagramSpec) {
				DSemanticDiagramSpec ds = (DSemanticDiagramSpec) eo;
				EObject target = ds.getTarget();

			}

			if (eo instanceof DNodeSpec) {
				DNodeSpec ds = (DNodeSpec) eo;
				EObject target = ds.getTarget();

				if (target instanceof edu.cmu.emfta.Event) {
					return true;
				}

				if (target instanceof edu.cmu.emfta.Tree) {
					return true;
				}

				if (target instanceof edu.cmu.emfta.FTAModel) {
					return true;
				}
			}

		}
		return false;
	}

}
