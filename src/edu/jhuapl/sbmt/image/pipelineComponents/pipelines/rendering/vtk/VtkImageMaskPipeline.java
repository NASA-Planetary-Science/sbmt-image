package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.vtk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer.LayerMaskOperator;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class VtkImageMaskPipeline
{
	List<Layer> updatedData;

	public VtkImageMaskPipeline()
	{
		updatedData = new ArrayList<Layer>();
	}

	public void run(Layer layer, int iLowerOffset, int iUpperOffset, int jLowerOffset, int jUpperOffset) throws IOException, Exception
	{
		updatedData.clear();
		IPipelinePublisher<Layer> reader = new Just<Layer>(layer);
		reader
			.operate(new LayerMaskOperator(iLowerOffset, iUpperOffset, jLowerOffset, jUpperOffset))
			.subscribe(new Sink<Layer>(updatedData))
			.run();
	}

	public List<Layer> getUpdatedData()
	{
		return updatedData;
	}

}
