/**
 */
package edu.cmu.emfta.impl;

import edu.cmu.emfta.EmftaPackage;
import edu.cmu.emfta.Event;
import edu.cmu.emfta.EventType;
import edu.cmu.emfta.Gate;
import java.util.Collection;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Event</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link edu.cmu.emfta.impl.EventImpl#getType <em>Type</em>}</li>
 *   <li>{@link edu.cmu.emfta.impl.EventImpl#getName <em>Name</em>}</li>
 *   <li>{@link edu.cmu.emfta.impl.EventImpl#getProbability <em>Probability</em>}</li>
 *   <li>{@link edu.cmu.emfta.impl.EventImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link edu.cmu.emfta.impl.EventImpl#getGate <em>Gate</em>}</li>
 *   <li>{@link edu.cmu.emfta.impl.EventImpl#getRelatedObject <em>Related Object</em>}</li>
 *   <li>{@link edu.cmu.emfta.impl.EventImpl#getReferenceCount <em>Reference Count</em>}</li>
 * </ul>
 *
 * @generated
 */
public class EventImpl extends MinimalEObjectImpl.Container implements Event {
	/**
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final EventType TYPE_EDEFAULT = EventType.BASIC;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected EventType type = TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = "";

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getProbability() <em>Probability</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProbability()
	 * @generated
	 * @ordered
	 */
	protected static final double PROBABILITY_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getProbability() <em>Probability</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProbability()
	 * @generated
	 * @ordered
	 */
	protected double probability = PROBABILITY_EDEFAULT;

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
	 * The cached value of the '{@link #getGate() <em>Gate</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGate()
	 * @generated
	 * @ordered
	 */
	protected Gate gate;

	/**
	 * The cached value of the '{@link #getRelatedObject() <em>Related Object</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRelatedObject()
	 * @generated
	 * @ordered
	 */
	protected EList<Object> relatedObject;

	/**
	 * The default value of the '{@link #getReferenceCount() <em>Reference Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReferenceCount()
	 * @generated
	 * @ordered
	 */
	protected static final int REFERENCE_COUNT_EDEFAULT = 1;

	/**
	 * The cached value of the '{@link #getReferenceCount() <em>Reference Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReferenceCount()
	 * @generated
	 * @ordered
	 */
	protected int referenceCount = REFERENCE_COUNT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EventImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return EmftaPackage.Literals.EVENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EventType getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(EventType newType) {
		EventType oldType = type;
		type = newType == null ? TYPE_EDEFAULT : newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EmftaPackage.EVENT__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EmftaPackage.EVENT__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getProbability() {
		return probability;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setProbability(double newProbability) {
		double oldProbability = probability;
		probability = newProbability;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EmftaPackage.EVENT__PROBABILITY, oldProbability, probability));
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
			eNotify(new ENotificationImpl(this, Notification.SET, EmftaPackage.EVENT__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Gate getGate() {
		return gate;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetGate(Gate newGate, NotificationChain msgs) {
		Gate oldGate = gate;
		gate = newGate;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, EmftaPackage.EVENT__GATE, oldGate, newGate);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setGate(Gate newGate) {
		if (newGate != gate) {
			NotificationChain msgs = null;
			if (gate != null)
				msgs = ((InternalEObject)gate).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - EmftaPackage.EVENT__GATE, null, msgs);
			if (newGate != null)
				msgs = ((InternalEObject)newGate).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - EmftaPackage.EVENT__GATE, null, msgs);
			msgs = basicSetGate(newGate, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EmftaPackage.EVENT__GATE, newGate, newGate));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Object> getRelatedObject() {
		if (relatedObject == null) {
			relatedObject = new EDataTypeUniqueEList<Object>(Object.class, this, EmftaPackage.EVENT__RELATED_OBJECT);
		}
		return relatedObject;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getReferenceCount() {
		return referenceCount;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setReferenceCount(int newReferenceCount) {
		int oldReferenceCount = referenceCount;
		referenceCount = newReferenceCount;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EmftaPackage.EVENT__REFERENCE_COUNT, oldReferenceCount, referenceCount));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case EmftaPackage.EVENT__GATE:
				return basicSetGate(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case EmftaPackage.EVENT__TYPE:
				return getType();
			case EmftaPackage.EVENT__NAME:
				return getName();
			case EmftaPackage.EVENT__PROBABILITY:
				return getProbability();
			case EmftaPackage.EVENT__DESCRIPTION:
				return getDescription();
			case EmftaPackage.EVENT__GATE:
				return getGate();
			case EmftaPackage.EVENT__RELATED_OBJECT:
				return getRelatedObject();
			case EmftaPackage.EVENT__REFERENCE_COUNT:
				return getReferenceCount();
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
			case EmftaPackage.EVENT__TYPE:
				setType((EventType)newValue);
				return;
			case EmftaPackage.EVENT__NAME:
				setName((String)newValue);
				return;
			case EmftaPackage.EVENT__PROBABILITY:
				setProbability((Double)newValue);
				return;
			case EmftaPackage.EVENT__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case EmftaPackage.EVENT__GATE:
				setGate((Gate)newValue);
				return;
			case EmftaPackage.EVENT__RELATED_OBJECT:
				getRelatedObject().clear();
				getRelatedObject().addAll((Collection<? extends Object>)newValue);
				return;
			case EmftaPackage.EVENT__REFERENCE_COUNT:
				setReferenceCount((Integer)newValue);
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
			case EmftaPackage.EVENT__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case EmftaPackage.EVENT__NAME:
				setName(NAME_EDEFAULT);
				return;
			case EmftaPackage.EVENT__PROBABILITY:
				setProbability(PROBABILITY_EDEFAULT);
				return;
			case EmftaPackage.EVENT__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case EmftaPackage.EVENT__GATE:
				setGate((Gate)null);
				return;
			case EmftaPackage.EVENT__RELATED_OBJECT:
				getRelatedObject().clear();
				return;
			case EmftaPackage.EVENT__REFERENCE_COUNT:
				setReferenceCount(REFERENCE_COUNT_EDEFAULT);
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
			case EmftaPackage.EVENT__TYPE:
				return type != TYPE_EDEFAULT;
			case EmftaPackage.EVENT__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case EmftaPackage.EVENT__PROBABILITY:
				return probability != PROBABILITY_EDEFAULT;
			case EmftaPackage.EVENT__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case EmftaPackage.EVENT__GATE:
				return gate != null;
			case EmftaPackage.EVENT__RELATED_OBJECT:
				return relatedObject != null && !relatedObject.isEmpty();
			case EmftaPackage.EVENT__REFERENCE_COUNT:
				return referenceCount != REFERENCE_COUNT_EDEFAULT;
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
		result.append(", name: ");
		result.append(name);
		result.append(", probability: ");
		result.append(probability);
		result.append(", description: ");
		result.append(description);
		result.append(", relatedObject: ");
		result.append(relatedObject);
		result.append(", referenceCount: ");
		result.append(referenceCount);
		result.append(')');
		return result.toString();
	}

} //EventImpl
