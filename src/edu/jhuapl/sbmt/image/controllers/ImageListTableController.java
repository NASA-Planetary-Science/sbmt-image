package edu.jhuapl.sbmt.image.controllers;

import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.ui.table.ImageListTableView;
import glum.gui.action.PopupMenu;

public class ImageListTableController<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	ImageListTableView<G1> tablePanel;

	public ImageListTableController(PerspectiveImageCollection<G1> collection, PopupMenu<G1> popupMenu)
	{
		this.tablePanel = new ImageListTableView<G1>(collection, popupMenu);
		this.tablePanel.setup();
	}

	public ImageListTableView<G1> getPanel()
	{
		return tablePanel;
	}
}
