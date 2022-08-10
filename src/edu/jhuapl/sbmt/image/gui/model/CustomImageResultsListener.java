package edu.jhuapl.sbmt.image.gui.model;

import java.util.List;

import edu.jhuapl.sbmt.core.image.CustomImageKeyInterface;

public interface CustomImageResultsListener
{
    public void resultsChanged(List<CustomImageKeyInterface> results);
}
