package edu.jhuapl.sbmt.image.ui.table.popup.rendering;

import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

public class ShowFrustumAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
    /**
	 *
	 */
	private final PerspectiveImageCollection<G1> aManager;

	/**
	 * @param imagePopupMenu
	 */
	public ShowFrustumAction(PerspectiveImageCollection<G1> aManager)
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
			aManager.setImageFrustumVisible(aItem, !aItem.isFrustumShowing());
		}
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// If any items are not visible then set checkbox to unselected
		// in order to allow all chosen items to be toggled on
		boolean isSelected = true;
		for (G1 aItem : aItemC)
			isSelected &= aItem.isFrustumShowing() == true;
		((JCheckBoxMenuItem) aAssocMI).setSelected(isSelected);
	}

//	public void actionPerformed(ActionEvent e)
//    {
//        for (ImageKeyInterface imageKey : imageKeys)
//        {
//            try
//            {
//                this.imagePopupMenu.imageCollection.addImage(imageKey);
//                PerspectiveImage image = (PerspectiveImage)this.imagePopupMenu.imageCollection.getImage(imageKey);
//                image.setShowFrustum(this.imagePopupMenu.showFrustumMenuItem.isSelected());
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