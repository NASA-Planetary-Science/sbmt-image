package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer;

import java.io.IOException;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class LayerXFlipOperator extends BasePipelineOperator<Layer, Layer>
{
    protected final LayerTransformFactory TransformFactory = new LayerTransformFactory();


	public LayerXFlipOperator()
	{

	}

	@Override
	public void processData() throws IOException, Exception
	{
		for (Layer layer : inputs)
		{
			Layer rotatedLayer = TransformFactory.flipAboutX().apply(layer);
			outputs.add(rotatedLayer);
		}
	}

}
