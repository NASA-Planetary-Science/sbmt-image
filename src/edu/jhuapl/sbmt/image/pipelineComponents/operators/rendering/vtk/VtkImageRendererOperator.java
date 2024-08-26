package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import edu.jhuapl.saavtk.util.ThreadService;
import edu.jhuapl.saavtk.util.VtkDataTypes;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.PixelDouble;
import edu.jhuapl.sbmt.layer.api.PixelVector;
import edu.jhuapl.sbmt.layer.impl.PixelDoubleFactory;
import edu.jhuapl.sbmt.layer.impl.PixelVectorDoubleFactory;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import vtk.vtkImageData;

public class VtkImageRendererOperator extends BasePipelineOperator<Layer, vtkImageData>
{
	vtkImageData output;
	vtkImageData redOutput;
	vtkImageData greenOutput;
	vtkImageData blueOutput;
	PixelDoubleFactory pixelDoubleFactory;
	int layerWidth, layerDepth, layerHeight;
	private boolean invertY = false;

	public VtkImageRendererOperator()
	{
		this(false);
	}

	public VtkImageRendererOperator(boolean invertY)
	{
		pixelDoubleFactory = new PixelDoubleFactory();
		this.invertY = invertY;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		outputs = new ArrayList<vtkImageData>();
		layerWidth = inputs.get(0).iSize();
		layerHeight = inputs.get(0).jSize();
		layerDepth = inputs.get(0).dataSizes().get(0);
		output = new vtkImageData();
		output.SetSpacing(1.0, 1.0, 1.0);
		output.SetOrigin(0.0, 0.0, 0.0);
		output.SetDimensions(layerWidth, layerHeight, layerDepth);
		ThreadService.initialize(60);
		final List<Future<Void>> resultList;
		List<Callable<Void>> taskList = new ArrayList<>();

		if (layerDepth == 1)
		{
			output.AllocateScalars(VtkDataTypes.VTK_FLOAT, 1);
			PixelDouble pixel = pixelDoubleFactory.of(0, -Double.NaN, -Double.NaN);
			for (int i = 0; i < layerWidth; i++)
			{
				for (int j = 0; j < layerHeight; j++)
				{
					inputs.get(0).get(i, j, pixel);
					output.SetScalarComponentFromDouble(i, invertY ? layerHeight - j - 1 : j, 0, 0, pixel.get());
				}
//				Callable<Void> task = new LayerRowToImageDataRow2DTask(i);
//				taskList.add(task);
			}
//			resultList = ThreadService.submitAll(taskList);
			
			outputs.add(output);
		}
		else if (layerDepth >= 3)
		{
			redOutput = new vtkImageData();
			redOutput.SetSpacing(1.0, 1.0, 1.0);
			redOutput.SetOrigin(0.0, 0.0, 0.0);
			redOutput.SetDimensions(layerWidth, layerHeight, 1);
			redOutput.AllocateScalars(VtkDataTypes.VTK_FLOAT, 1);
			
			greenOutput = new vtkImageData();
			greenOutput.SetSpacing(1.0, 1.0, 1.0);
			greenOutput.SetOrigin(0.0, 0.0, 0.0);
			greenOutput.SetDimensions(layerWidth, layerHeight, 1);
			greenOutput.AllocateScalars(VtkDataTypes.VTK_FLOAT, 1);
			
			blueOutput = new vtkImageData();
			blueOutput.SetSpacing(1.0, 1.0, 1.0);
			blueOutput.SetOrigin(0.0, 0.0, 0.0);
			blueOutput.SetDimensions(layerWidth, layerHeight, 1);
			blueOutput.AllocateScalars(VtkDataTypes.VTK_FLOAT, 1);
			
			output.AllocateScalars(VtkDataTypes.VTK_UNSIGNED_CHAR, layerDepth);
			for (int i = 0; i < layerWidth; i++)
			{
				Callable<Void> task = new LayerRowToImageDataRow3DTask(i);
				taskList.add(task);
			}

			resultList = ThreadService.submitAll(taskList);
			
			outputs.add(output);
			outputs.add(redOutput);
			outputs.add(greenOutput);
			outputs.add(blueOutput);
		}
		
	}

	private class LayerRowToImageDataRow2DTask implements Callable<Void>
	{
		private int i;
		PixelDouble pixel = pixelDoubleFactory.of(0, -Double.NaN, -Double.NaN);
		
		public LayerRowToImageDataRow2DTask(int i)
		{
			this.i = i;
		}

		@Override
		public Void call() throws Exception
		{
			for (int j = 0; j < layerHeight; j++)
			{
				inputs.get(0).get(i, j, pixel);
				output.SetScalarComponentFromDouble(i, invertY ? layerHeight - j - 1 : j, 0, 0, pixel.get());
			}
			return null;
		}
	}

	private class LayerRowToImageDataRow3DTask implements Callable<Void>
	{
		private int i;

		public LayerRowToImageDataRow3DTask(int i)
		{
			this.i = i;
		}

		@Override
		public Void call() throws Exception
		{
			PixelVector pixel = new PixelVectorDoubleFactory().of(layerDepth, -Double.NaN, -Double.NaN);
			for (int j = 0; j < layerHeight; j++)
			{
				inputs.get(0).get(i, j, pixel);
				for (int k = 0; k < layerDepth; k++)
				{
					PixelDouble vecPixel = (PixelDouble) pixel.get(k);
					int yIndex = invertY ? layerHeight - j - 1 : j;
					output.SetScalarComponentFromFloat(i, yIndex, 0, k, (float)vecPixel.get());
					if (k == 0) redOutput.SetScalarComponentFromFloat(i, yIndex, 0, 0, (float)vecPixel.get());
					if (k == 1) greenOutput.SetScalarComponentFromFloat(i, yIndex, 0, 0, (float)vecPixel.get());
					if (k == 2) blueOutput.SetScalarComponentFromFloat(i, yIndex, 0, 0, (float)vecPixel.get());
				}
			}
			
			return null;
		}
	}
}
