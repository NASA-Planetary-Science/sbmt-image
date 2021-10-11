package edu.jhuapl.sbmt.image.impl;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.image.api.Pixel;
import edu.jhuapl.sbmt.image.api.PixelDouble;
import edu.jhuapl.sbmt.image.api.PixelVectorDouble;

/**
 * Implementation of {@link PixelVectorDouble} that inherits its general
 * {@link Pixel} functionality from {@link BasicPixel}.
 *
 * @author James Peachey
 *
 */
public abstract class BasicPixelVectorDouble extends BasicPixel implements PixelVectorDouble
{

    private final ImmutableList<ScalarPixel> pixels;

    protected BasicPixelVectorDouble(int size, boolean isValid, boolean inBounds)
    {
        super(isValid, inBounds);

        ImmutableList.Builder<ScalarPixel> builder = ImmutableList.builder();
        for (int index = 0; index < size; ++index)
        {
            builder.add(new ScalarPixel(0.));
        }

        this.pixels = builder.build();
    }

    protected BasicPixelVectorDouble(BasicPixelVectorDouble source) {
        super(source.isValid(), source.isInBounds());

        ImmutableList.Builder<ScalarPixel> builder = ImmutableList.builder();
        for (int index = 0; index < source.size(); ++index)
        {
            builder.add(new ScalarPixel(source.get(index)));
        }

        this.pixels = builder.build();
    }
    @Override
    public int size()
    {
        return pixels.size();
    }

    @Override
    public ScalarPixel get(int index)
    {
        if (!checkIndex(index, 0, size()) ) {
            throw new IndexOutOfBoundsException();
        }

        return pixels.get(index);
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
        StringBuilder builder = new StringBuilder("(");
        String delim = "";
        for (int k = 0; k < size(); ++k)
        {
            builder.append(delim);
            builder.append(get(k));

            delim = ", ";
        }
        builder.append(")");

        return builder.toString();
    }

    public class ScalarPixel implements PixelDouble
    {

        private volatile double value;

        protected ScalarPixel(double value)
        {
            this.value = value;
        }

        protected ScalarPixel(ScalarPixel source) {
            this.value = source.get();
        }

        @Override
        public boolean isValid()
        {
            return BasicPixelVectorDouble.this.isValid();
        }

        @Override
        public void setIsValid(boolean isValid) {
            BasicPixelVectorDouble.this.setIsValid(isValid);
        }

        @Override
        public boolean isInBounds()
        {
            return BasicPixelVectorDouble.this.isInBounds();
        }

        @Override
        public void setInBounds(boolean inBounds)
        {
            BasicPixelVectorDouble.this.setInBounds(inBounds);
        }

        @Override
        public double get()
        {
            if (!isInBounds())
            {
                return getOutOfBoundsValue();
            }

            return getStoredValue();
        }

        @Override
        public double getStoredValue()
        {
            return value;
        }

        @Override
        public void set(double value)
        {
            this.value = value;
        }

        @Override
        public double getOutOfBoundsValue()
        {
            return BasicPixelVectorDouble.this.getOutOfBoundsValue();
        }

        @Override
        public String toString()
        {
            String formattedValue = String.format("%.3g", get());
            String stringFormat = "%9s";
            if (!isInBounds())
            {
                formattedValue = String.format(stringFormat, "(O) " + formattedValue);
            }
            else if (!isValid())
            {
                formattedValue = String.format(stringFormat, "(I) " + formattedValue);
            }
            else
            {
                formattedValue = String.format(stringFormat, formattedValue);
            }

            return formattedValue;
        }

    }

}
