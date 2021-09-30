package edu.jhuapl.sbmt.image.impl;

import edu.jhuapl.sbmt.image.api.Layer;

/**
 * Base class for factory classes for creating {@link Layer} instances starting
 * from a singly-indexed data collection, such as an array or list. This
 * provides utility interfaces and methods to convert two indices (I, J) into a
 * single index that accurately retrieves data from the underlying
 * singly-indexed data structure.
 * <p>
 * This base implementation makes no assumptions about the type of data held by
 * the layer.
 *
 * @author James Peachey
 *
 */
public abstract class LayerFromCollection1dFactory
{

    @FunctionalInterface
    public interface IJtoSingleIndex
    {

        int getIndex(int i, int j, int iSize, int jSize);

    }

    @FunctionalInterface
    public interface IJKtoSingleIndex
    {

        int getIndex(int i, int j, int k, int iSize, int jSize, int kSize);

    }

    public interface Reindexer extends IJtoSingleIndex, IJKtoSingleIndex
    {

        @Override
        default int getIndex(int i, int j, int k, int iSize, int jSize, int kSize)
        {
            return getIndex(i, j, iSize, jSize) * kSize + k;
        }

    }

    public static final Reindexer RowIColumnJ = new Reindexer() {

        @Override
        public int getIndex(int i, int j, int iSize, int jSize)
        {
            return i * jSize + j;
        }

    };

    public static final Reindexer ColumnIRowJ = new Reindexer() {

        @Override
        public int getIndex(int i, int j, int iSize, int jSize)
        {
            return j * iSize + i;
        }

    };

    public LayerFromCollection1dFactory()
    {
        super();
    }

}
