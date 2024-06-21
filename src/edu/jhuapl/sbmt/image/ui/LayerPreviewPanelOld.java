package edu.jhuapl.sbmt.image.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image.controllers.preview.ImageContrastController;
import edu.jhuapl.sbmt.image.controllers.preview.ImageFillValuesController;
import edu.jhuapl.sbmt.image.controllers.preview.ImageMaskController;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.IPerspectiveImageToLayerAndMetadataPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.vtk.VtkImageContrastPipeline;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import vtk.vtkImageData;
import vtk.vtkImageReslice;
import vtk.vtkImageSlice;
import vtk.vtkImageSliceMapper;
import vtk.vtkInteractorStyleImage;
import vtk.vtkTransform;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class LayerPreviewPanelOld<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends ModelInfoWindow implements MouseListener, MouseMotionListener, PropertyChangeListener
{
	public static final double VIEWPOINT_DELTA = 1.0;
	public static final double ROTATION_DELTA = 5.0;

	ImageMaskController maskController;
	ImageContrastController contrastController;
	ImageFillValuesController fillValuesController;
	private List<Layer> layers;
	private Layer layer;
	private vtkJoglPanelComponent renWin;
	private vtkImageSlice actor = new vtkImageSlice();
	private vtkImageReslice reslice;
	private boolean centerFrustumMode = false;
	private vtkImageData displayedImage;
	private List<HashMap<String, String>> metadata;
	private List<List<HashMap<String, String>>> metadatas;
	private Runnable completionBlock;
	private JComboBox<String> layerComboBox;
	private int displayedLayerIndex = 0;
	private JSplitPane splitPane;
	private JPanel layerPanel;
	private JPanel controlsPanel;
	private IntensityRange intensityRange;
	private int[] currentMaskValues;
	private double[] currentFillValues;
	private JCheckBox syncCheckBox;
	private G1 image;

	public LayerPreviewPanelOld(String title, final List<Layer> layers, int currentLayerIndex, IntensityRange intensityRange, int[] currentMaskValues, double[] currentFillValues, List<List<HashMap<String, String>>> metadatas, Runnable completionBlock) throws IOException, Exception
	{
		this.layers = layers;
		this.intensityRange = intensityRange;
		this.currentMaskValues = currentMaskValues;
		this.currentFillValues = currentFillValues;
		this.displayedLayerIndex = currentLayerIndex;
		this.layer = layers.get(currentLayerIndex);
		this.metadatas = metadatas;
		this.metadata = metadatas.get(currentLayerIndex);
		this.completionBlock = completionBlock;
		this.layerPanel = new JPanel();
		this.layerPanel.setLayout(new GridBagLayout());
		this.controlsPanel = new JPanel();
		this.controlsPanel.setLayout(new GridBagLayout());
		controlsPanel.setBorder(BorderFactory.createTitledBorder("Image Appearance"));
		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, layerPanel, controlsPanel);
		initComponents();
		renderLayer(layer);
		setIntensity(intensityRange);
		createMenus();
		setTitle(title);
		maskController.setMaskValues(currentMaskValues);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(splitPane, gridBagConstraints);
		pack();
		setVisible(true);
		setSize(700, 700);
		this.splitPane.setDividerLocation(0.9);

		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				renWin.resetCamera();
				renWin.Render();
			}
		});
	}

	public void setCompletionBlock(Runnable completionBlock)
	{
		this.completionBlock = completionBlock;
	}

	private void setIntensity(IntensityRange range) throws IOException, Exception
	{
		VtkImageContrastPipeline pipeline = new VtkImageContrastPipeline(displayedImage, range);
		displayedImage = pipeline.getUpdatedData().get(0);
 		updateImage(displayedImage);
	}

	private void generateVtkImageData(Layer layer) throws IOException, Exception
	{
		List<vtkImageData> displayedImages = new ArrayList<vtkImageData>();
		IPipelinePublisher<Layer> reader = new Just<Layer>(layer);
		reader.
			operate(new VtkImageRendererOperator()).
			subscribe(new Sink<vtkImageData>(displayedImages)).run();
		displayedImage = displayedImages.get(0);
		contrastController.setImageData(displayedImage);
		if (displayedImage.GetNumberOfScalarComponents() != 1)
			contrastController.getView().setVisible(false);
		this.layer = layer;
	}

	private void renderLayer(Layer layer) throws IOException, Exception
	{
		generateVtkImageData(layer);

		renWin = new vtkJoglPanelComponent();
		renWin.getComponent().setPreferredSize(new Dimension(550, 550));

		vtkInteractorStyleImage style = new vtkInteractorStyleImage();
		renWin.setInteractorStyle(style);

//		renWin.getRenderWindow().GetInteractor().GetInteractorStyle().AddObserver("WindowLevelEvent", this,
//				"levelsChanged");

		updateImage(displayedImage);

		renWin.getRenderer().AddActor(actor);

		renWin.setSize(550, 550);
		renWin.getRenderer().SetBackground(new double[] {0.5f, 0.5f, 0.5f});

		renWin.getComponent().addMouseListener(this);
		renWin.getComponent().addMouseMotionListener(this);
		renWin.getRenderer().GetActiveCamera().Dolly(0.2);
		// renWin.addKeyListener(this);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		layerPanel.add(renWin.getComponent(), gridBagConstraints);
	}

	private void updateImage(vtkImageData displayedImage)
	{

		double[] center = displayedImage.GetCenter();
		int[] dims = displayedImage.GetDimensions();
		// Rotate image by 90 degrees so it appears the same way as when you
		// use the Center in Image option.
		vtkTransform imageTransform = new vtkTransform();
		imageTransform.Translate(center[0], center[1], 0.0);
		imageTransform.RotateZ(0.0);
		imageTransform.Translate(-center[1], -center[0], 0.0);

		reslice = new vtkImageReslice();
		reslice.SetInputData(displayedImage);
//		reslice.SetResliceTransform(imageTransform);
		reslice.SetInterpolationModeToNearestNeighbor();
		reslice.SetOutputSpacing(1.0, 1.0, 1.0);
		reslice.SetOutputOrigin(0.0, 0.0, 0.0);
		reslice.SetOutputExtent(0, dims[0] - 1, 0, dims[1] - 1, 0, dims[2]);
		reslice.Update();

		vtkImageSliceMapper imageSliceMapper = new vtkImageSliceMapper();
		imageSliceMapper.SetInputConnection(reslice.GetOutputPort());
		imageSliceMapper.Update();

		actor.SetMapper(imageSliceMapper);
		actor.GetProperty().SetInterpolationTypeToLinear();
	}

	@Override
	public Model getModel()
	{
		return null;
	}

	@Override
	public Model getCollectionModel()
	{
		return null; //imageCollection;
	}

	@SuppressWarnings("unchecked")
	private void initComponents()
	{
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(500, 500));
		setPreferredSize(new Dimension(775, 500));
		getContentPane().setLayout(new GridBagLayout());

		buildSyncCheckBox(0);
		buildLayerComboBox(1);
		buildContrastController(2);
		buildTrimController(3);
		buildFillValuesController(4);
		pack();
	}

	private void buildSyncCheckBox(int ylevel)
	{
		syncCheckBox = new JCheckBox("Sync with Body");
		syncCheckBox.setSelected(false);
		syncCheckBox.addActionListener(evt -> {
			completionBlock.run();
		});

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = ylevel;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.insets = new Insets(3, 20, 10, 0);
		controlsPanel.add(syncCheckBox, gridBagConstraints);
	}

	private void buildLayerComboBox(int ylevel)
	{
		if (layers.size() > 1)
		{
			String[] layerNames = new String[layers.size()];
			if (metadata.size() != 0)
			{
				List<HashMap<String, String>> list = metadatas.get(0);
				HashMap<String, String> values = list.get(0);
				values
					 .keySet()
					 .stream()
					 .filter(item -> item.contains("PLANE"))
					 .map(key  -> key + " - " + metadatas.get(0).get(0).get(key))
					 .sorted()
					 .toList()
					 .toArray(layerNames);
			}
			else
				for (int i=0; i<5; i++)
				{
					String paddedIndex = StringUtils.leftPad(""+(i+1), 2);
					layerNames[i] = "PLANE" + (i+1);
				}
			layerComboBox = new JComboBox<String>(layerNames);
			int indexToSelect = 0;
			Optional<String> matchedPlane = Arrays.stream(layerNames).filter(name -> name.contains("PLANE" + displayedLayerIndex)).findFirst();
			if (matchedPlane.isPresent())
				indexToSelect = Arrays.stream(layerNames).toList().indexOf(matchedPlane.get()) + 1;
			layerComboBox.setSelectedIndex(indexToSelect);
			layerComboBox.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					String title = (String)layerComboBox.getSelectedItem();
					int index = Integer.parseInt(title.split(" ")[0].replace("PLANE", "")) - 1;
					try
					{
						layer = layers.get(index);
						maskController.setLayer(layer);
						generateVtkImageData(layers.get(index));
						setIntensity(null);
						renWin.Render();
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = ylevel;
			gridBagConstraints.gridwidth = 1;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 0.0;
			controlsPanel.add(layerComboBox, gridBagConstraints);

			JButton applyToBodyButton = new JButton("Apply to Body");
			applyToBodyButton.addActionListener(evt -> {
				String title = (String)layerComboBox.getSelectedItem();
				displayedLayerIndex = Integer.parseInt(title.split(" ")[0].replace("PLANE", "")) - 1;
				if (completionBlock != null && syncCheckBox.isSelected()) completionBlock.run();
			});

			gridBagConstraints.gridx = 1;
			controlsPanel.add(applyToBodyButton, gridBagConstraints);
		}
	}

	private void buildContrastController(int ylevel)
	{
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = ylevel;
		gridBagConstraints.gridwidth = controlsPanel.getWidth();
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		contrastController = new ImageContrastController(displayedImage, intensityRange, new Function<vtkImageData, Void>() {

			@Override
			public Void apply(vtkImageData t)
			{
				try
				{
//					renderLayer();
					displayedImage = t;
					setIntensity(null);
					renWin.Render();
					if (completionBlock != null && syncCheckBox.isSelected()) completionBlock.run();
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		});

		controlsPanel.add(contrastController.getView(), gridBagConstraints);
	}

	private void buildTrimController(int ylevel)
	{
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = ylevel;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(3, 0, 3, 0);
		maskController = new ImageMaskController(layer, currentMaskValues, new Function<Pair<Layer, int[]>, Void>()
		{

			@Override
			public Void apply(Pair<Layer, int[]> items)
			{
				try
				{
//					int[] masks = items.getRight();
//					renderLayer();
					generateVtkImageData(layers.get(displayedLayerIndex));
					updateImage(displayedImage);
					setIntensity(contrastController.getIntensityRange());
					if (renWin == null) return null;
					renWin.Render();
					layer = items.getLeft();
					if (completionBlock != null && syncCheckBox.isSelected()) completionBlock.run();
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
		});
		controlsPanel.add(maskController.getView(), gridBagConstraints);
	}

	private void buildFillValuesController(int ylevel)
	{
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = ylevel;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.insets = new Insets(3, 0, 3, 0);
		fillValuesController = new ImageFillValuesController(new Function<double[], Void>()
		{
			@Override
			public Void apply(double[] items)
			{
				if (completionBlock != null && syncCheckBox.isSelected()) completionBlock.run();
				return null;
			}
		});

		for (double val : currentFillValues)
		{
			fillValuesController.getFillValuesTextField().setText(fillValuesController.getFillValuesTextField().getText() + val + ",");
			fillValuesController.setFillValues(currentFillValues);
		}

		fillValuesController.getFillValuesButton().addActionListener(e -> {
			String[] valueStrings = fillValuesController.getFillValuesTextField().getText().split(",");
			double[] doubleArray = new double[valueStrings.length];
			if (valueStrings.length == 0 || valueStrings[0].isBlank())
			{
				try
				{
					fillValuesController.setFillValues(new double[] {});
					renderLayer();
					generateVtkImageData(layers.get(displayedLayerIndex));
//					updateImage(displayedImage);
					setIntensity(contrastController.getIntensityRange());
					if (renWin == null) return;
					SwingUtilities.invokeLater(() -> { renWin.Render(); });

					return;
				}
				catch (Exception e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			int i=0;
			for (String val : valueStrings)
			{
				doubleArray[i++] = Double.parseDouble(val);
			}
			fillValuesController.setFillValues(doubleArray);
			renderLayer();
		});

		controlsPanel.add(fillValuesController.getView(), gridBagConstraints);
	}

	public IntensityRange getIntensityRange()
	{
		return contrastController.getIntensityRange();
	}

	public int[] getMaskValues()
	{
		return maskController.getMaskValues();
	}

	public double[] getFillValues()
	{
		return fillValuesController.getFillValues();
	}

	public int getDisplayedLayerIndex()
	{
		return displayedLayerIndex;
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (centerFrustumMode && e.getButton() == 1)
		{
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
		}

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
		if (centerFrustumMode && !e.isAltDown())
		{
//			((PerspectiveImage) image).calculateFrustum();
//			((PerspectiveImage) image).firePropertyChange();
		}
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (centerFrustumMode && e.getButton() == 1)
		{
//			if (!e.isAltDown())
//			{
//				centerFrustumOnPixel(e);
//				((PerspectiveImage) image).loadFootprint();
//			}
//
//			((PerspectiveImage) image).firePropertyChange();

		}
//		else
//			updateSpectrumRegion(e);
	}

//	private void interpolateCheckBox1ActionPerformed(ActionEvent evt) throws Exception
//	{
//		currentInterpolateState = interpolateCheckBox1.isSelected();
//		image.setInterpolateState(currentInterpolateState);
//		runEditingPipeline();
//	}

	@Override
	public void mouseMoved(MouseEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	public void propertyChange(PropertyChangeEvent arg0)
	{
		if (renWin.getRenderWindow().GetNeverRendered() > 0)
			return;
		renWin.Render();
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
//				File file = CustomFileChooser.showSaveDialog(renWin.getComponent(), "Export to PNG Image...",
//						"image.png", "png");
//				RenderIoUtil.saveToFile(file, renWin, null);
//			}
//		});
//		fileMenu.add(mi);
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);

		setJMenuBar(menuBar);
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(G1 image)
	{
		this.image = image;
	}

	private void regenerateLayerFromImage()
	{
		try
		{
			IPerspectiveImageToLayerAndMetadataPipeline pipeline = IPerspectiveImageToLayerAndMetadataPipeline.of(image);
			this.layers = pipeline.getLayers();
			this.layer =(Layer) pipeline.getLayers().get(displayedLayerIndex);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void renderLayer()
	{
		if (image == null) return;

		try
		{
			regenerateLayerFromImage();
			if (layer == null) return;
			renderLayer(layer);
			setIntensity(image.getIntensityRange());
		}
		catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
}