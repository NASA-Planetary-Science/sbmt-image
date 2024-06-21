package edu.jhuapl.sbmt.image.ui.table.popup.export;

import java.io.File;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.io.export.SaveImageFileFromCacheOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import glum.gui.action.PopAction;

public class SaveImageAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	PerspectiveImageCollection<G1> collection;

	/**
	 * @param imagePopupMenu
	 */
	public SaveImageAction(PerspectiveImageCollection<G1> collection)
	{
		this.collection = collection;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		G1 aItem = (G1)collection.getSelectedItems().asList().get(0);
		List<File> files = Lists.newArrayList();
		try
		{
			Just.of(aItem)
				.operate(new SaveImageFileFromCacheOperator<G1>())
				.subscribe(Sink.of(files))
				.run();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}