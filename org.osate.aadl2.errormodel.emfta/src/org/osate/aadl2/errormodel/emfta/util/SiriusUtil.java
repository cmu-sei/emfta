package org.osate.aadl2.errormodel.emfta.util;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sirius.business.api.componentization.ViewpointRegistry;
import org.eclipse.sirius.business.api.modelingproject.AbstractRepresentationsFileJob;
import org.eclipse.sirius.business.api.modelingproject.ModelingProject;
import org.eclipse.sirius.business.api.query.DViewQuery;
import org.eclipse.sirius.business.api.query.ViewpointQuery;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.business.api.session.SessionStatus;
import org.eclipse.sirius.ext.base.Option;
import org.eclipse.sirius.ui.business.api.dialect.DialectUIManager;
import org.eclipse.sirius.ui.business.api.session.IEditingSession;
import org.eclipse.sirius.ui.business.api.session.SessionUIManager;
import org.eclipse.sirius.ui.business.api.viewpoint.ViewpointSelectionCallback;
import org.eclipse.sirius.ui.tools.api.project.ModelingProjectManager;
import org.eclipse.sirius.ui.tools.api.views.ViewHelper;
import org.eclipse.sirius.ui.tools.internal.actions.creation.CreateRepresentationAction;
import org.eclipse.sirius.ui.tools.internal.views.common.SessionLabelProvider;
import org.eclipse.sirius.ui.tools.internal.views.common.modelingproject.OpenRepresentationsFileJob;
import org.eclipse.sirius.viewpoint.DAnalysis;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.sirius.viewpoint.DView;
import org.eclipse.sirius.viewpoint.description.RepresentationDescription;
import org.eclipse.sirius.viewpoint.description.Viewpoint;
import org.eclipse.swt.widgets.Display;
import org.osate.aadl2.util.OsateDebug;

/**
 * Utilities around Sirius, sessions and representations
 * 
 * @author St�phane Thibaudeau <stephane.thibaudeau@obeo.fr>
 *
 */
@SuppressWarnings("restriction")
public class SiriusUtil {
	public static SiriusUtil INSTANCE = new SiriusUtil();

	private SiriusUtil() {
		super();
	}

	/**
	 * 
	 * @param session
	 * @param monitor
	 */
	public void saveSession(Session session, IProgressMonitor monitor) {
		if (SessionStatus.DIRTY.equals(session.getStatus())) {
			session.save(monitor);
		}
	}

	public Viewpoint getViewpoint(final Session session, URI viewpointURI, final IProgressMonitor monitor) {
		Viewpoint result = getViewpointFromSession(session, viewpointURI);
		if (result != null)
			return result;
		ViewpointRegistry registry = ViewpointRegistry.getInstance();
		try {
			final Viewpoint regViewpoint = registry.getViewpoint(viewpointURI);
			// make sure it is selected
			if (regViewpoint != null) {

				IEditingSession uiSession = SessionUIManager.INSTANCE.getOrCreateUISession(session);
				uiSession.open();

				// Ensure the viewpoint is selected for the session
				session.getTransactionalEditingDomain().getCommandStack()
						.execute(new RecordingCommand(session.getTransactionalEditingDomain()) {

							@Override
							protected void doExecute() {

								// Check if already selected
								Collection<Viewpoint> sel = session.getSelectedViewpoints(false);
								if (!session.getSelectedViewpoints(false).contains(regViewpoint)) {
									ViewpointSelectionCallback selection = new ViewpointSelectionCallback();
									selection.selectViewpoint(regViewpoint, session, monitor);
								}
							}
						});
			}
		} catch (Exception e) {
			// Unable to retrieve viewpoint
		}
		return getViewpointFromSession(session, viewpointURI);
	}

	public Viewpoint getViewpointFromSession(Session session, URI viewpointURI) {
		String viewpointName = viewpointURI.lastSegment();
		Collection<Viewpoint> viewpoints = session.getSelectedViewpoints(false);
		for (Viewpoint viewpoint : viewpoints) {
			if (viewpointName.endsWith(viewpoint.getName())) {
				return viewpoint;
			}
		}
		return null;
	}

	/**
	 * Retrieves a viewpoint from its URI
	 * @param viewpointURI
	 * @return Viewpoint from the viewpoints registry
	 */
	public Viewpoint getViewpointFromRegistry(URI viewpointURI) {
		ViewpointRegistry registry = ViewpointRegistry.getInstance();
		try {
			return registry.getViewpoint(viewpointURI);
		} catch (Exception e) {
			// Unable to retrieve viewpoint
		}
		return null;
	}

	/**
	 * Retrieves a representation description
	 * @param viewpoint
	 * @param id Id of the representation description (the id is set in the odesign model)
	 * @return
	 */
	public RepresentationDescription getRepresentationDescription(Viewpoint viewpoint, String id) {
		for (RepresentationDescription description : new ViewpointQuery(viewpoint).getAllRepresentationDescriptions()) {
			if (id.equals(description.getName())) {
				return description;
			}
		}
		return null;
	}

	public DRepresentation findRepresentation(final Session existingSession, final Viewpoint viewpoint,
			final RepresentationDescription description, final String representationName) {

		// Step 2: get the DRepresentation to open
		DAnalysis root = (DAnalysis) existingSession.getSessionResource().getContents().get(0);
		for (DView dView : root.getOwnedViews()) {
			if (dView.getViewpoint().getName().equals(viewpoint.getName())) {
				DViewQuery q = new DViewQuery(dView);
				List<DRepresentation> myRepresentations = q.getLoadedRepresentations();
				for (DRepresentation dRepresentation : myRepresentations) {
					if (representationName.equalsIgnoreCase(dRepresentation.getName())) {
						return dRepresentation;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Creates and opens a representation on the specified object 
	 * @param existingSession Sirirus session
	 * @param viewpoint Viewpoint containing the representation description
	 * @param description Representation description used to indicate which kind of representation to create
	 * @param representationName Name of the new representation
	 * @param object Semantic object
	 * @param monitor Progress monitor
	 */
	public void createAndOpenRepresentation(final Session existingSession, final Viewpoint viewpoint,
			final RepresentationDescription description, final String representationName, final EObject object,
			final IProgressMonitor monitor) {

		// Create and open the representation
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				CreateRepresentationAction action = new CreateRepresentationAction(existingSession, object, description,
						new SessionLabelProvider(ViewHelper.INSTANCE.createAdapterFactory())) {
					@Override
					protected String getRepresentationName() {
						return representationName;
					}
				};
				action.run();
			}
		});
	}

	/**
	 * Returns a session for the project. The session is loaded and references the URI as a semantid resource
	 * @param project
	 * @param semanticResourceURI
	 * @param monitor
	 * @return loaded session or null
	 */
	public Session getSessionForProjectAndResource(IProject project, URI semanticResourceURI,
			IProgressMonitor monitor) {
		Session existingSession = getSessionForSemanticURI(semanticResourceURI);

		if (existingSession == null) {
			// Add "Modeling" nature to project
			if (!ModelingProject.hasModelingProjectNature(project)) {
				try {
					ModelingProjectManager.INSTANCE.convertToModelingProject(project, monitor);
				} catch (Exception e) {
					// Error while converting to modeling project
					// throws a class cast exception on a list.
					// XXX not returning null is ok.
					return null;
				}
			}

			final Option<ModelingProject> prj = ModelingProject.asModelingProject(project);
			if (prj.some()) {
				ModelingProject modelingProject = prj.get();
				existingSession = modelingProject.getSession();

				if (existingSession == null) {
					loadSession(modelingProject, monitor);
					waitWhileSessionLoads(monitor);
					existingSession = modelingProject.getSession();
				}
				if (existingSession != null) {
					addSemanticResource(existingSession, semanticResourceURI, monitor);
				}

				return existingSession;
			}
		}
		return existingSession;
	}

	/**
	 * Adds a resource as a semantic resource for the session
	 * @param session
	 * @param semanticResourceURI
	 * @param monitor
	 */
	private void addSemanticResource(final Session session, final URI semanticResourceURI,
			final IProgressMonitor monitor) {
		// Check whether we really have to add the resource
		Resource resource = getResourceFromSession(session, semanticResourceURI);
		if (resource == null) {
			// We really have to add it
			TransactionalEditingDomain ted = session.getTransactionalEditingDomain();
			ted.getCommandStack().execute(new RecordingCommand(ted) {

				@Override
				protected void doExecute() {
					session.addSemanticResource(semanticResourceURI, monitor);

				}
			});
		}
	}

	/**
	 * Retrieves the Sirius session referencing the URI as a semantic resource
	 * 
	 * @param uri URI of the potential semantic resource
	 * @return the Sirius session or null if no corresponding session was found
	 */
	public Session getSessionForSemanticURI(URI uri) {
		for (Session session : SessionManager.INSTANCE.getSessions()) {
			ResourceSet set = session.getTransactionalEditingDomain().getResourceSet();
			// Loop on resources to check if the URI matches one of the semantic resources
			for (Resource res : set.getResources()) {
				if (res.getURI() != null) {
					if (set.getURIConverter().normalize(res.getURI()).equals(uri)) {
						return session;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Loads a Sirius session for a modeling project
	 * @param project
	 * @param monitor
	 */
	private void loadSession(ModelingProject project, IProgressMonitor monitor) {
		Option<URI> optionalMainSessionFileURI = project.getMainRepresentationsFileURI(monitor, false, false);
		if (optionalMainSessionFileURI.some()) {
			// Load the main representations file of this modeling
			// project if it's not already loaded or during loading.
			ModelingProjectManager.INSTANCE.loadAndOpenRepresentationsFile(optionalMainSessionFileURI.get());
		}
	}

	/**
	 * Waits until all sessions are loaded
	 * @param monitor
	 */
	private void waitWhileSessionLoads(IProgressMonitor monitor) {
		if (OpenRepresentationsFileJob.shouldWaitOtherJobs()) {
			// We are loading session(s), wait loading is finished
			// before continuing.
			try {
				Job.getJobManager().join(AbstractRepresentationsFileJob.FAMILY, monitor);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
	}

	/**
	 * Returns a session's semantic resource from its URI 
	 * @param session
	 * @param uri
	 * @return the semantic resource or null if the session references no resource with the specified URI
	 */
	public Resource getResourceFromSession(Session session, URI uri) {
		ResourceSet set = session.getTransactionalEditingDomain().getResourceSet();
		for (Resource resource : session.getSemanticResources()) {
			if (resource.getURI() != null) {
				if (set.getURIConverter().normalize(resource.getURI()).equals(uri)) {
					return resource;
				}
			}
		}
		return null;
	}

	public String getPrintName(EObject obj) {
		Object res = obj.eGet(obj.eClass().getEStructuralFeature("name"));
		if (res != null) {
			return (String) res;
		}
		return obj.eResource().getURI().trimFileExtension().toString();
	}

	/**
	 * Creates and opens a FTA Tree on the specified resource
	 * @param project
	 * @param resourceUri
	 * @param monitor
	 */
	public void createAndOpenSiruisView(final URI ftamodelUri, final IProject project, String viewPoint,
			String representation, IProgressMonitor monitor) {
		URI viewpointURI = URI.createURI(viewPoint);

		URI semanticResourceURI = URI.createPlatformResourceURI(ftamodelUri.toPlatformString(true), true);
		Session existingSession = getSessionForProjectAndResource(project, semanticResourceURI, monitor);
		if (existingSession == null) {
			// give it a second try. null was returned the first time due to a class cast exception at the end of
			// setting the Modeling perspective.
			existingSession = getSessionForProjectAndResource(project, semanticResourceURI, monitor);
		}
		if (existingSession != null) {
			saveSession(existingSession, monitor);
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
			final Viewpoint emftaVP = getViewpoint(existingSession, viewpointURI, monitor);
			final RepresentationDescription description = getRepresentationDescription(emftaVP, representation);
			String modelRootName = getPrintName(model);
			String representationName = modelRootName + " " + representation;
			final DRepresentation rep = findRepresentation(existingSession, emftaVP, description, representationName);
			if (rep != null) {
				DialectUIManager.INSTANCE.openEditor(existingSession, rep, monitor);
			} else {
				try {
					createAndOpenRepresentation(existingSession, emftaVP, description, representationName, model,
							monitor);
				} catch (Exception e) {
					OsateDebug.osateDebug("Could not create and open model " + modelRootName);
					return;
				}
			}
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch (CoreException e) {
				// Error while refreshing the project
			}
		}
	}

	public void autoOpenModel(final URI newURI, final IProject activeProject, final String viewPoint,
			final String representation, final String jobName) {

		try {

			Job ftaTreeCreationJob = new Job(jobName) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {

					monitor.beginTask(jobName, 100);

					createAndOpenSiruisView(newURI, activeProject, viewPoint, representation, monitor);
//					}
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
