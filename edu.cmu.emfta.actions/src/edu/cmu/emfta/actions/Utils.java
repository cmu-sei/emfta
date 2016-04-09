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

package edu.cmu.emfta.actions;

import java.io.PrintWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.ui.util.ResourceUtil;

import edu.cmu.emfta.Event;
import edu.cmu.emfta.Gate;

public class Utils {
	public static void refreshWorkspace(IProgressMonitor monitor) {
		for (IProject ip : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			try {
				ip.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static IPath getPath(final URI resourceURI) {
		/*
		 * I don't really understand why this method does what it does, but
		 * the point seems to be to take a URI for a Resource that resembles
		 * "platform:/resource/xxx/yyy/zzz" and return the Eclipse IPath to
		 * the file for that Resource. This seems to involve removing the
		 * "/resource/" part.
		 * 
		 * --aarong
		 */

		// Is it a "plaform:" uri?
		if (resourceURI.scheme() != null && resourceURI.scheme().equalsIgnoreCase("platform")) {
			// Get the segments. See if the first is "resource"
			final String[] segments = resourceURI.segments();
			final StringBuffer path = new StringBuffer();

			if (segments.length >= 1) {
				final int firstSegment = segments[0].equals("resource") ? 1 : 0;
				final int lastIdx = segments.length - 1;
				for (int i = firstSegment; i < (lastIdx); i++) {
					path.append(segments[i]);
					path.append('/');
				}
				if (lastIdx >= 0) {
					path.append(segments[lastIdx]);
				}
			}
			return new Path(null, path.toString());
		} else if (resourceURI.isFile()) {
			return new Path(resourceURI.toFileString());
		} else {
			throw new IllegalArgumentException("Cannot decode URI protocol: " + resourceURI.scheme());
		}
	}

	public static void checkProbability(Event event) {
		Gate gate = event.getGate();
		IResource res = getResourceForModel(event);

		double result = 0;

		if (gate == null) {
			if (event.getProbability() == 0) {
				IMarker marker;
				String msg = "Probability for leaf event " + event.getName() + " is zero";
				try {
					marker = res.createMarker(IMarker.PROBLEM);
					marker.setAttribute(IMarker.MESSAGE, msg);
					marker.setAttribute(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
					marker.setAttribute(Activator.EMFTA_MARKER, "true");
				} catch (CoreException e) {
					throw new RuntimeException(e);
				}
			}
			return;
		}

		result = getProbability(event);

		if (result != event.getProbability()) {

//			System.out.println("[Utils] here");
			IMarker marker;
			String msg = "Probability mismatch for event " + event.getName() + "declared=" + event.getProbability()
					+ ";actual=" + result;
			try {
				marker = res.createMarker(IMarker.PROBLEM);
				marker.setAttribute(IMarker.MESSAGE, msg);
				marker.setAttribute(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
				marker.setAttribute(Activator.EMFTA_MARKER, "true");
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}

//			System.out.println("[Utils] " + msg);
		}
	}

	/**
	 * For leaf event it returns the probability stored with the event.
	 * For non-leaf events (events with a gate) it recursively calculates the probability from subevents.
	 * @param event
	 * @return double probability
	 */
	public static double getProbability(Event event) {
		Gate gate = event.getGate();
		double result;

		if (gate != null) {
			switch (gate.getType()) {
			case AND: {
				result = 1;
				for (Event subEvent : gate.getEvents()) {
					result = result * getProbability(subEvent);
				}
				break;
			}
			case PRIORITY_AND: {
				// TODO need to adjust for ordered events
				result = 1;
				for (Event subEvent : gate.getEvents()) {
					result = result * getProbability(subEvent);
				}
				break;
			}
			case XOR: {
				double inverseProb = 1;
				for (Event subEvent : gate.getEvents()) {
					inverseProb *= (1 - getProbability(subEvent));
				}
				result = 1 - inverseProb;
				break;
			}
			case OR: {
				result = 0;
				for (Event subEvent : gate.getEvents()) {
					result = result + getProbability(subEvent);
				}
				break;
			}
			case INTERMEDIATE: {
				result = 0;
				for (Event subEvent : gate.getEvents()) {
					result = result + getProbability(subEvent);
				}
				break;
			}
			default: {
				System.out.println("[Utils] Unsupported for now");
				result = -1;
				break;
			}
			}
			System.out.println("[Utils] Probability for " + event.getName() + ":" + result);

		} else {
			result = event.getProbability();
		}
		return result;
	}

	/**
	 * return sum of probabilities of direct subevents.
	 * @param event
	 * @return double
	 */
	public static double getSubeventProbabilities(Event event) {
		Gate gate = event.getGate();
		double result;

		if (gate != null) {
			switch (gate.getType()) {
			case AND: {
				result = 1;
				for (Event subEvent : gate.getEvents()) {
					result = result * subEvent.getProbability();
				}
				break;
			}
			case PRIORITY_AND: {
				// TODO need to adjust for ordered events
				result = 1;
				for (Event subEvent : gate.getEvents()) {
					result = result * subEvent.getProbability();
				}
				break;
			}
			case XOR: {
				double inverseProb = 1;
				for (Event subEvent : gate.getEvents()) {
					inverseProb *= (1 - subEvent.getProbability());
				}
				result = 1 - inverseProb;
				break;
			}
			case OR: {
				result = 0;
				for (Event subEvent : gate.getEvents()) {
					result = result + subEvent.getProbability();
				}
				break;
			}
			case INTERMEDIATE: {
				result = 0;
				for (Event subEvent : gate.getEvents()) {
					result = result + subEvent.getProbability();
				}
				break;
			}
			default: {
				System.out.println("[Utils] Unsupported for now");
				result = -1;
				break;
			}
			}
			System.out.println("[Utils] Probability for " + event.getName() + ":" + result);

		} else {
			result = event.getProbability();
		}
		return result;
	}

	/**
	 * Get the IResource object associated with the EObject
	 * @param obj
	 * @return - the IResource associated with this EObject
	 */
	public static IResource getResourceForModel(EObject obj) {
		URI uri = obj.eResource().getURI();
		// assuming platform://resource/project/path/to/file
		String project = uri.segment(1);
		IPath path = new Path(uri.path()).removeFirstSegments(2);
		return ResourcesPlugin.getWorkspace().getRoot().getProject(project).findMember(path);
	}

	/**
	 * Refresh project associated with the eobject given as parameter
	 * @param obj - the eobject associated with the project
	 */
	public static void refreshProject(EObject obj) {
		URI uri = obj.eResource().getURI();
		// assuming platform://resource/project/path/to/file
		String project = uri.segment(1);
		IPath path = new Path(uri.path()).removeFirstSegments(2);
		IProject activeProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
		try {
			activeProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Remove all Markers for the eobject passed as parameter
	 * @param obj - the object associated with all the markers
	 */
	public static void removeAllMarkers(EObject obj) {
		IResource res = getResourceForModel(obj);
		try {
			res.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			System.out.println("[Utils] Try to delete marker for object " + obj);

			e.printStackTrace();
		}
	}

	public static void writeFile(StringBuffer sb, EObject eobj) {
//		OsateDebug.osateDebug("[EMFTAAction]", "serializeReqSpecModel activeProject=" + activeProject);
		IProject activeProject = ResourceUtil.getFile(eobj.eResource()).getProject();
		String filename;

		filename = eobj.eResource().getURI().lastSegment();
		filename = filename.replaceAll("emfta", "");
		filename = filename + "-probability.csv";

		IFile newFile = activeProject.getFile(filename);
//		OsateDebug.osateDebug("[EMFTAAction]", "save in file=" + newFile.getName());

		try {

//			ResourceSet set = new ResourceSetImpl();
//			Resource res = set.createResource(URI.createURI(newFile.toString()));
			PrintWriter out = new PrintWriter(newFile.getRawLocation().toOSString());

			out.write(sb.toString());
			out.close();

			//
//			java.io.File file = newFile.getRawLocation().toFile();
//			InputStream stream = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
//			
//			file.createNewFile();
//			file.
//			file.create(stream, true, null);
//			res.getContents().add(emftaModel);

//			FileOutputStream fos = new FileOutputStream(newFile.getRawLocation().toFile());

//			fos.write(sb.toString().getBytes(), 0, sb.toString().getBytes().length);
//			res.save(fos, null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
