package edu.jhuapl.sbmt.image.ui;

import java.awt.event.InputEvent;
import java.util.List;

import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.sbmt.image.model.Image;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.model.PerspectiveImageMetadata;

/**
 * PickListener responsible for notifying picked ({@link PerspectiveImageMetadata})
 * images of the actual surface point corresponding to the picked action.
 * <P>
 * This basis for this logic originated from the file (prior to 2019Oct28):
 * edu.jhuapl.sbmt.gui.image.ui.images.ImagePickManager.
 *
 * @author lopeznr1
 */
public class ImageDefaultPickHandler2 implements PickListener
{
	// Reference vars
	private final ModelManager refModelManager;

	/**
	 * Standard Constructor
	 */
	public ImageDefaultPickHandler2(ModelManager aModelManager)
	{
		refModelManager = aModelManager;
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if not the right type of model
		Model tmpModel = refModelManager.getModel(aPrimaryTarg.getActor());
		if (tmpModel instanceof PerspectiveImageCollection == false)
			return;

		// Bail if no surface target or not the primary action
		if (aSurfaceTarg == PickTarget.Invalid || aMode != PickMode.ActivePri)
			return;

		// Ensure we have at least one image
		PerspectiveImageCollection imageCollection = (PerspectiveImageCollection) tmpModel;
		List<Image> imageS = imageCollection.getAllItems();
		if (imageS.size() == 0)
			return;

		//TODO FIX THIS
//		// TODO Not sure why the first image is always set
//		PerspectiveImage firstImage = imageS.iterator().next();
////		System.out.println("Picked image: " + firstImage.getClass().getSimpleName());
//		if (firstImage instanceof PerspectiveImage)
//			((PerspectiveImage) firstImage).setPickedPosition(aSurfaceTarg.getPosition().toArray());
//
//		// Get the image that was actually picked
//		vtkActor tmpActor = aPrimaryTarg.getActor();
//		PerspectiveImage pickedImage = imageCollection.getImage(tmpActor);
//		if (pickedImage instanceof PerspectiveImage)
//			((PerspectiveImage) pickedImage).setPickedPosition(aSurfaceTarg.getPosition().toArray());
	}

}
