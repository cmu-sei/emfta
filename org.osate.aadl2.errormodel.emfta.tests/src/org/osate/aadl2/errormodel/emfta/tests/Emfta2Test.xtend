package org.osate.aadl2.errormodel.emfta.tests

import com.itemis.xtext.testing.XtextRunner2
import org.eclipse.core.runtime.Path
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.util.Files
import org.junit.Test
import org.junit.runner.RunWith
import org.osate.aadl2.AadlPackage
import org.osate.aadl2.SystemImplementation
import org.osate.aadl2.errormodel.emfta.fta.EMFTACreateModel
import org.osate.aadl2.errormodel.tests.ErrorModelUiInjectorProvider
import org.osate.aadl2.instantiation.InstantiateModel
import org.osate.core.test.OsateTest

import static org.junit.Assert.*

@RunWith(typeof(XtextRunner2))
@InjectWith(typeof(ErrorModelUiInjectorProvider))
class Emfta2Test extends OsateTest {
	override getProjectName() {
		"test2"
	}
	

/**
 * This test uses a composite error state declaration with an AND that references
 * the last two subcomponents in a flow. For each we trace backward along propagations
 * to include the input to the subcomponent.
 */
	@Test
	def void basicfta() {
		val aadlFile = "changeme.aadl"
		val errorlibFile = "errorlib.aadl"
		val state = "state Failed"
		createFiles(aadlFile -> aadlText, errorlibFile -> errorlibText) 
		suppressSerialization
		val result = testFile(aadlFile , errorlibFile/*, referencedFile1, referencedFile2, etc. */)

		// get the correct package
		val pkg = result.resource.contents.head as AadlPackage
		val cls = pkg.ownedPublicSection.ownedClassifiers
		assertTrue('', cls.exists[name == 'main.i'])

		// instantiate
		val sysImpl = cls.findFirst[name == 'main.i'] as SystemImplementation
		val instance = InstantiateModel::buildInstanceModelFile(sysImpl)
//		assertEquals("fta_main_i_Instance", instance.name)

		
		val checker = new EMFTACreateModel(instance)
		val uri =checker.createTransformedFTA(instance,state)
		val file = workspaceRoot.getFile(new Path(uri.toPlatformString(true)))
		val actual = Files.readStreamIntoString(file.contents)
		assertEquals('error', expected.trim, actual.trim)
	}

	val aadlText = '''
package Emfta2Test


public

data mydata
end mydata;

system s
features
	datain : in data port mydata; 
annex EMV2 {**
	use types ErrorModelLibrary;
	use behavior ErrorModelLibrary::Simple;
	
	error propagations
		datain : in propagation {BadValue};
	flows
		f0 : error sink datain {BadValue};
	end propagations;
	component error behavior
	transitions
		t0 : Operational -[datain{BadValue}]-> Failed;
	end component;
**};
end s;

device sensor
features
	dataout : out data port mydata;
annex EMV2 {**
	use types ErrorModelLibrary;
	use behavior ErrorModelLibrary::Simple;
	error propagations
		dataout : out propagation {BadValue};
	flows
		f0 : error source dataout {BadValue};
	end propagations;
**};	
end sensor;

system main
end main;

system implementation main.i
subcomponents
	s1 : system s;
	s2 : system s;
	sens1 : device sensor;
	sens2 : device sensor;
connections
	c0 : port sens1.dataout -> s1.datain;
	c1 : port sens2.dataout -> s2.datain;
annex EMV2 {**
	use types ErrorModelLibrary;
	use behavior ErrorModelLibrary::Simple;
	
	composite error behavior
		states
			[s1.Failed and s2.Failed]-> Failed;
		end composite;  
	
**};
end main.i;

end Emfta2Test;
	'''

	val errorlibText = '''
package ErrorModelLibrary
public
annex EMV2 {**
	error types
		NoValue : type;
		BadValue : type;
		LateValue : type;
		NoService : type;
	end types;

	
	error behavior Simple
	events
		Failure : error event ;
	states
		Operational : initial state ;
		Failed : state ;
	transitions
		BadValueTransition : Operational -[ Failure ]-> Failed ;
	end behavior ;
		-- simple error model
	error behavior Basic
	events
	    Failure : error event;
	states
	    Operational: initial state;
	    Failed: state;
	transitions
	     Operational -[Failure]-> Failed;
	end behavior;
	
**};

end ErrorModelLibrary;
	'''

	val expected = '''
<?xml version="1.0" encoding="ASCII"?>
<emfta:FTAModel xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:emfta="http://cmu.edu/emfta" root="//@events.6" name="emfta2test_main_i-failed" description="Top Level Failure">
  <events name="s1-failure" description="Error event Failure on component s1"/>
  <events name="sens1-f0-badvalue" description="Error source f0 on component sens1 from dataout with types {BadValue}"/>
  <events type="Intermediate" name="s1-failed">
    <gate events="//@events.0 //@events.1"/>
  </events>
  <events name="s2-failure" description="Error event Failure on component s2"/>
  <events name="sens2-f0-badvalue" description="Error source f0 on component sens2 from dataout with types {BadValue}"/>
  <events type="Intermediate" name="s2-failed">
    <gate events="//@events.3 //@events.4"/>
  </events>
  <events type="Intermediate" name="emfta2test_main_i-failed">
    <gate type="AND" events="//@events.2 //@events.5"/>
  </events>
</emfta:FTAModel>
	'''
}
