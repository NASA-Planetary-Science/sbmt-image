/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ImagingSearchPanel.java
 *
 * Created on May 5, 2011, 3:15:17 PM
 */
package edu.jhuapl.sbmt.image.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.common.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.common.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.image.ImageKeyInterface;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.ImageCube;
import edu.jhuapl.sbmt.image.model.ImageCube.ImageCubeKey;
import edu.jhuapl.sbmt.image.model.ImageCubeCollection;

@Deprecated
public class CubicalImagingSearchPanel extends ImagingSearchPanel implements PropertyChangeListener, ChangeListener, ListSelectionListener
{
    private JPanel bandPanel;
    private JLabel bandValue;
    private JSlider monoSlider;
    private JCheckBox defaultFrustum;
    private BoundedRangeModel monoBoundedRangeModel;
    private javax.swing.JList imageList = null;

    private int nbands = 1;
    private int currentSlice = 0;
    int numImagesInCollection = -1;

    public int getCurrentSlice() { return currentSlice; }

    public String getCurrentBand() { return Integer.toString(currentSlice); }

    /** Creates new form ImagingSearchPanel */
    public CubicalImagingSearchPanel(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            final PickManager pickManager,
            Renderer renderer,
            ImagingInstrument instrument)
    {
        super(smallBodyConfig, modelManager, infoPanelManager, spectrumPanelManager, pickManager, renderer, instrument);
    }


    public void initComponents()
    {

    }

    protected void populateMonochromePanel(JPanel panel)
    {
        panel.setLayout(new BorderLayout());
        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        bandPanel.add(new JLabel("Layer:"));
        int midband = (nbands-1) / 2;
        String midbandString = Integer.toString(midband);
        bandValue = new JLabel(midbandString);
        bandPanel.add(bandValue);
        monoBoundedRangeModel = new DefaultBoundedRangeModel(midband, 0, 0, nbands-1);
        monoSlider = new JSlider(monoBoundedRangeModel);
        monoSlider.addChangeListener(this);

        panel.add(bandPanel, BorderLayout.NORTH);
        panel.add(monoSlider, BorderLayout.SOUTH);
    }

    private void setNumberOfBands(int nbands)
    {
        // Select midband by default
        setNumberOfBands(nbands, (nbands-1)/2);
    }

    private void setNumberOfBands(int nbands, int activeBand)
    {
        this.nbands = nbands;
        String activeBandString = Integer.toString(activeBand);
        bandValue.setText(activeBandString);
        monoBoundedRangeModel = new DefaultBoundedRangeModel(activeBand, 0, 0, nbands-1);
        monoSlider.setModel(monoBoundedRangeModel);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
        {
            // If an image was added/removed, then
        	ImageCubeCollection images = (ImageCubeCollection)getModelManager().getModel(getImageCubeCollectionModelName());
            int currImagesInCollection = images.getImages().size();

            if(currImagesInCollection != numImagesInCollection)
            {
                // Update count of number of images in collection and update slider
                numImagesInCollection = currImagesInCollection;
                valueChanged(null);
            }
        }

        super.propertyChange(evt);
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        imageList = getImageCubesDisplayedList();

        int index = imageList.getSelectedIndex();
        ImageCubeKey selectedValue = (ImageCubeKey)imageList.getSelectedValue();
        if (selectedValue == null)
            return;

        String imagestring = selectedValue.fileNameString();
        String[]tokens = imagestring.split(",");
        String imagename = tokens[0].trim();

//        System.out.println("Cubical Images Panel Slider Moved");
        JSlider source = (JSlider)e.getSource();
        currentSlice = (int)source.getValue();
        bandValue.setText(Integer.toString(currentSlice));

        ImageCubeCollection images = (ImageCubeCollection)getModelManager().getModel(getImageCubeCollectionModelName());

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
               return; // twupy1: Only change band for a single image now even if multiple ones are highlighted since different cubical images can have different numbers of bands.
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        imageList = getImageCubesDisplayedList();

        int index = imageList.getSelectedIndex();
        ImageCubeKey selectedValue = (ImageCubeKey)imageList.getSelectedValue();
        if (selectedValue == null)
        {
            setNumberOfBands(1);
            return;
        }

        String imagestring = selectedValue.fileNameString();
        String[]tokens = imagestring.split(",");
        String imagename = tokens[0].trim();
//        System.out.println("Image: " + index + ", " + imagename);

        ImageCubeCollection images = (ImageCubeCollection)getModelManager().getModel(getImageCubeCollectionModelName());

        Set<ImageCube> imageSet = images.getImages();
        for (ImageCube image : imageSet)
        {
            ImageKeyInterface key = image.getKey();
            String name = image.getImageName();
            if (name.equals(imagename))
            {
                int depth = image.getImageDepth();
                currentSlice = image.getCurrentSlice();
                setNumberOfBands(depth,currentSlice);
                image.setDisplayedImageRange(null);
                return; // twupy1: Only do this for a single image now even if multiple ones are highlighted since differeent cubical images can have different numbers of bands.
            }
        }

        // if no multi-band image found, set number of bands in slider to 1
        setNumberOfBands(1);
    }

//    @Override
//    public void stateChanged(ChangeEvent e)
//    {
//        JSlider source = (JSlider)e.getSource();
//        currentSlice = (int)source.getValue();
//        bandValue.setText(Integer.toString(currentSlice));
//
//        ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());
//
//        Set<Image> imageSet = images.getImages();
//        for (Image i : imageSet)
//        {
//            PerspectiveImage image = (PerspectiveImage)i;
//            ImageKey key = image.getKey();
//            ImageType type = key.instrument.type;
////            String name = i.getImageName();
////            Boolean isVisible = i.isVisible();
////            System.out.println(name + ", " + type + ", " + isVisible);
////            if (type == ImageType.LEISA_JUPITER_IMAGE) // this should not be specific to a given image type, should it? -turnerj1
////            {
//                if (image.isVisible())
//                {
//                   image.setCurrentSlice(currentSlice);
////                   image.setDisplayedImageRange(image.getDisplayedRange());
//                   image.setDisplayedImageRange(null);
//                   if (!source.getValueIsAdjusting())
//                   {
////                        System.out.println("Recalculate footprint...");
//                        image.loadFootprint();
//                        image.firePropertyChange();
//                   }
//                }
////            }
//        }
//
////            System.out.println("State changed: " + fps);
//    }
//
}
