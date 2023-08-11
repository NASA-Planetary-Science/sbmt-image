package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.color;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import vtk.vtkImageData;

import edu.jhuapl.saavtk.util.ImageDataUtil;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.saavtk.util.VtkDataTypes;
import edu.jhuapl.sbmt.image.model.Chromatism;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.impl.LayerDoubleTransformFactory;
import edu.jhuapl.sbmt.layer.impl.ValidityCheckerDoubleFactory;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;

public class RGBALayerMergeOperator extends BasePipelineOperator<Layer, vtkImageData>
{
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
		Chromatism chromatism = Chromatism.POLY;
		IntensityRange redIntensityRange = new IntensityRange(0, 255);
	    IntensityRange greenIntensityRange = new IntensityRange(0, 255);
	    IntensityRange blueIntensityRange = new IntensityRange(0, 255);

	    double redScale = 1.0;
	    double greenScale = 1.0;
	    double blueScale = 1.0;

		vtkImageData colorImage = new vtkImageData();
        int imageWidth = layer.iSize();
        int imageHeight = layer.jSize();
        colorImage.SetDimensions(imageWidth, imageHeight, 1);
        colorImage.SetSpacing(1.0, 1.0, 1.0);
        colorImage.SetOrigin(0.0, 0.0, 0.0);
        colorImage.AllocateScalars(VtkDataTypes.VTK_UNSIGNED_CHAR, 3);

        List<vtkImageData> redImageData = Lists.newArrayList();
        Just.of(new LayerDoubleTransformFactory().slice(0, -Double.NaN).apply(layer))
		.operate(new VtkImageRendererOperator())
		.subscribe(Sink.of(redImageData))
		.run();

        List<vtkImageData> greenImageData = Lists.newArrayList();
        Just.of(new LayerDoubleTransformFactory().slice(1, -Double.NaN).apply(layer))
		.operate(new VtkImageRendererOperator())
		.subscribe(Sink.of(greenImageData))
		.run();

        //new ValidityCheckerDoubleFactory().checker2d(image.getFillValues())
        
        List<vtkImageData> blueImageData = Lists.newArrayList();
        Just.of(new LayerDoubleTransformFactory().slice(2, -Double.NaN).apply(layer))
		.operate(new VtkImageRendererOperator())
		.subscribe(Sink.of(blueImageData))
		.run();

		double[] redRange = redImageData.get(0).GetScalarRange();
        double[] greenRange = greenImageData.get(0).GetScalarRange();
        double[] blueRange = blueImageData.get(0).GetScalarRange();
        
//        System.out.println("RGBALayerMergeOperator: processData: red range " + Arrays.toString(redRange));
//        System.out.println("RGBALayerMergeOperator: processData: green range " + Arrays.toString(greenRange));
//        System.out.println("RGBALayerMergeOperator: processData: blue range " + Arrays.toString(blueRange));

        float[][] redPixelData = ImageDataUtil.vtkImageDataToArray2D(redImageData.get(0), 0);
		float[][] greenPixelData = ImageDataUtil.vtkImageDataToArray2D(greenImageData.get(0), 0);
		float[][] bluePixelData = ImageDataUtil.vtkImageDataToArray2D(blueImageData.get(0), 0);


        double redfullRange = redRange[1] - redRange[0];
        double reddx = redfullRange / 255.0;
        double redmin = redRange[0] + redIntensityRange.min*reddx;
        double redmax = redRange[0] + redIntensityRange.max*reddx;
        double redstretchRange = redmax - redmin;

        double greenfullRange = greenRange[1] - greenRange[0];
        double greendx = greenfullRange / 255.0;
        double greenmin = greenRange[0] + greenIntensityRange.min*greendx;
        double greenmax = greenRange[0] + greenIntensityRange.max*greendx;
        double greenstretchRange = greenmax - greenmin;

        double bluefullRange = blueRange[1] - blueRange[0];
        double bluedx = bluefullRange / 255.0;
        double bluemin = blueRange[0] + blueIntensityRange.min*bluedx;
        double bluemax = blueRange[0] + blueIntensityRange.max*bluedx;
        double bluestretchRange = bluemax - bluemin;


        for (int i=0; i<imageHeight; ++i)
        {
            for (int j=0; j<imageWidth; ++j)
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
        }

        outputs.add(colorImage);
	}
}
