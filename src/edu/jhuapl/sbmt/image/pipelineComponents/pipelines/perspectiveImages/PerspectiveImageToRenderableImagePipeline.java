package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.perspectiveImages;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer.LayerMasking;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImageGenerator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.IPerspectiveImageToLayerAndMetadataPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.gdal.InvalidGDALFileTypeException;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.pointing.SumfileReaderPublisher;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.pointing.io.PointingFileReader;

public class PerspectiveImageToRenderableImagePipeline
{
	List<RenderablePointedImage> renderableImages = Lists.newArrayList();
	List<HashMap<String, String>> metadata = Lists.newArrayList();

	public PerspectiveImageToRenderableImagePipeline(List<IPerspectiveImage> images) throws InvalidGDALFileTypeException, Exception
	{
		for (IPerspectiveImage image : images)
		{
			String filename = image.getFilename();
			String pointingFile = image.getPointingSource();
			synchronized(PerspectiveImageToRenderableImagePipeline.class) {
				if (!new File(filename).exists())
				{
					filename = FileCache.getFileFromServer(image.getFilename()).getAbsolutePath();
					pointingFile = FileCache.getFileFromServer(image.getPointingSource()).getAbsolutePath();
				}
				processFile(filename, pointingFile, image);
			}
		}
	}

	private void processFile(String filename, String pointingFile, IPerspectiveImage image) throws InvalidGDALFileTypeException, Exception
	{
		IPerspectiveImageToLayerAndMetadataPipeline inputPipeline = IPerspectiveImageToLayerAndMetadataPipeline.of(image);
		List<Layer> updatedLayers = inputPipeline.getLayers();
		metadata = inputPipeline.getMetadata();

		//generate image pointing (in: filename, out: ImagePointing)
		IPipelinePublisher<PointingFileReader> pointingPublisher = null;
		if (image.getPointingSourceType() == PointingSource.SPICE || image.getPointingSourceType() == PointingSource.CORRECTED_SPICE)
			pointingPublisher = new InfofileReaderPublisher(pointingFile);
		else
			pointingPublisher = new SumfileReaderPublisher(pointingFile);
		metadata.get(0).put("Name", new File(image.getFilename()).getName());
		metadata.get(0).put("Start Time", pointingPublisher.getOutputs().get(0).getStartTime());
		metadata.get(0).put("Stop Time", pointingPublisher.getOutputs().get(0).getStartTime());


		if (metadata.get(0).get("WINDOWH") != null && metadata.get(0).get("WINDOWH").equals("512"))
		{
			int windowH = Integer.parseInt(metadata.get(0).get("WINDOWH"));
			int windowX = Integer.parseInt(metadata.get(0).get("WINDOWX"));
			int windowY = Integer.parseInt(metadata.get(0).get("WINDOWY"));

			image.setAutoMaskValues(new int[] {windowX, windowH - windowX,windowY,windowH - windowY});
			image.setMaskValues(new int[] {windowX, windowH - windowX,windowY,windowH - windowY});
		}
		else
		{
			image.setAutoMaskValues(image.getMaskValues());
		}

		//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
		IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
		IPipelinePublisher<Triple<Layer, HashMap<String, String>, PointingFileReader>> imageComponents = Publishers.formTriple(layerPublisher, Just.of(metadata), pointingPublisher);

		IPipelineOperator<Triple<Layer, HashMap<String, String>, PointingFileReader>, RenderablePointedImage> renderableImageGenerator = new RenderablePointedImageGenerator();

		//***************************************************************************************
		//generate image polydata with texture coords (in: RenderableImage, out: vtkPolydata)
		//***************************************************************************************
		List<RenderablePointedImage> renderableImage = Lists.newArrayList();
		imageComponents
			.operate(renderableImageGenerator)
			.subscribe(Sink.of(renderableImage)).run();
		renderableImages.addAll(renderableImage);

		for (RenderablePointedImage renderableImg : renderableImages)
		{
			renderableImg.setImageSource(image.getPointingSourceType());
			renderableImg.setFilename(image.getFilename());
			renderableImg.setMasking(new LayerMasking(image.getMaskValues()));
			renderableImg.setOffset(image.getOffset());
			renderableImg.setDefaultOffset(image.getDefaultOffset());
			renderableImg.setIntensityRange(image.getIntensityRange());
			renderableImg.setOfflimbIntensityRange(image.getOfflimbIntensityRange());
//			renderableImg.setPad(image.getPadValues());
//			renderableImg.setFullSize(image.getMaxSizeValues());
			renderableImg.setImageBinPadding(image.getImageBinPadding());
			renderableImg.setBinning(image.getImageBinning());
//			double diagonalLength = smallBodyModel.get(0).getBoundingBoxDiagonalLength();
//			System.out.println("RenderablePointedImageActorPipeline: RenderablePointedImageActorPipeline: diag length " + diagonalLength);
//			double[] scPos = renderableImages.get(0).getPointing().getSpacecraftPosition();
			renderableImg.setMinFrustumLength(image.getMinFrustumLength());
			renderableImg.setMaxFrustumLength(image.getMaxFrustumLength());
			renderableImg.setOfflimbDepth(image.getOfflimbDepth());
			renderableImg.setLinearInterpolation(image.getInterpolateState());
			//This is a special case for AMICA, because it reads binning and padding information from the metadata, need a better way to handle this, but hard because these fields are non-uniform
			if (metadata.get(0).containsKey("START_H"))
			{
				renderableImg.setStartH(Integer.valueOf(metadata.get(0).get("START_H")));
				renderableImg.setLastV(Integer.valueOf(metadata.get(0).get("LAST_V")));
				renderableImg.setBinning(Integer.valueOf(metadata.get(0).get("BINNING")));

			}
		}
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
