package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer;

import java.io.IOException;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.impl.LayerDoubleTransformFactory;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class LayerLinearInterpolaterOperator extends BasePipelineOperator<Layer, Layer>
{
    protected static final LayerDoubleTransformFactory DoubleTransformFactory = new LayerDoubleTransformFactory();
    private int width, height;

	public LayerLinearInterpolaterOperator(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		for (Layer layer : inputs)
			outputs.add((DoubleTransformFactory.linearInterpolate(width, height).apply(layer)));
	}

}
