package edu.jhuapl.sbmt.image.impl;

import java.util.List;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelDouble;
import edu.jhuapl.sbmt.image.api.PixelVectorDouble;
import edu.jhuapl.sbmt.image.impl.LayerTransformFactory.ForwardingLayer;

/**
 * Factory class for creating {@link Layer} transforms (that is,
 * {@link Function} instances that operate on a layer and return a new layer).
 * <p>
 * This factory provides transforms that involve changes to the data associated
 * with a pixel. It can operate on layers that support pixels of type
 * {@link PixelDouble} and {@link PixelVectorDouble}.
 * <p>
 * The {@link Function#apply(Layer)} methods for all the functions returned by
 * this factory will return null if called with a null layer argument.
 *
 * @author James Peachey
 *
 */
public class LayerDoubleTransformFactory
{

    protected static final PixelDoubleFactory PixelScalarFactory = new PixelDoubleFactory();

    protected static final PixelVectorDoubleFactory PixelVectorFactory = new PixelVectorDoubleFactory();

    @FunctionalInterface
    public interface DoubleTransform
    {
        double apply(double value);
    }

    public static final DoubleTransform DoubleIdentity = value -> {
        return value;
    };

    public LayerDoubleTransformFactory()
    {
        super();
    }

    /**
     * Convert the kernel of a transform, (a {@link DoubleTransform}, which
     * operates on a scalar double value) into a {@link Function} that operates
     * on a {@link Layer} by transforming values within the pixel.
     * <p>
     * The base implementation handles {@link PixelDouble} and
     * {@link PixelVectorDouble} pixels. When overriding this method, take care
     * to ensure that out-of-bounds values are not used in computations, and
     * that the correct {@link DoubleTransform} instance is used for valid and
     * invalid values.
     *
     * @param valueTransform the transform to use on valid values
     * @param invalidValueTransform the transform to use on invalid values (if
     *            null, the regular valueTransform will be used for invalid
     *            values as well).
     * @return the layer-to-layer transform
     */
    public Function<Layer, Layer> toLayerTransform(DoubleTransform valueTransform, DoubleTransform invalidValueTransform)
    {
        Preconditions.checkNotNull(valueTransform);

        DoubleTransform finalInvalidValueTransform;
        if (invalidValueTransform == null)
        {
            finalInvalidValueTransform = valueTransform;
        }
        else
        {
            finalInvalidValueTransform = invalidValueTransform;
        }

        Function<Layer, Layer> function = layer -> {
            if (layer == null)
            {
                return null;
            }

            return new ForwardingLayer(layer) {

                @Override
                public void get(int i, int j, Pixel p)
                {
                    if (p instanceof PixelDouble)
                    {
                        PixelDouble pd = (PixelDouble) p;

                        // Make a copy of the pixel, and get its state from the
                        // layer.
                        PixelDouble tmpPd = PixelScalarFactory.of(pd);
                        layer.get(i, j, tmpPd);

                        boolean valid = tmpPd.isValid();
                        boolean inBounds = tmpPd.isInBounds();
                        double outOfBoundsValue = tmpPd.getOutOfBoundsValue();

                        // Handle all the special cases.
                        double value;
                        if (!inBounds)
                        {
                            // Do not transform an out-of-bounds value, ever.
                            // Pass through the canonical value.
                            value = outOfBoundsValue;
                        }
                        else if (valid)
                        {
                            // Value is in-bounds and valid, so use the regular
                            // value transform.
                            value = valueTransform.apply(tmpPd.getStoredValue());
                        }
                        else
                        {
                            // Value is not valid, so use the invalid value
                            // transform.
                            value = finalInvalidValueTransform.apply(tmpPd.getStoredValue());
                        }

                        pd.set(value);
                        pd.setIsValid(valid);
                        pd.setInBounds(inBounds);
                    }
                    else if (p instanceof PixelVectorDouble)
                    {
                        PixelVectorDouble pvd = (PixelVectorDouble) p;

                        // Make a copy of the pixel, and get its state from the
                        // layer.
                        PixelVectorDouble tmpPvd = PixelVectorFactory.of(pvd);
                        layer.get(i, j, tmpPvd);

                        boolean valid = tmpPvd.isValid();
                        boolean inBounds = tmpPvd.isInBounds();
                        double outOfBoundsValue = tmpPvd.getOutOfBoundsValue();
                        int kSize = tmpPvd.size();

                        for (int k = 0; k < kSize; ++k)
                        {
                            // Handle all the special cases.
                            double value;
                            if (!inBounds || !checkIndex(k, 0, kSize))
                            {
                                // Do not transform an out-of-bounds value,
                                // ever. Pass through the canonical value.
                                value = outOfBoundsValue;
                            }
                            else if (valid)
                            {
                                // Value is in-bounds and valid, so use the
                                // regular value transform.
                                value = valueTransform.apply(tmpPvd.get(k).getStoredValue());
                            }
                            else
                            {
                                // Value is not valid, so use the invalid value
                                // transform.
                                value = finalInvalidValueTransform.apply(tmpPvd.get(k).getStoredValue());
                            }

                            pvd.get(k).set(value);
                            pvd.setIsValid(valid);
                            pvd.setInBounds(inBounds);
                        }
                    }
                    else
                    {
                        throw new UnsupportedOperationException();
                    }
                }

            };

        };

        return function;
    }

    public Function<Layer, Layer> slice(int index, double outOfBoundsValue, Double invalidValue)
    {
        return layer -> {
            if (layer == null)
            {
                return null;
            }

            Preconditions.checkArgument(0 <= index);

            List<Integer> dataSizes = layer.dataSizes();
            Preconditions.checkNotNull(dataSizes);

            Integer size;
            if (dataSizes.isEmpty())
            {
                // Slicing a scalar layer is OK, though that will force index to be 0 below.
                size = Integer.valueOf(1);
            }
            else
            {
                // Slicing a vector layer is OK.
                Preconditions.checkArgument(dataSizes.size() == 1);
                size = dataSizes.get(0);
            }

            // Confirm the layer has at least *some* data.
            Preconditions.checkNotNull(size);
            Preconditions.checkArgument(size > index);

            PixelVectorDouble p = PixelVectorFactory.of(size, outOfBoundsValue, invalidValue);

            return new BasicLayerOfDouble(layer.iSize(), layer.jSize()) {

                @Override
                protected double doGetDouble(int i, int j)
                {
                    layer.get(i, j, p);

                    return p.get(index).get();
                }

            };

        };
    }

    /**
     * Return a flag that indicates whether the specified index is in the
     * half-open range [minValue, maxValue).
     *
     * @param index the index value to check
     * @param minValid the minimum valid value for the index
     * @param maxValid one-past the maximum valid value for the index
     * @return true if the specified index is valid (in-bounds), false
     *         otherwise.
     */
    protected boolean checkIndex(int index, int minValid, int maxValid)
    {
        return index >= minValid && index < maxValid;
    }

}
