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
import org.osate.aadl2.ComponentClassifier;
import org.osate.aadl2.DirectionType;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.util.OsateDebug;
import org.osate.xtext.aadl2.errormodel.errorModel.AndExpression;
import org.osate.xtext.aadl2.errormodel.errorModel.BranchValue;
import org.osate.xtext.aadl2.errormodel.errorModel.CompositeState;
import org.osate.xtext.aadl2.errormodel.errorModel.ConditionElement;
import org.osate.xtext.aadl2.errormodel.errorModel.ConditionExpression;
import org.osate.xtext.aadl2.errormodel.errorModel.EMV2Path;
import org.osate.xtext.aadl2.errormodel.errorModel.EMV2PropertyAssociation;
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
import org.osate.xtext.aadl2.errormodel.errorModel.QualifiedErrorBehaviorState;
import org.osate.xtext.aadl2.errormodel.errorModel.SConditionElement;
import org.osate.xtext.aadl2.errormodel.errorModel.TransitionBranch;
import org.osate.xtext.aadl2.errormodel.errorModel.TypeSet;
import org.osate.xtext.aadl2.errormodel.util.AnalysisModel;
import org.osate.xtext.aadl2.errormodel.util.EM2TypeSetUtil;
import org.osate.xtext.aadl2.errormodel.util.EMV2Properties;
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
	private Event createEvent(ComponentInstance component, NamedElement namedElement, ErrorTypes type) {
		Event newEvent = EmftaFactory.eINSTANCE.createEvent();
		String name = buildName(component, namedElement, type);
		emftaModel.getEvents().add(newEvent);
		newEvent.setName(name);
		newEvent.setType(EventType.BASIC);
		return newEvent;
	}

	/**
	 * create an intermediate Event
	 * @return
	 */
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
				emftaRootEvent = processCompositeErrorStates(rootComponent, rootComponentState, rootComponentTypes);
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
		Event result = getFromCache(component, errorPropagation, targetType);
		if (result != null) {
			return result;
		}
		result = processOutgoingErrorPropagationConditions(component, errorPropagation, targetType);
		if (result == null) {
			result = processReverseErrorFlow(component, errorPropagation, targetType);
		}
		if (result != null) {
			result.setType(EventType.BASIC);
			result.setName(buildName(component, errorPropagation, type));
			putInCache(component, errorPropagation, type, result);
		}
		return result;
	}

	/**
	 * determine the target type given the original type of a backward propagation.
	 * Use the original if no constraint was provided (null)
	 * If the original is contained in the constraint use it. 
	 * If not use the constraint as it may represent a mapping, e.g., for an error path
	 * @param constraint ErrorTypes that is expected on the left hand side
	 * @param original ErrroTypes that is the actual origin of the backward proapagation
	 * @return ErrorTypes
	 */
	private ErrorTypes getTargetType(ErrorTypes constraint, ErrorTypes original) {
		if (constraint == null)
			return original;
		if (original == null)
			return constraint;
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
		Event found = getFromCache(component, propagation, type);
		if (found != null) {
			return found;
		}
		List<Event> subEvents = new ArrayList<Event>();
		for (OutgoingPropagationCondition opc : EMV2Util.getAllOutgoingPropagationConditions(component)) {
			Event conditionEvent = null;
			if (EMV2Util.isSame(opc.getOutgoing(), propagation)) {
				ConditionExpression conditionExpression = opc.getCondition();
				if (conditionExpression != null) {
					OsateDebug.osateDebug("condition expression" + conditionExpression);
					conditionEvent = processCondition(component, conditionExpression, type);
				}
			}
			Event stateEvent = processErrorBehaviorState(component, opc.getState(), type);
			Event consolidated = consolidateAsAnd(stateEvent, conditionEvent);
			if (consolidated != null) {
				subEvents.add(consolidated);
			}
		}
		Event result = finalizeAsGatedEvents(subEvents, GateType.OR);
		return result;
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

	/**
	 * process error state according to transitions. Recursively deal with source states of transitions (an AND gate).
	 * We only process error events (not recover or repair) and error propagations referenced by the expression.
	 * @param component ComponentInstance
	 * @param state ErrorBehaviorState
	 * @param type ErrorTypes
	 * @return event
	 */
	public Event processErrorBehaviorState(ComponentInstance component, ErrorBehaviorState state, ErrorTypes type) {
		List<Event> subEvents = new ArrayList<Event>();
		if (state == null) {
			return null;
		}
		for (ErrorBehaviorTransition ebt : EMV2Util.getAllErrorBehaviorTransitions(component)) {
			if (!EMV2Util.isSame(ebt.getSource(), state)) {
				Event conditionEvent = null;
				ConditionExpression conditionExpression = null;
				double scale = 1;
				if (ebt.getTarget() != null && EMV2Util.isSame(state, ebt.getTarget())) {
					conditionExpression = ebt.getCondition();
				} else {
					// deal with transition branches
					EList<TransitionBranch> tbs = ebt.getDestinationBranches();
					for (TransitionBranch transitionBranch : tbs) {
						if (EMV2Util.isSame(transitionBranch.getTarget(), state)) {
							conditionExpression = ebt.getCondition();
							BranchValue val = transitionBranch.getValue();
							if (val.getRealvalue() != null) {
								scale = Double.valueOf(val.getRealvalue());
							} else if (val.getSymboliclabel() != null) {
								ComponentClassifier cl = EMV2Util.getAssociatedClassifier(ebt);
								List<EMV2PropertyAssociation> pa = EMV2Properties
										.getProperty(val.getSymboliclabel().getQualifiedName(), cl, ebt, null);
								for (EMV2PropertyAssociation emv2PropertyAssociation : pa) {
									scale = scale + EMV2Properties.getRealValue(emv2PropertyAssociation);
								}
							}
						}
					}
				}
				if (conditionExpression != null) {
					conditionEvent = processCondition(component, conditionExpression, type, scale);
				}
				Event stateEvent = EMV2Util.isSame(ebt.getSource(), state) ? null
						: processErrorBehaviorState(component, ebt.getSource(), type);
// PHF: without this trick we get a null as subevent element
				if (stateEvent != null && stateEvent.getType() == EventType.UNDEVELOPPED) {
					// operational state that has not been entered by an error event is ignored.
					removeEvent(stateEvent);
					stateEvent = null;
				}
				// previous contributors as probability product
				Event consolidated = consolidateAsAnd(stateEvent, conditionEvent);
				if (consolidated != null) {
					subEvents.add(consolidated);
				}
			}
		}
		Event result = finalizeAsGatedEvents(subEvents, GateType.OR);
		if (result == null) {
			// PHF: without this trick we get a null as subevent element
			// create a state Event to indicate operational state as source.
			result = createEvent(component, state, type);
			result.setType(EventType.UNDEVELOPPED);
		}
		return result;
	}

	/**
	 * turn list of subevents into an specified gate.
	 * In the process flatten any sub gates of the same tpye (one level is sufficient since we flatten at each step
	 * @param subEvents List<Event>
	 * @return Event (or null if empty list)
	 */
	private Event finalizeAsGatedEvents(List<Event> subEvents, GateType gt) {
		if (subEvents.size() == 0)
			return null;
		if (subEvents.size() == 1) {
			return subEvents.get(0);
		}
		Event combined = this.createIntermediateEvent();
		Gate emftaGate = EmftaFactory.eINSTANCE.createGate();
		emftaGate.setType(gt);

		combined.setGate(emftaGate);
		// flatten
		for (Event se : subEvents) {
			if (se.getGate() != null && se.getGate().getType() == gt) {
				emftaGate.getEvents().addAll(se.getGate().getEvents());
				removeEvent(se);
			} else {
				emftaGate.getEvents().add(se);
			}
		}
		return combined;
	}

	private boolean hasAndGate(Event event) {
		return event.getGate() != null && event.getGate().getType() == GateType.AND;
	}

	private boolean hasOrGate(Event event) {
		return event.getGate() != null && event.getGate().getType() == GateType.OR;
	}

	private Event pullUpCommonAndEvents(Event root) {
		if (root.getGate() == null)
			return root;
		Gate rootGate = root.getGate();
		if (rootGate.getEvents().isEmpty()) {
			root.setGate(null);
			return root;
		}
		if (rootGate.getType() == GateType.AND) {
			for (Event event : rootGate.getEvents()) {

			}
		}

		return root;
	}

	/**
	 * process error flows in reverse direction that match the outgoing error propagation.
	 * The flows can be a path or source and are treated as an OR. 
	 * An error source is treated as an BASIC event with probability values on the error source
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
						Event result = processIncomingErrorPropagation(component, ep.getIncoming(), type);
						if (result != null) {
							subEvents.add(result);
						}
					}
				} else if (ef instanceof ErrorSource) {
					ErrorSource errorSource = (ErrorSource) ef;

					if (EMV2Util.isSame(errorSource.getOutgoing(), errorPropagation)) {
						if (EM2TypeSetUtil.contains(errorSource.getTypeTokenConstraint(), type)) {

							Event newEvent = this.createEvent(component, errorSource, ef.getTypeTokenConstraint());
							newEvent.setType(EventType.BASIC);
							Utils.fillProperties(newEvent, component, errorSource, ef.getTypeTokenConstraint());
							subEvents.add(newEvent);
						}
					}
				}
			}
		}
		return finalizeAsGatedEvents(subEvents, GateType.OR);
	}

	/**
	 * process an incoming error propagation.
	 * Follow any propagation path according to the AnalysisModel.
	 * If none are found use the incoming error propagation as an EXTERNAL event with probability value.
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
			}

		}
		// create event to represent external incoming.
		if (subEvents.isEmpty()) {
			Event emftaEvent = createEvent(component, errorPropagation, type);
			emftaEvent.setType(EventType.EXTERNAL);
			emftaEvent.setDescription(Utils.getDescription(component, errorPropagation, type));
			Utils.fillProperties(emftaEvent, component, errorPropagation, type);
			return emftaEvent;
		}
		return finalizeAsGatedEvents(subEvents, GateType.OR);
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
	public Event processCondition(ComponentInstance component, ConditionExpression condition, ErrorTypes type) {
		return processCondition(component, condition, type, 1);
	}

	public Event processCondition(ComponentInstance component, ConditionExpression condition, ErrorTypes type,
			double scale) {

		// OsateDebug.osateDebug("[EmftaWrapper] condition=" + condition);

		/**
		 * We have an AND expression, so, we create an EVENT to AND' sub events.
		 */
		if (condition instanceof AndExpression) {
			AndExpression expression = (AndExpression) condition;
			List<Event> subEvents = new ArrayList<Event>();

			for (ConditionExpression ce : expression.getOperands()) {
				subEvents.add(processCondition(component, ce, type, scale));
			}

			return finalizeAsGatedEvents(subEvents, GateType.AND);
		}

		if (condition instanceof OrExpression) {
			OrExpression expression = (OrExpression) condition;
			List<Event> subEvents = new ArrayList<Event>();

			for (ConditionExpression ce : expression.getOperands()) {
				subEvents.add(processCondition(component, ce, type, scale));
			}
			return finalizeAsGatedEvents(subEvents, GateType.OR);
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
					QualifiedErrorBehaviorState qs = sconditionElement.getQualifiedState();
					ComponentInstance referencedInstance = EMV2Util.getLastComponentInstance(qs, component);
					Event result = null;
					ErrorTypes referencedErrorType = getTargetType(sconditionElement.getConstraint(), type);
					if (referencedInstance != null) {
						result = processCompositeErrorStates(referencedInstance, EMV2Util.getState(sconditionElement),
								referencedErrorType);
					}
					if (result != null) {
						return result;
					}
					return processErrorBehaviorState(referencedInstance, EMV2Util.getState(sconditionElement),
							referencedErrorType);
				}

			}

			if (conditionElement.getQualifiedErrorPropagationReference() != null) {
				EMV2Path path = conditionElement.getQualifiedErrorPropagationReference();

				ComponentInstance relatedComponent = EMV2Util.getLastComponentInstance(path, component);
				NamedElement errorModelElement = EMV2Util.getErrorModelElement(path);
				/**
				 * Here, we have an error event. Likely, this is something we
				 * can get when we are analyzing error component behavior.
				 */
				if (errorModelElement instanceof ErrorEvent) {
					ErrorEvent errorEvent;
					Event emftaEvent;

					errorEvent = (ErrorEvent) errorModelElement;
					emftaEvent = this.createEvent(relatedComponent, errorEvent, errorEvent.getTypeSet());
					ErrorTypes referencedErrorType = getTargetType(conditionElement.getConstraint(), type);
					Utils.fillProperties(emftaEvent, relatedComponent, errorEvent, referencedErrorType, scale);

					return emftaEvent;
				}

				/**
				 * Here, we have an error propagation. This is notified with the
				 * in propagation within a composite error model.
				 */
				if (errorModelElement instanceof ErrorPropagation) {
					ErrorPropagation errorPropagation = (ErrorPropagation) errorModelElement;
					if (errorPropagation.getDirection() == DirectionType.IN) {
						return processIncomingErrorPropagation(relatedComponent, errorPropagation,
								conditionElement.getConstraint());
					} else {
						return processOutgoingErrorPropagation(relatedComponent, errorPropagation,
								conditionElement.getConstraint());
					}
				}

			}
		}
		return null;

	}

	/**
	 * Process composite error states who target is the specified state.
	 * We may process more than one composite state declaration if the error type is matched by more than one composite target.
	 * recursively descend composite state declarations
	 * For each composite state declaration also follow incoming propagations.
	 * For each leaf state look up the occurrence probability
	 *
	 * @param component
	 *            - the component under analysis
	 * @param state
	 *            - the target state under analysis
	 * @param type
	 *            - the type associated to the target state (if any)
	 * @return - Event
	 */
	public Event processCompositeErrorStates(ComponentInstance component, ErrorBehaviorState state, ErrorTypes type) {
		List<Event> subEvents;
		subEvents = new ArrayList<Event>();
		// should only match one composite state declaration.
		for (CompositeState cs : EMV2Util.getAllCompositeStates(component)) {
			if (cs.getState() == state) {
				subEvents.add(processCondition(component, cs.getCondition(), type));
			}
		}
		Event result = finalizeAsGatedEvents(subEvents, GateType.OR);
		if (result != null && result.getType() == EventType.INTERMEDIATE) {
			result.setType(EventType.BASIC);
			result.setName(buildName(component, state, type));
		}
		return result;
	}

}
