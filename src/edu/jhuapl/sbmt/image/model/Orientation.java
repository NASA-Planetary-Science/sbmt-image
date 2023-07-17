package edu.jhuapl.sbmt.image.model;

import java.util.Objects;

public class Orientation
{
    private final ImageFlip flip;
    private final double rotation;
    private final boolean transpose;

    protected Orientation(ImageFlip flip, double rotation, boolean transpose)
    {
        this.flip = flip;
        this.rotation = rotation;
        this.transpose = transpose;
    }

    public ImageFlip getFlip()
    {
        return flip;
    }

    public double getRotation()
    {
        return rotation;
    }

    public boolean isTranspose()
    {
        return transpose;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(flip, rotation, transpose);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof Orientation))
        {
            return false;
        }
        Orientation other = (Orientation) obj;
        return flip == other.flip && Double.doubleToLongBits(rotation) == Double.doubleToLongBits(other.rotation) && transpose == other.transpose;
    }

    @Override
    public String toString()
    {
        return "Orientation [flip=" + getFlip() + ", rotation=" + getRotation() + ", transpose=" + isTranspose() + "]";
    }

}
