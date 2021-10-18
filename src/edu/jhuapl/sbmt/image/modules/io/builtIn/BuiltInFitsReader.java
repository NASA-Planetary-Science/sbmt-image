package edu.jhuapl.sbmt.image.modules.io.builtIn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelDouble;
import edu.jhuapl.sbmt.image.impl.LayerDoubleFactory;
import edu.jhuapl.sbmt.image.impl.LayerDoubleFactory.DoubleGetter2d;
import edu.jhuapl.sbmt.image.impl.LayerDoubleTransformFactory;
import edu.jhuapl.sbmt.image.impl.LayerTransformFactory;
import edu.jhuapl.sbmt.image.impl.LayerValidityChecker;
import edu.jhuapl.sbmt.image.impl.PixelDoubleFactory;
import edu.jhuapl.sbmt.image.impl.PixelVectorDoubleFactory;
import edu.jhuapl.sbmt.image.pipeline.publisher.BasePipelinePublisher;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

public class BuiltInFitsReader extends BasePipelinePublisher<Layer>
{

	public static void main(String[] args) throws FitsException, IOException
	{
		BuiltInFitsReader reader = new BuiltInFitsReader("/Users/steelrj1/Desktop/M0125990473F4_2P_IOF_DBL.FIT", new Float[] {});
	}

	private String filename;

    protected static final LayerDoubleFactory LayerFactory = new LayerDoubleFactory();
    protected static final PixelDoubleFactory PixelScalarFactory = new PixelDoubleFactory();
    protected static final PixelVectorDoubleFactory PixelVectorFactory = new PixelVectorDoubleFactory();
    protected static final LayerTransformFactory TransformFactory = new LayerTransformFactory();
    protected static final LayerDoubleTransformFactory DoubleTransformFactory = new LayerDoubleTransformFactory();

    protected static final double TestOOBValue = -100.0;

    private float[][] array2D = null;
    // height is axis 0
    int fitsHeight = 0;
    // for 2D pixel arrays, width is axis 1, for 3D pixel arrays, width axis is 2
    int fitsWidth = 0;

	public BuiltInFitsReader(String filename, Float[] fill) throws FitsException, IOException
	{
		this.filename = filename;
		loadData();
		PixelDouble pd = new PixelDoubleFactory().of(0.0, Double.NaN, Double.NaN);
		Layer layer;
		if (fill.length == 0) layer = ofScalar(fitsHeight, fitsWidth, null);
		else {
			layer = ofScalar(fitsHeight, fitsWidth, new LayerValidityChecker()
			{

				@Override
				public boolean test(Layer layer, int i, int j)
				{
					List<Float> fillValues = Arrays.asList(fill);
					pd.set(array2D[i][j]);
					return !fillValues.contains((float)pd.getStoredValue());
				}
			});
		}
		layer = TransformFactory.rotateCCW().apply(layer);
//		layer = DoubleTransformFactory.linearInterpolate(537, 412).apply(layer);
//		displayLayer("FITS 1D Layer", layer, 0, null);
		outputs = new ArrayList<Layer>();
		outputs.add(layer);
	}

	private void loadData() throws IOException, FitsException
	{
        int[] fitsAxes = null;
        int fitsNAxes = 0;

        // for 2D pixel arrays, depth is 0, for 3D pixel arrays, depth axis is 1
        int fitsDepth = 0;

        // single file images (e.g. LORRI and LEISA)
            try (Fits f = new Fits(filename))
            {
                BasicHDU<?> h = f.getHDU(0);

                fitsAxes = h.getAxes();
                fitsNAxes = fitsAxes.length;
                fitsHeight = fitsAxes[0];
                fitsWidth = fitsNAxes == 3 ? fitsAxes[2] : fitsAxes[1];
                fitsDepth = fitsNAxes == 3 ? fitsAxes[1] : 1;

                Object data = h.getData().getData();

                if (data instanceof float[][])
                {
                    array2D = (float[][]) data;
                }
                else if (data instanceof short[][])
                {
                    short[][] arrayS = (short[][]) data;
                    array2D = new float[fitsHeight][fitsWidth];

                    for (int i = 0; i < fitsHeight; ++i)
                        for (int j = 0; j < fitsWidth; ++j)
                        {
                            array2D[i][j] = arrayS[i][j];
                        }
                }
                else if (data instanceof double[][])
                {
                    double[][] arrayDouble = (double[][]) data;
                    array2D = new float[fitsHeight][fitsWidth];

                    for (int i = 0; i < fitsHeight; ++i)
                        for (int j = 0; j < fitsWidth; ++j)
                        {
                            array2D[i][j] = (float) arrayDouble[i][j];
                        }
                }
                else if (data instanceof byte[][])
                {
                    byte[][] arrayB = (byte[][]) data;
                    array2D = new float[fitsHeight][fitsWidth];

                    for (int i = 0; i < fitsHeight; ++i)
                        for (int j = 0; j < fitsWidth; ++j)
                        {
                            array2D[i][j] = arrayB[i][j] & 0xFF;
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

                    array2D = new float[fitsHeight][fitsWidth];

                    for (int i = 0; i < fitsHeight; ++i)
                        for (int j = 0; j < fitsWidth; ++j)
                        {
                            array2D[i][j] = arrayB[0][i][j] & 0xFF;
                        }
                }
                else
                {
                    System.out.println("Data type not supported: " + data.getClass().getCanonicalName());
                    return;
                }

                // load in calibration info
//                loadImageCalibrationData(f);
            }

	}

    protected Layer ofScalar(int iSize, int jSize, LayerValidityChecker checker)
    {
        DoubleGetter2d doubleGetter = (i, j) -> {
            return array2D[i][j];
        };

        return checker != null ? //
                LayerFactory.ofScalar(doubleGetter, iSize, jSize, checker) : //
                LayerFactory.ofScalar(doubleGetter, iSize, jSize);
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
