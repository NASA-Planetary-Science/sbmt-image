package edu.jhuapl.sbmt.image.impl;

import java.util.List;
import java.util.Set;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.Pixel;

public abstract class ResampledLayer extends BasicLayer
{
    protected ResampledLayer(int iSize, int jSize)
    {
        super(iSize, jSize);
    }

    protected abstract Layer getInputLayer();

    @Override
    public List<Integer> dataSizes()
    {
        return getInputLayer().dataSizes();
    }

    @Override
    public Set<Class<?>> getPixelTypes()
    {
        return getInputLayer().getPixelTypes();
    }

    @Override
    protected abstract void getElement(int i, int j, int k, Pixel p);

    @Override
    public String toString()
    {
        return super.toString() + " resampled from " + getInputLayer().toString();
    }

}
