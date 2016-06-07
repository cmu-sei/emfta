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
import org.osate.aadl2.modelsupport.errorreporting.AnalysisErrorReporterManager
import org.osate.analysis.flows.actions.CheckFlowLatency
import org.osate.core.test.Aadl2UiInjectorProvider
import org.osate.core.test.OsateTest

import static org.junit.Assert.*

@RunWith(typeof(XtextRunner2))
@InjectWith(typeof(Aadl2UiInjectorProvider))
class Emfta1Test extends OsateTest {
	override getProjectName() {
		"test"
	}

	@Test
	def void flows_pullprotocols() {
		val aadlFile = "changeme.aadl"
		createFiles(aadlFile -> aadlText) // TODO add all files to workspace
		suppressSerialization
		val result = testFile(aadlFile /*, referencedFile1, referencedFile2, etc. */)

		// get the correct package
		val pkg = result.resource.contents.head as AadlPackage
		val cls = pkg.ownedPublicSection.ownedClassifiers
		assertTrue('', cls.exists[name == 'stub.i'])

		// instantiate
		val sysImpl = cls.findFirst[name == 'stub.i'] as SystemImplementation
		val instance = InstantiateModel::buildInstanceModelFile(sysImpl)
		assertEquals("stub_i_Instance", instance.name)

		// check flow latency --> TODO: replace with call to fta export
		val errorManager = AnalysisErrorReporterManager.NULL_ERROR_MANANGER
		val checker = new CheckFlowLatency()
		val som = instance.systemOperationModes.head
		checker.invoke(new NullProgressMonitor, errorManager, instance, som)

		// read csv --> TODO: replace with result checking
		val uri = URI.createURI(
			resourceRoot + "/instances/reports/latency/pullprotocols_stub_i_Instance__latency_AS-MF-ET-EQ.csv")
		val file = workspaceRoot.getFile(new Path(uri.toPlatformString(true)))
		val actual = Files.readStreamIntoString(file.contents)
		assertEquals('error', expected.trim, actual.trim)
	}

	val aadlText = '''
		package PullProtocols
		public
		
		virtual bus DCFMInputPullProtocol
		properties
		Transmission_Type => pull;
		Latency => 300 ms .. 300 ms ; -- should be allowed on virtual bus
		-- Implemented_As => classifier (PullDCFMInputDataset.CrossPartition);
		-- implemented_as wants a system implementation or an abstract implementation
		-- On the other hand latency analysis currently assumes that threads sit inside processes for one way of recognizing partition boundaries.
		end DCFMInputPullProtocol;
		
		process PullDCFMInputDataset
		features
		SenderData: in data port ;
		ReceiverData: out data port ;
		flows
		Xfer: flow path SenderData -> ReceiverData;
		end PullDCFMInputDataset; 
		
		process implementation PullDCFMInputDataset.CrossPartition
		subcomponents
		 sender: thread PullDCFMDataSetSender;
		 requestor: thread PullDCFMDataSetRequestor;
		 connections
		 	incoming: port SenderData -> sender.SenderData;
		 	outgoing: port requestor.ReceiverData -> ReceiverData;
		 	STRequest: port requestor.SourceTracksRequest -> sender.SourceTracksRequest ;--{Timing => Immediate;};
		 	STReply: port sender.SendSourceTrackSet -> requestor.ReceivedSourceTrackSet
		 		;--{Timing => Delayed;};
		 	CTRequest: port requestor.CorrelatedTracksRequest -> sender.CorrelatedTracksRequest;--{Timing => Immediate;};
		 	CTReply: port sender.SendCorrelatedTrackSet -> requestor.ReceivedCorrelatedTrackSet;-- {Timing => Delayed;};
		 	APRequest: port requestor.OwnAircraftPositionRequest -> sender.OwnAircraftPositionRequest;--{Timing => Immediate;};
		 	APReply: port sender.SendOwnAircraftPosition -> requestor.ReceivedOwnAircraftPosition;-- {Timing => Delayed;};
		 flows
		 -- flow to measure latency of protocol. The result is to be reflected in the latency property of the virtual bus it implements
		 	XferOnly: end to end flow sender.SourceTrackFlow -> STReply -> requestor.SourceTrackReceivedFlow -> CTRequest ->
		 		sender.CorrelatedFlow -> CTReply -> requestor.CorrelatedTrackReceivedFlow -> 
		 		APRequest -> sender.AircraftPositionFlow -> APReply -> requestor.AircraftPositionReceivedFlow;
		-- 		{Latency => 10 ms .. 10 ms;};
		 -- flow to be used when abstract implementation is used in the transfer instead of a protocol binding 
		 	Xfer: flow path SenderData -> incoming -> sender.SenderDataSetFLow -> STReply -> requestor.SourceTrackReceivedFlow -> CTRequest ->
		 		sender.CorrelatedFlow -> CTReply -> requestor.CorrelatedTrackReceivedFlow -> APRequest -> sender.AircraftPositionFlow -> APReply -> requestor.ReceivedDataSetFlow
		 		-> outgoing -> ReceiverData;
		 	properties
		 	-- alternating immediate/delayed emulates partition slots and communication within a frame.
		 	-- making all delayed emulates frame delayed cross partition communication.
		 	-- we could also indicates partitions by tagging the process as SEI::isPartition and a SEI::Partition_Latency
		 	-- Alternatively we can use virtual processor and the ARINC653 properties or the period on the virtual processor
		 		Timing => Immediate applies to STRequest,CTRequest,APRequest;
		 		Timing => Delayed applies to STReply,CTReply,APReply;
		 		Latency => 300 ms .. 300 ms applies to XferOnly;
		 		Period => 100 ms applies to sender, requestor;
		-- 		Deadline => 1 ms applies to sender, requestor;
		 		Dispatch_Protocol => Periodic applies to sender, requestor;
		end PullDCFMInputDataset.CrossPartition;
		
		system stub
		end stub;
		
		system implementation stub.i
		subcomponents
		 prot: process PullDCFMInputDataset.CrossPartition;
		end stub.i;
		
		thread PullDCFMDataSetSender
		features
		-- data to be transferred
		SenderData: in data port  ;
		-- protocol interaction ports
		SourceTracksRequest: in data port;
		SendSourceTrackSet: out data port ;
		CorrelatedTracksRequest: in data port;
		SendCorrelatedTrackSet: out data port ;
		OwnAircraftPositionRequest: in data port;
		SendOwnAircraftPosition: out data port  ;
		flows
			SenderDataSetFLow: flow path SenderData -> SendSourceTrackSet;
			SourceTrackFlow: flow source SendSourceTrackSet;
			SourceTrackFlowpath: flow path SourceTracksRequest->SendSourceTrackSet;
			CorrelatedFlow: flow path CorrelatedTracksRequest -> SendCorrelatedTrackSet;
			AircraftPositionFlow: flow path OwnAircraftPositionRequest -> SendOwnAircraftPosition;
		properties
			Dispatch_Protocol => Periodic;
			Latency => 1ms..1ms applies to SourceTrackFlowpath, CorrelatedFlow, AircraftPositionFlow;
		end PullDCFMDataSetSender;
		
		thread PullDCFMDataSetRequestor
		features
		-- data being transferred
		ReceiverData: out data port  ;
		-- protocol interaction ports
		SourceTracksRequest: out data port;
		ReceivedSourceTrackSet: in data port ;
		CorrelatedTracksRequest: out data port;
		ReceivedCorrelatedTrackSet: in data port ;
		OwnAircraftPositionRequest: out data port;
		ReceivedOwnAircraftPosition: in data port  ;
		flows
			SourceTrackRequestFlow: flow source SourceTracksRequest;
			SourceTrackReceivedFlow: flow path ReceivedSourceTrackSet -> CorrelatedTracksRequest;
			CorrelatedTrackReceivedFlow: flow path ReceivedCorrelatedTrackSet -> OwnAircraftPositionRequest;
			AircraftPositionReceivedFlow: flow sink ReceivedOwnAircraftPosition ;
			ReceivedDataSetFlow: flow path ReceivedOwnAircraftPosition -> ReceiverData;
		properties
			Dispatch_Protocol => Periodic;
			Latency => 1ms..1ms applies to SourceTrackReceivedFlow, CorrelatedTrackReceivedFlow;
			Latency => 0ms..0ms applies to SourceTracksRequest, AircraftPositionReceivedFlow;
			end PullDCFMDataSetRequestor;
		
		end PullProtocols;
	'''

	val expected = '''
Latency analysis for end-to-end flow 'prot.XferOnly' of system 'stub.i' with latency preference stettings AS-MF-ET-EQ,

Contributor,Min Specified,Min Value,Min Method,Max Specified,Max Value,Max Method,Comments,
thread prot.sender,,0.0ms,first sampling,,0.0ms,first sampling,Initial 100.0ms sampling latency not added,
thread prot.sender,,0.0ms,no latency,,0.0ms,no latency,
Delayed Connection ,,0.0ms,no latency,,0.0ms,no latency,
thread prot.requestor,,100.0ms,delayed sampling,,100.0ms,delayed sampling,Min: Sampling period 100.0ms,Max: Sampling period 100.0ms,
thread prot.requestor,1.0ms,1.0ms,specified,1.0ms,1.0ms,specified,
Immediate Connection ,,0.0ms,no latency,,0.0ms,no latency,
thread prot.sender,,0.0ms,no latency,,0.0ms,no latency,
thread prot.sender,1.0ms,1.0ms,specified,1.0ms,1.0ms,specified,
Delayed Connection ,,0.0ms,no latency,,0.0ms,no latency,
thread prot.requestor,,100.0ms,delayed sampling,,100.0ms,delayed sampling,Min: Sampling period 100.0ms,Max: Sampling period 100.0ms,
thread prot.requestor,1.0ms,1.0ms,specified,1.0ms,1.0ms,specified,
Immediate Connection ,,0.0ms,no latency,,0.0ms,no latency,
thread prot.sender,,0.0ms,no latency,,0.0ms,no latency,
thread prot.sender,1.0ms,1.0ms,specified,1.0ms,1.0ms,specified,
Delayed Connection ,,0.0ms,no latency,,0.0ms,no latency,
thread prot.requestor,,100.0ms,delayed sampling,,100.0ms,delayed sampling,Min: Sampling period 100.0ms,Max: Sampling period 100.0ms,
thread prot.requestor,,0.0ms,no latency,,0.0ms,no latency,
Latency Total,4.0ms,304.0ms,,4.0ms,304.0ms,,
End to End Latency,,300.0ms,,,300.0ms,,
End to end Latency Summary,
WARNING,Minimum specified flow latency total 4.00ms less then expected minimum end to end latency 300.0ms (better response time),
ERROR,Minimum actual latency total 304.0ms exceeds expected maximum end to end latency 300.0ms,
ERROR,Maximum actual latency total 304.0ms exceeds expected maximum end to end latency 300.0ms,



	'''
}
