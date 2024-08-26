package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering;

import java.io.IOException;

import edu.jhuapl.sbmt.image.model.ImageBinPadding;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import vtk.vtkImageConstantPad;
import vtk.vtkImageData;
import vtk.vtkImageReslice;
import vtk.vtkImageTranslateExtent;

public class PadImageOperator extends BasePipelineOperator<vtkImageData, vtkImageData>
{
	private int xShift, yShift, xSize, ySize, xFinalSize, yFinalSize;
	private double xSpacing, ySpacing, zSpacing;

	public PadImageOperator(int xShift, int yShift, int xSize, int ySize)
	{
		this.xShift = xShift;
		this.yShift = yShift;
		this.xSize = xSize;
		this.ySize = ySize;
	}

	public PadImageOperator(ImageBinPadding imageBinPadding, int binning)
	{
		this.xShift = imageBinPadding.binTranslations.get(binning).xTranslation();
		this.yShift = imageBinPadding.binTranslations.get(binning).yTranslation();
		this.xSize = imageBinPadding.binExtents.get(binning).xExtent();
		this.ySize = imageBinPadding.binExtents.get(binning).yExtent();
		this.xFinalSize = imageBinPadding.binExtents.get(binning).xFinalExtent();
		this.yFinalSize = imageBinPadding.binExtents.get(binning).yFinalExtent();
		this.xSpacing = imageBinPadding.binSpacings.get(binning).xSpacing();
		this.ySpacing = imageBinPadding.binSpacings.get(binning).ySpacing();
		this.zSpacing = imageBinPadding.binSpacings.get(binning).zSpacing();
	}

	@Override
	public void processData() throws IOException, Exception
	{
		vtkImageData rawImage = inputs.get(0);
		if (rawImage.GetDimensions()[0] != xSize || rawImage.GetDimensions()[1] != ySize)
		{
			vtkImageTranslateExtent translateExtent = new vtkImageTranslateExtent();
			translateExtent.SetInputData(rawImage);
			translateExtent.SetTranslation(xShift, yShift, 0);
			translateExtent.Update();

			vtkImageConstantPad pad = new vtkImageConstantPad();
			pad.SetInputConnection(translateExtent.GetOutputPort());
			pad.SetOutputWholeExtent(0, xSize-1, 0, ySize-1, 0, 0);
			pad.Update();

			vtkImageData padOutput = pad.GetOutput();
			rawImage.DeepCopy(padOutput);

			// shift origin back to zero
			rawImage.SetOrigin(0.0, 0.0, 0.0);

			rawImage.SetSpacing(xSpacing, ySpacing, zSpacing);
			vtkImageReslice resample = new vtkImageReslice();
            resample.SetInputData(rawImage);
            resample.InterpolateOff();
            resample.SetOutputExtent(0, xFinalSize-1, 0, yFinalSize-1, 0, 0);
            resample.SetOutputOrigin(0.0, 0.0, 0.0);
            resample.SetOutputSpacing(1.0, 1.0, 1.0);
            resample.Update();
            vtkImageData resampleOutput = resample.GetOutput();
            rawImage.DeepCopy(resampleOutput);

		}

		outputs.add(rawImage);
	}

	@Override
	public String toString()
	{
		return String.format(
				"PadImageOperator [xShift=%s, yShift=%s, xSize=%s, ySize=%s, xFinalSize=%s, yFinalSize=%s, xSpacing=%s, ySpacing=%s, zSpacing=%s]",
				xShift, yShift, xSize, ySize, xFinalSize, yFinalSize, xSpacing, ySpacing, zSpacing);
	}
}
