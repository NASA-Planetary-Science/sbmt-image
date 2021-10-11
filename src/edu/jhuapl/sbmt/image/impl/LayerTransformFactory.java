package edu.jhuapl.sbmt.image.impl;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.google.common.base.Preconditions;

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
        if (layer == null)
        {
            return null;
        }

        return new ForwardingLayer(layer) {

            @Override
            public void get(int i, int j, Pixel p)
            {
                super.get(iSize() - 1 - i, j, p);
            }

        };
    };

    /**
     * Invert indices in the Jth dimension.
     */
    private static final Function<Layer, Layer> InvertJ = layer -> {
        if (layer == null)
        {
            return null;
        }

        return new ForwardingLayer(layer) {

            @Override
            public void get(int i, int j, Pixel p)
            {
                super.get(i, jSize() - 1 - j, p);
            }

        };
    };

    /**
     * Invert indices in the both I and J dimensions.
     */
    private static final Function<Layer, Layer> InvertIJ = layer -> {
        if (layer == null)
        {
            return null;
        }

        return new ForwardingLayer(layer) {

            @Override
            public void get(int i, int j, Pixel p)
            {
                super.get(iSize() - 1 - i, jSize() - 1 - j, p);
            }

        };
    };

    /**
     * Swap I with J. This is like rotation about the diagonal elements, or
     * doing a flip and a rotation together.
     */
    private static final Function<Layer, Layer> SwapIJ = layer -> {
        if (layer == null)
        {
            return null;
        }

        return new ForwardingLayer(layer) {

            @Override
            public int iSize()
            {
                return super.jSize();
            }

            @Override
            public int jSize()
            {
                return super.iSize();
            }

            @Override
            public void get(int i, int j, Pixel p)
            {
                super.get(j, i, p);
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
     * @return the function
     */
    public Function<Layer, Layer> swapIJ()
    {
        return SwapIJ;
    }

    /**
     * Return a function that extracts a subset of the image in the I-th
     * dimension whose range is specified by the arguments. The I index in the
     * layer that is returned will run from 0 to an iSize() equal to iMax -
     * iMin. For an I index of 0 in the new layer, the same pixel will be
     * returned as at the I index iMin in the original layer, and when the I
     * index is (iSize() - 1) == (iMax - iMin - 1) in the new layer, the same
     * pixel will be returned as at the I index (iMax - 1) in the original
     * layer.
     *
     * @param iMin the minimum index in the original Layer's I-th dimension
     *            space
     * @param iMax the maximum index (one past last kept) in the original
     *            Layer's I-th dimension space
     * @return the function
     * @throws IllegalArgumentException if iMin or iMax is negative, or iMax <
     *             iMin
     */
    public Function<Layer, Layer> subsetI(int iMin, int iMax)
    {
        Preconditions.checkArgument(iMin >= 0);
        Preconditions.checkArgument(iMax >= iMin);

        int iNewSize = iMax - iMin;

        return layer -> {
            return subsetLayer(layer, iMin, iNewSize, 0, layer.jSize());
        };
    }

    /**
     * Return a function that extracts a subset of the image in the J-th
     * dimension whose range is specified by the arguments. The J index in the
     * layer that is returned will run from 0 to a jSize() equal to jMax - jMin.
     * For a J index of 0 in the new layer, the same pixel will be returned as
     * at the J index jMin in the original layer, and when the J index is
     * (jSize() - 1) == (jMax - jMin - 1) in the new layer, the same pixel will
     * be returned as at the J index (jMax - 1) in the original layer.
     *
     * @param jMin the minimum index in the original Layer's J-th dimension
     *            space
     * @param jMax the maximum index (one past last kept) in the original
     *            Layer's J-th dimension space
     * @return the function
     * @throws IllegalArgumentException if jMin or jMax is negative, or jMax <
     *             jMin
     */
    public Function<Layer, Layer> subsetJ(int jMin, int jMax)
    {
        Preconditions.checkArgument(jMin >= 0);
        Preconditions.checkArgument(jMax >= jMin);

        int jNewSize = jMax - jMin;

        return layer -> {
            return subsetLayer(layer, 0, layer.iSize(), jMin, jNewSize);
        };
    }

    /**
     * Return a function that extracts a subset of the image both I and J
     * spaces. Behaves like a composition of the functions returned by
     * {@link #subsetI(int, int)} and {@link #subsetJ(int, int)}.
     *
     * @param iMin the minimum index in the original Layer's I-th dimension
     *            space
     * @param iMax the maximum index (one past last kept) in the original
     *            Layer's I-th dimension space
     * @param jMin the minimum index in the original Layer's J-th dimension
     *            space
     * @param jMax the maximum index (one past last kept) in the original
     *            Layer's J-th dimension space
     * @return the function
     * @throws IllegalArgumentException if iMin or iMax is negative, iMax <
     *             iMin, jMin or jMax is negative, or jMax < jMin
     */
    public Function<Layer, Layer> subset(int iMin, int iMax, int jMin, int jMax)
    {
        Preconditions.checkArgument(iMin >= 0);
        Preconditions.checkArgument(iMax >= iMin);
        Preconditions.checkArgument(jMin >= 0);
        Preconditions.checkArgument(jMax >= jMin);

        int iNewSize = iMax - iMin;
        int jNewSize = jMax - jMin;

        return layer -> {
            return subsetLayer(layer, iMin, iNewSize, jMin, jNewSize);
        };
    }

    /**
     * Return a function that trims pixels off either/both ends of a layer in
     * the I-th dimension. The layer returned by the function will have new
     * iSize() == (original iSize() - iLowerOffset - iUpperOffset). When the I
     * index is 0 in the new layer, the pixel located with I = iLowerOffset in
     * the original image will be accessed. When the I index is (iSize() - 1) in
     * the new layer, the pixel located at (iSize() - iUpperOffset - 1) in the
     * original layer will be accessed.
     *
     * @param iLowerOffset number of pixels to offset at the lower end of the I
     *            index
     * @param iUpperOffset number of pixels to offset at the upper end of the I
     *            index
     * @return the function
     * @throws IllegalArgumentException if either argument is negative
     */
    public Function<Layer, Layer> trimI(int iLowerOffset, int iUpperOffset)
    {
        Preconditions.checkArgument(iLowerOffset >= 0);
        Preconditions.checkArgument(iUpperOffset >= 0);

        return layer -> {
            if (layer == null)
            {
                return null;
            }

            int iNewSize = layer.iSize() - iLowerOffset - iUpperOffset;

            return subsetLayer(layer, iLowerOffset, iNewSize, 0, layer.jSize());
        };
    }

    /**
     * Return a function that trims pixels off either/both ends of a layer in
     * the J-th dimension. The layer returned by the function will have new
     * jSize() == (original jSize() - jLowerOffset - jUpperOffset). When the J
     * index is 0 in the new layer, the pixel located with J = jLowerOffset in
     * the original image will be accessed. When the J index is (jSize() - 1) in
     * the new layer, the pixel located at (jSize() - jUpperOffset - 1) in the
     * original layer will be accessed.
     *
     * @param jLowerOffset number of pixels to offset at the lower end of the J
     *            index
     * @param jUpperOffset number of pixels to offset at the upper end of the J
     *            index
     * @return the function
     * @throws IllegalArgumentException if either argument is negative
     */
    public Function<Layer, Layer> trimJ(int jLowerOffset, int jUpperOffset)
    {
        Preconditions.checkArgument(jLowerOffset >= 0);
        Preconditions.checkArgument(jUpperOffset >= 0);

        return layer -> {
            if (layer == null)
            {
                return null;
            }

            int jNewSize = layer.jSize() - jLowerOffset - jUpperOffset;

            return subsetLayer(layer, 0, layer.iSize(), jLowerOffset, jNewSize);
        };
    }

    /**
     * Return a function that trims pixels off either/both ends of a layer in
     * both I-th and J-th dimensions. Behaves like a composition of functions
     * returned by {@link #trimI(int, int)} and {@link #trimJ(int, int)}.
     * <p>
     * This differs from {@link #mask(int, int, int, int)} in that this function
     * in effect removes the pixels from the edges, so that the actual size of
     * the returned layer is smaller.
     *
     * @param iLowerOffset number of pixels to offset at the lower end of the I
     *            index
     * @param iUpperOffset number of pixels to offset at the upper end of the I
     *            index
     * @param jLowerOffset number of pixels to offset at the lower end of the J
     *            index
     * @param jUpperOffset number of pixels to offset at the upper end of the J
     *            index
     * @return the function
     * @throws IllegalArgumentException if any argument is negative
     */
    public Function<Layer, Layer> trim(int iLowerOffset, int iUpperOffset, int jLowerOffset, int jUpperOffset)
    {
        Preconditions.checkArgument(iLowerOffset >= 0);
        Preconditions.checkArgument(iUpperOffset >= 0);
        Preconditions.checkArgument(jLowerOffset >= 0);
        Preconditions.checkArgument(jUpperOffset >= 0);

        return layer -> {
            if (layer == null)
            {
                return null;
            }

            int iNewSize = layer.iSize() - iLowerOffset - iUpperOffset;
            int jNewSize = layer.jSize() - jLowerOffset - jUpperOffset;

            return subsetLayer(layer, iLowerOffset, iNewSize, jLowerOffset, jNewSize);
        };
    }

    /**
     * Return a function that masks pixels off the edges of a layer.
     * <p>
     * This differs from {@link #trim(int, int, int, int)} in that masking
     * produces a layer of exactly the same size as the input layer, but with
     * the area that is masked off being treated as out-of-bounds.
     *
     * @param iLowerOffset number of pixels to mask at the lower end of the I
     *            index
     * @param iUpperOffset number of pixels to mask at the upper end of the I
     *            index
     * @param jLowerOffset number of pixels to mask at the lower end of the J
     *            index
     * @param jUpperOffset number of pixels to mask at the upper end of the J
     *            index
     * @return the function
     * @throws IllegalArgumentException if any argument is negative
     */
    public Function<Layer, Layer> mask(int iLowerOffset, int iUpperOffset, int jLowerOffset, int jUpperOffset)
    {
        Preconditions.checkArgument(iLowerOffset >= 0);
        Preconditions.checkArgument(iUpperOffset >= 0);
        Preconditions.checkArgument(jLowerOffset >= 0);
        Preconditions.checkArgument(jUpperOffset >= 0);

        return layer -> {
            if (layer == null)
            {
                return null;
            }

            return new ForwardingLayer(layer) {

                @Override
                public boolean isValid(int i, int j)
                {
                    if (!isWithinFrame(i, j))
                    {
                        return false;
                    }

                    return super.isValid(i, j);
                }

                @Override
                public boolean isInBounds(int i, int j)
                {
                    if (!isWithinFrame(i, j))
                    {
                        return false;
                    }

                    return super.isInBounds(i, j);
                }

                @Override
                public void get(int i, int j, Pixel p)
                {
                    if (!isWithinFrame(i, j))
                    {
                        p.setInBounds(false);
                    }
                    else
                    {
                        super.get(i, j, p);
                    }
                }

                /**
                 * Utility method that determines whether a location is within
                 * the frame formed by the mask pixels around the edge of the
                 * image.
                 *
                 * @param i the I index to check
                 * @param j the J index to check
                 * @return true if the location is in the central portion of the
                 *         image, i.e., NOT masked off
                 */
                protected boolean isWithinFrame(int i, int j)
                {
                    return i >= iLowerOffset && //
                            i < iSize() - iUpperOffset && //
                            j >= jLowerOffset && //
                            j < jSize() - jUpperOffset;
                }

            };
        };
    }

    /**
     * General utility method for extracting a subset from a layer in both I and
     * J dimensions. For each index (I and J), the sum of the minimum index plus
     * the new size must be <= the original target layer's size for the same
     * index.
     * <p>
     * This method just removes pixels from the layer, as in a crop/subset/trim
     * operation; it does not change (interpolate, expand, etc.) any of the
     * remaining pixels.
     *
     * @param layer input target layer to be resized
     * @param iMin the minimum index in the original layer's I-th dimension
     *            space
     * @param iNewSize the size of the new layer in the output layer's I-th
     *            dimension space
     * @param iMin the minimum index in the original layer's J-th dimension
     *            space
     * @param iNewSize the size of the new layer in the output layer's J-th
     *            dimension space
     * @return the resized output layer
     * @throws IllegalArgumentException if any argument is negative, or if (iMin
     *             + iNewSize) > original layer's iSize() or if (jMin +
     *             jNewSize) > original layer's jSize()
     */
    protected ForwardingLayer subsetLayer(Layer layer, int iMin, int iNewSize, int jMin, int jNewSize)
    {
        if (layer == null)
        {
            return null;
        }

        int iOrigSize = layer.iSize();
        int jOrigSize = layer.jSize();

        Preconditions.checkArgument(iMin >= 0);
        Preconditions.checkArgument(iNewSize >= 0);
        Preconditions.checkArgument((iMin + iNewSize) <= iOrigSize);

        Preconditions.checkArgument(jMin >= 0);
        Preconditions.checkArgument(jNewSize >= 0);
        Preconditions.checkArgument((jMin + jNewSize) <= jOrigSize);

        return new ForwardingLayer(layer) {

            @Override
            public int iSize()
            {
                return iNewSize;
            }

            @Override
            public int jSize()
            {
                return jNewSize;
            }

            @Override
            public boolean isValid(int i, int j)
            {
                i = toOutputIndex(i, iMin, iOrigSize, iNewSize);
                j = toOutputIndex(j, jMin, jOrigSize, jNewSize);

                return super.isValid(i, j);
            }

            @Override
            public boolean isInBounds(int i, int j)
            {
                i = toOutputIndex(i, iMin, iOrigSize, iNewSize);
                j = toOutputIndex(j, jMin, jOrigSize, jNewSize);

                return super.isInBounds(i, j);
            }

            @Override
            public void get(int i, int j, Pixel p)
            {
                i = toOutputIndex(i, iMin, iOrigSize, iNewSize);
                j = toOutputIndex(j, jMin, jOrigSize, jNewSize);

                super.get(i, j, p);
            }

            /**
             * Utility to convert the specified index in the output layer's
             * index "coordinate system" back to the input target layer's index
             * "coordinate system". The index returned by this method is safe to
             * pass to any of the forwarded/target methods that require this
             * index.
             *
             * @param index in the new output layer's index coordinate
             * @param offset to be applied to the index argument to match the
             *            original input layer's index coordinate
             * @param origSize size of the original input target layer
             * @param newSize size of the output layer
             * @return index in the target layer's index coordinate
             */
            protected int toOutputIndex(int index, int offset, int origSize, int newSize)
            {
                if (index < 0)
                {
                    // Below minimum in output = below minimum in input.
                    return -1;
                }
                else if (index >= newSize)
                {
                    // Above maximum in output = above maximum in input.
                    return origSize;
                }

                return index += offset;
            }

        };

    }

    /**
     * Implementation of {@link Layer} that forwards all its methods to another
     * instance of {@link Layer}. Use this as the base class to override only
     * some behaviors defined in another implementation.
     */
    public static class ForwardingLayer implements Layer
    {

        private final Layer target;

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
            return "Layer(" + iSize() + ", " + jSize() + ")";
        }

    }

}
