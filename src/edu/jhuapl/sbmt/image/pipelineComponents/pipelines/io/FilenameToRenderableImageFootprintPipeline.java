package edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io;

import java.io.IOException;
import java.util.List;

import vtk.vtkPolyData;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages.RenderablePointedImageFootprintGeneratorPipeline;

public class FilenameToRenderableImageFootprintPipeline
{
	private List<RenderablePointedImage> images;
	private List<String> pointingFilenames;
	private List<vtkPolyData> footprints;

	public FilenameToRenderableImageFootprintPipeline(String filename, PointingSource imageSource, List<SmallBodyModel> smallBodyModels, ImagingInstrument selectedInstrument) throws IOException, Exception
	{
//		ISmallBodyViewConfig config = smallBodyModels.get(0).getSmallBodyConfig();

		FilenameToRenderableImagePipeline pipeline = FilenameToRenderableImagePipeline.of(filename, imageSource, selectedInstrument);
		images = pipeline.getImages();
		pointingFilenames = pipeline.getPointingFilenames();

    	RenderablePointedImageFootprintGeneratorPipeline pipeline2 = new RenderablePointedImageFootprintGeneratorPipeline(images.get(0), smallBodyModels);
		footprints = pipeline2.getFootprintPolyData();
	}

	public static FilenameToRenderableImageFootprintPipeline of(String filename, PointingSource imageSource, List<SmallBodyModel> smallBodyModels, ImagingInstrument selectedInstrument) throws IOException, Exception
	{
		return new FilenameToRenderableImageFootprintPipeline(filename, imageSource, smallBodyModels, selectedInstrument);
	}

	public List<RenderablePointedImage> getImages()
	{
		return images;
	}

	public List<String> getPointingFilenames()
	{
		return pointingFilenames;
	}

	public List<vtkPolyData> getFootprints()
	{
		return footprints;
	}
}
