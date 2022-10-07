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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

@Deprecated
public class QuadraspectralImagingSearchPanel extends ImagingSearchPanel implements ActionListener
{
    private JPanel bandPanel;
    private JLabel bandValue;

    private JComboBox monoComboBox;
    private ComboBoxModel monoComboBoxModel;

    private JCheckBox defaultFrustum;

    private String[] bandNames = { "Red", "Blue", "NIR", "MH4" };
    private Integer[] bandIndices = { 0, 1, 2, 3 };

    // set current band to MH4 band
    private int currentBandIndex = 3;
    private Map<String, Integer> bandNamesToPrefixes = new HashMap<String, Integer>();

    public int getCurrentSlice() { return currentBandIndex; }

    public String getCurrentBand() { return bandNames[currentBandIndex]; }

    /** Creates new form ImagingSearchPanel */
    public QuadraspectralImagingSearchPanel(SmallBodyViewConfig smallBodyConfig,
            final ModelManager modelManager,
            SbmtInfoWindowManager infoPanelManager,
            SbmtSpectrumWindowManager spectrumPanelManager,
            final PickManager pickManager,
            Renderer renderer,
            ImagingInstrument instrument)
    {
        super(smallBodyConfig, modelManager, infoPanelManager, spectrumPanelManager, pickManager, renderer, instrument);

        for (int i=0; i<bandNames.length; i++)
            bandNamesToPrefixes.put(bandNames[i], bandIndices[i]);

    }

    public ImagingSearchPanel init()
    {
        super.init();

        return this;
    }

    protected void populateMonochromePanel(JPanel panel)
    {
        panel.setLayout(new BorderLayout());
        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

        bandPanel.add(new JLabel("Band:"));
        monoComboBoxModel = new DefaultComboBoxModel(bandNames);
        monoComboBox = new JComboBox(monoComboBoxModel);
        monoComboBox.addActionListener(this);
        // initialize for the MH4 band
        monoComboBox.setSelectedIndex(3);
        bandPanel.add(monoComboBox);

        defaultFrustum = new JCheckBox("Default Frame");
        defaultFrustum.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultFrustumActionPerformed(evt);
            }
        });

        bandPanel.add(defaultFrustum);
        panel.add(bandPanel, BorderLayout.WEST);
    }

    @Override
    protected List<List<String>> processResults(List<List<String>> input)
    {
        List<List<String>> results = new ArrayList<List<String>>();
        Set<String> fileSuffixes = new HashSet<String>();

        for (List<String> item : input)
        {
            String path = item.get(0);
            String time = item.get(1);
            String[] pathArray = path.split("/");
            int size = pathArray.length;
            String fileName = pathArray[size-1];
            String fileSuffix = fileName.substring(4);
            if (!fileSuffixes.contains(fileSuffix))
            {
                fileSuffixes.add(fileSuffix);
                String resultPath = "/";
                for (int i=0; i<size-1; i++)
                    resultPath += pathArray[i] + "/";
                resultPath += fileSuffix;
                List<String> newItem = new ArrayList<String>();
                newItem.add(resultPath);
                newItem.add(time);

                results.add(newItem);
            }
        }

        return results;
    }

    protected void loadImage(ImageKey key, ImageCollection images) throws FitsException, IOException
    {
        super.loadImage(key, images);
        PerspectiveImage image = (PerspectiveImage)images.getImage(key);
        image.setCurrentSlice(currentBandIndex);
        image.setDisplayedImageRange(null);
        image.loadFootprint();
        image.firePropertyChange();
   }

    protected void unloadImage(ImageKeyInterface key, ImageCollection images)
    {
        super.unloadImage(key, images);
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
            if (type == ImageType.MVIC_JUPITER_IMAGE) // this should not be specific to a given image type, should it? -turnerj1
            {
                if (image instanceof PerspectiveImage)
                {
                   ((PerspectiveImage)image).setUseDefaultFootprint(defaultFrustum.isSelected());
                }
            }
        }
    }//GEN-LAST:event_greenMonoCheckboxActionPerformed

    @Override
    public void actionPerformed(ActionEvent arg0)
    {
        String newBandName = (String)((JComboBox)arg0.getSource()).getSelectedItem();
        int newBandIndex = bandNamesToPrefixes.get(newBandName);
        currentBandIndex = newBandIndex;

        ImageCollection images = (ImageCollection)getModelManager().getModel(getImageCollectionModelName());
        Set<Image> imageSet = images.getImages();

        for (Image i : imageSet)
        {
            PerspectiveImage image = (PerspectiveImage)i;
            ImageKeyInterface key = image.getKey();
            ImageType type = key.getInstrument().getType();
            if (type == ImageType.MVIC_JUPITER_IMAGE) // this should not be specific to a given image type, should it? -turnerj1
            {
                image.setCurrentSlice(newBandIndex);
                image.setDisplayedImageRange(null);
                image.loadFootprint();
                image.firePropertyChange();
            }
        }
    }
}
