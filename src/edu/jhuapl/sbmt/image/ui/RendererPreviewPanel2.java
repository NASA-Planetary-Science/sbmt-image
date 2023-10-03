package edu.jhuapl.sbmt.image.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.tuple.Pair;

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
import edu.jhuapl.sbmt.image.controllers.preview.PlaybackPanelController;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.preview.BodyPositionPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.preview.RenderableImagePipeline;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.publisher.Publishers;

public class RendererPreviewPanel2 extends ModelInfoWindow implements MouseListener, MouseMotionListener, PropertyChangeListener, VtkPropProvider
{
	public static final double VIEWPOINT_DELTA = 1.0;
	public static final double ROTATION_DELTA = 5.0;

//	private vtkJoglPanelComponent renWin;
	private Renderer renderer;
	RenderableImagePipeline renderableImagesPipeline;
	BodyPositionPipeline bodyPositionPipeline;
	PlaybackPanelController playbackController;
	List<vtkActor> inputs;
	List<vtkProp> props = Lists.newArrayList();

//	public RendererPreviewPanel2(SmallBodyModel smallBodyModel, IPipelinePublisher<Pair<List<SmallBodyModel>, List<RenderableImage>>> sceneObjects) throws Exception
	public RendererPreviewPanel2(SmallBodyModel smallBodyModel, RenderableImagePipeline renderableImagesPipeline, BodyPositionPipeline bodyPositionPipeline, double startTime) throws Exception
	{
		this.bodyPositionPipeline = bodyPositionPipeline;
		this.renderableImagesPipeline = renderableImagesPipeline;
		this.playbackController = new PlaybackPanelController(startTime, startTime + 180, new Function<Double, Void>()
		{

			@Override
			public Void apply(Double t)
			{
				try
				{
					bodyPositionPipeline.run(t);
					List<vtkActor> inputs = Lists.newArrayList();
					runBodyPositionUpdate(inputs);
					loadActors(inputs);
					SwingUtilities.invokeLater(new Runnable()
					{

						@Override
						public void run()
						{
							renderer.notifySceneChange();
						}
					});

				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		});
		inputs = Lists.newArrayList();
		this.renderer = new Renderer(smallBodyModel);
		renderer.setLightCfg(LightUtil.getSystemLightCfg());
		renderer.addVtkPropProvider(this);
		initComponents();

		runBodyPositionUpdate(inputs);

		loadActors(inputs);

		createMenus();

		setTitle("Renderer Preview2");

		pack();
		setVisible(true);

//		initialized = true;

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

	private void runBodyPositionUpdate(List<vtkActor> inputs) throws Exception
	{
		//run the pipelines
		renderableImagesPipeline.run();
		List<RenderablePointedImage> renderableImages = renderableImagesPipeline.getOutput();

		bodyPositionPipeline.run();
		List<SmallBodyModel> updatedBodies = bodyPositionPipeline.getOutput();

		//*************************
		//zip the sources together
		//*************************
		IPipelinePublisher<Pair<SmallBodyModel, RenderablePointedImage>> sceneObjects = Publishers.formPair(Just.of(updatedBodies), Just.of(renderableImages));

//		//***************************************************************************
//		//Pass them into the scene builder to perform intersection calculations
//		//***************************************************************************
//		IPipelineOperator<Pair<List<SmallBodyModel>, List<RenderableImage>>, vtkActor> sceneBuilder = new SceneBuilderOperator();
//
//		//*****************************************
//		//Throw them to inputs for the preview tool
//		//*****************************************
//		sceneObjects
//			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
//			.subscribe(Sink.of(inputs))		//subscribe to the scene builder with the preview
//			.run();
	}

//	private void runBodyPositionUpdate(double time, List<vtkActor> inputs) throws Exception
//	{
//		//run the pipelines
//		renderableImagesPipeline.run();
//		List<RenderableImage> renderableImages = renderableImagesPipeline.getOutput();
//
//		bodyPositionPipeline.run(time);
//		List<SmallBodyModel> updatedBodies = bodyPositionPipeline.getOutput();
//
//		//*************************
//		//zip the sources together
//		//*************************
//		IPipelinePublisher<Pair<List<SmallBodyModel>, List<RenderableImage>>> sceneObjects = Publishers.formPair(Just.of(updatedBodies), Just.of(renderableImages));
//
//		//***************************************************************************
//		//Pass them into the scene builder to perform intersection calculations
//		//***************************************************************************
//		IPipelineOperator<Pair<List<SmallBodyModel>, List<RenderableImage>>, vtkActor> sceneBuilder = new SceneBuilderOperator();
//
//		//*******************************
//		//Throw them to the preview tool
//		//*******************************
//		sceneObjects
//			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
//			.subscribe(Sink.of(inputs))		//subscribe to the scene builder with the preview
//			.run();
//	}

	@Override
	public Collection<vtkProp> getProps()
	{
		return props;
	}

	private void loadActors(List<vtkActor> inputs)
	{
//		List<vtkProp> props = Lists.newArrayList();
		props.clear();
		for (vtkActor actor : inputs)
			props.add(actor);
		renderer.notifySceneChange();
//		renderer.addVtkPropProvider(new VtkPropProvider()
//		{
//
//			@Override
//			public Collection<vtkProp> getProps()
//			{
//				return props;
//			}
//		});
	}

	@Override
	public Model getModel()
	{
		return null;
//		return image;
	}

	@Override
	public Model getCollectionModel()
	{
		return null; //imageCollection;
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
//		renWin = new vtkJoglPanelComponent();
//		renWin.getComponent().setPreferredSize(new Dimension(550, 550));
//
//		vtkInteractorStyleImage style = new vtkInteractorStyleImage();
//		renWin.setInteractorStyle(style);
//
////		renWin.getRenderWindow().GetInteractor().GetInteractorStyle().AddObserver("WindowLevelEvent", this,
////				"levelsChanged");
//
////		updateImage(displayedImage);
//
//
////		renWin.getRenderer().AddActor(actor);
//
//		renWin.setSize(550, 550);
////		renWin.getRenderer().SetBackground(new double[] {0.5f, 0.5f, 0.5f});
//
////		imagePicker = new vtkPropPicker();
////		imagePicker.PickFromListOn();
////		imagePicker.InitializePickList();
////		vtkPropCollection smallBodyPickList = imagePicker.GetPickList();
////		smallBodyPickList.RemoveAllItems();
////		imagePicker.AddPickList(actor);
//		renWin.getComponent().addMouseListener(this);
//		renWin.getComponent().addMouseMotionListener(this);
//		renWin.getRenderer().GetActiveCamera().Dolly(0.2);
		// renWin.addKeyListener(this);

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
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
//		gridBagConstraints.weighty = 1.0;
		getContentPane().add(playbackController.getView(), gridBagConstraints);
	}

	private void createMenus()
	{
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
//		JMenuItem mi = new JMenuItem(new AbstractAction("Export to Image...")
//		{
//			@Override
//			public void actionPerformed(ActionEvent e)
//			{
//				File file = CustomFileChooser.showSaveDialog(renderer.getRenderWindowPanel().getComponent(), "Export to PNG Image...",
//						"image.png", "png");
//				RenderIoUtil.saveToFile(file, renderer.getRenderWindowPanel(), null);
//			}
//		});
//		fileMenu.add(mi);
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);

		/**
		 * The following is a bit of a hack. We want to reuse the PopupMenu
		 * class, but instead of having a right-click popup menu, we want
		 * instead to use it as an actual menu in a menu bar. Therefore we
		 * simply grab the menu items from that class and put these in our new
		 * JMenu.
		 */
//		ImagePopupMenu imagesPopupMenu = new ImagePopupMenu(null, imageCollection, imageBoundaryCollection, null, null,
//				null, this);
//
//		imagesPopupMenu.setCurrentImage(image.getKey());
//
//		JMenu menu = new JMenu("Options");
//		menu.setMnemonic('O');
//
//		Component[] components = imagesPopupMenu.getComponents();
//		for (Component item : components)
//		{
//			if (item instanceof JMenuItem)
//			{
//				// Do not show the "Show Image" option since that creates
//				// problems
//				// since it's supposed to close this window also.
//				if (!(((JMenuItem) item).getAction() instanceof ImagePopupMenu.MapImageAction))
//					menu.add(item);
//			}
//		}
//
//		menuBar.add(menu);

		setJMenuBar(menuBar);
	}

	@Override
	public void mouseMoved(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	public void propertyChange(PropertyChangeEvent arg0)
	{
		if (renderer.getRenderWindowPanel().getRenderWindow().GetNeverRendered() > 0)
			return;
		renderer.getRenderWindowPanel().Render();
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
//		if (centerFrustumMode && e.getButton() == 1)
//		{
//			if (e.isAltDown())
//			{
//				// System.out.println("Resetting pointing...");
//				// ((PerspectiveImage)image).resetSpacecraftState();
//			}
//			else
//			{
//				centerFrustumOnPixel(e);
//
//				((PerspectiveImage) image).loadFootprint();
//				// ((PerspectiveImage)image).calculateFrustum();
//			}
//			// PerspectiveImageBoundary boundary =
//			// imageBoundaryCollection.getBoundary(image.getKey());
//			// boundary.update();
//			// ((PerspectiveImageBoundary)boundary).firePropertyChange();
//
//			((PerspectiveImage) image).firePropertyChange();
//		}

//		int pickSucceeded = doPick(e, imagePicker, renWin);
//		if (pickSucceeded == 1)
		{
//			double[] p = imagePicker.GetPickPosition();
//
//			// Display selected pixel coordinates in console output
//			// Note we reverse x and y so that the pixel is in the form the
//			// camera
//			// position/orientation program expects.
//			System.out.println(p[1] + " " + p[0]);
//
//			// Display status bar message upon being picked
//			refStatusHandler.setLeftTextSource(image, null, 0, p);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
//		if (centerFrustumMode && !e.isAltDown())
//		{
////			((PerspectiveImage) image).calculateFrustum();
////			((PerspectiveImage) image).firePropertyChange();
//		}
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
//		if (centerFrustumMode && e.getButton() == 1)
//		{
//			if (!e.isAltDown())
//			{
//				centerFrustumOnPixel(e);
//				((PerspectiveImage) image).loadFootprint();
//			}
//
//			((PerspectiveImage) image).firePropertyChange();
//
//		}
//		else
//			updateSpectrumRegion(e);
	}

}