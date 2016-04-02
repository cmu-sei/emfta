package org.osate.aadl2.errormodel.emfta.fta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.instance.ComponentInstance;
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

	public Map<String, edu.cmu.emfta.Event> cache;

	private String buildName(ComponentInstance component, NamedElement namedElement, ErrorTypes type) {
		String name = eventIdentifier + "-" + buildIdentifier(component, namedElement, type);
		name = name.replaceAll("\\{", "").replaceAll("\\}", "").toLowerCase();

		eventIdentifier = eventIdentifier + 1;
		return name;
	}

	private String buildIdentifier(ComponentInstance component, NamedElement namedElement, ErrorTypes type) {
		String identifier;

		identifier = component.getName();
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

	private Event getFromCache(ComponentInstance component, NamedElement namedElement, ErrorTypes type) {
		String id = buildIdentifier(component, namedElement, type);
		if (cache.containsKey(id)) {
			return cache.get(id);
		}
		return null;
	}

	private void putInCache(ComponentInstance component, NamedElement namedElement, ErrorTypes type, Event event) {
		String identifier = buildIdentifier(component, namedElement, type);
		cache.put(identifier, event);
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

	private void removeEvent(Event event) {
		if (event.getReferenceCount() > 1) {
			event.setReferenceCount(event.getReferenceCount() - 1);
		} else {
			emftaModel.getEvents().remove(event);
		}
	}

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
			String longName = buildName(getRootComponent(), ne, rootComponentTypes);
			if (emftaRootEvent.getGate() == null && !emftaRootEvent.getName().equals(longName)) {
				Gate top = EmftaFactory.eINSTANCE.createGate();
				top.setType(GateType.OR);
				top.getEvents().add(emftaRootEvent);
				Event topEvent = createIntermediateEvent(getRootComponent(), ne, rootComponentTypes);
				topEvent.setGate(top);
				emftaRootEvent = topEvent;
			}
			emftaModel.setRoot(emftaRootEvent);
		}
		return emftaModel;
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
		List<Event> multiple = new LinkedList<Event>();
		// flatten
		for (Object seobj : subEvents) {
			Event se = (Event) seobj;
			if (se.getGate() != null
					&& (se.getGate().getType() == GateType.XOR || se.getGate().getType() == GateType.OR)) {
				for (Event ev : se.getGate().getEvents()) {
					if (!emftaGate.getEvents().add(ev)) {
						multiple.add(ev);
						removeEvent(ev);
					}
				}
				removeEvent(se);
			} else {
				if (!emftaGate.getEvents().add(se)) {
					multiple.add(se);
					removeEvent(se);
				}
			}
		}
		emftaGate.getEvents().removeAll(multiple);
		for (Event event : multiple) {
			removeEvent(event);
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
		// flatten
		for (Object seobj : subEvents) {
			Event se = (Event) seobj;
			if (se.getGate() != null && se.getGate().getType() == GateType.OR) {
				for (Event ev : se.getGate().getEvents()) {
					if (!emftaGate.getEvents().add(ev)) {
						removeEvent(ev);
					}
				}
				removeEvent(se);
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
		// flatten
		for (Object seobj : subEvents) {
			Event se = (Event) seobj;
			if (se.getGate() != null && se.getGate().getType() == GateType.AND) {
				for (Event ev : se.getGate().getEvents()) {
					if (!emftaGate.getEvents().add(ev)) {
						removeEvent(ev);
					}
				}
				removeEvent(se);
			} else {
				emftaGate.getEvents().add(se);
			}
		}
		return combined;
	}

	/**
	 * create an AND gate if both are non-null. Otherwise return the non-null Event or null if both are null.
	 * @param stateEvent Event representing the source state of a transition or outgoing propagation condition
	 * @param conditionEvent Event representing the condition of a transition or outgoing propagation condition
	 * @return Event or null
	 */
	private Event consolidateAsAnd(Event stateEvent, Event conditionEvent) {
		if (stateEvent == null && conditionEvent != null) {
			return conditionEvent;
		} else if (stateEvent != null && conditionEvent == null) {
			return stateEvent;
		} else if (stateEvent != null && conditionEvent != null) {
			Event inter = createIntermediateEvent();
			Gate emftaGate = EmftaFactory.eINSTANCE.createGate();
			emftaGate.setType(GateType.AND);
			inter.setGate(emftaGate);
			if (stateEvent.getGate() != null && stateEvent.getGate().getType() == GateType.AND) {
				emftaGate.getEvents().addAll(stateEvent.getGate().getEvents());
				removeEvent(stateEvent);
			} else {
				emftaGate.getEvents().add(stateEvent);
			}
			if (conditionEvent.getGate() != null && conditionEvent.getGate().getType() == GateType.AND) {
				emftaGate.getEvents().addAll(conditionEvent.getGate().getEvents());
				removeEvent(conditionEvent);
			} else {
				emftaGate.getEvents().add(conditionEvent);
			}
			return inter;
		}
		return null;
	}

//	methods to be overwritten by applicaitons

	@Override
	protected EObject postProcessOutgoingErrorPropagation(ComponentInstance component,
			ErrorPropagation errorPropagation, ErrorTypes targetType, List<EObject> subResults) {
		Event result = finalizeAsOrEvents(subResults);
		if (result.getType() == EventType.INTERMEDIATE) {
			result.setName(buildName(component, errorPropagation, targetType));
		}
		putInCache(component, errorPropagation, targetType, result);
		return result;
	}

	@Override
	protected EObject preProcessOutgoingErrorPropagation(ComponentInstance component, ErrorPropagation errorPropagation,
			ErrorTypes targetType) {
		Event res = getFromCache(component, errorPropagation, targetType);
		if (res != null) {
			res.setReferenceCount(res.getReferenceCount() + 1);
		}
		return res;
	}

	@Override
	protected EObject postProcessErrorFlows(ComponentInstance component, ErrorPropagation errorPropagation,
			ErrorTypes targetType, List<EObject> subResults) {
		Event result = finalizeAsOrEvents(subResults);
		if (result.getType() == EventType.INTERMEDIATE) {
			result.setName(buildName(component, errorPropagation, targetType));
		}
		putInCache(component, errorPropagation, targetType, result);
		return result;
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
		Event emftaEvent = createBasicEvent(component, incoming, type);
		emftaEvent.setType(EventType.EXTERNAL);
		emftaEvent.setDescription(Utils.getDescription(component, incoming, type));
		Utils.fillProperties(emftaEvent, component, incoming, type);
		return emftaEvent;
	}

	@Override
	protected EObject postProcessIncomingErrorPropagation(ComponentInstance component,
			ErrorPropagation errorPropagation, ErrorTypes targetType, List<EObject> subResults) {
		return finalizeAsOrEvents(subResults);
	}

	@Override
	protected EObject processOutgoingErrorPropagationCondition(ComponentInstance component,
			OutgoingPropagationCondition opc, ErrorTypes type, EObject conditionResult, EObject stateResult) {
		Event consolidated = consolidateAsAnd((Event) stateResult, (Event) conditionResult);
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
		Event consolidated = consolidateAsAnd((Event) stateResult, (Event) conditionResult);
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
}
