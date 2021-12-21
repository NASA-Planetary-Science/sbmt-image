package edu.jhuapl.sbmt.image.controllers.custom;

import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.IPositionOrientationManager;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.image.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.image.controllers.color.ColorImageController;
import edu.jhuapl.sbmt.image.controllers.cubes.ImageCubeController;
import edu.jhuapl.sbmt.image.controllers.cubes.SpectralImageCubeController;
import edu.jhuapl.sbmt.image.core.ImagingInstrument;
import edu.jhuapl.sbmt.image.core.listeners.CustomImageResultsListener;
import edu.jhuapl.sbmt.image.gui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.image.gui.search.ImagingSearchPanel;
import edu.jhuapl.sbmt.image.types.ImageCollection;
import edu.jhuapl.sbmt.image.types.ImageSearchModel;
import edu.jhuapl.sbmt.image.types.customImage.CustomColorImageModel;
import edu.jhuapl.sbmt.image.types.customImage.CustomImageCubeModel;
import edu.jhuapl.sbmt.image.types.customImage.CustomImageKeyInterface;
import edu.jhuapl.sbmt.image.types.customImage.CustomImagesModel;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCubeCollection;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundaryCollection;

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
        ImageCollection imageCollection = (ImageCollection)modelManager.getModel(imageSearchModel.getImageCollectionModelName()).get(0);
        PerspectiveImageBoundaryCollection imageBoundaryCollection = (PerspectiveImageBoundaryCollection)modelManager.getModel(imageSearchModel.getImageBoundaryCollectionModelName()).get(0);

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
        ImageCubeCollection imageCubeCollection = (ImageCubeCollection)customImageModel.getModelManager().getModel(customCubeModel.getImageCubeCollectionModelName()).get(0);
        customCubeModel.setImageSearchModel(customImageModel);
        customCubeModel.setColorImageCollection(imageCubeCollection);
        ImageCubePopupMenu imageCubePopupMenu = new ImageCubePopupMenu(imageCubeCollection, /*imageBoundaryCollection,*/ infoPanelManager, spectrumPanelManager, renderer, getPanel());
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

	public void setPositionOrientationManager(IPositionOrientationManager manager)
	{
		imageResultsTableController.setPositionOrientationManager(manager);
	}
}