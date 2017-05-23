/**
 * Copyright 2015 Carnegie Mellon University. All Rights Reserved.
 *
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING INSTITUTE
 * MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO
 * WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER INCLUDING,
 * BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE OR MERCHANTABILITY,
 * EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE MATERIAL. CARNEGIE MELLON
 * UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM FROM
 * PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * Released under the Eclipse Public License (http://www.eclipse.org/org/documents/epl-v10.php)
 *
 * See COPYRIGHT file for full details.
 */

package org.osate.aadl2.errormodel.emfta.util;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.ui.business.api.dialect.DialectUIManager;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.sirius.viewpoint.description.RepresentationDescription;
import org.eclipse.sirius.viewpoint.description.Viewpoint;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.ui.util.ResourceUtil;
import org.osate.aadl2.modelsupport.resources.OsateResourceUtil;
import org.osate.aadl2.util.OsateDebug;

public class SiriusViewHandler extends AbstractHandler {

	private IWorkbenchWindow window;
	private ExecutionEvent executionEvent;

	protected String getViewPoint() {
		return "viewpoint:/emfta.design/EMFTA";
	}

	protected String getRepresentation() {
		return "Tree.diagram";
	}

	protected String getPrintName(EObject obj) {
		Object res = obj.eGet(obj.eClass().getEStructuralFeature("name"));
		if (res != null) {
			return (String) res;
		}
		return obj.eResource().getURI().trimFileExtension().toString();
	}

	protected ExecutionEvent getExecutionEvent() {
		return this.executionEvent;
	}

	@Override
	public Object execute(ExecutionEvent event) {
		IFile object = getFile(HandlerUtil.getCurrentSelection(event));
		this.executionEvent = event;
		if (object == null) {
			return null;
		}
		if (object instanceof IFile) {
			Resource res = OsateResourceUtil.getResource((IResource) object);
			EList<EObject> rl = res.getContents();
			if (!rl.isEmpty()) {
				return runJob(rl.get(0), new NullProgressMonitor());
			}
		}

		window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window == null) {
			return null;
		}

		return null;
	}

	private IFile getFile(ISelection currentSelection) {
		if (currentSelection instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) currentSelection;
			if (iss.size() == 1) {
				return (IFile) iss.getFirstElement();
			}
		}
		return null;
	}

	protected IWorkbenchWindow getWindow() {
		return window;
	}

	protected String getJobName() {
		return "Open Sirius View";
	}

	protected IStatus runJob(EObject ftamodel, IProgressMonitor monitor) {
		URI newURI = EcoreUtil.getURI(ftamodel);
		if (newURI != null) {
			autoOpenModel(newURI, ResourceUtil.getFile(ftamodel.eResource()).getProject());
			monitor.done();
			return Status.OK_STATUS;
		}
		return Status.OK_STATUS;
	}

	public void autoOpenModel(final URI newURI, final IProject activeProject) {

		try {

			Job ftaTreeCreationJob = new Job(getJobName()) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {

					monitor.beginTask(getJobName(), 100);

					createAndOpenSiruisView(activeProject, newURI, monitor);
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
	private void createAndOpenSiruisView(final IProject project, final URI ftamodelUri, IProgressMonitor monitor) {
		SiriusUtil util = SiriusUtil.INSTANCE;
		URI emftaViewpointURI = URI.createURI(getViewPoint());

		URI semanticResourceURI = URI.createPlatformResourceURI(ftamodelUri.toPlatformString(true), true);
		Session existingSession = util.getSessionForProjectAndResource(project, semanticResourceURI, monitor);
		if (existingSession == null) {
			// give it a second try. null was returned the first time due to a class cast exception at the end of
			// setting the Modeling perspective.
			existingSession = util.getSessionForProjectAndResource(project, semanticResourceURI, monitor);
		}
		if (existingSession != null) {
			util.saveSession(existingSession, monitor);
			ResourceSetImpl resset = new ResourceSetImpl();
			EObject model = getModelFromSession(existingSession, semanticResourceURI);
			// XXX this next piece of code tries to compensate for a bug in Sirius where it cannot find the model
			// It should be there since the getSessionForProjectandResource would have put it there.
			if (model == null) {
				OsateDebug.osateDebug(
						"Could not find semantic resource in session for URI " + semanticResourceURI.path());
				model = resset.getEObject(ftamodelUri, true);
			}
			if (model == null) {
				OsateDebug.osateDebug("Could not find model for URI " + ftamodelUri.path());
				return;
			}
			final Viewpoint emftaVP = util.getViewpointFromRegistry(emftaViewpointURI);
			final RepresentationDescription description = util.getRepresentationDescription(emftaVP,
					getRepresentation());
			String modelRootName = getPrintName(model);
			String representationName = modelRootName + " " + getRepresentation();
			final DRepresentation rep = util.findRepresentation(existingSession, emftaVP, description,
					representationName);
			if (rep != null) {
				DialectUIManager.INSTANCE.openEditor(existingSession, rep, new NullProgressMonitor());
			} else {
				try {
					util.createAndOpenRepresentation(existingSession, emftaVP, description, representationName, model,
							monitor);
				} catch (Exception e) {
					OsateDebug.osateDebug("Could not create and open model " + modelRootName);
					return;
				}
			}

		}
	}

	/**
	 * Retrieves a Model instance from a semantic resource
	 * The model element must be one of the root objects in the specified semantic resource
	 * @param session
	 * @param uri
	 * @return
	 */
	private EObject getModelFromSession(Session session, URI uri) {
		Resource resource = SiriusUtil.INSTANCE.getResourceFromSession(session, uri);
		if (resource != null) {
			for (EObject object : resource.getContents()) {
				return object;
			}
		}
		return null;
	}

}
