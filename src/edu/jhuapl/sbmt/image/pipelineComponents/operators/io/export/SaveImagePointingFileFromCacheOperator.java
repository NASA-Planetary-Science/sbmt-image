package edu.jhuapl.sbmt.image.pipelineComponents.operators.io.export;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import edu.jhuapl.saavtk.gui.dialog.DirectoryChooser;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class SaveImagePointingFileFromCacheOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<G1, File>
{

	@Override
	public void processData() throws IOException, Exception
	{
		File file = null;
		try
		{
//			String defaultFileName = FilenameUtils.getBaseName(inputs.get(0).getPointingSource());
//			String defaultFileType = inputs.get(0).getPointingSourceType() == ImageSource.GASKELL ? "SUM" : "INFO";
//			file = CustomFileChooser.showSaveDialog(null, "Save Pointing file as...", defaultFileName + "." + defaultFileType);
//			if (file == null) return;
//
//			String filename = file.getAbsolutePath();
//
//			PerspectiveImageToRenderableImagePipeline pipeline = new PerspectiveImageToRenderableImagePipeline(List.of(inputs.get(0)));
//			InfoFileWriter writer = new InfoFileWriter(filename, pipeline.getRenderableImages().get(0).getPointing(), false);
//			writer.write();
//			outputs.add(file);

			File outDir = DirectoryChooser.showOpenDialog(null, "Save Pointing file to Directory...");
			if (outDir == null)
				return;

			String pointingFileName = inputs.get(0).getPointingSource();
			File pointingFile = FileCache.getFileFromServer(pointingFileName);
			File newPointingFile = new File(outDir, pointingFile.getName());
			//System.out.println("SaveImagePointingFileFromCacheOperator: processData: new pointing file " + newPointingFile.getAbsolutePath());
			FileUtils.copyFile(pointingFile, newPointingFile);

		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null,
					"Unable to save file to " + file.getAbsolutePath(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}
}
 