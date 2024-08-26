package edu.jhuapl.sbmt.image.pipelineComponents.publishers.builtin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.internal.Lists;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.api.Pixel;
import edu.jhuapl.sbmt.layer.impl.DoubleBuilderBase.DoubleRangeGetter;
import edu.jhuapl.sbmt.layer.impl.DoubleGetter2d;
import edu.jhuapl.sbmt.layer.impl.DoubleGetter3d;
import edu.jhuapl.sbmt.layer.impl.LayerDoubleBuilder;
import edu.jhuapl.sbmt.layer.impl.LayerDoubleTransformFactory;
import edu.jhuapl.sbmt.layer.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.layer.impl.PixelDoubleFactory;
import edu.jhuapl.sbmt.layer.impl.PixelVectorDoubleFactory;
import edu.jhuapl.sbmt.layer.impl.RangeGetterDoubleBuilder;
import edu.jhuapl.sbmt.layer.impl.RangeGetterVectorDoubleFactory;
import edu.jhuapl.sbmt.layer.impl.ValidityChecker2d;
import edu.jhuapl.sbmt.layer.impl.ValidityChecker3d;
import edu.jhuapl.sbmt.layer.impl.ValidityCheckerDoubleFactory;
import edu.jhuapl.sbmt.pipeline.publisher.BasePipelinePublisher;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.header.Standard;

public class BuiltInFitsReader extends BasePipelinePublisher<Layer>
{

    public static void main(String[] args) throws FitsException, IOException
    {
        BuiltInFitsReader reader = new BuiltInFitsReader("/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.FIT", new double[] {});
    }

    private String filename;
    private double[] fill;

    protected static final PixelDoubleFactory PixelScalarFactory = new PixelDoubleFactory();
    protected static final PixelVectorDoubleFactory PixelVectorFactory = new PixelVectorDoubleFactory();
    protected static final LayerTransformFactory TransformFactory = new LayerTransformFactory();
    protected static final LayerDoubleTransformFactory DoubleTransformFactory = new LayerDoubleTransformFactory();

    protected static final double TestOOBValue = -100.0;

//    private float[][] array2D = null;
    private float[][][] dataArray = null;
    // height is axis 0
    private int fitsHeight = 0;
    // for 2D pixel arrays, width is axis 1, for 3D pixel arrays, width axis is
    // 2
    private int fitsWidth = 0;
    // for 2D pixel arrays, depth is 0, for 3D pixel arrays, depth axis is 1
    private int fitsDepth = 0;

    private Double dataMin = null;
    private Double dataMax = null;

    public BuiltInFitsReader(String filename, double[] fill) throws FitsException, IOException
    {
    	this(filename, fill, true);
    }

    public BuiltInFitsReader(String filename, double[] fill, boolean transpose) throws FitsException, IOException
    {
        this.filename = filename;
        this.fill = fill;
        loadData();
        outputs = new ArrayList<Layer>();
        for (int i=0; i<fitsDepth; i++)
        {
        	Layer layer = ofScalar(i);
        	if (transpose)
        		layer = TransformFactory.swapIJ().apply(layer);
//        	layer = TransformFactory.flipAboutX().apply(layer);
        	outputs.add(layer);
        }

//        List<Layer> layers = fitsDepth == 1 ? List.of(ofScalar()) : ofVector(fitsDepth, fitsWidth, fitsHeight);
//
//        for (Layer layer : layers)
//        {
//	        layer = TransformFactory.rotateCCW().apply(layer);
//	        // layer = DoubleTransformFactory.linearInterpolate(537,
//	        // 412).apply(layer);
//	//         displayLayer("FITS 1D Layer", layer, 0, null);
//	        outputs = new ArrayList<Layer>();
//	        outputs.add(layer);
//        }
    }

    private void loadData() throws IOException, FitsException
    {
        int[] fitsAxes = null;
        int fitsNAxes = 0;

        // single file images (e.g. LORRI and LEISA)
        try (Fits f = new Fits(filename))
        {
            BasicHDU<?> hdu = f.getHDU(0);

            fitsAxes = hdu.getAxes();
            fitsNAxes = fitsAxes.length;
            fitsHeight = fitsAxes[0];
            fitsWidth = fitsAxes[1];
            fitsDepth = 1;
            if (fitsNAxes == 3)
            {
            	fitsHeight = fitsAxes[1];
            	fitsWidth = fitsAxes[2];
            	fitsDepth = fitsAxes[0];
            }
//            System.out.println("BuiltInFitsReader: loadData: width " + fitsWidth + " " + fitsHeight);
            // Do not use BasicHDU to get these optional keywords. BasicHDU
            // would return a value of 0. for missing keywords. We need to SKIP
            // missing DATAMIN/DATAMAX. Use the Header interface instead.
            Header h = hdu.getHeader();
            dataMin = h.findCard(Standard.DATAMIN) != null ? h.getDoubleValue(Standard.DATAMIN) : null;
            dataMax = h.findCard(Standard.DATAMAX) != null ? h.getDoubleValue(Standard.DATAMAX) : null;

            Object data = hdu.getData().getData();
//            System.out.println("BuiltInFitsReader: loadData: data type " + data.getClass());
//            System.out.println("BuiltInFitsReader: loadData: " + ((float[][][])data)[0].length);
            dataArray = new float[fitsDepth][fitsHeight][fitsWidth];
            for (int k = 0; k < fitsDepth; k++)
            {
	            if (data instanceof float[][])
	            {
	            	dataArray[k] = (float[][]) data;
//	                array2D = (float[][]) data;
	            }
	            else if (data instanceof float[][][])
	            {
//	            	dataArray[k] = ((float[][][]) data)[k];
	            	float[][] array = ((float[][][]) data)[k];
//	                dataArray = new float[fitsDepth][fitsHeight][fitsWidth];
	                for (int i = 0; i < fitsHeight; ++i)
	                {
	                    for (int j = 0; j < fitsWidth; ++j)
	                    {
	                        dataArray[k][fitsHeight-1-i][j] = array[i][j];
	                    }
	                }
	            }
	            else if (data instanceof short[][])
	            {
	                short[][] arrayS = (short[][]) data;
	                dataArray = new float[1][fitsHeight][fitsWidth];

	                for (int i = 0; i < fitsHeight; ++i)
	                    for (int j = 0; j < fitsWidth; ++j)
	                    {
	                        dataArray[0][i][j] = arrayS[i][j];
	                    }
	            }
	            else if (data instanceof short[][][])
	            {
	            	dataArray[k] = ((float[][][]) data)[k];
	            }
	            else if (data instanceof double[][])
	            {
	                double[][] arrayDouble = (double[][]) data;
	                dataArray = new float[1][fitsHeight][fitsWidth];

	                for (int i = 0; i < fitsHeight; ++i)
	                    for (int j = 0; j < fitsWidth; ++j)
	                    {
	                    	dataArray[0][i][j] = (float) arrayDouble[i][j];
	                    }
	            }
	            else if (data instanceof byte[][])
	            {
	                byte[][] arrayB = (byte[][]) data;
	                dataArray = new float[1][fitsHeight][fitsWidth];

	                for (int i = 0; i < fitsHeight; ++i)
	                    for (int j = 0; j < fitsWidth; ++j)
	                    {
	                    	dataArray[0][i][j] = arrayB[i][j] & 0xFF;
	                    }
	            }
	            // WARNING: THIS IS A TOTAL HACK TO SUPPORT DART LUKE TEST IMAGES:
	            else if (data instanceof byte[][][])
	            {
	                // DART LUKE images are color: 3-d slab with the 3rd
	                // dimension being RGB, but the first test images are
	                // monochrome. Thus, in order to process the images, making
	                // this temporary hack.
	                byte[][][] arrayB = (byte[][][]) data;

	                // Override the default setup used for other 3-d images.
	                fitsDepth = 1;
	                fitsHeight = arrayB[0].length;
	                fitsWidth = arrayB[0][0].length;

	                dataArray = new float[0][fitsHeight][fitsWidth];

	                for (int i = 0; i < fitsHeight; ++i)
	                    for (int j = 0; j < fitsWidth; ++j)
	                    {
	                    	dataArray[0][i][j] = arrayB[0][i][j] & 0xFF;
	                    }
	            }
	            else
	            {
	                System.out.println("Data type not supported: " + data.getClass().getCanonicalName());
	                return;
	            }
            }
            // load in calibration info
            // loadImageCalibrationData(f);
        }

    }

//    private void load2DData()

    protected Layer ofScalar(int layerIndex)
    {
        // Make builders for both the layer and the range checker. Use the
        // dimensions from the fits file to set I, J sizes...
        LayerDoubleBuilder layerBuilder = new LayerDoubleBuilder();
        RangeGetterDoubleBuilder rangeBuilder = new RangeGetterDoubleBuilder();

        // Adapt the array to the appropriate getter interface. Both builders
        // need this.
        DoubleGetter2d doubleGetter = (i, j) -> {
            return dataArray[layerIndex][i][j];
        };

        layerBuilder.doubleGetter(doubleGetter, fitsHeight, fitsWidth);
        rangeBuilder.getter(doubleGetter, fitsHeight, fitsWidth);

        // Also tell the range builder (only) any min or max values that were
        // defined
        // by keywords.
        if (dataMin != null)
        {
            rangeBuilder.min(dataMin);
        }
        if (dataMax != null)
        {
            rangeBuilder.max(dataMax);
        }

        // Both builders need to know how to check for validity as well.
        if (fill != null && fill.length > 0)
        {
            ValidityChecker2d checker = new ValidityCheckerDoubleFactory().checker2d(fill);

            layerBuilder.checker(checker);
            rangeBuilder.checker(checker);
        }

        // Here's the trick: build the range getter first and inject it into the
        // layer builder just before building the layer.
        layerBuilder.rangeGetter(rangeBuilder.build());

        return layerBuilder.build();
    }

    protected List<Layer> ofVector(int iSize, int jSize, int kSize)
	{
    	List<Layer> layers = Lists.newArrayList();
		// Make builders for both the layer and the range checker. Use the
        // dimensions from the fits file to set I, J sizes...
        LayerDoubleBuilder layerBuilder = new LayerDoubleBuilder();
		DoubleGetter3d doubleGetter = (i, j, k) ->
		{
//			System.out.println("BuiltInFitsReader: ofVector: ijk " + i + " " + j + " " + k);
//			System.out.println("BuiltInFitsReader: ofVector: returning " + dataArray[i][j][k]);
			return dataArray[i][j][k];
		};

		layerBuilder.doubleGetter(doubleGetter, iSize, jSize, kSize);

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

            layerBuilder.checker(checker);
        }

        RangeGetterVectorDoubleFactory rangeBuilder = new RangeGetterVectorDoubleFactory();
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
        layers.add(layerBuilder.build());

        return layers;
	}

    protected void displayLayer(String message, Layer layer, int displayKsize, Double invalidValueSubstitute)
    {
        System.out.println("************************************************");
        System.out.println(message);
        System.out.println("1D Fits Test");
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
