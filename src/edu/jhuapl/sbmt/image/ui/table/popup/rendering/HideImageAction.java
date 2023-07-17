package edu.jhuapl.sbmt.image.ui.table.popup.rendering;

import java.util.List;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

public class HideImageAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
    /**
	 *
	 */
	private final PerspectiveImageCollection aManager;

	/**
	 * @param imagePopupMenu
	 */
	public HideImageAction(PerspectiveImageCollection aManager)
	{
		this.aManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() == 0)
			return;

		for (G1 aItem : aItemL)
		{
			aManager.setImageMapped(aItem, !aItem.isMapped());
		}
	}

//	public void actionPerformed(ActionEvent e)
//    {
//        for (ImageKeyInterface imageKey : imageKeys)
//        {
//            try
//            {
//                this.imagePopupMenu.imageCollection.addImage(imageKey);
//                Image image = this.imagePopupMenu.imageCollection.getImage(imageKey);
//                image.setVisible(!this.imagePopupMenu.hideImageMenuItem.isSelected());
//            }
//            catch (Exception ex)
//            {
//                ex.printStackTrace();
//            }
//        }
//
//        this.imagePopupMenu.updateMenuItems();
//    }
}