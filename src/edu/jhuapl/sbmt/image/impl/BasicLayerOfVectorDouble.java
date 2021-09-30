package edu.jhuapl.sbmt.image.impl;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelDouble;
import edu.jhuapl.sbmt.image.api.PixelVectorDouble;

/**
 * Abstract base implementation of {@link LayerOfDouble} that assumes the native
 * form of the underlying data is a one-dimensional array double[]. The single
 * index values of this one-dimensional array are derived from the layer's (I,
 * J) indices.
 * <p>
 * The base implementation of {@link #getDouble(int, int)} never throws an
 * exception, but instead returns {@link Double#NaN} if the specified indices
 * are invalid. This behavior may be overridden in subclasses by overriding
 * either {@link #getDouble(int, int)} or {@link #getOutOfBoundsValue()}, as
 * described below.
 *
 * @author James Peachey
 *
 */
public abstract class BasicLayerOfVectorDouble extends BasicLayer
{

    private static final Set<Class<?>> AcceptedPixelTypes = ImmutableSet.of(PixelDouble.class, PixelVectorDouble.class);

    /**
     * Constructor that creates a layer using the specified dimensions and
     * validity checker. The caller must ensure iSize and jSize are
     * non-negative. The isValid argument may be null, in which case all
     * in-bounds values are considered valid.
     *
     * @param iSize the number of in-bounds values of the I index
     * @param jSize the number of in-bounds values of the J index
     */
    protected BasicLayerOfVectorDouble(int iSize, int jSize)
    {
        super(iSize, jSize);
    }

    @Override
    public Set<Class<?>> getPixelTypes()
    {
        return AcceptedPixelTypes;
    }

    public void get(int i, int j, Pixel p)
    {
        Preconditions.checkNotNull(p);

        boolean isInBounds = checkIndices(i, j);
        boolean isValid = isInBounds ? isValid(i, j) : false;

        p.setIsValid(isValid);
        p.setInBounds(isInBounds);

        if (p instanceof PixelDouble)
        {
            PixelDouble dp = (PixelDouble) p;

            double value = isInBounds ? doGetDouble(i, j, 0) : dp.getOutOfBoundsValue();

            dp.set(value);

        }
        else if (p instanceof PixelVectorDouble)
        {
            PixelVectorDouble vdp = (PixelVectorDouble) p;

            double outOfBoundsValue = vdp.getOutOfBoundsValue();

            for (int k = 0; k < vdp.size(); ++k)
            {
                // Now need to ensure the number of items in the pixel does not
                // exceed the number in this layer.
                if (!checkIndex(k, 0, getKsize(i, j)))
                {
                    isInBounds = false;
                }

                double value = isInBounds ? doGetDouble(i, j, k) : outOfBoundsValue;

                vdp.set(k, value);
            }

        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    protected abstract int getKsize(int i, int j);

    protected abstract double doGetDouble(int i, int j, int k);

    public String toString()
    {
        return "LayerOfVectorDouble(" + iSize() + ", " + jSize() + ")";
    }

}
