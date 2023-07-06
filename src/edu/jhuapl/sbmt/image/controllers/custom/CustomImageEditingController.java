package edu.jhuapl.sbmt.image.controllers.custom;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import vtk.vtkImageData;
import vtk.vtkImageReslice;
import vtk.vtkImageSlice;
import vtk.vtkImageSliceMapper;
import vtk.vtkInteractorStyleImage;
import vtk.vtkTransform;
import vtk.rendering.jogl.vtkJoglPanelComponent;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.controllers.preview.ImageContrastController;
import edu.jhuapl.sbmt.image.controllers.preview.ImageFillValuesController;
import edu.jhuapl.sbmt.image.controllers.preview.ImageMaskController;
import edu.jhuapl.sbmt.image.interfaces.IImagingInstrument;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.CylindricalBounds;
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.model.Orientation;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.io.IPerspectiveImageToLayerAndMetadataPipeline;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.rendering.vtk.VtkImageContrastPipeline;
import edu.jhuapl.sbmt.image.ui.custom.editing.CustomImageEditingDialog;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.publisher.IPipelinePublisher;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class CustomImageEditingController<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> implements MouseListener, MouseMotionListener, PropertyChangeListener
{
	CustomImageEditingDialog<G1> dialog;
	private G1 existingImage;
	private boolean isPerspective;
	private ImageType imageType = null;
	private List<Layer> layers = Lists.newArrayList();
	private Layer layer;

	private vtkJoglPanelComponent renWin;
	private vtkImageSlice actor = new vtkImageSlice();
	private vtkImageReslice reslice;
	private vtkImageData displayedImage;
	private boolean isEllipsoid;

	ImageMaskController maskController;
	ImageContrastController contrastController;
	private ImageFillValuesController fillValuesController;
	private IImagingInstrument instrument;
	private Runnable completionBlock;

	public CustomImageEditingController(Window parent, boolean isEllipsoid, boolean isPerspective, G1 existingImage, IImagingInstrument instrument, Runnable completionBlock)
	{
		this.existingImage = existingImage;
		this.isEllipsoid = isEllipsoid;
		this.completionBlock = completionBlock;
		this.isPerspective = isPerspective;
		this.instrument = instrument;

		if (!existingImage.getPointingSource().equals("FILE NOT FOUND"))
			regenerateLayerFromImage(existingImage);
		contrastController = new ImageContrastController(displayedImage, existingImage.getIntensityRange(), new Function<vtkImageData, Void>() {

			@Override
			public Void apply(vtkImageData t)
			{
				try
				{
					displayedImage = t;
					updateImage(displayedImage);
					setIntensity(null);
					renWin.Render();
					existingImage.setIntensityRange(contrastController.getIntensityRange());
					maskController.setLayer(layer);
					if (completionBlock != null) completionBlock.run();
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
		});

		maskController = new ImageMaskController(layer, existingImage.getMaskValues(), new Function<Pair<Layer, int[]>, Void>()
		{

			@Override
			public Void apply(Pair<Layer, int[]> items)
			{
				try
				{
					existingImage.setMaskValues(items.getRight());
					regenerateLayerFromImage(existingImage);
					if (displayedImage == null) return null;
					generateVtkImageData(layer);
					updateImage(displayedImage);
					setIntensity(contrastController.getIntensityRange());
					if (renWin == null) return null;
					renWin.Render();
					if (completionBlock != null) completionBlock.run();
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
		});

		fillValuesController = new ImageFillValuesController(new Function<double[], Void>()
		{

			@Override
			public Void apply(double[] t)
			{
				existingImage.setFillValues(t);
				return null;
			}
		});

		dialog = new CustomImageEditingDialog<G1>(dialog, existingImage, isPerspective, completionBlock, maskController, contrastController, fillValuesController);
		populateUI();
		if (!existingImage.getPointingSource().equals("FILE NOT FOUND") || existingImage.getPointingSourceType() == PointingSource.LOCAL_CYLINDRICAL)
			renderLayerAndAddAttributes();
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (renWin != null)
				{
					renWin.resetCamera();
					renWin.Render();
				}
			}
		});
	}

	private void renderLayerAndAddAttributes()
	{
		try
		{
			if (layer == null) regenerateLayerFromImage(existingImage);
			if (layer == null) return;
			dialog.getAppearancePanel().setEnabled(true);

			SwingUtilities.invokeLater(() -> {
				try
				{
					renderLayer(layer);
					if (displayedImage.GetNumberOfScalarComponents() == 1)
						setIntensity(existingImage.getIntensityRange());
				}
				catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			});

		}
		catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void populateUI()
	{
		dialog.getImagePathTextField().setText(existingImage.getFilename());
		dialog.getImageNameTextField().setText(existingImage.getName());


		if (!existingImage.getPointingSourceType().toString().contains("Cylindrical"))
		{
			dialog.getPointingTypeComboBox().setSelectedIndex(0);
			dialog.getPointingFilenameTextField().setText(existingImage.getPointingSource());
//			if (instrument != null)
//			{
//				Orientation orientation = instrument.getOrientation(existingImage.getPointingSourceType());
//				dialog.getImageFlipComboBox().setSelectedItem(existingImage.getFlip());
//				dialog.getImageRotationComboBox().setSelectedItem("" + (int) (existingImage.getRotation()));
//
//
////				dialog.getImageFlipComboBox().setSelectedItem(orientation.getFlip().toString());
////				dialog.getImageRotationComboBox().setSelectedItem("" + (int) (orientation.getRotation()));
//			}
//			else
			{
				dialog.getImageFlipComboBox().setSelectedItem(existingImage.getFlip());
				dialog.getImageRotationComboBox().setSelectedItem("" + (int) (existingImage.getRotation()));
				dialog.getFlipAboutXCheckBox().setSelected(existingImage.getFlip().equals("X"));
			}
		}
		else
		{
			if (instrument != null)
			{
				try
				{
					Orientation orientation = instrument.getOrientation(existingImage.getPointingSourceType());
					dialog.getFlipAboutXCheckBox().setSelected(orientation.getFlip().toString().equals("X"));
				} catch (IllegalArgumentException iae)
				{
					dialog.getFlipAboutXCheckBox().setSelected(existingImage.getFlip().equals("X"));
				}
			}
			dialog.getPointingTypeComboBox().setSelectedIndex(1);
			dialog.getMinLatitudeTextField().setText("" + existingImage.getBounds().minLatitude());
			dialog.getMaxLatitudeTextField().setText("" + existingImage.getBounds().maxLatitude());
			dialog.getMinLongitudeTextField().setText("" + existingImage.getBounds().minLongitude());
			dialog.getMaxLongitudeTextField().setText("" + existingImage.getBounds().maxLongitude());
		}

		for (double val : existingImage.getFillValues())
		{
			fillValuesController.getFillValuesTextField().setText(fillValuesController.getFillValuesTextField().getText() + val + ",");
		}

		if (existingImage.getPointingSourceType().toString().contains("Cylindrical"))
			dialog.getPointingTypeComboBox().setSelectedItem("Simple Cylindrical Projection");
		else
			dialog.getPointingTypeComboBox().setSelectedItem("Perspective Projection");

		dialog.getPointingTypeComboBox().addActionListener(e -> {
			if (dialog.getPointingTypeComboBox().getSelectedIndex() == 1)
				existingImage.setPointingSourceType(PointingSource.LOCAL_CYLINDRICAL);
			else
			{
				existingImage.setPointingSourceType(PointingSource.SPICE);
				if (dialog.getPointingFilenameTextField().getText().toLowerCase().endsWith("sum"))
					existingImage.setPointingSourceType(PointingSource.GASKELL);
			}
		});

		dialog.getMaskController().setMaskValues(existingImage.getMaskValues());

		dialog.getBrowseButton().addActionListener(e ->
		{
			File[] files = CustomFileChooser.showOpenDialog(this.getDialog(), "Select Pointing File...", List.of("info", "INFO", "sum", "SUM"), false);
			if (files == null || files.length == 0)
	        {
	            return;
	        }

			String filename = files[0].getAbsolutePath();
			dialog.getPointingFilenameTextField().setText(filename);
			existingImage.setPointingSource(filename);
			if (dialog.getPointingFilenameTextField().getText().toLowerCase().endsWith("sum"))
				existingImage.setPointingSourceType(PointingSource.GASKELL);
			else
				existingImage.setPointingSourceType(PointingSource.SPICE);
			if (instrument != null)
			{
				Orientation orientation2 = instrument.getOrientation(existingImage.getPointingSourceType());
				dialog.getImageRotationComboBox().setSelectedItem("" + (int) (orientation2.getRotation()));
				dialog.getImageFlipComboBox().setSelectedItem(orientation2.getFlip().toString());
			}
			renderLayerAndAddAttributes();
		});

		dialog.getOkButton().addActionListener(e ->
		{
			String errorString = validateInput();
			if (errorString != null)
			{
				JOptionPane.showMessageDialog(getDialog(), errorString, "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			storeImage();

			dialog.setVisible(false);
		});

		dialog.getImageFlipComboBox().addActionListener(e -> {
			existingImage.setFlip((String)dialog.getImageFlipComboBox().getSelectedItem());
			Thread thread = new Thread(() -> {
				renderLayerAndAddAttributes();
			});
			thread.start();

		});

		dialog.getImageRotationComboBox().addActionListener(e -> {
			existingImage.setRotation(Double.parseDouble((String)dialog.getImageRotationComboBox().getSelectedItem()));
			Thread thread = new Thread(() -> {
				renderLayerAndAddAttributes();
			});
			thread.start();
		});

		fillValuesController.getFillValuesButton().addActionListener(e -> {
			String[] valueStrings = fillValuesController.getFillValuesTextField().getText().split(",");
			double[] doubleArray = new double[valueStrings.length];
			if (valueStrings.length == 0 || valueStrings[0].isBlank())
			{
				existingImage.setFillValues(doubleArray);
				return;
			}
			int i=0;
			for (String val : valueStrings)
			{
				doubleArray[i++] = Double.parseDouble(val);
			}
			existingImage.setFillValues(doubleArray);
			Thread thread = new Thread(() -> {
				renderLayerAndAddAttributes();
			});
			thread.start();
		});
	}

	private String validateInput()
	{
		String imagePath = dialog.getImagePathTextField().getText();
		if (imagePath == null)
			imagePath = "";

		String imageName = dialog.getImageNameTextField().getText();
		if (imageName == null)
			imageName = "";
		if (imageName.trim().isEmpty())
			return "Please enter a name for the image. The name can be any text that describes the image.";
		if (imageName.contains(","))
			return "Name may not contain commas.";

		// TODO fix this
		// if (!isEditMode && currentNames.contains(imageName))
		// {
		// return "Name for custom image already exists.";
		// }

		// if (imageTypeComboBox.getSelectedItem().toString().equals("<CHOOSE
		// IMAGE TYPE>"))
		// {
		// return "Select an image type.";
		// }

		if (dialog.getPointingTypeComboBox().getSelectedItem().equals("Simple Cylindrical Projection"))
		{
			try
			{
				double lllat = Double.parseDouble(dialog.getMinLatitudeTextField().getText());
				double urlat = Double.parseDouble(dialog.getMaxLatitudeTextField().getText());
				Double.parseDouble(dialog.getMinLongitudeTextField().getText());
				Double.parseDouble(dialog.getMaxLongitudeTextField().getText());

				if (lllat < -90.0 || lllat > 90.0 || urlat < -90.0 || urlat > 90.0)
					return "Latitudes must be between -90 and +90.";
				if (lllat >= urlat)
					return "Upper right latitude must be greater than lower left latitude.";

				if (!isEllipsoid)
				{
					if ((lllat < 1.0 && lllat > 0.0) || (lllat > -1.0 && lllat < 0.0) || (urlat < 1.0 && urlat > 0.0)
							|| (urlat > -1.0 && urlat < 0.0))
						return "For non-ellipsoidal shape models, latitudes must be (in degrees) either 0, greater than +1, or less then -1.";
				}
			}
			catch (NumberFormatException e)
			{
				return "An error occurred parsing one of the required fields.";
			}
		}
		else // uses pointing file
		{
			String pointingFileName = dialog.getPointingFilenameTextField().getText();
			int typeIndex = dialog.getPointingTypeComboBox().getSelectedIndex();
			if (pointingFileName.equals("FILE NOT FOUND") &&  typeIndex == 0) return "Please select a pointing file";
			if ((!pointingFileName.isEmpty()))
			{
				if (dialog.getPointingFilenameTextField().getText().isEmpty() == true)
					return "Please enter the path to a pointing file.";

				if (!pointingFileName.isEmpty())
				{
					File file = new File(pointingFileName);
					if (!file.exists() || !file.canRead() || !file.isFile())
						return pointingFileName + " does not exist or is not readable.";

					if (pointingFileName.contains(","))
						return "Path may not contain commas.";
				}
			}
		}

		return null;
	}

	private void storeImage()
	{
		imageType = existingImage.getImageType();
		existingImage.setName(dialog.getImageNameTextField().getText());
		if (dialog.getPointingTypeComboBox().getSelectedItem().equals("Perspective Projection"))
		{
			existingImage.setPointingSource(dialog.getPointingFilenameTextField().getText());
			if (instrument != null)
			{
				existingImage.setFlip(existingImage.getFlip());
				existingImage.setRotation(existingImage.getRotation());
			}
		}
		else
		{
			Double minLat = Double.parseDouble(dialog.getMinLatitudeTextField().getText());
			Double maxLat = Double.parseDouble(dialog.getMaxLatitudeTextField().getText());
			Double minLon = Double.parseDouble(dialog.getMinLongitudeTextField().getText());
			Double maxLon = Double.parseDouble(dialog.getMaxLongitudeTextField().getText());
			existingImage.setBounds(new CylindricalBounds(minLat, maxLat, minLon, maxLon));
			if (dialog.getFlipAboutXCheckBox().isSelected())
				existingImage.setFlip("X");
			else
				existingImage.setFlip("None");
		}
		//Fill values are handled by the apply button
	}

	private void generateVtkImageData(Layer layer) throws IOException, Exception
	{
		if (dialog == null) return;
		List<vtkImageData> displayedImages = new ArrayList<vtkImageData>();
		IPipelinePublisher<Layer> reader = new Just<Layer>(layer);
		reader.operate(new VtkImageRendererOperator(true)).subscribe(new Sink<vtkImageData>(displayedImages)).run();
		displayedImage = displayedImages.get(0);
		dialog.getContrastController().setImageData(displayedImage);
		if (displayedImage.GetNumberOfScalarComponents() != 1)
			dialog.getContrastController().getView().setVisible(false);
		this.layer = layer;
	}

	private void regenerateLayerFromImage(G1 image)
	{
		try
		{
			IPerspectiveImageToLayerAndMetadataPipeline pipeline = IPerspectiveImageToLayerAndMetadataPipeline.of(image);
			this.layer = pipeline.getLayers().get(0);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void renderLayer(Layer layer) throws IOException, Exception
	{
		generateVtkImageData(layer);

		if (renWin == null)
		{
			renWin = new vtkJoglPanelComponent();
			renWin.getComponent().setPreferredSize(new Dimension(550, 550));

			vtkInteractorStyleImage style = new vtkInteractorStyleImage();
			renWin.setInteractorStyle(style);

			renWin.setSize(550, 550);

			renWin.getRenderer().SetBackground(new double[]{ 0.5f, 0.5f, 0.5f });

			renWin.getComponent().addMouseListener(this);
			renWin.getComponent().addMouseMotionListener(this);
			renWin.getRenderer().GetActiveCamera().Dolly(0.2);
			renWin.resetCamera();
			// renWin.addKeyListener(this);

			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			dialog.getLayerPanel().add(renWin.getComponent(), gridBagConstraints);
		}
		updateImage(displayedImage);
		renWin.getRenderer().AddActor(actor);
		dialog.repaint();
		dialog.revalidate();
		renWin.resetCamera();

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
		// reslice.SetResliceTransform(imageTransform);
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

	private void setIntensity(IntensityRange range) throws IOException, Exception
	{
		VtkImageContrastPipeline pipeline = new VtkImageContrastPipeline(displayedImage, range);
		displayedImage = pipeline.getUpdatedData().get(0);
 		updateImage(displayedImage);
		if (completionBlock != null) completionBlock.run();
	}

	public CustomImageEditingDialog<G1> getDialog()
	{
		return dialog;
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

	@Override
	public void mouseClicked(MouseEvent e)
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
}
