package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.perspectiveImages;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.io.PerspectiveImageToDerviedMetadataOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.FootprintToIlluminationAttributesOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.ImageIllumination;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.ImagePixelScale;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImageToPixelScaleAttributesOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import vtk.vtkPolyData;

public class PerspectiveImageToDerivedMetadataPipeline
{
	List<HashMap<String, String>> metadata = Lists.newArrayList();
	List<ImageIllumination> illumAtts = Lists.newArrayList();
	List<ImagePixelScale> pixelAtts = Lists.newArrayList();

	public PerspectiveImageToDerivedMetadataPipeline(IRenderableImage image, List<SmallBodyModel> smallBodyModels) throws Exception
	{
		RenderablePointedImage renderableImage = (RenderablePointedImage)image;
		RenderablePointedImageFootprintGeneratorPipeline footprintPipeline =
				new RenderablePointedImageFootprintGeneratorPipeline(renderableImage, smallBodyModels);
		List<vtkPolyData> polyData = footprintPipeline.getFootprintPolyData();

		RenderablePointedImageToPixelScaleAttributesOperator pixelOperator = new RenderablePointedImageToPixelScaleAttributesOperator();
		FootprintToIlluminationAttributesOperator illuminationOperator = new FootprintToIlluminationAttributesOperator();


		Just.of(Pair.of(polyData.get(0), renderableImage))
			.operate(pixelOperator)
			.subscribe(Sink.of(pixelAtts))
			.run();


		Just.of(Pair.of(polyData.get(0), renderableImage.getPointing()))
			.operate(illuminationOperator)
			.subscribe(Sink.of(illumAtts))
			.run();

		Triple<RenderablePointedImage, ImagePixelScale, ImageIllumination> inputs =
				Triple.of(renderableImage,
						pixelAtts.get(0),
						illumAtts.get(0));

		Just.of(inputs)
			.operate(new PerspectiveImageToDerviedMetadataOperator())
			.subscribe(Sink.of(metadata))
			.run();

	}

	public HashMap<String, String> getMetadata()
	{
		return metadata.get(0);
	}

	public List<ImageIllumination> getIllumAtts()
	{
		return illumAtts;
	}

	public List<ImagePixelScale> getPixelAtts()
	{
		return pixelAtts;
	}
}
