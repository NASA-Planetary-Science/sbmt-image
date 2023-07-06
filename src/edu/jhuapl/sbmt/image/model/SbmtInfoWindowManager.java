package edu.jhuapl.sbmt.image.model;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import edu.jhuapl.saavtk.gui.ModelInfoWindow;
import edu.jhuapl.saavtk.gui.WindowManager;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.util.Properties;

public class SbmtInfoWindowManager implements WindowManager, PropertyChangeListener
{
    static HashMap<Class, InfoWindowManagerBuilder<Model>> registeredPanels = new HashMap<Class, InfoWindowManagerBuilder<Model>>();

    static HashMap<Model, ModelInfoWindow> activatedInfoPanels = new HashMap<Model, ModelInfoWindow>();

    public static void registerInfoWindowManager(Class model, InfoWindowManagerBuilder<Model> builder)
    {
    	registeredPanels.put(model, builder);
    }

    ModelManager modelManager;

    public SbmtInfoWindowManager(ModelManager modelManager)
    {
        this.modelManager = modelManager;
    }

    @Override
	public void addData(final Model model) throws Exception
    {
        if (activatedInfoPanels.containsKey(model))
        {
        	activatedInfoPanels.get(model).setVisible(true);
        	activatedInfoPanels.get(model).toFront();
        }
        else
        {
            final ModelInfoWindow infoPanel = createModelInfoWindow(model, modelManager);

            if (infoPanel == null)
            {
                throw new Exception("The Info Panel Manager cannot handle the model you specified.");
            }

            final Model collectionModel = infoPanel.getCollectionModel();

            model.addPropertyChangeListener(infoPanel);

            collectionModel.addPropertyChangeListener(this);

            infoPanel.addWindowListener(new WindowAdapter()
            {
                @Override
					public void windowClosed(WindowEvent e)
                {
                    Model mod = infoPanel.getModel();
                    activatedInfoPanels.remove(mod);
                    model.removePropertyChangeListener(infoPanel);
                    collectionModel.removePropertyChangeListener(SbmtInfoWindowManager.this);
                }
            });

            activatedInfoPanels.put(model, infoPanel);
        }
    }

    @Override
	public void propertyChange(PropertyChangeEvent e)
    {
        if (e.getPropertyName().equals(Properties.MODEL_REMOVED))
        {
            Object model = e.getNewValue();
            if (activatedInfoPanels.containsKey(model))
            {
                ModelInfoWindow frame = activatedInfoPanels.get(model);
                frame.setVisible(false);
                frame.dispose();
            }
        }
    }

    public ModelInfoWindow createModelInfoWindow(Model model, ModelManager modelManager)
    {
    	//TODO FOR NOW, commenting out images since new way deals with this differently
//    	if (model instanceof ColorImage)
//    		return registeredPanels.get(ColorImage.class).buildModelInfoWindow(model);
//    	else if (model instanceof Image)
//    		return registeredPanels.get(Image.class).buildModelInfoWindow(model);
//    	else
    		return registeredPanels.get(model.getClass()).buildModelInfoWindow(model);
    }
}
