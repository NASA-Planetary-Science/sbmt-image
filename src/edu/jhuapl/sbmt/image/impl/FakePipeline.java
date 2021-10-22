package edu.jhuapl.sbmt.image.impl;

import java.util.List;
import java.util.function.Function;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelVector;
import edu.jhuapl.sbmt.image.impl.LayerDoubleFactory.DoubleGetter2d;
import edu.jhuapl.sbmt.image.impl.LayerDoubleFactory.DoubleGetter3d;
import edu.jhuapl.sbmt.image.impl.LayerTransformFactory.ForwardingLayer;
import edu.jhuapl.sbmt.image.impl.ValidityCheckerDoubleFactory.ScalarValidityChecker;

/**
 * Sample/test class for demonstrate how to create and manipulate {@link Layer}s
 * using {@link Pixel}s to extract information from them.
 *
 * @author James Peachey
 *
 */
public abstract class FakePipeline
{

    /**
     * Factories for layers, pixels, and transforms.
     */
    protected static final LayerDoubleFactory LayerFactory = new LayerDoubleFactory();
    protected static final PixelDoubleFactory PixelScalarFactory = new PixelDoubleFactory();
    protected static final PixelVectorDoubleFactory PixelVectorFactory = new PixelVectorDoubleFactory();
    protected static final LayerTransformFactory TransformFactory = new LayerTransformFactory();
    protected static final LayerDoubleTransformFactory DoubleTransformFactory = new LayerDoubleTransformFactory();

    private final String pipelineTitle;
    private Function<Layer, Layer> transform;

    public FakePipeline(String pipelineTitle, Function<Layer, Layer> transform)
    {
        super();

        this.pipelineTitle = pipelineTitle;
        this.transform = transform;
    }

    /**
     * Each pipeline creates (simulates loading) a layer, then processing the
     * layer, displaying what the layer looks like after each step.
     */
    public final void run()
    {
        Layer layer = createLayer();

        Layer processedLayer = transform.apply(layer);

        if (processedLayer != layer)
        {
            displayCreatedLayer(layer);
        }

        displayProcessedLayer(processedLayer);
    }

    /**
     * Construct the title for this layer. The title is derived from the
     * pipeline's title string, with information added about the sizes of the
     * layer dimensions.
     *
     * @param layer the layer whose title to construct
     * @return the title string, suitable for display
     */
    protected String getPipelineTitle(Layer layer)
    {
        String delim = ", ";

        StringBuilder builder = new StringBuilder(pipelineTitle);
        builder.append(" (");
        builder.append(layer.iSize());

        builder.append(delim);
        builder.append(layer.jSize());

        for (Integer size : layer.dataSizes())
        {
            builder.append(delim);
            builder.append(size != null ? size : "?");
        }
        builder.append(")");

        return builder.toString();
    }

    /**
     * Create ("load") a {@link Layer} to be used in a pipeline run. Details are
     * up to the concrete pipeline implementation. This method may return any
     * type of {@link Layer} (scalar or vector).
     *
     * @return the loaded layer
     */
    protected abstract Layer createLayer();

    protected Layer createScalarLayer()
    {
        return ofScalar(TestISize, TestJSize, testScalarChecker(TestISize));
    }

    protected Layer createVectorLayer()
    {
        return ofVector(TestISize, TestJSize, TestKSize, testVectorChecker(TestISize, TestJSize));
    }

    /**
     * Create a scalar layer of the specified dimensions, using the specified
     * validity checker. The value at each point in the layer will be a ramp
     * starting from 0 and going through iSize * jSize - 1 in a row-wise manner.
     * <p>
     * The checker may be null, in which case no checker is used and all
     * in-bounds pixels will be considered valid as far as the layer is
     * concerned. Restated, if the checker is null, the output layer's
     * {@link Layer#isValid(int, int)} returns the same result as
     * {@link Layer#isInBounds(int, int)} for every value of the (I, J) indices.
     *
     * @param iSize number of in-bounds I index values
     * @param jSize number of in-bounds J index values
     * @param checker validity checker, or null for all in-bound data valid
     * @return the layer
     */
    protected Layer ofScalar(int iSize, int jSize, ValidityCheckerDoubleFactory.ScalarValidityChecker checker)
    {
        DoubleGetter2d doubleGetter = (i, j) -> {
            return LayerFromDoubleCollection1dFactory.ColumnIRowJ.getIndex(i, j, iSize, jSize);
        };

        return checker != null ? //
                LayerFactory.ofScalar(doubleGetter, iSize, jSize, checker) : //
                LayerFactory.ofScalar(doubleGetter, iSize, jSize);
    }

    /**
     * Create a vector layer of the specified dimensions, using the specified
     * validity checker. The value at each point in the layer will be a ramp
     * starting from 0 and going through iSize * jSize * kSize - 1 in a row-wise
     * manner.
     * <p>
     * The checker may be null, in which case no checker is used and all
     * in-bounds pixels will be considered valid as far as the layer is
     * concerned. Restated, if the checker is null, the output layer's
     * {@link Layer#isValid(int, int)} returns the same result as
     * {@link Layer#isInBounds(int, int)} for every value of the (I, J) indices.
     *
     * @param iSize number of in-bounds I index values
     * @param jSize number of in-bounds J index values
     * @param kSize number of in-bounds K index values
     * @param checker validity checker, or null for all in-bound data valid
     * @return the layer
     */
    protected Layer ofVector(int iSize, int jSize, int kSize, ValidityCheckerDoubleFactory.ScalarValidityChecker checker)
    {
        DoubleGetter3d doubleGetter = (i, j, k) -> {
            return kSize * LayerFromDoubleCollection1dFactory.ColumnIRowJ.getIndex(i, j, iSize, jSize) + k;
        };

        return checker != null ? //
                LayerFactory.ofVector(doubleGetter, iSize, jSize, kSize, checker) : //
                LayerFactory.ofVector(doubleGetter, iSize, jSize, kSize);
    }

    protected Layer ofVector(int iSize, int jSize, int kSize, ValidityCheckerDoubleFactory.VectorValidityChecker checker)
    {
        DoubleGetter3d doubleGetter = (i, j, k) -> {
            return kSize * LayerFromDoubleCollection1dFactory.ColumnIRowJ.getIndex(i, j, iSize, jSize) + k;
        };

        return checker != null ? //
                LayerFactory.ofVector(doubleGetter, iSize, jSize, kSize, checker) : //
                LayerFactory.ofVector(doubleGetter, iSize, jSize, kSize);
    }

    protected abstract void displayCreatedLayer(Layer layer);

    protected abstract void displayProcessedLayer(Layer layer);

    protected void displayLayer(String message, Layer layer, int displayKsize, Double invalidValueSubstitute)
    {
        System.out.println("************************************************");
        System.out.println(message);
        System.out.println(getPipelineTitle(layer));
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

    /**
     * Default dimensions for generated test layers.
     */
    protected static final int TestISize = 6;
    protected static final int TestJSize = 4;
    // This is used for layers that are created and/or displayed as vectors.
    protected static final int TestKSize = 3;

    /**
     * Value returned for each data element if any of its indices are out of
     * bounds for the relevant dimension (I, J, or K). In actual practice, when
     * rendering in VTK, {@link Double#NaN} should be used for this value.
     * <p>
     * Unlike the similar constant {@link #TestChecker}, it is always necessary
     * for calling code to define this value, as the layer otherwise has no idea
     * what value to assign when indices are out of bounds.
     */
    protected static final double TestOOBValue = -100.0;

    /**
     * Value that *may be* substituted for any data element that has been
     * flagged as being "invalid". In actual practice, when rendering in VTK,
     * {@link Double#NaN} should be used for all such substitutions.
     * <p>
     * When rendering, one always wants to make this substitution. However, for
     * purposes of calculations, it is preferable *not* to specify a replacement
     * value for invalid data, because there could be multiple
     * reasons/mechanisms for marking elements as invalid, and the actual value
     * at the specified location may have relevance. In any case, the original
     * data are preserved by the layer.
     */
    protected static final double TestInvalidValueSubstitute = -200.0;

    /**
     * Arbitrary scalar validity checker -- mark every 5th position "invalid",
     * starting with column 4.
     */
    protected ValidityCheckerDoubleFactory.ScalarValidityChecker testScalarChecker(int iSize)
    {
        return (i, j, value) -> {
            boolean isValid = (j * iSize + i + 1) % 5 != 0;
            return isValid;
        };
    };

    /**
     * Arbitrary vector validity checker. Use the scalar checker but also
     * invalidate every value that is a multiple of 5.
     */
    protected ValidityCheckerDoubleFactory.VectorValidityChecker testVectorChecker(int iSize, int jSize)
    {
        ValidityCheckerDoubleFactory.ScalarValidityChecker scalarChecker = testScalarChecker(iSize);

        return (i, j, k, value) -> {
            // Return false if the scalar checker returns false.
            if (!scalarChecker.test(i, j, value))
            {
                return false;
            }

            return (int) (value % 5.0) != 0;
        };
    };

    protected static final Function<Layer, Layer> TouchLayer = layer -> {
        return new ForwardingLayer(layer);
    };

    /**
     * Create a pipeline that loads a {@link Layer} that contains scalar double
     * values, retrieved and displayed as scalars.
     *
     * @param invalidValueSubstitute the value to substitute for all invalid
     *            data elements, or null to show the actual data elements
     *            present in each case
     * @return the pipeline
     */
    protected static FakePipeline scalarToScalar(Double invalidValueSubstitute)
    {
        return scalarToScalar(invalidValueSubstitute, Function.identity());
    }

    /**
     * Create a pipeline that loads a {@link Layer} that contains scalar double
     * values, and applies the specified transform. The original and transformed
     * layers are displayed as scalars.
     *
     * @param invalidValueSubstitute the value to substitute for all invalid
     *            data elements, or null to show the actual data elements
     *            present in each case
     * @param transform the transform to apply to the layer
     * @return the pipeline
     */
    protected static FakePipeline scalarToScalar(Double invalidValueSubstitute, Function<Layer, Layer> transform)
    {
        return new FakePipeline("Load a scalar layer, display as scalar layer", transform) {

            @Override
            protected Layer createLayer()
            {
                return createScalarLayer();
            }

            @Override
            protected void displayCreatedLayer(Layer layer)
            {
                displayLayer("Created layer", layer, 0, invalidValueSubstitute);
            }

            @Override
            protected void displayProcessedLayer(Layer layer)
            {
                displayLayer("Transformed layer", layer, 0, invalidValueSubstitute);
            }

        };
    }

    /**
     * Create a pipeline that loads a {@link Layer} that contains scalar double
     * values, retrieved and displayed as a vector.
     *
     * @param displayKsize the number of elements to display at each indexed
     *            location (I, J).
     * @param invalidValueSubstitute the value to substitute for all invalid
     *            data elements, or null to show the actual data elements
     *            present in each case
     * @return the pipeline
     */
    protected static FakePipeline scalarToVector(int displayKsize, Double invalidValueSubstitute)
    {
        return scalarToVector(displayKsize, invalidValueSubstitute, Function.identity());
    }

    /**
     * Create a pipeline that loads a {@link Layer} that contains scalar double
     * values, and applies the specified transform. The original layer is
     * displayed as a scalar, while the transformed one is displayed as a
     * vector.
     *
     * @param displayKsize the number of elements to display at each indexed
     *            location (I, J).
     * @param invalidValueSubstitute the value to substitute for all invalid
     *            data elements, or null to show the actual data elements
     *            present in each case
     * @param transform the transform to apply to the layer
     * @return the pipeline
     */
    protected static FakePipeline scalarToVector(int displayKsize, Double invalidValueSubstitute, Function<Layer, Layer> transform)
    {
        return new FakePipeline("Load a scalar layer, display as vector layer", transform) {

            @Override
            protected Layer createLayer()
            {
                return createScalarLayer();
            }

            @Override
            protected void displayCreatedLayer(Layer layer)
            {
                displayLayer("Created layer", layer, 0, invalidValueSubstitute);
            }

            @Override
            protected void displayProcessedLayer(Layer layer)
            {
                displayLayer("Transformed layer", layer, displayKsize, invalidValueSubstitute);
            }

        };
    }

    /**
     * Create a pipeline that loads a {@link Layer} that contains vector double
     * values, displayed as a vector layer.
     * <p>
     * The caller may specify any value for displayKsize, which is the number of
     * elements displayed for each element in the vector. This can be different
     * (larger or smaller) than the actual the depth of the layer. If larger,
     * the displayed layer is padded with out-of-bounds pixel values. If
     * smaller, the higher elements are simply not displayed.
     *
     * @param displayKsize the number of elements to display at each indexed
     *            location (I, J).
     * @param invalidValueSubstitute the value to substitute for all invalid
     *            data elements, or null to show the actual data elements
     *            present in each case
     *
     * @return the pipeline
     */
    protected static FakePipeline vectorToVector(int displayKsize, Double invalidValueSubstitute)
    {
        return vectorToVector(displayKsize, invalidValueSubstitute, Function.identity());
    }

    /**
     * Create a pipeline that loads a {@link Layer} that contains vector double
     * values, displayed as a vector layer.
     * <p>
     * The caller may specify any value for displayKsize, which is the number of
     * elements displayed for each element in the vector. This can be different
     * (larger or smaller) than the actual the depth of the layer. If larger,
     * the displayed layer is padded with out-of-bounds pixel values. If
     * smaller, the higher elements are simply not displayed.
     *
     * @param displayKsize the number of elements to display at each indexed
     *            location (I, J).
     * @param invalidValueSubstitute the value to substitute for all invalid
     *            data elements, or null to show the actual data elements
     *            present in each case
     * @param transform the transform to apply to the layer
     *
     * @return the pipeline
     */
    protected static FakePipeline vectorToVector(int displayKsize, Double invalidValueSubstitute, Function<Layer, Layer> transform)
    {
        return new FakePipeline("Load a vector layer, display as vector layer", transform) {

            @Override
            protected String getPipelineTitle(Layer layer)
            {
                String title = super.getPipelineTitle(layer);
                List<Integer> dataSizes = layer.dataSizes();
                if ((dataSizes.isEmpty() && displayKsize != 0) || !Integer.valueOf(displayKsize).equals(dataSizes.get(0)))
                {
                    title += " displayed with kSize = " + displayKsize;
                }

                return title;
            }

            @Override
            protected Layer createLayer()
            {
                return createVectorLayer();
            }

            @Override
            protected void displayCreatedLayer(Layer layer)
            {
                displayLayer("Created layer", layer, TestKSize, invalidValueSubstitute);
            }

            @Override
            protected void displayProcessedLayer(Layer layer)
            {
                displayLayer("Transformed layer", layer, displayKsize, invalidValueSubstitute);
            }

        };
    }

    /**
     * Create a pipeline that loads a {@link Layer} that contains vector double
     * values, and applies the specified set of functions in the
     * {@link #processLayer(Layer)} implementation. Finally, the layer is
     * displayed as a scalar, despite the fact that the input is really a
     * vector. Only the first element in each pixel (with K index == 0) will be
     * displayed.
     *
     * @param invalidValueSubstitute the value to substitute for all invalid
     *            data elements, or null to show the actual data elements
     *            present in each case
     * @return the pipeline
     */
    protected static FakePipeline vectorToScalar(Double invalidValueSubstitute)
    {
        return vectorToScalar(invalidValueSubstitute, Function.identity());
    }

    /**
     * Create a pipeline that loads a {@link Layer} that contains vector double
     * values, and applies the specified set of functions in the
     * {@link #processLayer(Layer)} implementation. Finally, the layer is
     * displayed as a scalar, despite the fact that the input is really a
     * vector. Only the first element in each pixel (with K index == 0) will be
     * displayed.
     *
     * @param invalidValueSubstitute the value to substitute for all invalid
     *            data elements, or null to show the actual data elements
     *            present in each case
     * @param transform the transform to apply to the layer
     * @return the pipeline
     */
    protected static FakePipeline vectorToScalar(Double invalidValueSubstitute, Function<Layer, Layer> transform)
    {
        return new FakePipeline("Load a vector layer, display as scalar layer", transform) {

            @Override
            protected Layer createLayer()
            {
                return createVectorLayer();
            }

            @Override
            protected void displayCreatedLayer(Layer layer)
            {
                displayLayer("Created layer", layer, TestKSize, invalidValueSubstitute);
            }

            @Override
            protected void displayProcessedLayer(Layer layer)
            {
                displayLayer("Transformed layer", layer, 0, invalidValueSubstitute);
            }

        };
    }

    /**
     * Show a start-up message for the overall pipeline (same for each run).
     */
    protected static void displayStartupMessage()
    {
        System.out.println("Layer pipeline simulator.");
        System.out.println();
        System.out.println("In displays below, indices i and j run -1, 0, 1, 2, ... N - 1, N");
        System.out.println("Indices -1 and N should show as out-of-bounds: \"(O) " + (int) TestOOBValue + "\"");
        System.out.println();
        System.out.println("The elements themselves are a ramp of values from 0 through iSize * jSize * kSize - 1,");
        System.out.println("where iSize, jSize, kSize are the X, Y, and Z dimensions. kSize == 1 for a scalar, > 1 for a vector.");
        System.out.println("Every 5th element in each layer, starting with i = 4, j = 0, should be marked invalid \"(I)\".");
        System.out.println();
        System.out.println("      x(i) ->");
        System.out.println(" y(j)");
        System.out.println("  |");
        System.out.println("  v");
        System.out.println();
    }

    public static void main(String[] args)
    {
        displayStartupMessage();

        // Demonstrate options for how to handle "invalid" data.
        System.out.println("Create scalar layer and display all its data, even those marked as \"invalid\".");
        scalarToScalar(null).run();

        System.out.println("Create scalar layer, then display it with each \"invalid\" element replaced by \"" + //
                (int) TestInvalidValueSubstitute + "\".");
        System.out.println("Note that this does not modify the layer! It only changes how data values are extracted from the layer.");
        scalarToScalar(TestInvalidValueSubstitute).run();

        System.out.println("Create scalar layer, then display it with each \"invalid\" element replaced by ");
        System.out.println("the same value as out-of-bound pixels, \"" + (int) TestOOBValue + "\". This is what");
        System.out.println("should happen during VTK rendering, except both should use NaN, not \"" + (int) TestOOBValue + "\".");
        scalarToScalar(TestOOBValue).run();
        System.out.println();

        // Show how vector layers are handled, first where the layer is
        // displayed in its own native depth, then displayed with fewer "K"
        // values and more "K" values than are present in the layer. This
        // simulates handing data of unknown structure to a renderer that can
        // handle layers with depth.
        System.out.println("Create and show vector layer, with invalid values substituted. At each (i, j) is an array of size " + TestKSize + ".");
        vectorToVector(TestKSize, TestInvalidValueSubstitute).run();

        System.out.println("Create vector layer, but only show the first " + (TestKSize - 1) + " values out of " + TestKSize + ".");
        System.out.println("Notice there are skips in the sequences. This is right!");
        vectorToVector(TestKSize - 1, null).run();

        System.out.println("Create vector layer, but try to access and show MORE elements than are in the layer, " + (TestKSize + 1) + " instead of " + TestKSize + ".");
        System.out.println("The missing elements are shown with \"" + (int) TestOOBValue + "\"");
        vectorToVector(TestKSize + 1, null).run();
        System.out.println();

        System.out.println("Create a vector layer but treat it as if it were a scalar layer.");
        System.out.println("This simulates, e.g., loading an image cube or color image but rendering it with a monochrome renderer.");
        // Use the TouchLayer transform so it displays the vector first as a
        // vector, then as a scalar.
        vectorToScalar(null, TouchLayer).run();

        System.out.println("Create a scalar layer but treat it as if it were a vector layer.");
        System.out.println("This simulates loading a monochrome image but rendering it with a color renderer.");
        // Use the TouchLayer transform so it displays the scalar first as a
        // scalar, then as a vector.
        scalarToVector(TestKSize, null, TouchLayer).run();
        System.out.println();

        // Now show transforms. These work equally well on vector layers, but
        // it's easier to see what's going on with scalars.
        System.out.println("Back to a scalar layer, but now transform it by swapping I and J");
        scalarToScalar(null, TransformFactory.swapIJ()).run();

        System.out.println("Show effect of flipping about the Y axis");
        scalarToScalar(null, TransformFactory.flipAboutY()).run();

        System.out.println("Show effect of clockwise rotation");
        scalarToScalar(null, TransformFactory.rotateCW()).run();

        System.out.println("Show effect of counterclockwise rotation on a vector");
        vectorToVector(TestKSize, null, TransformFactory.rotateCCW()).run();

        System.out.println("Show that flipping about X AND Y, and then rotating half-way around, gets you back to the original layer");
        scalarToScalar(null, TransformFactory.rotateHalfway().compose(TransformFactory.flipAboutXY())).run();

        System.out.println("Show what happens when one multiplies valid values by a factor of 2.0,");
        System.out.println("leaving untouched out-of-bounds and \"invalid\" values.");
        scalarToScalar(null, DoubleTransformFactory.toLayerTransform(value -> {
            return 2.0 * value;
        }, LayerDoubleTransformFactory.DoubleIdentity)).run();

        System.out.println("Show what happens when one multiplies valid AND invalid values by a factor of 2.0.");
        System.out.println("In no case is math EVER attempted on out-of-bounds elements.");
        scalarToScalar(null, DoubleTransformFactory.toLayerTransform(value -> {
            return 2.0 * value;
        }, null)).run();

        System.out.println("Show what happens when one slices a vector, pulling out the middle element of the 3");
        PixelVector pv = PixelVectorFactory.of(3, TestOOBValue, null);
        vectorToVector(TestKSize, null, TransformFactory.slice(pv, 1)).run();

        System.out.println("Show what happens when a subset of a scalar layer I range = [1, 4), J range = [1, 3) is taken");
        scalarToScalar(null, TransformFactory.subset(1, 4, 1, 3)).run();

        System.out.println("Show what happens when a vector layer is trimmed by 1 on left (I) and 2 on the right (I), and 1 each from top and bottom J)");
        vectorToVector(TestKSize, null, TransformFactory.trim(1, 2, 1, 1)).run();

        System.out.println("Show what happens when a vector layer is masked by 2 on left (I) and 1 on the right (I), and 1 each from top and bottom (J)");
        vectorToVector(TestKSize, null, TransformFactory.mask(2, 1, 1, 1)).run();

        System.out.println("Show what happens when a scalar layer is resampled from 6x4 to 4x6, using nearest-neighbors.");
        scalarToScalar(null, TransformFactory.resampleNearestNeighbor(4, 6)).run();

        System.out.println("Show what happens when a scalar layer is resampled from 6x4 to 6x12, using linear interpolation.");
        scalarToScalar(null, DoubleTransformFactory.linearInterpolate(6, 12)).run();
    }

}
