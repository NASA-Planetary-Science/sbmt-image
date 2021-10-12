package edu.jhuapl.sbmt.image.impl;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.image.api.Layer;

/**
 * Low-level but general factory for creating {@link Layer} instances with
 * vectors or scalars whose data are of type double.
 *
 * @author James Peachey
 *
 */
public class LayerDoubleFactory
{

    @FunctionalInterface
    public interface DoubleGetter1d
    {

        double get(int i);

    }

    @FunctionalInterface
    public interface DoubleGetter2d
    {

        double get(int i, int j);

    }

    @FunctionalInterface
    public interface DoubleGetter3d
    {

        double get(int i, int j, int k);

    }

    public LayerDoubleFactory()
    {
        super();
    }

    public Layer ofScalar(DoubleGetter2d doubleGetter, int iSize, int jSize)
    {
        Preconditions.checkNotNull(doubleGetter);
        Preconditions.checkArgument(iSize >= 0);
        Preconditions.checkArgument(jSize >= 0);

        return new BasicLayerOfDouble(iSize, jSize) {

            @Override
            protected double doGetDouble(int i, int j)
            {
                return doubleGetter.get(i, j);
            }

        };
    }

    public Layer ofScalar(DoubleGetter2d doubleGetter, int iSize, int jSize, LayerValidityChecker checker)
    {
        Preconditions.checkNotNull(doubleGetter);
        Preconditions.checkNotNull(checker);
        Preconditions.checkArgument(iSize >= 0);
        Preconditions.checkArgument(jSize >= 0);

        return new BasicLayerOfDouble(iSize, jSize) {

            @Override
            protected double doGetDouble(int i, int j)
            {
                return doubleGetter.get(i, j);
            }

            @Override
            public boolean isValid(int i, int j)
            {
                return checker.test(this, i, j);
            }

        };
    }

    public Layer ofVector(DoubleGetter3d doubleGetter, int iSize, int jSize, int kSize)
    {
        Preconditions.checkNotNull(doubleGetter);
        Preconditions.checkArgument(iSize >= 0);
        Preconditions.checkArgument(jSize >= 0);
        Preconditions.checkArgument(kSize >= 0);

        List<Integer> dataSizes = ImmutableList.of(Integer.valueOf(kSize));

        return new BasicLayerOfVectorDouble(iSize, jSize) {

            @Override
            public List<Integer> dataSizes()
            {
                return dataSizes;
            }

            @Override
            protected double doGetDouble(int i, int j, int k)
            {
                return doubleGetter.get(i, j, k);
            }

        };
    }

    public Layer ofVector(DoubleGetter3d doubleGetter, int iSize, int jSize, int kSize, LayerValidityChecker checker)
    {
        Preconditions.checkNotNull(doubleGetter);
        Preconditions.checkNotNull(checker);
        Preconditions.checkArgument(iSize >= 0);
        Preconditions.checkArgument(jSize >= 0);
        Preconditions.checkArgument(kSize >= 0);

        List<Integer> dataSizes = ImmutableList.of(Integer.valueOf(kSize));

        return new BasicLayerOfVectorDouble(iSize, jSize) {

            @Override
            public List<Integer> dataSizes()
            {
                return dataSizes;
            }

            @Override
            protected double doGetDouble(int i, int j, int k)
            {
                return doubleGetter.get(i, j, k);
            }

            @Override
            public boolean isValid(int i, int j)
            {
                return checker.test(this, i, j);
            }

        };
    }

}
