package edu.jhuapl.sbmt.image.ui.table.popup.boundaryColor;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import glum.gui.action.PopAction;

class CustomBoundaryColorAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
   private PerspectiveImageCollection<G1> aManager;
   private final Component refParent;

	/**
	 * @param imagePopupMenu
	 */
	CustomBoundaryColorAction(PerspectiveImageCollection<G1> aManager, Component aParent)
	{
		this.aManager = aManager;
		this.refParent = aParent;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{

		//TODO fix this
//		Color tmpColor = refManager.getColorProviderTarget(aItemL.get(0)).getBaseColor();
		Color newColor = ColorChooser.showColorChooser(refParent, Color.red);
		if (newColor == null)
			return;

		ColorProvider tmpCP = new ConstColorProvider(newColor);
		for (G1 item : aManager.getSelectedItems())
			aManager.setImageBoundaryColor(item, tmpCP.getBaseColor());
//		refManager.installCustomColorProviders(aItemL, tmpCP, tmpCP);

	}

//	public void actionPerformed(ActionEvent e)
//    {
//        PerspectiveImageBoundary boundary = this.imagePopupMenu.imageBoundaryCollection.getBoundary(imageKeys.get(0));
//        int[] currentColor = boundary.getBoundaryColor();
//        Color newColor = ColorChooser.showColorChooser(this.imagePopupMenu.invoker, currentColor);
//        if (newColor != null)
//        {
//            for (ImageKeyInterface imageKey : imageKeys)
//            {
//                boundary = this.imagePopupMenu.imageBoundaryCollection.getBoundary(imageKey);
//                boundary.setBoundaryColor(newColor);
//            }
//        }
//    }
}