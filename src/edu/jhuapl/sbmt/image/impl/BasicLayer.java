package edu.jhuapl.sbmt.image.impl;

import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelVector;

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
public abstract class BasicLayer implements Layer
{

    private static final Layer EmptyLayer = new BasicLayer(0, 0) {

        @Override
        public List<Integer> dataSizes()
        {
            return ImmutableList.of(Integer.valueOf(1));
        }

        @Override
        public Set<Class<?>> getPixelTypes()
        {
            return ImmutableSet.of();
        }

        @Override
        protected boolean checkIndices(int i, int j, Pixel p)
        {
            return false;
        }

    };

    /**
     * Return an immutable empty layer, that is, one with no pixels, empty data
     * sizes, etc.
     */
    public static Layer emptyLayer()
    {
        return EmptyLayer;
    }

    private final int iSize;
    private final int jSize;

    /**
     * Constructor that creates a layer using the specified dimensions and
     * validity checker. The caller must ensure iSize and jSize are
     * non-negative. The isValid argument may be null, in which case all
     * in-bounds values are considered valid.
     *
     * @param iSize the number of in-bounds values of the I index
     * @param jSize the number of in-bounds values of the J index
     */
    protected BasicLayer(int iSize, int jSize)
    {
        super();

        this.iSize = iSize;
        this.jSize = jSize;
    }

    @Override
    public int iSize()
    {
        return iSize;
    }

    @Override
    public int jSize()
    {
        return jSize;
    }

    /**
     * Override this if the size of non-scalar pixels varies by (I, J).
     *
     * @param i the I index
     * @param j the J index
     * @return the size in the K dimension
     */
    protected int kSize(int i, int j)
    {
        return dataSizes().get(0);
    }

    @Override
    public boolean isValid(int i, int j)
    {
        return checkIndices(i, j, null);
    }

    /**
     * The base implementation handles whether or not the pixel location is
     * "valid", and whether it is in-bounds in its (I, J) indices. It delegates
     * the actual getting to pixel subtype-specific methods.
     */
    @Override
    public void get(int i, int j, Pixel p)
    {
        Preconditions.checkNotNull(p);

        p.setIsValid(isValid(i, j));

        if (checkIndices(i, j, p))
        {
            p.setInBounds(true);

            if (p instanceof PixelVector pv)
            {
                getVector(i, j, pv);
            }
            else
            {
                getScalar(i, j, p);
            }
        }
        else
        {
            p.setInBounds(false);
        }
    }

    /**
     * Override this to set the value in the specified pixel instance. This
     * method does not need to check validity or bounds of the (I, J) index
     * pair.
     *
     * @param i the I index
     * @param j the J index
     * @param p the output pixel
     */
    protected void getScalar(int i, int j, Pixel p)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Override this to set the value in the specified pixel instance. This
     * method does not need to check validity or bounds of the (I, J) index
     * pair. However, when overriding this method, it is necessary to avoid
     * exceptions should the size of the specified pixel be insufficient to hold
     * the full vector present in the layer at index (I, J).
     *
     * @param i the I index
     * @param j the J index
     * @param pv the output pixel
     */
    protected void getVector(int i, int j, PixelVector pv)
    {
        for (int k = 0; k < pv.size(); ++k)
        {
            Pixel p = pv.get(k);
            if (checkIndex(k, 0, kSize(i, j)))
            {
                // This layer has data for this pixel.
                getScalar(i, j, p);
            }
            else
            {
                // This layer does not have data for this pixel.
                // Do use the layer's validity for this location.
                p.setIsValid(isValid(i, j));
                // K index is out of bounds.
                p.setInBounds(false);
            }

        }
    }

    /**
     * Check the inputs to determine whether the indices are in-bounds. The base
     * implementation checks only the I and J indices and ignores the
     * {@link Pixel} argument.
     *
     * @param i the I index
     * @param j the J index
     * @param p the pixel to check
     * @return true if the specified indices are valid (in-bounds), false
     *         otherwise.
     */
    protected boolean checkIndices(int i, int j, Pixel p)
    {
        if (!checkIndex(i, 0, iSize()))
        {
            return false;
        }

        if (!checkIndex(j, 0, jSize()))
        {
            return false;
        }

        return true;
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

    @Override
    public String toString()
    {
        return "Layer(" + iSize() + ", " + jSize() + ")";
    }

}
