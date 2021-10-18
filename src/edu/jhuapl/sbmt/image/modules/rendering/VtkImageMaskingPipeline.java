package edu.jhuapl.sbmt.image.modules.rendering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image.pipeline.subscriber.Sink;

public class VtkImageMaskingPipeline
{
	List<Layer> updatedData;

	public VtkImageMaskingPipeline()
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
