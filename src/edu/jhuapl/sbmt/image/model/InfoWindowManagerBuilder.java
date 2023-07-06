package edu.jhuapl.sbmt.image.model;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;

@FunctionalInterface
public interface InfoWindowManagerBuilder<Model> 
{
	ModelInfoWindow buildModelInfoWindow(Model model);
}
