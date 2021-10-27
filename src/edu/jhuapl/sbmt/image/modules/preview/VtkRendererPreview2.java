package edu.jhuapl.sbmt.image.modules.preview;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

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
import edu.jhuapl.sbmt.client.SmallBodyModel;
import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.modules.io.builtIn.BuiltInFitsHeaderReader;
import edu.jhuapl.sbmt.image.modules.io.builtIn.BuiltInFitsReader;
import edu.jhuapl.sbmt.image.modules.io.builtIn.BuiltInOBJReader;
import edu.jhuapl.sbmt.image.modules.pointing.InfofileReaderPublisher;
import edu.jhuapl.sbmt.image.modules.pointing.SpiceBodyOperator;
import edu.jhuapl.sbmt.image.modules.pointing.SpiceReaderPublisher;
import edu.jhuapl.sbmt.image.modules.preview.VtkRendererPreview2.BodyPositionPipeline;
import edu.jhuapl.sbmt.image.modules.preview.VtkRendererPreview2.RenderableImagesPipeline;
import edu.jhuapl.sbmt.image.modules.rendering.LayerRotationOperator;
import edu.jhuapl.sbmt.image.modules.rendering.RenderableImage;
import edu.jhuapl.sbmt.image.modules.rendering.RenderableImageGenerator;
import edu.jhuapl.sbmt.image.modules.rendering.SceneBuilderOperator;
import edu.jhuapl.sbmt.image.pipeline.IPipeline;
import edu.jhuapl.sbmt.image.pipeline.operator.IPipelineOperator;
import edu.jhuapl.sbmt.image.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.image.pipeline.publisher.Just;
import edu.jhuapl.sbmt.image.pipeline.publisher.Publishers;
import edu.jhuapl.sbmt.image.pipeline.subscriber.BasePipelineSubscriber;
import edu.jhuapl.sbmt.image.pipeline.subscriber.Sink;
import edu.jhuapl.sbmt.model.image.InfoFileReader;
import edu.jhuapl.sbmt.pointing.spice.SpiceInfo;
import edu.jhuapl.sbmt.pointing.spice.SpicePointingProvider;
import edu.jhuapl.sbmt.util.TimeUtil;

public class VtkRendererPreview2 extends BasePipelineSubscriber<vtkActor>
{
	private IPipelinePublisher<vtkActor> publisher;
	private SmallBodyModel smallBodyModel;
	IPipelinePublisher<Pair<List<SmallBodyModel>, List<RenderableImage>>> sceneObjects;
	RenderableImagesPipeline renderableImagesPipeline;
	BodyPositionPipeline bodyPositionPipeline;
	double startTime;

	class RenderableImagesPipeline implements IPipeline<RenderableImage>
	{
		List<RenderableImage> renderableImages = Lists.newArrayList();
		IPipelinePublisher<Triple<Layer, HashMap<String, String>, InfoFileReader>> imageComponents;
		IPipelineOperator<Triple<Layer, HashMap<String, String>, InfoFileReader>, RenderableImage> renderableImageGenerator;

		public RenderableImagesPipeline(String[] imageFiles, String[] pointingFiles) throws Exception
		{
			IPipelinePublisher<Layer> reader = new BuiltInFitsReader("/Users/steelrj1/Desktop/dart_717891977_782_01.fits", new double[] {-32768.0, -32767.0, 4095.0});
			LayerRotationOperator rotationOperator = new LayerRotationOperator();

			List<Layer> updatedLayers = Lists.newArrayList();
			reader
				.operate(rotationOperator)
				.subscribe(Sink.of(updatedLayers))
				.run();

			//generate image pointing (in: filename, out: ImagePointing)
			IPipelinePublisher<InfoFileReader> pointingPublisher = new InfofileReaderPublisher("/Users/steelrj1/Desktop/dart_717891977_782_01.INFO");

			//generate metadata (in: filename, out: ImageMetadata)
			IPipelinePublisher<HashMap<String, String>> metadataReader = new BuiltInFitsHeaderReader("/Users/steelrj1/Desktop/dart_717891977_782_01.fits");

			//combine image source (in: Layer+ImageMetadata+ImagePointing, out: RenderableImage)
			IPipelinePublisher<Layer> layerPublisher = new Just<Layer>(updatedLayers.get(0));
			imageComponents = Publishers.formTriple(layerPublisher, metadataReader, pointingPublisher);
			renderableImageGenerator = new RenderableImageGenerator();
		}

		public void run() throws Exception
		{
			//***************************************************************************************
			//generate image polydata with texture coords (in: RenderableImage, out: vtkPolydata)
			//***************************************************************************************
			renderableImages.clear();
			imageComponents
				.operate(renderableImageGenerator)
				.subscribe(Sink.of(renderableImages))
				.run();
		}

		public List<RenderableImage> getOutput()
		{
			return renderableImages;
		}
	}

	class BodyPositionPipeline implements IPipeline<SmallBodyModel>
	{
		List<SmallBodyModel> updatedBodies = Lists.newArrayList();
		IPipelinePublisher<Pair<SmallBodyModel, SpicePointingProvider>> spiceBodyObjects;
		IPipelineOperator<Pair<SmallBodyModel, SpicePointingProvider>, SmallBodyModel> spiceBodyOperator;
		IPipelinePublisher<SmallBodyModel> vtkReader;
		IPipelinePublisher<SpicePointingProvider> pointingProviders;
		String[] bodyFiles;
		String[] bodyNames;
		SpiceInfo[] spiceInfos;
		String mkPath;
		String centerBodyName;
		String initialTime;
		String instFrame;
		SpiceInfo activeSpiceInfo;

		public BodyPositionPipeline(String[] bodyFiles, String[] bodyNames, SpiceInfo[] spiceInfos, String mkPath, String centerBodyName, String initialTime, String instFrame) throws Exception
		{
			this.bodyFiles = bodyFiles;
			this.bodyNames = bodyNames;
			this.spiceInfos = spiceInfos;
			this.mkPath = mkPath;
			this.centerBodyName = centerBodyName;
			this.initialTime = initialTime;
			this.instFrame = instFrame;
//			//***********************
//			//generate body polydata
//			//***********************
			vtkReader = new BuiltInOBJReader(bodyFiles, bodyNames);
//			System.out.println("VtkRendererPreview2.BodyPositionPipeline: BodyPositionPipeline: num vtk " + vtkReader.getOutputs().size());
//			//*********************************
//			//Use SPICE to position the bodies
//			//*********************************
			activeSpiceInfo = Arrays.asList(spiceInfos).stream().filter(info -> info.getBodyName().equals(centerBodyName)).collect(Collectors.toList()).get(0);
			pointingProviders = new SpiceReaderPublisher(mkPath, activeSpiceInfo, instFrame);
			spiceBodyObjects = Publishers.formPair(vtkReader, pointingProviders);

			spiceBodyOperator = new SpiceBodyOperator(centerBodyName, TimeUtil.str2et(initialTime));

		}

		private IPipelinePublisher<SmallBodyModel> of()
		{
			//***********************
			//generate body polydata
			//***********************
			vtkReader = new BuiltInOBJReader(bodyFiles, bodyNames);
			//*********************************
			//Use SPICE to position the bodies
			//*********************************
			pointingProviders = new SpiceReaderPublisher(mkPath, activeSpiceInfo, instFrame);
			spiceBodyObjects = Publishers.formPair(vtkReader, pointingProviders);

			return spiceBodyObjects
				.operate(spiceBodyOperator)
				.subscribe(Sink.of(updatedBodies));
		}

		private IPipelinePublisher<SmallBodyModel> of(double time)
		{
			//***********************
			//generate body polydata
			//***********************
			vtkReader = new BuiltInOBJReader(bodyFiles, bodyNames);
			//*********************************
			//Use SPICE to position the bodies
			//*********************************
			pointingProviders = new SpiceReaderPublisher(mkPath, activeSpiceInfo, instFrame);
			spiceBodyObjects = Publishers.formPair(vtkReader, pointingProviders);
			spiceBodyOperator = new SpiceBodyOperator(centerBodyName, time);
			return spiceBodyObjects
				.operate(spiceBodyOperator)
				.subscribe(Sink.of(updatedBodies));
		}

		public void run() throws Exception
		{
			updatedBodies.clear();
			of().run();
		}

		public void run(double time) throws Exception
		{
			((SpiceBodyOperator)spiceBodyOperator).setTime(time);
			updatedBodies.clear();
//			System.out.println("VtkRendererPreview2.BodyPositionPipeline: run: updated time is " + TimeUtil.et2str(time));
			of(time).run();
//			spiceBodyObjects
//				.operate(spiceBodyOperator)
//				.subscribe(Sink.of(updatedBodies))
//				.run();
		}

		public List<SmallBodyModel> getOutput()
		{
			return updatedBodies;
		}
	}

	public VtkRendererPreview2(String[] imageFiles, String[] pointingFiles, String[] bodyFiles, String[] bodyNames, SpiceInfo[] spiceInfos, String mkPath, String centerBodyName, String initialTime, String instFrame) throws Exception
	{
		this.startTime = TimeUtil.str2et(initialTime);

		renderableImagesPipeline = new RenderableImagesPipeline(imageFiles, pointingFiles);
//		renderableImagesPipeline.run();
//		List<RenderableImage> renderableImages = renderableImagesPipeline.getOutput();

		bodyPositionPipeline = new BodyPositionPipeline(bodyFiles, bodyNames, spiceInfos, mkPath, centerBodyName, initialTime, instFrame);
		bodyPositionPipeline.run();
		List<SmallBodyModel> updatedBodies = bodyPositionPipeline.getOutput();

		RendererPreviewPanel2 preview = new RendererPreviewPanel2(updatedBodies.get(0), renderableImagesPipeline, bodyPositionPipeline, startTime);
	}

	@Override
	public void receive(List<vtkActor> items)
	{
		try
		{
			RendererPreviewPanel2 preview = new RendererPreviewPanel2(smallBodyModel, renderableImagesPipeline, bodyPositionPipeline, startTime);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class RendererPreviewPanel2 extends ModelInfoWindow implements MouseListener, MouseMotionListener, PropertyChangeListener, VtkPropProvider
{
	public static final double VIEWPOINT_DELTA = 1.0;
	public static final double ROTATION_DELTA = 5.0;

//	private vtkJoglPanelComponent renWin;
	private Renderer renderer;
	RenderableImagesPipeline renderableImagesPipeline;
	BodyPositionPipeline bodyPositionPipeline;
	PlaybackPanelController playbackController;
	List<vtkActor> inputs;
	List<vtkProp> props = Lists.newArrayList();

//	public RendererPreviewPanel2(SmallBodyModel smallBodyModel, IPipelinePublisher<Pair<List<SmallBodyModel>, List<RenderableImage>>> sceneObjects) throws Exception
	public RendererPreviewPanel2(SmallBodyModel smallBodyModel, RenderableImagesPipeline renderableImagesPipeline, BodyPositionPipeline bodyPositionPipeline, double startTime) throws Exception
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

		setTitle("Renderer Preview");

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
		List<RenderableImage> renderableImages = renderableImagesPipeline.getOutput();

		bodyPositionPipeline.run();
		List<SmallBodyModel> updatedBodies = bodyPositionPipeline.getOutput();

		//*************************
		//zip the sources together
		//*************************
		IPipelinePublisher<Pair<List<SmallBodyModel>, List<RenderableImage>>> sceneObjects = Publishers.formPair(Just.of(updatedBodies), Just.of(renderableImages));

		//***************************************************************************
		//Pass them into the scene builder to perform intersection calculations
		//***************************************************************************
		IPipelineOperator<Pair<List<SmallBodyModel>, List<RenderableImage>>, vtkActor> sceneBuilder = new SceneBuilderOperator();

		//*****************************************
		//Throw them to inputs for the preview tool
		//*****************************************
		sceneObjects
			.operate(sceneBuilder) 	//feed the zipped sources to scene builder operator
			.subscribe(Sink.of(inputs))		//subscribe to the scene builder with the preview
			.run();
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