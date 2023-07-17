package edu.jhuapl.sbmt.image.controllers;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import com.jidesoft.swing.CheckBoxTree;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.image.config.ImagingInstrumentConfig;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.interfaces.ImageSearchModelListener;
import edu.jhuapl.sbmt.image.model.ImageSearchParametersModel;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.pipelineComponents.pipelines.search.ImageSearchPipeline;
import edu.jhuapl.sbmt.image.ui.SBMTDateSpinner;
import edu.jhuapl.sbmt.image.ui.search.ImageSearchParametersPanel;
import edu.jhuapl.sbmt.image.ui.search.SpectralImageSearchParametersPanel;

public class ImageSearchParametersController<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable>
{
	protected SpectralImageSearchParametersPanel panel;
    protected ImageSearchParametersModel model;
    private PickManager pickManager;
    private JPanel auxPanel;
    protected ImagingInstrumentConfig config;
    private boolean isFixedListSearch = false;
    private ModelManager modelManager;
    private PerspectiveImageCollection<G1> collection;

    public ImageSearchParametersController(ImagingInstrumentConfig config, PerspectiveImageCollection<G1> collection,  ImageSearchParametersModel model, ModelManager modelManager, PickManager pickManager)
    {
        this.model = model;
        this.panel = new SpectralImageSearchParametersPanel();
        this.pickManager = pickManager;
        this.modelManager = modelManager;
        this.config = config;
        this.collection = collection;
        model.addModelChangedListener(new ImageSearchModelListener()
        {
            @Override
            public void modelUpdated()
            {
                pullFromModel();
            }
        });
    }


    public void setupSearchParametersPanel()
    {
        boolean showSourceLabelAndComboBox = true; //imageSources.length > 1 ? true : false;
        panel.getSourceLabel().setVisible(showSourceLabelAndComboBox);
        panel.getSourceComboBox().setVisible(showSourceLabelAndComboBox);

        PointingSource imageSources[] = model.getInstrument().getSearchImageSources();
        panel.getSourceComboBox().setModel(new DefaultComboBoxModel(imageSources));

        panel.getSourceComboBox().addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                sourceComboBoxItemStateChanged(evt);
            }
        });

        SBMTDateSpinner startSpinner = panel.getStartSpinner();
        startSpinner.setDate(config.imageSearchDefaultStartDate);
        startSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                startSpinnerStateChanged(evt);
            }
        });


        panel.getEndDateLabel().setText("End Date:");

        SBMTDateSpinner endSpinner = panel.getEndSpinner();
        endSpinner.setDate(config.imageSearchDefaultEndDate);
        endSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                endSpinnerStateChanged(evt);
            }
        });

        panel.addComponentListener(new ComponentAdapter() {
            public void componentHidden(ComponentEvent evt) {
                formComponentHidden(evt);
            }
        });

        panel.getSourceComboBox().addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                sourceComboBoxItemStateChanged(evt);
            }
        });
        panel.getHasLimbComboBox().setModel(new DefaultComboBoxModel(new String[] { "with or without", "with only", "without only" }));

        panel.getHasLimbComboBox().addItemListener(new ItemListener()
        {

            @Override
            public void itemStateChanged(ItemEvent e)
            {
                model.setSelectedLimbIndex(panel.getHasLimbComboBox().getSelectedIndex());
                model.setSelectedLimbString((String)panel.getHasLimbComboBox().getSelectedItem());
            }
        });

        panel.getFilenameRadioButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (panel.getFilenameRadioButton().isSelected())
                    model.setSearchByFilename(true);
                else
                    model.setSearchByFilename(false);
            }
        });

        //May not need this with the above
        panel.getParametersRadioButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (panel.getParametersRadioButton().isSelected())
                    model.setSearchByFilename(false);
                else
                    model.setSearchByFilename(true);
            }
        });

        JFormattedTextField toPhaseTextField = panel.getToPhaseTextField();
        toPhaseTextField.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0.###"))));
        toPhaseTextField.setText("180");
        toPhaseTextField.setPreferredSize(new Dimension(0, 22));

        JFormattedTextField fromPhaseTextField = panel.getFromPhaseTextField();
        fromPhaseTextField.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0.###"))));
        fromPhaseTextField.setText("0");
        fromPhaseTextField.setPreferredSize(new Dimension(0, 22));

        JFormattedTextField toEmissionTextField = panel.getToEmissionTextField();
        toEmissionTextField.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0.###"))));
        toEmissionTextField.setText("180");
        toEmissionTextField.setPreferredSize(new Dimension(0, 22));

        JFormattedTextField fromEmissionTextField = panel.getFromEmissionTextField();
        fromEmissionTextField.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0.###"))));
        fromEmissionTextField.setText("0");
        fromEmissionTextField.setPreferredSize(new Dimension(0, 22));

        JFormattedTextField toIncidenceTextField = panel.getToIncidenceTextField();
        toIncidenceTextField.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0.###"))));
        toIncidenceTextField.setText("180");
        toIncidenceTextField.setPreferredSize(new Dimension(0, 22));

        JFormattedTextField fromIncidenceTextField = panel.getFromIncidenceTextField();
        fromIncidenceTextField.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0.###"))));
        fromIncidenceTextField.setText("0");
        fromIncidenceTextField.setPreferredSize(new Dimension(0, 22));

        panel.getEndResolutionLabel().setText("mpp");
        panel.getEndResolutionLabel().setToolTipText("meters per pixel");

        JFormattedTextField toResolutionTextField = panel.getToResolutionTextField();
        toResolutionTextField.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0.###"))));
        toResolutionTextField.setText("50");
        toResolutionTextField.setPreferredSize(new Dimension(0, 22));

        JFormattedTextField fromResolutionTextField = panel.getFromResolutionTextField();
        fromResolutionTextField.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0.###"))));
        fromResolutionTextField.setText("0");
        fromResolutionTextField.setPreferredSize(new Dimension(0, 22));

        JFormattedTextField toDistanceTextField = panel.getToDistanceTextField();
        toDistanceTextField.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0.###"))));
        toDistanceTextField.setText("1000");
        toDistanceTextField.setPreferredSize(new Dimension(0, 22));

        JFormattedTextField fromDistanceTextField = panel.getFromDistanceTextField();
        fromDistanceTextField.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0.###"))));
        fromDistanceTextField.setText("0");
        fromDistanceTextField.setPreferredSize(new Dimension(0, 22));

        panel.getClearRegionButton().setText("Clear Region");
        panel.getClearRegionButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                clearRegionButtonActionPerformed(evt);
            }
        });


        panel.getSubmitButton().setText("Search");
        panel.getSubmitButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                pushInputToModel();
                panel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                panel.getSelectRegionButton().setSelected(false);
                pickManager.setPickMode(PickMode.DEFAULT);
                ImageSearchPipeline<G1> pipeline = null;
                collection.clearSearchedImages();
				try
				{
					pipeline = new ImageSearchPipeline<G1>(config, modelManager, model);
				} catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (pipeline == null) return;
                List<G1> images = pipeline.getImages();
                collection.setImages(images);
                panel.setCursor(Cursor.getDefaultCursor());
                collection.updateActiveBoundaries(null);
            }
        });

        panel.getSelectRegionButton().setText("Select Region");
        panel.getSelectRegionButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                selectRegionButtonActionPerformed(evt);
            }
        });

        panel.getExcludeGaskellCheckBox().addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                model.setExcludeGaskell(panel.getExcludeGaskellCheckBox().isSelected());
            }
        });

        pushInputToModel();

        toDistanceTextField.setValue(config.imageSearchDefaultMaxSpacecraftDistance);
        toResolutionTextField.setValue(config.imageSearchDefaultMaxResolution);

        initHierarchicalImageSearch();
    }

    // Sets up everything related to hierarchical image searches
    protected void initHierarchicalImageSearch()
    {
        // Show/hide panels depending on whether this body has hierarchical image search capabilities
        if(model.getSmallBodyConfig().hasHierarchicalImageSearch)
        {
            // Has hierarchical search capabilities, these replace the camera and filter checkboxes so hide them
//            panel.getFilterCheckBoxPanel().setVisible(false);
//            panel.getUserDefinedCheckBoxPanel().setVisible(false);
            panel.getAuxPanel().setVisible(false);

            // Create the tree
            CheckBoxTree checkBoxTree = new CheckBoxTree(config.hierarchicalImageSearchSpecification.getTreeModel());

            // Connect tree to panel.
            panel.setCheckBoxTree(checkBoxTree);

            // Bind the checkbox-specific tree selection model to the "spec"
            config.hierarchicalImageSearchSpecification.setSelectionModel(checkBoxTree.getCheckBoxTreeSelectionModel());

            // Place the tree in the panel
            panel.getHierarchicalSearchScrollPane().setViewportView(panel.getCheckBoxTree());
        }
        else
        {
            // No hierarchical search capabilities, hide the scroll pane
            if (panel.getHierarchicalSearchScrollPane() != null)
                panel.getHierarchicalSearchScrollPane().setVisible(false);
        }
    }

    protected void pullFromModel()
    {
        panel.getSourceComboBox().setSelectedItem(ImageSearchParametersController.this.model.getImageSourceOfLastQuery());

        if (model.isSearchByFilename() == true)
            panel.getFilenameRadioButton().setSelected(true);
        else
            panel.getParametersRadioButton().setSelected(true);

        if (model.getSearchFilename() != null)
            panel.getSearchByNumberTextField().setText(model.getSearchFilename());

        panel.getStartSpinner().setValue(model.getStartDate());
        panel.getEndSpinner().setValue(model.getEndDate());

        panel.getHasLimbComboBox().setSelectedItem(model.getSelectedLimbString());

        panel.getFromDistanceTextField().setText(""+model.getMinDistanceQuery());
        panel.getToDistanceTextField().setText(""+model.getMaxDistanceQuery());
        panel.getFromIncidenceTextField().setText(""+model.getMinIncidenceQuery());
        panel.getToIncidenceTextField().setText(""+model.getMaxIncidenceQuery());
        panel.getFromEmissionTextField().setText(""+model.getMinEmissionQuery());
        panel.getToEmissionTextField().setText(""+model.getMaxEmissionQuery());
        panel.getFromPhaseTextField().setText(""+model.getMinPhaseQuery());
        panel.getToPhaseTextField().setText(""+model.getMaxPhaseQuery());
        panel.getFromResolutionTextField().setText(""+model.getMinResolutionQuery());
        panel.getToResolutionTextField().setText(""+model.getMaxResolutionQuery());
    }

    protected void pushInputToModel()
    {
        model.setImageSourceOfLastQuery((PointingSource)panel.getSourceComboBox().getSelectedItem());

        model.setSearchByFilename(panel.getFilenameRadioButton().isSelected());

        if (!panel.getSearchByNumberTextField().getText().trim().equals(""))
            model.setSearchFilename(panel.getSearchByNumberTextField().getText().trim());
        else
            model.setSearchFilename(null);

        model.setStartDate(panel.getStartSpinner().getDate());
        model.setEndDate(panel.getEndSpinner().getDate());
        model.setMinDistanceQuery(Double.parseDouble(panel.getFromDistanceTextField().getText()));
        model.setMaxDistanceQuery(Double.parseDouble(panel.getToDistanceTextField().getText()));
        model.setMinIncidenceQuery(Double.parseDouble(panel.getFromIncidenceTextField().getText()));
        model.setMaxIncidenceQuery(Double.parseDouble(panel.getToIncidenceTextField().getText()));
        model.setMinEmissionQuery(Double.parseDouble(panel.getFromEmissionTextField().getText()));
        model.setMaxEmissionQuery(Double.parseDouble(panel.getToEmissionTextField().getText()));
        model.setMinPhaseQuery(Double.parseDouble(panel.getFromPhaseTextField().getText()));
        model.setMaxPhaseQuery(Double.parseDouble(panel.getToPhaseTextField().getText()));
        model.setMinResolutionQuery(Double.parseDouble(panel.getFromResolutionTextField().getText()));
        model.setMaxResolutionQuery(Double.parseDouble(panel.getToResolutionTextField().getText()));
    }

    private void formComponentHidden(ComponentEvent evt)
    {
        panel.getSelectRegionButton().setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);
    }

    private void startSpinnerStateChanged(ChangeEvent evt)
    {
        Date date =
                ((SpinnerDateModel)panel.getStartSpinner().getModel()).getDate();
        if (date != null)
            model.setStartDate(date);
    }

    private void endSpinnerStateChanged(ChangeEvent evt)
    {
        Date date =
                ((SpinnerDateModel)panel.getEndSpinner().getModel()).getDate();
        if (date != null)
            model.setEndDate(date);
    }

    private void sourceComboBoxItemStateChanged(ItemEvent evt)
    {
        JComboBox<PointingSource> sourceComboBox = panel.getSourceComboBox();
        PointingSource imageSource = (PointingSource)(sourceComboBox.getSelectedItem());
        for (int i=0; i< sourceComboBox.getModel().getSize(); i++)
        {
        	PointingSource itemAt = sourceComboBox.getItemAt(i);

            if (itemAt == PointingSource.GASKELL_UPDATED)
            {
                panel.getExcludeGaskellCheckBox().setVisible(imageSource == PointingSource.SPICE);
                model.setExcludeGaskellEnabled(panel.getExcludeGaskellCheckBox().isVisible());
            }
        }
    }

    private void selectRegionButtonActionPerformed(ActionEvent evt)
    {
        if (panel.getSelectRegionButton().isSelected())
            pickManager.setPickMode(PickMode.CIRCLE_SELECTION);
        else
            pickManager.setPickMode(PickMode.DEFAULT);
    }

    private void clearRegionButtonActionPerformed(ActionEvent evt)
    {
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);
        selectionModel.removeAllStructures();
    }

    public ImageSearchParametersPanel getPanel()
    {
        return panel;
    }

    public void setPanel(SpectralImageSearchParametersPanel panel)
    {
        this.panel = panel;
    }

    public JPanel getAuxPanel()
    {
        return auxPanel;
    }

    public void setAuxPanel(JPanel auxPanel)
    {
        this.auxPanel = auxPanel;
        panel.setAuxPanel(auxPanel);
    }

	public boolean isFixedListSearch()
	{
		return isFixedListSearch;
	}

	public void setFixedListSearch(boolean isFixedListSearch)
	{
		this.isFixedListSearch = isFixedListSearch;
		panel.setFixedListSearch(isFixedListSearch);
	}
}