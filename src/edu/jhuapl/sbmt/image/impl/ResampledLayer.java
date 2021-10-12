package edu.jhuapl.sbmt.image.impl;

import java.util.List;
import java.util.Set;

import edu.jhuapl.sbmt.image.api.Layer;
import edu.jhuapl.sbmt.image.api.PixelDouble;
import edu.jhuapl.sbmt.image.api.PixelVectorDouble;

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
    protected abstract void get(int i, int j, PixelDouble pd);

    @Override
    protected void get(int i, int j, PixelVectorDouble pvd)
    {
        for (int index = 0; index < pvd.size(); ++index) {
            get(i, j, pvd.get(index));
        }
    }

    @Override
    public String toString()
    {
        return super.toString() + " resampled from " + getInputLayer().toString();
    }

}
