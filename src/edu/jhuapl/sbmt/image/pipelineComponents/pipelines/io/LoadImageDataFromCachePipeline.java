package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io;

import java.io.IOException;
import java.util.List;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Optional;

import vtk.vtkImageData;

import edu.jhuapl.sbmt.image.pipelineComponents.operators.io.LoadImageDataFromFileOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class LoadImageDataFromCachePipeline
{
	List<vtkImageData> polydata = Lists.newArrayList();

	public LoadImageDataFromCachePipeline(String filename) throws IOException, Exception
	{
		Just.of(filename)
			.operate(new LoadImageDataFromFileOperator())
			.subscribe(Sink.of(polydata))
			.run();
	}

	public static Optional<vtkImageData> of(String filename) throws IOException, Exception
	{
		List<vtkImageData> polydataList = new LoadImageDataFromCachePipeline(filename).getPolydata();
		Optional<vtkImageData> polydata = polydataList.size() == 1 ? Optional.of(polydataList.get(0)) : Optional.absent();
		return polydata;
	}

	public List<vtkImageData> getPolydata()
	{
		return polydata;
	}
}
