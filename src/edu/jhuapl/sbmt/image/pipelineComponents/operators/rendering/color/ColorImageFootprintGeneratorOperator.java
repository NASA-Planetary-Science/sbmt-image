package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.ImageDataUtil;
import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.VtkDataTypes;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.image.model.Chromatism;
import edu.jhuapl.sbmt.image.model.IRenderableImage;
import edu.jhuapl.sbmt.image.model.NoOverlapException;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.pointedImage.RenderablePointedImage;
import edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.vtk.VtkImageRendererOperator;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.pipeline.publisher.Just;
import edu.jhuapl.sbmt.pipeline.subscriber.Sink;
import vtk.vtkGenericCell;
import vtk.vtkImageData;
import vtk.vtkPolyData;
import vtk.vtksbCellLocator;

public class ColorImageFootprintGeneratorOperator extends BasePipelineOperator<IRenderableImage, Pair<vtkImageData, vtkPolyData>>
{
	private List<SmallBodyModel> smallBodyModels;

	public ColorImageFootprintGeneratorOperator(List<SmallBodyModel> smallBodyModels)
	{
		this.smallBodyModels = smallBodyModels;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		Chromatism chromatism = Chromatism.POLY;
		IntensityRange redIntensityRange = new IntensityRange(0, 255);
	    IntensityRange greenIntensityRange = new IntensityRange(0, 255);
	    IntensityRange blueIntensityRange = new IntensityRange(0, 255);

	    double redScale = 1.0;
	    double greenScale = 1.0;
	    double blueScale = 1.0;

		RenderablePointedImage redImage =  (RenderablePointedImage)inputs.get(0);
		RenderablePointedImage greenImage =  (RenderablePointedImage)inputs.get(1);
		RenderablePointedImage blueImage = (RenderablePointedImage)inputs.get(2);

		List<vtkImageData> imageDatas = Lists.newArrayList();
		for (IRenderableImage image : inputs)
		{
			List<vtkImageData> imageData = Lists.newArrayList();
			Just.of(image.getLayer())
				.operate(new VtkImageRendererOperator())
				.subscribe(Sink.of(imageData))
				.run();
			imageDatas.addAll(imageData);
		}

		float[][] redPixelData = ImageDataUtil.vtkImageDataToArray2D(imageDatas.get(0), 0);
		float[][] greenPixelData = ImageDataUtil.vtkImageDataToArray2D(imageDatas.get(1), 0);
		float[][] bluePixelData = ImageDataUtil.vtkImageDataToArray2D(imageDatas.get(2), 0);


		vtkImageData colorImage = new vtkImageData();
        int imageWidth = redImage.getImageWidth();
        int imageHeight = redImage.getImageHeight();
        colorImage.SetDimensions(imageWidth, imageHeight, 1);
        colorImage.SetSpacing(1.0, 1.0, 1.0);
        colorImage.SetOrigin(0.0, 0.0, 0.0);
        colorImage.AllocateScalars(VtkDataTypes.VTK_UNSIGNED_CHAR, 3);

        double[] spacecraftPositionAdjustedRed = redImage.getPointing().getSpacecraftPosition();
    	double[] frustum1AdjustedRed = redImage.getPointing().getFrustum1();
    	double[] frustum2AdjustedRed = redImage.getPointing().getFrustum2();
    	double[] frustum3AdjustedRed = redImage.getPointing().getFrustum3();
    	double[] frustum4AdjustedRed = redImage.getPointing().getFrustum4();
    	Frustum redFrustum = new Frustum(spacecraftPositionAdjustedRed,
						    			frustum1AdjustedRed,
						    			frustum3AdjustedRed,
						    			frustum4AdjustedRed,
						    			frustum2AdjustedRed);

    	double[] spacecraftPositionAdjustedGreen = greenImage.getPointing().getSpacecraftPosition();
    	double[] frustum1AdjustedGreen = greenImage.getPointing().getFrustum1();
    	double[] frustum2AdjustedGreen = greenImage.getPointing().getFrustum2();
    	double[] frustum3AdjustedGreen = greenImage.getPointing().getFrustum3();
    	double[] frustum4AdjustedGreen = greenImage.getPointing().getFrustum4();
    	Frustum greenFrustum = new Frustum(spacecraftPositionAdjustedGreen,
						    			frustum1AdjustedGreen,
						    			frustum3AdjustedGreen,
						    			frustum4AdjustedGreen,
						    			frustum2AdjustedGreen);

    	double[] spacecraftPositionAdjustedBlue = blueImage.getPointing().getSpacecraftPosition();
    	double[] frustum1AdjustedBlue = blueImage.getPointing().getFrustum1();
    	double[] frustum2AdjustedBlue = blueImage.getPointing().getFrustum2();
    	double[] frustum3AdjustedBlue = blueImage.getPointing().getFrustum3();
    	double[] frustum4AdjustedBlue = blueImage.getPointing().getFrustum4();
    	Frustum blueFrustum = new Frustum(spacecraftPositionAdjustedBlue,
						    			frustum1AdjustedBlue,
						    			frustum3AdjustedBlue,
						    			frustum4AdjustedBlue,
						    			frustum2AdjustedBlue);

        double[] redRange = imageDatas.get(0).GetScalarRange();
        double[] greenRange = imageDatas.get(1).GetScalarRange();
        double[] blueRange = imageDatas.get(2).GetScalarRange();

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

        List<Frustum> frustums = new ArrayList<Frustum>();
        frustums.add(redFrustum);
        frustums.add(greenFrustum);
        frustums.add(blueFrustum);

        vtkPolyData shiftedFootprint;
        for (SmallBodyModel smallBodyModel : smallBodyModels)
        {
        	shiftedFootprint = new vtkPolyData();
	        vtkPolyData footprint = smallBodyModel.computeMultipleFrustumIntersection(frustums);

	        if (footprint == null)
	            throw new NoOverlapException();

	        // Need to clear out scalar data since if coloring data is being shown,
	        // then the color might mix-in with the image.
	        footprint.GetCellData().SetScalars(null);
	        footprint.GetPointData().SetScalars(null);

	        int IMAGE_WIDTH = redImage.getImageWidth();
	        int IMAGE_HEIGHT = redImage.getImageHeight();

	        PolyDataUtil.generateTextureCoordinates(redFrustum, IMAGE_WIDTH, IMAGE_HEIGHT, footprint);

	        shiftedFootprint.DeepCopy(footprint);
	        PolyDataUtil.shiftPolyDataInNormalDirection(shiftedFootprint, 0.0001);	//TODO: was getOffset()

	        // Now compute a color image with each channel one of these images.
	        // To do that go through each pixel of the red image, and intersect a ray into the asteroid in
	        // the direction of that pixel. Then compute the texture coordinates that the intersection
	        // point would have for the green and blue images. Do linear interpolation in
	        // the green and blue images to compute the green and blue channels.

	        vtksbCellLocator cellLocator = new vtksbCellLocator();
	        cellLocator.SetDataSet(footprint);
	        cellLocator.CacheCellBoundsOn();
	        cellLocator.AutomaticOn();
	        //cellLocator.SetMaxLevel(10);
	        //cellLocator.SetNumberOfCellsPerNode(15);
	        cellLocator.BuildLocator();

	        vtkGenericCell cell = new vtkGenericCell();

	        double[] spacecraftPosition = redFrustum.origin;
	        double[] frustum1 = redFrustum.ul;
	        double[] frustum2 = redFrustum.lr;
	        double[] frustum3 = redFrustum.ur;

	        double[] corner1 = {
	                spacecraftPosition[0] + frustum1[0],
	                spacecraftPosition[1] + frustum1[1],
	                spacecraftPosition[2] + frustum1[2]
	        };
	        double[] corner2 = {
	                spacecraftPosition[0] + frustum2[0],
	                spacecraftPosition[1] + frustum2[1],
	                spacecraftPosition[2] + frustum2[2]
	        };
	        double[] corner3 = {
	                spacecraftPosition[0] + frustum3[0],
	                spacecraftPosition[1] + frustum3[1],
	                spacecraftPosition[2] + frustum3[2]
	        };
	        double[] vec12 = {
	                corner2[0] - corner1[0],
	                corner2[1] - corner1[1],
	                corner2[2] - corner1[2]
	        };
	        double[] vec13 = {
	                corner3[0] - corner1[0],
	                corner3[1] - corner1[1],
	                corner3[2] - corner1[2]
	        };


	        double scdist = MathUtil.vnorm(spacecraftPosition);

	        for (int i=0; i<IMAGE_HEIGHT; ++i)
	        {
	            // Compute the vector on the left of the row.
	            double fracHeight = ((double)i / (double)(IMAGE_HEIGHT-1));
	            double[] left = {
	                    corner1[0] + fracHeight*vec13[0],
	                    corner1[1] + fracHeight*vec13[1],
	                    corner1[2] + fracHeight*vec13[2]
	            };

	            for (int j=0; j<IMAGE_WIDTH; ++j)
	            {
	                double fracWidth = ((double)j / (double)(IMAGE_WIDTH-1));
	                double[] vec = {
	                        left[0] + fracWidth*vec12[0],
	                        left[1] + fracWidth*vec12[1],
	                        left[2] + fracWidth*vec12[2]
	                };
	                vec[0] -= spacecraftPosition[0];
	                vec[1] -= spacecraftPosition[1];
	                vec[2] -= spacecraftPosition[2];
	                MathUtil.unorm(vec, vec);

	                double[] lookPt = {
	                        spacecraftPosition[0] + 2.0*scdist*vec[0],
	                        spacecraftPosition[1] + 2.0*scdist*vec[1],
	                        spacecraftPosition[2] + 2.0*scdist*vec[2]
	                };

	                double tol = 1e-6;
	                double[] t = new double[1];
	                double[] x = new double[3];
	                double[] pcoords = new double[3];
	                int[] subId = new int[1];
	                long[] cellId = new long[1];
	                int result = cellLocator.IntersectWithLine(spacecraftPosition, lookPt, tol, t, x, pcoords, subId, cellId, cell);

	                if (result > 0)
	                {
	                    double redValue = redPixelData[j][i];

	                    double[] uv = new double[2];

	                    greenFrustum.computeTextureCoordinatesFromPoint(x, IMAGE_WIDTH, IMAGE_HEIGHT, uv, true);
	                    double greenValue = ImageDataUtil.interpolateWithinImage(
	                            greenPixelData,
	                            IMAGE_WIDTH,
	                            IMAGE_HEIGHT,
	                            uv[1],
	                            uv[0]);

	                    blueFrustum.computeTextureCoordinatesFromPoint(x, IMAGE_WIDTH, IMAGE_HEIGHT, uv, true);
	                    double blueValue = ImageDataUtil.interpolateWithinImage(
	                            bluePixelData,
	                            IMAGE_WIDTH,
	                            IMAGE_HEIGHT,
	                            uv[1],
	                            uv[0]);

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
	                // If there is no intersection then set the pixel to black. The
	                // memory associated with vtkImageData is not cleared by default.
	                else
	                {
	                    colorImage.SetScalarComponentFromFloat(j, i, 0, 0, 0.00f);
	                    colorImage.SetScalarComponentFromFloat(j, i, 0, 1, 0.00f);
	                    colorImage.SetScalarComponentFromFloat(j, i, 0, 2, 0.00f);
	                }
	            }
	        }

	        colorImage.Modified();
	        outputs.add(Pair.of(colorImage, shiftedFootprint));
        }
	}
}
