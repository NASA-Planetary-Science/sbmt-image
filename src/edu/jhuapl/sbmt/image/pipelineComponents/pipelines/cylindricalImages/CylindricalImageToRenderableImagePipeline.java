package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.cylindricalImages;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.model.CylindricalBounds;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.model.RenderableCylindricalImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.cylindricalImage.RenderableCylindricalImageGenerator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.IPerspectiveImageToLayerAndMetadataPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.gdal.InvalidGDALFileTypeException;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class CylindricalImageToRenderableImagePipeline
{
	List<RenderableCylindricalImage> renderableImages = Lists.newArrayList();
	List<HashMap<String, String>> metadata = Lists.newArrayList();

	CylindricalImageToRenderableImagePipeline(List<IPerspectiveImage> images) throws InvalidGDALFileTypeException, Exception
	{
		for (IPerspectiveImage image : images)
		{
			IPerspectiveImageToLayerAndMetadataPipeline inputPipeline = IPerspectiveImageToLayerAndMetadataPipeline.of(image);
			List<Layer> updatedLayers = inputPipeline.getLayers();
			if (image.getFilename().toLowerCase().endsWith("png") || image.getFilename().toLowerCase().endsWith("jpg") || image.getFilename().toLowerCase().endsWith("jpeg"))
				updatedLayers = List.of(inputPipeline.getLayers().get(0));
			metadata = inputPipeline.getMetadata();
			List<CylindricalBounds> boundsCollection = Lists.newArrayList();
			for (int i=0; i<updatedLayers.size(); i++)
			{
				boundsCollection.add(image.getBounds());
			}
			IPipelinePublisher<HashMap<String, String>> metadataPipeline = Just.of(metadata);
			IPipelinePublisher<CylindricalBounds> boundsPipeline = Just.of(boundsCollection);
			IPipelinePublisher<Triple<Layer, HashMap<String, String>, CylindricalBounds>> imageComponents = Publishers.formTriple(Just.of(updatedLayers), metadataPipeline, boundsPipeline);
			//make a renderable image generator
			IPipelineOperator<Triple<Layer, HashMap<String, String>, CylindricalBounds>, RenderableCylindricalImage> renderableImageGenerator = new RenderableCylindricalImageGenerator();

			//*******************************************************************************************************************************
			//generate renderable image from the components using the generator (in: layer/metadata/bounds, out: RenderableCylindricalImage)
			//*******************************************************************************************************************************
			imageComponents
				.operate(renderableImageGenerator)
				.subscribe(Sink.of(renderableImages)).run();

			for (RenderableCylindricalImage renderableImg : renderableImages)
			{
				renderableImg.setFilename(image.getFilename());
			}
		}
	}

	public static CylindricalImageToRenderableImagePipeline of(List<IPerspectiveImage> images) throws InvalidGDALFileTypeException, Exception
	{
		return new CylindricalImageToRenderableImagePipeline(images);
	}

	public List<IRenderableImage> getRenderableImages()
	{
		List<IRenderableImage> images = Lists.newArrayList();
		images.addAll(renderableImages);
		return images;
	}

	public List<HashMap<String, String>> getMetadata()
	{
		return metadata;
	}
}