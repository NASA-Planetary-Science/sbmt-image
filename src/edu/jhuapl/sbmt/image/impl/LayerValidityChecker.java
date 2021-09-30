package edu.jhuapl.sbmt.image.impl;

import edu.jhuapl.sbmt.image.api.Layer;

@FunctionalInterface
public interface LayerValidityChecker
{
    boolean test(Layer layer, int i, int j);
}
