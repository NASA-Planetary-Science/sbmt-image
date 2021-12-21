package edu.jhuapl.sbmt.image.controllers.color;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.view.light.LightUtil;
import edu.jhuapl.sbmt.image.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image.common.ImageKeyInterface;
import edu.jhuapl.sbmt.image.controllers.StringRenderer;
import edu.jhuapl.sbmt.image.core.listeners.ColorImageResultsListener;
import edu.jhuapl.sbmt.image.core.listeners.ImageSearchResultsListener;
import edu.jhuapl.sbmt.image.gui.color.ColorImageGenerationPanel;
import edu.jhuapl.sbmt.image.gui.color.ColorImagePopupMenu;
import edu.jhuapl.sbmt.image.types.ImageSearchModel;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImage;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImage.ColorImageKey;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImage.NoOverlapException;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImageCollection;
import edu.jhuapl.sbmt.image.types.colorImage.ColorImageModel;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundaryCollection;

import nom.tam.fits.FitsException;

public class ColorImageController
{
    private ImageSearchModel model;
    private ColorImageModel colorModel;
    private ColorImageGenerationPanel panel;
    private SbmtInfoWindowManager infoPanelManager;
    private StringRenderer stringRenderer;
    private PerspectiveImageBoundaryCollection boundaries;
    private ColorImageResultsTableModeListener tableModelListener;
    private ColorImageResultsPropertyChangeListener propertyChangeListener;
    private ColorImageCollection colorImages;
    private Renderer renderer;

    public ColorImageController(ImageSearchModel model, ColorImageModel colorModel, SbmtInfoWindowManager infoPanelManager, Renderer renderer)
    {
        this.model = model;
        this.renderer = renderer;
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
            }
        });
        this.colorModel = colorModel;
        colorImages = (ColorImageCollection)model.getModelManager().getModel(colorModel.getImageCollectionModelName()).get(0);
        colorModel.setImages(colorImages);
        panel = new ColorImageGenerationPanel();
        this.infoPanelManager = infoPanelManager;
        propertyChangeListener = new ColorImageResultsPropertyChangeListener();
        tableModelListener = new ColorImageResultsTableModeListener();
        setupPanel();

        colorModel.addResultsChangedListener(new ColorImageResultsListener()
        {

            @Override
            public void colorImageAdded(ColorImageKey colorKey)
            {
                JTable colorImagesDisplayedList = panel.getDisplayedImageList();
                ((DefaultTableModel)colorImagesDisplayedList.getModel()).setRowCount(colorImagesDisplayedList.getRowCount()+1);
                int mapColumnIndex = panel.getMapColumnIndex();
                int showFootprintColumnIndex = panel.getShowFootprintColumnIndex();
                int filenameColumnIndex = panel.getFilenameColumnIndex();
                int i = colorImagesDisplayedList.getRowCount();
                colorImagesDisplayedList.setValueAt(true, i-1, mapColumnIndex);
                colorImagesDisplayedList.setValueAt(true, i-1, showFootprintColumnIndex);
                colorImagesDisplayedList.setValueAt(colorKey.toString(), i-1, filenameColumnIndex);

            }

            @Override
            public void colorImageRemoved(ColorImageKey image)
            {
            	 DefaultTableModel tableModel = (DefaultTableModel)panel.getDisplayedImageList().getModel();
                 int index = panel.getDisplayedImageList().getSelectedRow();
                 tableModel.removeRow(index);
            }
        });
    }

    private void setupPanel()
    {
        boundaries = (PerspectiveImageBoundaryCollection)model.getModelManager().getModel(colorModel.getImageBoundaryCollectionModelName()).get(0);

        ColorImagePopupMenu colorImagePopupMenu = new ColorImagePopupMenu(colorImages, /*boundaries,*/ infoPanelManager, model.getModelManager(), renderer, panel);
        panel.setColorImagePopupMenu(colorImagePopupMenu);

        colorImages.addPropertyChangeListener(propertyChangeListener);
        boundaries.addPropertyChangeListener(propertyChangeListener);

        panel.getRemoveColorImageButton().setText("Remove Color Image");
        panel.getRemoveColorImageButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeColorImageButtonActionPerformed(evt);
            }
        });

        panel.getGenerateColorImageButton().setText("Generate Color Image");
        panel.getGenerateColorImageButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateColorImageButtonActionPerformed(evt);
            }
        });

        JButton redButton = panel.getRedButton();
        redButton.setBackground(new java.awt.Color(255, 0, 0));
        redButton.setText("Red");
        redButton.setToolTipText("Select an image from the list above and then press this button");
        redButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redButtonActionPerformed(evt);
            }
        });

        JButton greenButton = panel.getGreenButton();
        greenButton.setBackground(new java.awt.Color(0, 255, 0));
        greenButton.setText("Green");
        greenButton.setToolTipText("Select an image from the list above and then press this button");
        greenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                greenButtonActionPerformed(evt);
            }
        });

        JButton blueButton = panel.getBlueButton();
        blueButton.setBackground(new java.awt.Color(0, 0, 255));
        blueButton.setText("Blue");
        blueButton.setToolTipText("Select an image from the list above and then press this button");
        blueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blueButtonActionPerformed(evt);
            }
        });


        String[] columnNames = new String[]{
                "Map",
                "Show",
                "Filename",
        };

        panel.getDisplayedImageList().setModel(new ColorImageTableModel(new Object[0][3], columnNames));
        stringRenderer = new StringRenderer(model, model.getImageResults());
        panel.getDisplayedImageList().setDefaultRenderer(String.class, stringRenderer);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getMapColumnIndex()).setPreferredWidth(31);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getShowFootprintColumnIndex()).setPreferredWidth(35);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getMapColumnIndex()).setResizable(true);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getShowFootprintColumnIndex()).setResizable(true);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getFilenameColumnIndex()).setPreferredWidth(350);
        panel.getDisplayedImageList().getColumnModel().getColumn(panel.getFilenameColumnIndex()).setResizable(true);
        panel.getDisplayedImageList().getModel().addTableModelListener(tableModelListener);
        colorImages.addPropertyChangeListener(propertyChangeListener);

        panel.getDisplayedImageList().addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                colorImagesDisplayedListMaybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e)
            {
                colorImagesDisplayedListMaybeShowPopup(e);
            }
        });


        panel.getDisplayedImageList().getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
//                    imageSearchModel.setSelectedImageIndex(imageResultsTableView.getResultList().getSelectedRows());
//                    imageResultsTableView.getViewResultsGalleryButton().setEnabled(imageResultsTableView.isEnableGallery() && imageResultsTableView.getResultList().getSelectedRowCount() > 0);
                }
            }
        });

    }

    private void generateColorImageButtonActionPerformed(ActionEvent evt)
    {
        try
        {
            colorModel.generateColorImage(evt);
        }
        catch (IOException e1)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                    "There was an error mapping the image.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        }
        catch (FitsException e1)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                    "There was an error mapping the image.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        }
        catch (ColorImage.NoOverlapException e1)
        {
            JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(panel),
                    "Color Image Generation: The images you selected do not overlap.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeColorImageButtonActionPerformed(ActionEvent evt)
    {
        int index = panel.getDisplayedImageList().getSelectedRow();
        if (index >= 0)
        {
        	ColorImageKey colorKey = (ColorImageKey)colorImages.getLoadedImageKeys().get(index);
            colorModel.removeColorImage(colorKey);
        }
    }

    private void redButtonActionPerformed(ActionEvent evt)
    {
        ImageKeyInterface selectedKey = model.getSelectedImageKeys()[0];
        if (selectedKey != null)
        {
            String name = selectedKey.getName();
//            if (!selectedKey.band.equals("0"))
//                name = selectedKey.band + ":" + name;
            panel.getRedLabel().setText(new File(name).getName());
            colorModel.setSelectedRedKey(selectedKey);
        }
    }

    private void greenButtonActionPerformed(ActionEvent evt)
    {
    	ImageKeyInterface selectedKey = model.getSelectedImageKeys()[0];
        if (selectedKey != null)
        {
            String name = selectedKey.getName();
//            if (!selectedKey.band.equals("0"))
//                name = selectedKey.band + ":" + name;
            panel.getGreenLabel().setText(new File(name).getName());
            colorModel.setSelectedGreenKey(selectedKey);
        }
    }

    private void blueButtonActionPerformed(ActionEvent evt)
    {
    	ImageKeyInterface selectedKey = model.getSelectedImageKeys()[0];
        if (selectedKey != null)
        {
            String name = selectedKey.getName();
//            if (!selectedKey.band.equals("0"))
//                name = selectedKey.band + ":" + name;
            panel.getBlueLabel().setText(new File(name).getName());
            colorModel.setSelectedBlueKey(selectedKey);
        }
    }




    private void colorImagesDisplayedListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JTable colorImagesDisplayedList = panel.getDisplayedImageList();
            int index = colorImagesDisplayedList.rowAtPoint(e.getPoint());

            if (index >= 0)
            {
                if (!colorImagesDisplayedList.isRowSelected(index))
                {
                    colorImagesDisplayedList.clearSelection();
                    colorImagesDisplayedList.setRowSelectionInterval(index, index);
                }
                ColorImage image = colorImages.getLoadedImages().get(index);
                ColorImageKey colorKey = image.getColorKey();
                panel.getColorImagePopupMenu().setCurrentImage(colorKey);
                panel.getColorImagePopupMenu().show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public ColorImageGenerationPanel getPanel()
    {
        return panel;
    }

    public class ColorImageTableModel extends DefaultTableModel
    {
        public ColorImageTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            // Only allow editing the hide column if the image is mapped
            if (column == panel.getShowFootprintColumnIndex())
            {
                return (Boolean)getValueAt(row, 0);
            }
            else
            {
                return column == panel.getMapColumnIndex();
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

    class ColorImageResultsTableModeListener implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
            if (e.getColumn() == panel.getMapColumnIndex())
            {
                int row = e.getFirstRow();
                ColorImage image = colorImages.getLoadedImages().get(row);
                ColorImageKey key = image.getColorKey();

                if ((Boolean)panel.getDisplayedImageList().getValueAt(row, panel.getMapColumnIndex()))
                    try
                    {
                        colorModel.loadImage(key);
                    }
                    catch (FitsException | IOException | NoOverlapException e1)
                    {
                        e1.printStackTrace();
                    }
                else
                {
                    colorModel.unloadImage(key);
                    LightUtil.switchToLightKit(model.getRenderer());
                }
            }
            else if (e.getColumn() == panel.getShowFootprintColumnIndex())
            {
                int row = e.getFirstRow();
                boolean visible = (Boolean)panel.getDisplayedImageList().getValueAt(row, panel.getShowFootprintColumnIndex());
                colorImages.getLoadedImages().get(row).setVisible(visible);
            }
        }
    }

    class ColorImageResultsPropertyChangeListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            {
                JTable resultList = panel.getDisplayedImageList();
                if (resultList.getRowCount() == 0) return;
                resultList.getModel().removeTableModelListener(tableModelListener);
                int i=0;
                for (ColorImage colorImage : colorImages.getLoadedImages())
                {
                    if (colorImages.getImages().contains(colorImage))
                    {
                        resultList.setValueAt(true, i, panel.getMapColumnIndex());
                        resultList.setValueAt(colorImage.isVisible(), i, panel.getShowFootprintColumnIndex());
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
