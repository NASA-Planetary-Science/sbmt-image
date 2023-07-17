package edu.jhuapl.sbmt.image.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import vtk.vtkActor;
import vtk.vtkImageData;
import vtk.vtkImageReslice;
import vtk.vtkInteractorStyleTrackballCamera;
import vtk.rendering.jogl.vtkJoglPanelComponent;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.RenderIoUtil;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.image.controllers.preview.ImageContrastController;
import edu.jhuapl.sbmt.image.controllers.preview.ImageMaskController;
import edu.jhuapl.sbmt.image.controllers.preview.ImagePropertiesController;
import edu.jhuapl.sbmt.image.controllers.preview.ImageTrimController;
import edu.jhuapl.sbmt.image.model.ImageProperty;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.vtk.VtkImageMaskingPipeline;

public class ActorPreviewPanel extends ModelInfoWindow implements MouseListener, MouseMotionListener, PropertyChangeListener
{
	public static final double VIEWPOINT_DELTA = 1.0;
	public static final double ROTATION_DELTA = 5.0;

	VtkImageMaskingPipeline maskPipeline;
	ImageTrimController trimController;
	ImageMaskController maskController;
	ImageContrastController contrastController;
	private vtkActor actor;
	private vtkJoglPanelComponent renWin;
	private vtkImageReslice reslice;


	private JPanel tablePanel;

	vtkImageData displayedImage;
	private HashMap<String, String> metadata;
	private boolean showContrast;

	public ActorPreviewPanel(String title, final vtkActor actor, HashMap<String, String> metadata, boolean showContrast) throws IOException, Exception
	{
		this.maskPipeline = new VtkImageMaskingPipeline();
		this.showContrast = showContrast;
		this.actor = actor;
		this.metadata = metadata;
		initComponents();
		if (showContrast)
			contrastController.setImageData(displayedImage);
		prepareRenderer();

//		setIntensity(new IntensityRange(0, 255));

		createMenus();

		setTitle(title);

		pack();
		setVisible(true);

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

//	private void setIntensity(IntensityRange range) throws IOException, Exception
//	{
//		VtkImageContrastPipeline pipeline = new VtkImageContrastPipeline(displayedImage, new IntensityRange(0, 255));
//		displayedImage = pipeline.getUpdatedData().get(0);
//		updateImage(displayedImage);
//	}

	private void prepareRenderer() //throws IOException, Exception
	{
		renWin = new vtkJoglPanelComponent();
		renWin.getComponent().setPreferredSize(new Dimension(550, 550));

		vtkInteractorStyleTrackballCamera style = new vtkInteractorStyleTrackballCamera();
		renWin.setInteractorStyle(style);

		renWin.getRenderWindow().GetInteractor().GetInteractorStyle().AddObserver("WindowLevelEvent", this,
				"levelsChanged");
//
//		updateImage(displayedImage);
//		System.out.println("ActorPreviewPanel: prepareRenderer: actor is " + actor);
		actor.SetVisibility(1);
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

//	private void updateImage(vtkImageData displayedImage)
//	{
//		double[] center = displayedImage.GetCenter();
//		int[] dims = displayedImage.GetDimensions();
//		// Rotate image by 90 degrees so it appears the same way as when you
//		// use the Center in Image option.
//		vtkTransform imageTransform = new vtkTransform();
//		imageTransform.Translate(center[0], center[1], 0.0);
//		imageTransform.RotateZ(-90.0);
//		imageTransform.Translate(-center[1], -center[0], 0.0);
//
//		reslice = new vtkImageReslice();
//		reslice.SetInputData(displayedImage);
////		reslice.SetResliceTransform(imageTransform);
//		reslice.SetInterpolationModeToNearestNeighbor();
//		reslice.SetOutputSpacing(1.0, 1.0, 1.0);
//		reslice.SetOutputOrigin(0.0, 0.0, 0.0);
//		reslice.SetOutputExtent(0, dims[0] - 1, 0, dims[1] - 1, 0, 0);
//		reslice.Update();
//
//		vtkImageSliceMapper imageSliceMapper = new vtkImageSliceMapper();
//		imageSliceMapper.SetInputConnection(reslice.GetOutputPort());
//		imageSliceMapper.Update();
//
//		actor.SetMapper(imageSliceMapper);
//		actor.GetProperty().SetInterpolationTypeToLinear();
//	}

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
		List<ImageProperty> properties = new ArrayList<ImageProperty>();
		for (String key : metadata.keySet())
		{
			properties.add(new ImageProperty(key, metadata.get(key)));
		}
		ImagePropertiesController propertiesController = new ImagePropertiesController(properties);
		tablePanel = propertiesController.getView();

//		jScrollPane1 = new JScrollPane();


		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(500, 500));
		setPreferredSize(new Dimension(775, 900));
		getContentPane().setLayout(new GridBagLayout());



		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 6;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.weightx = 1.0;
		getContentPane().add(tablePanel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.gridwidth = getContentPane().getWidth();
		gridBagConstraints.weightx = 1;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		contrastController = new ImageContrastController(displayedImage, new IntensityRange(0, 255), new Function<vtkImageData, Void>() {

			@Override
			public Void apply(vtkImageData t)
			{
				try
				{
					displayedImage = t;
//					updateImage(displayedImage);
//					setIntensity(null);
					renWin.Render();
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
		});
		if (showContrast)
			getContentPane().add(contrastController.getView(), gridBagConstraints);



		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 2;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.insets = new Insets(3, 6, 3, 0);


		pack();
	}// </editor-fold>//GEN-END:initComponents





	private void factorTextField1ActionPerformed(ActionEvent evt)
	{// GEN-FIRST:event_factorTextField1ActionPerformed
		// TODO add your handling code here:
	}// GEN-LAST:event_factorTextField1ActionPerformed

	// private void applyAdjustmentsButton1ActionPerformed(ActionEvent evt)
	// {//GEN-FIRST:event_applyAdjustmentsButton1ActionPerformed
	// // TODO add your handling code here:
	// }//GEN-LAST:event_applyAdjustmentsButton1ActionPerformed



	@Override
	public void mouseClicked(MouseEvent e)
	{
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
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{

	}

	private void updateSpectrumRegion(MouseEvent e)
	{

	}

	private void centerFrustumOnPixel(MouseEvent e)
	{

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

		setJMenuBar(menuBar);
	}
}