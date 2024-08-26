package edu.jhuapl.sbmt.image.ui.table.popup.properties;

import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.cylindricalImages.CylindricalImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.perspectiveImages.PerspectiveImageToDerivedMetadataPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.perspectiveImages.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.publishers.gdal.InvalidGDALFileTypeException;
import edu.jhuapl.sbmt.image.pipelineComponents.subscribers.preview.MetadataPreview;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import glum.gui.action.PopAction;

public class ShowHeadersAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	private MetadataPreview preview = null;
	private PerspectiveImageCollection<G1> aManager;
	private SmallBodyModel smallBodyModel;

	/**
	 * @param imagePopupMenu
	 */
	public ShowHeadersAction(SmallBodyModel smallBodyModel, PerspectiveImageCollection<G1> aManager)
	{
		this.aManager = aManager;
		this.smallBodyModel = smallBodyModel;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() != 1)
			return;

		try
		{
			G1 image = aItemL.get(0);


			if (image.getPointingSourceType() == PointingSource.LOCAL_CYLINDRICAL)
			{
				CylindricalImageToRenderableImagePipeline pipeline = CylindricalImageToRenderableImagePipeline.of(List.of(aItemL.get(0)));
				List<HashMap<String, String>> metadata = pipeline.getMetadata();
				List<HashMap<String, String>> metadatas = List.of(pipeline.getMetadata().get(0));
				preview = new MetadataPreview("Metadata for " + image.getName());
				Just.of(metadatas)
					.subscribe(preview)
					.run();

				preview.getPanel().setVisible(true);
			}
			else
			{
				if (image.getNumberOfLayers() == 3)
				{
					HashMap<String, String> metadata = new HashMap<String, String>();
					metadata.put("Image 1", image.getImages().get(0).getFilename());
					metadata.put("Image 2", image.getImages().get(1).getFilename());
					metadata.put("Image 3", image.getImages().get(2).getFilename());
					preview = new MetadataPreview("Metadata for " + image.getName());
					Just.of(metadata)
						.subscribe(preview)
						.run();

					preview.getPanel().setVisible(true);
				}
				else
				{
					PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(List.of(aItemL.get(0)));
					List<IRenderableImage> renderableImages = pipeline.getRenderableImages();
					List<HashMap<String, String>> metadata = pipeline.getMetadata();
					HashMap<String, String> derivedMetadata = new PerspectiveImageToDerivedMetadataPipeline(renderableImages.get(0), List.of(smallBodyModel)).getMetadata();
					List<HashMap<String, String>> metadatas = List.of(pipeline.getMetadata().get(0), derivedMetadata);

					preview = new MetadataPreview("Metadata for " + image.getName());
					Just.of(metadatas)
						.subscribe(preview)
						.run();

					preview.getPanel().setVisible(true);
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
	}
}