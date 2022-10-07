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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.sbmt.common.client.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.common.client.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.common.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.core.image.Image;
import edu.jhuapl.sbmt.core.image.ImageKeyInterface;
import edu.jhuapl.sbmt.core.image.ImageType;
import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.core.rendering.PerspectiveImage;
import edu.jhuapl.sbmt.image.model.ImageCollection;
import edu.jhuapl.sbmt.image.model.keys.ImageKey;

import nom.tam.fits.FitsException;

public class HyperspectralImagingSearchPanel extends ImagingSearchPanel implements ActionListener, ChangeListener
{
    private JPanel bandPanel;
    private JLabel bandValue;
    private JSlider monoSlider;
    private JCheckBox defaultFrustum;
    private BoundedRangeModel monoBoundedRangeModel;

    private int nbands;
    private int currentSlice = 127;

    public int getCurrentSlice() { return currentSlice; }

    public String getCurrentBand() { return Integer.toString(currentSlice); }

    /** Creates new form ImagingSearchPanel */
    public HyperspectralImagingSearchPanel(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            final PickManager pickManager,
            Renderer renderer,
            ImagingInstrument instrument,
            int nbands)
    {
        super(smallBodyConfig, modelManager, infoPanelManager, spectrumPanelManager, pickManager, renderer, instrument);

        this.nbands = nbands;
    }

    public ImagingSearchPanel init()
    {
        super.init();

        getRedComboBox().addActionListener(this);
        getGreenComboBox().addActionListener(this);
        getBlueComboBox().addActionListener(this);

        return this;
    }

    protected void populateMonochromePanel(JPanel panel)
    {
        panel.setLayout(new BorderLayout());
        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        bandPanel.add(new JLabel("Band:"));
        int midband = (nbands-1) / 2;
        String midbandString = Integer.toString(midband);
        bandValue = new JLabel(midbandString);
        bandPanel.add(bandValue);
        monoBoundedRangeModel = new DefaultBoundedRangeModel(midband, 0, 0, nbands-1);
        monoSlider = new JSlider(monoBoundedRangeModel);
        monoSlider.addChangeListener(this);

        defaultFrustum = new JCheckBox("Default Frame");
        defaultFrustum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultFrustumActionPerformed(evt);
            }
        });

        bandPanel.add(defaultFrustum);

        panel.add(bandPanel, BorderLayout.NORTH);
        panel.add(monoSlider, BorderLayout.SOUTH);
    }


    protected void loadImage(ImageKey key, ImageCollection images) throws FitsException, IOException
    {
        super.loadImage(key, images);

        // For this panel, we have a global band slider as opposed to per image.  Therefore, have
        // the image come up with the currently chosen band
        PerspectiveImage loadedImage = (PerspectiveImage) images.getImage(key);
        loadedImage.setCurrentSlice(currentSlice);
        loadedImage.setDisplayedImageRange(null);
        loadedImage.loadFootprint();
        loadedImage.firePropertyChange();
    }

    protected void unloadImage(ImageKeyInterface key, ImageCollection images)
    {
        super.unloadImage(key, images);
    }

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        String newBandName = (String)((JComboBox)arg0.getSource()).getSelectedItem();
        System.out.println("ComboBox Value Changed: " + newBandName);
    }

    private void defaultFrustumActionPerformed(java.awt.event.ActionEvent evt)
    {
        ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());

        Set<Image> imageSet = images.getImages();
        for (Image i : imageSet)
        {
            PerspectiveImage image = (PerspectiveImage)i;
            ImageKeyInterface key = image.getKey();
            ImageType type = key.getInstrument().getType();
            if (type == ImageType.LEISA_JUPITER_IMAGE) // this should not be specific to a given image type, should it? -turnerj1
            {
                if (image instanceof PerspectiveImage)
                {
                   ((PerspectiveImage)image).setUseDefaultFootprint(defaultFrustum.isSelected());
                }
            }
        }
    }//GEN-LAST:event_greenMonoCheckboxActionPerformed

    @Override
    public void stateChanged(ChangeEvent e)
    {
        JSlider source = (JSlider)e.getSource();
        currentSlice = (int)source.getValue();
        bandValue.setText(Integer.toString(currentSlice));

        ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());

        Set<Image> imageSet = images.getImages();
        for (Image i : imageSet)
        {
            PerspectiveImage image = (PerspectiveImage)i;
            ImageKeyInterface key = image.getKey();
            ImageType type = key.getInstrument().getType();
            if (type == ImageType.LEISA_JUPITER_IMAGE) // this should not be specific to a given image type, should it? -turnerj1
            {
               image.setCurrentSlice(currentSlice);
               image.setDisplayedImageRange(null);
               if (!source.getValueIsAdjusting())
               {
                   image.loadFootprint();
                   image.firePropertyChange();
               }
            }
        }
    }
}
