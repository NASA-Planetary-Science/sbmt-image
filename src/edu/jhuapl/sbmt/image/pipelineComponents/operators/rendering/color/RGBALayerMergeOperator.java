package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.saavtk.util.ImageDataUtil;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.saavtk.util.ThreadService;
import edu.jhuapl.saavtk.util.VtkDataTypes;
import edu.jhuapl.sbmt.image.model.Chromatism;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import vtk.vtkImageData;

public class RGBALayerMergeOperator extends BasePipelineOperator<Layer, vtkImageData>
{
	int imageWidth, imageDepth, imageHeight;
	float[][] redPixelData, greenPixelData, bluePixelData;
    double redmin, redmax, greenmin, greenmax, bluemin, bluemax;
    double redstretchRange, greenstretchRange, bluestretchRange;
    double redScale, greenScale, blueScale;
    vtkImageData colorImage;
    Chromatism chromatism;
    Logger log = Logger.getAnonymousLogger();
	
	public RGBALayerMergeOperator()
	{
	}

	@Override
	public void processData() throws IOException, Exception
	{
		List<vtkImageData> imageDatas = Lists.newArrayList();
		Layer layer = inputs.get(0);
		List<vtkImageData> imageData = Lists.newArrayList();
		Just.of(layer)
			.operate(new VtkImageRendererOperator())
			.subscribe(Sink.of(imageData))
			.run();
		imageDatas.addAll(imageData);
		chromatism = Chromatism.POLY;
		IntensityRange redIntensityRange = new IntensityRange(0, 255);
	    IntensityRange greenIntensityRange = new IntensityRange(0, 255);
	    IntensityRange blueIntensityRange = new IntensityRange(0, 255);

	    redScale = 1.0;
	    greenScale = 1.0;
	    blueScale = 1.0;

		colorImage = new vtkImageData();
        imageWidth = layer.iSize();
        imageHeight = layer.jSize();
        colorImage.SetDimensions(imageWidth, imageHeight, 1);
        colorImage.SetSpacing(1.0, 1.0, 1.0);
        colorImage.SetOrigin(0.0, 0.0, 0.0);
        colorImage.AllocateScalars(VtkDataTypes.VTK_UNSIGNED_CHAR, 3);

        List<vtkImageData> redImageData = Lists.newArrayList();
        redImageData.add(imageDatas.get(1));

        List<vtkImageData> greenImageData = Lists.newArrayList();
        greenImageData.add(imageDatas.get(2));

        List<vtkImageData> blueImageData = Lists.newArrayList();
        blueImageData.add(imageDatas.get(3));

		double[] redRange = redImageData.get(0).GetScalarRange();
        double[] greenRange = greenImageData.get(0).GetScalarRange();
        double[] blueRange = blueImageData.get(0).GetScalarRange();

        redPixelData = ImageDataUtil.vtkImageDataToArray2D(redImageData.get(0), 0);
		greenPixelData = ImageDataUtil.vtkImageDataToArray2D(greenImageData.get(0), 0);
		bluePixelData = ImageDataUtil.vtkImageDataToArray2D(blueImageData.get(0), 0);		

        double redfullRange = redRange[1] - redRange[0];
        double reddx = redfullRange / 255.0;
        redmin = redRange[0] + redIntensityRange.min*reddx;
        redmax = redRange[0] + redIntensityRange.max*reddx;
        redstretchRange = redmax - redmin;

        double greenfullRange = greenRange[1] - greenRange[0];
        double greendx = greenfullRange / 255.0;
        greenmin = greenRange[0] + greenIntensityRange.min*greendx;
        greenmax = greenRange[0] + greenIntensityRange.max*greendx;
        greenstretchRange = greenmax - greenmin;

        double bluefullRange = blueRange[1] - blueRange[0];
        double bluedx = bluefullRange / 255.0;
        bluemin = blueRange[0] + blueIntensityRange.min*bluedx;
        bluemax = blueRange[0] + blueIntensityRange.max*bluedx;
        bluestretchRange = bluemax - bluemin;
        
        ThreadService.initialize(60);
		final List<Future<Void>> resultList;
		List<Callable<Void>> taskList = new ArrayList<>();
        for (int i = 0; i < imageHeight; i++)
		{
			Callable<Void> task = new LayerRowToImageDataRow2DTask(i);
			taskList.add(task);
		}

		resultList = ThreadService.submitAll(taskList);
        outputs.add(colorImage);
	}
	
	private class LayerRowToImageDataRow2DTask implements Callable<Void>
	{
		private int i;
		
		public LayerRowToImageDataRow2DTask(int i)
		{
			this.i = i;
		}

		@Override
		public synchronized Void call() throws Exception
		{
			for (int j = 0; j < imageWidth; j++)
			{
				double redValue = redPixelData[j][i];
				double greenValue = greenPixelData[j][i];
				double blueValue = bluePixelData[j][i];

				if (redValue < redmin)
					redValue = redmin;
				if (redValue > redmax)
					redValue = redmax;

				if (greenValue < greenmin)
					greenValue = greenmin;
				if (greenValue > greenmax)
					greenValue = greenmax;

				if (blueValue < bluemin)
					blueValue = bluemin;
				if (blueValue > bluemax)
					blueValue = bluemax;

				double redComponent = 255.0 * redScale * (redValue - redmin) / redstretchRange;
				double greenComponent = 255.0 * greenScale * (greenValue - greenmin) / greenstretchRange;
				double blueComponent = 255.0 * blueScale * (blueValue - bluemin) / bluestretchRange;

				if (chromatism == Chromatism.MONO_RED)
					greenComponent = blueComponent = redComponent;
				else if (chromatism == Chromatism.MONO_GREEN)
					blueComponent = redComponent = greenComponent;
				else if (chromatism == Chromatism.MONO_BLUE)
					greenComponent = redComponent = blueComponent;
				
				colorImage.SetScalarComponentFromFloat(j, i, 0, 0, (float)redComponent);
                colorImage.SetScalarComponentFromFloat(j, i, 0, 1, (float)greenComponent);
                colorImage.SetScalarComponentFromFloat(j, i, 0, 2, (float)blueComponent);
			}
			return null;
		}
	}
}
