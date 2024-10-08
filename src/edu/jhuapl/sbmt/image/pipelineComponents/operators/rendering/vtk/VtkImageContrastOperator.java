package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk;

import java.io.IOException;
import java.util.ArrayList;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import vtk.vtkImageData;
import vtk.vtkImageMapToColors;
import vtk.vtkLookupTable;

public class VtkImageContrastOperator//<InputType extends vtkImageData, OutputType extends vtkImageData>
		extends BasePipelineOperator<vtkImageData, vtkImageData>
{
	private IntensityRange displayedRange;
	private double minValue;
	private double maxValue;

	public VtkImageContrastOperator(IntensityRange intensityRange)
	{
		this.displayedRange = intensityRange;

	}

	@Override
	public void processData() throws IOException, Exception
	{
		minValue = inputs.get(0).GetScalarRange()[0];
		maxValue = inputs.get(0).GetScalarRange()[1];
		outputs = new ArrayList<vtkImageData>();
		outputs.add(getImageWithDisplayedRange());
	}

	vtkImageData getImageWithDisplayedRange()
	{
		if (inputs.get(0).GetNumberOfScalarComponents() > 1 ) return inputs.get(0);
		double dx = (maxValue - minValue) / 255.0f;

		double min = minValue;
		double max = maxValue;

		IntensityRange displayedRange = getDisplayedRange();
		min = minValue + displayedRange.min * dx;
		max = minValue + displayedRange.max * dx;
 		// Update the displayed image
		vtkLookupTable lut = new vtkLookupTable();
		lut.SetTableRange(min, max);
		lut.SetValueRange(0.0, 1.0);
		lut.SetHueRange(0.0, 0.0);
		lut.SetSaturationRange(0.0, 0.0);
		lut.SetRampToLinear();
		lut.Build();

//		Colormap colormap = Colormaps.getNewInstanceOfBuiltInColormap(Colormaps.getDefaultColormapName());
//		vtkLookupTable lut = colormap.getLookupTable();
//		lut.SetTableRange(min, max);

		vtkImageMapToColors mapToColors = new vtkImageMapToColors();
		mapToColors.SetInputData(inputs.get(0));
		mapToColors.SetOutputFormatToRGBA();
		mapToColors.SetLookupTable(lut);
		mapToColors.Update();
		vtkImageData mapToColorsOutput = mapToColors.GetOutput();
		return mapToColorsOutput;

	}

	/**
	 * This getter lazily initializes the range field as necessary to ensure
	 * this returns a valid, non-null range as long as the argument is in range
	 * for this image.
	 *
	 * @param slice
	 *            the number of the slice whose displayed range to return.
	 */
	IntensityRange getDisplayedRange()
	{

		if (displayedRange == null)
		{
			displayedRange = new IntensityRange(0, 255);
		}

		return displayedRange;
	}
}
