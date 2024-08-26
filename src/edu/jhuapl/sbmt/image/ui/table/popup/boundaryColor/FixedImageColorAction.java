package edu.jhuapl.sbmt.image.ui.table.popup.boundaryColor;

import java.awt.Color;
import java.util.List;

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import glum.gui.action.PopAction;

/**
 * {@link PopAction} that defines the action: "Fixed Color".
 *
 * @author lopeznr1
 */
class FixedImageColorAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	// Ref vars
	private final PerspectiveImageCollection<G1> refManager;
	private final ColorProvider refCP;

	/**
	 * Standard Constructor
	 */
	public FixedImageColorAction(PerspectiveImageCollection<G1> aManager, Color aColor)
	{
		refManager = aManager;
		refCP = new ConstColorProvider(aColor);
	}

	/**
	 * Returns the color associated with this Action
	 */
	public Color getColor()
	{
		return refCP.getBaseColor();
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		for (G1 item : refManager.getSelectedItems())
			refManager.setImageBoundaryColor(item, refCP.getBaseColor());
	}

}