package edu.jhuapl.sbmt.image.core.listeners;

public interface OfflimbModelChangedListener
{
    public void currentSliceChanged(int slice);
    public void currentDepthChanged(int depth);
    public void currentAlphaChanged(int alpha);
//    public void currentContrastLowChanged(int contrastMin);
//    public void currentContrastHighChanged(int contrastMax);
    public void showBoundaryChanged();
	public void syncContrastChanged();
}
