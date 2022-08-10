package edu.jhuapl.sbmt.image.gui.controllers.spectral;

import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Controller;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.common.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.common.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.core.imageui.search.ImagingSearchPanel;
import edu.jhuapl.sbmt.image.gui.controllers.color.ColorImageController;
import edu.jhuapl.sbmt.image.gui.controllers.cubes.SpectralImageCubeController;
import edu.jhuapl.sbmt.image.gui.controllers.images.ImageResultsTableController;
import edu.jhuapl.sbmt.image.gui.controllers.images.OfflimbImageResultsTableController;
import edu.jhuapl.sbmt.image.gui.controllers.search.SpectralImageSearchParametersController;
import edu.jhuapl.sbmt.image.gui.model.color.ColorImageModel;
import edu.jhuapl.sbmt.image.gui.model.cubes.ImageCubeModel;
import edu.jhuapl.sbmt.image.gui.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.image.gui.ui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.image.model.ImageCollection;
import edu.jhuapl.sbmt.image.model.ImageCubeCollection;


public class SpectralImagingSearchController implements Controller<ImageSearchModel, ImagingSearchPanel>
{
    ImageResultsTableController imageResultsTableController;
    SpectralImageSearchParametersController searchParametersController;
    SpectralImageCubeController imageCubeController;
    ColorImageController colorImageController;

    private final ImageSearchModel model;
    private final ImagingSearchPanel panel;

    private SmallBodyViewConfig smallBodyConfig;
    protected final ModelManager modelManager;
    private final SbmtInfoWindowManager infoPanelManager;
    private final SbmtSpectrumWindowManager spectrumPanelManager;
    private final PickManager pickManager;
    protected final Renderer renderer;


    public SpectralImagingSearchController(SmallBodyViewConfig smallBodyConfig,
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
        ImageCollection imageCollection = (ImageCollection)modelManager.getModel(imageSearchModel.getImageCollectionModelName());

        this.imageResultsTableController = new OfflimbImageResultsTableController(instrument, imageCollection, imageSearchModel, renderer, infoPanelManager, spectrumPanelManager);
        this.imageResultsTableController.setImageResultsPanel();

        this.searchParametersController = new SpectralImageSearchParametersController(imageSearchModel, pickManager);
        this.searchParametersController.setupSearchParametersPanel();

        ImageCubeModel cubeModel = new ImageCubeModel();
        ImageCubeCollection imageCubeCollection = (ImageCubeCollection)imageSearchModel.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName());
        cubeModel.setColorImageCollection(imageCubeCollection);
        ImageCubePopupMenu imageCubePopupMenu = new ImageCubePopupMenu(imageCubeCollection, infoPanelManager, spectrumPanelManager, renderer, getPanel());
        this.imageCubeController = new SpectralImageCubeController(imageSearchModel, cubeModel, infoPanelManager, imageCubePopupMenu, spectrumPanelManager, renderer);

        ColorImageModel colorModel = new ColorImageModel();
        this.colorImageController = new ColorImageController(imageSearchModel, colorModel, infoPanelManager, renderer);

        this.model = imageSearchModel;
        this.panel = new ImagingSearchPanel();
        this.panel.addSubPanel(searchParametersController.getPanel());
        this.panel.addSubPanel(imageResultsTableController.getPanel());
        this.panel.addSubPanel(imageCubeController.getPanel());
        this.panel.addSubPanel(colorImageController.getPanel());
    }

    protected void initExtraComponents()
    {
        // to be overridden by subclasses
    }

    protected void populateMonochromePanel(JPanel panel)
    {
        // to be overridden by subclasses
    }

    public JPanel getPanel()
    {
        return panel;
    }

    @Override
    public ImageSearchModel getModel()
    {
        return model;
    }

    @Override
    public ImagingSearchPanel getView()
    {
        return panel;
    }



}
