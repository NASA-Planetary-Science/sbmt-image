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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import vtk.vtkImageData;
import vtk.vtkImageReslice;
import vtk.vtkImageSlice;
import vtk.vtkImageSliceMapper;
import vtk.vtkInteractorStyleImage;
import vtk.vtkPropPicker;
import vtk.vtkTransform;
import vtk.rendering.jogl.vtkJoglPanelComponent;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.RenderIoUtil;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image.controllers.preview.ImagePropertiesController;
import edu.jhuapl.sbmt.image.model.ImageProperty;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.vtk.VtkImageContrastPipeline;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class LayerRendererPanel extends ModelInfoWindow implements MouseListener, MouseMotionListener, PropertyChangeListener
{
	public static final double VIEWPOINT_DELTA = 1.0;
	public static final double ROTATION_DELTA = 5.0;

//	VtkImageMaskingPipeline maskPipeline;
//	ImageTrimController trimController;
//	ImageMaskController maskController;
//	ImageContrastController contrastController;
	private Layer layer;
	private vtkJoglPanelComponent renWin;
	private vtkImageSlice actor = new vtkImageSlice();
	private vtkImageReslice reslice;
	private vtkPropPicker imagePicker;
	private boolean initialized = false;
	private boolean centerFrustumMode = false;

	private JScrollPane jScrollPane1;
	private JPanel tablePanel;
	private int[] previousLevels = null;
	vtkImageData displayedImage;
	private boolean invertY = false;

	public LayerRendererPanel(final Layer layer, boolean invertY) throws IOException, Exception
	{
		this.invertY = invertY;
//		this.maskPipeline = new VtkImageMaskingPipeline();
		this.layer = layer;
		initComponents();
		renderLayer(layer);

		setIntensity(null);

		createMenus();

		setTitle("Image Properties");

		pack();

		initialized = true;

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

	private void setIntensity(IntensityRange range) throws IOException, Exception
	{
		VtkImageContrastPipeline pipeline = new VtkImageContrastPipeline(displayedImage, null);
		displayedImage = pipeline.getUpdatedData().get(0);
		updateImage(displayedImage);
	}

	private void generateVtkImageData(Layer layer) throws IOException, Exception
	{
		List<vtkImageData> displayedImages = new ArrayList<vtkImageData>();
		IPipelinePublisher<Layer> reader = new Just<Layer>(layer);
		reader.
			operate(new VtkImageRendererOperator(invertY)).
			subscribe(new Sink<vtkImageData>(displayedImages)).run();
		displayedImage = displayedImages.get(0);
//		contrastController.setImageData(displayedImage);
	}

	private void renderLayer(Layer layer) throws IOException, Exception
	{
		generateVtkImageData(layer);

		renWin = new vtkJoglPanelComponent();
		renWin.getComponent().setPreferredSize(new Dimension(200, 200));

		vtkInteractorStyleImage style = new vtkInteractorStyleImage();
		renWin.setInteractorStyle(style);

		renWin.getRenderWindow().GetInteractor().GetInteractorStyle().AddObserver("WindowLevelEvent", this,
				"levelsChanged");

		updateImage(displayedImage);

		renWin.getRenderer().AddActor(actor);

		renWin.setSize(550, 550);
		renWin.getRenderer().SetBackground(new double[] {0.5f, 0.5f, 0.5f});

		renWin.getComponent().addMouseListener(this);
		renWin.getComponent().addMouseMotionListener(this);
		renWin.getRenderer().GetActiveCamera().Dolly(0.2);
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
		getContentPane().add(renWin.getComponent(), gridBagConstraints);
	}

	private void updateImage(vtkImageData displayedImage)
	{
		double[] center = displayedImage.GetCenter();
		int[] dims = displayedImage.GetDimensions();
		// Rotate image by 90 degrees so it appears the same way as when you
		// use the Center in Image option.
		vtkTransform imageTransform = new vtkTransform();
		imageTransform.Translate(center[0], center[1], 0.0);
		imageTransform.RotateZ(-90.0);
		imageTransform.Translate(-center[1], -center[0], 0.0);

		reslice = new vtkImageReslice();
		reslice.SetInputData(displayedImage);
		reslice.SetResliceTransform(imageTransform);
		reslice.SetInterpolationModeToNearestNeighbor();
		reslice.SetOutputSpacing(1.0, 1.0, 1.0);
		reslice.SetOutputOrigin(0.0, 0.0, 0.0);
		reslice.SetOutputExtent(0, dims[1] - 1, 0, dims[0] - 1, 0, 0);
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
//		return image;
	}

	@Override
	public Model getCollectionModel()
	{
		return null; //imageCollection;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents()
	{
		GridBagConstraints gridBagConstraints;
//		LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
		List<ImageProperty> properties = new ArrayList<ImageProperty>();
		properties.add(new ImageProperty("Prop1", "Val1"));
		ImagePropertiesController propertiesController = new ImagePropertiesController(properties);
		tablePanel = propertiesController.getView();

		jScrollPane1 = new JScrollPane();

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(500, 500));
		setPreferredSize(new Dimension(775, 900));
		getContentPane().setLayout(new GridBagLayout());

//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 6;
//		gridBagConstraints.gridwidth = 2;
//		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//		gridBagConstraints.anchor = GridBagConstraints.WEST;
//		gridBagConstraints.weightx = 1.0;
//		getContentPane().add(tablePanel, gridBagConstraints);
//
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 1;
//		gridBagConstraints.gridwidth = getContentPane().getWidth();
//		gridBagConstraints.weightx = 1;
//		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//		contrastController = new ImageContrastController(displayedImage, new IntensityRange(0, 255), new Function<vtkImageData, Void>() {
//
//			@Override
//			public Void apply(vtkImageData t)
//			{
//				try
//				{
//					displayedImage = t;
//					updateImage(displayedImage);
//					setIntensity(null);
//					renWin.Render();
//				}
//				catch (Exception e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				return null;
//			}
//		});
//		getContentPane().add(contrastController.getView(), gridBagConstraints);



//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 2;
//		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//		gridBagConstraints.insets = new Insets(3, 6, 3, 0);
//		trimController = new ImageTrimController(layer, new Function<Layer, Void>()
//		{
//
//			@Override
//			public Void apply(Layer t)
//			{
//				try
//				{
//					generateVtkImageData(t);
//					updateImage(displayedImage);
//					setIntensity(null);
//					renWin.Render();
//					layer = t;
////					maskController.setLayer(t);
////					trimController.setLayer(t);
//				}
//				catch (Exception e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				return null;
//			}
//		});
//
//		getContentPane().add(trimController.getView(), gridBagConstraints);

//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 3;
//		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//		gridBagConstraints.insets = new Insets(3, 6, 3, 0);
//		maskController = new ImageMaskController(layer, new Function<Layer, Void>()
//		{
//
//			@Override
//			public Void apply(Layer t)
//			{
//				try
//				{
//					generateVtkImageData(t);
//					updateImage(displayedImage);
//					setIntensity(null);
//					renWin.Render();
//					layer = t;
//					trimController.setLayer(t);
//					maskController.setLayer(t);
//				}
//				catch (Exception e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				return null;
//			}
//		});
//
//		getContentPane().add(maskController.getView(), gridBagConstraints);

//		pointingPanel.setLayout(new GridBagLayout());
//
//		leftButton.setText("<");
//		leftButton.setToolTipText("left");
//		leftButton.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent evt)
//			{
//				leftButtonActionPerformed(evt);
//			}
//		});
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 1;
//		gridBagConstraints.anchor = GridBagConstraints.WEST;
//		gridBagConstraints.weightx = 1.0;
//		pointingPanel.add(leftButton, gridBagConstraints);
//
//		rightButton.setText(">");
//		rightButton.setToolTipText("right");
//		rightButton.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent evt)
//			{
//				rightButtonActionPerformed(evt);
//			}
//		});
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.weightx = 1.0;
//		gridBagConstraints.gridx = 1;
//		gridBagConstraints.gridy = 1;
//		pointingPanel.add(rightButton, gridBagConstraints);
//
//		upButton.setText("^");
//		upButton.setToolTipText("up");
//		upButton.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent evt)
//			{
//				upButtonActionPerformed(evt);
//			}
//		});
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 2;
//		gridBagConstraints.gridy = 1;
//		gridBagConstraints.weightx = 1.0;
//		pointingPanel.add(upButton, gridBagConstraints);
//
//		downButton.setText("v");
//		downButton.setToolTipText("down");
//		downButton.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent evt)
//			{
//				downButtonActionPerformed(evt);
//			}
//		});
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 3;
//		gridBagConstraints.gridy = 1;
//		gridBagConstraints.weightx = 1.0;
//		pointingPanel.add(downButton, gridBagConstraints);
//
//		rotateLeftButton.setText("\\");
//		rotateLeftButton.setToolTipText("rotate left");
//		rotateLeftButton.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent evt)
//			{
//				rotateLeftButtonActionPerformed(evt);
//			}
//		});
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 6;
//		gridBagConstraints.gridy = 1;
//		gridBagConstraints.weightx = 1.0;
//		pointingPanel.add(rotateLeftButton, gridBagConstraints);
//
//		zoomOutButton.setText("-><-");
//		zoomOutButton.setToolTipText("zoom out");
//		zoomOutButton.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent evt)
//			{
//				zoomOutButtonActionPerformed(evt);
//			}
//		});
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 4;
//		gridBagConstraints.gridy = 1;
//		gridBagConstraints.weightx = 1.0;
//		pointingPanel.add(zoomOutButton, gridBagConstraints);
//
//		zoomInButton.setText("<-->");
//		zoomInButton.setToolTipText("zoom in");
//		zoomInButton.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent evt)
//			{
//				zoomInButtonActionPerformed(evt);
//			}
//		});
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.weightx = 1.0;
//		gridBagConstraints.gridx = 5;
//		gridBagConstraints.gridy = 1;
//		pointingPanel.add(zoomInButton, gridBagConstraints);
//
//		rotateRightButton.setText("/");
//		rotateRightButton.setToolTipText("rotate right");
//		rotateRightButton.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent evt)
//			{
//				rotateRightButtonActionPerformed(evt);
//			}
//		});
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 7;
//		gridBagConstraints.gridy = 1;
//		gridBagConstraints.weightx = 1.0;
//		pointingPanel.add(rotateRightButton, gridBagConstraints);
//
//		interpolateCheckBox1.setSelected(true);
//		interpolateCheckBox1.setText("Interpolate Pixels");
//		interpolateCheckBox1.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent evt)
//			{
//				interpolateCheckBox1ActionPerformed(evt);
//			}
//		});
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.ipadx = 15;
//		gridBagConstraints.weightx = 1.0;
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.gridwidth = 2;
//		pointingPanel.add(interpolateCheckBox1, gridBagConstraints);
//
//		resetFrameAdjustmentsButton.setText("Reset Pointing");
//		resetFrameAdjustmentsButton.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent evt)
//			{
//				resetFrameAdjustmentsButtonActionPerformed(evt);
//			}
//		});
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 6;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.gridwidth = 2;
//		gridBagConstraints.weightx = 1.0;
//		pointingPanel.add(resetFrameAdjustmentsButton, gridBagConstraints);
//
//		adjustFrameCheckBox3.setText("Select Target");
//		adjustFrameCheckBox3.setName(""); // NOI18N
//		adjustFrameCheckBox3.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent evt)
//			{
//				adjustFrameCheckBox3ActionPerformed(evt);
//			}
//		});
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 2;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.gridwidth = 2;
//		gridBagConstraints.ipadx = 15;
//		gridBagConstraints.weightx = 1.0;
//		pointingPanel.add(adjustFrameCheckBox3, gridBagConstraints);
//
//		factorLabel1.setText("Factor");
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 5;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.ipadx = 15;
//		gridBagConstraints.weightx = 1.0;
//		pointingPanel.add(factorLabel1, gridBagConstraints);
//
//		// factorTextField1.setColumns(5);
//		factorTextField1.setText("1.0");
//		factorTextField1.setPreferredSize(new Dimension(14, 28));
//		factorTextField1.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent evt)
//			{
//				factorTextField1ActionPerformed(evt);
//			}
//		});
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 4;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.ipadx = 50;
//		gridBagConstraints.weightx = 1.0;
//		pointingPanel.add(factorTextField1, gridBagConstraints);
//
//		TitledBorder pointingBorder = BorderFactory.createTitledBorder("Pointing Adjustments");
//		pointingBorder.setTitleJustification(TitledBorder.CENTER);
//		pointingPanel.setBorder(pointingBorder);
//
//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 4;
//		gridBagConstraints.gridwidth = 2;
//		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
//		gridBagConstraints.weightx = 1.0;
//
//		getContentPane().add(pointingPanel, gridBagConstraints);

		// applyAdjustmentsButton1.setSelected(true);
		// applyAdjustmentsButton1.setText("Apply Adjustments");
		// applyAdjustmentsButton1.setName(""); // NOI18N
		// applyAdjustmentsButton1.addActionListener(new event.ActionListener()
		// {
		// public void actionPerformed(event.ActionEvent evt) {
		// applyAdjustmentsButton1ActionPerformed(evt);
		// }
		// });
		// gridBagConstraints = new GridBagConstraints();
		// gridBagConstraints.gridx = 2;
		// gridBagConstraints.gridy = 0;
		// gridBagConstraints.ipadx = 15;
		// gridBagConstraints.weightx = 1.0;
		// pointingPanel.add(applyAdjustmentsButton1, gridBagConstraints);

//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 1;
//		gridBagConstraints.gridy = 3;
//		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
//		gridBagConstraints.weightx = 1.0;
//		gridBagConstraints.insets = new Insets(0, 2, 0, 0);
//		getContentPane().add(jPanel3, gridBagConstraints);
//


//		// set up panel for offlimb settings
//		if (image instanceof PerspectiveImage)
//		{
//			gridBagConstraints = new GridBagConstraints();
//			gridBagConstraints.gridx = 0;
//			gridBagConstraints.gridy = 5;
//			gridBagConstraints.gridwidth = 2;
//			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//			gridBagConstraints.anchor = GridBagConstraints.LINE_START;
//			gridBagConstraints.weightx = 1.0;
//
//			TitledBorder titledBorder = BorderFactory.createTitledBorder("Offlimb Settings");
//			titledBorder.setTitleJustification(TitledBorder.CENTER);
//			offLimbPanel.setBorder(titledBorder);
//			offLimbPanel.add(offlimbController.getControlsPanel());
//			getContentPane().add(offLimbPanel, gridBagConstraints);
//		}

		pack();
	}// </editor-fold>//GEN-END:initComponents



//	private void zoomInButtonActionPerformed(ActionEvent evt)
//	{// GEN-FIRST:event_zoomInButtonActionPerformed
//		// System.out.println("Zoom In");
//		if (image instanceof PerspectiveImage)
//		{
//			((PerspectiveImage) image).moveZoomFactorBy(Math.pow(1.1, -getAdjustFactor()));
//			((PerspectiveImage) image).firePropertyChange();
//		}
//	}// GEN-LAST:event_zoomInButtonActionPerformed
//
//	private void leftButtonActionPerformed(ActionEvent evt)
//	{// GEN-FIRST:event_leftButtonActionPerformed
//		if (image instanceof PerspectiveImage)
//		{
//			double[] delta =
//			{ getAdjustFactor(), 0.0 };
//			// ((PerspectiveImage)image).moveTargetPixelCoordinates(delta);
//			// ((PerspectiveImage)image).moveYawAngleBy(getAdjustFactor());
//			((PerspectiveImage) image).moveLineOffsetBy(-getAdjustFactor() / 1000.0);
//			((PerspectiveImage) image).firePropertyChange();
//		}
//	}// GEN-LAST:event_leftButtonActionPerformed
//
//	private void rightButtonActionPerformed(ActionEvent evt)
//	{// GEN-FIRST:event_rightButtonActionPerformed
//		if (image instanceof PerspectiveImage)
//		{
//			double[] delta =
//			{ -getAdjustFactor(), 0.0 };
//			// ((PerspectiveImage)image).moveTargetPixelCoordinates(delta);
//			// ((PerspectiveImage)image).moveYawAngleBy(-getAdjustFactor());
//			((PerspectiveImage) image).moveLineOffsetBy(getAdjustFactor() / 1000.0);
//			((PerspectiveImage) image).firePropertyChange();
//		}
//	}// GEN-LAST:event_rightButtonActionPerformed
//
//	private void upButtonActionPerformed(ActionEvent evt)
//	{// GEN-FIRST:event_upButtonActionPerformed
//		if (image instanceof PerspectiveImage)
//		{
//			double[] delta =
//			{ 0.0, -getAdjustFactor() };
//			// ((PerspectiveImage)image).moveTargetPixelCoordinates(delta);
//			// ((PerspectiveImage)image).movePitchAngleBy(getAdjustFactor());
//			((PerspectiveImage) image).moveSampleOffsetBy(-getAdjustFactor() / 1000.0);
//			((PerspectiveImage) image).firePropertyChange();
//		}
//	}// GEN-LAST:event_upButtonActionPerformed
//
//	private void downButtonActionPerformed(ActionEvent evt)
//	{// GEN-FIRST:event_downButtonActionPerformed
//		if (image instanceof PerspectiveImage)
//		{
//			double[] delta =
//			{ 0.0, getAdjustFactor() };
//			// ((PerspectiveImage)image).moveTargetPixelCoordinates(delta);
//			// ((PerspectiveImage)image).movePitchAngleBy(-getAdjustFactor());
//			((PerspectiveImage) image).moveSampleOffsetBy(getAdjustFactor() / 1000.0);
//			((PerspectiveImage) image).firePropertyChange();
//		}
//	}// GEN-LAST:event_downButtonActionPerformed
//
//	private void rotateLeftButtonActionPerformed(ActionEvent evt)
//	{// GEN-FIRST:event_rotateLeftButtonActionPerformed
//		// System.out.println("Rotate Left");
//		if (image instanceof PerspectiveImage)
//		{
//			((PerspectiveImage) image).moveRotationAngleBy(-getAdjustFactor());
//			((PerspectiveImage) image).firePropertyChange();
//		}
//	}// GEN-LAST:event_rotateLeftButtonActionPerformed
//
//	private void rotateRightButtonActionPerformed(ActionEvent evt)
//	{// GEN-FIRST:event_rotateRightButtonActionPerformed
//		// System.out.println("Rotate Right");
//		if (image instanceof PerspectiveImage)
//		{
//			((PerspectiveImage) image).moveRotationAngleBy(getAdjustFactor());
//			((PerspectiveImage) image).firePropertyChange();
//		}
//	}// GEN-LAST:event_rotateRightButtonActionPerformed
//
//	private void interpolateCheckBox1ActionPerformed(ActionEvent evt)
//	{// GEN-FIRST:event_interpolateCheckBox1ActionPerformed
//		if (image instanceof PerspectiveImage)
//		{
//			boolean interpolate = interpolateCheckBox1.isSelected();
//			((PerspectiveImage) image).setInterpolate(interpolate);
//			if (interpolate)
//				actor.GetProperty().SetInterpolationTypeToLinear();
//			else
//				actor.GetProperty().SetInterpolationTypeToNearest();
//			((PerspectiveImage) image).firePropertyChange();
//		}
//	}// GEN-LAST:event_interpolateCheckBox1ActionPerformed
//
//	private void resetFrameAdjustmentsButtonActionPerformed(ActionEvent evt)
//	{// GEN-FIRST:event_resetFrameAdjustmentsButtonActionPerformed
//		// System.out.println("Reset Frame Adjustments");
//		((PerspectiveImage) image).resetSpacecraftState();
//		((PerspectiveImage) image).firePropertyChange();
//	}// GEN-LAST:event_resetFrameAdjustmentsButtonActionPerformed
//
//	private void adjustFrameCheckBox3ActionPerformed(ActionEvent evt)
//	{// GEN-FIRST:event_adjustFrameCheckBox3ActionPerformed
//		// System.out.println("Adjust frame...");
//		centerFrustumMode = adjustFrameCheckBox3.isSelected();
//	}// GEN-LAST:event_adjustFrameCheckBox3ActionPerformed
//
//	private void zoomOutButtonActionPerformed(ActionEvent evt)
//	{// GEN-FIRST:event_zoomOutButtonActionPerformed
//		// System.out.println("Zoom In");
//		if (image instanceof PerspectiveImage)
//		{
//			((PerspectiveImage) image).moveZoomFactorBy(Math.pow(1.1, getAdjustFactor()));
//			((PerspectiveImage) image).firePropertyChange();
//		}
//	}// GEN-LAST:event_zoomOutButtonActionPerformed

	private void factorTextField1ActionPerformed(ActionEvent evt)
	{// GEN-FIRST:event_factorTextField1ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_factorTextField1ActionPerformed

	// private void applyAdjustmentsButton1ActionPerformed(ActionEvent evt)
	// {//GEN-FIRST:event_applyAdjustmentsButton1ActionPerformed
	// // TODO add your handling code here:
	// }//GEN-LAST:event_applyAdjustmentsButton1ActionPerformed

//	private double getAdjustFactor()
//	{
//		double result = 1.0;
//		try
//		{
//			double delta = 1.0 * Double.parseDouble(factorTextField1.getText());
//			result = delta;
//		}
//		catch (Exception e)
//		{
//		}
//
//		return result;
//	}
//


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
		else
			updateSpectrumRegion(e);
	}

	private void updateSpectrumRegion(MouseEvent e)
	{
//		int pickSucceeded = doPick(e, imagePicker, renWin);
//		if (pickSucceeded == 1)
//		{
//			double[] p = imagePicker.GetPickPosition();
//			double[][] spectrumRegion =
//			{
//					{ p[0], p[1] } };
////			if (image instanceof PerspectiveImage)
////				((PerspectiveImage) image).setSpectrumRegion(spectrumRegion);
//		}
	}

	private void centerFrustumOnPixel(MouseEvent e)
	{
//		int pickSucceeded = doPick(e, imagePicker, renWin);
//		if (pickSucceeded == 1)
//		{
//			double[] pickPosition = imagePicker.GetPickPosition();
//			// Note we reverse x and y so that the pixel is in the form the
//			// camera
//			// position/orientation program expects.
////			if (image instanceof PerspectiveImage)
////			{
////				PerspectiveImage pi = (PerspectiveImage) image;
////				pi.setTargetPixelCoordinates(pickPosition);
////			}
//		}
	}

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

	private void levelsChanged()
	{
		// don't do anything for now; this seems to disable the auto-contrast
		// functionality

		/*
		 * vtkInteractorStyleImage
		 * style=(vtkInteractorStyleImage)renWin.getRenderWindowInteractor().
		 * GetInteractorStyle(); int[]
		 * currentLevels=style.GetWindowLevelCurrentPosition(); if
		 * (previousLevels==null) previousLevels=currentLevels; int
		 * dBrightness=currentLevels[1]-previousLevels[1]; int
		 * dContrast=currentLevels[0]-previousLevels[0]; int
		 * sliderChange=(int)((double)(slider.getMaximum()-slider.getMinimum())*
		 * (double)dContrast/(double)renWin.getComponent().getWidth());
		 * System.out.println(dContrast+" "+sliderChange);
		 * slider.setHighValue(slider.getHighValue()+sliderChange); // int
		 * lowVal = slider.getLowValue(); int highVal = slider.getHighValue();
		 * if (image != null) image.setDisplayedImageRange(new
		 * IntensityRange(lowVal, highVal)); previousLevels=currentLevels;
		 */
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
				File file = CustomFileChooser.showSaveDialog(renWin.getComponent(), "Export to PNG Image...",
						"image.png", "png");
				RenderIoUtil.saveToFile(file, renWin, null);
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
}