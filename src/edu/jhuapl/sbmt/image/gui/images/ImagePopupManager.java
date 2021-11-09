package edu.jhuapl.sbmt.image.gui.images;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.popup.PopupManager;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.sbmt.image.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.image.types.ImageCollection;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundaryCollection;

/**
 * This class is responsible for the creation of popups and for the routing of
 * the right click events (i.e. show popup events) to the correct model.
 */
public class ImagePopupManager extends PopupManager
{
	public ImagePopupManager(ModelManager modelManager, SbmtInfoWindowManager infoPanelManager,
			SbmtSpectrumWindowManager spectrumPanelManager, Renderer renderer)
	{
		super(modelManager);

		ImageCollection imageCollection = (ImageCollection) modelManager.getModel(ModelNames.IMAGES).get(0);
		PerspectiveImageBoundaryCollection imageBoundaries = (PerspectiveImageBoundaryCollection) modelManager
				.getModel(ModelNames.PERSPECTIVE_IMAGE_BOUNDARIES).get(0);
		PopupMenu popupMenu = new ImagePopupMenu(modelManager, imageCollection, imageBoundaries, infoPanelManager,
				spectrumPanelManager, renderer, renderer);
		registerPopup(modelManager.getModel(ModelNames.IMAGES).get(0), popupMenu);
	}

}
