package edu.jhuapl.sbmt.image.ui.table.popup.export;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import vtk.vtkActor;
import vtk.vtkOBJExporter;
import vtk.rendering.jogl.vtkJoglPanelComponent;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;

import glum.gui.action.PopAction;

public class SaveGeometryAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	PerspectiveImageCollection<G1> collection;

	public SaveGeometryAction(PerspectiveImageCollection<G1> collection)
	{
		this.collection = collection;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		try
		{
			for (G1 image : collection.getSelectedItems())
			{
				File file = CustomFileChooser.showSaveDialog(null, "Export to OBJ",
						FilenameUtils.getBaseName(FilenameUtils.removeExtension(image.getName())) + ".obj");

				if (file != null)
				{

					String fileprefix = FilenameUtils.removeExtension(file.getAbsolutePath());

					vtkJoglPanelComponent renderPanel = new vtkJoglPanelComponent();
					renderPanel.getRenderWindow().OffScreenRenderingOn();
					vtkOBJExporter exporter = new vtkOBJExporter();
					exporter.SetRenderWindow(renderPanel.getRenderWindow());

					Map<List<vtkActor>, String> actorsToSave = collection.getImageRenderedComponents(image);
					for (List<vtkActor> actors : actorsToSave.keySet())
					{
						String type = actorsToSave.get(actors);
						for (vtkActor actor : actors)
						{
							if (actor.GetVisibility() == 1)
							{
								renderPanel.getRenderer().AddActor(actor);
								renderPanel.Render();
								exporter.SetFilePrefix(fileprefix + "_" + type);
								exporter.Update();
								renderPanel.getRenderer().RemoveActor(actor);
							}
						}
					}
					renderPanel.Delete();
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}