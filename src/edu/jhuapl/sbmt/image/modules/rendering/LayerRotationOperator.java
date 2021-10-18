package edu.jhuapl.sbmt.image.modules.rendering;

import java.io.IOException;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.image.pipeline.operator.BasePipelineOperator;

public class LayerRotationOperator extends BasePipelineOperator<Layer, Layer>
{
    protected final LayerTransformFactory TransformFactory = new LayerTransformFactory();


	public LayerRotationOperator()
	{

	}

	@Override
	public void processData() throws IOException, Exception
	{
		Layer rotatedLayer = TransformFactory.rotateCCW().apply(inputs.get(0));
		outputs.add(rotatedLayer);
	}

}
