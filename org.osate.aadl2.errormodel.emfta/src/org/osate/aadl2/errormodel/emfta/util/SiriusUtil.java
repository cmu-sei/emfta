package org.osate.aadl2.errormodel.emfta.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sirius.business.api.componentization.ViewpointRegistry;
import org.eclipse.sirius.business.api.modelingproject.AbstractRepresentationsFileJob;
import org.eclipse.sirius.business.api.modelingproject.ModelingProject;
import org.eclipse.sirius.business.api.query.ViewpointQuery;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.ext.base.Option;
import org.eclipse.sirius.ui.business.api.session.IEditingSession;
import org.eclipse.sirius.ui.business.api.session.SessionUIManager;
import org.eclipse.sirius.ui.business.api.viewpoint.ViewpointSelectionCallback;
import org.eclipse.sirius.ui.tools.api.project.ModelingProjectManager;
import org.eclipse.sirius.ui.tools.api.views.ViewHelper;
import org.eclipse.sirius.ui.tools.internal.actions.creation.CreateRepresentationAction;
import org.eclipse.sirius.ui.tools.internal.views.common.SessionLabelProvider;
import org.eclipse.sirius.ui.tools.internal.views.common.modelingproject.OpenRepresentationsFileJob;
import org.eclipse.sirius.viewpoint.description.RepresentationDescription;
import org.eclipse.sirius.viewpoint.description.Viewpoint;
import org.eclipse.swt.widgets.Display;

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
	
	/**
	 * Creates and opens a representation on the specified object 
	 * @param existingSession Sirirus session
	 * @param viewpoint Viewpoint containing the representation description
	 * @param description Representation description used to indicate which kind of representation to create
	 * @param representationName Name of the new representation
	 * @param object Semantic object
	 * @param monitor Progress monitor
	 */
	public void createAndOpenRepresentation(final Session existingSession, final Viewpoint viewpoint, final RepresentationDescription description, final String representationName, final EObject object, final IProgressMonitor monitor) {
		if (viewpoint != null) {
		
		IEditingSession uiSession = SessionUIManager.INSTANCE.getOrCreateUISession(existingSession);
		uiSession.open();
		
		// Ensure the viewpoint is selected for the session
		existingSession.getTransactionalEditingDomain().getCommandStack()
				.execute(new RecordingCommand(existingSession.getTransactionalEditingDomain()) {

					@Override
					protected void doExecute() {
						
						// Check if already selected
						if (!existingSession.getSelectedViewpoints(false).contains(viewpoint)) {
							ViewpointSelectionCallback selection = new ViewpointSelectionCallback();
							selection.selectViewpoint(viewpoint, existingSession, monitor);
						}
					}
				});
		}
		
		// Create and open the representation
        Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				CreateRepresentationAction action = new CreateRepresentationAction(
						existingSession,
						object,
						description,
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
	public Session getSessionForProjectAndResource(IProject project, URI semanticResourceURI, IProgressMonitor monitor) {
		Session existingSession = getSessionForSemanticURI(semanticResourceURI);
		
		if (existingSession == null) {
			// Add "Modeling" nature to project
			if (!ModelingProject.hasModelingProjectNature(project)) {
				try {
					ModelingProjectManager.INSTANCE.convertToModelingProject(project, monitor);
				} catch (CoreException e) {
					// Error while converting to modeling project
					return null;
				}				
			}
	
			final Option<ModelingProject> prj = ModelingProject.asModelingProject(project);
			if (prj.some()) {
				ModelingProject modelingProject = prj.get();
				existingSession = modelingProject.getSession();
				
				if (existingSession == null) {
					loadSession(modelingProject, monitor);
				}
				waitWhileSessionLoads(monitor);
				existingSession = modelingProject.getSession();
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
	private void addSemanticResource(final Session session, final URI semanticResourceURI, final IProgressMonitor monitor) {
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

}
