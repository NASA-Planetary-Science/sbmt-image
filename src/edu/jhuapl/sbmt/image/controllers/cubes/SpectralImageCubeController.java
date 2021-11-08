package edu.jhuapl.sbmt.image.controllers.cubes;

import java.util.Set;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.sbmt.image.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.image.common.ImageKeyInterface;
import edu.jhuapl.sbmt.image.gui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.image.gui.spectral.SpectralImageCubeGenerationPanel;
import edu.jhuapl.sbmt.image.types.ImageSearchModel;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCube;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCubeCollection;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCubeModel;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCube.ImageCubeKey;

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

                ImageCubeCollection images = (ImageCubeCollection)model.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName()).get(0);

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
