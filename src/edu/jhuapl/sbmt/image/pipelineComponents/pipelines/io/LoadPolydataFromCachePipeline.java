package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io;

import java.io.IOException;
import java.util.List;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Optional;

import vtk.vtkPolyData;

import edu.jhuapl.sbmt.image.pipelineComponents.operators.io.LoadPolydataFromFileOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class LoadPolydataFromCachePipeline
{
	List<vtkPolyData> polydata = Lists.newArrayList();

	public LoadPolydataFromCachePipeline(String filename) throws IOException, Exception
	{
		Just.of(filename)
			.operate(new LoadPolydataFromFileOperator())
			.subscribe(Sink.of(polydata))
			.run();
	}

	public static Optional<vtkPolyData> of(String filename) throws IOException, Exception
	{
		List<vtkPolyData> polydataList = new LoadPolydataFromCachePipeline(filename).getPolydata();
		Optional<vtkPolyData> polydata = polydataList.size() == 1 ? Optional.of(polydataList.get(0)) : Optional.absent();
		return polydata;
	}

	public List<vtkPolyData> getPolydata()
	{
		return polydata;
	}
}
