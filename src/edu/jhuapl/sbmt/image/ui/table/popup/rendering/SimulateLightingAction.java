package edu.jhuapl.sbmt.image.ui.table.popup.rendering;

import java.util.Collection;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.PerspectiveImageSimulateLightingPipeline;
import glum.gui.action.PopAction;

public class SimulateLightingAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	/**
	 *
	 */
	private final PerspectiveImageCollection<G1> aManager;

	private final Renderer renderer;

	private boolean newSelectedState = false;

	/**
	 * @param imagePopupMenu
	 */
	public SimulateLightingAction(PerspectiveImageCollection<G1> aManager, Renderer renderer)
	{
		this.aManager = aManager;
		this.renderer = renderer;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() != 1)
			return;

		G1 aItem = aItemL.get(0);
		try
		{
			newSelectedState = !aItem.isSimulateLighting();
			PerspectiveImageSimulateLightingPipeline.of(aItem, renderer, newSelectedState);
			aManager.setSimulateLighting(aItem, newSelectedState);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (G1 tempImage : aManager.getAllItems())
		{
			aManager.setSimulateLighting(tempImage, false);
		}
		aItem.setSimulateLighting(newSelectedState);
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		boolean isSelected = aManager.isSimulateLighting(aManager.getSelectedItems().asList().get(0));
		((JCheckBoxMenuItem) aAssocMI).setSelected(isSelected);
	}
}