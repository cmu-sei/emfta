/**
 */
package edu.cmu.emfta;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see edu.cmu.emfta.EmftaFactory
 * @model kind="package"
 * @generated
 */
public interface EmftaPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "emfta";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://cmu.edu/emfta";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "emfta";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	EmftaPackage eINSTANCE = edu.cmu.emfta.impl.EmftaPackageImpl.init();

	/**
	 * The meta object id for the '{@link edu.cmu.emfta.impl.EventImpl <em>Event</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see edu.cmu.emfta.impl.EventImpl
	 * @see edu.cmu.emfta.impl.EmftaPackageImpl#getEvent()
	 * @generated
	 */
	int EVENT = 0;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT__NAME = 1;

	/**
	 * The feature id for the '<em><b>Probability</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT__PROBABILITY = 2;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT__DESCRIPTION = 3;

	/**
	 * The feature id for the '<em><b>Gate</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT__GATE = 4;

	/**
	 * The feature id for the '<em><b>Related Object</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT__RELATED_OBJECT = 5;

	/**
	 * The number of structural features of the '<em>Event</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Event</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link edu.cmu.emfta.impl.GateImpl <em>Gate</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see edu.cmu.emfta.impl.GateImpl
	 * @see edu.cmu.emfta.impl.EmftaPackageImpl#getGate()
	 * @generated
	 */
	int GATE = 1;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GATE__TYPE = 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GATE__DESCRIPTION = 1;

	/**
	 * The feature id for the '<em><b>Events</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GATE__EVENTS = 2;

	/**
	 * The number of structural features of the '<em>Gate</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GATE_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Gate</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GATE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link edu.cmu.emfta.impl.FTAModelImpl <em>FTA Model</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see edu.cmu.emfta.impl.FTAModelImpl
	 * @see edu.cmu.emfta.impl.EmftaPackageImpl#getFTAModel()
	 * @generated
	 */
	int FTA_MODEL = 2;

	/**
	 * The feature id for the '<em><b>Root</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FTA_MODEL__ROOT = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FTA_MODEL__NAME = 1;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FTA_MODEL__DESCRIPTION = 2;

	/**
	 * The feature id for the '<em><b>Comments</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FTA_MODEL__COMMENTS = 3;

	/**
	 * The feature id for the '<em><b>Events</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FTA_MODEL__EVENTS = 4;

	/**
	 * The number of structural features of the '<em>FTA Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FTA_MODEL_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>FTA Model</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FTA_MODEL_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link edu.cmu.emfta.EventType <em>Event Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see edu.cmu.emfta.EventType
	 * @see edu.cmu.emfta.impl.EmftaPackageImpl#getEventType()
	 * @generated
	 */
	int EVENT_TYPE = 3;

	/**
	 * The meta object id for the '{@link edu.cmu.emfta.GateType <em>Gate Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see edu.cmu.emfta.GateType
	 * @see edu.cmu.emfta.impl.EmftaPackageImpl#getGateType()
	 * @generated
	 */
	int GATE_TYPE = 4;


	/**
	 * Returns the meta object for class '{@link edu.cmu.emfta.Event <em>Event</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Event</em>'.
	 * @see edu.cmu.emfta.Event
	 * @generated
	 */
	EClass getEvent();

	/**
	 * Returns the meta object for the attribute '{@link edu.cmu.emfta.Event#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see edu.cmu.emfta.Event#getType()
	 * @see #getEvent()
	 * @generated
	 */
	EAttribute getEvent_Type();

	/**
	 * Returns the meta object for the attribute '{@link edu.cmu.emfta.Event#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see edu.cmu.emfta.Event#getName()
	 * @see #getEvent()
	 * @generated
	 */
	EAttribute getEvent_Name();

	/**
	 * Returns the meta object for the attribute '{@link edu.cmu.emfta.Event#getProbability <em>Probability</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Probability</em>'.
	 * @see edu.cmu.emfta.Event#getProbability()
	 * @see #getEvent()
	 * @generated
	 */
	EAttribute getEvent_Probability();

	/**
	 * Returns the meta object for the attribute '{@link edu.cmu.emfta.Event#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see edu.cmu.emfta.Event#getDescription()
	 * @see #getEvent()
	 * @generated
	 */
	EAttribute getEvent_Description();

	/**
	 * Returns the meta object for the containment reference '{@link edu.cmu.emfta.Event#getGate <em>Gate</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Gate</em>'.
	 * @see edu.cmu.emfta.Event#getGate()
	 * @see #getEvent()
	 * @generated
	 */
	EReference getEvent_Gate();

	/**
	 * Returns the meta object for the attribute list '{@link edu.cmu.emfta.Event#getRelatedObject <em>Related Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Related Object</em>'.
	 * @see edu.cmu.emfta.Event#getRelatedObject()
	 * @see #getEvent()
	 * @generated
	 */
	EAttribute getEvent_RelatedObject();

	/**
	 * Returns the meta object for class '{@link edu.cmu.emfta.Gate <em>Gate</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Gate</em>'.
	 * @see edu.cmu.emfta.Gate
	 * @generated
	 */
	EClass getGate();

	/**
	 * Returns the meta object for the attribute '{@link edu.cmu.emfta.Gate#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see edu.cmu.emfta.Gate#getType()
	 * @see #getGate()
	 * @generated
	 */
	EAttribute getGate_Type();

	/**
	 * Returns the meta object for the attribute '{@link edu.cmu.emfta.Gate#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see edu.cmu.emfta.Gate#getDescription()
	 * @see #getGate()
	 * @generated
	 */
	EAttribute getGate_Description();

	/**
	 * Returns the meta object for the reference list '{@link edu.cmu.emfta.Gate#getEvents <em>Events</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Events</em>'.
	 * @see edu.cmu.emfta.Gate#getEvents()
	 * @see #getGate()
	 * @generated
	 */
	EReference getGate_Events();

	/**
	 * Returns the meta object for class '{@link edu.cmu.emfta.FTAModel <em>FTA Model</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>FTA Model</em>'.
	 * @see edu.cmu.emfta.FTAModel
	 * @generated
	 */
	EClass getFTAModel();

	/**
	 * Returns the meta object for the reference '{@link edu.cmu.emfta.FTAModel#getRoot <em>Root</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Root</em>'.
	 * @see edu.cmu.emfta.FTAModel#getRoot()
	 * @see #getFTAModel()
	 * @generated
	 */
	EReference getFTAModel_Root();

	/**
	 * Returns the meta object for the attribute '{@link edu.cmu.emfta.FTAModel#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see edu.cmu.emfta.FTAModel#getName()
	 * @see #getFTAModel()
	 * @generated
	 */
	EAttribute getFTAModel_Name();

	/**
	 * Returns the meta object for the attribute '{@link edu.cmu.emfta.FTAModel#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see edu.cmu.emfta.FTAModel#getDescription()
	 * @see #getFTAModel()
	 * @generated
	 */
	EAttribute getFTAModel_Description();

	/**
	 * Returns the meta object for the attribute '{@link edu.cmu.emfta.FTAModel#getComments <em>Comments</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Comments</em>'.
	 * @see edu.cmu.emfta.FTAModel#getComments()
	 * @see #getFTAModel()
	 * @generated
	 */
	EAttribute getFTAModel_Comments();

	/**
	 * Returns the meta object for the containment reference list '{@link edu.cmu.emfta.FTAModel#getEvents <em>Events</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Events</em>'.
	 * @see edu.cmu.emfta.FTAModel#getEvents()
	 * @see #getFTAModel()
	 * @generated
	 */
	EReference getFTAModel_Events();

	/**
	 * Returns the meta object for enum '{@link edu.cmu.emfta.EventType <em>Event Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Event Type</em>'.
	 * @see edu.cmu.emfta.EventType
	 * @generated
	 */
	EEnum getEventType();

	/**
	 * Returns the meta object for enum '{@link edu.cmu.emfta.GateType <em>Gate Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Gate Type</em>'.
	 * @see edu.cmu.emfta.GateType
	 * @generated
	 */
	EEnum getGateType();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	EmftaFactory getEmftaFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link edu.cmu.emfta.impl.EventImpl <em>Event</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see edu.cmu.emfta.impl.EventImpl
		 * @see edu.cmu.emfta.impl.EmftaPackageImpl#getEvent()
		 * @generated
		 */
		EClass EVENT = eINSTANCE.getEvent();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EVENT__TYPE = eINSTANCE.getEvent_Type();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EVENT__NAME = eINSTANCE.getEvent_Name();

		/**
		 * The meta object literal for the '<em><b>Probability</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EVENT__PROBABILITY = eINSTANCE.getEvent_Probability();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EVENT__DESCRIPTION = eINSTANCE.getEvent_Description();

		/**
		 * The meta object literal for the '<em><b>Gate</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EVENT__GATE = eINSTANCE.getEvent_Gate();

		/**
		 * The meta object literal for the '<em><b>Related Object</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EVENT__RELATED_OBJECT = eINSTANCE.getEvent_RelatedObject();

		/**
		 * The meta object literal for the '{@link edu.cmu.emfta.impl.GateImpl <em>Gate</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see edu.cmu.emfta.impl.GateImpl
		 * @see edu.cmu.emfta.impl.EmftaPackageImpl#getGate()
		 * @generated
		 */
		EClass GATE = eINSTANCE.getGate();

		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GATE__TYPE = eINSTANCE.getGate_Type();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute GATE__DESCRIPTION = eINSTANCE.getGate_Description();

		/**
		 * The meta object literal for the '<em><b>Events</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GATE__EVENTS = eINSTANCE.getGate_Events();

		/**
		 * The meta object literal for the '{@link edu.cmu.emfta.impl.FTAModelImpl <em>FTA Model</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see edu.cmu.emfta.impl.FTAModelImpl
		 * @see edu.cmu.emfta.impl.EmftaPackageImpl#getFTAModel()
		 * @generated
		 */
		EClass FTA_MODEL = eINSTANCE.getFTAModel();

		/**
		 * The meta object literal for the '<em><b>Root</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FTA_MODEL__ROOT = eINSTANCE.getFTAModel_Root();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FTA_MODEL__NAME = eINSTANCE.getFTAModel_Name();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FTA_MODEL__DESCRIPTION = eINSTANCE.getFTAModel_Description();

		/**
		 * The meta object literal for the '<em><b>Comments</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FTA_MODEL__COMMENTS = eINSTANCE.getFTAModel_Comments();

		/**
		 * The meta object literal for the '<em><b>Events</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FTA_MODEL__EVENTS = eINSTANCE.getFTAModel_Events();

		/**
		 * The meta object literal for the '{@link edu.cmu.emfta.EventType <em>Event Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see edu.cmu.emfta.EventType
		 * @see edu.cmu.emfta.impl.EmftaPackageImpl#getEventType()
		 * @generated
		 */
		EEnum EVENT_TYPE = eINSTANCE.getEventType();

		/**
		 * The meta object literal for the '{@link edu.cmu.emfta.GateType <em>Gate Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see edu.cmu.emfta.GateType
		 * @see edu.cmu.emfta.impl.EmftaPackageImpl#getGateType()
		 * @generated
		 */
		EEnum GATE_TYPE = eINSTANCE.getGateType();

	}

} //EmftaPackage
