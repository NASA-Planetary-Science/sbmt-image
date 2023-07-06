package edu.jhuapl.sbmt.image.ui.table.popup.properties;

import java.util.List;

import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.editing.PointedRenderableImageEditingPipeline;

import glum.gui.action.PopAction;

public class EditPointingAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	private PerspectiveImageCollection<G1> aManager;

	public EditPointingAction(PerspectiveImageCollection<G1> aManager)
	{
		this.aManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if no items are selected
		if (aItemL.size() != 1)
			return;
		if (aItemL.get(0).getPointingSourceType() == PointingSource.LOCAL_CYLINDRICAL) return;	//maybe do error message here
		try
		{
			PointedRenderableImageEditingPipeline pipeline =
					new PointedRenderableImageEditingPipeline(aItemL.get(0), aManager.getSmallBodyModels());
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
