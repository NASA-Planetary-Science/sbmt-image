package edu.jhuapl.sbmt.image.controllers.hyperspectral;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.image.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.image.controllers.color.ColorImageController;
import edu.jhuapl.sbmt.image.controllers.cubes.ImageCubeController;
import edu.jhuapl.sbmt.image.controllers.images.OfflimbImageResultsTableController;
import edu.jhuapl.sbmt.image.controllers.search.SpectralImageSearchParametersController;
import edu.jhuapl.sbmt.image.core.ImagingInstrument;
import edu.jhuapl.sbmt.image.gui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.image.gui.search.ImagingSearchPanel;
import edu.jhuapl.sbmt.image.types.ImageCollection;
import edu.jhuapl.sbmt.image.types.ImageSearchModel;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImageModel;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCubeCollection;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCubeModel;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundaryCollection;


public class HyperspectralImagingSearchController
{
    OfflimbImageResultsTableController imageResultsTableController;
    SpectralImageSearchParametersController searchParametersController;
    ImageCubeController imageCubeController;
    ColorImageController colorImageController;

    private ImagingSearchPanel panel;

    private SmallBodyViewConfig smallBodyConfig;
    protected final ModelManager modelManager;
    private final SbmtInfoWindowManager infoPanelManager;
    private final SbmtSpectrumWindowManager spectrumPanelManager;
    private final PickManager pickManager;
    protected final Renderer renderer;


    public HyperspectralImagingSearchController(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            final PickManager pickManager, Renderer renderer,
            ImagingInstrument instrument)
    {
        this.smallBodyConfig = smallBodyConfig;
        this.modelManager = modelManager;
        this.infoPanelManager = infoPanelManager;
        this.spectrumPanelManager = spectrumPanelManager;
        this.renderer = renderer;
        this.pickManager = pickManager;

        ImageSearchModel imageSearchModel = new ImageSearchModel(smallBodyConfig, modelManager, renderer, instrument);
        ImageCollection imageCollection = (ImageCollection)modelManager.getModel(imageSearchModel.getImageCollectionModelName()).get(0);
        PerspectiveImageBoundaryCollection imageBoundaryCollection = (PerspectiveImageBoundaryCollection)modelManager.getModel(imageSearchModel.getImageBoundaryCollectionModelName()).get(0);

        this.imageResultsTableController = new OfflimbImageResultsTableController(instrument, imageCollection, imageSearchModel, renderer, infoPanelManager, spectrumPanelManager);
        this.imageResultsTableController.setImageResultsPanel();

        this.searchParametersController = new SpectralImageSearchParametersController(imageSearchModel, pickManager);
        this.searchParametersController.setupSearchParametersPanel();

        ImageCubeModel cubeModel = new ImageCubeModel();
        ImageCubeCollection imageCubeCollection = (ImageCubeCollection)imageSearchModel.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName()).get(0);
        cubeModel.setColorImageCollection(imageCubeCollection);
        ImageCubePopupMenu imageCubePopupMenu = new ImageCubePopupMenu(imageCubeCollection, /*imageBoundaryCollection,*/ infoPanelManager, spectrumPanelManager, renderer, getPanel());
        this.imageCubeController = new ImageCubeController(imageSearchModel, cubeModel, infoPanelManager, imageCubePopupMenu, spectrumPanelManager, renderer);

        ColorImageModel colorModel = new ColorImageModel();
        this.colorImageController = new ColorImageController(imageSearchModel, colorModel, infoPanelManager, renderer);

        init();
    }

    public void init()
    {
        panel = new ImagingSearchPanel();
        panel.addSubPanel(searchParametersController.getPanel());
        panel.addSubPanel(imageResultsTableController.getPanel());
        panel.addSubPanel(imageCubeController.getPanel());
        panel.addSubPanel(colorImageController.getPanel());
    }

    public JPanel getPanel()
    {
        return panel;
    }
}
