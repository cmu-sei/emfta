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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.osate.aadl2.DirectionType;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.Subcomponent;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.util.OsateDebug;
import org.osate.xtext.aadl2.errormodel.errorModel.AndExpression;
import org.osate.xtext.aadl2.errormodel.errorModel.CompositeState;
import org.osate.xtext.aadl2.errormodel.errorModel.ConditionElement;
import org.osate.xtext.aadl2.errormodel.errorModel.ConditionExpression;
import org.osate.xtext.aadl2.errormodel.errorModel.EMV2Path;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorBehaviorTransition;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorEvent;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorFlow;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorPath;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorPropagation;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorSource;
import org.osate.xtext.aadl2.errormodel.errorModel.ErrorTypes;
import org.osate.xtext.aadl2.errormodel.errorModel.OrExpression;
import org.osate.xtext.aadl2.errormodel.errorModel.OutgoingPropagationCondition;
import org.osate.xtext.aadl2.errormodel.errorModel.SConditionElement;
import org.osate.xtext.aadl2.errormodel.errorModel.SubcomponentElement;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeSet;
import org.osate.xtext.aadl2.errormodel.util.AnalysisModel;
import org.osate.xtext.aadl2.errormodel.util.EM2TypeSetUtil;
import org.osate.xtext.aadl2.errormodel.util.EMV2Util;
import org.osate.xtext.aadl2.errormodel.util.PropagationPathEnd;

import edu.cmu.emfta.EmftaFactory;
import edu.cmu.emfta.Event;
import edu.cmu.emfta.EventType;
import edu.cmu.emfta.Gate;
import edu.cmu.emfta.GateType;

public class EmftaWrapper {
	class PropagationRecord {
		ErrorPropagation ep;
		TypeSet constraint;
	}

	private edu.cmu.emfta.FTAModel emftaModel;
	private AnalysisModel currentAnalysisModel;
	private ComponentInstance rootComponent;
	private ErrorBehaviorState rootComponentState;
	private ErrorPropagation rootComponentPropagation;
	private ErrorTypes rootComponentTypes;
	private int eventIdentifier;

	public Map<String, edu.cmu.emfta.Event> cache;

	private String buildName(ComponentInstance component, NamedElement namedElement, TypeSet typeSet) {
		String name = eventIdentifier + "-" + buildIdentifier(component, namedElement, typeSet);
		name = name.replaceAll("\\{", "").replaceAll("\\}", "").toLowerCase();

		eventIdentifier = eventIdentifier + 1;
		return name;
	}

	private String buildIdentifier(ComponentInstance component, NamedElement namedElement, TypeSet typeSet) {
		String identifier;

		identifier = component.getName();
		identifier += "-";

		if (namedElement == null) {
			identifier += "unidentified";

		} else {
			identifier = EMV2Util.getPrintName(namedElement);
//			if (namedElement instanceof ErrorPropagation) {
//				ErrorPropagation ep = (ErrorPropagation) namedElement;
//				if (ep.getName() != null) {
//					identifier += ep.getName();
//				} else {
//					if (ep.getKind() != null) {
//						identifier += ep.getKind();
//					} else {
//
//						if ((ep.getFeatureorPPRef() != null) && (ep.getFeatureorPPRef().getFeatureorPP() != null)) {
//							NamedElement ref = ep.getFeatureorPPRef().getFeatureorPP();
//							identifier += "-" + "propagation-from-" + ref.getName();
//						} else {
//							identifier += "unknwon-epkind";
//						}
//					}
//				}
//
//			} else if (namedElement instanceof ErrorEvent) {
//				ErrorEvent ev = (ErrorEvent) namedElement;
//				identifier += ev.getName();
//			} else if (namedElement instanceof ErrorBehaviorState) {
//				ErrorBehaviorState ebs = (ErrorBehaviorState) namedElement;
//				identifier += ebs.getName();
//			} else if (namedElement instanceof ErrorSource) {
//				ErrorSource es = (ErrorSource) namedElement;
//				identifier += es.getName();
//			} else {
//				identifier += "unknown";
//			}
		}

		if (typeSet != null && typeSet.getName() != null) {
			identifier += "-" + typeSet.getName();
		} else {
			identifier += "-" + EMV2Util.getPrintName(typeSet);
		}

		return identifier;
	}

	private Event getFromCache(ComponentInstance component, NamedElement namedElement, TypeSet typeSet) {
		String id = buildIdentifier(component, namedElement, typeSet);
		if (cache.containsKey(id)) {
			return cache.get(id);
		}
		return null;
	}

	private void putInCache(ComponentInstance component, NamedElement namedElement, TypeSet typeSet, Event event) {
		String identifier = buildIdentifier(component, namedElement, typeSet);
		cache.put(identifier, event);
	}

	private Event createEvent(ComponentInstance component, NamedElement namedElement, TypeSet typeSet) {
		Event newEvent = EmftaFactory.eINSTANCE.createEvent();
		String name = buildName(component, namedElement, typeSet);
		emftaModel.getEvents().add(newEvent);
		newEvent.setName(name);
		newEvent.setGate(null);
		return newEvent;
	}

	private Event createIntermediateEvent() {
		Event newEvent = EmftaFactory.eINSTANCE.createEvent();
		newEvent.setType(EventType.INTERMEDIATE);
		newEvent.setName("Intermediate");
		emftaModel.getEvents().add(newEvent);
		return newEvent;
	}

	private void removeEvent(Event event) {
		emftaModel.getEvents().remove(event);
	}

	public EmftaWrapper(ComponentInstance root, ErrorBehaviorState errorState, ErrorTypes errorTypes) {
		emftaModel = null;
		cache = new HashMap<String, edu.cmu.emfta.Event>();
		rootComponentTypes = errorTypes;
		rootComponentState = errorState;
		rootComponentPropagation = null;
		rootComponent = root;
		eventIdentifier = 0;
		currentAnalysisModel = new AnalysisModel(rootComponent);
	}

	public EmftaWrapper(ComponentInstance root, ErrorPropagation errorPropagation, ErrorTypes errorTypes) {
		// TOFIX
		emftaModel = null;
		cache = new HashMap<String, edu.cmu.emfta.Event>();
		rootComponentTypes = errorTypes;
		rootComponentPropagation = errorPropagation;
		rootComponentState = null;
		rootComponent = root;
		eventIdentifier = 0;
		currentAnalysisModel = new AnalysisModel(rootComponent);
	}

	public edu.cmu.emfta.FTAModel getEmftaModel() {
		if (emftaModel == null) {
			edu.cmu.emfta.Event emftaRootEvent;

			emftaModel = EmftaFactory.eINSTANCE.createFTAModel();
			emftaModel.setName(rootComponent.getName());
			emftaModel.setDescription("Top Level Failure");

			if (rootComponentState != null) {
				emftaRootEvent = processCompositeErrorBehavior(rootComponent, rootComponentState, rootComponentTypes);
			} else {
				emftaRootEvent = processOutgoingErrorPropagation(rootComponent, rootComponentPropagation,
						rootComponentTypes);
			}
			emftaModel.setRoot(emftaRootEvent);
		}

		return emftaModel;
	}

	/**
	 * process an Outgoing Error Propagation by going backwards in the propagation graph
	 * First we attempt to go backwards according to the component error behavior, i.e., the OutgoingPropagationCondition.
	 * If not present we do it according to error flow specifications.
	 * @param component ComponentInstance
	 * @param errorPropagation outgoing ErrorPropagation
	 * @param type ErrorTypes
	 * @return Event 
	 */
	public Event processOutgoingErrorPropagation(final ComponentInstance component,
			final ErrorPropagation errorPropagation, final ErrorTypes type) {
		ErrorTypes targetType = getTargetType(errorPropagation.getTypeSet(), type);
		Event result = processOutgoingErrorPropagationConditions(component, errorPropagation, targetType);
		if (result == null) {
			result = processReverseErrorFlow(component, errorPropagation, targetType);
		}
		// XXX here we want to cache
		return result;
	}

	/**
	 * determine the target type given the original type of a backward propagation.
	 * If the original is contained in the constraint use it. 
	 * If not use the constraint as it may represent a mapping, e.g., for an error path
	 * @param constraint ErrorTypes that is expectd on the left hand side
	 * @param original ErrroTypes that is the actual origin of the backward proapagation
	 * @return ErrorTypes
	 */
	private ErrorTypes getTargetType(ErrorTypes constraint, ErrorTypes original) {
		return EM2TypeSetUtil.contains(constraint, original) ? original : constraint;
	}

	/**
	 * Process all OutgoingPropagationConditions that match the propagation and type of interest
	 * It is an OR of the OutgoingPropagationConditions that match. 
	 * Fore each it is an AND of the source state (if it involves an error event or propagation).
	 * @param component ComponentInstance
	 * @param propagation (outgoing) ErrorPropagation
	 * @param type ErrorTypes
	 * @return Event (can be null)
	 */
	public Event processOutgoingErrorPropagationConditions(ComponentInstance component, ErrorPropagation propagation,
			ErrorTypes type) {
		if (propagation.getDirection() != DirectionType.OUT)
			return null;
		List<Event> subEvents = new ArrayList<Event>();
		for (OutgoingPropagationCondition opc : EMV2Util.getAllOutgoingPropagationConditions(component)) {
			Event conditionEvent = null;
			if (EMV2Util.isSame(opc.getOutgoing(), propagation)) {
				ConditionExpression conditionExpression = opc.getCondition();
				if (conditionExpression != null) {
					OsateDebug.osateDebug("condition expression" + conditionExpression);
					conditionEvent = processCondition(component, conditionExpression);
				}
			}
			Event stateEvent = processBehaviorState(component, opc.getState(), type);
			Event consolidated = consolidateAsAnd(stateEvent, conditionEvent);
			if (consolidated != null) {
				subEvents.add(consolidated);
			}
		}
		return finalizeAsOrEvents(subEvents);
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
			if (stateEvent.getGate().getType() == GateType.AND) {
				emftaGate.getEvents().addAll(stateEvent.getGate().getEvents());
				removeEvent(stateEvent);
			} else {
				emftaGate.getEvents().add(stateEvent);
			}
			if (conditionEvent.getGate().getType() == GateType.AND) {
				emftaGate.getEvents().addAll(conditionEvent.getGate().getEvents());
				removeEvent(conditionEvent);
			} else {
				emftaGate.getEvents().add(conditionEvent);
			}
			return inter;
		}
		return null;
	}

	/**
	 * process error state according to transitions. Recursively deal with source states of transitions (an AND gate).
	 * We only process error events (not recover or repair) and error propagations referenced by the expression.
	 * @param component ComponentInstance
	 * @param state ErrorBehaviorState
	 * @param type ErrorTypes
	 * @return event
	 */
	public Event processBehaviorState(ComponentInstance component, ErrorBehaviorState state, ErrorTypes type) {
		List<Event> subEvents = new ArrayList<Event>();
		for (ErrorBehaviorTransition ebt : EMV2Util.getAllErrorBehaviorTransitions(component)) {
			Event conditionEvent = null;
			if (EMV2Util.isSame(state, ebt.getTarget())) {
				ConditionExpression conditionExpression = ebt.getCondition();
				if (conditionExpression != null) {
					OsateDebug.osateDebug("condition expression" + conditionExpression);
					conditionEvent = processCondition(component, conditionExpression);
				}
			}
			Event stateEvent = processBehaviorState(component, ebt.getSource(), type);
			Event consolidated = consolidateAsAnd(stateEvent, conditionEvent);
			if (consolidated != null) {
				subEvents.add(consolidated);
			}
		}
		return finalizeAsOrEvents(subEvents);
	}

	/**
	 * turn list of subevents into an OR gate.
	 * In the process flatten any sub OR gates (one level is sufficient since we flatten at each step
	 * @param subEvents List<Event>
	 * @return Event (or null if empty list)
	 */
	private Event finalizeAsOrEvents(List<Event> subEvents) {
		if (subEvents.size() == 0)
			return null;
		if (subEvents.size() == 1) {
			return subEvents.get(0);
		}
		Event combined = this.createIntermediateEvent();
		Gate emftaGate = EmftaFactory.eINSTANCE.createGate();
		emftaGate.setType(GateType.OR);

		combined.setGate(emftaGate);
		// flatten and optimize OR?
		for (Event se : subEvents) {
			if (se.getGate().getType() == GateType.OR) {
				emftaGate.getEvents().addAll(se.getGate().getEvents());
				removeEvent(se);
			} else {
				emftaGate.getEvents().add(se);
			}
		}
		return combined;
	}

	/**
	 * process error flows in reverse direction that match the outgoing error propagation.
	 * The flows can be a path or source and are treated as an OR. 
	 * An error source is treated as an EXTERNAL event with probability values on the error source
	 * XXX possible TODO: look at the when.
	 * @param component ComponentInstance
	 * @param errorPropagation ErrorPropagation
	 * @param type ErrorTypes
	 * @return Event or null if no matching flows
	 */
	public Event processReverseErrorFlow(final ComponentInstance component, final ErrorPropagation errorPropagation,
			final ErrorTypes type) {
		List<Event> subEvents = new ArrayList<Event>();
		if (errorPropagation.getDirection() == DirectionType.OUT) {
			for (ErrorFlow ef : EMV2Util.getAllErrorFlows(component)) {
				if (ef instanceof ErrorPath) {
					ErrorPath ep = (ErrorPath) ef;
					if (EMV2Util.isSame(ep.getOutgoing(), errorPropagation)
							&& EM2TypeSetUtil.contains(type, ep.getTargetToken())) {
						subEvents.add(processIncomingErrorPropagation(component, ep.getIncoming(), type));
					}
				} else if (ef instanceof ErrorSource) {
					ErrorSource errorSource = (ErrorSource) ef;

					if (EMV2Util.isSame(errorSource.getOutgoing(), errorPropagation)) {
						if (EM2TypeSetUtil.contains(errorSource.getTypeTokenConstraint(), type)) {

							Event newEvent;

							newEvent = getFromCache(component, errorSource, ef.getTypeTokenConstraint());
							if (newEvent == null) {
								newEvent = this.createEvent(component, errorSource, ef.getTypeTokenConstraint());
								newEvent.setType(EventType.EXTERNAL);
								Utils.fillProperties(newEvent, component, errorSource, ef.getTypeTokenConstraint());
							}

							subEvents.add(newEvent);
						}
					}
				}
			}
		}
		return finalizeAsOrEvents(subEvents);
	}

	/**
	 * process an incoming error propagation.
	 * Follow any propagation path according to the AnalysisModel.
	 * If none are found use the incoming error propagation as an EXTERANAL event with probability value.
	 * @param component
	 * @param errorPropagation
	 * @param type
	 * @return
	 */
	public Event processIncomingErrorPropagation(final ComponentInstance component,
			final ErrorPropagation errorPropagation, final ErrorTypes type) {
		List<Event> subEvents = new ArrayList<Event>();
		EList<PropagationPathEnd> propagationSources = currentAnalysisModel.getAllPropagationSourceEnds(component,
				errorPropagation);

		for (PropagationPathEnd ppe : propagationSources) {
			ComponentInstance componentSource = ppe.getComponentInstance();
			ErrorPropagation propagationSource = ppe.getErrorPropagation();

			Event result = processOutgoingErrorPropagation(componentSource, propagationSource, type);
			if (result != null) {
				subEvents.add(result);
			} else {
				// create event to represent external incoming.
				Event emftaEvent = createEvent(component, errorPropagation, errorPropagation.getTypeSet());
				emftaEvent.setType(EventType.EXTERNAL);
				Utils.fillProperties(emftaEvent, component, errorPropagation, errorPropagation.getTypeSet());
				subEvents.add(emftaEvent);
			}

		}
		return finalizeAsOrEvents(subEvents);
	}

	/**
	 * Process a condition, either from a component error behavior or a
	 * composite error behavior.
	 *
	 * @param component
	 *            - the component that contains the condition
	 * @param condition
	 *            - the ConditionExpression to be analyzed
	 * @return a list of events related to the condition
	 */
	public Event processCondition(ComponentInstance component, ConditionExpression condition) {

		// OsateDebug.osateDebug("[EmftaWrapper] condition=" + condition);

		/**
		 * We have an AND expression, so, we create an EVENT to AND' sub events.
		 */
		if (condition instanceof AndExpression) {
			AndExpression expression;
			Event emftaEvent = EmftaFactory.eINSTANCE.createEvent();
			emftaModel.getEvents().add(emftaEvent);

			emftaEvent.setType(EventType.BASIC);
			Gate emftaGate = EmftaFactory.eINSTANCE.createGate();
			emftaGate.setType(GateType.AND);
			emftaEvent.setGate(emftaGate);

			emftaEvent.setDescription("Occurrence of all the following events");

			expression = (AndExpression) condition;
			List<Event> subEvents = new ArrayList<Event>();

			for (ConditionExpression ce : expression.getOperands()) {
				subEvents.add(processCondition(component, ce));
			}

			for (Event e : subEvents) {
				emftaGate.getEvents().add(e);
			}
			return emftaEvent;
		}

		if (condition instanceof OrExpression) {
			OrExpression expression;
			Event emftaEvent = EmftaFactory.eINSTANCE.createEvent();
			emftaModel.getEvents().add(emftaEvent);

			emftaEvent.setType(EventType.BASIC);

			Gate emftaGate = EmftaFactory.eINSTANCE.createGate();
			emftaGate.setType(GateType.OR);
			emftaEvent.setGate(emftaGate);

			emftaEvent.setDescription("Occurrence of at least one the following events");

			expression = (OrExpression) condition;
			for (ConditionExpression ce : expression.getOperands()) {
				Event tmpEvent = processCondition(component, ce);

				if ((tmpEvent.getGate() != null) && (tmpEvent.getGate().getType() == GateType.OR)) {
					emftaModel.getEvents().remove(tmpEvent);
					for (Event e : tmpEvent.getGate().getEvents()) {
						emftaGate.getEvents().add(e);
					}
				} else {
					emftaGate.getEvents().add(tmpEvent);
				}
				// Pre-optimization code
				// emftaGate.getEvents().add(processCondition(component, ce));
			}
			return emftaEvent;
		}

		/**
		 * Here, we have a single condition element.
		 */
		if (condition instanceof ConditionElement) {
			ConditionElement conditionElement = (ConditionElement) condition;

			if (condition instanceof SConditionElement) {
				SConditionElement sconditionElement = (SConditionElement) condition;
				if (sconditionElement.getQualifiedState() != null) {
					OsateDebug.osateDebug("conditionElement" + sconditionElement.getQualifiedState());
					/**
					 * In the following, it seems that we reference another
					 * component. This is typically the case when the condition is
					 * within an composite error behavior.
					 *
					 * So, we find the referenced component in the component
					 * hierarchy and add all its contributors to the returned
					 * events.
					 */
					// OsateDebug.osateDebug("[EmftaWrapper] processCondition
					// subcomponents are present, size=" +
					// conditionElement.getSubcomponents().size());
					SubcomponentElement subcomponentElement = sconditionElement.getQualifiedState().getSubcomponent();
					Subcomponent subcomponent = subcomponentElement.getSubcomponent();
					ComponentInstance referencedInstance;
					ErrorTypes referencedErrorType;
					referencedInstance = null;
					referencedErrorType = null;
					// OsateDebug.osateDebug("[EmftaWrapper] subcomponent=" +
					// subcomponent);

					for (ComponentInstance sub : component.getComponentInstances()) {
						// OsateDebug.osateDebug("[EmftaWrapper] sub=" +
						// sub.getSubcomponent());
						if (sub.getSubcomponent().getName().equalsIgnoreCase(subcomponent.getName())) {
							referencedInstance = sub;
						}
					}

					if ((sconditionElement.getConstraint() != null)
							&& (sconditionElement.getConstraint().getTypeTokens().size() > 0)) {
						referencedErrorType = sconditionElement.getConstraint().getTypeTokens().get(0).getType().get(0);
					}

					// OsateDebug.osateDebug("[EmftaWrapper] referenced component
					// instance=" + referencedInstance);
					// OsateDebug.osateDebug("[EmftaWrapper] referenced type=" +
					// referencedErrorType);

					return processErrorState(referencedInstance, EMV2Util.getState(sconditionElement),
							referencedErrorType);
				}

			}

			if (conditionElement.getQualifiedErrorPropagationReference() != null) {
				OsateDebug.osateDebug("conditionElement" + conditionElement.getQualifiedErrorPropagationReference());
				EMV2Path path = conditionElement.getQualifiedErrorPropagationReference();

				ComponentInstance relatedComponent = EMV2Util.getLastComponentInstance(path, component);
				NamedElement errorModelElement = EMV2Util.getErrorModelElement(path);
				OsateDebug.osateDebug("actualComponent   =" + component);
				OsateDebug.osateDebug("errorModelElement =" + errorModelElement);
				OsateDebug.osateDebug("relatedComponent  =" + relatedComponent);

				// OsateDebug.osateDebug("emv el = " + EMV2Util.getErrorModelElement(path));
				// OsateDebug.osateDebug("path" + path);
				// OsateDebug.osateDebug("ne=" + pe.getNamedElement());
				// OsateDebug.osateDebug("me2"+ path.getElementRoot());
				// OsateDebug.osateDebug("kind=" + pe.getEmv2PropagationKind());

				// OsateDebug.osateDebug("[EmftaWrapper] processCondition incoming="
				// + conditionElement.getIncoming());

				/**
				 * Here, we have an error event. Likely, this is something we
				 * can get when we are analyzing error component behavior.
				 */
				if (errorModelElement instanceof ErrorEvent) {
					ErrorEvent errorEvent;
					Event emftaEvent;

					errorEvent = (ErrorEvent) errorModelElement;
					emftaEvent = this.createEvent(relatedComponent, errorEvent, errorEvent.getTypeSet());
					emftaEvent.setType(EventType.BASIC);
					// XXX should this be the tt coming backwards? Above already).
					// XXX probability on which type of EE
					Utils.fillProperties(emftaEvent, relatedComponent, errorEvent, errorEvent.getTypeSet());

					return emftaEvent;
				}

				/**
				 * Here, we have an error propagation. This is notified with the
				 * in propagation within a composite error model.
				 */
				if (errorModelElement instanceof ErrorPropagation) {
					ErrorPropagation errorPropagation;
					// XXX the conditoin element may have a null constraint.
					// XXX we need the type as input the the condition.
					errorPropagation = (ErrorPropagation) errorModelElement;
					Event incoming = processIncomingErrorPropagation(relatedComponent, errorPropagation,
							conditionElement.getConstraint());
					return incoming;
				}

			}
		}
		return null;

	}

	/**
	 * Process a component error behavior, analyze its transition and produces a
	 * list of all events that could then be added in a fault-tree.
	 *
	 * @param component
	 *            - The component under analysis (the one that contains the
	 *            error behavior)
	 * @param state
	 *            - The target states of the transitions under analysis
	 * @param type
	 *            - The type associated with the target state
	 * @return - list of events that are related to the target state in this
	 *         component.
	 */
	public Event processComponentErrorBehavior(ComponentInstance component, ErrorBehaviorState state, ErrorTypes type) {
		/**
		 * Depending on the condition, it returns either a single element, an
		 * AND or an OR.
		 */
		List<Event> subEvents;

		subEvents = new ArrayList<Event>();

		for (ErrorBehaviorTransition transition : EMV2Util.getAllErrorBehaviorTransitions(component)) {
			if (transition.getTarget() == state) {
				subEvents.add(processCondition(component, transition.getCondition()));
				// XXX handle source state as AND it may have been reached by a transition though additional events
			}
		}

		if (subEvents.size() == 1) {
			return subEvents.get(0);
		}
		if (subEvents.size() > 1) {

			Event combined;
			combined = getFromCache(component, state, state.getTypeSet());

			if (combined == null) {
				combined = this.createEvent(component, state, state.getTypeSet());
				combined.setType(EventType.BASIC);
				Utils.fillProperties(combined, component, state, null);
				Gate emftaGate = EmftaFactory.eINSTANCE.createGate();
				emftaGate.setType(GateType.OR);

				combined.setGate(emftaGate);

				for (Event se : subEvents) {
					emftaGate.getEvents().add(se);
				}
			}

			return combined;
		}

		return null;
	}

	/**
	 * Process a composite error behavior for a component and try to get all
	 * related potential events to add in a FTA
	 *
	 * @param component
	 *            - the component under analysis
	 * @param state
	 *            - the target state under analysis
	 * @param type
	 *            - the type associated to the target state (if any)
	 * @return - the list of all potential related FTA events
	 */
	public Event processCompositeErrorBehavior(ComponentInstance component, ErrorBehaviorState state, ErrorTypes type) {
		/**
		 * Depending on the condition, it returns either a single element, an
		 * AND or an OR.
		 */
		List<Event> subEvents;

		subEvents = new ArrayList<Event>();

		for (CompositeState cs : EMV2Util.getAllCompositeStates(component)) {
			if (cs.getState() == state) {
				subEvents.add(processCondition(component, cs.getCondition()));
			}
		}

		if (subEvents.size() == 1) {
			return subEvents.get(0);
		}
		if (subEvents.size() > 1) {
			Event combined = this.createEvent(component, state, state.getTypeSet());
			combined.setType(EventType.BASIC);
			Utils.fillProperties(combined, component, state, null);
			Gate emftaGate = EmftaFactory.eINSTANCE.createGate();
			emftaGate.setType(GateType.OR);

			combined.setGate(emftaGate);

			for (Event se : subEvents) {
				emftaGate.getEvents().add(se);
			}

			return combined;
		}
		return null;

	}

	/**
	 * Process a particular error behavior state and try to get all potential
	 * error contributors, either from the component error behavior or the
	 * composite error behavior.
	 *
	 * @param component
	 *            - the component under analysis
	 * @param state
	 *            - the failure mode under analysis
	 * @param type
	 *            - the type related to the failure mode (null if not useful)
	 * @return - a node that represents either the single failure state or an
	 *         AND- or OR- nodes if several.
	 */
	public Event processErrorState(ComponentInstance component, ErrorBehaviorState state, ErrorTypes type) {

		Event compositeBehaviorEvent;
		Event componentBehaviorEvent;

		List<Event> subEvents = new ArrayList<Event>();

		componentBehaviorEvent = processComponentErrorBehavior(component, state, type);
		if (componentBehaviorEvent != null) {
			subEvents.add(componentBehaviorEvent);
		}

		compositeBehaviorEvent = processCompositeErrorBehavior(component, state, type);
		if (compositeBehaviorEvent != null) {
			subEvents.add(compositeBehaviorEvent);
		}

		if (subEvents.size() == 0) {
			Event errorStateEvent = this.createEvent(component, state, state.getTypeSet());
			errorStateEvent.setType(EventType.BASIC);
			Utils.fillProperties(errorStateEvent, component, state, state.getTypeSet());
			errorStateEvent.setGate(null);
			return errorStateEvent;
		}

		if (subEvents.size() == 1) {
			return subEvents.get(0);
		}

		if (subEvents.size() > 0) {
			Event errorStateEvent = this.createEvent(component, state, state.getTypeSet());
			errorStateEvent.setType(EventType.BASIC);
			Utils.fillProperties(errorStateEvent, component, state, state.getTypeSet());
			Gate emftaGate = EmftaFactory.eINSTANCE.createGate();
			emftaGate.setType(GateType.OR);
			errorStateEvent.setGate(emftaGate);

			for (Event e : subEvents) {
				emftaGate.getEvents().add(e);
			}
			return errorStateEvent;
		}

		return null;

	}

}
