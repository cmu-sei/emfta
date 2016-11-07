/**
 */
package edu.cmu.emfta.impl;

import edu.cmu.emfta.EmftaPackage;
import edu.cmu.emfta.Event;
import edu.cmu.emfta.Gate;
import edu.cmu.emfta.GateType;
import java.util.Collection;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Gate</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link edu.cmu.emfta.impl.GateImpl#getType <em>Type</em>}</li>
 *   <li>{@link edu.cmu.emfta.impl.GateImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link edu.cmu.emfta.impl.GateImpl#getEvents <em>Events</em>}</li>
 *   <li>{@link edu.cmu.emfta.impl.GateImpl#getNbOccurrences <em>Nb Occurrences</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GateImpl extends MinimalEObjectImpl.Container implements Gate {
	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final GateType TYPE_EDEFAULT = GateType.OR;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected GateType type = TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getEvents() <em>Events</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEvents()
	 * @generated
	 * @ordered
	 */
	protected EList<Event> events;

	/**
	 * The default value of the '{@link #getNbOccurrences() <em>Nb Occurrences</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNbOccurrences()
	 * @generated
	 * @ordered
	 */
	protected static final int NB_OCCURRENCES_EDEFAULT = 1;

	/**
	 * The cached value of the '{@link #getNbOccurrences() <em>Nb Occurrences</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNbOccurrences()
	 * @generated
	 * @ordered
	 */
	protected int nbOccurrences = NB_OCCURRENCES_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GateImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return EmftaPackage.Literals.GATE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GateType getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(GateType newType) {
		GateType oldType = type;
		type = newType == null ? TYPE_EDEFAULT : newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EmftaPackage.GATE__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EmftaPackage.GATE__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Event> getEvents() {
		if (events == null) {
			events = new EObjectResolvingEList<Event>(Event.class, this, EmftaPackage.GATE__EVENTS);
		}
		return events;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getNbOccurrences() {
		return nbOccurrences;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNbOccurrences(int newNbOccurrences) {
		int oldNbOccurrences = nbOccurrences;
		nbOccurrences = newNbOccurrences;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EmftaPackage.GATE__NB_OCCURRENCES, oldNbOccurrences, nbOccurrences));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case EmftaPackage.GATE__TYPE:
				return getType();
			case EmftaPackage.GATE__DESCRIPTION:
				return getDescription();
			case EmftaPackage.GATE__EVENTS:
				return getEvents();
			case EmftaPackage.GATE__NB_OCCURRENCES:
				return getNbOccurrences();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case EmftaPackage.GATE__TYPE:
				setType((GateType)newValue);
				return;
			case EmftaPackage.GATE__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case EmftaPackage.GATE__EVENTS:
				getEvents().clear();
				getEvents().addAll((Collection<? extends Event>)newValue);
				return;
			case EmftaPackage.GATE__NB_OCCURRENCES:
				setNbOccurrences((Integer)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case EmftaPackage.GATE__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case EmftaPackage.GATE__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case EmftaPackage.GATE__EVENTS:
				getEvents().clear();
				return;
			case EmftaPackage.GATE__NB_OCCURRENCES:
				setNbOccurrences(NB_OCCURRENCES_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case EmftaPackage.GATE__TYPE:
				return type != TYPE_EDEFAULT;
			case EmftaPackage.GATE__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case EmftaPackage.GATE__EVENTS:
				return events != null && !events.isEmpty();
			case EmftaPackage.GATE__NB_OCCURRENCES:
				return nbOccurrences != NB_OCCURRENCES_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (type: ");
		result.append(type);
		result.append(", description: ");
		result.append(description);
		result.append(", nbOccurrences: ");
		result.append(nbOccurrences);
		result.append(')');
		return result.toString();
	}

} //GateImpl
