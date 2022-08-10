package edu.jhuapl.sbmt.image.gui.controllers.cubes;

import java.util.Set;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.common.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.common.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.core.image.ImageKeyInterface;
import edu.jhuapl.sbmt.image.gui.model.cubes.ImageCubeModel;
import edu.jhuapl.sbmt.image.gui.model.images.ImageSearchModel;
import edu.jhuapl.sbmt.image.gui.ui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.image.gui.ui.spectral.SpectralImageCubeGenerationPanel;
import edu.jhuapl.sbmt.image.model.ImageCube;
import edu.jhuapl.sbmt.image.model.ImageCube.ImageCubeKey;
import edu.jhuapl.sbmt.image.model.ImageCubeCollection;

public class SpectralImageCubeController extends ImageCubeController
{
    private int currentSlice = 0;

    public SpectralImageCubeController(ImageSearchModel model,
            ImageCubeModel cubeModel,
            SbmtInfoWindowManager infoPanelManager,
            ImageCubePopupMenu imageCubePopupMenu,
            SbmtSpectrumWindowManager spectrumPanelManager, Renderer renderer)
    {
        super(model, cubeModel, infoPanelManager, imageCubePopupMenu, spectrumPanelManager, renderer);
    }

    @Override
    protected void setupPanel()
    {
        super.setupPanel();

        panel.getImageCubeTable().getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting() && (panel.getImageCubeTable().getSelectedRow() >= 0))
                {
                    ImageCube cube = cubeModel.getImageCubeCollection().getLoadedImages().get(panel.getImageCubeTable().getSelectedRow());
                    ((SpectralImageCubeGenerationPanel)panel).getLayerSlider().setEnabled(true);
                    ((SpectralImageCubeGenerationPanel)panel).getLayerSlider().setMaximum(0);
                    ((SpectralImageCubeGenerationPanel)panel).getLayerSlider().setMaximum(cube.getNimages()-1);
                }
            }
        });


        ((SpectralImageCubeGenerationPanel)panel).getLayerSlider().addChangeListener(new ChangeListener()
        {

            @Override
            public void stateChanged(ChangeEvent e)
            {
                JTable imageList = panel.getImageCubeTable();

                int index = imageList.getSelectedRow();
                if (index == -1)
                {
                    setNumberOfBands(1);
                    return;
                }

                ImageCube selectedImage = imageCubes.getLoadedImages().get(panel.getImageCubeTable().getSelectedRow());
                ImageCubeKey selectedValue = selectedImage.getImageCubeKey();

                String imagestring = selectedValue.fileNameString();
                String[]tokens = imagestring.split(",");
                String imagename = tokens[0].trim();

                JSlider source = (JSlider)e.getSource();
                currentSlice = (int)source.getValue();
                ((SpectralImageCubeGenerationPanel)panel).getLayerValue().setText(Integer.toString(currentSlice+1));

                ImageCubeCollection images = (ImageCubeCollection)model.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName());

                Set<ImageCube> imageSet = images.getImages();
                for (ImageCube image : imageSet)
                {
                    ImageKeyInterface key = image.getKey();
                    String name = image.getImageName();

                    if(name.equals(imagename))
                    {
                       image.setCurrentSlice(currentSlice);
                       image.setDisplayedImageRange(null);
                       if (!source.getValueIsAdjusting())
                       {
                            image.loadFootprint();
                            image.firePropertyChange();
                       }
                       return;
                    }
                }
            }
        });

    }

    private void setNumberOfBands(int nbands)
    {
        // Select midband by default
        setNumberOfBands(nbands, (nbands-1)/2);
    }

    private void setNumberOfBands(int nbands, int activeBand)
    {
        SpectralImageCubeGenerationPanel specPanel = (SpectralImageCubeGenerationPanel)panel;
        cubeModel.setNbands(nbands);
        specPanel.setNBands(nbands);
        String activeBandString = Integer.toString(activeBand+1);
        specPanel.getLayerValue().setText(activeBandString);
        DefaultBoundedRangeModel monoBoundedRangeModel = new DefaultBoundedRangeModel(activeBand, 0, 1, nbands-1);
        specPanel.getLayerSlider().setModel(monoBoundedRangeModel);
    }
}
