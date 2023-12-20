package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.color;

import java.io.IOException;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.perspectiveImages.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class ColorImageGeneratorOperator extends BasePipelineOperator<IPerspectiveImage, IRenderableImage>
{
	private boolean forceUpdate = false;
	
	public ColorImageGeneratorOperator(boolean forceUpdate)
	{
		this.forceUpdate = forceUpdate;
	}
	
	@Override
	public void processData() throws IOException, Exception
	{
		PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(inputs, forceUpdate);
		outputs.addAll(pipeline.getRenderableImages());
	}
}
