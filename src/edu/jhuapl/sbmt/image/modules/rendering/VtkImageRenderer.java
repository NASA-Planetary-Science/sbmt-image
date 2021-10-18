package edu.jhuapl.sbmt.image.modules.rendering;

import java.io.IOException;
import java.util.ArrayList;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.util.VtkDataTypes;
import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.PixelDouble;
import edu.jhuapl.sbmt.image.impl.PixelDoubleFactory;
import edu.jhuapl.sbmt.image.pipeline.operator.BasePipelineOperator;

public class VtkImageRenderer//<InputType extends Layer, OutputType extends vtkImageData>
		extends BasePipelineOperator<Layer, vtkImageData>
{
	vtkImageData output;
	PixelDoubleFactory pixelDoubleFactory;

	public VtkImageRenderer()
	{
		pixelDoubleFactory = new PixelDoubleFactory();
	}

	@Override
	public void processData() throws IOException, Exception
	{
		outputs = new ArrayList<vtkImageData>();
		PixelDouble pixel = pixelDoubleFactory.of(0, -Double.NaN, -Double.NaN);
		int layerWidth = inputs.get(0).iSize();
		int layerHeight = inputs.get(0).jSize();
		output = new vtkImageData();
//		 if (transpose)
        output.SetDimensions(layerWidth, layerHeight, 1);
//	        else
//        output.SetDimensions(layerHeight, layerWidth, 1);
        output.SetSpacing(1.0, 1.0, 1.0);
        output.SetOrigin(0.0, 0.0, 0.0);
        output.AllocateScalars(VtkDataTypes.VTK_FLOAT, 1);

		for (int i = 0; i < layerWidth; i++)
		{
			for (int j = 0; j < layerHeight; j++)
			{
				inputs.get(0).get(i, j, pixel);
				output.SetScalarComponentFromDouble(i, j, 0, 0, pixel.get());
			}
		}
		outputs.add(output);
	}
}
