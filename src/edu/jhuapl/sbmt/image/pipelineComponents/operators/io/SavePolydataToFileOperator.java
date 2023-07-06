package edu.jhuapl.sbmt.image.pipelineComponents.operators.io;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;

import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;

import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class SavePolydataToFileOperator extends BasePipelineOperator<Pair<String, vtkPolyData>, Void>
{
	@Override
	public void processData() throws IOException, Exception
	{
		String filename = inputs.get(0).getLeft();
		new File(filename).getParentFile().mkdirs();
        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInputData(inputs.get(0).getRight());
        writer.SetFileName(new File(filename).toString());
        writer.SetFileTypeToBinary();
        writer.Write();
	}
}
