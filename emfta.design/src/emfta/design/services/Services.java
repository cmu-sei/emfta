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
		if (context instanceof FTAModel){
			EList<Event> AllEvents = ((FTAModel)context).getEvents();
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
		}else if (context instanceof Gate){
			eventsToReturn.addAll(((Gate)context).getEvents());
		}
		return eventsToReturn;
	}
}
