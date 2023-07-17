package edu.jhuapl.sbmt.image.ui.search;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import com.jidesoft.swing.CheckBoxTree;

import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.ui.SBMTDateSpinner;


public class ImageSearchParametersPanel extends JPanel
{
    protected CheckBoxTree checkBoxTree;
    private JFormattedTextField searchByNumberTextField;
    private JToggleButton selectRegionButton;
    private JComboBox<PointingSource> sourceComboBox;
    private JLabel sourceLabel;
    private JLabel startDateLabel;
    private SBMTDateSpinner startSpinner;
    private JButton submitButton;
    private JLabel toDistanceLabel;
    private JFormattedTextField toDistanceTextField;
    private JLabel toEmissionLabel;
    private JFormattedTextField toEmissionTextField;
    private JLabel toIncidenceLabel;
    private JFormattedTextField toIncidenceTextField;
    private JLabel toPhaseLabel;
    private JFormattedTextField toPhaseTextField;
    private JLabel toResolutionLabel;
    private JFormattedTextField toResolutionTextField;
    private JLabel endDateLabel;
    private JLabel endDistanceLabel;
    private JLabel endEmissionLabel;
    private JLabel endIncidenceLabel;
    private JLabel endPhaseLabel;
    private JLabel endResolutionLabel;
    private SBMTDateSpinner endSpinner;
    private JCheckBox excludeGaskellCheckBox;

    private JLabel fromDistanceLabel;
    private JFormattedTextField fromDistanceTextField;
    private JLabel fromEmissionLabel;
    private JFormattedTextField fromEmissionTextField;
    private JLabel fromIncidenceLabel;
    private JFormattedTextField fromIncidenceTextField;
    private JLabel fromPhaseLabel;
    private JFormattedTextField fromPhaseTextField;
    private JLabel fromResolutionLabel;
    private JFormattedTextField fromResolutionTextField;
    private JComboBox<String> hasLimbComboBox;
    private JLabel hasLimbLabel;
    protected JScrollPane hierarchicalSearchScrollPane;
    private JButton clearRegionButton;
    private JTextField textField;
    private JPanel auxPanel;
    private JRadioButton parametersRadioButton;
    private JRadioButton filenameRadioButton;
    private boolean isFixedListSearch = false;
    private ButtonGroup searchByGroup;
    private JPanel parametersPanel;
    private JPanel filenamePanel;

    public ImageSearchParametersPanel()
    {
        setBorder(new TitledBorder(null, "Search Parameters",
                TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // TODO: Override and setup in subclass
        hierarchicalSearchScrollPane = new javax.swing.JScrollPane();

        Component verticalStrut_10 = Box.createVerticalStrut(5);
        add(verticalStrut_10);

        JPanel pointingPanel = new JPanel();
        add(pointingPanel);
        pointingPanel.setLayout(new BoxLayout(pointingPanel, BoxLayout.X_AXIS));

        sourceLabel = new JLabel("Pointing:");
        pointingPanel.add(sourceLabel);

        sourceComboBox = new JComboBox<PointingSource>();
        sourceComboBox.setMaximumSize(
                new java.awt.Dimension(sourceComboBox.getWidth(), 22));
        pointingPanel.add(sourceComboBox);

        Component horizontalGlue_7 = Box.createHorizontalGlue();
        pointingPanel.add(horizontalGlue_7);

        excludeGaskellCheckBox = new JCheckBox("Exclude SPC Derived");
        pointingPanel.add(excludeGaskellCheckBox);
        // TODO Auto-generated constructor stub
        excludeGaskellCheckBox.setVisible(false);

        Component verticalGlue = Box.createVerticalGlue();
        add(verticalGlue);

        JPanel choicePanel = new JPanel();
        add(choicePanel);

        parametersRadioButton = new JRadioButton(
                "Search by Parameters");
        if (isFixedListSearch == false)
        	choicePanel.add(parametersRadioButton);


        filenameRadioButton = new JRadioButton(
                "Search by Filename");
        choicePanel.add(filenameRadioButton);


        searchByGroup = new ButtonGroup();
        searchByGroup.add(filenameRadioButton);
        searchByGroup.add(parametersRadioButton);
        parametersRadioButton.setSelected(true);


        Component verticalStrut_9 = Box.createVerticalStrut(20);
        add(verticalStrut_9);

        filenamePanel = new JPanel();
        filenamePanel.setVisible(false);
        add(filenamePanel);
        filenamePanel.setLayout(new BoxLayout(filenamePanel, BoxLayout.X_AXIS));

        JLabel lblFilename = new JLabel("Filename:");
        filenamePanel.add(lblFilename);

        searchByNumberTextField = new JFormattedTextField();
        searchByNumberTextField.setMaximumSize( new Dimension(100, searchByNumberTextField.getPreferredSize().height) );
        filenamePanel.add(searchByNumberTextField);
        searchByNumberTextField.setColumns(30);
        searchByNumberTextField.setPreferredSize(
                new Dimension(200, 20));
//        searchByNumberTextField.setMaximumSize(
//                new Dimension(searchByNumberTextField.getWidth(), 20));

        Component horizontalGlue = Box.createHorizontalGlue();
        filenamePanel.add(horizontalGlue);

        parametersPanel = new JPanel();
        add(parametersPanel);
        parametersPanel
                .setLayout(new BoxLayout(parametersPanel, BoxLayout.Y_AXIS));

        parametersRadioButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
            	searchByNumberTextField.setText("");
                parametersPanel.setVisible(true);
                filenamePanel.setVisible(false);
//                selectRegionButton.setVisible(true);
//                clearRegionButton.setVisible(true);
            }
        });

        filenameRadioButton.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                parametersPanel.setVisible(false);
                filenamePanel.setVisible(true);
//                selectRegionButton.setVisible(false);
//                clearRegionButton.setVisible(false);
            }
        });

        JPanel panel_1 = new JPanel();
        parametersPanel.add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        startDateLabel = new JLabel("Start Date:");
        panel_1.add(startDateLabel);

        startSpinner = new SBMTDateSpinner();
        panel_1.add(startSpinner);

        Component horizontalGlue_8 = Box.createHorizontalGlue();
        panel_1.add(horizontalGlue_8);

        Component verticalStrut_8 = Box.createVerticalStrut(10);
        parametersPanel.add(verticalStrut_8);

        JPanel panel_2 = new JPanel();
        parametersPanel.add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        endDateLabel = new JLabel("  End Date:");
        panel_2.add(endDateLabel);

        endSpinner = new SBMTDateSpinner();
        panel_2.add(endSpinner);

        Component horizontalGlue_9 = Box.createHorizontalGlue();
        panel_2.add(horizontalGlue_9);

        Component verticalStrut_7 = Box.createVerticalStrut(20);
        parametersPanel.add(verticalStrut_7);

        JPanel panel_3 = new JPanel();
        parametersPanel.add(panel_3);
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

        hasLimbLabel = new JLabel("Limb:");
        panel_3.add(hasLimbLabel);

        hasLimbComboBox = new JComboBox<String>();
        hasLimbComboBox.setMaximumSize(
                new java.awt.Dimension(hasLimbComboBox.getWidth(), 22));
        panel_3.add(hasLimbComboBox);

        Component horizontalGlue_6 = Box.createHorizontalGlue();
        panel_3.add(horizontalGlue_6);

        Component verticalStrut_6 = Box.createVerticalStrut(20);
        parametersPanel.add(verticalStrut_6);

        JPanel panel_4 = new JPanel();
        parametersPanel.add(panel_4);

        JLabel lblScDistanceFrom = new JLabel("S/C Distance from");
        panel_4.add(lblScDistanceFrom);

        fromDistanceTextField = new JFormattedTextField();
        fromDistanceTextField.setText("0");
        fromDistanceTextField.setMaximumSize(
                new Dimension(fromDistanceTextField.getWidth(), 20));
        fromDistanceTextField.setColumns(5);
        panel_4.add(fromDistanceTextField);

        panel_4.add(new JLabel("to"));

        toDistanceTextField = new JFormattedTextField();
        toDistanceTextField.setText("1000");
        toDistanceTextField.setMaximumSize(
                new Dimension(toDistanceTextField.getWidth(), 20));
        toDistanceTextField.setColumns(5);
        panel_4.add(toDistanceTextField);

        JLabel lblKm = new JLabel("km");
        panel_4.add(lblKm);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        Component horizontalGlue_1 = Box.createHorizontalGlue();
        panel_4.add(horizontalGlue_1);

        Component verticalStrut_5 = Box.createVerticalStrut(10);
        parametersPanel.add(verticalStrut_5);

        JPanel panel_5 = new JPanel();
        parametersPanel.add(panel_5);

        fromResolutionLabel = new JLabel("    Resolution from");
        panel_5.add(fromResolutionLabel);

        fromResolutionTextField = new JFormattedTextField();
        fromResolutionTextField.setText("0");
        fromResolutionTextField.setMaximumSize(
                new Dimension(fromResolutionTextField.getWidth(), 20));
        fromResolutionTextField.setColumns(5);
        panel_5.add(fromResolutionTextField);

        toResolutionLabel = new JLabel("to");
        panel_5.add(toResolutionLabel);

        toResolutionTextField = new JFormattedTextField();
        toResolutionTextField.setText("50");
        toResolutionTextField.setMaximumSize(
                new Dimension(toResolutionTextField.getWidth(), 20));
        toResolutionTextField.setColumns(5);
        panel_5.add(toResolutionTextField);

        endResolutionLabel = new JLabel("mpp");
        panel_5.add(endResolutionLabel);
        panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));

        Component horizontalGlue_2 = Box.createHorizontalGlue();
        panel_5.add(horizontalGlue_2);

        Component verticalStrut_4 = Box.createVerticalStrut(10);
        parametersPanel.add(verticalStrut_4);

        JPanel panel_6 = new JPanel();
        parametersPanel.add(panel_6);

        fromIncidenceLabel = new JLabel("      Incidence from");
        panel_6.add(fromIncidenceLabel);

        fromIncidenceTextField = new JFormattedTextField();
        fromIncidenceTextField.setText("0");
        fromIncidenceTextField.setMaximumSize(
                new Dimension(fromIncidenceTextField.getWidth(), 20));
        fromIncidenceTextField.setColumns(5);
        panel_6.add(fromIncidenceTextField);

        toIncidenceLabel = new JLabel("to");
        panel_6.add(toIncidenceLabel);

        toIncidenceTextField = new JFormattedTextField();
        toIncidenceTextField.setText("180");
        toIncidenceTextField.setMaximumSize(
                new Dimension(toIncidenceTextField.getWidth(), 20));
        toIncidenceTextField.setColumns(5);
        panel_6.add(toIncidenceTextField);

        panel_6.add(new JLabel("deg"));
        panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));

        Component horizontalGlue_3 = Box.createHorizontalGlue();
        panel_6.add(horizontalGlue_3);

        Component verticalStrut_3 = Box.createVerticalStrut(10);
        parametersPanel.add(verticalStrut_3);

        JPanel panel_7 = new JPanel();
        parametersPanel.add(panel_7);

        fromEmissionLabel = new JLabel("      Emission from");
        panel_7.add(fromEmissionLabel);

        fromEmissionTextField = new JFormattedTextField();
        fromEmissionTextField.setText("0");
        fromEmissionTextField.setMaximumSize(
                new Dimension(fromEmissionTextField.getWidth(), 20));
        fromEmissionTextField.setColumns(5);
        panel_7.add(fromEmissionTextField);

        toEmissionLabel = new JLabel("to");
        panel_7.add(toEmissionLabel);

        toEmissionTextField = new JFormattedTextField();
        toEmissionTextField.setText("180");
        toEmissionTextField.setMaximumSize(
                new Dimension(toEmissionTextField.getWidth(), 20));
        toEmissionTextField.setColumns(5);
        panel_7.add(toEmissionTextField);

        panel_7.add(new JLabel("deg"));
        panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.X_AXIS));

        Component horizontalGlue_4 = Box.createHorizontalGlue();
        panel_7.add(horizontalGlue_4);

        Component verticalStrut_2 = Box.createVerticalStrut(10);
        parametersPanel.add(verticalStrut_2);

        JPanel panel_8 = new JPanel();
        parametersPanel.add(panel_8);

        fromPhaseLabel = new JLabel("           Phase from");
        panel_8.add(fromPhaseLabel);

        fromPhaseTextField = new JFormattedTextField();
        fromPhaseTextField.setText("0");
        fromPhaseTextField.setMaximumSize(
                new Dimension(fromPhaseTextField.getWidth(), 20));
        fromPhaseTextField.setColumns(5);
        panel_8.add(fromPhaseTextField);

        toPhaseLabel = new JLabel("to");
        panel_8.add(toPhaseLabel);

        toPhaseTextField = new JFormattedTextField();
        toPhaseTextField.setText("180");
        toPhaseTextField
                .setMaximumSize(new Dimension(toPhaseTextField.getWidth(), 20));
        toPhaseTextField.setColumns(5);
        panel_8.add(toPhaseTextField);

        panel_8.add(new JLabel("deg"));
        panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.X_AXIS));

        Component horizontalGlue_5 = Box.createHorizontalGlue();
        panel_8.add(horizontalGlue_5);

        auxPanel = new JPanel();
        parametersPanel.add(auxPanel);
        auxPanel.setLayout(new BoxLayout(auxPanel, BoxLayout.Y_AXIS));

        Component verticalStrut = Box.createVerticalStrut(20);
        add(verticalStrut);

        JPanel panel_10 = new JPanel();
        add(panel_10);
        panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));

        selectRegionButton = new JToggleButton("Select Region");
        panel_10.add(selectRegionButton);

        clearRegionButton = new JButton("Clear Region");
        panel_10.add(clearRegionButton);

        submitButton = new JButton("Search");
        panel_10.add(submitButton);

        Component verticalStrut_1 = Box.createVerticalStrut(20);
        add(verticalStrut_1);



        // maybe these need to overridden later?
        // redComboBox.setVisible(false);
        // greenComboBox.setVisible(false);
        // blueComboBox.setVisible(false);
        //
        // ComboBoxModel redModel = getRedComboBoxModel();
        // ComboBoxModel greenModel = getGreenComboBoxModel();
        // ComboBoxModel blueModel = getBlueComboBoxModel();
        // if (redModel != null && greenModel != null && blueModel != null)
        // {
        // redComboBox.setModel(redModel);
        // greenComboBox.setModel(greenModel);
        // blueComboBox.setModel(blueModel);
        //
        // redComboBox.setVisible(true);
        // greenComboBox.setVisible(true);
        // blueComboBox.setVisible(true);
        //
        // redButton.setVisible(false);
        // greenButton.setVisible(false);
        // blueButton.setVisible(false);
        // }

    }

    public ImageSearchParametersPanel(LayoutManager layout)
    {
        super(layout);
        // TODO Auto-generated constructor stub
    }

    public ImageSearchParametersPanel(boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    public ImageSearchParametersPanel(LayoutManager layout,
            boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    protected List<List<String>> processResults(List<List<String>> input)
    {
        return input;
    }

    public CheckBoxTree getCheckBoxTree()
    {
        return checkBoxTree;
    }

    public void setCheckBoxTree(CheckBoxTree checkBoxTree)
    {
        this.checkBoxTree = checkBoxTree;
    }

    public JFormattedTextField getSearchByNumberTextField()
    {
        return searchByNumberTextField;
    }

    public JToggleButton getSelectRegionButton()
    {
        return selectRegionButton;
    }

    public JComboBox<PointingSource> getSourceComboBox()
    {
        return sourceComboBox;
    }

    public JLabel getSourceLabel()
    {
        return sourceLabel;
    }

    public JLabel getStartDateLabel()
    {
        return startDateLabel;
    }

    public SBMTDateSpinner getStartSpinner()
    {
        return startSpinner;
    }

    public JButton getSubmitButton()
    {
        return submitButton;
    }

    public JLabel getToDistanceLabel()
    {
        return toDistanceLabel;
    }

    public JFormattedTextField getToDistanceTextField()
    {
        return toDistanceTextField;
    }

    public JLabel getToEmissionLabel()
    {
        return toEmissionLabel;
    }

    public JFormattedTextField getToEmissionTextField()
    {
        return toEmissionTextField;
    }

    public JLabel getToIncidenceLabel()
    {
        return toIncidenceLabel;
    }

    public JFormattedTextField getToIncidenceTextField()
    {
        return toIncidenceTextField;
    }

    public JLabel getToPhaseLabel()
    {
        return toPhaseLabel;
    }

    public JFormattedTextField getToPhaseTextField()
    {
        return toPhaseTextField;
    }

    public JLabel getToResolutionLabel()
    {
        return toResolutionLabel;
    }

    public JFormattedTextField getToResolutionTextField()
    {
        return toResolutionTextField;
    }

    public JLabel getEndDateLabel()
    {
        return endDateLabel;
    }

    public JLabel getEndDistanceLabel()
    {
        return endDistanceLabel;
    }

    public JLabel getEndEmissionLabel()
    {
        return endEmissionLabel;
    }

    public JLabel getEndIncidenceLabel()
    {
        return endIncidenceLabel;
    }

    public JLabel getEndPhaseLabel()
    {
        return endPhaseLabel;
    }

    public JLabel getEndResolutionLabel()
    {
        return endResolutionLabel;
    }

    public SBMTDateSpinner getEndSpinner()
    {
        return endSpinner;
    }

    public JCheckBox getExcludeGaskellCheckBox()
    {
        return excludeGaskellCheckBox;
    }

    public JLabel getFromDistanceLabel()
    {
        return fromDistanceLabel;
    }

    public JFormattedTextField getFromDistanceTextField()
    {
        return fromDistanceTextField;
    }

    public JLabel getFromEmissionLabel()
    {
        return fromEmissionLabel;
    }

    public JFormattedTextField getFromEmissionTextField()
    {
        return fromEmissionTextField;
    }

    public JLabel getFromIncidenceLabel()
    {
        return fromIncidenceLabel;
    }

    public JFormattedTextField getFromIncidenceTextField()
    {
        return fromIncidenceTextField;
    }

    public JLabel getFromPhaseLabel()
    {
        return fromPhaseLabel;
    }

    public JFormattedTextField getFromPhaseTextField()
    {
        return fromPhaseTextField;
    }

    public JLabel getFromResolutionLabel()
    {
        return fromResolutionLabel;
    }

    public JFormattedTextField getFromResolutionTextField()
    {
        return fromResolutionTextField;
    }

    public JComboBox<String> getHasLimbComboBox()
    {
        return hasLimbComboBox;
    }

    public JLabel getHasLimbLabel()
    {
        return hasLimbLabel;
    }

    public JScrollPane getHierarchicalSearchScrollPane()
    {
        return hierarchicalSearchScrollPane;
    }

    public JButton getClearRegionButton()
    {
        return clearRegionButton;
    }

    public JPanel getAuxPanel()
    {
        return auxPanel;
    }

    public void setAuxPanel(JPanel auxPanel)
    {
        this.auxPanel = auxPanel;
    }

    public JRadioButton getFilenameRadioButton()
    {
        return filenameRadioButton;
    }

    public JRadioButton getParametersRadioButton()
    {
        return parametersRadioButton;
    }

	public void setFixedListSearch(boolean isFixedListSearch)
	{
		this.isFixedListSearch = isFixedListSearch;
		if (isFixedListSearch)
		{
			 parametersPanel.setVisible(false);
             filenamePanel.setVisible(true);
             selectRegionButton.setVisible(false);
             clearRegionButton.setVisible(false);
             parametersRadioButton.setVisible(false);
             filenameRadioButton.setVisible(false);
		}
		else
		{
			searchByNumberTextField.setText("");
            parametersPanel.setVisible(true);
            filenamePanel.setVisible(false);
            selectRegionButton.setVisible(true);
            clearRegionButton.setVisible(true);
            parametersRadioButton.setVisible(true);
            filenameRadioButton.setVisible(true);
		}
	}

}
