package edu.jhuapl.sbmt.image.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.beust.jcommander.internal.Lists;

import vtk.vtkActor;
import vtk.vtkProp;
import vtk.vtkProperty;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.RenderIoUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.view.light.LightUtil;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingDelta;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingState;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.io.export.SaveModifiedImagePointingFileToCacheOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.perspectiveImages.PerspectiveImageToRenderableImagePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointedImages.RenderablePointedImageToScenePipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.pointing.offset.PointedImageEditingPipeline;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class PointedImageEditingPanel<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends ModelInfoWindow
		implements MouseListener, MouseMotionListener
{
	public static final double VIEWPOINT_DELTA = 1.0;
	public static final double ROTATION_DELTA = 5.0;

	// private vtkJoglPanelComponent renWin;
	private Renderer renderer;

	private JButton exportModifiedPointingFileButton;
	private JButton rotateLeftButton;
	private JButton rotateRightButton;
	private JButton upButton;
	private JButton zoomInButton;
	private JButton zoomOutButton;
	private JButton leftButton;
	private JButton resetFrameAdjustmentsButton;
	private JButton rightButton;
	private JPanel pointingPanel;
	private JButton downButton;
	private JLabel factorLabel1;
	private JTextField factorTextField1;
	private JCheckBox adjustFrameCheckBox3;
	private GridBagConstraints gridBagConstraints;
	private G1 image;
	private boolean resetOnNextUpdate;
	private double currentZoomFactor = 1.0;
	private double currentSampleOffset = 0.0;
	private double currentLineOffset = 0.0;
	private double currentRotationAngle = 0.0;
	private SmallBodyModel smallBodyModel;
	List<vtkProp> props = Lists.newArrayList();
	VtkPropProvider propProvider;
	List<vtkActor> inputs;
	private SpacecraftPointingDelta delta = new SpacecraftPointingDelta();
	private JLabel currentRotationDeltaLabel;
	private JLabel currentSampleDeltaLabel;
	private JLabel currentLineDeltaLabel;
	private JLabel currentZoomDeltaLabel;
	private JCheckBox modifiedEnabled;
	private JSlider modifiedAlphaSlider;
	private JSlider originalAlphaSlider;
	private boolean isCustom = false;

	public PointedImageEditingPanel(G1 image, SmallBodyModel smallBodyModel, List<vtkActor> inputs, boolean isCustom)
	{
		this.image = image;
		this.inputs = inputs;
		this.smallBodyModel = smallBodyModel;
		this.renderer = new Renderer(smallBodyModel);
		this.isCustom = isCustom;
		renderer.setLightCfg(LightUtil.getSystemLightCfg());
		propProvider = new VtkPropProvider()
		{

			@Override
			public Collection<vtkProp> getProps()
			{
				return props;
			}
		};
		renderer.addVtkPropProvider(propProvider);
		initComponents();
		loadActors(inputs);
		createMenus();

		setTitle("Renderer Preview");

		pack();
		setVisible(true);

		// initialized = true;

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

	private void loadActors(List<vtkActor> inputs)
	{
		props.clear();
		for (vtkActor actor : inputs)
		{
			props.add(actor);
		}
		if (renderer.hasVtkPropProvider(propProvider))
			renderer.addVtkPropProvider(propProvider);
		renderer.notifySceneChange();
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

        pointingPanel = new JPanel();
        leftButton = new JButton();
        rightButton = new JButton();
        upButton = new JButton();
        downButton = new JButton();
        rotateLeftButton = new JButton();
        zoomOutButton = new JButton();
        zoomInButton = new JButton();
        rotateRightButton = new JButton();
        resetFrameAdjustmentsButton = new JButton();
        adjustFrameCheckBox3 = new JCheckBox();
        factorLabel1 = new JLabel();
        factorTextField1 = new JTextField();

        pointingPanel.setLayout(new GridBagLayout());

        exportModifiedPointingFileButton = new JButton("Export Pointing File");
        exportModifiedPointingFileButton.setEnabled(false);
        exportModifiedPointingFileButton.addActionListener(e -> {
        	String name = image.getModifiedPointingSource().get();
        	File file = CustomFileChooser.showSaveDialog(this, "Save Modified Pointing File...");
        	if (file != null)
        	{
        		try
				{
        			if (!name.toLowerCase().endsWith("info")) name += ".INFO";
					FileUtils.copyFile(new File(name), file);
				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        	}
        });

        gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = -1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(exportModifiedPointingFileButton, gridBagConstraints);

		leftButton.setText("<");
		leftButton.setToolTipText("left");
		leftButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					leftButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(leftButton, gridBagConstraints);

		rightButton.setText(">");
		rightButton.setToolTipText("right");
		rightButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					rightButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		pointingPanel.add(rightButton, gridBagConstraints);

		upButton.setText("^");
		upButton.setToolTipText("up");
		upButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					upButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(upButton, gridBagConstraints);

		downButton.setText("v");
		downButton.setToolTipText("down");
		downButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					downButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(downButton, gridBagConstraints);

		rotateLeftButton.setText("\\");
		rotateLeftButton.setToolTipText("rotate left");
		rotateLeftButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					rotateLeftButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 6;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(rotateLeftButton, gridBagConstraints);

		zoomOutButton.setText("-><-");
		zoomOutButton.setToolTipText("zoom out");
		zoomOutButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					zoomOutButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(zoomOutButton, gridBagConstraints);

		zoomInButton.setText("<-->");
		zoomInButton.setToolTipText("zoom in");
		zoomInButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					zoomInButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 1;
		pointingPanel.add(zoomInButton, gridBagConstraints);

		rotateRightButton.setText("/");
		rotateRightButton.setToolTipText("rotate right");
		rotateRightButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					rotateRightButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 7;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(rotateRightButton, gridBagConstraints);

//		gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.ipadx = 15;
//		gridBagConstraints.weightx = 1.0;
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 0;
//		gridBagConstraints.gridwidth = 2;

		resetFrameAdjustmentsButton.setText("Reset Pointing");
		resetFrameAdjustmentsButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					resetFrameAdjustmentsButtonActionPerformed(evt);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 6;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(resetFrameAdjustmentsButton, gridBagConstraints);

		adjustFrameCheckBox3.setText("Select Target");
		adjustFrameCheckBox3.setName(""); // NOI18N
		adjustFrameCheckBox3.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				try
				{
					adjustFrameCheckBox3ActionPerformed(evt);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.ipadx = 15;
		gridBagConstraints.weightx = 1.0;
//		pointingPanel.add(adjustFrameCheckBox3, gridBagConstraints);

		factorLabel1.setText("Factor");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.ipadx = 15;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(factorLabel1, gridBagConstraints);

		// factorTextField1.setColumns(5);
		factorTextField1.setText("1.0");
		factorTextField1.setPreferredSize(new Dimension(14, 28));
		factorTextField1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				factorTextField1ActionPerformed(evt);
			}
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 4;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.ipadx = 50;
		gridBagConstraints.weightx = 1.0;
		pointingPanel.add(factorTextField1, gridBagConstraints);

		TitledBorder pointingBorder = BorderFactory.createTitledBorder("Pointing Adjustments");
		pointingBorder.setTitleJustification(TitledBorder.CENTER);
		pointingPanel.setBorder(pointingBorder);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;

		getContentPane().add(pointingPanel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;
		getContentPane().add(getImageSettingsPanel(), gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		gridBagConstraints.weightx = 1.0;
		getContentPane().add(getCurrentPointingDeltaPanel(), gridBagConstraints);

		pack();
	}

	private void initRenderer()
	{
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
//		JMenuItem mi = new JMenuItem(new AbstractAction("Export to Image...")
//		{
//			@Override
//			public void actionPerformed(ActionEvent e)
//			{
//				File file = CustomFileChooser.showSaveDialog(renderer.getRenderWindowPanel().getComponent(),
//						"Export to PNG Image...", "image.png", "png");
//				RenderIoUtil.saveToFile(file, renderer.getRenderWindowPanel(), null);
//			}
//		});
//		fileMenu.add(mi);
		fileMenu.setMnemonic('F');
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
	}


	/**
	 * Updates the image properties and rerenders the image
	 *
	 * @throws Exception
	 */
	private void runEditingPipeline() throws Exception
	{
		List<IRenderableImage> renderableImages;
		PerspectiveImageToRenderableImagePipeline pipeline =
				new PerspectiveImageToRenderableImagePipeline(List.of(image));
		renderableImages = pipeline.getRenderableImages();
		RenderablePointedImage renderableImage = (RenderablePointedImage)renderableImages.get(0);
		SpacecraftPointingState origState =
				new SpacecraftPointingState(renderableImage.getPointing(), renderableImage.getImageWidth(), renderableImage.getImageHeight());
		delta = generateDelta();

		PointedImageEditingPipeline editingPipeline =
				new PointedImageEditingPipeline(origState, delta);
		Pair<SpacecraftPointingState, SpacecraftPointingDelta> updatedState = editingPipeline.getFinalState();

		//set modified pointing object to image

		//save that to disk
		List<File> updatedPointingFiles = Lists.newArrayList();
		Triple<G1, SpacecraftPointingState, SpacecraftPointingDelta> input =
				Triple.of(image, updatedState.getLeft(), delta);
		Just.of(input)
			.operate(new SaveModifiedImagePointingFileToCacheOperator<G1>(isCustom))
			.subscribe(Sink.of(updatedPointingFiles))
			.run();
		image.setModifiedPointingSource(Optional.of(updatedPointingFiles.get(0).getAbsolutePath()));

		RenderablePointedImageToScenePipeline<G1> actorPipeline =
				new RenderablePointedImageToScenePipeline<G1>(image, List.of(smallBodyModel));

		List<vtkActor> allActors = Lists.newArrayList();
		allActors.add(inputs.get(0));

		allActors.addAll(actorPipeline.getRenderableImageActors());
		allActors.addAll(actorPipeline.getRenderableModifiedImageActors());
		loadActors(allActors);
		modifiedEnabled.setEnabled(true);
		exportModifiedPointingFileButton.setEnabled(true);
		adjustPropOpactity(props.get(1), originalAlphaSlider.getValue());
		adjustPropOpactity(props.get(2), modifiedAlphaSlider.getValue());
	}

	private SpacecraftPointingDelta generateDelta()
	{
		SpacecraftPointingDelta delta = new SpacecraftPointingDelta();
		if (resetOnNextUpdate)
		{
			factorTextField1.setText("1.0");
			currentLineOffset = 0.0;
			currentSampleOffset = 0.0;
			currentRotationAngle = 0.0;
			currentZoomFactor = 1.0;
			resetOnNextUpdate = false;

			currentZoomDeltaLabel.setText("" + delta.getZoomFactor());
			currentSampleDeltaLabel.setText("" + delta.getSampleOffset());
			currentLineDeltaLabel.setText("" + delta.getLineOffset());
			currentRotationDeltaLabel.setText("" + delta.getRotationOffset());
			modifiedEnabled.setSelected(false);
			return delta;
		}
		modifiedEnabled.setSelected(true);
		delta.setLineOffset(currentLineOffset);
		delta.setRotationOffset(currentRotationAngle);
		delta.setSampleOffset(currentSampleOffset);
		delta.setZoomFactor(currentZoomFactor);

		currentZoomDeltaLabel.setText("" + delta.getZoomFactor());
		currentSampleDeltaLabel.setText("" + delta.getSampleOffset());
		currentLineDeltaLabel.setText("" + delta.getLineOffset());
		currentRotationDeltaLabel.setText("" + delta.getRotationOffset());

		return delta;
	}


	private void zoomInButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentZoomFactor *= Math.pow(1.1, -getAdjustFactor());
		runEditingPipeline();
	}

	private void leftButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentLineOffset += -getAdjustFactor()/1000.0;
		runEditingPipeline();
	}

	private void rightButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentLineOffset += getAdjustFactor()/1000.0;
		runEditingPipeline();
	}

	private void upButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentSampleOffset += -getAdjustFactor()/1000.0;
		runEditingPipeline();
	}

	private void downButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentSampleOffset += getAdjustFactor()/1000.0;
		runEditingPipeline();
	}

	private void rotateLeftButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentRotationAngle += -getAdjustFactor();
		runEditingPipeline();
	}

	private void rotateRightButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentRotationAngle += getAdjustFactor();
		runEditingPipeline();
	}

//	private void interpolateCheckBox1ActionPerformed(ActionEvent evt) throws Exception
//	{
//		currentInterpolateState = interpolateCheckBox1.isSelected();
//		image.setInterpolateState(currentInterpolateState);
//		runEditingPipeline();
//	}

	private void resetFrameAdjustmentsButtonActionPerformed(ActionEvent evt) throws Exception
	{
		resetOnNextUpdate = true;
		runEditingPipeline();
	}

	private void adjustFrameCheckBox3ActionPerformed(ActionEvent evt) throws Exception
	{
		//TODO I HAVE NO CLUE WHAT THIS DID ORIGINALLY
		runEditingPipeline();
		// System.out.println("Adjust frame...");
//		centerFrustumMode = adjustFrameCheckBox3.isSelected();
	}

	private void zoomOutButtonActionPerformed(ActionEvent evt) throws Exception
	{
		currentZoomFactor *= Math.pow(1.1, getAdjustFactor());
		runEditingPipeline();
	}

	private double getAdjustFactor()
	{
		return Double.parseDouble(factorTextField1.getText());
	}

	private void factorTextField1ActionPerformed(ActionEvent evt) {
    }

	@Override
	public void mouseMoved(MouseEvent arg0)
	{
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
		// if (centerFrustumMode && e.getButton() == 1)
		// {
		// if (e.isAltDown())
		// {
		// // System.out.println("Resetting pointing...");
		// // ((PerspectiveImage)image).resetSpacecraftState();
		// }
		// else
		// {
		// centerFrustumOnPixel(e);
		//
		// ((PerspectiveImage) image).loadFootprint();
		// // ((PerspectiveImage)image).calculateFrustum();
		// }
		// // PerspectiveImageBoundary boundary =
		// // imageBoundaryCollection.getBoundary(image.getKey());
		// // boundary.update();
		// // ((PerspectiveImageBoundary)boundary).firePropertyChange();
		//
		// ((PerspectiveImage) image).firePropertyChange();
		// }

		// int pickSucceeded = doPick(e, imagePicker, renWin);
		// if (pickSucceeded == 1)
		{
			// double[] p = imagePicker.GetPickPosition();
			//
			// // Display selected pixel coordinates in console output
			// // Note we reverse x and y so that the pixel is in the form the
			// // camera
			// // position/orientation program expects.
			// System.out.println(p[1] + " " + p[0]);
			//
			// // Display status bar message upon being picked
			// refStatusHandler.setLeftTextSource(image, null, 0, p);
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
		// if (centerFrustumMode && !e.isAltDown())
		// {
		//// ((PerspectiveImage) image).calculateFrustum();
		//// ((PerspectiveImage) image).firePropertyChange();
		// }
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		// if (centerFrustumMode && e.getButton() == 1)
		// {
		// if (!e.isAltDown())
		// {
		// centerFrustumOnPixel(e);
		// ((PerspectiveImage) image).loadFootprint();
		// }
		//
		// ((PerspectiveImage) image).firePropertyChange();
		//
		// }
		// else
		// updateSpectrumRegion(e);
	}

	private JPanel getImageSettingsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Image Settings"));
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.ipadx = 50;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		JCheckBox originalEnabled = new JCheckBox("Original Image");
		originalEnabled.setSelected(true);
		originalEnabled.addActionListener(e -> {
			JCheckBox checkBox = (JCheckBox)e.getSource();
			props.get(1).SetVisibility(checkBox.isSelected() ? 1 : 0);
			renderer.notifySceneChange();
		});
		panel.add(originalEnabled, gridBagConstraints);

		JLabel originalAlphaSliderLabel = new JLabel("Alpha");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
//		gridBagConstraints.ipadx = 50;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		panel.add(originalAlphaSliderLabel, gridBagConstraints);

		originalAlphaSlider = new JSlider();
		originalAlphaSlider.setValue(100);
		JLabel originalAlphaSliderValueLabel = new JLabel(""+originalAlphaSlider.getValue());

		originalAlphaSlider.addChangeListener(e -> {
			JSlider source = (JSlider)e.getSource();
			originalAlphaSliderValueLabel.setText(""+source.getValue());
			adjustPropOpactity(props.get(1), source.getValue());
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.ipadx = 100;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		panel.add(originalAlphaSlider, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		panel.add(originalAlphaSliderValueLabel, gridBagConstraints);

		modifiedEnabled = new JCheckBox("Modified Image");
		modifiedEnabled.setEnabled(false);
		modifiedEnabled.addActionListener(e -> {
			JCheckBox checkBox = (JCheckBox)e.getSource();
			props.get(2).SetVisibility(checkBox.isSelected() ? 1 : 0);
			renderer.notifySceneChange();
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.ipadx = 50;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		panel.add(modifiedEnabled, gridBagConstraints);

		JLabel modifiedAlphaSliderLabel = new JLabel("Alpha");
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		panel.add(modifiedAlphaSliderLabel, gridBagConstraints);

		modifiedAlphaSlider = new JSlider();
		modifiedAlphaSlider.setValue(100);
		JLabel modifiedAlphaSliderValueLabel = new JLabel(""+modifiedAlphaSlider.getValue());
		modifiedAlphaSlider.addChangeListener(e -> {
			JSlider source = (JSlider)e.getSource();
			modifiedAlphaSliderValueLabel.setText(""+source.getValue());
			adjustPropOpactity(props.get(2), source.getValue());
		});
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.ipadx = 100;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		panel.add(modifiedAlphaSlider, gridBagConstraints);


		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		panel.add(modifiedAlphaSliderValueLabel, gridBagConstraints);

		return panel;
	}

	private void adjustPropOpactity(vtkProp prop, double percentValue)
	{
		vtkProperty interiorProperty = ((vtkActor)(prop)).GetProperty();
		interiorProperty.SetOpacity(percentValue/100.0);
		renderer.notifySceneChange();
	}

	private JPanel getCurrentPointingDeltaPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Current Delta Values"));
		JLabel currentRotationLabel = new JLabel("Rotation Angle (deg):", SwingConstants.LEFT);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.ipadx = 50;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		panel.add(currentRotationLabel, gridBagConstraints);

		currentRotationDeltaLabel = new JLabel(""+delta.getRotationOffset(), SwingConstants.RIGHT);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.ipadx = 50;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		panel.add(currentRotationDeltaLabel, gridBagConstraints);

		JLabel currentSampleLabel = new JLabel("Sample (km):", SwingConstants.LEFT);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.ipadx = 50;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		panel.add(currentSampleLabel, gridBagConstraints);

		currentSampleDeltaLabel = new JLabel(""+delta.getSampleOffset(), SwingConstants.RIGHT);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.ipadx = 50;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		panel.add(currentSampleDeltaLabel, gridBagConstraints);

		JLabel currentLineLabel = new JLabel("Line (km):", SwingConstants.LEFT);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.ipadx = 50;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		panel.add(currentLineLabel, gridBagConstraints);

		currentLineDeltaLabel = new JLabel(""+delta.getLineOffset(), SwingConstants.RIGHT);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.ipadx = 50;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		panel.add(currentLineDeltaLabel, gridBagConstraints);

		JLabel currentZoomLabel = new JLabel("Zoom :", SwingConstants.LEFT);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.ipadx = 50;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		panel.add(currentZoomLabel, gridBagConstraints);

		currentZoomDeltaLabel = new JLabel(""+delta.getZoomFactor(), SwingConstants.RIGHT);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 3;
		gridBagConstraints.ipadx = 50;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		panel.add(currentZoomDeltaLabel, gridBagConstraints);

		return panel;
	}
}