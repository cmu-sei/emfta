package edu.cmu.emfta.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import edu.cmu.emfta.EmftaFactory;
import edu.cmu.emfta.Event;
import edu.cmu.emfta.EventType;
import edu.cmu.emfta.FTAModel;
import edu.cmu.emfta.Gate;
import edu.cmu.emfta.GateType;

public class OptimizeLogic {
	private Event 			rootEvent;
	private boolean 		removeUselessOr;
	private boolean			factorize;
	private boolean			expand;
	
	public OptimizeLogic (Event root, boolean _removeUselessOr, boolean _factorize, boolean _expand)
	{
		this.rootEvent = root;
		this.factorize = _factorize;
		this.expand = _expand;
		this.removeUselessOr = _removeUselessOr;
	}
	
	/**
	 * return the FTAModel associated with an event
	 * @param e
	 * @return
	 */
	public FTAModel getModel (Event e)
	{
		Object o = e;
		while (! (o instanceof FTAModel))
		{
			o = e.eContainer();
		}
		return (FTAModel) o;
	}
	
	/**
	 * return the max height of a subtree
	 * @param e - the root of the subtree
	 * @return the height of the new subtree
	 */
	public int getMaxHeight (Event e)
	{
		int max = 0;
		if(e.getGate() == null)
		{
			return 0;
		}
		else
		{
			Gate gate = e.getGate();
			for (Event subEvent : gate.getEvents())
			{
				max = Math.max(getMaxHeight(subEvent), max);
			}
		}
		max++;
		return max;
		
	}
	
	/**
	 * isInList indicates if an event is in a list according
	 * to the event named. Because list.contains(e) will do the
	 * comparison according to the object reference, we would like
	 * to do this according to the event name that should be
	 * unique in the system.
	 * @param event the event to test
	 * @param list a list of event
	 * @return true if the first parameter is in the list based on the event name
	 */
	public boolean isInList (Event event, List<Event> list)
	{
		for (Event e : list)
		{
			if (e.getName().equalsIgnoreCase(event.getName())) 
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	
	public void perform () throws Exception
	{
		if (this.expand)
		{
			expand (rootEvent);
		}
		
		if (this.removeUselessOr)
		{
			optimizeCommonOrEvents (rootEvent, new ArrayList<Event>() , new Stack<Event>());
		}
		
		if (this.factorize)
		{
			factorize (rootEvent);
		}
	}
	
	/**
	 * Build an expanded list with all the AND gates nodes below.
	 * The result is a list of all potential list of event combination that
	 * is possible for a particular set. The number of combination is given
	 * with nbEventsPerList.
	 * 
	 * @param events - list of events to combine
	 * @param nbEventsPerList - number of events per combination
	 * @param result - the result
	 * @param stack - temporary stack to process to compute the list.
	 */
	private void buildExpandedList (List<Event> events, int nbEventsPerList, List<List<Event>> result, Stack<Event> stack)
	{
		if (stack.size() == nbEventsPerList)
		{
			List<Event> l = new ArrayList<Event> (nbEventsPerList);
			l.addAll(stack);
			
			/**
			 * Check if the sub-tree was already added or not.
			 * It avoids to add the following sub-tree: (B and C), (C and B) 
			 */
			for (List<Event> tmp : result)
			{
				if (tmp.containsAll(l) && l.containsAll(tmp))
				{
					return;
				}
			}
			
			result.add(l);
			return;
		}
		
		for (Event e : events)
		{
			if (! stack.contains(e))
			{
				stack.push(e);
				buildExpandedList(events, nbEventsPerList, result, stack);
				stack.pop();
			}
		}
	}
	
	private List<List<Event>> buildExpandedList (List<Event> events, int nbEventsPerList)
	{
		List<List<Event>> result = new ArrayList<List<Event>>();
		buildExpandedList(events, nbEventsPerList, result, new Stack<Event> ());
		return result;
	}
	
	/**
	 * expand will expand the initial event that has a gate with ormore or orless
	 * GateType. The ormore type will be expanded with a combination of AND gates.
	 * In other words, it transforms a single ormore gate into a more complex
	 * construct with OR and AND gates.
	 * @param event
	 */
	private void expand (Event event) throws Exception
	{
		if (event == null)
		{
			return;
		}
		
		FTAModel model = getModel (event);
		Gate gate = event.getGate();
		int nbOccurrences = gate.getNbOccurrences();
		
		if (gate == null)
		{
			return;
		}
		
		if (gate.getType() == GateType.ORMORE)
		{
			List<Event> gateEvents = new ArrayList<Event> ();
			for (Event e : gate.getEvents())
			{
				gateEvents.add(e);
			}
			
			if (nbOccurrences >= gateEvents.size())
			{
				throw new Exception ("Cannot expand - need more subevents");
			}
			
			List<List<Event>> expandedLists = buildExpandedList (gateEvents, nbOccurrences);

			if ((expandedLists == null) || (expandedLists.size() == 0))
			{
				throw new Exception ("Cannot expand - need more subevents");	
			}
			
			gate.setType(GateType.OR);
			for (Event e : gateEvents)
			{
				gate.getEvents().remove(e);
			}
			
			for (List<Event> l : expandedLists)
			{
				Event intermediateEvent = EmftaFactory.eINSTANCE.createEvent();
				model.getEvents().add(intermediateEvent);
				gate.getEvents().add(intermediateEvent);
				intermediateEvent.setName("Intermediate event");
				Gate newGate = EmftaFactory.eINSTANCE.createGate();
				intermediateEvent.setGate(newGate);
				newGate.setType(GateType.AND);
				newGate.getEvents().addAll(l);
			}
		}
		
		if (gate.getType() == GateType.ORLESS)
		{
			List<Event> gateEvents = new ArrayList<Event> ();
			for (Event e : gate.getEvents())
			{
				gateEvents.add(e);
			}
			
			if (nbOccurrences >= gateEvents.size())
			{
				throw new Exception ("Cannot expand - need more subevents");
			}
			
			List<List<Event>> expandedLists = new ArrayList<List<Event>> ();
			for (int i = 2 ; i <= nbOccurrences ; i++)
			{
				expandedLists.addAll(buildExpandedList (gateEvents, i));
			}
			
			
			if ((expandedLists == null) || (expandedLists.size() == 0))
			{
				throw new Exception ("Cannot expand - need more subevents");	
			}
			
			gate.setType(GateType.OR);

			
			for (List<Event> l : expandedLists)
			{
				Event intermediateEvent = EmftaFactory.eINSTANCE.createEvent();
				model.getEvents().add(intermediateEvent);
				gate.getEvents().add(intermediateEvent);
				intermediateEvent.setName("Intermediate event");
				Gate newGate = EmftaFactory.eINSTANCE.createGate();
				intermediateEvent.setGate(newGate);
				newGate.setType(GateType.AND);
				newGate.getEvents().addAll(l);
			}
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
		
		
		model = getModel(event);

		
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
	 * @param browsedElements - all events that have been browsed during the optimization.
	 * @param stack - stack of events that have been traversed so far. Used to see if after
	 *                optimization, only one event remains and if we can then delete the
	 *                intermediate event. This does not contains terminal elements/events.
	 */
	private void optimizeCommonOrEvents (Event event, List<Event> browsedElements, Stack<Event> stack)
	{

		Gate gate = event.getGate();
		List<Event> toDelete = new ArrayList<Event>();
		List<Event> gateEvents;
		
		/**
		 * if there is no event, we do not go further
		 */
		if (gate == null)
		{
			return;
		}
		
		gateEvents = gate.getEvents();

		/**
		 * We start to see if there is any redundant events in the sub-events.
		 * We add the events to delete in a list, because if we delete that
		 * directly, it might generate inconsistencies in the list members
		 * and the iterator that browses the getEvents() call.
		 */
		for (Event subEvent : gateEvents)
		{
			if (isInList(subEvent, browsedElements))
			{
//				System.out.println("[OptimizationAction] should delete: "+subEvent.getName());
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
				gateEvents.remove(del);
				getModel(event).getEvents().remove(del);
			}
		}
		
		/**
		 * If this is an OR gate, we then add the events as being already browsed.
		 * Then, these elements will be removed from further/deeper OR'd gates.
		 */
		if (gate.getType() == GateType.OR)
		{
			for (Event subEvent : gateEvents)
			{
				if (! isInList(subEvent, browsedElements))
				{
					browsedElements.add(subEvent);
				}
			}
			
			/**
			 * We continue and browse sub-events. We apply the optimization
			 * logic. Then, if the resulting gate has only one event, we delete
			 * the gate and add the number on the top-gate.
			 */
			stack.push(event);
			for (int i = 0 ; i < gateEvents.size() ; i++)
			{
				Event subEvent = gateEvents.get(i);
				optimizeCommonOrEvents (subEvent, browsedElements, stack);

			}
			stack.pop();
			
			if (gate.getEvents().size() == 1)
			{
				Event topEventWithOr = stack.peek();
				if (topEventWithOr != null)
				{
					topEventWithOr.getGate().getEvents().addAll(gate.getEvents());
					event.setGate (null); 
				}
			}
			
			/**
			 * Clean the tree. If the tree has sub-events with only one child
			 * or intermediate events that no longer have a gate, we remove
			 * them automatically.
			 */
			for (Event subEvent : gateEvents)
			{
				if (
						( (subEvent.getType() == EventType.INTERMEDIATE) && (subEvent.getGate() != null) && (subEvent.getGate().getEvents().size() == 0)) ||
						( (subEvent.getType() == EventType.INTERMEDIATE) && (subEvent.getGate() == null))
					)
				{
					gateEvents.remove(subEvent);
					getModel(event).getEvents().remove(subEvent);
				}	 
			}
//			System.out.println("event=" + event.getName() + " height=" + getMaxHeight(event));
			
			/**
			 * Next, we "flatten" the FTA by removing intermediate event. 
			 */
			if (getMaxHeight(event) == 2)
			{
				List<Event> delete = new ArrayList<Event>();
				
				for (Event subEvent : gateEvents)
				{
					if (subEvent.getGate() == null)
					{
						continue;
					}
					
					if (subEvent.getGate().getType() != GateType.OR)
					{
						continue;
					}
					
					delete.add(subEvent);
				}
				
				for (Event e : delete)
				{
					event.getGate().getEvents().remove(e);
					getModel(event).getEvents().remove(e);
					event.getGate().getEvents().addAll(e.getGate().getEvents());
				}
			}
		}
		
		
		if (gate.getType() == GateType.AND)
		{
			/**
			 * TODO - fix the AND gate. This code has been written bu not tested. One
			 * should test it before release.
			 * The idea here is that a AND gate is a new context. We cannot reuse the
			 * same set of browsed element. So, we start a new context with an empty list
			 * of browsed elements.
			 */
			for (Event subEvent : gateEvents)
			{
				optimizeCommonOrEvents  (subEvent, new ArrayList<Event> (), new Stack<Event>());
			}
		}
	}
}
