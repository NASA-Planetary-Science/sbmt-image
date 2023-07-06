package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.vtk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageContrastOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class VtkImageContrastPipeline
{
	List<vtkImageData> updatedData;

	public VtkImageContrastPipeline(vtkImageData imageData, IntensityRange intensityRange) throws IOException, Exception
	{
		updatedData = new ArrayList<vtkImageData>();
		IPipelinePublisher<vtkImageData> reader = new Just<vtkImageData>(imageData);
		reader
			.operate(new VtkImageContrastOperator(intensityRange))
			.subscribe(new Sink<vtkImageData>(updatedData))
			.run();
	}

	public List<vtkImageData> getUpdatedData()
	{
		return updatedData;
	}

}
