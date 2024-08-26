package edu.jhuapl.sbmt.image.ui.custom.importer;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.controllers.custom.CustomImageEditingController;
import edu.jhuapl.sbmt.image.interfaces.IImagingInstrument;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.CompositePerspectiveImage;
import edu.jhuapl.sbmt.image.model.CylindricalBounds;
import edu.jhuapl.sbmt.image.model.ImageOrigin;
import edu.jhuapl.sbmt.image.model.ImageType;
import edu.jhuapl.sbmt.image.model.Orientation;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.model.PerspectiveImageMetadata;
import edu.jhuapl.sbmt.image.ui.custom.importer.table.CustomImageImporterTableView;
import glum.item.BaseItemManager;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;

public class CustomImageImporterDialog2<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends JDialog
{
	private boolean isPerspective;
	private boolean isEllipsoid;
	private PerspectiveImageCollection<G1> imageCollection;
	private IImagingInstrument instrument;
	private CustomImageImporterTableView<G1> table;
	private JComboBox<ImageType> imageTypeComboBox;
	private BaseItemManager<G1> tempCollection;

	public CustomImageImporterDialog2(Window parent, boolean isEditMode,  boolean isPerspective, IImagingInstrument instrument, boolean isEllipsoid, PerspectiveImageCollection<G1> imageCollection)
	{
		 super(parent, isEditMode ? "Edit Image" : "Import New Image", Dialog.ModalityType.APPLICATION_MODAL);
		 this.instrument = instrument;
		 this.isPerspective = isPerspective;
		 this.isEllipsoid = isEllipsoid;
		 this.imageCollection = imageCollection;
		 this.tempCollection = new BaseItemManager<G1>();
		 tempCollection.addListener(new ItemEventListener()
		 {
			@Override
			public void handleItemEvent(Object aSource, ItemEventType aEventType)
			{
				table.getDeleteImageButton().setEnabled(tempCollection.getSelectedItems().size() > 0);
				table.getEditImageButton().setEnabled(tempCollection.getSelectedItems().size() == 1);
			}
		 });
		 initGUI();
		 setSize(700, 400);
	}

	private void initGUI()
	{
		table = new CustomImageImporterTableView<G1>(tempCollection, null);
		table.setup();

		table.getLoadImageButton().addActionListener(e -> {
			File[] files = CustomFileChooser.showOpenDialog(table, "Select images...", List.of("fits", "fit", "FIT", "FITS", "png", "PNG", "JPG", "jpg", "IMG", "img"), true);
			if (files == null || files.length == 0)
	        {
	            return;
	        }
			List<G1> tempImages = Lists.newArrayList(tempCollection.getAllItems());
			int index = 1;
			boolean showPointingFileNotFoundDialog = false;
 			for (File file : files)
			{
 				G1 image;
				try
				{
					image = resolvePointingFilename(file.getAbsolutePath());
					if (image.getPointingSource().equals("FILE NOT FOUND"))
						showPointingFileNotFoundDialog = true;
					image.setIndex(index++);
					tempImages.add(image);
				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
 			if (showPointingFileNotFoundDialog && isPerspective)
 			{
 				JOptionPane.showMessageDialog(this, "Pointing file(s) not found. Review table and edit the imported images to find the pointing(s).");
 			}
 			tempCollection.setAllItems(tempImages);
 			tempCollection.setSelectedItems(List.of(tempImages.get(tempImages.size()-1)));
		});

		table.getDeleteImageButton().addActionListener(e -> {
			tempCollection.removeItems(tempCollection.getSelectedItems());
		});

		table.getEditImageButton().addActionListener(e -> {
			showEditPointingDialog();
		});


		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

		topPanel.add(buildImageTypeInput());
//		topPanel.add(buildPointingInput());
		if (isPerspective)
			table.setType("Perspective Projection");
		else
			table.setType("Simple Cylindrical Projection");

		getContentPane().add(topPanel);
		getContentPane().add(table);
		getContentPane().add(buildSubmitCancelPanel());
	}

	private void showEditPointingDialog()
	{
		ImmutableSet<G1> selectedItems = tempCollection.getSelectedItems();
		if (selectedItems.size() != 1) return;
		G1 image = selectedItems.asList().get(0);
		if (image.getNumberOfLayers() == 1)	//editing custom single layer image
		{
			Runnable completionBlock = new Runnable()
			{
				@Override
				public void run()
				{
					imageCollection.updateImage(image);
				}
			};

			CustomImageEditingController<G1> dialog = new CustomImageEditingController<G1>(null, isEllipsoid, isPerspective, image, instrument, completionBlock);
	        dialog.getDialog().setLocationRelativeTo(getContentPane());
	        dialog.getDialog().setVisible(true);
//	        ImageSource pointingSourceType = image.getPointingSource().endsWith("sum") || image.getPointingSource().endsWith("SUM") ? ImageSource.GASKELL : ImageSource.SPICE;
//	        if (image.getPointingSource().equals("FILE NOT FOUND")) pointingSourceType = ImageSource.LOCAL_CYLINDRICAL;
//	        image.setPointingSourceType(pointingSourceType);
	        storeImage(image.getFilename(), image.getFilename(), image.getPointingSourceType(), image.getPointingSource(), image.getFlip());
		}
//		else if (image.getNumberOfLayers() == 3) //editing custom color image
//		{
//			ColorImageBuilderController controller = new ColorImageBuilderController(smallBodyModels, tempCollection, Optional.of(image));
//			controller.setImages(image.getImages());
//			BasicFrame frame = new BasicFrame();
//			frame.add(controller.getView());
//			frame.setSize(775, 900);
//			frame.setTitle("Edit Color Image");
//			frame.setVisible(true);
//			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		}
//		else //editing custom n > 1, n!=3 spectral image
//		{
//
//		}
	}

	private JPanel buildImageTypeInput()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(new JLabel("Image Type:"));

		if (instrument != null && isPerspective)
			imageTypeComboBox = new JComboBox<ImageType>(new ImageType[] {instrument.getType(), ImageType.GENERIC_IMAGE});
		else
			imageTypeComboBox = new JComboBox<ImageType>(new ImageType[] {ImageType.GENERIC_IMAGE});
		imageTypeComboBox.setMaximumSize(new Dimension(350, 30));
		panel.add(Box.createHorizontalStrut(10));
		panel.add(imageTypeComboBox);
		panel.add(Box.createHorizontalStrut(100));
		return panel;
	}



	private JPanel buildSubmitCancelPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");

		okButton.addActionListener(e -> {
			String errorString = validateInput();
	        if (errorString != null)
	        {
	            JOptionPane.showMessageDialog(this,
	                    errorString,
	                    "Error",
	                    JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        saveImagesToCollection();
	        setVisible(false);
		});

		cancelButton.addActionListener(e -> setVisible(false));

		panel.add(okButton);
		panel.add(cancelButton);
		return panel;
	}

	private String validateInput()
	{
		for (G1 image : tempCollection.getAllItems())
		{
			if (image.getPointingSource().equals("FILE NOT FOUND") && isPerspective) return "Please pick pointing for all files listed as FILE NOT FOUND";
		}
		return null;
	}

	private void saveImagesToCollection()
	{
		List<G1> images = tempCollection.getAllItems();
		for (G1 image : images)
		{
			imageCollection.addUserImage(image);
		}
		imageCollection.loadUserList();
		imageCollection.setImagingInstrument(null);
	}

	private G1 resolvePointingFilename(String filename) throws IOException
	{
		String newFilepath = imageCollection.getSmallBodyModels().get(0).getCustomDataFolder() + File.separator + new File(filename).getName();
		FileUtil.copyFile(filename,  newFilepath);


		String withoutExtension = FilenameUtils.removeExtension(filename);
		String pointingSource = "";
		PointingSource pointingSourceType = null;
		if (isPerspective)
		{
			if (new File(withoutExtension + ".SUM").exists())
			{
				pointingSource = new File(withoutExtension + ".SUM").getAbsolutePath();
				pointingSourceType = PointingSource.GASKELL;
			}
			else if (new File(withoutExtension + ".INFO").exists())
			{
				pointingSource = new File(withoutExtension + ".INFO").getAbsolutePath();
				pointingSourceType = PointingSource.SPICE;
			}
			else
			{
				pointingSource = new File(withoutExtension + ".LBL").getAbsolutePath();
				pointingSourceType = PointingSource.LABEL;
			}
		}
		else
			pointingSourceType = PointingSource.LOCAL_CYLINDRICAL;

		String newPointingFilepath = "FILE NOT FOUND";
		if (!pointingSource.isEmpty())
		{
			newPointingFilepath = imageCollection.getSmallBodyModels().get(0).getCustomDataFolder() + File.separator + new File(pointingSource).getName();
			if (new File(pointingSource).exists())
				FileUtil.copyFile(pointingSource,  newPointingFilepath);
			else
			{
				newPointingFilepath = "FILE NOT FOUND";
			}
			String extension = FilenameUtils.getExtension(pointingSource).toLowerCase();
			pointingSourceType = extension.toLowerCase().equals("sum") ? PointingSource.GASKELL : PointingSource.SPICE;
			return storeImage(filename, newFilepath, pointingSourceType, newPointingFilepath);
		}
		return storeImage(filename, newFilepath, pointingSourceType, newPointingFilepath);
	}

	private G1 storeImage(String filename, String newFilepath, PointingSource pointingSourceType, String newPointingFilepath)
	{
		return storeImage(filename, newFilepath, pointingSourceType, newPointingFilepath, "");
	}

	private G1 storeImage(String filename, String newFilepath, PointingSource pointingSourceType, String newPointingFilepath, String flip)
	{
		ImageType imageType = (ImageType)imageTypeComboBox.getSelectedItem();

		double[] fillValues = new double[] {};
		PerspectiveImageMetadata image = new PerspectiveImageMetadata(newFilepath, imageType, pointingSourceType, newPointingFilepath, fillValues);

		image.setName(getName());
		image.setImageOrigin(ImageOrigin.LOCAL);
		image.setLongTime(new Date().getTime());
		if (pointingSourceType == PointingSource.LOCAL_CYLINDRICAL)
		{
			image.setBounds(new CylindricalBounds(-90,90,0,360));
			image.setFlip(flip);
		}
		else
		{
			if ((imageType != ImageType.GENERIC_IMAGE) && (newPointingFilepath != "FILE NOT FOUND"))
			{
			    Orientation orientation = instrument.getOrientation(pointingSourceType);

			    image.setLinearInterpolatorDims(instrument.getLinearInterpolationDims());
				image.setMaskValues(instrument.getMaskValues());
				image.setFillValues(instrument.getFillValues());
				image.setFlip(orientation.getFlip().flip());
				image.setRotation(orientation.getRotation());
			}
		}
		CompositePerspectiveImage compImage = new CompositePerspectiveImage(List.of(image));
		compImage.setName(FilenameUtils.getBaseName(filename));
		return (G1)compImage;
	}
}
