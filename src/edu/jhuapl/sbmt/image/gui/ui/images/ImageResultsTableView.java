package edu.jhuapl.sbmt.image.gui.ui.images;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.ImageCollection;

public class ImageResultsTableView extends JPanel
{
    private JButton loadImageListButton;
    private JPanel monochromePanel;
    private JButton nextButton;
    private JComboBox<Integer> numberOfBoundariesComboBox;
    private JButton prevButton;
    private JButton removeAllButton;
    private JButton removeAllImagesButton;
    private JButton saveImageListButton;
    private JButton saveSelectedImageListButton;
    private JButton viewResultsGalleryButton;
    private ImagePopupMenu imagePopupMenu;
    String[] columnNames;
    protected ImageResultsTable resultList;
    private JLabel resultsLabel;
    private JLabel lblNumberBoundaries;
    protected JPanel buttonPanel3;
    private final boolean enableGallery;

    /**
     * @wbp.parser.constructor
     */
    public ImageResultsTableView(ImagingInstrument instrument, ImageCollection imageCollection, ImagePopupMenu imagePopupMenu)
    {
        super();

        this.imagePopupMenu = imagePopupMenu;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.enableGallery = instrument != null && instrument.getSearchQuery().getGalleryPath() != null;

        init();
    }

    protected void init()
    {
        resultsLabel = new JLabel("0 Results");
        resultList = new ImageResultsTable();
        lblNumberBoundaries = new JLabel("Number of Boundaries:");
        numberOfBoundariesComboBox = new JComboBox<Integer>();
        prevButton = new JButton("Prev");
        nextButton = new JButton("Next");
        removeAllImagesButton = new JButton("Remove All Images");
        removeAllButton = new JButton("Remove All Boundaries");
        loadImageListButton = new JButton("Load...");
        saveImageListButton = new JButton("Save...");
        saveSelectedImageListButton = new JButton("Save Selected...");
        viewResultsGalleryButton = new JButton("View Results as Image Gallery");
    }

    public void setup()
    {
        resultList.setAutoCreateRowSorter(true);
//    	resultList.setDragEnabled(true);

        JPanel panel_4 = new JPanel();
        add(panel_4);
        panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

        panel_4.add(resultsLabel);

        Component horizontalGlue = Box.createHorizontalGlue();
        panel_4.add(horizontalGlue);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new java.awt.Dimension(300, 300));
        add(scrollPane);

        scrollPane.setViewportView(resultList);

        JPanel panel = new JPanel();
        add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        panel.add(lblNumberBoundaries);

        panel.add(numberOfBoundariesComboBox);

        panel.add(prevButton);

        panel.add(nextButton);

        JPanel panel_1 = new JPanel();
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        panel_1.add(removeAllImagesButton);

        panel_1.add(removeAllButton);

        JPanel panel_2 = new JPanel();
        add(panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        panel_2.add(loadImageListButton);

        panel_2.add(saveImageListButton);

        panel_2.add(saveSelectedImageListButton);

        buttonPanel3 = new JPanel();
        add(buttonPanel3);

        if (enableGallery)
        {
            buttonPanel3.add(viewResultsGalleryButton);
        }
    }

    public JTable getResultList()
    {
        return resultList;
    }

    public JLabel getResultsLabel()
    {
        return resultsLabel;
    }

    public JComboBox<Integer> getNumberOfBoundariesComboBox()
    {
        return numberOfBoundariesComboBox;
    }

    public JButton getLoadImageListButton()
    {
        return loadImageListButton;
    }

    public JPanel getMonochromePanel()
    {
        return monochromePanel;
    }

    public JButton getNextButton()
    {
        return nextButton;
    }

    public JButton getPrevButton()
    {
        return prevButton;
    }

    public JButton getRemoveAllButton()
    {
        return removeAllButton;
    }

    public JButton getRemoveAllImagesButton()
    {
        return removeAllImagesButton;
    }

    public JButton getSaveImageListButton()
    {
        return saveImageListButton;
    }

    public JButton getSaveSelectedImageListButton()
    {
        return saveSelectedImageListButton;
    }

    public JButton getViewResultsGalleryButton()
    {
        return viewResultsGalleryButton;
    }

    public ImagePopupMenu getImagePopupMenu()
    {
        return imagePopupMenu;
    }

    public void setNumberOfBoundariesComboBox(JComboBox<Integer> numberOfBoundariesComboBox)
    {
        this.numberOfBoundariesComboBox = numberOfBoundariesComboBox;
    }

    public void setResultsLabel(JLabel resultsLabel)
    {
        this.resultsLabel = resultsLabel;
    }

    public int getMapColumnIndex()
    {
        return resultList.mapColumnIndex;
    }

    public int getShowFootprintColumnIndex()
    {
        return resultList.showFootprintColumnIndex;
    }

    public int getFrusColumnIndex()
    {
        return resultList.frusColumnIndex;
    }

    public int getBndrColumnIndex()
    {
        return resultList.bndrColumnIndex;
    }

    public int getDateColumnIndex()
    {
        return resultList.dateColumnIndex;
    }

    public int getIdColumnIndex()
    {
        return resultList.idColumnIndex;
    }

    public int getFilenameColumnIndex()
    {
        return resultList.filenameColumnIndex;
    }

    public JLabel getLblNumberBoundaries()
    {
        return lblNumberBoundaries;
    }

}
