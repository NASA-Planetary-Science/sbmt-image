package edu.jhuapl.sbmt.image.pipelineComponents.operators.io.export;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class SaveImageFileFromCacheOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<G1, File>
{
	String outputDir = null;

	public SaveImageFileFromCacheOperator()
	{

	}

	public SaveImageFileFromCacheOperator(String outputDir)
	{
		this.outputDir = outputDir;
	}


	@Override
	public void processData() throws IOException, Exception
	{
		File file = null;
		try
		{
			String path = inputs.get(0).getFilename();
			String extension = FilenameUtils.getExtension(path);
			String imageFileName = FilenameUtils.getBaseName(path);

			if (outputDir == null)
				file = CustomFileChooser.showSaveDialog(null, "Save FITS image", imageFileName,
					extension);
			else
				file = new File(outputDir, imageFileName + "." + extension);
			if (file != null)
			{
				File fitFile = FileCache.getFileFromServer(inputs.get(0).getFilename());
				FileUtil.copyFile(fitFile, file);
				outputs.add(file);
			}
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null,
					"Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}
}
