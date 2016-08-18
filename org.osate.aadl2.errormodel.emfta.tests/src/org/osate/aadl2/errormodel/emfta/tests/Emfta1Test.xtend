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
import org.osate.core.test.OsateTest

import static org.junit.Assert.*

@RunWith(typeof(XtextRunner2))
@InjectWith(typeof(ErrorModelUiInjectorProvider))
class Emfta1Test extends OsateTest {
	override getProjectName() {
		"test1"
	}
	
	

/**
 * example of simple composite error state with an AND operator.
 * The subcomponents have two states and a transition triggered by an error event.
 * The error event is a Basic Event.
 */
	@Test
	def void basicfta() {
		val aadlFile = "changeme.aadl"
		val state = "state Failed"
		createFiles(aadlFile -> aadlText) // TODO add all files to workspace
		suppressSerialization
		val result = testFile(aadlFile /*, referencedFile1, referencedFile2, etc. */)

		// get the correct package
		val pkg = result.resource.contents.head as AadlPackage
		val cls = pkg.ownedPublicSection.ownedClassifiers
		assertTrue('', cls.exists[name == 'main.i'])

		// instantiate
		val sysImpl = cls.findFirst[name == 'main.i'] as SystemImplementation
		val instance = InstantiateModel::buildInstanceModelFile(sysImpl)
//		assertEquals("fta_main_i_Instance", instance.name)

		
		val checker = new EMFTACreateModel(instance)
		val testuri = checker.createModel(instance,state,false)
		val file = workspaceRoot.getFile(new Path(testuri.toPlatformString(true)))
		val actual = Files.readStreamIntoString(file.contents)
		assertEquals('error', expected.trim, actual.trim)
	}

	val aadlText = '''
package fta_sample


public

system s
annex EMV2 {**
	use types fta_sample;
	use behavior fta_sample::Simple;
**};
end s;

system main
end main;

system implementation main.i
subcomponents
	s1 : system s;
	s2 : system s;

annex EMV2 {**
	use types fta_sample;
	use behavior fta_sample::Simple;
	
	composite error behavior
		states
			[s1.Failed and s2.Failed]-> Failed;
		end composite;  
	
**};
end main.i;

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

end fta_sample;
	'''

	val expected = '''
<?xml version="1.0" encoding="ASCII"?>
<emfta:FTAModel xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:emfta="http://cmu.edu/emfta" root="//@events.2" name="fta_sample_main_i-failed" description="Top Level Failure">
  <events name="s1-failure" description="Error event Failure on component s1"/>
  <events name="s2-failure" description="Error event Failure on component s2"/>
  <events type="Intermediate" name="fta_sample_main_i-failed">
    <gate type="AND" events="//@events.0 //@events.1"/>
  </events>
</emfta:FTAModel>
	'''
}
