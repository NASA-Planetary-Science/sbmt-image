package edu.jhuapl.sbmt.image.ui.table.popup.boundaryColor;

import java.awt.Color;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import glum.gui.action.PopAction;

/**
 * {@link PopAction} that defines the action: "Reset Colors".
 *
 * @author lopeznr1
 */
class ResetBoundaryColorAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	// Ref vars
	private final PerspectiveImageCollection<G1> refManager;

	/**
	 * Standard Constructor
	 */
	public ResetBoundaryColorAction(PerspectiveImageCollection<G1> aManager)
	{
		refManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		for (G1 item : refManager.getSelectedItems())
			refManager.setImageBoundaryColor(item, Color.red);
		//TODO FIX THIS
//		refManager.clearCustomColorProvider(aItemL);
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Determine if any of the lidar colors can be reset
		boolean isResetAvail = false;
		//TODO FIX THIS
//		for (G1 aItem : aItemC)
//			isResetAvail |= refManager.hasCustomColorProvider(aItem) == true;

		aAssocMI.setEnabled(isResetAvail);
	}
}