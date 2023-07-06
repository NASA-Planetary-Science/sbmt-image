package edu.jhuapl.sbmt.image.ui.table.popup.export;

import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.io.export.SaveImageToENVIOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;

import glum.gui.action.PopAction;

public class ExportENVIImageAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	private PerspectiveImageCollection<G1> aManager;

	/**
	 * @param imagePopupMenu
	 */
	public ExportENVIImageAction(PerspectiveImageCollection<G1> aManager)
	{
		this.aManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		ImmutableSet<G1> selectedItems = aManager.getSelectedItems();
		if (selectedItems.size() != 1)
			return;
		for (IPerspectiveImage aItem : selectedItems)
		{
			File file = null;
	        try
	        {
	            // Default name
	            String fullPathName = aItem.getFilename();
	            String imageFileName = new File(fullPathName).getName();

	            String defaultFileName = null;
	            if (imageFileName != null)
	                defaultFileName = imageFileName.substring(0, imageFileName.length()-4);

	            // Open save dialog
	            file = CustomFileChooser.showSaveDialog(null, "Export ENVI image as", defaultFileName + "hdr", "hdr");
	            if (file != null)
	            {
	                String filename = file.getAbsolutePath();
	                Just.of(aItem)
	                	.operate(new SaveImageToENVIOperator(filename))
//	                	.subscribe(Sink.of(null))
	                	.run();
	            }
	        }
	        catch(Exception ex)
	        {
	            // Something went wrong during the file conversion/export process
	            // (after save dialog has returned)
	            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
	                    "Unable to export ENVI image as " + file.getAbsolutePath(),
	                    "Error Exporting ENVI Image",
	                    JOptionPane.ERROR_MESSAGE);
	            ex.printStackTrace();
	        }
		}
	}

//	public void actionPerformed(ActionEvent e)
//    {
//        // Only works for a single image (for now)
//        if (imageKeys.size() != 1)
//            return;
//        ImageKeyInterface imageKey = imageKeys.get(0);
//
//        File file = null;
//        try
//        {
//            // Get the PerspectiveImage
//            this.imagePopupMenu.imageCollection.addImage(imageKey);
//            PerspectiveImage image = (PerspectiveImage)this.imagePopupMenu.imageCollection.getImage(imageKey);
//
//            // Default name
//            String fullPathName = image.getFitFileFullPath();
//            if (fullPathName == null)
//                fullPathName = image.getPngFileFullPath();
//            String imageFileName = new File(fullPathName).getName();
//
//            String defaultFileName = null;
//            if (imageFileName != null)
//                defaultFileName = imageFileName.substring(0, imageFileName.length()-4);
//
//            // Open save dialog
//            file = CustomFileChooser.showSaveDialog(this.imagePopupMenu.invoker, "Export ENVI image as", defaultFileName + ".hdr", "hdr");
//            if (file != null)
//            {
//                String filename = file.getAbsolutePath();
//                image.exportAsEnvi(filename.substring(0, filename.length()-4), "bsq", true);
//            }
//        }
//        catch(Exception ex)
//        {
//            // Something went wrong during the file conversion/export process
//            // (after save dialog has returned)
//            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this.imagePopupMenu.invoker),
//                    "Unable to export ENVI image as " + file.getAbsolutePath(),
//                    "Error Exporting ENVI Image",
//                    JOptionPane.ERROR_MESSAGE);
//            ex.printStackTrace();
//        }
//    }
}