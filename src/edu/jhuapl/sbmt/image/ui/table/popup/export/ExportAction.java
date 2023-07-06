package edu.jhuapl.sbmt.image.ui.table.popup.export;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

public class ExportAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{

	// State vars
	private Map<JMenuItem, PopAction<G1>> actionM;

	public ExportAction(PerspectiveImageCollection aManager, Component aParent, JMenu aMenu)
	{
		actionM = new HashMap<>();

		JMenuItem exportFITS = formMenuItem(new SaveImageAction<G1>(aManager), "FITS");
		aMenu.add(exportFITS);

		JMenuItem exportFITSPointing = formMenuItem(new ExportFitsInfoPairsAction<G1>(aManager), "FITS/Pointing Pair");
		aMenu.add(exportFITSPointing);

		JMenuItem exportPointing = formMenuItem(new ExportInfofileAction<G1>(aManager), "Pointing File");
		aMenu.add(exportPointing);

		JMenuItem exportENVI = formMenuItem(new ExportENVIImageAction<G1>(aManager), "ENVI File");
		aMenu.add(exportENVI);

		JMenuItem exportBackplanes = formMenuItem(new SaveBackplanesAction<G1>(aManager), "Backplanes");
		aMenu.add(exportBackplanes);

		JMenuItem exportGeometry = formMenuItem(new SaveGeometryAction<G1>(aManager), "Image Geometry (OBJ)");
		aMenu.add(exportGeometry);


	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		; // Nothing to do
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);
	}

	/**
	 * Helper method to form and return the specified menu item.
	 * <P>
	 * The menu item will be registered into the action map.
	 *
	 * @param aAction Action corresponding to the menu item.
	 * @param aTitle The title of the menu item.
	 */
	private JMenuItem formMenuItem(PopAction<G1> aAction, String aTitle)
	{
		JMenuItem retMI = new JMenuItem(aAction);
		retMI.setText(aTitle);

		actionM.put(retMI, aAction);

		return retMI;
	}
}
