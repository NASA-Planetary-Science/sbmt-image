package edu.jhuapl.sbmt.image.model;

import java.awt.Color;

import edu.jhuapl.saavtk.util.IntensityRange;

public class PerspectiveImageRenderingState<G1>
{
	public boolean isMapped = false;
	public boolean isFrustumShowing = false;
	public boolean isBoundaryShowing = false;
	public boolean isOfflimbShowing = false;
	public boolean isOffLimbBoundaryShowing = false;
	public Color boundaryColor;
	public Color offLimbBoundaryColor = Color.red;
	public Color frustumColor = Color.green;
	public double offLimbFootprintDepth;
	public boolean contrastSynced = false;
	public IntensityRange imageContrastRange;
	public IntensityRange offLimbContrastRange;
}