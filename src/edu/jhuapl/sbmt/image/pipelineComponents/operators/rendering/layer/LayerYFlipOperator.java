package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer;

import java.io.IOException;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class LayerYFlipOperator extends BasePipelineOperator<Layer, Layer>
{
    protected final LayerTransformFactory TransformFactory = new LayerTransformFactory();


	public LayerYFlipOperator()
	{

	}

	@Override
	public void processData() throws IOException, Exception
	{
		for (Layer layer : inputs)
		{
			Layer rotatedLayer = TransformFactory.flipAboutY().apply(layer);
			outputs.add(rotatedLayer);
		}
	}

}
