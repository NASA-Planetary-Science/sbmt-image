package edu.jhuapl.sbmt.image.pipelineComponents.operators.io;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import vtk.vtkImageData;
import vtk.vtkXMLImageDataWriter;

public class SaveImageDataToFileOperator extends BasePipelineOperator<Pair<String, vtkImageData>, Void>
{
	@Override
	public void processData() throws IOException, Exception
	{
		String filename = inputs.get(0).getLeft();
		new File(filename).getParentFile().mkdirs();
		vtkXMLImageDataWriter writer = new vtkXMLImageDataWriter();
        writer.SetInputData(inputs.get(0).getRight());
        writer.SetFileName(new File(filename).toString());
        writer.Write();
	}
}
