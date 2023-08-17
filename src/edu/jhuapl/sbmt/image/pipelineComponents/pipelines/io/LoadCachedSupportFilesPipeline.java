package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.io.LoadCachedSupportFilesOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.PairSink;
import vtk.vtkImageData;
import vtk.vtkPolyData;

public class LoadCachedSupportFilesPipeline
{	
	Pair<List<vtkImageData>, List<vtkPolyData>>[] outputs = new Pair[1];
	
	public LoadCachedSupportFilesPipeline(String filename) throws IOException, Exception
	{
		Just.of(filename)
			.operate(new LoadCachedSupportFilesOperator())
			.subscribe(PairSink.of(outputs))
			.run();
	}

	public static <G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> LoadCachedSupportFilesPipeline of(String filename) throws IOException, Exception
	{
		return new LoadCachedSupportFilesPipeline(filename);
	}

	public vtkPolyData getFootprintPolyData()
	{
		if (outputs[0] == null || outputs[0].getRight().isEmpty()) return null;
		return outputs[0].getRight().get(0);
	}

	public vtkImageData getImageData()
	{
		if (outputs[0] == null || outputs[0].getLeft().isEmpty()) return null;
		return outputs[0].getLeft().get(0);
	}

}
