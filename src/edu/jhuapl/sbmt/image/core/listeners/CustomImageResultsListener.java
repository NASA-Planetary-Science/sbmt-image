package edu.jhuapl.sbmt.image.core.listeners;

import java.util.List;

import edu.jhuapl.sbmt.image.types.customImage.CustomImageKeyInterface;

public interface CustomImageResultsListener
{
    public void resultsChanged(List<CustomImageKeyInterface> results);
}
