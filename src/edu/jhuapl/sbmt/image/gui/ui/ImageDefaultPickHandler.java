package edu.jhuapl.sbmt.image.gui.ui;

import java.awt.event.InputEvent;
import java.util.Set;

import vtk.vtkActor;

import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.sbmt.core.image.Image;
import edu.jhuapl.sbmt.core.rendering.PerspectiveImage;
import edu.jhuapl.sbmt.image.model.ImageCollection;

/**
 * PickListener responsible for notifying picked ({@link PerspectiveImage})
 * images of the actual surface point corresponding to the picked action.
 * <P>
 * This basis for this logic originated from the file (prior to 2019Oct28):
 * edu.jhuapl.sbmt.gui.image.ui.images.ImagePickManager.
 *
 * @author lopeznr1
 */
public class ImageDefaultPickHandler implements PickListener
{
	// Reference vars
	private final ModelManager refModelManager;

	/**
	 * Standard Constructor
	 */
	public ImageDefaultPickHandler(ModelManager aModelManager)
	{
		refModelManager = aModelManager;
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if not the right type of model
		Model tmpModel = refModelManager.getModel(aPrimaryTarg.getActor());
		if (tmpModel instanceof ImageCollection == false)
			return;

		// Bail if no surface target or not the primary action
		if (aSurfaceTarg == PickTarget.Invalid || aMode != PickMode.ActivePri)
			return;

		// Ensure we have at least one image
		ImageCollection imageCollection = (ImageCollection) tmpModel;
		Set<Image> imageS = imageCollection.getImages();
		if (imageS.size() == 0)
			return;

		// TODO Not sure why the first image is always set
		Image firstImage = imageS.iterator().next();
//		System.out.println("Picked image: " + firstImage.getClass().getSimpleName());
		if (firstImage instanceof PerspectiveImage)
			((PerspectiveImage) firstImage).setPickedPosition(aSurfaceTarg.getPosition().toArray());

		// Get the image that was actually picked
		vtkActor tmpActor = aPrimaryTarg.getActor();
		Image pickedImage = imageCollection.getImage(tmpActor);
		if (pickedImage instanceof PerspectiveImage)
			((PerspectiveImage) pickedImage).setPickedPosition(aSurfaceTarg.getPosition().toArray());
	}

}
