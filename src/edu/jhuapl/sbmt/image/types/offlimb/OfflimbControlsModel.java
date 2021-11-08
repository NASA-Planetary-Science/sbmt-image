package edu.jhuapl.sbmt.image.types.offlimb;

import java.util.Vector;

import edu.jhuapl.saavtk.model.Controller;
import edu.jhuapl.sbmt.image.core.listeners.OfflimbModelChangedListener;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImage;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

public class OfflimbControlsModel implements Controller.Model, MetadataManager
{
    private PerspectiveImage image;
    private int currentSlice;
    private int currentAlpha;
    private int currentDepth;
    private int contrastLow;
    private int contrastHigh;
    private boolean showBoundary = true; // true by default
	private boolean syncContrast = false; // false by default
    Vector<OfflimbModelChangedListener> listeners;

    final Key<Integer> currentSliceKey = Key.of("currentSlice");
    final Key<Integer> currentAlphaKey = Key.of("currentAlpha");
    final Key<Integer> currentDepthKey = Key.of("currentDepth");
    final Key<Integer> contrastLowKey = Key.of("contrastLow");
    final Key<Integer> contrastHighKey = Key.of("contrastHigh");

    public OfflimbControlsModel(PerspectiveImage image, int currentSlice)
    {
        this.image = image;
        this.currentSlice = currentSlice;
        this.listeners = new Vector<OfflimbModelChangedListener>();
    }

    public void addModelChangedListener(OfflimbModelChangedListener listener)
    {
        listeners.add(listener);
    }

    public PerspectiveImage getImage()
    {
        return image;
    }

    public int getCurrentSlice()
    {
        return currentSlice;
    }

    public int getCurrentAlpha()
    {
        return currentAlpha;
    }

    public void setCurrentAlpha(int currentAlpha)
    {
        this.currentAlpha = currentAlpha;
        for (OfflimbModelChangedListener listener : listeners)
        {
            listener.currentAlphaChanged(currentAlpha);
        }
    }

    public int getCurrentDepth()
    {
        return currentDepth;
    }

    public void setCurrentDepth(int currentDepth)
    {
        this.currentDepth = currentDepth;
        for (OfflimbModelChangedListener listener : listeners)
        {
            listener.currentDepthChanged(currentDepth);
        }
    }

    public int getContrastLow()
    {
        return contrastLow;
    }

    public void setContrastLow(int contrastLow)
    {
        this.contrastLow = contrastLow;
//        for (OfflimbModelChangedListener listener : listeners)
//        {
//            listener.currentContrastLowChanged(contrastLow);
//        }
    }

    public int getContrastHigh()
    {
        return contrastHigh;
    }


    public boolean getShowBoundary()
    {
        return showBoundary;
    }

	public boolean getSyncContrast() {
		return syncContrast;
	}

    public void setContrastHigh(int contrastHigh)
    {
        this.contrastHigh = contrastHigh;
//        for (OfflimbModelChangedListener listener : listeners)
//        {
//            listener.currentContrastHighChanged(contrastHigh);
//        }
    }

    public void setImage(PerspectiveImage image)
    {
        this.image = image;
    }

    public void setShowBoundary(boolean show)
    {
        this.showBoundary = show;
    }
    public void setSyncContrast(boolean sync){
        this.syncContrast = sync;
    }

    public void setCurrentSlice(int currentSlice)
    {
        this.currentSlice = currentSlice;
        for (OfflimbModelChangedListener listener : listeners)
        {
            listener.currentSliceChanged(currentSlice);
        }
    }

    @Override
    public Metadata store()
    {
        SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
        result.put(currentAlphaKey, currentAlpha);
        result.put(currentDepthKey, currentDepth);
        result.put(currentSliceKey, currentSlice);
        result.put(contrastLowKey, contrastLow);
        result.put(contrastHighKey, contrastHigh);


        return result;
    }

    @Override
    public void retrieve(Metadata source)
    {
        currentAlpha = source.get(currentAlphaKey);
        currentDepth = source.get(currentDepthKey);
        currentSlice = source.get(currentSliceKey);
        contrastLow = source.get(contrastLowKey);
        contrastHigh = source.get(contrastHighKey);
    }



}
