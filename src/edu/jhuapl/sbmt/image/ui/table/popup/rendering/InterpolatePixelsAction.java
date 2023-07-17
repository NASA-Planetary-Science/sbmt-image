package edu.jhuapl.sbmt.image.ui.table.popup.rendering;

import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

public class InterpolatePixelsAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
    /**
	 *
	 */
	private final PerspectiveImageCollection<G1> aManager;

	/**
	 * @param imagePopupMenu
	 */
	public InterpolatePixelsAction(PerspectiveImageCollection<G1> aManager)
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
			aManager.setImageInterpolationState(aItem, !aItem.getInterpolateState());
//			try
//			{
//				SaveImageStateToDiskPipeline.of(aItem);
//			}
//			catch (Exception e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
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
			isSelected &= aItem.getInterpolateState() == true;
		((JCheckBoxMenuItem) aAssocMI).setSelected(isSelected);
	}
}
