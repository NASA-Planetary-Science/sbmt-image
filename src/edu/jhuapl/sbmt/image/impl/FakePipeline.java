package edu.jhuapl.sbmt.image.impl;

import java.util.function.Function;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelDouble;
import edu.jhuapl.sbmt.image.api.PixelVectorDouble;
import edu.jhuapl.sbmt.image.impl.LayerDoubleFactory.DoubleGetter2d;
import edu.jhuapl.sbmt.image.impl.LayerDoubleFactory.DoubleGetter3d;

/**
 * Sample/test class for demonstrate how to load and manipulate {@link Layer}s
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

    public FakePipeline(String pipelineTitle)
    {
        super();

        this.pipelineTitle = pipelineTitle;
    }

    /**
     * Each pipeline simulates loading a layer, then processing the layer,
     * displaying what the layer looks like after each step.
     */
    public void run()
    {
        Layer layer = loadLayer();

        System.out.println("************************************************");
        System.out.println(getPipelineTitle(layer));
        System.out.println("************************************************");

        display("Loaded layer:", layer);
        System.out.println();

        Layer processedLayer = processLayer(layer);

        if (processedLayer != layer)
        {
            display("Processed layer:", processedLayer);
            System.out.println();
        }
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
     * Load a {@link Layer} to be used in a pipeline run. Details are up to the
     * concrete pipeline implementation. This method may return any type of
     * {@link Layer} (scalar or vector).
     *
     * @return the loaded layer
     */
    protected abstract Layer loadLayer();

    /**
     * Apply a series of processing/transform steps to a layer. Each concrete
     * pipeline implementation defines the steps it performs.
     *
     * @param layer the layer to process
     * @return the layer resulting from the the final processing step
     */
    protected abstract Layer processLayer(Layer layer);

    /**
     * Display a message concerning the specifed {@link Layer}.
     *
     * @param message the message
     * @param layer the layer
     */
    protected abstract void display(String message, Layer layer);

    /**
     * Display the state of a {@link Layer}, prefaced by a message, and using
     * the specified {@link Pixel} instance to retrieve data from the layer.
     *
     * @param message the message used as a preface
     * @param layer the layer whose state to display
     * @param p the pixel used to get data from the layer
     */
    protected void displayState(String message, Layer layer, Pixel p)
    {
        System.out.println(message);
        for (int row = -1; row <= layer.jSize(); ++row)
        {
            StringBuilder builder = new StringBuilder();
            String delim = "";
            for (int column = -1; column <= layer.iSize(); ++column)
            {
                layer.get(column, row, p);
                builder.append(delim);
                builder.append(p);
                delim = ", ";
            }
            System.out.println(builder.toString());
        }
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
    protected Layer ofScalar(int iSize, int jSize, LayerValidityChecker checker)
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
    protected Layer ofVector(int iSize, int jSize, int kSize, LayerValidityChecker checker)
    {
        DoubleGetter3d doubleGetter = (i, j, k) -> {
            return kSize * LayerFromDoubleCollection1dFactory.ColumnIRowJ.getIndex(i, j, iSize, jSize) + k;
        };

        return checker != null ? //
                LayerFactory.ofVector(doubleGetter, iSize, jSize, kSize, checker) : //
                LayerFactory.ofVector(doubleGetter, iSize, jSize, kSize);
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
     * Arbitrary validity checker -- mark every 5th column "invalid", starting
     * with column 4.
     */
    protected static final LayerValidityChecker TestChecker = (layer, i, j) -> {
        return (j * layer.iSize() + i + 1) % 5 != 0;
    };

    /**
     * Create a pipeline that loads a {@link Layer} that contains scalar double
     * values, and applies the specified set of functions in the
     * {@link #processLayer(Layer)} implementation.
     * <p>
     * After the transforms are applied, the layer is displayed in the natural
     * way, as a scalar.
     *
     * @param invalidValueSubstitute the value to substitute for all invalid
     *            data elements, or null to show the actual data elements
     *            present in each case
     * @param functions to apply to the layer
     *
     * @return the pipeline
     */
    @SafeVarargs
    protected static FakePipeline scalarToScalar(Double invalidValueSubstitute, Function<Layer, Layer>... functions)
    {
        return new FakePipeline("Load a scalar layer, display as scalar layer") {

            @Override
            protected Layer loadLayer()
            {
                return ofScalar(TestISize, TestJSize, TestChecker);
            }

            @Override
            protected Layer processLayer(Layer layer)
            {
                for (Function<Layer, Layer> f : functions)
                {
                    layer = f.apply(layer);
                }

                return layer;
            }

            @Override
            protected void display(String message, Layer layer)
            {
                PixelDouble p;
                if (invalidValueSubstitute != null)
                {
                    p = PixelScalarFactory.of(0.0, TestOOBValue, invalidValueSubstitute);
                }
                else
                {
                    p = PixelScalarFactory.of(0.0, TestOOBValue);
                }

                displayState(message, layer, p);
            }

        };
    }

    /**
     * Create a pipeline that loads a {@link Layer} that contains scalar double
     * values, and applies the specified set of functions in the
     * {@link #processLayer(Layer)} implementation.
     * <p>
     * After the transforms are applied, the layer is displayed as if it were a vector. The extra layers
     * that are not present in the input are created with elements padded with
     * the out-of-bounds value.
     *
     * @param invalidValueSubstitute the value to substitute for all invalid
     *            data elements, or null to show the actual data elements
     *            present in each case
     * @param displayKsize the number of elements to display at each indexed
     *            location (I, J).
     * @param functions to apply to the layer
     *
     * @return the pipeline
     */
    @SafeVarargs
    protected static FakePipeline scalarToVector(Double invalidValueSubstitute, int displayKsize, Function<Layer, Layer>... functions)
    {
        return new FakePipeline("Load a scalar layer, display as vector layer") {

            @Override
            protected Layer loadLayer()
            {
                return ofScalar(TestISize, TestJSize, TestChecker);
            }

            @Override
            protected Layer processLayer(Layer layer)
            {
                for (Function<Layer, Layer> f : functions)
                {
                    layer = f.apply(layer);
                }

                return layer;
            }

            @Override
            protected void display(String message, Layer layer)
            {
                PixelVectorDouble p;
                if (invalidValueSubstitute != null)
                {
                    p = PixelVectorFactory.of(displayKsize, TestOOBValue, invalidValueSubstitute);
                }
                else
                {
                    p = PixelVectorFactory.of(displayKsize, TestOOBValue);
                }

                displayState(message, layer, p);
            }

        };
    }

    /**
     * Create a pipeline that loads a {@link Layer} that contains vector double
     * values, and applies the specified set of functions in the
     * {@link #processLayer(Layer)} implementation. Finally, the layer is
     * displayed in the natural way, as a vector.
     * <p>
     * However, the caller may specify any value for displayKsize, which is the
     * number of elements displayed for each element in the vector. This can be
     * different (larger or smaller) than the actual the depth of the layer. If
     * larger, the displayed layer is padded with out-of-bounds pixel values. If
     * smaller, the higher elements are simply not displayed.
     *
     * @param invalidValueSubstitute the value to substitute for all invalid
     *            data elements, or null to show the actual data elements
     *            present in each case
     * @param displayKsize the number of elements to display at each indexed
     *            location (I, J).
     * @param functions to apply to the layer
     *
     * @return the pipeline
     */
    @SafeVarargs
    protected static FakePipeline vectorToVector(Double invalidValueSubstitute, int displayKsize, Function<Layer, Layer>... functions)
    {
        return new FakePipeline("Load a vector layer, display as vector layer") {

            @Override
            protected String getPipelineTitle(Layer layer)
            {
                String title = super.getPipelineTitle(layer);
                if (!Integer.valueOf(displayKsize).equals(layer.dataSizes().get(0)))
                {
                    title += " displayed with kSize = " + displayKsize;
                }

                return title;
            }

            @Override
            protected Layer loadLayer()
            {
                return ofVector(TestISize, TestJSize, TestKSize, TestChecker);
            }

            @Override
            protected Layer processLayer(Layer layer)
            {
                for (Function<Layer, Layer> f : functions)
                {
                    layer = f.apply(layer);
                }

                return layer;
            }

            @Override
            protected void display(String message, Layer layer)
            {
                PixelVectorDouble p;
                if (invalidValueSubstitute != null)
                {
                    p = PixelVectorFactory.of(displayKsize, TestOOBValue, invalidValueSubstitute);
                }
                else
                {
                    p = PixelVectorFactory.of(displayKsize, TestOOBValue);
                }

                displayState(message, layer, p);
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
     * @param functions to apply to the layer
     *
     * @return the pipeline
     */
    @SafeVarargs
    protected static FakePipeline vectorToScalar(Double invalidValueSubstitute, Function<Layer, Layer>... functions)
    {
        return new FakePipeline("Load a vector layer, display as scalar layer") {

            @Override
            protected String getPipelineTitle(Layer layer)
            {
                String title = super.getPipelineTitle(layer) + " displayed as a scalar layer";

                return title;
            }

            @Override
            protected Layer loadLayer()
            {
                return ofVector(TestISize, TestJSize, TestKSize, TestChecker);
            }

            @Override
            protected Layer processLayer(Layer layer)
            {
                for (Function<Layer, Layer> f : functions)
                {
                    layer = f.apply(layer);
                }

                return layer;
            }

            @Override
            protected void display(String message, Layer layer)
            {
                PixelDouble p;
                if (invalidValueSubstitute != null)
                {
                    p = PixelScalarFactory.of(0.0, TestOOBValue, invalidValueSubstitute);
                }
                else
                {
                    p = PixelScalarFactory.of(0.0, TestOOBValue);
                }

                displayState(message, layer, p);
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
        System.out.println("Indices -1 and N should show as out-of-bounds: \"(O) " + TestOOBValue + "\"");
        System.out.println("Every 5th element in each layer, starting with i = 4, j = 0, should show as invalid (I).");
        System.out.println("The elements themselves are a ramp of values from 0 through iSize * jSize * kSize - 1,");
        System.out.println("where iSize, jSize, kSize are the X, Y, and Z dimensions. kSize == 1 for a scalar, > 1 for a vector.");
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
        System.out.println();
        scalarToScalar(null).run();

        System.out.println();
        System.out.println("Create scalar layer, then display it with each \"invalid\" element replaced by \"" + TestInvalidValueSubstitute + "\".");
        System.out.println("Note that this does not modify the layer! It only changes how data values are extracted from the layer.");
        System.out.println();
        scalarToScalar(TestInvalidValueSubstitute).run();

        System.out.println();
        System.out.println("Create scalar layer, then display it with each \"invalid\" element replaced by ");
        System.out.println("the same value as out-of-bound pixels, \"" + TestOOBValue + "\". This is what");
        System.out.println("should happen during VTK rendering, except both should use NaN, not \"" + TestOOBValue + "\".");
        System.out.println();
        scalarToScalar(TestOOBValue).run();

        // Show how vector layers are handled, first where the layer is
        // displayed in its own native depth, then displayed with fewer "K"
        // values and more "K" values than are present in the layer. This
        // simulates handing data of unknown structure to a renderer that can
        // handle layers with depth.
        System.out.println();
        System.out.println();
        System.out.println("Create and show vector layer. At each (i, j) is an array of size " + TestKSize + ".");
        vectorToVector(null, TestKSize).run();

        System.out.println();
        System.out.println("Create and show vector layer, but only the first " + (TestKSize - 1) + " values out of " + TestKSize + ".");
        System.out.println("Notice there are skips in the sequences. This is right!");
        vectorToVector(null, TestKSize - 1).run();

        System.out.println();
        System.out.println("Create and show vector layer, but try to access MORE elements than are in the layer, " + (TestKSize + 1) + " instead of " + TestKSize + ".");
        System.out.println("The missing elements are shown with \"(O) " + TestOOBValue + "\"");
        vectorToVector(null, TestKSize + 1).run();

        System.out.println();
        System.out.println("Create a vector layer but treat it as if it were a scalar layer.");
        System.out.println("This simulates loading an image cube or color image but rendering it with a monochrome renderer.");
        vectorToScalar(null).run();

        System.out.println();
        System.out.println("Create a scalar layer but treat it as if it were a vector layer.");
        System.out.println("This simulates loading a monochrome image but rendering it with a color renderer.");
        scalarToVector(null, TestKSize).run();

        // Now show transforms. These work equally well on vector layers, but
        // it's easier to see what's going on with scalars.
        System.out.println();
        System.out.println();
        System.out.println("Back to a scalar layer, but now transform it by swapping I and J");
        scalarToScalar(null, TransformFactory.swapIJ()).run();

        System.out.println();
        System.out.println("Show effect of flipping about the Y axis");
        scalarToScalar(null, TransformFactory.flipAboutY()).run();

        System.out.println();
        System.out.println("Show effect of clockwise rotation");
        scalarToScalar(null, TransformFactory.rotateCW()).run();

        System.out.println();
        System.out.println("Show that flipping about X AND Y, and then rotating half-way around, gets you back to the original state");
        scalarToScalar(null, TransformFactory.flipAboutXY(), TransformFactory.rotateHalfway()).run();

        System.out.println();
        System.out.println("Show what happens when one multiplies valid values by a factor of 2.0,");
        System.out.println("leaving untouched out-of-bounds and \"invalid\" values.");
        scalarToScalar(null, DoubleTransformFactory.toLayerTransform(value -> {
            return 2.0 * value;
        }, LayerDoubleTransformFactory.DoubleIdentity)).run();

        System.out.println();
        System.out.println("Show what happens when one multiplies valid AND invalid values by a factor of 2.0.");
        System.out.println("In no case is math EVER attempted on out-of-bounds elements.");
        scalarToScalar(null, DoubleTransformFactory.toLayerTransform(value -> {
            return 2.0 * value;
        }, null)).run();
    }

}
