package edu.jhuapl.sbmt.image.pipelineComponents.pipelines;

import java.util.List;

import javax.swing.JOptionPane;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.colorImages.ColorImageGeneratorPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.cylindricalImages.RenderableCylindricalImageToScenePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages.RenderablePointedImageToScenePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.gdal.InvalidGDALFileTypeException;

public class ImagePipelineFactory
{
	public static <G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> ImageToScenePipeline of(G1 image, List<SmallBodyModel> smallBodyModels)
	{
		ImageToScenePipeline pipeline = null;
		try
		{
			if (image.getImageType() != ImageType.GENERIC_IMAGE)
			{
				if (image.getNumberOfLayers() == 1)
					if (image.getPointingSourceType() == PointingSource.LOCAL_CYLINDRICAL)
					{
						pipeline = new RenderableCylindricalImageToScenePipeline(image, smallBodyModels);
					}
					else
					{
						pipeline = new RenderablePointedImageToScenePipeline<G1>(image, smallBodyModels);
					}
				else if (image.getNumberOfLayers() == 3)
				{
					pipeline = new ColorImageGeneratorPipeline(image.getImages(), smallBodyModels);
				}
				else
				{
					if (image.getPointingSourceType() == PointingSource.LOCAL_CYLINDRICAL)
					{
						pipeline = new RenderableCylindricalImageToScenePipeline(image, smallBodyModels);
					}
					else
					{
						pipeline = new RenderablePointedImageToScenePipeline<G1>(image, smallBodyModels);
					}
				}
			}
			else
			{
				if (image.getPointingSourceType() == PointingSource.LOCAL_CYLINDRICAL)
				{
					pipeline = new RenderableCylindricalImageToScenePipeline(image, smallBodyModels);
				}
				else
				{
					pipeline = new RenderablePointedImageToScenePipeline<G1>(image, smallBodyModels);
				}

			}
		}
		catch (InvalidGDALFileTypeException e)
		{
			 JOptionPane.showMessageDialog(null,
                     e.getMessage(),
                     "Invalid file type encountered",
                     JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pipeline;
	}
}
