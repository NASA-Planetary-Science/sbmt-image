package edu.jhuapl.sbmt.image.impl;

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
 *
 * @author James Peachey
 *
 */
public class LayerDoubleTransformFactory
{

    protected static final PixelDoubleFactory ScalarPixelFactory = new PixelDoubleFactory();

    protected static final PixelVectorDoubleFactory VectorPixelFactory = new PixelVectorDoubleFactory();

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
            return new ForwardingLayer(layer) {

                @Override
                public void get(int i, int j, Pixel p)
                {
                    if (p instanceof PixelDouble)
                    {
                        PixelDouble pd = (PixelDouble) p;

                        // Make a copy of the pixel, and get its state from the
                        // layer.
                        PixelDouble tmpPd = ScalarPixelFactory.of(pd);
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
                        PixelVectorDouble pd = (PixelVectorDouble) p;

                        // Make a copy of the pixel, and get its state from the
                        // layer.
                        PixelVectorDouble tmpPd = VectorPixelFactory.of(pd);
                        layer.get(i, j, tmpPd);

                        boolean valid = tmpPd.isValid();
                        boolean inBounds = tmpPd.isInBounds();
                        double outOfBoundsValue = tmpPd.getOutOfBoundsValue();
                        int kSize = tmpPd.size();

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
                                value = valueTransform.apply(tmpPd.getStoredValue(k));
                            }
                            else
                            {
                                // Value is not valid, so use the invalid value
                                // transform.
                                value = finalInvalidValueTransform.apply(tmpPd.getStoredValue(k));
                            }

                            pd.set(k, value);
                            pd.setIsValid(valid);
                            pd.setInBounds(inBounds);
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
