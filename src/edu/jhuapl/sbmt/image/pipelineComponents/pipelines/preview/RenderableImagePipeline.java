package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.preview;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IImagingInstrument;
import edu.jhuapl.sbmt.image.model.ImageFlip;
import edu.jhuapl.sbmt.image.model.Orientation;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer.LayerLinearInterpolaterOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer.LayerRotationOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer.LayerXFlipOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer.LayerYFlipOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImageGenerator;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.builtin.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.builtin.BuiltInFitsReader;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.pointing.SumfileReaderPublisher;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.IPipeline;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.operator.PassthroughOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class RenderableImagePipeline implements IPipeline<RenderablePointedImage>
{
	List<RenderablePointedImage> renderableImages = Lists.newArrayList();
	IPipelinePublisher<Triple<Layer, HashMap<String, String>, PointingFileReader>> imageComponents;
	IPipelineOperator<Triple<Layer, HashMap<String, String>, PointingFileReader>, RenderablePointedImage> renderableImageGenerator;
	private String imageFile;
	private PointingSource imageSource;

	public RenderableImagePipeline(String imageFile, String pointingFile, IImagingInstrument instrument,  PointingSource imageSource) throws Exception
	{
		this.imageFile = imageFile;
		this.imageSource = imageSource;
		IPipelinePublisher<Layer> reader = new BuiltInFitsReader(imageFile, instrument.getFillValues());
		Orientation orientation = instrument.getOrientation(imageSource);

		LayerRotationOperator rotationOperator = new LayerRotationOperator(orientation.getRotation());
		BasePipelineOperator<Layer, Layer> flipOperator = new PassthroughOperator<Layer>();
		if (orientation.getFlip().equals(ImageFlip.X))
			flipOperator = new LayerXFlipOperator();
		else if (orientation.getFlip().equals(ImageFlip.Y))
			flipOperator = new LayerYFlipOperator();

		IPipelineOperator<Layer, Layer> linearInterpolator = null;
		if (instrument.getLinearInterpolationDims() == null || (instrument.getLinearInterpolationDims()[0] == 0 && instrument.getLinearInterpolationDims()[1] == 0))
			linearInterpolator = new PassthroughOperator<>();
		else
			linearInterpolator = new LayerLinearInterpolaterOperator(instrument.getLinearInterpolationDims()[0], instrument.getLinearInterpolationDims()[1]);

		List<Layer> updatedLayers = Lists.newArrayList();
		reader
			.operate(linearInterpolator)
			.operate(rotationOperator)
			.operate(flipOperator)
			.subscribe(Sink.of(updatedLayers))
			.run();

		// generate image pointing (in: filename, out: ImagePointing)
		IPipelinePublisher<PointingFileReader> pointingPublisher = null;
		if (pointingFile.endsWith(".INFO") || pointingFile.endsWith(".adjusted"))
			pointingPublisher = new InfofileReaderPublisher(pointingFile);
		else
			pointingPublisher = new SumfileReaderPublisher(pointingFile);

		// generate metadata (in: filename, out: ImageMetadata)
		IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInFitsHeaderReader(
				imageFile);

		// combine image source (in: Layer+ImageMetadata+ImagePointing, out:
		// RenderableImage)
		IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
		imageComponents = Publishers.formTriple(layerPublisher, metadataReader, pointingPublisher);
		renderableImageGenerator = new RenderablePointedImageGenerator();
	}

	public void run() throws Exception
	{
		// ***************************************************************************************
		// generate image polydata with texture coords (in: RenderableImage,
		// out: vtkPolydata)
		// ***************************************************************************************
		renderableImages.clear();
		imageComponents
			.operate(renderableImageGenerator)
			.subscribe(Sink.of(renderableImages))
			.run();
		renderableImages.get(0).setFilename(imageFile);
		renderableImages.get(0).setImageSource(imageSource);
	}

	public List<RenderablePointedImage> getOutput()
	{
		return renderableImages;
	}
}