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
class Emfta1Test extends OsateTest {
	override getProjectName() {
		"test"
	}

	@Test
	def void basicfta() {
		val aadlFile = "changeme.aadl"
		val state = "stateFailed"
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

		
		val checker = new EMFTAAction()
		checker.systemInstance = instance
		checker.createModel(state)
		
		val uri = URI.createURI(
			resourceRoot + "/fta/main_i_instance_failed.emfta")
		val file = workspaceRoot.getFile(new Path(uri.toPlatformString(true)))
		val actual = Files.readStreamIntoString(file.contents)
		assertEquals('error', expected.trim, actual.trim)
	}

	val aadlText = '''
package fta_sample


public

system s
annex EMV2 {**
	use types ErrorModelLibrary;
	use behavior ErrorModelLibrary::Simple;
**};
end s;

system main
end main;

system implementation main.i
subcomponents
	s1 : system s;
	s2 : system s;

annex EMV2 {**
	use types ErrorModelLibrary;
	use behavior ErrorModelLibrary::Simple;
	
	composite error behavior
		states
			[s1.Failed and s2.Failed]-> Failed;
		end composite;  
	
**};
end main.i;

end fta_sample;
	'''

	val expected = '''
<?xml version="1.0" encoding="ASCII"?>
<emfta:FTAModel xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:emfta="http://cmu.edu/emfta" root="//@events.2" name="main_i_Instance" description="Top Level Failure">
  <events name="0-s1-failure" description="Error event Failure on component s1" referenceCount="1"/>
  <events name="1-s2-failure" description="Error event Failure on component s2" referenceCount="1"/>
  <events type="Intermediate" name="2-main_i_instance-failed" referenceCount="1">
    <gate type="AND" events="//@events.0 //@events.1"/>
  </events>
</emfta:FTAModel>
	'''
}
