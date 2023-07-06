package edu.jhuapl.sbmt.image.ui.offlimb;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;



public class OfflimbImageControlPanel extends JPanel
{

    private JLabel footprintDepthLabel;
    private JLabel footprintDepthValue;
    private DepthSlider footprintDepthSlider;

    private JLabel footprintTransparencyLabel;
    private JLabel footprintTransparencyValue;
    private AlphaSlider footprintTransparencySlider;

//    private JLabel imageContrastLabel;
//    private JLabel imageContrastValue;
//    private ContrastSlider imageContrastSlider;

    private JCheckBox showOfflimbButton;
    private ShowBoundaryButton showBoundaryButton;
//    private ColorChooser boundaryColorPicker;
    private JButton chooseBoundaryColorBtn;
	private SyncContrastSlidersButton syncContrastButton;
	private JButton resetButton;

	private DecimalFormat formatter;

    public OfflimbImageControlPanel()
    {
        super();
        this.formatter = new DecimalFormat("##.##");
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();


        JPanel depthPanel = new JPanel();
        depthPanel.setLayout(new BoxLayout(depthPanel, BoxLayout.X_AXIS));
        footprintDepthLabel = new JLabel("Off-limb footprint depth:");
        depthPanel.add(footprintDepthLabel);

        footprintDepthValue = new JLabel(" 0");
        depthPanel.add(footprintDepthValue);

        footprintDepthSlider = new DepthSlider();
        footprintDepthSlider.setValue(0);
        depthPanel.add(footprintDepthSlider);

        JPanel transparencyPanel = new JPanel();
        transparencyPanel
                .setLayout(new BoxLayout(transparencyPanel, BoxLayout.X_AXIS));
        footprintTransparencyLabel = new JLabel(
                "Off-limb footprint opacity:"); // changed to opacity because of the way it works (opposite of transparency)
        transparencyPanel.add(footprintTransparencyLabel);

        footprintTransparencyValue = new JLabel(" 50%");
        transparencyPanel.add(footprintTransparencyValue);

        footprintTransparencySlider = new AlphaSlider();
        footprintTransparencySlider.setValue(50);
        transparencyPanel.add(footprintTransparencySlider);

        JPanel buttonPanel = new JPanel();

        showOfflimbButton = new JCheckBox("Show Offlimb");
        buttonPanel.add(showOfflimbButton);
//        JPanel contrastPanel = new JPanel();
//        contrastPanel.setLayout(new BoxLayout(contrastPanel, BoxLayout.X_AXIS));
//        imageContrastLabel = new JLabel("Off-limb Contrast:");
//        contrastPanel.add(imageContrastLabel);

//        imageContrastValue = new JLabel("0");
//        contrastPanel.add(imageContrastValue);

//        imageContrastSlider = new ContrastSlider(image, false);
        syncContrastButton = new SyncContrastSlidersButton();
        syncContrastButton.setSelected(true);
//        contrastPanel.add(imageContrastSlider);

//        syncContrastButton = syncButton;
//        buttonPanel.add(syncContrastButton);

        showBoundaryButton = new ShowBoundaryButton();
        buttonPanel.add(showBoundaryButton);

        chooseBoundaryColorBtn = new JButton("Set Boundary Color");
        buttonPanel.add(chooseBoundaryColorBtn);


        this.resetButton = new JButton("Reset");
        buttonPanel.add(resetButton);


        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 3, 10);
        add(depthPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 10);
        add(transparencyPanel, gridBagConstraints);

//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.gridwidth = 2;
//        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
//        gridBagConstraints.insets = new java.awt.Insets(3, 10, 3, 10);
//        add(contrastPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(3, 10, 3, 10);
        add(buttonPanel, gridBagConstraints);

    }

    public JLabel getFootprintDepthValue()
    {
        return footprintDepthValue;
    }

    public DepthSlider getFootprintDepthSlider()
    {
        return footprintDepthSlider;
    }

    public JLabel getFootprintTransparencyValue()
    {
        return footprintTransparencyValue;
    }

    public AlphaSlider getFootprintTransparencySlider()
    {
        return footprintTransparencySlider;
    }

//    public JLabel getImageContrastValue()
//    {
//        return imageContrastValue;
//    }

//    public ContrastSlider getImageContrastSlider()
//    {
//        return imageContrastSlider;
//    }

    public JCheckBox getShowOfflimbButton()
    {
        return showOfflimbButton;
    }

    public ShowBoundaryButton getShowBoundaryButton()
    {
        return showBoundaryButton;
    }
    public SyncContrastSlidersButton getSyncContrastButton()
    {
        return syncContrastButton;
    }

    public void setFootprintDepthSlider(DepthSlider footprintDepthSlider)
    {
        this.footprintDepthSlider = footprintDepthSlider;
    }

    public void setFootprintTransparencySlider(
            AlphaSlider footprintTransparencySlider)
    {
        this.footprintTransparencySlider = footprintTransparencySlider;
    }

    public void setShowBoundaryButton(ShowBoundaryButton showbounds)
    {
        this.showBoundaryButton = showbounds;
    }

//    public void setImageContrastSlider(ContrastSlider imageContrastSlider)
//    {
//        this.imageContrastSlider = imageContrastSlider;
//    }

	public JButton getResetButton() {
		return resetButton;
	}

	public JButton getBoundaryColorBtn() {
		return chooseBoundaryColorBtn;
	}


}
