package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk;

import java.io.IOException;
import java.util.ArrayList;

import vtk.vtkImageCanvasSource2D;
import vtk.vtkImageData;
import vtk.vtkImageMask;

import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class VtkImageVtkMaskingOperator //<vtkImageData, vtkImageData>
		extends BasePipelineOperator<vtkImageData, vtkImageData>
{
	private int[] masking;
	private int imageWidth, imageHeight;
	private vtkImageCanvasSource2D maskSource;

	public VtkImageVtkMaskingOperator(int[] masking)
	{
		this.masking = masking;
		maskSource = new vtkImageCanvasSource2D();
		maskSource.SetScalarTypeToUnsignedChar();
		maskSource.SetNumberOfScalarComponents(1);
	}

	@Override
	public void processData() throws IOException, Exception
	{
		outputs = new ArrayList<vtkImageData>();
		imageWidth = (int) inputs.get(0).GetBounds()[1]+1;
		imageHeight = (int) inputs.get(0).GetBounds()[3]+1;
		maskSource.SetExtent(0, imageWidth - 1, 0, imageHeight - 1, 0, 0);
		int topMask = masking[3]; 		//3    was 2
		int rightMask = masking[1];		//0	   was 3
		int bottomMask = masking[2];	//1    was 0
		int leftMask = masking[0];		//2    was 1
		// Initialize the mask to black which masks out the image
		maskSource.SetDrawColor(0.0, 0.0, 0.0, 0.0);
		maskSource.FillBox(0, imageWidth - 1, 0, imageHeight - 1);
		// Create a square inside mask which passes through the image.
		maskSource.SetDrawColor(255.0, 255.0, 255.0, 255.0);
		maskSource.FillBox(leftMask, imageWidth - 1 - rightMask, bottomMask, imageHeight - 1 - topMask);
		maskSource.Update();

		vtkImageData maskSourceOutput = maskSource.GetOutput();

        vtkImageMask maskFilter = new vtkImageMask();
        maskFilter.SetImageInputData(inputs.get(0));
        maskFilter.SetMaskInputData(maskSourceOutput);
        maskFilter.Update();

        vtkImageData maskFilterOutput = maskFilter.GetOutput();

        outputs.add(maskFilterOutput);
	}
}