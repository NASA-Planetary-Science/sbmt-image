package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.image.pipelineComponents.operators.io.SavePolydataToFileOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import vtk.vtkPolyData;

public class SavePolydataToCachePipeline
{

	private SavePolydataToCachePipeline(vtkPolyData polydata, String filename) throws IOException, Exception
	{
		Just.of(Pair.of(filename, polydata))
			.operate(new SavePolydataToFileOperator())
			.run();
	}

	public static SavePolydataToCachePipeline of(vtkPolyData polydata, String filename) throws IOException, Exception
	{
		return new SavePolydataToCachePipeline(polydata, filename);
	}
}
