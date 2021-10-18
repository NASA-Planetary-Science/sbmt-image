package edu.jhuapl.sbmt.image.modules.rendering;

import java.io.IOException;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.impl.LayerDoubleTransformFactory;
import edu.jhuapl.sbmt.image.pipeline.operator.BasePipelineOperator;

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
		outputs.add((DoubleTransformFactory.linearInterpolate(width, height).apply(inputs.get(0))));
	}

}
