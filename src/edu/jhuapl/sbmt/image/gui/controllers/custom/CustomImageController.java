package edu.jhuapl.sbmt.image.gui.controllers.custom;

import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.common.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.common.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.image.CustomImageKeyInterface;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.core.imageui.search.ImagingSearchPanel;
import edu.jhuapl.sbmt.image.gui.controllers.color.ColorImageController;
import edu.jhuapl.sbmt.image.gui.controllers.cubes.ImageCubeController;
import edu.jhuapl.sbmt.image.gui.controllers.cubes.SpectralImageCubeController;
import edu.jhuapl.sbmt.image.gui.model.CustomImageResultsListener;
import edu.jhuapl.sbmt.image.gui.model.custom.CustomColorImageModel;
import edu.jhuapl.sbmt.image.gui.model.custom.CustomImageCubeModel;
import edu.jhuapl.sbmt.image.gui.model.custom.CustomImagesModel;
import edu.jhuapl.sbmt.image.gui.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.image.gui.ui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.image.model.ImageCollection;
import edu.jhuapl.sbmt.image.model.ImageCubeCollection;

public class CustomImageController
{
    CustomImageResultsTableController imageResultsTableController;
    CustomImagesControlController controlController;
    ImageCubeController imageCubeController;
    ColorImageController colorImageController;

    private ImagingSearchPanel panel;

    protected final ModelManager modelManager;
    protected final Renderer renderer;
    private CustomImagesModel customImageModel;


    public CustomImageController(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            final PickManager pickManager, Renderer renderer,
            ImagingInstrument instrument)
    {
        this.modelManager = modelManager;
        this.renderer = renderer;

        ImageSearchModel imageSearchModel = new ImageSearchModel(smallBodyConfig, modelManager, renderer, instrument);
        ImageCollection imageCollection = (ImageCollection)modelManager.getModel(imageSearchModel.getImageCollectionModelName());

        customImageModel = new CustomImagesModel(imageSearchModel);
        customImageModel.addResultsChangedListener(new CustomImageResultsListener()
        {
            @Override
            public void resultsChanged(List<CustomImageKeyInterface> results)
            {
                List<List<String>> resultList = new Vector<List<String>>();
                for (CustomImageKeyInterface info : results)
                {
                    resultList.add(info.toList());
                }
                customImageModel.setImageResults(resultList);
                imageResultsTableController.setImageResults(resultList);
            }
        });

        ImageCollection customImageCollection = customImageModel.getImageCollection();
        this.imageResultsTableController = new CustomOfflimbImageResultsTableController(instrument, customImageCollection, customImageModel, renderer, infoPanelManager, spectrumPanelManager);
        this.imageResultsTableController.setImageResultsPanel();

        this.controlController = new CustomImagesControlController(customImageModel);

        CustomImageCubeModel customCubeModel = new CustomImageCubeModel();
        ImageCubeCollection imageCubeCollection = (ImageCubeCollection)customImageModel.getModelManager().getModel(customCubeModel.getImageCubeCollectionModelName());
        customCubeModel.setImageSearchModel(customImageModel);
        customCubeModel.setColorImageCollection(imageCubeCollection);
        ImageCubePopupMenu imageCubePopupMenu = new ImageCubePopupMenu(imageCubeCollection, infoPanelManager, spectrumPanelManager, renderer, getPanel());
        this.imageCubeController = new SpectralImageCubeController(customImageModel, customCubeModel, infoPanelManager, imageCubePopupMenu, spectrumPanelManager, renderer);
        CustomColorImageModel colorModel = new CustomColorImageModel();
        this.colorImageController = new ColorImageController(customImageModel, colorModel, infoPanelManager, renderer);

        init();
    }

    public void init()
    {
        panel = new ImagingSearchPanel();
        panel.addSubPanel(controlController.getPanel());
        panel.addSubPanel(imageResultsTableController.getPanel());
        panel.addSubPanel(imageCubeController.getPanel());
        panel.addSubPanel(colorImageController.getPanel());
    }

    public JPanel getPanel()
    {
        return panel;
    }
}