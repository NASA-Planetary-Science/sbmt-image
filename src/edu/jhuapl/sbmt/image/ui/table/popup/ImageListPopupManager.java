package edu.jhuapl.sbmt.image.ui.table.popup;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.popup.PopupManager;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.model.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image.model.SbmtSpectralImageWindowManager;
import glum.gui.action.PopupMenu;

/**
 * This class is responsible for the creation of popups and for the routing of
 * the right click events (i.e. show popup events) to the correct model.
 */
public class ImageListPopupManager extends PopupManager
{
	public <G1 extends IPerspectiveImage  & IPerspectiveImageTableRepresentable> ImageListPopupManager(ModelManager modelManager, SbmtInfoWindowManager infoPanelManager,
			SbmtSpectralImageWindowManager spectrumPanelManager, Renderer renderer)
	{
		super(modelManager);

		@SuppressWarnings("unchecked")
		PerspectiveImageCollection<G1> imageCollection = (PerspectiveImageCollection<G1>) modelManager.getModel(ModelNames.provide("IMAGES_V2"));
		@SuppressWarnings("unused")
		PopupMenu<G1> popupMenu =
        		new ImageListPopupMenu<G1>(modelManager, imageCollection, infoPanelManager,
						spectrumPanelManager, renderer, renderer);
		//TODO FIX THIS
//		registerPopup(modelManager.getModel(ModelNames.IMAGES_V2), popupMenu);
	}

}
