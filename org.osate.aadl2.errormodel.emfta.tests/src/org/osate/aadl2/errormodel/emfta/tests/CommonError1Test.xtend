package org.osate.aadl2.errormodel.emfta.tests

import org.eclipse.core.runtime.Path
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.util.Files
import org.eclipselabs.xtext.utils.unittesting.XtextRunner2
import org.junit.Test
import org.junit.runner.RunWith
import org.osate.aadl2.AadlPackage
import org.osate.aadl2.SystemImplementation
import org.osate.aadl2.errormodel.emfta.fta.EMFTACreateModel
import org.osate.aadl2.errormodel.tests.ErrorModelUiInjectorProvider
import org.osate.aadl2.instantiation.InstantiateModel
import org.osate.aadl2.util.OsateDebug
import org.osate.core.test.OsateTest
import org.osate.xtext.aadl2.errormodel.util.EMV2Util

import static org.junit.Assert.*

@RunWith(typeof(XtextRunner2))
@InjectWith(typeof(ErrorModelUiInjectorProvider))
class CommonError1Test extends OsateTest {
	override getProjectName() {
		"CommonError1Test"
	}
	
	

/**
 * example with composite error state to the last subcomponents that affect the system error state
 * other components are included based on backward flow. 
 * The sensor contribution is based on an error source declaration.
 * We are generating FailStop.
 * 
 * We also test for Operational state.
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
		assertTrue('', cls.exists[name == 'main.commonsource'])

		// instantiate
		val sysImpl = cls.findFirst[name == 'main.commonsource'] as SystemImplementation

	// XXX get the EMV2 annex subclause
	// Similar to the EMV2 tests
	// When running a release build it returns null
	// works fine when running it as JUnit plugin test
		val res = EMV2Util.getEmbeddedEMV2Subclause(sysImpl)
		OsateDebug.osateDebug("SysImpol "+res)


		val instance = InstantiateModel::buildInstanceModelFile(sysImpl)
//		assertEquals("fta_main_i_Instance", instance.name)
		
		val checker = new EMFTACreateModel()
		val uri =checker.createModel(instance,state,false)
		
		val file = workspaceRoot.getFile(new Path(uri.toPlatformString(true)))
		val actual = Files.readStreamIntoString(file.contents)
		assertEquals('error', expected.trim, actual.trim)
		
		val stateop = "state Operational"
		val uriop=checker.createModel(instance, stateop,false)
		
		val fileop = workspaceRoot.getFile(new Path(uriop.toPlatformString(true)))
		val actualop = Files.readStreamIntoString(fileop.contents)
		assertEquals('error', expectedOperational.trim, actualop.trim)
	}

	val aadlText = '''
package common_error1
public

data mydata
end mydata;

device sensor
features
	valueout : out data port mydata;
annex EMV2{**
 	use types ErrorLibrary;
 	error propagations
 		valueout : out propagation {LateDelivery};
 	flows
 		ef0 : error source valueout{LateDelivery};
 	end propagations;
 **};
end sensor;


system computing
features
	valuein : in data port mydata;
	valueout1 : out data port mydata;
	valueout2 : out data port mydata;
annex EMV2{**
 	use types ErrorLibrary;
	error propagations
 		valuein : in propagation {LateDelivery};
 		valueout1 : out propagation {ServiceError};
 		valueout2 : out propagation {ServiceError};
 	flows
 		ef0 : error path valuein{LateDelivery} -> valueout1{ServiceError};
 		ef1 : error path valuein{LateDelivery} -> valueout2{ServiceError};
 	end propagations;
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
-- The sensor contribution is based on an error source declaration.
system implementation main.commonsource
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
end main.commonsource;


end common_error1;
	'''

	val expected = '''
<?xml version="1.0" encoding="ASCII"?>
<emfta:FTAModel xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:emfta="http://cmu.edu/emfta" root="//@events.4" name="common_error1_main_commonsource-failstop" description="Top Level Failure">
  <events name="a0-failure" description="Error event Failure on component a0" referenceCount="1"/>
  <events name="s0-ef0-latedelivery" description="Error source ef0 on component s0 from valueout with types {LateDelivery}" referenceCount="1"/>
  <events name="a1-failure" description="Error event Failure on component a1" referenceCount="1"/>
  <events type="Intermediate" name="Intermediate0" referenceCount="1">
    <gate type="AND" events="//@events.0 //@events.2"/>
  </events>
  <events type="Intermediate" name="common_error1_main_commonsource-failstop" referenceCount="1">
    <gate events="//@events.3 //@events.1"/>
  </events>
</emfta:FTAModel>
	'''

	val expectedOperational = '''
<?xml version="1.0" encoding="ASCII"?>
<emfta:FTAModel xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:emfta="http://cmu.edu/emfta" root="//@events.0" name="common_error1_main_commonsource-operational" description="Top Level Failure">
  <events type="Intermediate" name="common_error1_main_commonsource-operational" referenceCount="1"/>
</emfta:FTAModel>
	'''
}
