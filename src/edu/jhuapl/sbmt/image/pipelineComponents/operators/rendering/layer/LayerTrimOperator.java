package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer;

import java.io.IOException;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class LayerTrimOperator extends BasePipelineOperator<Layer, Layer>
{
    protected static final LayerTransformFactory TransformFactory = new LayerTransformFactory();
    private int iLowerOffset, iUpperOffset, jLowerOffset, jUpperOffset;

	public LayerTrimOperator(int iLowerOffset, int iUpperOffset, int jLowerOffset, int jUpperOffset)
	{
		this.iLowerOffset = iLowerOffset;
		this.iUpperOffset = iUpperOffset;
		this.jLowerOffset = jLowerOffset;
		this.jUpperOffset = jUpperOffset;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		for (Layer layer : inputs)
		{
			Layer outputLayer = (TransformFactory.trim(iLowerOffset, iUpperOffset, jLowerOffset, jUpperOffset).apply(layer));
			outputs.add(outputLayer);
		}
	}

}
