package edu.jhuapl.sbmt.image.pipelineComponents.operators.io.export;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Triple;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingDelta;
import edu.jhuapl.sbmt.image.model.SpacecraftPointingState;
import edu.jhuapl.sbmt.image.util.ModifiedInfoFileWriter;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class SaveModifiedImagePointingFileToCacheOperator<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends BasePipelineOperator<Triple<G1, SpacecraftPointingState, SpacecraftPointingDelta>, File>
{
	ModifiedInfoFileWriter<G1> writer = null;
	File file = null;
	private boolean isCustom = false;
	
	public SaveModifiedImagePointingFileToCacheOperator(boolean isCustom)
	{
		this.isCustom = isCustom;
	}
	
	
	@Override
	public void processData() throws IOException, Exception
	{
		try
		{
			Triple<G1, SpacecraftPointingState, SpacecraftPointingDelta> input = inputs.get(0);
			G1 image = input.getLeft();
			SpacecraftPointingState state = input.getMiddle();
			SpacecraftPointingDelta delta = input.getRight();
			File cachedFile = null;
			if (isCustom)
			{
				cachedFile = new File(image.getPointingSource());
				if (cachedFile.exists() == false)
					cachedFile = FileCache.getFileFromServer(image.getPointingSource());
			}
			else
				cachedFile = FileCache.getFileFromServer(image.getPointingSource());
			String nameNoExtension = FilenameUtils.removeExtension(cachedFile.getAbsolutePath());
			file = new File(nameNoExtension + ".INFO");
			writer = new ModifiedInfoFileWriter<G1>(file.getAbsolutePath(), image, state, delta, true);
			writer.write();
			outputs.add(new File(file.getAbsolutePath() + ".adjusted"));
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null,
					"Unable to save file to " + file, "Error Saving Modified File", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}
}
