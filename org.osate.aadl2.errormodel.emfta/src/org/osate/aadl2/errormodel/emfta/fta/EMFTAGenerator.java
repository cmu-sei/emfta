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
	private boolean pureTree = false;

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

	public FTAModel getEmftaModel(boolean pureTree) {
		this.pureTree = pureTree;
		return getEmftaModel();
	}

	public FTAModel getEmftaModel() {
		if (emftaModel == null) {
			edu.cmu.emfta.Event emftaRootEvent;

			emftaModel = EmftaFactory.eINSTANCE.createFTAModel();
			emftaModel.setName(getRootComponent().getName());
			emftaModel.setDescription("Top Level Failure");
			NamedElement ne = null;

			if (rootComponentState != null) {
				emftaRootEvent = (Event) traverseCompositeErrorState(getRootComponent(), rootComponentState,
						rootComponentTypes);
				ne = rootComponentState;
			} else {
				emftaRootEvent = (Event) traverseOutgoingErrorPropagation(getRootComponent(), rootComponentPropagation,
						rootComponentTypes);
				ne = rootComponentPropagation;
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
		}
		cleanupEMFTAModel(emftaModel);
		return emftaModel;
	}

	private String buildName(ComponentInstance component, NamedElement namedElement, ErrorTypes type) {
		String name = eventIdentifier + "-" + buildIdentifier(component, namedElement, type);
		name = name.replaceAll("\\{", "").replaceAll("\\}", "").toLowerCase();

		eventIdentifier = eventIdentifier + 1;
		return name;
	}

	private String buildIdentifier(ComponentInstance component, NamedElement namedElement, ErrorTypes type) {
		String identifier;

		identifier = component instanceof SystemInstance ? component.getName() : component.getComponentInstancePath();
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

		return identifier;
	}

	/**
	 * return the entry and increment the reference count on the Event
	 * @param component
	 * @param namedElement
	 * @param type
	 * @return
	 */
	private Event getFromCache(ComponentInstance component, NamedElement namedElement, ErrorTypes type) {
		if (this.pureTree)
			return null;
		String id = buildIdentifier(component, namedElement, type);
		if (cache.containsKey(id)) {
			Event res = cache.get(id);
			if (res != null) {
				return res;
			}
		}
		return null;
	}

	/**
	 * put Event into cache with (assumed) reference count of 1
	 * @param component
	 * @param namedElement
	 * @param type
	 * @param event
	 */
	private void putInCache(ComponentInstance component, NamedElement namedElement, ErrorTypes type, Event event) {
		if (this.pureTree)
			return;
		String identifier = buildIdentifier(component, namedElement, type);
		cache.put(identifier, event);
	}

	private void cleanupEMFTAModel(FTAModel ftamodel) {
		Event root = ftamodel.getRoot();
		EList<Event> eventlist = ftamodel.getEvents();
		resetReferenceCounts(eventlist);
		updateReferenceCount(root);
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

	private void updateReferenceCount(Event ev) {
		incrementReferenceCount(ev);
		if (ev.getGate() != null) {
			EList<Event> events = ev.getGate().getEvents();
			for (Event event : events) {
				updateReferenceCount(event);
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
		Event newEvent = EmftaFactory.eINSTANCE.createEvent();
		String name = buildName(component, namedElement, type);
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
		Event newEvent = EmftaFactory.eINSTANCE.createEvent();
		String name = buildName(component, namedElement, type);
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
	static private int count = 0;

	private Event createIntermediateEvent() {
		Event newEvent = EmftaFactory.eINSTANCE.createEvent();
		newEvent.setType(EventType.INTERMEDIATE);
		newEvent.setName("Intermediate" + count);
		count++;
		emftaModel.getEvents().add(newEvent);
		newEvent.setReferenceCount(1);
		return newEvent;
	}

	/**
	 * turn list of subevents into an specified gate.
	 * In the process flatten any sub gates of the same type (one level is sufficient since we flatten at each step
	 * @param subEvents List<Event>
	 * @return Event (or null if empty list)
	 */
	private Event finalizeAsXOrEvents(List<EObject> subEvents) {
		if (subEvents.size() == 0)
			return null;
		if (subEvents.size() == 1) {
			return (Event) subEvents.get(0);
		}
		Event combined = this.createIntermediateEvent();
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
			} else if (se.getGate() != null && (se.getGate().getType() == GateType.OR)) {
				if (emftaGate.getEvents().add(se)) {
					if (intersection == null) {
						intersection = new HashSet<Event>(se.getGate().getEvents());
					} else {
						intersection.retainAll(se.getGate().getEvents());
					}
				}
			} else {
				emftaGate.getEvents().add(se);
			}
		}
		if (intersection != null && !intersection.isEmpty()) {
			// remove from lower OR and create an OR above XOR
			Event top = this.createIntermediateEvent();
			Gate newor = EmftaFactory.eINSTANCE.createGate();
			newor.setType(GateType.OR);
			top.setGate(newor);
			newor.getEvents().add(combined);
			for (Event event : intersection) {
				newor.getEvents().add(event);
			}
			for (Object seobj : subEvents) {
				Event se = (Event) seobj;
				if (se.getGate() != null && (se.getGate().getType() == GateType.OR)) {
					for (Event event : intersection) {
						se.getGate().getEvents().remove(event);
					}
					if (se.getGate().getEvents().isEmpty()) {
						// remove event with OR gate from enclosing XOR gate
						emftaGate.getEvents().remove(se);
					} else if (se.getGate().getEvents().size() == 1) {
						Event temp = se.getGate().getEvents().get(0);
						emftaGate.getEvents().remove(se);
						emftaGate.getEvents().add(temp);
					}
				}
			}
			return top;
		}
		return combined;

	}

	private Event finalizeAsOrEvents(List<EObject> subEvents) {
		if (subEvents.size() == 0)
			return null;
		if (subEvents.size() == 1) {
			return (Event) subEvents.get(0);
		}
		Event combined = this.createIntermediateEvent();
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

	private Event finalizeAsAndEvents(List<EObject> subEvents) {
		if (subEvents.size() == 0)
			return null;
		if (subEvents.size() == 1) {
			return (Event) subEvents.get(0);
		}
		Event combined = this.createIntermediateEvent();
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
			} else if (se.getGate() != null && (se.getGate().getType() == GateType.OR)) {
				emftaGate.getEvents().add(se);
				if (intersection == null) {
					intersection = new HashSet<Event>(se.getGate().getEvents());
				} else {
					intersection.retainAll(se.getGate().getEvents());
				}
			} else {
				emftaGate.getEvents().add(se);
			}
		}
		if (intersection != null && !intersection.isEmpty()) {
			// remove from lower OR and create an OR above AND
			Event top = this.createIntermediateEvent();
			Gate newor = EmftaFactory.eINSTANCE.createGate();
			newor.setType(GateType.OR);
			top.setGate(newor);
			newor.getEvents().add(combined);
			newor.getEvents().addAll(intersection);
			for (Object seobj : subEvents) {
				Event se = (Event) seobj;
				if (se.getGate() != null && (se.getGate().getType() == GateType.OR)) {
					for (Event event : intersection) {
						se.getGate().getEvents().remove(event);
					}
					if (se.getGate().getEvents().isEmpty()) {
						// remove event with OR gate from enclosing AND gate
						emftaGate.getEvents().remove(se);
					} else if (se.getGate().getEvents().size() == 1) {
						Event temp = se.getGate().getEvents().get(0);
						emftaGate.getEvents().add(temp);
						emftaGate.getEvents().remove(se);
					}
				}
			}
			return top;
		}
		return combined;
	}

	private Event finalizeAsPriorityAndEvents(List<EObject> subEvents) {
		if (subEvents.size() == 0)
			return null;
		if (subEvents.size() == 1) {
			return (Event) subEvents.get(0);
		}
		Event combined = this.createIntermediateEvent();
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

	/**
	 * create an PRIORITY_AND gate if both are non-null. Otherwise return the non-null Event or null if both are null.
	 * @param stateEvent Event representing the source state of a transition or outgoing propagation condition
	 * @param conditionEvent Event representing the condition of a transition or outgoing propagation condition
	 * @return Event or null
	 */
	private Event consolidateAsPriorityAnd(Event stateEvent, Event conditionEvent) {
		if (stateEvent == null && conditionEvent != null) {
			return conditionEvent;
		} else if (stateEvent != null && conditionEvent == null) {
			return stateEvent;
		} else if (stateEvent != null && conditionEvent != null) {
			Event inter = createIntermediateEvent();
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
		Event result = finalizeAsOrEvents(subResults);
		if (result != null && result.getType() == EventType.INTERMEDIATE) {
			result.setName(buildName(component, errorPropagation, targetType));
		}
		putInCache(component, errorPropagation, targetType, result);
		return result;
	}

	@Override
	protected EObject preProcessOutgoingErrorPropagation(ComponentInstance component, ErrorPropagation errorPropagation,
			ErrorTypes targetType) {
		Event res = getFromCache(component, errorPropagation, targetType);
		return res;
	}

	@Override
	protected EObject postProcessErrorFlows(ComponentInstance component, ErrorPropagation errorPropagation,
			ErrorTypes targetType, List<EObject> subResults) {
		Event result = finalizeAsOrEvents(subResults);
		if (result != null && result.getType() == EventType.INTERMEDIATE) {
			result.setName(buildName(component, errorPropagation, targetType));
		}
		putInCache(component, errorPropagation, targetType, result);
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
		Event res = getFromCache(component, incoming, type);
		if (res != null) {
			return res;
		}
		res = createBasicEvent(component, incoming, type);
		if (component instanceof SystemInstance) {
			res.setType(EventType.EXTERNAL);
		} else {
			res.setType(EventType.UNDEVELOPPED);
		}
		res.setDescription(Utils.getDescription(component, incoming, type));
		Utils.fillProperties(res, component, incoming, type);
		putInCache(component, incoming, type, res);
		return res;
	}

	@Override
	protected EObject postProcessIncomingErrorPropagation(ComponentInstance component,
			ErrorPropagation errorPropagation, ErrorTypes targetType, List<EObject> subResults) {
		return finalizeAsOrEvents(subResults);
	}

	@Override
	protected EObject processOutgoingErrorPropagationCondition(ComponentInstance component,
			OutgoingPropagationCondition opc, ErrorTypes type, EObject conditionResult, EObject stateResult) {
		Event consolidated = consolidateAsPriorityAnd((Event) stateResult, (Event) conditionResult);
		return consolidated;
	}

	@Override
	protected EObject postProcessCompositeErrorStates(ComponentInstance component, ErrorBehaviorState state,
			ErrorTypes targetType, List<EObject> subResults) {
		Event result = finalizeAsOrEvents(subResults);
		if (result != null && result.getType() == EventType.INTERMEDIATE) {
			result.setName(buildName(component, state, targetType));
		}
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
		Event result = finalizeAsOrEvents(subResults);
		if (result != null && result.getType() == EventType.INTERMEDIATE) {
			result.setName(buildName(component, state, type));
		}
		return result;
	}

	@Override
	protected EObject processTransitionCondition(ComponentInstance component, ErrorBehaviorState source,
			ErrorTypes type, EObject conditionResult, EObject stateResult) {
		Event consolidated = consolidateAsPriorityAnd((Event) stateResult, (Event) conditionResult);
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
		return finalizeAsAndEvents(subResults);
	}

	@Override
	protected EObject postProcessXor(ComponentInstance component, ConditionExpression condition, ErrorTypes type,
			double scale, List<EObject> subResults) {
		return finalizeAsXOrEvents(subResults);
	}

	@Override
	protected EObject postProcessOr(ComponentInstance component, ConditionExpression condition, ErrorTypes type,
			double scale, List<EObject> subResults) {
		return finalizeAsOrEvents(subResults);
	}
}
