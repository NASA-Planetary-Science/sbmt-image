package edu.jhuapl.sbmt.image.impl;

import edu.jhuapl.sbmt.image.api.Layer;

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

    @Override
    public boolean isValid(int i, int j)
    {
        return checkIndices(i, j);
    }

    /**
     * @param i the I index
     * @param j the J index
     * @return true if the specified indices are valid (in-bounds), false
     *         otherwise.
     */
    protected boolean checkIndices(int i, int j)
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
