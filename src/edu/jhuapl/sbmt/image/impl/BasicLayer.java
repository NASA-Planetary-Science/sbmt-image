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
 * Abstract base implementation of {@link Layer}, which includes partial support
 * for getting pixels of type {@link PixelVector}, in which each individual
 * element is retrieved as a (presumably scalar-valued) {@link Pixel} element.
 *
 * @author James Peachey
 *
 */
public abstract class BasicLayer implements Layer
{
    protected static final List<Integer> ScalarDataSizes = ImmutableList.of(Integer.valueOf(1));

    /**
     * A single instance of an immutable empty layer.
     */
    private static final Layer EmptyLayer = new BasicLayer(0, 0) {

        @Override
        public List<Integer> dataSizes()
        {
            return ScalarDataSizes;
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

        @Override
        public void get(int i, int j, Pixel p)
        {
            Preconditions.checkNotNull(p);
            throw new IllegalArgumentException();
        }

    };

    /**
     * Return an immutable empty layer, that is, one with zero size in I and J,
     * no pixels, no supported pixel types, etc.
     */
    public static Layer emptyLayer()
    {
        return EmptyLayer;
    }

    private final int iSize;
    private final int jSize;

    /**
     * Constructor that creates a layer using the specified dimensions. The
     * caller must ensure iSize and jSize are non-negative.
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
     * The base implementation returns a value consistent with scalar data: a
     * list with one element equal to the integer 1.
     */
    @Override
    public List<Integer> dataSizes()
    {
        return ScalarDataSizes;
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

    /**
     * The base implementation checks bounds on its (I, J) indices and calls
     * {@link Pixel#setInBounds(boolean)} accordingly. If the pixel is
     * in-bounds, it delegates the actual getting to pixel subtype-specific
     * methods {@link #getElement(int, int, int, Pixel)} or
     * {@link #getVector(int, int, PixelVector)}, respectively.
     */
    @Override
    public void get(int i, int j, Pixel p)
    {
        Preconditions.checkNotNull(p);

        boolean inBounds = checkIndices(i, j, p);

        if (inBounds)
        {
            if (p instanceof PixelVector pv)
            {
                getVector(i, j, pv);
            }
            else
            {
                getElement(i, j, 0, p);
            }
        }
        p.setInBounds(inBounds);
    }

    /**
     * The base implementation always throws an exception. Override this to set
     * the value in the specified pixel instance. This method does not need to
     * check the bounds of the I or J index.
     * <p>
     * The K index may or may not be used, depending on the implementation.
     *
     * @param i the I index
     * @param j the J index
     * @param k the K index
     * @param p the output pixel
     */
    protected void getElement(int i, int j, int k, Pixel p)
    {
        throw new IllegalArgumentException();
    }

    /**
     * Override this to set the value in the specified vector pixel instance.
     * This method does not need to check validity or bounds of the (I, J) index
     * pair.
     * <p>
     * However, when overriding this method, it is necessary to consider that
     * the output {@link PixelVector} may have a different number of elements
     * than this {@link Layer} implementation has at the specified (I, J) inex
     * pair. If the output vector is too large, the extra elements shall be set
     * to have the out-of-bounds value, and shall be marked as out-of-bounds. If
     * the output vector is too small, implementations shall not attempt to fill
     * elements outside its defined range.
     *
     * @param i the I index
     * @param j the J index
     * @param pv the output (vector) pixel
     */
    protected void getVector(int i, int j, PixelVector pv)
    {
        int kSize = kSize(i, j);

        for (int k = 0; k < pv.size(); ++k)
        {
            // Handle the case in which the number of elements in the pixel
            // exceeds the number in this layer at this position.
            boolean inBounds = checkIndex(k, 0, kSize);

            Pixel p = pv.get(k);

            if (inBounds)
            {
                // This layer has data for this pixel.
                getElement(i, j, k, p);
            }
            p.setInBounds(inBounds);
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
     * @return true if the specified indices are in-bounds, false otherwise.
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
     * @param minValid the minimum value for the index
     * @param maxValid one-past the maximum value for the index
     * @return true if the specified index is in-bounds, false otherwise.
     */
    protected boolean checkIndex(int index, int minValid, int maxValid)
    {
        return index >= minValid && index < maxValid;
    }

    @Override
    public String toString()
    {
        List<Integer> dataSizes = dataSizes();
        String delim = ", ";

        StringBuilder builder = new StringBuilder();
        Integer vecSize = dataSizes.get(0);
        if (dataSizes.size() == 1)
        {
            builder.append(vecSize > 1 ? "Vector Layer(" : "Scalar Layer(");
        }
        else
        {
            builder.append("Multi-dim Layer(");
        }

        builder.append(iSize());
        builder.append(delim);
        builder.append(jSize());
        builder.append(")");

        if (dataSizes.size() == 1)
        {
            if (vecSize > 1)
            {
                builder.append(" x ");
                builder.append(vecSize);
            }
        }
        else
        {
            builder.append(" x (");
            delim = "";
            for (Integer size : dataSizes)
            {
                builder.append(delim);
                builder.append(size);
                delim = ", ";
            }
            builder.append(")");
        }

        return builder.toString();
    }

}
