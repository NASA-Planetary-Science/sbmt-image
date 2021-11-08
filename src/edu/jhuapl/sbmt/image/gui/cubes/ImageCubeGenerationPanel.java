package edu.jhuapl.sbmt.image.gui.cubes;

import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import edu.jhuapl.sbmt.image.gui.images.ImageResultsTable;

public abstract class ImageCubeGenerationPanel extends JPanel //implements PropertyChangeListener, TableModelListener, MouseListener, ListSelectionListener
{
    private JButton generateImageCubeButton;
    private JButton removeImageCubeButton;
    protected int mapColumnIndex,showFootprintColumnIndex,frusColumnIndex,/*bndrColumnIndex,*/dateColumnIndex,idColumnIndex,filenameColumnIndex;
    private ImageResultsTable imageCubeTable;
    private JScrollPane scrollPane;
    protected JPanel panel_1;
    private DefaultBoundedRangeModel monoBoundedRangeModel;
    private int nbands;

    public ImageCubeGenerationPanel()
    {
        setBorder(new TitledBorder(null, "Image Cube Generation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        generateImageCubeButton = new JButton("Generate Image Cube");
        panel.add(generateImageCubeButton);

        removeImageCubeButton = new JButton("Remove Image Cube");
        panel.add(removeImageCubeButton);

        scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new java.awt.Dimension(300, 100));
        add(scrollPane);

        imageCubeTable = new ImageResultsTable();
        scrollPane.setViewportView(imageCubeTable);

        panel_1 = new JPanel();
        add(panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));

        mapColumnIndex = 0;
        showFootprintColumnIndex = 1;
//        bndrColumnIndex = 2;
        filenameColumnIndex = 2;
    }

    public JTable getImageCubeTable()
    {
        return imageCubeTable;
    }


    public int getMapColumnIndex()
    {
        return mapColumnIndex;
    }


    public int getShowFootprintColumnIndex()
    {
        return showFootprintColumnIndex;
    }


    public int getFrusColumnIndex()
    {
        return frusColumnIndex;
    }


//    public int getBndrColumnIndex()
//    {
//        return bndrColumnIndex;
//    }


    public int getDateColumnIndex()
    {
        return dateColumnIndex;
    }


    public int getIdColumnIndex()
    {
        return idColumnIndex;
    }


    public int getFilenameColumnIndex()
    {
        return filenameColumnIndex;
    }


    public JButton getGenerateImageCubeButton()
    {
        return generateImageCubeButton;
    }


    public JButton getRemoveImageCubeButton()
    {
        return removeImageCubeButton;
    }
}
