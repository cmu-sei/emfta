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
import org.osate.aadl2.errormodel.emfta.fta.EMFTACreateModel

@RunWith(typeof(XtextRunner2))
@InjectWith(typeof(Aadl2UiInjectorProvider))
class CommonError2Test extends OsateTest {
	override getProjectName() {
		"CommonError2Test"
	}
	
	

/**
-- example with composite error state to the last subcomponents that affect the system error state
-- other components are included based on backward flow. 
-- Each of those components has its own error state machine with error events.
 * We are generating FailStop.
 */
	@Test
	def void commonerrorfta() {
		val aadlFile = "changeme.aadl"
		val state = "state FailStop"
		createFiles(aadlFile -> aadlText) // TODO add all files to workspace
		suppressSerialization
		val result = testFile(aadlFile /*, referencedFile1, referencedFile2, etc. */)

		// get the correct package
		val pkg = result.resource.contents.head as AadlPackage
		val cls = pkg.ownedPublicSection.ownedClassifiers
		assertTrue('', cls.exists[name == 'main.commonevents'])

		// instantiate
		val sysImpl = cls.findFirst[name == 'main.commonevents'] as SystemImplementation
		val instance = InstantiateModel::buildInstanceModelFile(sysImpl)
//		assertEquals("fta_main_i_Instance", instance.name)

		
		val checker = new EMFTACreateModel()
		val uri=checker.createModel(instance,state,false)
		val file = workspaceRoot.getFile(new Path(uri.toPlatformString(true)))
		val actual = Files.readStreamIntoString(file.contents)
		assertEquals('error', expected.trim, actual.trim)
		
	}

	val aadlText = '''
package common_error2
public

data mydata
end mydata;

device sensor
features
	valueout : out data port mydata;
annex EMV2{**
 	use types ErrorLibrary;
 	use behavior ErrorLibrary::FailStop;
 	error propagations
 		valueout : out propagation {LateDelivery,ServiceError};
 	flows
 		ef0 : error source valueout{LateDelivery};
 	end propagations;
 	component error behavior
 	propagations
 	FailStop -[]-> valueout{ServiceError};
	end component;
 **};
end sensor;


system computing
features
	valuein : in data port mydata;
	valueout1 : out data port mydata;
	valueout2 : out data port mydata;
annex EMV2{**
 	use types ErrorLibrary;
 	use behavior ErrorLibrary::FailStop;
	error propagations
 		valuein : in propagation {LateDelivery,ServiceError};
 		valueout1 : out propagation {ServiceError};
 		valueout2 : out propagation {ServiceError};
 	flows
 		ef0 : error path valuein{LateDelivery,ServiceError} -> valueout1{ServiceError};
 		ef1 : error path valuein{LateDelivery,ServiceError} -> valueout2{ServiceError};
 	end propagations;
 	component error behavior
 	propagations
 	FailStop -[]-> valueout1{ServiceError};
 	FailStop -[]-> valueout2{ServiceError};
 	end component;
 **};
end computing;


device actuator
features
	valuein : in data port mydata;
annex EMV2{**
 	use types ErrorLibrary;
 	use behavior ErrorLibrary::FailStop;
 	error propagations
 		valuein : in propagation {ServiceError};
 	flows
 		ef0 : error sink valuein{ServiceError};
 	end propagations;
 	
 	component error behavior
 	transitions
 		t0 : operational -[valuein{ServiceError}]-> failstop;
 	end component;
 	
 **};
end actuator;

system main
end main;

-- example with composite error state to the last subcomponents that affect the system error state
-- other components are included based on backward flow. 
-- Each of those components has its own error state machine with error events.
system implementation main.commonevents
subcomponents
	s0 : device sensor;
	c0 : system computing;
	a0 : device actuator;
	a1 : device actuator;
connections
	conn0 : port s0.valueout -> c0.valuein;
	conn1 : port c0.valueout1 -> a0.valuein;
	conn2 : port c0.valueout2 -> a1.valuein;
annex EMV2{**
 	use types ErrorLibrary;
 	use behavior ErrorLibrary::FailStop;
 	
 	
 	composite error behavior
 	states
 		[a0.failstop and a1.failstop ]-> failstop;
 	end composite;
 **};
end main.commonevents;


end common_error2;
	'''

	val expected = '''
<?xml version="1.0" encoding="ASCII"?>
<emfta:FTAModel xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:emfta="http://cmu.edu/emfta" root="//@events.4" name="common_error2_main_commonevents-failstop" description="Top Level Failure">
  <events name="a0-failure" description="Error event Failure on component a0" referenceCount="1"/>
  <events name="c0-failure" description="Error event Failure on component c0" referenceCount="1"/>
  <events name="a1-failure" description="Error event Failure on component a1" referenceCount="1"/>
  <events type="Intermediate" name="Intermediate0" referenceCount="1">
    <gate type="AND" events="//@events.0 //@events.2"/>
  </events>
  <events type="Intermediate" name="common_error2_main_commonevents-failstop" referenceCount="1">
    <gate events="//@events.3 //@events.1"/>
  </events>
</emfta:FTAModel>
	'''

}
