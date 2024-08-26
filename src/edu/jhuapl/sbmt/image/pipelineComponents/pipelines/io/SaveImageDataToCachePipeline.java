package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.image.pipelineComponents.operators.io.SaveImageDataToFileOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import vtk.vtkImageData;

public class SaveImageDataToCachePipeline
{

	private SaveImageDataToCachePipeline(vtkImageData imageData, String filename) throws IOException, Exception
	{
		Just.of(Pair.of(filename, imageData))
			.operate(new SaveImageDataToFileOperator())
			.run();
	}

	public static SaveImageDataToCachePipeline of(vtkImageData imageData, String filename) throws IOException, Exception
	{
		return new SaveImageDataToCachePipeline(imageData, filename);
	}
}
