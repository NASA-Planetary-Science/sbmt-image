package edu.jhuapl.sbmt.image.controllers.search;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import com.jidesoft.swing.CheckBoxTree;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManager.PickMode;
import edu.jhuapl.sbmt.client.SmallBodyViewConfig;
import edu.jhuapl.sbmt.image.core.listeners.ImageSearchModelListener;
import edu.jhuapl.sbmt.image.gui.search.ImageSearchParametersPanel;
import edu.jhuapl.sbmt.image.types.ImageSearchModel;
import edu.jhuapl.sbmt.model.image.ImageSource;

public class ImageSearchParametersController
{

    protected ImageSearchParametersPanel panel;
    protected ImageSearchModel model;
    private PickManager pickManager;
    private JPanel auxPanel;
    protected SmallBodyViewConfig smallBodyConfig;
    private boolean isFixedListSearch = false;

    public ImageSearchParametersController(ImageSearchModel model, PickManager pickManager)
    {
        this.model = model;
        this.panel = new ImageSearchParametersPanel();
        this.pickManager = pickManager;

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
        smallBodyConfig = model.getSmallBodyConfig();
        boolean showSourceLabelAndComboBox = true; //imageSources.length > 1 ? true : false;
        panel.getSourceLabel().setVisible(showSourceLabelAndComboBox);
        panel.getSourceComboBox().setVisible(showSourceLabelAndComboBox);

        ImageSource imageSources[] = model.getInstrument().getSearchImageSources();
        panel.getSourceComboBox().setModel(new DefaultComboBoxModel(imageSources));

        panel.getSourceComboBox().addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sourceComboBoxItemStateChanged(evt);
            }
        });

        JSpinner startSpinner = panel.getStartSpinner();
        startSpinner.setModel(new javax.swing.SpinnerDateModel(smallBodyConfig.imageSearchDefaultStartDate, null, null, java.util.Calendar.DAY_OF_MONTH));
        startSpinner.setEditor(new javax.swing.JSpinner.DateEditor(startSpinner, "yyyy-MMM-dd HH:mm:ss"));
        startSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        startSpinner.setPreferredSize(new java.awt.Dimension(180, 22));
        startSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                startSpinnerStateChanged(evt);
            }
        });


        panel.getEndDateLabel().setText("End Date:");
        JSpinner endSpinner = panel.getEndSpinner();
        endSpinner.setModel(new javax.swing.SpinnerDateModel(smallBodyConfig.imageSearchDefaultEndDate, null, null, java.util.Calendar.DAY_OF_MONTH));
        endSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endSpinner, "yyyy-MMM-dd HH:mm:ss"));
        endSpinner.setMinimumSize(new java.awt.Dimension(36, 22));
        endSpinner.setPreferredSize(new java.awt.Dimension(180, 22));
        endSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                endSpinnerStateChanged(evt);
            }
        });

        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentHidden(java.awt.event.ComponentEvent evt) {
                formComponentHidden(evt);
            }
        });

        panel.getSourceComboBox().addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sourceComboBoxItemStateChanged(evt);
            }
        });
        panel.getHasLimbComboBox().setModel(new javax.swing.DefaultComboBoxModel(new String[] { "with or without", "with only", "without only" }));

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
        toPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toPhaseTextField.setText("180");
        toPhaseTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField fromPhaseTextField = panel.getFromPhaseTextField();
        fromPhaseTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromPhaseTextField.setText("0");
        fromPhaseTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField toEmissionTextField = panel.getToEmissionTextField();
        toEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toEmissionTextField.setText("180");
        toEmissionTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField fromEmissionTextField = panel.getFromEmissionTextField();
        fromEmissionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromEmissionTextField.setText("0");
        fromEmissionTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField toIncidenceTextField = panel.getToIncidenceTextField();
        toIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toIncidenceTextField.setText("180");
        toIncidenceTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField fromIncidenceTextField = panel.getFromIncidenceTextField();
        fromIncidenceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromIncidenceTextField.setText("0");
        fromIncidenceTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        panel.getEndResolutionLabel().setText("mpp");
        panel.getEndResolutionLabel().setToolTipText("meters per pixel");

        JFormattedTextField toResolutionTextField = panel.getToResolutionTextField();
        toResolutionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toResolutionTextField.setText("50");
        toResolutionTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField fromResolutionTextField = panel.getFromResolutionTextField();
        fromResolutionTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromResolutionTextField.setText("0");
        fromResolutionTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField toDistanceTextField = panel.getToDistanceTextField();
        toDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        toDistanceTextField.setText("1000");
        toDistanceTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        JFormattedTextField fromDistanceTextField = panel.getFromDistanceTextField();
        fromDistanceTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.###"))));
        fromDistanceTextField.setText("0");
        fromDistanceTextField.setPreferredSize(new java.awt.Dimension(0, 22));

        panel.getClearRegionButton().setText("Clear Region");
        panel.getClearRegionButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearRegionButtonActionPerformed(evt);
            }
        });


        panel.getSubmitButton().setText("Search");
        panel.getSubmitButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                submitButtonActionPerformed(evt);
                pushInputToModel();
                panel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                panel.getSelectRegionButton().setSelected(false);
                pickManager.setPickMode(PickMode.DEFAULT);
                model.performSearch();
                panel.setCursor(Cursor.getDefaultCursor());
            }
        });



        panel.getSelectRegionButton().setText("Select Region");
        panel.getSelectRegionButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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

        toDistanceTextField.setValue(smallBodyConfig.imageSearchDefaultMaxSpacecraftDistance);
        toResolutionTextField.setValue(smallBodyConfig.imageSearchDefaultMaxResolution);

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
            CheckBoxTree checkBoxTree = new CheckBoxTree(smallBodyConfig.hierarchicalImageSearchSpecification.getTreeModel());

            // Connect tree to panel.
            panel.setCheckBoxTree(checkBoxTree);

            // Bind the checkbox-specific tree selection model to the "spec"
            smallBodyConfig.hierarchicalImageSearchSpecification.setSelectionModel(checkBoxTree.getCheckBoxTreeSelectionModel());

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

//    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_submitButtonActionPerformed
//    {
//        try
//        {
//            panel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
//            panel.getSelectRegionButton().setSelected(false);
//            pickManager.setPickMode(PickMode.DEFAULT);
//
//            String searchField = null;
//            if (panel.getFilenameRadioButton().isSelected())
//                searchField = panel.getSearchByNumberTextField().getText().trim();
//
//            GregorianCalendar startDateGreg = new GregorianCalendar();
//            GregorianCalendar endDateGreg = new GregorianCalendar();
//            startDateGreg.setTime(model.getStartDate());
//            endDateGreg.setTime(model.getEndDate());
//            DateTime startDateJoda = new DateTime(
//                    startDateGreg.get(GregorianCalendar.YEAR),
//                    startDateGreg.get(GregorianCalendar.MONTH)+1,
//                    startDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
//                    startDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
//                    startDateGreg.get(GregorianCalendar.MINUTE),
//                    startDateGreg.get(GregorianCalendar.SECOND),
//                    startDateGreg.get(GregorianCalendar.MILLISECOND),
//                    DateTimeZone.UTC);
//            DateTime endDateJoda = new DateTime(
//                    endDateGreg.get(GregorianCalendar.YEAR),
//                    endDateGreg.get(GregorianCalendar.MONTH)+1,
//                    endDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
//                    endDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
//                    endDateGreg.get(GregorianCalendar.MINUTE),
//                    endDateGreg.get(GregorianCalendar.SECOND),
//                    endDateGreg.get(GregorianCalendar.MILLISECOND),
//                    DateTimeZone.UTC);
//
//            TreeSet<Integer> cubeList = null;
//            AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)model.getModelManager().getModel(ModelNames.CIRCLE_SELECTION);
//            SmallBodyModel smallBodyModel = (SmallBodyModel)model.getModelManager().getModel(ModelNames.SMALL_BODY);
//            if (selectionModel.getNumberOfStructures() > 0)
//            {
//                AbstractEllipsePolygonModel.EllipsePolygon region = (AbstractEllipsePolygonModel.EllipsePolygon)selectionModel.getStructure(0);
//
//                // Always use the lowest resolution model for getting the intersection cubes list.
//                // Therefore, if the selection region was created using a higher resolution model,
//                // we need to recompute the selection region using the low res model.
//                if (smallBodyModel.getModelResolution() > 0)
//                {
//                    vtkPolyData interiorPoly = new vtkPolyData();
//                    smallBodyModel.drawRegularPolygonLowRes(region.center, region.radius, region.numberOfSides, interiorPoly, null);
//                    cubeList = smallBodyModel.getIntersectingCubes(interiorPoly);
//                }
//                else
//                {
//                    cubeList = smallBodyModel.getIntersectingCubes(region.interiorPolyData);
//                }
//            }
//
//            ImageSource imageSource = ImageSource.valueOf(((Enum)panel.getSourceComboBox().getSelectedItem()).name());
//
//            // Populate camera and filter list differently based on if we are doing sum-of-products or product-of-sums search
//            boolean sumOfProductsSearch;
////            List<Integer> camerasSelected;
////            List<Integer> filtersSelected;
//            SmallBodyViewConfig smallBodyConfig = model.getSmallBodyConfig();
//            if(smallBodyConfig.hasHierarchicalImageSearch)
//            {
//                // Sum of products (hierarchical) search: (CAMERA 1 AND FILTER 1) OR ... OR (CAMERA N AND FILTER N)
//                sumOfProductsSearch = true;
//
//                // Process the user's selections
////                smallBodyConfig.hierarchicalImageSearchSpecification.processTreeSelections(
////                        panel.getCheckBoxTree().getCheckBoxTreeSelectionModel().getSelectionPaths());
////
////                // Get the selected (camera,filter) pairs
////                camerasSelected = smallBodyConfig.hierarchicalImageSearchSpecification.getSelectedCameras();
////                filtersSelected = smallBodyConfig.hierarchicalImageSearchSpecification.getSelectedFilters();
//
//                Selection selection = smallBodyConfig.hierarchicalImageSearchSpecification.processTreeSelections();
//                camerasSelected = selection.getSelectedCameras();
//                System.out.println(
//                        "ImageSearchParametersController: submitButtonActionPerformed: cameras selected size " + camerasSelected.size());
//                filtersSelected = selection.getSelectedFilters();
//            }
//            else
//            {
//                // Product of sums (legacy) search: (CAMERA 1 OR ... OR CAMERA N) AND (FILTER 1 OR ... FILTER M)
//                sumOfProductsSearch = false;
//
//                // Populate list of selected cameras
////                camerasSelected = new LinkedList<Integer>();
////                int numberOfCameras = model.getNumberOfUserDefinedCheckBoxesActuallyUsed();
//                for (int i=0; i<numberOfCameras; i++)
//                {
//                    if(panel.getUserDefinedCheckBoxes()[i].isSelected())
//                    {
//                        camerasSelected.add(i);
//                    }
//                }
//
//                // Populate list of selected filters
//                filtersSelected = new LinkedList<Integer>();
//                int numberOfFilters = model.getNumberOfFiltersActuallyUsed();
//                for (int i=0; i<numberOfFilters; i++)
//                {
//                    if(panel.getFilterCheckBoxes()[i].isSelected())
//                    {
//                        filtersSelected.add(i);
//                    }
//                }
//            }
//            List<List<String>> results = null;
//            if (model.getInstrument().searchQuery instanceof FixedListQuery)
//            {
//                FixedListQuery query = (FixedListQuery) model.getInstrument().searchQuery;
//                results = query.runQuery(FixedListSearchMetadata.of("Imaging Search", "imagelist", "images", query.getRootPath(), imageSource)).getResultlist();
//            }
//            else
//            {
//                // Run queries based on user specifications
//                ImageDatabaseSearchMetadata searchMetadata = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
//                        Ranges.closed(Double.valueOf(panel.getFromDistanceTextField().getText()), Double.valueOf(panel.getToDistanceTextField().getText())),
//                        searchField, null,
//                        Ranges.closed(Double.valueOf(panel.getFromIncidenceTextField().getText()), Double.valueOf(panel.getToIncidenceTextField().getText())),
//                        Ranges.closed(Double.valueOf(panel.getFromEmissionTextField().getText()), Double.valueOf(panel.getToEmissionTextField().getText())),
//                        Ranges.closed(Double.valueOf(panel.getFromPhaseTextField().getText()), Double.valueOf(panel.getToPhaseTextField().getText())),
//                        sumOfProductsSearch, camerasSelected, filtersSelected,
//                        Ranges.closed(Double.valueOf(panel.getFromResolutionTextField().getText()), Double.valueOf(panel.getToResolutionTextField().getText())),
//                        cubeList, imageSource, panel.getHasLimbComboBox().getSelectedIndex());
//                results = model.getInstrument().searchQuery.runQuery(searchMetadata).getResultlist();
//           }
//
//            // If SPICE Derived (exclude Gaskell) or Gaskell Derived (exlude SPICE) is selected,
//            // then remove from the list images which are contained in the other list by doing
//            // an additional search.
//            if (imageSource == ImageSource.SPICE && panel.getExcludeGaskellCheckBox().isSelected())
//            {
//                List<List<String>> resultsOtherSource = null;
//                if (model.getInstrument().searchQuery instanceof FixedListQuery)
//                {
//                    FixedListQuery query = (FixedListQuery)model.getInstrument().searchQuery;
////                    FileInfo info = FileCache.getFileInfoFromServer(query.getRootPath() + "/" /*+ dataListPrefix + "/"*/ + imageListName);
////                    if (!info.isExistsOnServer().equals(YesOrNo.YES))
////                    {
////                        System.out.println("Could not find " + imageListName + ". Using imagelist.txt instead");
////                        imageListName = "imagelist.txt";
////                    }
//                    resultsOtherSource = query.runQuery(FixedListSearchMetadata.of("Imaging Search", "imagelist" /*imageListName*/, "images", query.getRootPath(), imageSource)).getResultlist();
//                }
//                else
//                {
//
//                    ImageDatabaseSearchMetadata searchMetadataOther = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
//                            Ranges.closed(Double.valueOf(panel.getFromDistanceTextField().getText()), Double.valueOf(panel.getToDistanceTextField().getText())),
//                            searchField, null,
//                            Ranges.closed(Double.valueOf(panel.getFromIncidenceTextField().getText()), Double.valueOf(panel.getToIncidenceTextField().getText())),
//                            Ranges.closed(Double.valueOf(panel.getFromEmissionTextField().getText()), Double.valueOf(panel.getToEmissionTextField().getText())),
//                            Ranges.closed(Double.valueOf(panel.getFromPhaseTextField().getText()), Double.valueOf(panel.getToPhaseTextField().getText())),
//                            sumOfProductsSearch, camerasSelected, filtersSelected,
//                            Ranges.closed(Double.valueOf(panel.getFromResolutionTextField().getText()), Double.valueOf(panel.getToResolutionTextField().getText())),
//                            cubeList, imageSource == ImageSource.SPICE ? ImageSource.GASKELL_UPDATED : ImageSource.SPICE, panel.getHasLimbComboBox().getSelectedIndex());
//
//                        resultsOtherSource = model.getInstrument().searchQuery.runQuery(searchMetadataOther).getResultlist();
//
//                }
//
//                int numOtherResults = resultsOtherSource.size();
//                for (int i=0; i<numOtherResults; ++i)
//                {
//                    String imageName = resultsOtherSource.get(i).get(0);
//                    int numResults = results.size();
//                    for (int j=0; j<numResults; ++j)
//                    {
//                        if (results.get(j).get(0).startsWith(imageName))
//                        {
//                            results.remove(j);
//                            break;
//                        }
//                    }
//                }
//            }
//            model.setImageSourceOfLastQuery(imageSource);
//
//            model.setImageResults(model.processResults(results));
//            panel.setCursor(Cursor.getDefaultCursor());
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }


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
        model.setImageSourceOfLastQuery((ImageSource)panel.getSourceComboBox().getSelectedItem());

        model.setSearchByFilename(panel.getFilenameRadioButton().isSelected());

        if (!panel.getSearchByNumberTextField().getText().trim().equals(""))
            model.setSearchFilename(panel.getSearchByNumberTextField().getText().trim());
        else
            model.setSearchFilename(null);

        model.setStartDate((Date)panel.getStartSpinner().getValue());
        model.setEndDate((Date)panel.getEndSpinner().getValue());
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

    private void formComponentHidden(java.awt.event.ComponentEvent evt)
    {
        panel.getSelectRegionButton().setSelected(false);
        pickManager.setPickMode(PickMode.DEFAULT);
    }

    private void startSpinnerStateChanged(javax.swing.event.ChangeEvent evt)
    {
        Date date =
                ((SpinnerDateModel)panel.getStartSpinner().getModel()).getDate();
        if (date != null)
            model.setStartDate(date);
    }

    private void endSpinnerStateChanged(javax.swing.event.ChangeEvent evt)
    {
        Date date =
                ((SpinnerDateModel)panel.getEndSpinner().getModel()).getDate();
        if (date != null)
            model.setEndDate(date);

    }

    private void sourceComboBoxItemStateChanged(ItemEvent evt)
    {
        JComboBox sourceComboBox = panel.getSourceComboBox();
        ImageSource imageSource = ImageSource.valueOf(((Enum)sourceComboBox.getSelectedItem()).name());
        for (int i=0; i< sourceComboBox.getModel().getSize(); i++)
        {
            ImageSource source = ImageSource.valueOf(((Enum)sourceComboBox.getItemAt(i)).name());
            if (source == ImageSource.GASKELL_UPDATED)
            {
                panel.getExcludeGaskellCheckBox().setVisible(imageSource == ImageSource.SPICE);
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
        AbstractEllipsePolygonModel selectionModel = (AbstractEllipsePolygonModel)model.getModelManager().getModel(ModelNames.CIRCLE_SELECTION).get(0);
        selectionModel.removeAllStructures();
    }


    public ImageSearchParametersPanel getPanel()
    {
        return panel;
    }

    public void setPanel(ImageSearchParametersPanel panel)
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
