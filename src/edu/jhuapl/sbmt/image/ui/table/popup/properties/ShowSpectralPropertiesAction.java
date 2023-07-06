package edu.jhuapl.sbmt.image.ui.table.popup.properties;

import java.util.List;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.spectrum.SbmtSpectrumWindowManager;

import glum.gui.action.PopAction;

public class ShowSpectralPropertiesAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
    /**
	 *
	 */
	private final PerspectiveImageCollection<G1> aManager;

	private final SbmtSpectrumWindowManager spectrumPanelManager;

	/**
	 * @param imagePopupMenu
	 */
	public ShowSpectralPropertiesAction(PerspectiveImageCollection<G1> aManager, SbmtSpectrumWindowManager spectrumPanelManager)
	{
		this.aManager = aManager;
		this.spectrumPanelManager = spectrumPanelManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() != 1)
			return;

		for (IPerspectiveImage aItem : aItemL)
		{
			//TODO make only valid for images with nLayers > 1 (spectra-type)
//			spectrumPanelManager.addData(aItem);
		}
	}

//	public void actionPerformed(ActionEvent e)
//    {
//        if (imageKeys.size() != 1)
//            return;
//        ImageKeyInterface imageKey = imageKeys.get(0);
//
//        try
//        {
//            this.imagePopupMenu.imageCollection.addImage(imageKey);
//            Image image = this.imagePopupMenu.imageCollection.getImage(imageKey);
//            if (image instanceof LEISAJupiterImage || image instanceof MVICQuadJupiterImage)
//                this.imagePopupMenu.spectrumPanelManager.addData(this.imagePopupMenu.imageCollection.getImage(imageKey));
//
//            this.imagePopupMenu.updateMenuItems();
//        }
//        catch (FitsException e1) {
//            e1.printStackTrace();
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//    }
}