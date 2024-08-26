package edu.jhuapl.sbmt.image.controllers;

import edu.jhuapl.saavtk.model.IPositionOrientationManager;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.ui.custom.table.CustomImageListTableView;
import glum.gui.action.PopupMenu;

public class CustomImageListTableController<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	CustomImageListTableView<G1> tablePanel;
	IPositionOrientationManager<SmallBodyModel> positionOrientationManager;

	public CustomImageListTableController(PerspectiveImageCollection<G1> collection, PopupMenu<G1> popupMenu)
	{
		this.tablePanel = new CustomImageListTableView<G1>(collection, popupMenu);
		this.tablePanel.setup();
	}

	public CustomImageListTableView<G1> getPanel()
	{
		return tablePanel;
	}
}
