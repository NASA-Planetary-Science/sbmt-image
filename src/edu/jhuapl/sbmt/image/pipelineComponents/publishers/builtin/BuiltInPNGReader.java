package edu.jhuapl.sbmt.image.pipelineComponents.publishers.builtin;

import java.io.IOException;
import java.util.ArrayList;

import edu.jhuapl.saavtk.util.IntensityRange;
import edu.jhuapl.saavtk.util.NativeLibraryLoader;
import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.Pixel;
import edu.jhuapl.sbmt.layer.impl.DoubleBuilderBase.DoubleRangeGetter;
import edu.jhuapl.sbmt.layer.impl.DoubleGetter3d;
import edu.jhuapl.sbmt.layer.impl.LayerDoubleBuilder;
import edu.jhuapl.sbmt.layer.impl.LayerDoubleTransformFactory;
import edu.jhuapl.sbmt.layer.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.layer.impl.PixelDoubleFactory;
import edu.jhuapl.sbmt.layer.impl.PixelVectorDoubleFactory;
import edu.jhuapl.sbmt.layer.impl.RangeGetterDoubleBuilder;
import edu.jhuapl.sbmt.layer.impl.RangeGetterVectorDoubleFactory;
import edu.jhuapl.sbmt.layer.impl.ValidityChecker3d;
import edu.jhuapl.sbmt.pipeline.publisher.BasePipelinePublisher;
import nom.tam.fits.FitsException;
import vtk.vtkImageData;
import vtk.vtkImageMapToColors;
import vtk.vtkLookupTable;
import vtk.vtkPNGReader;

//TODO: this is a placeholder until we get GDAL support in place - this reads data into a vtkImageData and then back to a layer so it goes with the FITS paradigm already established
//Once GDAL is in place this will go right into a Layer
public class BuiltInPNGReader extends BasePipelinePublisher<Layer>
{

	private String filename;

//	protected static final LayerDoubleFactory LayerFactory = new LayerDoubleFactory();
	protected static final PixelDoubleFactory PixelScalarFactory = new PixelDoubleFactory();
	protected static final PixelVectorDoubleFactory PixelVectorFactory = new PixelVectorDoubleFactory();
	protected static final LayerTransformFactory TransformFactory = new LayerTransformFactory();
	protected static final LayerDoubleTransformFactory DoubleTransformFactory = new LayerDoubleTransformFactory();

	protected static final double TestOOBValue = -100.0;

	int imageHeight = 0;
	int imageWidth = 0;
	int imageDepth = 0;
	private double[] fill;
//	private float[][][] array3D = null;
	private float minValue;
	private float maxValue;
	private IntensityRange displayedRange = new IntensityRange(1,0);
	private vtkImageData displayedImage;
	private vtkImageData rawImage;

	public static void main(String[] args) throws FitsException, IOException, Exception
	{
		NativeLibraryLoader.loadVtkLibraries();
		BuiltInPNGReader reader = new BuiltInPNGReader("/Users/steelrj1/Downloads/FRT00002992_03_IF162S_TRR7_RED_BLUE_RATIO_colortable.png"); //was "/Users/steelrj1/Desktop/image_map.png"
	}

	public BuiltInPNGReader(String filename) throws IOException, Exception
	{
		this.filename = filename;

        loadData();
        Layer layer;
        layer = ofVector(imageWidth, imageHeight, imageDepth);
//        layer = TransformFactory.rotateCCW().apply(layer);
        // layer = DoubleTransformFactory.linearInterpolate(537,
        // 412).apply(layer);
//        displayLayer("PNG Layer", layer, 3, null);
        outputs = new ArrayList<Layer>();
        outputs.add(layer);
	}

	private void loadData() throws IOException
	{
		vtkPNGReader reader = new vtkPNGReader();
		reader.SetFileName(filename);
		reader.Update();
		rawImage = new vtkImageData();
		rawImage.DeepCopy(reader.GetOutput());

//		double[] scalarRange = rawImage.GetScalarRange();
//        minValue = (float)scalarRange[0];
//        maxValue = (float)scalarRange[1];
//        setDisplayedImageRange(rawImage, new IntensityRange(0, 255));

		imageWidth = rawImage.GetDimensions()[0];
		imageHeight = rawImage.GetDimensions()[1];
		imageDepth = rawImage.GetNumberOfScalarComponents();
//		array3D = new float[imageWidth][imageHeight][imageDepth];
//
//		for (int i=0; i<imageWidth; i++)
//		{
//			if (i%1000==0) System.out.println("BuiltInPNGReader: loadData: i is " + i);
//			for (int j=0; j<imageHeight; j++)
//			{
//				for (int k=0; k<imageDepth; k++)
//				{
//					array3D[i][j][k] = (float) rawImage.GetScalarComponentAsFloat(i, j, 0, k);
////					System.out.println("BuiltInPNGReader: loadData: array at " + i + " " + j + " " + k + " is " + array3D[i][j][k]);
//				}
//			}
//		}

	}

	private void setDisplayedImageRange(vtkImageData rawImage, IntensityRange range)
    {
//		System.out.println("BuiltInPNGReader: setDisplayedImageRange: num scalar comp  " + rawImage.GetNumberOfScalarComponents());
//        if (rawImage.GetNumberOfScalarComponents() > 1)
//        {
//            displayedImage = rawImage;
//            System.out.println("BuiltInPNGReader: setDisplayedImageRange: returning raw");
//            return;
//        }
//        System.out.println("BuiltInPNGReader: setDisplayedImageRange: range " + range);
        if (displayedRange.min != range.min ||
                displayedRange.max != range.max)
        {
            displayedRange = range;

            float dx = (maxValue-minValue)/255.0f;
            float min = minValue + range.min*dx;
            float max = minValue + range.max*dx;

//            System.out.println("BuiltInPNGReader: setDisplayedImageRange: min max " + min + " " + max);
            // Update the displayed image
            vtkLookupTable lut = new vtkLookupTable();
            lut.SetTableRange(min, max);
            lut.SetValueRange(0.0, 1.0);
            lut.SetHueRange(0.0, 0.0);
            lut.SetSaturationRange(0.0, 0.0);
            //lut.SetNumberOfTableValues(402);
            lut.SetRampToLinear();
            lut.Build();

            /*
            // Change contrast of each channel separately and then combine the 3 channels into one image
            int numChannels = rawImage.GetNumberOfScalarComponents();
            vtkImageAppendComponents appendFilter = new vtkImageAppendComponents();
            for (int i=0; i<numChannels; ++i)
            {
                vtkImageMapToColors mapToColors = new vtkImageMapToColors();
                mapToColors.SetInput(rawImage);
                mapToColors.SetOutputFormatToRGB();
                mapToColors.SetLookupTable(lut);
                mapToColors.SetActiveComponent(i);
                mapToColors.Update();

                vtkAlgorithmOutput output = mapToColors.GetOutputPort();
                vtkImageMagnitude magnitudeFilter = new vtkImageMagnitude();
                magnitudeFilter.SetInputConnection(output);
                magnitudeFilter.Update();
                output = magnitudeFilter.GetOutputPort();

                if (i == 0)
                    appendFilter.SetInputConnection(0, output);
                else
                    appendFilter.AddInputConnection(0, output);
            }

            appendFilter.Update();
            vtkImageData appendFilterOutput = appendFilter.GetOutput();
             */

            vtkImageMapToColors mapToColors = new vtkImageMapToColors();
            mapToColors.SetInputData(rawImage);
            mapToColors.SetOutputFormatToRGBA();
            mapToColors.SetLookupTable(lut);
            mapToColors.Update();

            vtkImageData mapToColorsOutput = mapToColors.GetOutput();

            if (displayedImage == null)
                displayedImage = new vtkImageData();
//            System.out.println("BuiltInPNGReader: setDisplayedImageRange: returning maps to colors");
            displayedImage.DeepCopy(mapToColorsOutput);
//            System.out.println("BuiltInPNGReader: setDisplayedImageRange: num components " + displayedImage.GetNumberOfScalarComponents());
            mapToColors.Delete();
            lut.Delete();

        }
    }

	protected Layer ofVector(int iSize, int jSize, int kSize)
	{
		// Make builders for both the layer and the range checker. Use the
        // dimensions from the fits file to set I, J sizes...
        LayerDoubleBuilder layerBuilder = new LayerDoubleBuilder();
//        RangeGetterDoubleBuilder rangeBuilder = new RangeGetterDoubleBuilder();

		DoubleGetter3d doubleGetter = (i, j, k) ->
		{
			return rawImage.GetScalarComponentAsFloat(i, j, 0, k);
//			return array3D[i][j][k];
		};

		layerBuilder.doubleGetter(doubleGetter, iSize, jSize, kSize);
//	    rangeBuilder.getter(doubleGetter, iSize, jSize, kSize);

	 // Both builders need to know how to check for validity as well.
		ValidityChecker3d checker = null;
        if (fill != null && fill.length > 0)
        {
        	checker = (i, j, k, value) -> {
        		for (double invalidValue : fill)
                {
                    if (Double.compare(value, invalidValue) == 0)
                    {
                        return false;
                    }
                }

                return true;
        	};
//            checker = new ValidityCheckerDoubleFactory().scalar(fill);

            layerBuilder.checker(checker);
//            rangeBuilder.checker(checker);
        }

        RangeGetterVectorDoubleFactory rangeBuilder = new RangeGetterVectorDoubleFactory();

//        VectorValidityChecker checker = checkValidity ? testVectorChecker(iSize, jSize) : null;

        Double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        DoubleRangeGetter overallRange = null;
        if ( min != null || max != null) {
            RangeGetterDoubleBuilder b = new RangeGetterDoubleBuilder();
            if (min != null) {
                b.min(min);
            }
            if (max != null) {
                b.max(max);
            }

            overallRange = b.build();
        }

        // Here's the trick: build the range getter first and inject it into the
        // layer builder just before building the layer.
        layerBuilder.rangeGetter(rangeBuilder.of(doubleGetter, checker, overallRange, iSize, jSize, kSize));
        return layerBuilder.build();

//		return checker != null ? //
//				LayerFactory.ofScalar(doubleGetter, iSize, jSize, checker) : //
//				LayerFactory.ofScalar(doubleGetter, iSize, jSize);
	}

	protected void displayLayer(String message, Layer layer, int displayKsize, Double invalidValueSubstitute)
	{
		System.out.println("************************************************");
		System.out.println(message);
		System.out.println("PNG Loading Test");
		System.out.println("************************************************");

		Pixel pixel = displayKsize == 0 ? //
				PixelScalarFactory.of(0.0, TestOOBValue, invalidValueSubstitute) : //
				PixelVectorFactory.of(displayKsize, TestOOBValue, invalidValueSubstitute);
		display("Loaded layer:", layer, pixel);
		System.out.println();
	}

	/**
	 * Display the state of a {@link Layer}, prefaced by a message, and using
	 * the specified {@link Pixel} instance to retrieve data from the layer.
	 *
	 * @param message the message used as a preface
	 * @param layer the layer whose state to display
	 * @param pixel the pixel used to get data from the layer
	 */
	protected final void display(String message, Layer layer, Pixel pixel)
	{
		System.out.println(message);
		for (int row = -1; row <= layer.jSize(); ++row)
		{
			StringBuilder builder = new StringBuilder();
			String delim = "";
			for (int column = -1; column <= layer.iSize(); ++column)
			{
				layer.get(column, row, pixel);
				builder.append(delim);
				builder.append(pixel);
				delim = ", ";
			}
			System.out.println(builder.toString());
		}
	}

}
