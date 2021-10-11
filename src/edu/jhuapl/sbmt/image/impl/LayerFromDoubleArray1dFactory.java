package edu.jhuapl.sbmt.image.impl;

import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.PixelVectorDouble;
import edu.jhuapl.sbmt.image.impl.LayerDoubleFactory.DoubleGetter1d;

/**
 * Extension of {@link LayerFromDoubleCollection1dFactory} that creates its
 * layers starting from double[] arrays.
 *
 * @author James Peachey
 *
 */
public class LayerFromDoubleArray1dFactory extends LayerFromDoubleCollection1dFactory
{

    public LayerFromDoubleArray1dFactory()
    {
        super();
    }

    public Layer ofScalar(double[] array, int iSize, int jSize)
    {
        return ofScalar(array, ColumnIRowJ, iSize, jSize);
    }

    public Layer ofScalar(double[] array, int iSize, int jSize, LayerValidityChecker checker)
    {
        return ofScalar(array, ColumnIRowJ, iSize, jSize, checker);
    }

    public Layer ofScalar(double[] array, IJtoSingleIndex reIndexer, int iSize, int jSize)
    {
        DoubleGetter1d doubleGetter = index -> {
            return array[index];
        };

        return ofScalar(doubleGetter, reIndexer, iSize, jSize);
    }

    public Layer ofScalar(double[] array, IJtoSingleIndex reIndexer, int iSize, int jSize, LayerValidityChecker checker)
    {
        DoubleGetter1d doubleGetter = index -> {
            return array[index];
        };

        return ofScalar(doubleGetter, reIndexer, iSize, jSize, checker);
    }

    public Layer ofVector(double[] array, int iSize, int jSize, int kSize)
    {
        return ofVector(array, ColumnIRowJ, iSize, jSize, kSize);
    }

    public Layer ofVector(double[] array, int iSize, int jSize, int kSize, LayerValidityChecker checker)
    {
        return ofVector(array, ColumnIRowJ, iSize, jSize, kSize, checker);
    }

    public Layer ofVector(double[] array, IJKtoSingleIndex reIndexer, int iSize, int jSize, int kSize)
    {
        DoubleGetter1d doubleGetter = index -> {
            return array[index];
        };

        return ofVector(doubleGetter, reIndexer, iSize, jSize, kSize);
    }

    public Layer ofVector(double[] array, IJKtoSingleIndex reIndexer, int iSize, int jSize, int kSize, LayerValidityChecker checker)
    {
        DoubleGetter1d doubleGetter = index -> {
            return array[index];
        };

        return ofVector(doubleGetter, reIndexer, iSize, jSize, kSize, checker);
    }

    private double[] simulateGettingArrayFromGdal(int iSize, int jSize, int depth)
    {
        double[] array = new double[iSize * jSize * depth];

        for (int i = 0; i < iSize; ++i)
        {
            for (int j = 0; j < jSize; ++j)
            {
                for (int k = 0; k < depth; ++k)
                {
                    int index = depth * (i * jSize + j) + k;
                    array[index] = index;
                }
            }
        }
        return array;
    }

    public static void main(String[] args)
    {
        int iSize = 5;
        int jSize = 10;
        int depthReadFromFile = 3;

        LayerFromDoubleArray1dFactory factory = new LayerFromDoubleArray1dFactory();

        // Fake getting an array from a file.
        double[] arrayFromFile = factory.simulateGettingArrayFromGdal(iSize, jSize, depthReadFromFile);

        // Create a layer that is backed by the array. Diagonal elements in this
        // rectangular matrix are arbitrariliy flagged as being "invalid" by the
        // lambda function in the last parameter. For example, they could be hot
        // pixels, or as in DART's case, there are a couple modal flags
        // that indicate a pixel should be ignored.
        boolean forceScalarLayer = false;

        Layer layer;
        if (forceScalarLayer)
        {
            layer = factory.ofScalar(arrayFromFile, jSize, iSize, (l, i, j) -> {
                return i != j;
            });
        }
        else
        {

            layer = factory.ofVector(arrayFromFile, jSize, iSize, depthReadFromFile, (l, i, j) -> {
                return i != j;
            });
        }

        // Now assume the layer is bundled into a stack of layers, possibly
        // processed in whatever ways we want, etc.
        //
        // ...
        //
        // Then, sometime later, this layer has been handed to an arbitrary
        // renderer. The depth of the layer the renderer expects may or may not
        // match what the layer actually *has*.
        int depthToRender = depthReadFromFile;
//        depthToRender = 1;

        // If we were actually going to render it using VTK, we would build
        // another 1d array like what we started with, but for purposes of this
        // demo, use a nested list.
        List<List<List<Double>>> arrayToRender = new ArrayList<>();

        // To avoid overhead, create just one pixel here, outside of the
        // loops, and reuse it to retrieve the values from the layer.
        PixelVectorDouble p = new PixelVectorDoubleFactory().of(depthToRender, -1000., -2000.);

        // Include out-of-bounds indices in the loop to demonstrate how that
        // gets handled.
        for (int i = -1; i <= layer.iSize(); ++i)
        {
            // Include out-of-bounds indices in the loop to demonstrate how that
            // gets handled.
            List<List<Double>> slice = new ArrayList<>();
            for (int j = -1; j <= layer.jSize(); ++j)
            {
                // This retrieves the data from the layer and stuffs it into the
                // pixel.
                layer.get(i, j, p);
                if (!p.isValid())
                {
//                    p.setIsValid(true);
                }

                try
                {
                    p.get(p.size()).set(-7.);
                    System.err.println("Oops, expected this to throw.");
                }
                catch (IndexOutOfBoundsException e)
                {
                    // This is as it should be -- the index was
                    // one-past-last.
                }

                List<Double> list = new ArrayList<>(p.size());
                for (int k = 0; k < p.size(); ++k)
                {
                    list.add(p.get(k).get());
                }
                slice.add(list);
            }
            arrayToRender.add(slice);
        }

        // Now let's "render" the image by printing the pixels to the console.
        for (int i = 0; i < arrayToRender.size(); ++i)
        {
            List<List<Double>> slice = arrayToRender.get(i);
            StringBuilder builder = new StringBuilder();
            String delim = "";
            for (int j = 0; j < slice.size(); ++j)
            {
                List<Double> list = slice.get(j);
                builder.append(delim);
                builder.append("(");
                delim = "";
                for (int k = 0; k < list.size(); ++k)
                {
                    builder.append(delim);
                    builder.append(String.format("%7.1f", list.get(k)));
                    delim = ", ";
                }
                builder.append(")");
            }
            System.out.println(builder.toString());
        }
    }

}
