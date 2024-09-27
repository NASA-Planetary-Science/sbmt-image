package edu.jhuapl.sbmt.image.ui.table.popup.export;

import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.dialog.DirectoryChooser;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.io.export.SaveImageFileFromCacheOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import glum.gui.action.PopAction;

public class ExportFitsInfoPairsAction<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends PopAction<G1>
{
	private PerspectiveImageCollection<G1> aManager;

	/**
	 * @param imagePopupMenu
	 */
	public ExportFitsInfoPairsAction(PerspectiveImageCollection<G1> aManager)
	{
		this.aManager = aManager;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{

		File outDir = DirectoryChooser.showOpenDialog(null, "Save FITS/Pointing Pair to Directory...");
		if (outDir == null)
			return;
		ImmutableSet<G1> selectedItems = aManager.getSelectedItems();
		for (G1 aItem : selectedItems)
		{
			List<File> files = Lists.newArrayList();

			try
			{
				Just.of(aItem)
					.operate(new SaveImageFileFromCacheOperator<G1>(outDir.getAbsolutePath()))
					.subscribe(Sink.of(files))
					.run();
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			File file = null;
			try
			{
				String defaultFileName = FilenameUtils.getBaseName(aItem.getPointingSource());
				String defaultFileType = (aItem.getPointingSourceType() == PointingSource.GASKELL || aItem.getPointingSourceType() == PointingSource.GASKELL_UPDATED) ? "SUM" : "INFO";
				defaultFileType = "INFO";
//				file = CustomFileChooser.showSaveDialog(null, "Save Pointing file as...", defaultFileName + "." + defaultFileType);
				file = new File(outDir, defaultFileName + "." + defaultFileType);

//				String filename = file.getAbsolutePath();
				//What is below was dumb.  Export the file from the cache instead.
				//TODO for now, we don't have a good SUMFileWriter, so export things as INFO files.
//				PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(List.of(aItem));
//				filename = filename.replace(".SUM", ".INFO");
//				InfoFileWriter writer = new InfoFileWriter(filename, pipeline.getRenderableImages().get(0).getPointing(), false);
//				writer.write();

				String pointingFileName = aItem.getPointingSource();
				File pointingFile = FileCache.getFileFromServer(pointingFileName);
				File newPointingFile = new File(outDir, pointingFile.getName());
				FileUtils.copyFile(pointingFile, newPointingFile);

			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(null,
						"Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}

//		// Bail if no items are selected
//		if (aItemL.size() == 0)
//			return;
//
//		File outDir = DirectoryChooser.showOpenDialog(null, "Save FITS/INFO Pairs to Directory...");
//		if (outDir == null)
//			return;
//
//		for (PerspectiveImage aItem : aItemL)
//		{
//			File file = null;
//			try
//			{
//				String path = aItem.getFilename();
//				String extension = path.substring(path.lastIndexOf("."));
//
//				file = new File(outDir, path.substring(path.lastIndexOf("/")) + extension);
//				File fitFile = FileCache.getFileFromServer(path);
//				FileUtil.copyFile(fitFile, file);
//
//				// save info file
//				String defaultFileName = FilenameUtils.getBaseName(aItem.getPointingSource());
//
//				file = new File(outDir, defaultFileName);
//				String filename = file.getAbsolutePath();
//
//				//TODO fix this
//	//			image.saveImageInfo(filename);
//			}
//			catch (Exception ex)
//			{
//				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(null),
//						"Unable to save file to " + file.getAbsolutePath(), "Error Saving File",
//						JOptionPane.ERROR_MESSAGE);
//				ex.printStackTrace();
//			}
//		}
	}

//	public void actionPerformed(ActionEvent e)
//	{
//		File outputDir = DirectoryChooser.showOpenDialog(null, "Save FITS/INFO Pairs to Directory...");
//		if (outputDir == null)
//			return;
////        	if (outputDirChooser.showOpenDialog(ImagePopupMenu.this) == JFileChooser.ERROR_OPTION) return;	//TODO handle errors
//		File outDir = outputDir;
//		for (ImageKeyInterface imageKey : imageKeys)
//		{
//			if (!(this.imagePopupMenu.imageCollection.getImage(imageKey) instanceof PerspectiveImage))
//				continue;
//			File file = null;
//			try
//			{
//				// save fits
//				PerspectiveImage image = (PerspectiveImage) this.imagePopupMenu.imageCollection.getImage(imageKey);
//				String path = image.getFitFileFullPath();
//				String extension = path.substring(path.lastIndexOf("."));
//
//				file = new File(outDir,
//						imageKey.getImageFilename().substring(imageKey.getImageFilename().lastIndexOf("/"))
//								+ extension);
//				File fitFile = FileCache.getFileFromServer(imageKey.getName() + extension);
//				FileUtil.copyFile(fitFile, file);
//				// save info file
//				String fullPathName = image.getFitFileFullPath();
//				if (fullPathName == null)
//					fullPathName = image.getPngFileFullPath();
//				String imageFileName = new File(fullPathName).getName();
//
//				String defaultFileName = null;
//				if (imageFileName != null)
//					defaultFileName = FilenameUtils.getBaseName(imageFileName) + ".INFO";
//
//				file = new File(outDir, defaultFileName);
//				String filename = file.getAbsolutePath();
//				image.saveImageInfo(filename);
//
//			}
//			catch (Exception ex)
//			{
//				JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(this.imagePopupMenu.invoker),
//						"Unable to save file to " + file.getAbsolutePath(), "Error Saving File",
//						JOptionPane.ERROR_MESSAGE);
//				ex.printStackTrace();
//			}
//		}
//	}
}