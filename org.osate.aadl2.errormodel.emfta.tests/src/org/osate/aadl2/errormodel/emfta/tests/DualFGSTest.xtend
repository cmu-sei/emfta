package org.osate.aadl2.errormodel.emfta.tests

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.util.Files
import org.eclipselabs.xtext.utils.unittesting.XtextRunner2
import org.junit.Test
import org.junit.runner.RunWith
import org.osate.aadl2.AadlPackage
import org.osate.aadl2.SystemImplementation
import org.osate.aadl2.instantiation.InstantiateModel

import org.osate.core.test.Aadl2UiInjectorProvider
import org.osate.core.test.OsateTest

import static org.junit.Assert.*
import org.osate.aadl2.errormodel.emfta.actions.EMFTAAction

@RunWith(typeof(XtextRunner2))
@InjectWith(typeof(Aadl2UiInjectorProvider))
class DualFGSTest extends OsateTest {
	override getProjectName() {
		"DualFGSTest"
	}
	
	override void deleteProject(String projectName) {
	}
	

/**
-- composite error behavior specification of a dual channel FGS
-- It allows optimizing transformations to be applied.
* Under one gate are three sub events, two with gates for optimization, one without
* The two gates have an item in common (AP1) - normally this would be AP2 but now AP1 
* can be transformed to the outer gate. 
 * We are generating for CriticalModeFailure.
 */
	@Test
	def void dualfgsfta() {
		val aadlFile = "changeme.aadl"
		val fgserrorlibFile = "fgserrorlib.aadl"
		val state = "state CriticalModeFailure"
		createFiles(aadlFile -> aadlText,  fgserrorlibFile -> errorlib) // TODO add all files to workspace
		suppressSerialization
		val result = testFile(aadlFile, fgserrorlibFile /*, referencedFile1, referencedFile2, etc. */)

		// get the correct package
		val pkg = result.resource.contents.head as AadlPackage
		val cls = pkg.ownedPublicSection.ownedClassifiers
		assertTrue('', cls.exists[name == 'FGS.composite'])

		// instantiate
		val sysImpl = cls.findFirst[name == 'FGS.composite'] as SystemImplementation
		val instance = InstantiateModel::buildInstanceModelFile(sysImpl)
//		assertEquals("fta_main_i_Instance", instance.name)

		
		val checker = new EMFTAAction()
		checker.systemInstance = instance
		checker.createModel(state,false, false)
		
		val uri = URI.createURI(
			resourceRoot + "/fta/dualfgs_fgs_composite-criticalmodefailure.emfta")
		val file = workspaceRoot.getFile(new Path(uri.toPlatformString(true)))
		val actual = Files.readStreamIntoString(file.contents)
		assertEquals('error', expected.trim, actual.trim)
		
	}

	val aadlText = '''
package DualFGS
public

system FG
features
InPort: in data port;
OutPort: out data port;
annex emv2 {**
	use types FGSErrorModelLibrary;
  use behavior FGSErrorModelLibrary::Simple;        
	error propagations
	OutPort: out propagation {NoValue};
	end propagations;
	
component error behavior
    propagations  
       Failed-[]->Outport{NoValue};
   end component; 
	**};
end FG;
system AP
features
InPort: in data port;
OutPort: out data port;
annex emv2 {**
	use types FGSErrorModelLibrary;
  use behavior FGSErrorModelLibrary::Simple;        
	error propagations
	InPort: in propagation {NoValue};
	OutPort: out propagation {NoValue};
	end propagations;
	
component error behavior
    propagations  
      Failed-[]->Outport{NoValue};
      Operational -[Inport{NoValue}]-> OutPort{NoValue};
   end component; 
	**};
end AP;

system AC
features
FromAP1Port: in data port;
FromAP2Port: in data port;
OutPort: out data port;
annex emv2 {**
	use types FGSErrorModelLibrary;
  use behavior FGSErrorModelLibrary::Simple; 
	error propagations
	FromAP1Port: in propagation {NoValue};
	FromAP2Port: in propagation {NoValue};
	OutPort: out propagation {NoValue};
	end propagations;
	
component error behavior
    propagations  
      Failed-[]->Outport{NoValue};
      Operational -[FromAP1Port{NoValue} and FromAP2Port{NoValue}]-> OutPort{NoValue};
  end component;
	**};
end AC;


system FGS
features
inport : in data port;
outport: out data port;
powersupply : requires bus access PowerSupply;
 modes
Critical: mode;
NonCritical: initial mode;
annex emv2 {**
	use types FGSErrorModelLibrary;
  use behavior FGSErrorModelLibrary::ThreeState;
	error propagations
	inport: in propagation {NoValue};
	outport : out propagation {NoValue};
	powersupply: in propagation {NoService};
	flows
	esource: error source outport{NoValue} when CriticalModefailure;
	-- fault tree to reflect FGS as error source.
	ePath: error path inport{NoValue} -> outport{NoValue};
	epPower: error path powersupply{NoService} -> outport{NoValue};
	end propagations;
	**};
end FGS;

system implementation FGS.common
subcomponents
AP1: system AP;
AP2: system AP;
FG1: system FG;
FG2: system FG;
AC: system AC;
network: bus EtherSwitch;
connections
FGStoFG1: port inport -> FG1.inport;
FGStoFG2: port inport -> FG2.inport;
FG1toAP1: port FG1.outport -> AP1.inport;
FG2toAP2: port FG1.outport -> AP2.inport;
AP1toAC: port AP1.outport -> AC.FromAP1Port;
AP2toAC: port AP2.outport -> AC.FromAP2Port;
ACtoFGS: port AC.outport -> outport;
properties
Actual_Connection_Binding => (reference (network)) 
  applies to FGStoFG1,FGStoFG2,FG1toAP1,FG2toAP2,AP1toAC,AP2toAC,ACtoFGS;
end FGS.common;

system implementation FGS.composite extends FGS.common
annex emv2 {**
  use types FGSErrorModelLibrary;        
  use behavior FGSErrorModelLibrary::ThreeState;
composite error behavior 
 states
    [AP1.Operational and AP2.Operational
 and FG1.Operational and FG2.Operational and AC.Operational]->Operational ;
    [AC.Operational and 
  1 ormore (FG1.Failed, AP1.Failed) and 
     FG2.Operational and AP2.Operational
  or 1 ormore (FG2.Failed, AP2.Failed) and 
     FG1.Operational and AP1.Operational]-> NoncriticalModeFailure;
    [AC.Failed or 
    1 ormore (AP1.Failed, FG1.Failed) 
    and 1 ormore (AP1.Failed, FG2.Failed) and network.Failed ]->CriticalModefailure ;
end composite;
**};
end FGS.composite;

system implementation FGS.derive extends FGS.common
annex emv2 {**
  use types FGSErrorModelLibrary;        
  use behavior FGSErrorModelLibrary::ThreeState;
component error behavior 
transitions
	Operational -[powersupply{NoService}]-> CriticalModeFailure;
propagations
    Operational -[inport]-> outport{NoValue};
    CriticalModeFailure -[]-> outport{NoValue};
    NonCriticalModeFailure -[]-> outport{NoValue};
end component;
**};
end FGS.derive;

bus PowerSupply
annex emv2 {**
	use types FGSErrorModelLibrary;
  use behavior FGSErrorModelLibrary::Simple;        
	error propagations
	access: out propagation {NoService};
	flows
	esource: error source access{NoService} when Failed;
	end propagations;
	
component error behavior
    propagations  
      Failed-[]->access{NoService};
   end component; 
	**};
end PowerSupply;

bus EtherSwitch
annex emv2 {**
	use types FGSErrorModelLibrary;
  use behavior FGSErrorModelLibrary::Simple;        
	error propagations
	bindings: out propagation {NoService};
	flows
	esource: error source bindings{NoService} when Failed;
	end propagations;
	
component error behavior
    propagations  
      Failed-[]->bindings{NoService};
   end component; 
	**};
end EtherSwitch;

end DualFGS;
	'''
	
	val errorlib = '''
package FGSErrorModelLibrary
public
annex EMV2 {**
	error types
		NoValue : type;
		BadValue : type;
		LateValue : type;
		NoService : type;
	end types;

error behavior ThreeState
states
  Operational: initial state; 
  NonCriticalModeFailure: state; 
  CriticalModeFailure: state; 
end behavior;

error behavior ACThreeState
states
  Operational: initial state; 
  OneChannelFailure: state; 
  TwoChannelFailure: state; 
end behavior;

error behavior FourState
states
  Operational: initial state; 
  NonCriticalModeFailure: state; 
  CriticalModeFailure: state; 
  FailStop: state;
end behavior;

error behavior ThreeStateAC
states
  OperationalNonCritical: initial state; 
  OperationalCritical: state; 
  Failed: state; 
end behavior;
	
		
	error behavior Simple
	events
		Failure : error event ;
	states
		Operational : initial state ;
		Failed : state ;
	transitions
		BadValueTransition : Operational -[ Failure ]-> Failed ;
	end behavior ;
	
**};
end FGSErrorModelLibrary;
	'''

	val expected = '''
<?xml version="1.0" encoding="ASCII"?>
<emfta:FTAModel xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:emfta="http://cmu.edu/emfta" root="//@events.6" name="dualfgs_fgs_composite-criticalmodefailure" description="Top Level Failure">
  <events name="ac-failure" description="Error event Failure on component AC" referenceCount="1"/>
  <events name="ap1-failure" description="Error event Failure on component AP1" referenceCount="1"/>
  <events name="fg1-failure" description="Error event Failure on component FG1" referenceCount="1"/>
  <events name="fg2-failure" description="Error event Failure on component FG2" referenceCount="1"/>
  <events name="network-failure" description="Error event Failure on component network" referenceCount="1"/>
  <events type="Intermediate" name="Intermediate3" referenceCount="1">
    <gate type="AND" events="//@events.4 //@events.7"/>
  </events>
  <events type="Intermediate" name="dualfgs_fgs_composite-criticalmodefailure" referenceCount="1">
    <gate type="XOR" events="//@events.0 //@events.5"/>
  </events>
  <events type="Intermediate" name="Intermediate5" referenceCount="1">
    <gate events="//@events.1 //@events.8"/>
  </events>
  <events type="Intermediate" name="Intermediate6" referenceCount="1">
    <gate type="AND" events="//@events.2 //@events.3"/>
  </events>
</emfta:FTAModel>
	'''

}
