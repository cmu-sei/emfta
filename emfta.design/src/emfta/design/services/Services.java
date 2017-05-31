package emfta.design.services;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import edu.cmu.emfta.Event;
import edu.cmu.emfta.FTAModel;
import edu.cmu.emfta.Gate;

public class Services {

	public EList<Event> getEvents(EObject context) {
		EList<Event> eventsToReturn = new BasicEList<Event>();
		if (context instanceof FTAModel) {
			EList<Event> AllEvents = ((FTAModel) context).getEvents();
			EList<Event> firstLevelEvents = new BasicEList<Event>();
			firstLevelEvents.addAll(AllEvents);
			for (Event event : AllEvents) {
				if (event.getGate() != null) {
					if (AllEvents.containsAll(event.getGate().getEvents())) {
						firstLevelEvents.removeAll(event.getGate().getEvents());
					}
				}
			}
			eventsToReturn.addAll(firstLevelEvents);
		} else if (context instanceof Gate) {
			eventsToReturn.addAll(((Gate) context).getEvents());
		}
		return eventsToReturn;
	}

	public EList<Event> getCutsets(EObject context) {
		EList<Event> eventsToReturn = new BasicEList<Event>();
		if (context instanceof FTAModel) {
			eventsToReturn.add(((FTAModel) context).getRoot());
		} else if (context instanceof Event && ((Event) context).getGate() != null) {
			eventsToReturn.addAll(((Event) context).getGate().getEvents());
		}
		return eventsToReturn;
	}

	public EList<EObject> getChildren(EObject context) {
		EList<EObject> childrenToReturn = new BasicEList<EObject>();
		if (context instanceof FTAModel) {
			childrenToReturn.add(((FTAModel) context).getRoot());
		} else if (context instanceof Event) {
			childrenToReturn.add(((Event) context).getGate());
		} else if (context instanceof Gate) {
			childrenToReturn.addAll(((Gate) context).getEvents());
		}
		return childrenToReturn;
	}

	public String ProbabilityToString(EObject context) {
		if (context instanceof Event) {
			double val = ((Event) context).getProbability();
			String name = ((Event) context).getName();
			return String.format("%1$s\n(%2$.3E)", name, val);
		}
		return "";
	}

}
