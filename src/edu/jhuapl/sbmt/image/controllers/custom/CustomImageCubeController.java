package edu.jhuapl.sbmt.image.controllers.custom;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.view.light.LightUtil;
import edu.jhuapl.sbmt.image.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.image.controllers.StringRenderer;
import edu.jhuapl.sbmt.image.core.listeners.ImageCubeResultsListener;
import edu.jhuapl.sbmt.image.core.listeners.ImageSearchResultsListener;
import edu.jhuapl.sbmt.image.gui.cubes.ImageCubeGenerationPanel;
import edu.jhuapl.sbmt.image.gui.cubes.ImageCubePopupMenu;
import edu.jhuapl.sbmt.image.gui.spectral.SpectralImageCubeGenerationPanel;
import edu.jhuapl.sbmt.image.types.ImageCollection;
import edu.jhuapl.sbmt.image.types.ImageSearchModel;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImage.NoOverlapException;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCube;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCubeCollection;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCubeModel;
import edu.jhuapl.sbmt.image.types.imageCube.ImageCube.ImageCubeKey;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundaryCollection;

import nom.tam.fits.FitsException;

public class CustomImageCubeController
{
    protected ImageSearchModel model;
    protected ImageCubeModel cubeModel;
    protected ImageCubeGenerationPanel panel;
    private SbmtInfoWindowManager infoPanelManager;
    private ImageCubePopupMenu imageCubePopupMenu;
    private SbmtSpectrumWindowManager spectrumPanelManager;
    private Renderer renderer;
    ImageCubeCollection imageCubes;
    private StringRenderer stringRenderer;
    private CustomImageCubeResultsTableModeListener tableModelListener;
    private ImageCubeResultsPropertyChangeListener propertyChangeListener;
    private PerspectiveImageBoundaryCollection boundaries;

    public CustomImageCubeController(ImageSearchModel model,
            ImageCubeModel cubeModel,
            SbmtInfoWindowManager infoPanelManager,
            ImageCubePopupMenu imageCubePopupMenu,
            SbmtSpectrumWindowManager spectrumPanelManager, Renderer renderer)
    {
        super();
        this.model = model;
        model.addResultsChangedListener(new ImageSearchResultsListener()
        {
            @Override
            public void resultsChanged(List<List<String>> results)
            {
                stringRenderer.setImageRawResults(results);
            }

            @Override
            public void resultsCountChanged(int count)
            {
                // TODO Auto-generated method stub

            }
        });
        this.cubeModel = cubeModel;
        this.infoPanelManager = infoPanelManager;
        this.imageCubePopupMenu = imageCubePopupMenu;
        this.spectrumPanelManager = spectrumPanelManager;
        this.renderer = renderer;
        ImageCollection imageCollection = (ImageCollection)model.getModelManager().getModel(model.getImageCollectionModelName()).get(0);
        this.cubeModel.setImageCollection(imageCollection);
        this.cubeModel.setImageSearchModel(model);
        this.panel = new SpectralImageCubeGenerationPanel();
        propertyChangeListener = new ImageCubeResultsPropertyChangeListener();
        panel.getImageCubeTable().getModel().removeTableModelListener(tableModelListener);
        tableModelListener = new CustomImageCubeResultsTableModeListener();
        System.out.println("ImageCubeController: ImageCubeController: calling");
        setupPanel();
        System.out.println("ImageCubeController: ImageCubeController: called");

        cubeModel.addResultsChangedListener(new ImageCubeResultsListener()
        {

            @Override
            public void imageCubeAdded(ImageCubeKey imageCubeKey)
            {
                DefaultTableModel tableModel = (DefaultTableModel)panel.getImageCubeTable().getModel();
                tableModel.setRowCount(panel.getImageCubeTable().getRowCount()+1);
                int i = panel.getImageCubeTable().getRowCount();
                panel.getImageCubeTable().setValueAt(true, i-1, panel.getMapColumnIndex());
                panel.getImageCubeTable().setValueAt(true, i-1, panel.getShowFootprintColumnIndex());
                panel.getImageCubeTable().setValueAt(imageCubeKey.toString(), i-1, panel.getFilenameColumnIndex());
            }

            @Override
            public void imageCubeRemoved(ImageCubeKey imageCubeKey)
            {
                DefaultTableModel tableModel = (DefaultTableModel)panel.getImageCubeTable().getModel();
                int index = panel.getImageCubeTable().getSelectedRow();
                tableModel.removeRow(index);
            }

            @Override
            public void presentErrorMessage(String message)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                        message,
                        "Notification",
                        JOptionPane.ERROR_MESSAGE);
            }

            @Override
            public void presentInformationalMessage(String message)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                        message,
                        "Notification",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public void addPropertyChangeListner(PropertyChangeListener listener)
    {
        imageCubes.addPropertyChangeListener(listener);
    }

    protected void setupPanel()
    {
        System.out.println("ImageCubeController: setupPanel: ");
        boundaries = (PerspectiveImageBoundaryCollection)model.getModelManager().getModel(model.getImageBoundaryCollectionModelName()).get(0);
        imageCubes = (ImageCubeCollection)model.getModelManager().getModel(cubeModel.getImageCubeCollectionModelName()).get(0);
        imageCubePopupMenu = new ImageCubePopupMenu(imageCubes, boundaries, infoPanelManager, spectrumPanelManager, renderer, panel);

        panel.getRemoveImageCubeButton().setText("Remove Image Cube");
        panel.getRemoveImageCubeButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeImageCubeButtonActionPerformed(evt);
            }
        });

        panel.getGenerateImageCubeButton().setText("Generate Image Cube");
        panel.getGenerateImageCubeButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                generateImageCubeButtonActionPerformed(evt);
            }
        });

        String[] columnNames = new String[]{
                "Map",
                "Show",
                "Filename",
        };
        panel.getImageCubeTable().setModel(new CustomImageCubesTableModel(new Object[0][3], columnNames));
        stringRenderer = new StringRenderer(model, model.getImageResults());
        panel.getImageCubeTable().setDefaultRenderer(String.class, stringRenderer);
        panel.getImageCubeTable().getColumnModel().getColumn(panel.getMapColumnIndex()).setPreferredWidth(31);
        panel.getImageCubeTable().getColumnModel().getColumn(panel.getShowFootprintColumnIndex()).setPreferredWidth(35);
        panel.getImageCubeTable().getColumnModel().getColumn(panel.getMapColumnIndex()).setResizable(true);
        panel.getImageCubeTable().getColumnModel().getColumn(panel.getShowFootprintColumnIndex()).setResizable(true);
        panel.getImageCubeTable().getColumnModel().getColumn(panel.getFilenameColumnIndex()).setPreferredWidth(250);
        panel.getImageCubeTable().getModel().addTableModelListener(tableModelListener);
        imageCubes.addPropertyChangeListener(propertyChangeListener);

        panel.getImageCubeTable().addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                imageCubesDisplayedListMaybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e)
            {
                imageCubesDisplayedListMaybeShowPopup(e);
            }
        });


//        panel.getImageCubeTable().getSelectionModel().addListSelectionListener(new ListSelectionListener()
//        {
//            @Override
//            public void valueChanged(ListSelectionEvent e)
//            {
//                System.out.println(
//                        "ImageCubeController.setupPanel().new ListSelectionListener() {...}: valueChanged: ");
//                if (!e.getValueIsAdjusting())
//                {
////                    imageSearchModel.setSelectedImageIndex(imageResultsTableView.getResultList().getSelectedRows());
////                    imageResultsTableView.getViewResultsGalleryButton().setEnabled(imageResultsTableView.isEnableGallery() && imageResultsTableView.getResultList().getSelectedRowCount() > 0);
//                }
//            }
//        });
    }


    private void imageCubesDisplayedListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JTable imageCubesDisplayedList = panel.getImageCubeTable();
            int index = imageCubesDisplayedList.rowAtPoint(e.getPoint());

            if (index >= 0)
            {
                if (!imageCubesDisplayedList.isRowSelected(index))
                {
                    imageCubesDisplayedList.clearSelection();
                    imageCubesDisplayedList.setRowSelectionInterval(index, index);
                }
                ImageCube image = imageCubes.getLoadedImages().get(index);
                ImageCubeKey imageCubeKey = image.getImageCubeKey();
                imageCubePopupMenu.setCurrentImage(imageCubeKey);
                imageCubePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    protected void generateImageCube(ActionEvent e)
    {
        cubeModel.generateImageCube(e);
    }

    protected void removeImageCube(ActionEvent e)
    {
        JTable imageCubesDisplayedList = panel.getImageCubeTable();
        int index = imageCubesDisplayedList.getSelectedRow();
        if (index >= 0)
        {
            ImageCubeKey imageCubeKey = (ImageCubeKey)((DefaultListModel)imageCubesDisplayedList.getModel()).remove(index);
            cubeModel.removeImageCube(imageCubeKey);
        }
    }

    private void removeImageCubeButtonActionPerformed(ActionEvent evt) {
        removeImageCube(evt);
    }

    private void generateImageCubeButtonActionPerformed(ActionEvent evt) {
        generateImageCube(evt);
    }

    public ImageCubeGenerationPanel getPanel()
    {
        return panel;
    }

    public class CustomImageCubesTableModel extends DefaultTableModel
    {
        public CustomImageCubesTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            // Only allow editing the hide column if the image is mapped
            if (column == panel.getShowFootprintColumnIndex() /*|| column == panel.getFrusColumnIndex()*/)
            {
                return (Boolean)getValueAt(row, 0);
            }
            else
            {
                return column == panel.getMapColumnIndex() /*|| column == panel.getBndrColumnIndex()*/;
            }
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex <= panel.getShowFootprintColumnIndex())
                return Boolean.class;
            else
                return String.class;
        }
    }

    class CustomImageCubeResultsTableModeListener implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
            if (e.getColumn() == panel.getMapColumnIndex())
            {
                int row = e.getFirstRow();
                ImageCube image = imageCubes.getLoadedImages().get(row);
                ImageCubeKey key = image.getImageCubeKey();

                if ((Boolean)panel.getImageCubeTable().getValueAt(row, panel.getMapColumnIndex()))
                {
                    try
                    {
                        cubeModel.loadImage(key);
                    }
                    catch (FitsException | IOException | NoOverlapException e1)
                    {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                else
                {
                    cubeModel.unloadImage(key);
                    panel.getImageCubeTable().getModel().setValueAt(false, 0, panel.getShowFootprintColumnIndex());
                    LightUtil.switchToLightKit(model.getRenderer());
                }
            }
            else if (e.getColumn() == panel.getShowFootprintColumnIndex())
            {
                int row = e.getFirstRow();
                boolean visible = (Boolean)panel.getImageCubeTable().getValueAt(row, panel.getShowFootprintColumnIndex());
                if (imageCubes.getLoadedImages().size() == 0) return;
                imageCubes.getLoadedImages().get(row).setVisible(visible);
            }
        }
    }

    class ImageCubeResultsPropertyChangeListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            {
                JTable resultList = panel.getImageCubeTable();
                if (resultList.getRowCount() == 0) return;
                resultList.getModel().removeTableModelListener(tableModelListener);
                Set<ImageCube> imageCubeSet = imageCubes.getImages();
                System.out.println(
                        "ImageCubeController.ImageCubeResultsPropertyChangeListener: propertyChange: image cubes size " + imageCubeSet.size() + " " + Integer.toHexString(imageCubes.hashCode()));
                int i=0;
                for (ImageCube image : imageCubeSet)
                {
                    ImageCubeKey key = image.getImageCubeKey();
                    if (imageCubes.containsImage(key))
                    {
                        resultList.setValueAt(true, i, panel.getMapColumnIndex());
                        resultList.setValueAt(image.isVisible(), i, panel.getShowFootprintColumnIndex());
                    }
                    else
                    {
                        resultList.setValueAt(false, i, panel.getMapColumnIndex());
                        resultList.setValueAt(false, i, panel.getShowFootprintColumnIndex());
                    }
                    i++;
                }
                resultList.getModel().addTableModelListener(tableModelListener);
                // Repaint the list in case the boundary colors has changed
                resultList.repaint();
            }
        }
    }
}
