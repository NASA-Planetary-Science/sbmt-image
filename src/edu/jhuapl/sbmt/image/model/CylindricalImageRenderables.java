package edu.jhuapl.sbmt.image.model;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.internal.Lists;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.ImageRenderable;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageContrastOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageVtkMaskingOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.cylindricalImages.RenderableCylindricalImageFootprintGeneratorPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.LoadImageDataFromCachePipeline;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class CylindricalImageRenderables extends ImageRenderable
{
	List<vtkImageData> imageData = Lists.newArrayList();

	public CylindricalImageRenderables(IRenderableImage image, List<SmallBodyModel> smallBodyModels) throws IOException, Exception
	{
		this.smallBodyModels = smallBodyModels;
		prepareFootprints(image);
		processFootprints(footprintPolyData, imageData, image.isLinearInterpolation());
		processBoundaries();
	}

	private void prepareFootprints(IRenderableImage renderableImage) throws IOException, Exception
	{
		//clips if the image doesn't cover the entire body, and generates texture coords
		RenderableCylindricalImageFootprintGeneratorPipeline pipeline =
				new RenderableCylindricalImageFootprintGeneratorPipeline(renderableImage, smallBodyModels);
		footprintPolyData = pipeline.getFootprintPolyData();

		String imageDataFilename = getPrerenderingFileNameBase(renderableImage, smallBodyModels.get(0)) + "_imageData.vti";
		vtkImageData existingImageData = LoadImageDataFromCachePipeline.of(imageDataFilename).orNull();
		if (existingImageData != null)
		{
			imageData.add(existingImageData);
			return;
		}

        VtkImageRendererOperator imageRenderer = new VtkImageRendererOperator(true);
        Just.of(renderableImage.getLayer())
        	.operate(imageRenderer)
        	.operate(new VtkImageContrastOperator(renderableImage.getIntensityRange()))
        	.operate(new VtkImageVtkMaskingOperator(renderableImage.getMasking().getMask()))
        	.subscribe(Sink.of(imageData))
        	.run();

//        vtkXMLImageDataWriter writer = new vtkXMLImageDataWriter();
//        writer.SetFileName("/Users/steelrj1/Desktop/" + FilenameUtils.getBaseName(renderableImage.getFilename()) + "_" + smallBodyModels.get(0).getModelResolution() + "_" + smallBodyModels.get(0).getModelName().replaceAll(" ", "_")+ "_imageData.vti");
//        writer.SetInputData(imageData.get(0));
//        writer.Update();
	}

	private String getPrerenderingFileNameBase(IRenderableImage renderableImage, SmallBodyModel smallBodyModel)
    {
        String imageName = renderableImage.getFilename();
        String topPath = FileCache.instance().getFile(imageName).getParent();
        if (new File(imageName).exists()) topPath = new File(imageName).getParent();
        String result = SafeURLPaths.instance().getString(topPath, "support",
        												  FilenameUtils.getBaseName(imageName) + "_" + smallBodyModel.getModelResolution() + "_" + smallBodyModel.getModelName().replaceAll(" ", "_"));

        return result;
    }
}

