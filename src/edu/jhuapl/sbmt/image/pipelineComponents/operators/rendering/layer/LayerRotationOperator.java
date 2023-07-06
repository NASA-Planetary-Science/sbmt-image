package edu.jhuapl.sbmt.image.pipelineComponents.operators.rendering.layer;

import edu.jhuapl.sbmt.layer.api.Layer;
import edu.jhuapl.sbmt.layer.impl.LayerDoubleTransformFactory;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class LayerRotationOperator extends BasePipelineOperator<Layer, Layer>
{
    protected final LayerDoubleTransformFactory DoubleTransformFactory = new LayerDoubleTransformFactory();

    private final double rotation;

    public LayerRotationOperator(double rotation)
    {
        this.rotation = rotation;
    }

    @Override
    public void processData()
    {
        if (rotation == 0.0)
        {
            outputs.addAll(inputs);
        }
        else
        {
            // TODO: determine why the rotations seem to go in the opposite
            // sense. +90 should be CCW, +270 should be CW, but it is necessary
            // to reverse this.
            double rotation = 360.0 - DoubleTransformFactory.putInRange0to360(this.rotation);

            // Fill the expanded area of the rotated image with zeroes.
            // TODO: this should not be hard-wired like this; it should come out
            // of the imaging instrument/model metadata somehow.
            double expandValue = 0.0;

            for (Layer layer : inputs)
            {
                Layer rotatedLayer = DoubleTransformFactory.rotatePreservingSize(rotation, expandValue).apply(layer);
                outputs.add(rotatedLayer);
            }
        }
    }

}
