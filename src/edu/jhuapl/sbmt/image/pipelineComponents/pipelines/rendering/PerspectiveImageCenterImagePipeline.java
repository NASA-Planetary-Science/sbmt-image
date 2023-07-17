package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering;

import java.util.List;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.perspectiveImages.PerspectiveImageToRenderableImagePipeline;

public class PerspectiveImageCenterImagePipeline
{
	PerspectiveImageCenterImagePipeline(IPerspectiveImage image, Renderer renderer, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		PerspectiveImageToRenderableImagePipeline pipeline1 = new PerspectiveImageToRenderableImagePipeline(List.of(image));
		List<IRenderableImage> renderableImages = pipeline1.getRenderableImages();
		RenderablePointedImage renderableImage = (RenderablePointedImage)renderableImages.get(0);
		CameraOrientationPipeline pipeline2 = CameraOrientationPipeline.of(renderableImage, smallBodyModels, null);

		double viewAngle = renderableImage.getMaxFovAngle();
		renderer.setCameraOrientation(pipeline2.getSpacecraftPosition(), pipeline2.getFocalPoint(), pipeline2.getUpVector(), viewAngle);
	}

	public static void of(IPerspectiveImage image, Renderer renderer, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		new PerspectiveImageCenterImagePipeline(image, renderer, smallBodyModels);
	}
}
