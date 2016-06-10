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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.xtext.aadl2.errormodel.errorModel.ConditionExpression;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorEvent;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorPropagation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorSource;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorTypes;
import org.osate.xtext.aadl2.errormodel.errorModel.OutgoingPropagationCondition;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeSet;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;
import org.osate.xtext.aadl2.errormodel.util.PropagationGraphBackwardTraversal;

import edu.cmu.emfta.EmftaFactory;
import edu.cmu.emfta.Event;
import edu.cmu.emfta.EventType;
import edu.cmu.emfta.FTAModel;
import edu.cmu.emfta.Gate;
import edu.cmu.emfta.GateType;

public class EMFTAGenerator extends PropagationGraphBackwardTraversal {
	private edu.cmu.emfta.FTAModel emftaModel;
	private ErrorBehaviorState rootComponentState;
	private ErrorPropagation rootComponentPropagation;
	private ErrorTypes rootComponentTypes;
	private int eventIdentifier;
	private boolean fullTree = false;

	public Map<String, edu.cmu.emfta.Event> cache;

	public EMFTAGenerator(ComponentInstance root, ErrorBehaviorState errorState, ErrorTypes errorTypes) {
		super(root);
		emftaModel = null;
		cache = new HashMap<String, edu.cmu.emfta.Event>();
		rootComponentTypes = errorTypes;
		rootComponentState = errorState;
		rootComponentPropagation = null;
		eventIdentifier = 0;
	}

	public EMFTAGenerator(ComponentInstance root, ErrorPropagation errorPropagation, ErrorTypes errorTypes) {
		// TOFIX
		super(root);
		emftaModel = null;
		cache = new HashMap<String, edu.cmu.emfta.Event>();
		rootComponentTypes = errorTypes;
		rootComponentPropagation = errorPropagation;
		rootComponentState = null;
		eventIdentifier = 0;
	}

	public FTAModel getEmftaModel(boolean fullTree) {
		this.fullTree = fullTree;
		return getEmftaModel();
	}

	public FTAModel getEmftaModel() {
		if (emftaModel == null) {
			edu.cmu.emfta.Event emftaRootEvent;

			NamedElement ne = rootComponentState != null ? rootComponentState : rootComponentPropagation;
			emftaModel = EmftaFactory.eINSTANCE.createFTAModel();
			emftaModel.setName(buildIdentifier(getRootComponent(), ne, rootComponentTypes));
			emftaModel.setDescription("Top Level Failure");

			if (rootComponentState != null) {
				emftaRootEvent = (Event) traverseCompositeErrorState(getRootComponent(), rootComponentState,
						rootComponentTypes);
			} else {
				emftaRootEvent = (Event) traverseOutgoingErrorPropagation(getRootComponent(), rootComponentPropagation,
						rootComponentTypes);
			}
			if (emftaRootEvent == null) {
				emftaRootEvent = createIntermediateEvent(getRootComponent(), ne, rootComponentTypes);
			}
			String longName = buildName(getRootComponent(), ne, rootComponentTypes);
			if (emftaRootEvent.getGate() == null && !emftaRootEvent.getName().equals(longName)) {
				Gate top = EmftaFactory.eINSTANCE.createGate();
				top.setType(GateType.INTERMEDIATE);
				top.getEvents().add(emftaRootEvent);
				Event topEvent = createIntermediateEvent(getRootComponent(), ne, rootComponentTypes);
				topEvent.setGate(top);
				emftaRootEvent = topEvent;
			}
			emftaModel.setRoot(emftaRootEvent);
			if (!fullTree) {
				emftaModel.setRoot(optimizeGates(emftaModel.getRoot()));
			}
			emftaModel.getRoot().setName(longName);
			cleanupReferenceCounts(emftaModel);
		}
		return emftaModel;
	}

	private String buildName(ComponentInstance component, NamedElement namedElement, ErrorTypes type) {
		return buildIdentifier(component, namedElement, type);
//		
//		String name = eventIdentifier + "-" + buildIdentifier(component, namedElement, type);
//		eventIdentifier = eventIdentifier + 1;
//		return name;
	}

	private String buildIdentifier(ComponentInstance component, NamedElement namedElement, ErrorTypes type) {
		String identifier;

		identifier = component instanceof SystemInstance
				? component.getComponentClassifier().getQualifiedName().replaceAll("::", "_").replaceAll("\\.", "_")
				: component.getComponentInstancePath();
		identifier += "-";

		if (namedElement == null) {
			identifier += "unidentified";

		} else {
			identifier += EMV2Util.getPrintName(namedElement);
		}

		if (type == null) {
//			identifier+="-notypes";
		} else if (type.getName() != null) {
			identifier += "-" + type.getName();
		} else {
			identifier += "-" + EMV2Util.getPrintName(type);
		}
		identifier = identifier.replaceAll("\\{", "").replaceAll("\\}", "").toLowerCase();
		return identifier;
	}

	/**
	 * recomputes reference count and removes unused Events
	 * reference counts > 1 on Event identifies common cause events.
	 * @param ftamodel
	 */
	private void cleanupReferenceCounts(FTAModel ftamodel) {
		Event root = ftamodel.getRoot();
		EList<Event> eventlist = ftamodel.getEvents();
		resetReferenceCounts(eventlist);
		updateReferenceCounts(root);
		removeOrphans(eventlist);
	}

	private void resetReferenceCounts(EList<Event> events) {
		for (Event event : events) {
			event.setReferenceCount(0);
		}
	}

	private void removeOrphans(EList<Event> events) {
		EList<Event> orphans = new BasicEList<Event>();
		for (Event event : events) {
			if (event.getReferenceCount() == 0) {
				orphans.add(event);
			}
		}
		events.removeAll(orphans);
	}

	private void updateReferenceCounts(Event ev) {
		incrementReferenceCount(ev);
		if (ev.getGate() != null) {
			EList<Event> events = ev.getGate().getEvents();
			for (Event event : events) {
				updateReferenceCounts(event);
			}
		}
	}

	private void incrementReferenceCount(Event ev) {
		ev.setReferenceCount(ev.getReferenceCount() + 1);
	}

	/**
	 * create a BASIC event with the specified component, error model element, and type name
	 * @param component
	 * @param namedElement
	 * @param type
	 * @return Event
	 */
	private Event createBasicEvent(ComponentInstance component, NamedElement namedElement, ErrorTypes type) {
		String name = buildName(component, namedElement, type);
		Event result = findEvent(name);
		if (!fullTree && result != null)
			return result;
		Event newEvent = EmftaFactory.eINSTANCE.createEvent();
		emftaModel.getEvents().add(newEvent);
		newEvent.setName(name);
		newEvent.setType(EventType.BASIC);
		newEvent.setReferenceCount(1);
		return newEvent;
	}

	/**
	 * create a INTERMEDIATE event with the specified component, error model element, and type name
	 * @param component
	 * @param namedElement
	 * @param type
	 * @return Event
	 */
	private Event createIntermediateEvent(ComponentInstance component, NamedElement namedElement, ErrorTypes type) {
		String name = buildName(component, namedElement, type);
		Event result = findEvent(name);
		if (!fullTree && result != null)
			return result;
		Event newEvent = EmftaFactory.eINSTANCE.createEvent();
		emftaModel.getEvents().add(newEvent);
		newEvent.setName(name);
		newEvent.setType(EventType.INTERMEDIATE);
		newEvent.setReferenceCount(1);
		return newEvent;
	}

	/**
	 * create a generic intermediate Event
	 * @return
	 */
	private int count = 0;

	private Event createIntermediateEvent(String eventname) {
		if (eventname.isEmpty()) {
			eventname = "Intermediate" + count++;
		} else {
			Event result = findEvent(eventname);
			if (!fullTree && result != null)
				return result;
		}
		Event newEvent = EmftaFactory.eINSTANCE.createEvent();
		newEvent.setType(EventType.INTERMEDIATE);
		newEvent.setName(eventname);
		emftaModel.getEvents().add(newEvent);
		newEvent.setReferenceCount(1);
		return newEvent;
	}

	private Event findEvent(String eventName) {
		for (Event event : emftaModel.getEvents()) {
			if (event.getName().equalsIgnoreCase(eventName)) {
				return event;
			}
		}
		return null;
	}

	/**
	 * turn list of subevents into an specified gate.
	 * In the process flatten any sub gates of the same type (one level is sufficient since we flatten at each step
	 * @param subEvents List<Event>
	 * @return Event (or null if empty list)
	 */
	private Event finalizeAsXOrEvents(List<EObject> subEvents, String eventname) {
		if (subEvents.size() == 0)
			return null;
		if (subEvents.size() == 1) {
			return (Event) subEvents.get(0);
		}
		Event combined = this.createIntermediateEvent(eventname);
		Gate emftaGate = EmftaFactory.eINSTANCE.createGate();
		emftaGate.setType(GateType.XOR);

		combined.setGate(emftaGate);
		Set<Event> intersection = null;
		// flatten
		for (Object seobj : subEvents) {
			Event se = (Event) seobj;
			if (se.getGate() != null && (se.getGate().getType() == GateType.XOR)) {
				for (Event ev : se.getGate().getEvents()) {
					emftaGate.getEvents().add(ev);
				}
			} else {
				emftaGate.getEvents().add(se);
			}
		}
		if (combined.getGate().getEvents().size() == 1) {
			combined = combined.getGate().getEvents().get(0);
		}
		return combined;

	}

	private Event finalizeAsOrEvents(List<EObject> subEvents, String eventName) {
		if (subEvents.size() == 0)
			return null;
		if (subEvents.size() == 1) {
			return (Event) subEvents.get(0);
		}
		Event combined = this.createIntermediateEvent(eventName);
		Gate emftaGate = EmftaFactory.eINSTANCE.createGate();
		emftaGate.setType(GateType.OR);

		combined.setGate(emftaGate);
		// flatten OR
		for (Object seobj : subEvents) {
			Event se = (Event) seobj;
			if (se.getGate() != null && se.getGate().getType() == GateType.OR) {
				for (Event ev : se.getGate().getEvents()) {
					emftaGate.getEvents().add(ev);
				}
			} else {
				emftaGate.getEvents().add(se);
			}
		}
		return combined;

	}

	private Event finalizeAsAndEvents(List<EObject> subEvents, String eventname) {
		if (subEvents.size() == 0)
			return null;
		if (subEvents.size() == 1) {
			return (Event) subEvents.get(0);
		}
		Event combined = this.createIntermediateEvent(eventname);
		Gate emftaGate = EmftaFactory.eINSTANCE.createGate();
		emftaGate.setType(GateType.AND);

		combined.setGate(emftaGate);
		Set<Event> intersection = null;
		// flatten
		for (Object seobj : subEvents) {
			Event se = (Event) seobj;
			if (se.getGate() != null && se.getGate().getType() == GateType.AND) {
				for (Event ev : se.getGate().getEvents()) {
					emftaGate.getEvents().add(ev);
				}
			} else {
				emftaGate.getEvents().add(se);
			}
		}
		return combined;
	}

	private Event finalizeAsPriorityAndEvents(List<EObject> subEvents, String eventname) {
		if (subEvents.size() == 0)
			return null;
		if (subEvents.size() == 1) {
			return (Event) subEvents.get(0);
		}
		Event combined = this.createIntermediateEvent(eventname);
		Gate emftaGate = EmftaFactory.eINSTANCE.createGate();
		emftaGate.setType(GateType.PRIORITY_AND);

		combined.setGate(emftaGate);
		// flatten
		for (Object seobj : subEvents) {
			Event se = (Event) seobj;
			if (se.getGate() != null && se.getGate().getType() == GateType.PRIORITY_AND) {
				for (Event ev : se.getGate().getEvents()) {
					emftaGate.getEvents().add(ev);
				}
			} else {
				emftaGate.getEvents().add(se);
			}
		}
		return combined;
	}

	/*************
	 * Optimizations
	 */

	/**
	 * remove subgates with a single event and place event in enclosing gate
	 * @param topgate
	 */
	private void removeZeroOneEventSubGates(Gate topgate) {
		if (topgate == null)
			return;
		List<Event> subEvents = topgate.getEvents();
		List<Event> toRemove = new LinkedList<Event>();
		List<Event> toAdd = new LinkedList<Event>();
		for (Event event : subEvents) {
			if (event.getGate() != null) {
				EList<Event> subs = event.getGate().getEvents();
				if (subs.size() == 1) {
					toRemove.add(event);
					toAdd.add(event.getGate().getEvents().get(0));
				} else if (subs.isEmpty()) {
					toRemove.add(event);
				}
			}
		}
		if (!toRemove.isEmpty()) {
			subEvents.removeAll(toRemove);
		}
		if (!toAdd.isEmpty()) {
			subEvents.addAll(toAdd);
		}
	}

	/**
	 * flatten subgates of same type into given gate
	 * @param gate
	 */
	private void flattenSubgates(Gate gate) {
		GateType mytype = gate.getType();
		EList<Event> subEvents = gate.getEvents();
		List<Event> toAdd = new LinkedList<Event>();
		List<Event> toRemove = new LinkedList<Event>();
		for (Event se : subEvents) {
			if (se.getGate() != null && se.getGate().getType() == mytype) {
				for (Event ev : se.getGate().getEvents()) {
					toAdd.add(ev);
				}
				toRemove.add(se);
			}
		}
		if (!toRemove.isEmpty()) {
			subEvents.removeAll(toRemove);
		}
		if (!toAdd.isEmpty()) {
			subEvents.addAll(toAdd);
		}
	}

	/**
	 * recursively apply optimizations on subgates.
	 * At the end optimize gate of rootevent.
	 * This may result in a new rootevent
	 * @param rootevent
	 * @return Event original or new root event
	 */
	private Event optimizeGates(Event rootevent) {
		String rootname = rootevent.getName();
		List<Event> subEvents = rootevent.getGate().getEvents();
		List<Event> toAdd = new LinkedList<Event>();
		List<Event> toRemove = new LinkedList<Event>();
		for (Event event : subEvents) {
			if (event.getGate() != null) {
				Event res = optimizeGates(event);
				if (res != event) {
					toAdd.add(res);
					toRemove.add(event);
				}
			}
		}
		Event res = rootevent;
		if (!toAdd.isEmpty()) {
			subEvents.removeAll(toRemove);
			subEvents.addAll(toAdd);
			flattenSubgates(res.getGate());
			removeZeroOneEventSubGates(res.getGate());
		}
		if (res.getGate().getType() == GateType.AND || res.getGate().getType() == GateType.XOR) {
			Event tmp = transformSubgates(rootevent, GateType.OR, res.getGate().getType());
			if (tmp != res) {
				res = tmp;
				flattenSubgates(res.getGate());
				removeZeroOneEventSubGates(res.getGate());
			}
		}
		if (res.getGate().getType() == GateType.OR || res.getGate().getType() == GateType.XOR) {
			Event tmp = transformSubgates(res, GateType.AND, res.getGate().getType());
			if (tmp != res) {
				res = tmp;
				flattenSubgates(res.getGate());
				removeZeroOneEventSubGates(res.getGate());
			}
		}
		if (res.getGate().getType() == GateType.AND || res.getGate().getType() == GateType.XOR) {
			res = removeCommonSubEvents(res, GateType.OR);
		}
		if (res.getGate().getType() == GateType.OR) {
			res = removeCommonSubEvents(res, GateType.AND);
		}
		if (res.getGate().getType() == GateType.OR) {
			res = removeCommonSubEvents(res, GateType.PRIORITY_AND);
		}
		if (res.getGate().getType() == GateType.OR) {
			res = removeCommonSubEvents(res, GateType.XOR);
		}
		flattenSubgates(res.getGate());
		removeZeroOneEventSubGates(res.getGate());
		if (res.getGate().getEvents().size() == 1 && res.getGate().getType() != GateType.INTERMEDIATE) {
			res = res.getGate().getEvents().get(0);
		}
		if (!rootname.startsWith("Intermediate")) {
			res.setName(rootname);
		}
		return res;
	}

	/**
	 * find common events in subgates and move them to an enclosing gate
	 * Currently does it if all of the gates of a given type have something in common.
	 * Could do it for various subsets of events with the matching gate type.
	 * @param topevent
	 * @param gt
	 * @return Event 
	 */
	private Event transformSubgates(Event topevent, GateType gt, GateType topgt) {
		Gate topgate = topevent.getGate();
		if (topgate == null)
			return null;
		List<Event> subEvents = topgate.getEvents();
		if (subEvents.isEmpty())
			return null;
		if (subEvents.size() == 1) {
			return (Event) subEvents.get(0);
		}
		Set<Event> intersection = null;
		List<Event> todo = new LinkedList<Event>();
		for (Event se : subEvents) {
			if (se.getGate() != null && (se.getGate().getType() == gt)) {
				if (intersection == null) {
					intersection = new HashSet<Event>(se.getGate().getEvents());
				} else {
					intersection.retainAll(se.getGate().getEvents());
				}
				todo.add(se);
			}
		}
		if (todo.size() > 1 && intersection != null && !intersection.isEmpty()) {
			// remove from lower OR and create an OR above top gate
			Event newtopevent = this.createIntermediateEvent("");
			Gate newtopgate = EmftaFactory.eINSTANCE.createGate();
			if (!topevent.getName().startsWith("Intermediate")) {
				String newname = newtopevent.getName();
				newtopevent.setName(topevent.getName());
				topevent.setName(newname);
			}
			newtopgate.setType(gt);
			newtopevent.setGate(newtopgate);
			newtopgate.getEvents().add(topevent);
			for (Event event : intersection) {
				newtopgate.getEvents().add(event);
			}
			for (Event se : todo) {
				EList<Event> rem = se.getGate().getEvents();
				rem.removeAll(intersection);
			}
			flattenSubgates(topgate);
			removeZeroOneEventSubGates(topgate);
			flattenSubgates(newtopgate);
			removeZeroOneEventSubGates(newtopgate);
			return newtopevent;
		}
		flattenSubgates(topgate);
		removeZeroOneEventSubGates(topgate);
		return topevent;

	}

	/**
	 * find events in subgates that already exist in enclosing gate
	 * @param topevent
	 * @param gt
	 * @return Event new topevent or null if no optimization was done
	 */
	private Event removeCommonSubEvents(Event topevent, GateType gt) {
		Gate topgate = topevent.getGate();
		if (topgate == null)
			return null;
		List<Event> subEvents = topgate.getEvents();
		if (subEvents.isEmpty())
			return null;
		for (Event seobj : subEvents) {
			Event se = (Event) seobj;
			if (se.getGate() != null && (se.getGate().getType() == gt)) {
				Set<Event> intersection = new HashSet<Event>(se.getGate().getEvents());
				intersection.retainAll(subEvents);
				if (intersection != null && !intersection.isEmpty()) {
					se.getGate().getEvents().removeAll(intersection);
				}
			}
		}
		removeZeroOneEventSubGates(topgate);
		return topevent;

	}

	/**
	 * create an PRIORITY_AND gate if both are non-null. Otherwise return the non-null Event or null if both are null.
	 * @param stateEvent Event representing the source state of a transition or outgoing propagation condition
	 * @param conditionEvent Event representing the condition of a transition or outgoing propagation condition
	 * @return Event or null
	 */
	private Event consolidateAsPriorityAnd(Event stateEvent, Event conditionEvent, String eventname) {
		if (stateEvent == null && conditionEvent != null) {
			return conditionEvent;
		} else if (stateEvent != null && conditionEvent == null) {
			return stateEvent;
		} else if (stateEvent != null && conditionEvent != null) {
			Event inter = createIntermediateEvent(eventname);
			Gate emftaGate = EmftaFactory.eINSTANCE.createGate();
			emftaGate.setType(GateType.PRIORITY_AND);
			inter.setGate(emftaGate);
			if (stateEvent.getGate() != null && stateEvent.getGate().getType() == GateType.PRIORITY_AND) {
				emftaGate.getEvents().addAll(stateEvent.getGate().getEvents());
			} else {
				emftaGate.getEvents().add(stateEvent);
			}
			if (conditionEvent.getGate() != null && conditionEvent.getGate().getType() == GateType.PRIORITY_AND) {
				emftaGate.getEvents().addAll(conditionEvent.getGate().getEvents());
			} else {
				emftaGate.getEvents().add(conditionEvent);
			}
			return inter;
		}
		return null;
	}

//	methods to be overwritten by applications

	@Override
	protected EObject postProcessOutgoingErrorPropagation(ComponentInstance component,
			ErrorPropagation errorPropagation, ErrorTypes targetType, List<EObject> subResults) {
		String name = buildName(component, errorPropagation, targetType);
		Event result = finalizeAsOrEvents(subResults, name);
		if (result != null && result.getType() == EventType.INTERMEDIATE) {
			result.setName(buildName(component, errorPropagation, targetType));
		}
		return result;
	}

	@Override
	protected EObject postProcessErrorFlows(ComponentInstance component, ErrorPropagation errorPropagation,
			ErrorTypes targetType, List<EObject> subResults) {
		String name = buildName(component, errorPropagation, targetType);
		Event result = finalizeAsOrEvents(subResults, name);
		if (result != null && result.getType() == EventType.INTERMEDIATE) {
			result.setName(buildName(component, errorPropagation, targetType));
		}
		return result;
	}

	@Override
	protected EObject processOutgoingErrorPropagation(ComponentInstance component, ErrorPropagation errorPropagation,
			ErrorTypes targetType) {
		Event newEvent = createBasicEvent(component, errorPropagation, targetType);
		Utils.fillProperties(newEvent, component, errorPropagation, targetType);
		return newEvent;
	}

	@Override
	protected EObject processErrorSource(ComponentInstance component, ErrorSource errorSource,
			TypeSet typeTokenConstraint) {
		Event newEvent = this.createBasicEvent(component, errorSource, errorSource.getTypeTokenConstraint());
		Utils.fillProperties(newEvent, component, errorSource, errorSource.getTypeTokenConstraint());
		return newEvent;
	}

	@Override
	protected EObject processIncomingErrorPropagation(ComponentInstance component, ErrorPropagation incoming,
			ErrorTypes type) {
		Event res = createBasicEvent(component, incoming, type);
		if (component instanceof SystemInstance) {
			res.setType(EventType.EXTERNAL);
		} else {
			res.setType(EventType.UNDEVELOPPED);
		}
		res.setDescription(Utils.getDescription(component, incoming, type));
		Utils.fillProperties(res, component, incoming, type);
		return res;
	}

	@Override
	protected EObject postProcessIncomingErrorPropagation(ComponentInstance component,
			ErrorPropagation errorPropagation, ErrorTypes targetType, List<EObject> subResults) {
		String name = buildName(component, errorPropagation, targetType);
		return finalizeAsOrEvents(subResults, name);
	}

	@Override
	protected EObject processOutgoingErrorPropagationCondition(ComponentInstance component,
			OutgoingPropagationCondition opc, ErrorTypes type, EObject conditionResult, EObject stateResult) {
		Event consolidated = consolidateAsPriorityAnd((Event) stateResult, (Event) conditionResult,
				buildName(component, opc, type));
		return consolidated;
	}

	@Override
	protected EObject postProcessCompositeErrorStates(ComponentInstance component, ErrorBehaviorState state,
			ErrorTypes targetType, List<EObject> subResults) {
		String name = buildName(component, state, targetType);
		Event result = finalizeAsOrEvents(subResults, name);
		if (result == null) {
			Event newEvent = createBasicEvent(component, state, targetType);
			Utils.fillProperties(newEvent, component, state, targetType);
			return newEvent;
		}
		return result;
	}

	// do not generate event. Otherwise we get events for operational state.
//	@Override
//	protected EObject processErrorBehaviorState(ComponentInstance component, ErrorBehaviorState state,
//			ErrorTypes type) {
//		Event newEvent = createBasicEvent(component, state, type);
//		Utils.fillProperties(newEvent, component, state, type);
//		return newEvent;
//	}

	@Override
	protected EObject postProcessErrorBehaviorState(ComponentInstance component, ErrorBehaviorState state,
			ErrorTypes type, List<EObject> subResults) {
		String name = buildName(component, state, type);
		Event result = finalizeAsOrEvents(subResults, name);
		if (result != null && result.getType() == EventType.INTERMEDIATE) {
			result.setName(buildName(component, state, type));
		}
		return result;
	}

	@Override
	protected EObject processTransitionCondition(ComponentInstance component, ErrorBehaviorState source,
			ErrorTypes type, EObject conditionResult, EObject stateResult) {
		Event consolidated = consolidateAsPriorityAnd((Event) stateResult, (Event) conditionResult,
				buildName(component, source, type));
		return consolidated;
	}

	@Override
	protected EObject processErrorEvent(ComponentInstance component, ErrorEvent errorEvent, ErrorTypes type,
			double scale) {
		Event emftaEvent = this.createBasicEvent(component, errorEvent, errorEvent.getTypeSet());
		Utils.fillProperties(emftaEvent, component, errorEvent, type, scale);

		return emftaEvent;
	}

	@Override
	protected EObject postProcessAnd(ComponentInstance component, ConditionExpression condition, ErrorTypes type,
			double scale, List<EObject> subResults) {
		return finalizeAsAndEvents(subResults, "");
	}

	@Override
	protected EObject postProcessXor(ComponentInstance component, ConditionExpression condition, ErrorTypes type,
			double scale, List<EObject> subResults) {
		return finalizeAsXOrEvents(subResults, "");
	}

	@Override
	protected EObject postProcessOr(ComponentInstance component, ConditionExpression condition, ErrorTypes type,
			double scale, List<EObject> subResults) {
		return finalizeAsOrEvents(subResults, "");
	}
}
