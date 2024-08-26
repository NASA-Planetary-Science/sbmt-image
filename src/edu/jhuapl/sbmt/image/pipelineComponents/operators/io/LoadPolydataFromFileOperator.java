package edu.jhuapl.sbmt.image.pipelineComponents.operators.io;

import java.io.File;
import java.io.IOException;

import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.NonexistentRemoteFile;
import edu.jhuapl.saavtk.util.UnauthorizedAccessException;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import vtk.vtkPolyData;
import vtk.vtkPolyDataReader;

public class LoadPolydataFromFileOperator extends BasePipelineOperator<String, vtkPolyData>
{
	@Override
	public void processData() throws IOException, Exception
	{
		String imageDataFileName;
		vtkPolyDataReader reader = new vtkPolyDataReader();
		if (inputs.get(0).contains("models"))
		{
			if (!(new File(inputs.get(0)).exists())) return;
 			reader.SetFileName(inputs.get(0));
		    reader.Update();
		    vtkPolyData imageData = reader.GetOutput();
		    outputs.add(imageData);
		    return;
		}
		if ((inputs.get(0).split("cache/2/").length != 2) && (inputs.get(0).split("cache/").length != 2)) return;
		int numSegments = inputs.get(0).split("cache/2/").length;
		if (numSegments == 2)
			imageDataFileName = inputs.get(0).split("cache/2/")[1];
		else
			imageDataFileName = inputs.get(0).split("cache")[1];
		File file = null;
		
		try
		{
			file = FileCache.getFileFromServer(imageDataFileName);
		}
		catch (UnauthorizedAccessException | NonexistentRemoteFile e)
		{
		    // Report this but continue.
//		    e.printStackTrace();
		    file = null;
		}
		catch (Exception e)
		{
		    // Ignore this one.
//			e.printStackTrace();
		    file = null;
		}
		
		if (file != null && file.exists())
		{
		    reader.SetFileName(file.getAbsolutePath());
		    reader.Update();
		    vtkPolyData imageData = reader.GetOutput();
		    outputs.add(imageData);
		}
		else if (new File(inputs.get(0)).exists())
		{
		    reader.SetFileName(inputs.get(0));
		    reader.Update();
		    vtkPolyData imageData = reader.GetOutput();
		    outputs.add(imageData);
		}
		
	}
}
