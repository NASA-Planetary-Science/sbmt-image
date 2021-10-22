package edu.jhuapl.sbmt.image.impl;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.impl.LayerDoubleFactory.DoubleGetter1d;
import edu.jhuapl.sbmt.image.impl.LayerDoubleFactory.DoubleGetter2d;
import edu.jhuapl.sbmt.image.impl.LayerDoubleFactory.DoubleGetter3d;

/**
 * Factory class for creating {@link Layer} instances starting from a
 * singly-indexed data collection of doubles, such as an array or list. This
 * uses utility interfaces to convert two indices (I, J) into a single index
 * that accurately retrieves data from the underlying singly-indexed data
 * structure.
 * <p>
 * This class is double-specific for the type of data returned, but does not
 * presuppose any particular type of underlying single-indexed container.
 *
 * @author James Peachey
 *
 */
public class LayerFromDoubleCollection1dFactory extends LayerFromCollection1dFactory
{

    private static final LayerDoubleFactory DefaultInstance = new LayerDoubleFactory();

    public LayerFromDoubleCollection1dFactory()
    {
        super();
    }

    public Layer ofScalar(DoubleGetter1d doubleGetter, IJtoSingleIndex reIndexer, int iSize, int jSize)
    {
        DoubleGetter2d doubleGetter2d = (i, j) -> {
            return doubleGetter.get(reIndexer.getIndex(i, j, iSize, jSize));
        };

        return getLayerFactory().ofScalar(doubleGetter2d, iSize, jSize);
    }

    public Layer ofScalar(DoubleGetter1d doubleGetter, IJtoSingleIndex reIndexer, int iSize, int jSize, ValidityCheckerDoubleFactory.ScalarValidityChecker checker)
    {
        DoubleGetter2d doubleGetter2d = (i, j) -> {
            return doubleGetter.get(reIndexer.getIndex(i, j, iSize, jSize));
        };

        return getLayerFactory().ofScalar(doubleGetter2d, iSize, jSize, checker);
    }

    public Layer ofVector(DoubleGetter1d doubleGetter, IJKtoSingleIndex reIndexer, int iSize, int jSize, int kSize)
    {
        DoubleGetter3d doubleGetter3d = (i, j, k) -> {
            return doubleGetter.get(reIndexer.getIndex(i, j, k, iSize, jSize, kSize));
        };

        return getLayerFactory().ofVector(doubleGetter3d, iSize, jSize, kSize);
    }

    public Layer ofVector(DoubleGetter1d doubleGetter, IJKtoSingleIndex reIndexer, int iSize, int jSize, int kSize, ValidityCheckerDoubleFactory.ScalarValidityChecker checker)
    {
        DoubleGetter3d doubleGetter3d = (i, j, k) -> {
            return doubleGetter.get(reIndexer.getIndex(i, j, k, iSize, jSize, kSize));
        };

        return getLayerFactory().ofVector(doubleGetter3d, iSize, jSize, kSize, checker);
    }

    protected LayerDoubleFactory getLayerFactory()
    {
        return DefaultInstance;
    }

}
