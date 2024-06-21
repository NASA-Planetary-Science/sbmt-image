package edu.jhuapl.sbmt.image.ui.table.popup.rendering;

import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import glum.gui.action.PopAction;

public class MapBoundaryAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	/**
	 *
	 */
	private PerspectiveImageCollection<G1> aManager;

	/**
	 * @param imagePopupMenu
	 */
	public MapBoundaryAction(PerspectiveImageCollection<G1> aManager)
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
			aManager.setImageBoundaryShowing(aItem, !aItem.isBoundaryShowing());
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
			isSelected &= aItem.isBoundaryShowing() == true;
		((JCheckBoxMenuItem) aAssocMI).setSelected(isSelected);
	}

//	public void actionPerformed(ActionEvent e)
//	{
//		for (PerspectiveImage aItem : aItemL)
//		{
//			try
//			{
//				if (this.imagePopupMenu.mapBoundaryMenuItem.isSelected())
//				{
//					this.imagePopupMenu.imageBoundaryCollection.addBoundary(imageKey);
//					Image image = this.imagePopupMenu.imageCollection.getImage(imageKey);
//					if (image != null)
//					{
//						this.imagePopupMenu.imageBoundaryCollection.getBoundary(imageKey).setOffset(image.getOffset());
//					}
//				} else
//					this.imagePopupMenu.imageBoundaryCollection.removeBoundary(imageKey);
//			} catch (FitsException e1)
//			{
//				e1.printStackTrace();
//			} catch (IOException e1)
//			{
//				e1.printStackTrace();
//			}
//		}
//
//		this.imagePopupMenu.updateMenuItems();
//	}
}