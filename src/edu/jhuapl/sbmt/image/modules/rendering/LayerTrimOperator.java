package edu.jhuapl.sbmt.image.modules.rendering;

import java.io.IOException;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.image.pipeline.operator.BasePipelineOperator;

public class LayerTrimOperator extends BasePipelineOperator<Layer, Layer>
{
    protected static final LayerTransformFactory TransformFactory = new LayerTransformFactory();
    private int iLowerOffset, iUpperOffset, jLowerOffset, jUpperOffset;

	public LayerTrimOperator(int iLowerOffset, int iUpperOffset, int jLowerOffset, int jUpperOffset)
	{
		System.out.println("LayerTrimOperator: LayerTrimOperator: i upper " + iUpperOffset);
		this.iLowerOffset = iLowerOffset;
		this.iUpperOffset = iUpperOffset;
		this.jLowerOffset = jLowerOffset;
		this.jUpperOffset = jUpperOffset;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		Layer outputLayer = (TransformFactory.trim(iLowerOffset, iUpperOffset, jLowerOffset, jUpperOffset).apply(inputs.get(0)));
		outputs.add(outputLayer);
	}

}
