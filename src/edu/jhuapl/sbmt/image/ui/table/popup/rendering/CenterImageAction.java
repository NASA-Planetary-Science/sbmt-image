package edu.jhuapl.sbmt.image.ui.table.popup.rendering;

import java.util.List;

import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.PerspectiveImageCenterImagePipeline;
import glum.gui.action.PopAction;

public class CenterImageAction<G1 extends IPerspectiveImage> extends PopAction<G1>
{
	private final PerspectiveImageCollection aManager;
	private final Renderer renderer;
	private final List<SmallBodyModel> smallBodyModels;

	/**
	 * @param imagePopupMenu
	 */
	public CenterImageAction(PerspectiveImageCollection aManager, Renderer renderer, List<SmallBodyModel> smallBodyModels)
	{
		this.aManager = aManager;
		this.renderer = renderer;
		this.smallBodyModels = smallBodyModels;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() != 1)
			return;


		try
		{
			if (aItemL.get(0).getPointingSourceType() != PointingSource.LOCAL_CYLINDRICAL)
				PerspectiveImageCenterImagePipeline.of(aItemL.get(0), renderer, smallBodyModels);
			else
			{
				 JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
	                        "Center in Window is not available for cylindrically projected images, yet. This will be implemented in a "
	                        + "future version of SBMT",
	                        "Notification",
	                        JOptionPane.WARNING_MESSAGE);
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		for (PerspectiveImage aItem : aItemL)
//		{
//	        double[] spacecraftPosition = new double[3];
//	        double[] focalPoint = new double[3];
//	        double[] upVector = new double[3];
//	        double viewAngle = 0.0;
//
//	        //TODO fix this
////	        aItem.getCameraOrientation(spacecraftPosition, focalPoint, upVector);
////            viewAngle = image.getMaxFovAngle();
//
//	        renderer.setCameraOrientation(spacecraftPosition, focalPoint, upVector, viewAngle);
//		}
	}

//	public void actionPerformed(ActionEvent e)
//    {
//        if (imageKeys.size() != 1)
//            return;
//        ImageKeyInterface imageKey = imageKeys.get(0);
//
//        double[] spacecraftPosition = new double[3];
//        double[] focalPoint = new double[3];
//        double[] upVector = new double[3];
//        double viewAngle = 0.0;
//
//        if (this.imagePopupMenu.imageBoundaryCollection != null && this.imagePopupMenu.imageBoundaryCollection.containsBoundary(imageKey))
//        {
//            PerspectiveImageBoundary boundary = this.imagePopupMenu.imageBoundaryCollection.getBoundary(imageKey);
//            boundary.getCameraOrientation(spacecraftPosition, focalPoint, upVector);
//            viewAngle = boundary.getImage().getMaxFovAngle();
//        }
//        else if (this.imagePopupMenu.imageCollection.containsImage(imageKey))
//        {
//            PerspectiveImage image = (PerspectiveImage)this.imagePopupMenu.imageCollection.getImage(imageKey);
//            image.getCameraOrientation(spacecraftPosition, focalPoint, upVector);
//            viewAngle = image.getMaxFovAngle();
//        }
//        else
//        {
//            return;
//        }
//
//        this.imagePopupMenu.renderer.setCameraOrientation(spacecraftPosition, focalPoint, upVector, viewAngle);
//    }
}