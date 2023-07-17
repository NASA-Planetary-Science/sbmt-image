package edu.jhuapl.sbmt.image.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkProp;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.RenderIoUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.view.light.LightUtil;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;

public class RendererPreviewPanel extends ModelInfoWindow
{
	public static final double VIEWPOINT_DELTA = 1.0;
	public static final double ROTATION_DELTA = 5.0;
	private Renderer renderer;

	public RendererPreviewPanel(SmallBodyModel smallBodyModel, List<vtkActor> inputs)
	{
		this.renderer = new Renderer(smallBodyModel);
		renderer.setLightCfg(LightUtil.getSystemLightCfg());
		initComponents();
		loadActors(inputs, smallBodyModel);

		createMenus();

		setTitle("Renderer Preview");

		pack();
//		setVisible(true);
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				renderer.getRenderWindowPanel().resetCamera();
				renderer.getRenderWindowPanel().Render();
			}
		});
	}

	private void loadActors(List<vtkActor> inputs, SmallBodyModel smallBodyModel)
	{
		List<vtkProp> props = Lists.newArrayList();
		for (vtkActor actor : inputs)
			props.add(actor);
//		props.add(smallBodyModel.getSmallBodyActor());
		renderer.addVtkPropProvider(new VtkPropProvider()
		{

			@Override
			public Collection<vtkProp> getProps()
			{
				return props;
			}
		});
	}

	@Override
	public Model getModel()
	{
		return null;
	}

	@Override
	public Model getCollectionModel()
	{
		return null;
	}

	private void initComponents()
	{
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(500, 500));
		setPreferredSize(new Dimension(775, 900));
		getContentPane().setLayout(new GridBagLayout());

		initRenderer();
		pack();
	}

	private void initRenderer()
	{
		// Trying to add a vtksbmtJoglCanvasComponent in the netbeans gui
		// does not seem to work so instead add it here.
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(renderer, gridBagConstraints);
	}

	private void createMenus()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		JMenuItem mi = new JMenuItem(new AbstractAction("Export to Image...")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				File file = CustomFileChooser.showSaveDialog(renderer.getRenderWindowPanel().getComponent(), "Export to PNG Image...",
						"image.png", "png");
				RenderIoUtil.saveToFile(file, renderer.getRenderWindowPanel(), null);
			}
		});
		fileMenu.add(mi);
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);

		setJMenuBar(menuBar);
	}

	public void propertyChange(PropertyChangeEvent arg0)
	{
		if (renderer.getRenderWindowPanel().getRenderWindow().GetNeverRendered() > 0)
			return;
		renderer.getRenderWindowPanel().Render();
	}
}