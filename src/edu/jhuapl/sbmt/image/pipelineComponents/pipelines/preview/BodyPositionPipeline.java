package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.preview;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.util.TimeUtil;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.pointing.SpiceBodyOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.builtin.BuiltInOBJReader;
import edu.jhuapl.sbmt.pipeline.IPipeline;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.pointing.modules.SpiceReaderPublisher;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;

import crucible.mantle.spice.adapters.AdapterInstantiationException;
import crucible.mantle.spice.kernel.KernelInstantiationException;

public class BodyPositionPipeline implements IPipeline<SmallBodyModel>
{
	List<SmallBodyModel> updatedBodies = Lists.newArrayList();
	IPipelinePublisher<Pair<SmallBodyModel, SpicePointingProvider>> spiceBodyObjects;
	IPipelineOperator<Pair<SmallBodyModel, SpicePointingProvider>, SmallBodyModel> spiceBodyOperator;
	IPipelinePublisher<SmallBodyModel> vtkReader;
	IPipelinePublisher<SpicePointingProvider> pointingProviders;
	String[] bodyFiles;
	String[] bodyNames;
	SpiceInfo[] spiceInfos;
	String mkPath;
	String centerBodyName;
	String initialTime;
	String instName;
	SpiceInfo activeSpiceInfo;

	public BodyPositionPipeline(String[] bodyFiles, String[] bodyNames,
			SpiceInfo[] spiceInfos, String mkPath, String centerBodyName, String initialTime, String instName)
			throws Exception
	{
		this.bodyFiles = bodyFiles;
		this.bodyNames = bodyNames;
		this.spiceInfos = spiceInfos;
		this.mkPath = mkPath;
		this.centerBodyName = centerBodyName;
		this.initialTime = initialTime;
		this.instName = instName;
		//***********************
		//generate body polydata
		//***********************
		vtkReader = new BuiltInOBJReader(bodyFiles, bodyNames);
		//*********************************
		//Use SPICE to position the bodies
		//*********************************
		activeSpiceInfo = Arrays.asList(spiceInfos).stream().filter(info -> info.getBodyName().equals(centerBodyName))
				.collect(Collectors.toList()).get(0);
		pointingProviders = new SpiceReaderPublisher(mkPath, activeSpiceInfo, instName);
		spiceBodyObjects = Publishers.formPair(vtkReader, pointingProviders);
		spiceBodyOperator = new SpiceBodyOperator(centerBodyName, TimeUtil.str2et(initialTime));

	}

	private IPipelinePublisher<SmallBodyModel> of()
	{
		// ***********************
		// generate body polydata
		// ***********************
		vtkReader = new BuiltInOBJReader(bodyFiles, bodyNames);
		// *********************************
		// Use SPICE to position the bodies
		// *********************************
		try
		{
			pointingProviders = new SpiceReaderPublisher(mkPath, activeSpiceInfo, instName);
		}
		catch (KernelInstantiationException | AdapterInstantiationException | IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		spiceBodyObjects = Publishers.formPair(vtkReader, pointingProviders);

		return spiceBodyObjects.operate(spiceBodyOperator).subscribe(Sink.of(updatedBodies));
	}

	private IPipelinePublisher<SmallBodyModel> of(double time)
	{
		// ***********************
		// generate body polydata
		// ***********************
		vtkReader = new BuiltInOBJReader(bodyFiles, bodyNames);
		// *********************************
		// Use SPICE to position the bodies
		// *********************************
		try
		{
			pointingProviders = new SpiceReaderPublisher(mkPath, activeSpiceInfo, instName);
		}
		catch (KernelInstantiationException | AdapterInstantiationException | IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		spiceBodyObjects = Publishers.formPair(vtkReader, pointingProviders);
		spiceBodyOperator = new SpiceBodyOperator(centerBodyName, time);
		return spiceBodyObjects.operate(spiceBodyOperator).subscribe(Sink.of(updatedBodies));
	}

	public void run() throws Exception
	{
		updatedBodies.clear();
		of().run();
	}

	public void run(double time) throws Exception
	{
		((SpiceBodyOperator) spiceBodyOperator).setTime(time);
		updatedBodies.clear();
		of(time).run();
	}

	public List<SmallBodyModel> getOutput()
	{
		return updatedBodies;
	}
}