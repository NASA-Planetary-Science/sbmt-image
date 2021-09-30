package edu.jhuapl.sbmt.image.impl;

import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelDouble;
import edu.jhuapl.sbmt.image.api.PixelVectorDouble;

/**
 * Abstract base implementation of {@link Layer} that assumes each pixel
 * contains one scalar double value. Supported {@link Pixel} sub-types are
 * {@link PixelDouble} and {@link PixelVectorDouble}.
 *
 * @author James Peachey
 *
 */
public abstract class BasicLayerOfDouble extends BasicLayer
{
    private static final Set<Class<?>> AcceptedPixelTypes = ImmutableSet.of(PixelDouble.class, PixelVectorDouble.class);

    /**
     * Constructor that creates a layer using the specified dimensions. The
     * caller must ensure iSize and jSize are non-negative.
     *
     * @param iSize the number of in-bounds values of the I index
     * @param jSize the number of in-bounds values of the J index
     */
    protected BasicLayerOfDouble(int iSize, int jSize)
    {
        super(iSize, jSize);
    }

    @Override
    public List<Integer> dataSizes()
    {
        return ImmutableList.of();
    }

    /**
     * Returns a set containing supported pixel types
     * {@link PixelDouble}<code>.class</code> and
     * {@link PixelVectorDouble}<code>.class</code>.
     */
    @Override
    public Set<Class<?>> getPixelTypes()
    {
        return AcceptedPixelTypes;
    }

    /**
     *
     */
    @Override
    public void get(int i, int j, Pixel p)
    {
        Preconditions.checkNotNull(p);

        p.setIsValid(isValid(i, j));

        if (checkIndices(i, j))
        {
            p.setInBounds(true);

            if (p instanceof PixelDouble)
            {
                ((PixelDouble) p).set(doGetDouble(i, j));
            }
            else if (p instanceof PixelVectorDouble)
            {
                PixelVectorDouble vdp = (PixelVectorDouble) p;

                double value = doGetDouble(i, j);
                double outOfBoundsValue = vdp.getOutOfBoundsValue();

                for (int k = 0; k < vdp.size(); ++k)
                {
                    vdp.set(k, value);
                    value = outOfBoundsValue;
                }
            }
            else
            {
                throw new UnsupportedOperationException("Do not know how to set values in a pixel of type " + p.getClass());
            }
        }
        else
        {
            p.setInBounds(false);
        }

    }

    protected abstract double doGetDouble(int i, int j);

    public String toString()
    {
        return "Layer of double (" + iSize() + ", " + jSize() + ")";
    }

}
