package edu.cmu.emfta.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.emfta.EmftaFactory;
import edu.cmu.emfta.Event;
import edu.cmu.emfta.FTAModel;
import edu.cmu.emfta.Gate;
import edu.cmu.emfta.GateType;

public class OptimizeLogic {
	private Event 			rootEvent;
	private List<Event> 	browsedElements;
	private boolean 		removeUselessOr;
	private boolean			factorize;
	
	public OptimizeLogic (Event root, boolean _removeUselessOr, boolean _factorize)
	{
		this.rootEvent = root;
		this.factorize = _factorize;
		this.removeUselessOr = _removeUselessOr;
		
		browsedElements = new ArrayList<Event>();
	}
	
	public void perform ()
	{
		if (this.removeUselessOr)
		{
			optimizeCommonOrEvents (rootEvent);
		}
		
		if (this.factorize)
		{
			factorize (rootEvent);
		}
	}
	
	
	
	private void factorize (Event event)
	{
		/**
		 * canBeOptimized specifies if we can/should optimize this
		 * gate. The boolean will be true only if the gate is an AND gate
		 * with multiple ORed.
		 */
		boolean canBeOptimized;
		GateType mainGateType;
		GateType subGateType;
		
		
		Gate gate;
		FTAModel model;
		
		
		model = (FTAModel) event.eContainer();

		
		gate = event.getGate();
		canBeOptimized = false;
				
		if (gate == null)
		{
			return;
		}
		
		mainGateType = gate.getType();
		subGateType = null;
		
		/**
		 * Here, we factorize only the AND type
		 */
		
		canBeOptimized = true;
			
		for (Event subEvent : gate.getEvents())
		{
			Gate subGate = subEvent.getGate();
			
			if ((subGate != null) && (subGateType == null))
			{
				subGateType = subGate.getType();
			}
				
			if ( (subGate != null) && (subGate.getType() != subGateType))
			{
				canBeOptimized = false;
			}
		}
		
		if (subGateType == mainGateType)
		{
			return;
		}
		
		/**
		 * If we enter there, it means that we have an AND gate that is composed
		 * of OR sub-gate only.
		 */
		if (canBeOptimized)
		{
			Set<Event> allTerminalEvents;
			Set<Event> toFactorize;
			
			System.out.println("This gate can be optimized");
			
			allTerminalEvents = new HashSet<Event> ();
			toFactorize = new HashSet<Event> ();
			
			/**
			 * Here, we retrieve all the subEvents that can be factorized
			 * on the sub-gates
			 */
			for (Event subEvent : gate.getEvents())
			{
				Gate subGate = subEvent.getGate();
				
				if (subGate != null)
				{
					for (Event subSubEvent : subGate.getEvents())
					{
						if(allTerminalEvents.contains(subSubEvent))
						{
							toFactorize.add(subSubEvent);
						}
						allTerminalEvents.add(subSubEvent);
					}
				}
			}
			
			/**
			 * If there are common events, then, we factorize
			 */
			if (toFactorize.size() > 0)
			{
//				for (Event e : toFactorize)
//				{
//					System.out.println("Event " + e.getName() + " can be factorized\n");
//				}
				
				/**
				 * First, we change the main gate type and set the type
				 * to the same type as the sub gate.
				 */
				gate.setType(subGateType);
				
				/**
				 * We remove all the intermediate sub-events of the main gate.
				 */
				for (Event e : gate.getEvents())
				{
					model.getEvents().remove(e);
				}
				
				/**
				 * We remove all the children from the gate and we will then
				 * add manually all the common terminal events and build
				 * a new AND gate manually.
				 */
				gate.getEvents().clear();
				
				
				/**
				 * Add all the events to be factorize
				 */
				for (Event e : toFactorize)
				{
					gate.getEvents().add(e);
				}
				
				
				/**
				 * For all the events that are NOT factorized, we add them under a common
				 * gate created manually that has the type from the original main Gate (top-level gate).
				 */
				Event intermediateEvent = EmftaFactory.eINSTANCE.createEvent();
				Gate intermediateGate = EmftaFactory.eINSTANCE.createGate();
				
				intermediateEvent.setName("INTERMEDIATE");
				intermediateGate.setType(mainGateType);
				intermediateEvent.setGate(intermediateGate);
				
				gate.getEvents().add(intermediateEvent);
				
				model.getEvents().add(intermediateEvent);
				
				for (Event e1 : allTerminalEvents)
				{
					if (toFactorize.contains(e1) == false)
					{
						intermediateGate.getEvents().add (e1);
					}
				}
			}
		}
		
		
		/**
		 * We continue and browse sub-events.
		 */
		for (Event subEvent : gate.getEvents())
		{
			factorize (subEvent);
		}
	}
	

	
	/**
	 * Optimize event that are under several OR'd gates. We start with the top, collect
	 * all events that are under OR gates and remove/delete events that are in deeper
	 * or gates.
	 * 
	 * @param event - the event under investigation. the code will browse sub-events.
	 */
	private void optimizeCommonOrEvents (Event event)
	{
//		System.out.println("[OptimizationAction] process: "+event.getName());

		Gate gate = event.getGate();
		List<Event> toDelete = new ArrayList<Event>();

		/**
		 * if there is no event, we do not go further
		 */
		if (gate == null)
		{
			return;
		}
		

		/**
		 * We start to see if there is any redundant events in the sub-events.
		 * We add the events to delete in a list, because if we delete that
		 * directly, it might generate inconsistencies in the list members
		 * and the iterator that browses the getEvents() call.
		 */
		for (Event subEvent : gate.getEvents())
		{
			if (browsedElements.contains(subEvent))
			{
				toDelete.add(subEvent);
			}
		}
		
		/**
		 * Now, we delete the events from the list.
		 */
		if (toDelete.size() > 0)
		{
			for (Event del : toDelete)
			{
//				System.out.println("[OptimizationAction] delete: "+del.getName());
				gate.getEvents().remove(del);
			}
		}
		
		/**
		 * If this is an OR gate, we then add the events as being already browsed.
		 * Then, these elements will be removed from further/deeper OR'd gates.
		 */
		if (gate.getType() == GateType.OR)
		{
			for (Event subEvent : gate.getEvents())
			{
				if (! browsedElements.contains(subEvent))
				{
					browsedElements.add(subEvent);
				}
			}
		}
		
		/**
		 * We continue and browse sub-events.
		 */
		for (Event subEvent : gate.getEvents())
		{
			optimizeCommonOrEvents (subEvent);
		}
		
	}
}
