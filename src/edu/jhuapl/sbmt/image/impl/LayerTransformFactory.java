package edu.jhuapl.sbmt.image.impl;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.Pixel;

/**
 * Factory class for creating {@link Layer} transforms (that is,
 * {@link Function} instances that operate on a layer and return a new layer).
 * <p>
 * This factory provides transforms that involve only changes to indices, no
 * operations that are specific to the underlying data type associated with each
 * pixel.
 *
 * @author James Peachey
 *
 */
public class LayerTransformFactory
{

    /**
     * Invert indices in the Ith dimension.
     */
    private static final Function<Layer, Layer> InvertI = layer -> {

        return new ForwardingLayer(layer) {

            @Override
            public void get(int i, int j, Pixel p)
            {
                target.get(iSize() - 1 - i, j, p);
            }

        };
    };

    /**
     * Invert indices in the Jth dimension.
     */
    private static final Function<Layer, Layer> InvertJ = layer -> {

        return new ForwardingLayer(layer) {

            @Override
            public void get(int i, int j, Pixel p)
            {
                target.get(i, jSize() - 1 - j, p);
            }

        };
    };

    /**
     * Invert indices in the both I and J dimensions.
     */
    private static final Function<Layer, Layer> InvertIJ = layer -> {

        return new ForwardingLayer(layer) {

            @Override
            public void get(int i, int j, Pixel p)
            {
                target.get(iSize() - 1 - i, jSize() - 1 - j, p);
            }

        };
    };

    /**
     * Swap I with J. This is like rotation about the diagonal elements, or
     * doing a flip and a rotation together.
     */
    private static final Function<Layer, Layer> SwapIJ = layer -> {

        return new ForwardingLayer(layer) {

            @Override
            public int iSize()
            {
                return target.jSize();
            }

            @Override
            public int jSize()
            {
                return target.iSize();
            }

            @Override
            public void get(int i, int j, Pixel p)
            {
                target.get(j, i, p);
            }

        };
    };

    /**
     * Rotate clockwise.
     */
    private static final Function<Layer, Layer> RotateClockwise = InvertI.compose(SwapIJ);

    /**
     * Rotate counterclockwise.
     */
    private static final Function<Layer, Layer> RotateCounterClockwise = InvertJ.compose(SwapIJ);

    public LayerTransformFactory()
    {
        super();
    }

    /**
     * Return a function that flips a layer about its X axis.
     *
     * @return the function
     */
    public Function<Layer, Layer> flipAboutX()
    {
        return invertJ();
    }

    /**
     * Return a function that flips a layer about its Y axis. This is the same
     * function returned by {@link #invertI()}.
     *
     * @return the function
     */
    public Function<Layer, Layer> flipAboutY()
    {
        return invertI();
    }

    /**
     * Return a function that flips a layer about both its X and Y axes. This is
     * equivalent to rotating the layer by pi radians, and is the same function
     * returned by {@link #rotateHalfway()} and {@link #invertIJ()}.
     *
     * @return the function
     */
    public Function<Layer, Layer> flipAboutXY()
    {
        return invertIJ();
    }

    /**
     * Return a function that rotates a layer pi/2 radians clockwise.
     *
     * @return the function
     */
    public Function<Layer, Layer> rotateCW()
    {
        return RotateClockwise;
    }

    /**
     * Return a function that rotates a layer pi/2 radians counterclockwise.
     *
     * @return the function
     */
    public Function<Layer, Layer> rotateCCW()
    {
        return RotateCounterClockwise;
    }

    /**
     * Return a function that rotates a layer pi radians. This is the same
     * function returned by {@link #invertIJ()}.
     *
     * @return the function
     */
    public Function<Layer, Layer> rotateHalfway()
    {
        return invertIJ();
    }

    /**
     * Return a function that reverses the order of the I index so 0 becomes
     * iSize() - 1 and iSize() - 1 becomes 0. This is the same function returned
     * by {@link #flipAboutY()}.
     *
     * @return the function
     */
    public Function<Layer, Layer> invertI()
    {
        return InvertI;
    }

    /**
     * Return a function that reverses the order of the J index so 0 becomes
     * jSize() - 1 and jSize() - 1 becomes 0. This is the same function returned
     * by {@link #flipAboutX()}.
     *
     * @return the function
     */
    public Function<Layer, Layer> invertJ()
    {
        return InvertJ;
    }

    /**
     * Return a function that reverses the order of the both I and J indices so
     * 0 becomes N - 1 and N - 1 becomes 0, where N is iSize() or jSize(),
     * respectively. This is the same function returned by
     * {@link #flipAboutXY()} and {@link #rotateHalfway()}.
     *
     * @return the function
     */
    public Function<Layer, Layer> invertIJ()
    {
        return InvertIJ;
    }

    /**
     * Return a function that swaps the I and J indices of a layer. This is
     * equivalent to performing both a flip and a rotation, and is also
     * equivalent to rotation about the NW - SE diagonal.
     *
     * @return
     */
    public Function<Layer, Layer> swapIJ()
    {
        return SwapIJ;
    }

    /**
     * Implementation of {@link Layer} that forwards all its methods to another
     * instance of {@link Layer}. Use this as the base class to override only
     * some behaviors defined in another implementation.
     */
    public static class ForwardingLayer implements Layer
    {

        protected final Layer target;

        /**
         * The concrete implementation is responsible for ensuring the target is
         * non-null.
         *
         * @param target the layer to which to forward operations by default.
         */
        protected ForwardingLayer(Layer target)
        {
            super();
            this.target = target;
        }

        @Override
        public int iSize()
        {
            return target.iSize();
        }

        @Override
        public int jSize()
        {
            return target.jSize();
        }

        @Override
        public List<Integer> dataSizes()
        {
            return target.dataSizes();
        }

        @Override
        public boolean isValid(int i, int j)
        {
            return target.isValid(i, j);
        }

        @Override
        public boolean isInBounds(int i, int j)
        {
            return target.isInBounds(i, j);
        }

        @Override
        public Set<Class<?>> getPixelTypes()
        {
            return target.getPixelTypes();
        }

        @Override
        public boolean isGetAccepts(Class<?> pixelType)
        {
            return target.isGetAccepts(pixelType);
        }

        @Override
        public void get(int i, int j, Pixel p)
        {
            target.get(i, j, p);
        }

        @Override
        public String toString()
        {
            return target.toString();
        }

    }

}
