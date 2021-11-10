package edu.jhuapl.sbmt.image.controllers.custom;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.DefaultTableModel;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.sbmt.image.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.image.common.ImageKeyInterface;
import edu.jhuapl.sbmt.image.controllers.images.ImageResultsTableController;
import edu.jhuapl.sbmt.image.core.ImagingInstrument;
import edu.jhuapl.sbmt.image.gui.custom.CustomImageImporterDialog.ProjectionType;
import edu.jhuapl.sbmt.image.types.ImageCollection;
import edu.jhuapl.sbmt.image.types.customImage.CustomCylindricalImageKey;
import edu.jhuapl.sbmt.image.types.customImage.CustomImageKeyInterface;
import edu.jhuapl.sbmt.image.types.customImage.CustomImagesModel;
import edu.jhuapl.sbmt.image.types.customImage.CustomPerspectiveImageKey;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImage;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundaryCollection;

public class CustomImageResultsTableController extends ImageResultsTableController
{
    private List<CustomImageKeyInterface> results;
    protected CustomImagesModel model;
    int modifiedTableRow = -1;

    public CustomImageResultsTableController(ImagingInstrument instrument, ImageCollection imageCollection, CustomImagesModel model, Renderer renderer, SbmtInfoWindowManager infoPanelManager, SbmtSpectrumWindowManager spectrumPanelManager)
    {
        super(instrument, imageCollection, model, renderer, infoPanelManager, spectrumPanelManager);
        this.model = model;
        this.results = model.getCustomImages();
        this.boundaries = (PerspectiveImageBoundaryCollection)model.getModelManager().getModel(model.getImageBoundaryCollectionModelName()).get(0);
    }

    @Override
    public void setImageResultsPanel()
    {
        super.setImageResultsPanel();
        imageResultsTableView.getNextButton().setVisible(false);
        imageResultsTableView.getPrevButton().setVisible(false);
        imageResultsTableView.getNumberOfBoundariesComboBox().setVisible(false);
        imageResultsTableView.getLblNumberBoundaries().setVisible(false);

        imageResultsTableView.getResultList().setUI(new CustomDragDropRowTableUI());

        imageResultsTableView.getResultList().getModel().removeTableModelListener(tableModelListener);
        tableModelListener = new CustomImageResultsTableModeListener();
        imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);
        this.imageCollection.removePropertyChangeListener(propertyChangeListener);
        boundaries.removePropertyChangeListener(propertyChangeListener);
        propertyChangeListener = new CustomImageResultsPropertyChangeListener();

        this.imageResultsTableView.addComponentListener(new ComponentListener()
		{

			@Override
			public void componentShown(ComponentEvent e)
			{
				imageCollection.addPropertyChangeListener(propertyChangeListener);
				boundaries.addPropertyChangeListener(propertyChangeListener);
			}

			@Override
			public void componentResized(ComponentEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void componentMoved(ComponentEvent e)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void componentHidden(ComponentEvent e)
			{
				imageCollection.removePropertyChangeListener(propertyChangeListener);
		        boundaries.removePropertyChangeListener(propertyChangeListener);
			}
		});


        tableModel = new CustomImagesTableModel(new Object[0][7], columnNames);
        imageResultsTableView.getResultList().setModel(tableModel);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getMapColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getShowFootprintColumnIndex()).setPreferredWidth(35);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getFrusColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getBndrColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getMapColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getShowFootprintColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getFrusColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getBndrColumnIndex()).setResizable(true);

        imageResultsTableView.getResultList().addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                resultsListMaybeShowPopup(e);
                imageResultsTableView.getSaveSelectedImageListButton().setEnabled(imageResultsTableView.getResultList().getSelectedRowCount() > 0);
            }

            public void mouseReleased(MouseEvent e)
            {
                resultsListMaybeShowPopup(e);
                imageResultsTableView.getSaveSelectedImageListButton().setEnabled(imageResultsTableView.getResultList().getSelectedRowCount() > 0);
            }
        });


        imageResultsTableView.getResultList().getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    model.setSelectedImageIndex(imageResultsTableView.getResultList().getSelectedRows());
                    updateSearchResultsControls();
                }
            }
        });

        imageResultsTableView.getRemoveAllImagesButton().removeActionListener(imageResultsTableView.getRemoveAllImagesButton().getActionListeners()[0]);
        imageResultsTableView.getRemoveAllButton().removeActionListener(imageResultsTableView.getRemoveAllButton().getActionListeners()[0]);

        imageResultsTableView.getRemoveAllImagesButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                model.removeAllImagesButtonActionPerformed(e);
            }
        });

        imageResultsTableView.getRemoveAllButton().addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                model.removeAllButtonActionPerformed(e);
            }
        });

        try
        {
            model.initializeImageList();
            this.showImageBoundaries(null);

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void prevButtonActionPerformed(ActionEvent evt)
    {
        IdPair resultIntervalCurrentlyShown = model.getResultIntervalCurrentlyShown();
        IdPair originalInterval = resultIntervalCurrentlyShown;
        removeImageBoundaries(originalInterval);
        int step = Integer.parseInt((String) imageResultsTableView.getNumberOfBoundariesComboBox().getSelectedItem());
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the prev block if there's something left to show.
            if (resultIntervalCurrentlyShown.id1 > 0)
            {
            	if (resultIntervalCurrentlyShown.id1 - step > 0)
            	{
            		resultIntervalCurrentlyShown.prevBlock(step);
            		showImageBoundaries(resultIntervalCurrentlyShown);
            	}
            	else
            	{
            		resultIntervalCurrentlyShown = new IdPair(0, resultIntervalCurrentlyShown.id1);
            		showImageBoundaries(resultIntervalCurrentlyShown);
            	}
            }
            else
            {
            	resultIntervalCurrentlyShown = new IdPair(imageRawResults.size()-step, imageRawResults.size());
            	showImageBoundaries(resultIntervalCurrentlyShown);
            }
        }

    }

    @Override
    protected void nextButtonActionPerformed(java.awt.event.ActionEvent evt)
    {

        IdPair resultIntervalCurrentlyShown = model.getResultIntervalCurrentlyShown();
        if (resultIntervalCurrentlyShown != null)
        {
            // Only get the next block if there's something left to show.
            int step = Integer.parseInt((String) imageResultsTableView.getNumberOfBoundariesComboBox().getSelectedItem());

            if (resultIntervalCurrentlyShown.id2 < imageResultsTableView.getResultList().getModel().getRowCount())
            {
                resultIntervalCurrentlyShown.nextBlock(Integer.parseInt((String)imageResultsTableView.getNumberOfBoundariesComboBox().getSelectedItem()));
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
            else
            {
            	resultIntervalCurrentlyShown = new IdPair(0, step);
                showImageBoundaries(resultIntervalCurrentlyShown);
            }
        }
        else
        {
            resultIntervalCurrentlyShown = new IdPair(0, Integer.parseInt((String)imageResultsTableView.getNumberOfBoundariesComboBox().getSelectedItem()));
            showImageBoundaries(resultIntervalCurrentlyShown);
        }
    }

    @Override
    protected void loadImageListButtonActionPerformed(ActionEvent evt) {
        File file = CustomFileChooser.showOpenDialog(imageResultsTableView, "Select File");

        if (file != null)
        {
            try
            {
                model.loadImages(file.getAbsolutePath());
                model.setResultIntervalCurrentlyShown(new IdPair(0, model.getNumBoundaries()));
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView),
                        "There was an error reading the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }

    }

    @Override
    protected void saveImageListButtonActionPerformed(ActionEvent evt) {
        File file = CustomFileChooser.showSaveDialog(imageResultsTableView, "Select File", "imagelist.txt");

        if (file != null)
        {
            try
            {
                model.saveImages(results, file.getAbsolutePath());
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView),
                        "There was an error saving the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }
    }

    @Override
    protected void saveSelectedImageListButtonActionPerformed(java.awt.event.ActionEvent evt) {
        File file = CustomFileChooser.showSaveDialog(imageResultsTableView, "Select File", "imagelist.txt");

        if (file != null)
        {
            try
            {
                ArrayList<CustomImageKeyInterface> infos = new ArrayList<CustomImageKeyInterface>();
                int[] selectedIndices = imageResultsTableView.getResultList().getSelectedRows();
                for (int selectedIndex : selectedIndices)
                {
                    infos.add(model.getCustomImages().get(selectedIndex));
                }
                model.saveImages(infos, file.getAbsolutePath());

            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView),
                        "There was an error saving the file.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e.printStackTrace();
            }
        }
    }

    @Override
    public void setImageResults(List<List<String>> results)
    {
        super.setImageResults(results);
        model.setResultIntervalCurrentlyShown(new IdPair(0, results.size()));
    }

    @Override
    protected void showImageBoundaries(IdPair idPair)
    {
        if (idPair == null)
        {
            boundaries.removeAllBoundaries();
            return;
        }
        int startId = idPair.id1;
        int endId = idPair.id2;
        boundaries.removeAllBoundaries();
        model.setResultIntervalCurrentlyShown(idPair);
        for (int i=startId; i<endId; ++i)
        {
            if (i < 0)
                continue;
            else if(i >= imageRawResults.size())
                break;

            try
            {
                CustomImageKeyInterface key = model.getImageKeyForIndex(i);
                CustomImageKeyInterface info;
                if (key.getProjectionType() == ProjectionType.PERSPECTIVE)
        		{
        			info = new CustomPerspectiveImageKey( //
        			        SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + key.getImageFilename()), //
        			        key.getImageFilename(), key.getSource(), key.getImageType(), //
        			        ((CustomPerspectiveImageKey)key).getRotation(), ((CustomPerspectiveImageKey)key).getFlip(), //
        			        key.getFileType(), key.getPointingFile(), key.getDate(), key.getOriginalName());
                    boundaries.addBoundary(info);

        		}
        		else
        		{
        			info = new CustomCylindricalImageKey( //
        			        SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + key.getImageFilename()), //
        			        key.getImageFilename(), key.getImageType(), key.getSource(), key.getDate(), key.getOriginalName());
        		}
                //TODO For now, we don't handle cylindrical image boundaries, since it is a PerspectiveImageBoundary - need to make new classes for this.
            }
            catch (Exception e1) {
                JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView),
                        "There was an error mapping the boundary.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);

                e1.printStackTrace();
                break;
            }
        }

    }

    @Override
    protected void resultsListMaybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JTable resultList = imageResultsTableView.getResultList();
            int index = resultList.rowAtPoint(e.getPoint());

            if (index >= 0)
            {
                int[] selectedIndices = resultList.getSelectedRows();
                List<CustomImageKeyInterface> imageKeys = new ArrayList<CustomImageKeyInterface>();
                for (int selectedIndex : selectedIndices)
                {
                    CustomImageKeyInterface imageInfo = ((CustomImagesModel)imageSearchModel).getCustomImages().get(selectedIndex);
                    CustomImageKeyInterface revisedKey = ((CustomImagesModel)imageSearchModel).getRevisedKey(imageInfo);
                    imageKeys.add(revisedKey);
                }
                imagePopupMenu.setCurrentImages(imageKeys);
                imagePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    class CustomImageResultsPropertyChangeListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (Properties.MODEL_CHANGED.equals(evt.getPropertyName()))
            {
                updateTable();
            }
        }
    }

    private void updateTable()
    {
    	JTable resultList = imageResultsTableView.getResultList();
        imageResultsTableView.getResultList().getModel().removeTableModelListener(tableModelListener);
        int size = imageRawResults.size();
        int startIndex = imageSearchModel.getResultIntervalCurrentlyShown().id1;
        int endIndex = Math.min(size, imageSearchModel.getResultIntervalCurrentlyShown().id2);
        if (modifiedTableRow > size) modifiedTableRow = -1;
        if (modifiedTableRow != -1)
        {
        	startIndex = modifiedTableRow;
        	endIndex = startIndex + 1;
        }

        for (int i=startIndex; i<endIndex; ++i)
        {
            CustomImageKeyInterface info = getConvertedKey(model.getImageKeyForIndex(i));
            if (imageCollection.containsImage(info))
            {
                resultList.setValueAt(true, i, imageResultsTableView.getMapColumnIndex());
                resultList.setValueAt(true, i, imageResultsTableView.getShowFootprintColumnIndex());
                if (imageCollection.getImage(info) instanceof PerspectiveImage)
                {
                    PerspectiveImage image = (PerspectiveImage)imageCollection.getImage(model.getImageKeyForIndex(i));
                    resultList.setValueAt(image.isFrustumShowing(), i, imageResultsTableView.getFrusColumnIndex());
                }
            }
            else
            {
                resultList.setValueAt(false, i, imageResultsTableView.getMapColumnIndex());
                resultList.setValueAt(false, i, imageResultsTableView.getShowFootprintColumnIndex());
                resultList.setValueAt(false, i, imageResultsTableView.getFrusColumnIndex());
            }

            if (boundaries.containsBoundary(info))
            {
                resultList.setValueAt(true, i, imageResultsTableView.getBndrColumnIndex());
            }
            else
            {
                resultList.setValueAt(false, i, imageResultsTableView.getBndrColumnIndex());
            }
        }
        imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);
        // Repaint the list in case the boundary colors has changed
        resultList.repaint();
        modifiedTableRow = -1;
    }

    protected CustomImageKeyInterface getConvertedKey(CustomImageKeyInterface key)
    {
        CustomImageKeyInterface info;
        if (key.getProjectionType() == ProjectionType.PERSPECTIVE)
		{
			info = new CustomPerspectiveImageKey(//
			        SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + key.getImageFilename()), //
			        key.getImageFilename(), key.getSource(), key.getImageType(), //
			        ((CustomPerspectiveImageKey)key).getRotation(), ((CustomPerspectiveImageKey)key).getFlip(), //
			        key.getFileType(), key.getPointingFile(), key.getDate(), key.getOriginalName());
		}
		else
		{
			info = new CustomCylindricalImageKey(SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + key.getImageFilename()), key.getImageFilename(), key.getImageType(), key.getSource(), key.getDate(), key.getName());
		}
        return info;
    }

    public class CustomImagesTableModel extends DefaultTableModel
    {
        public CustomImagesTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            // Only allow editing the hide column if the image is mapped
            if (column == imageResultsTableView.getShowFootprintColumnIndex() || column == imageResultsTableView.getFrusColumnIndex())
            {
            	CustomImageKeyInterface info = getConvertedKey(model.getImageKeyForIndex(row));
                return imageCollection.containsImage(info);
            }
            else
            {
                return column == imageResultsTableView.getMapColumnIndex() || column == imageResultsTableView.getBndrColumnIndex();
            }
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex <= imageResultsTableView.getBndrColumnIndex())
                return Boolean.class;
            else
                return String.class;
        }
    }

    public String getCustomDataFolder()
    {
        return model.getModelManager().getPolyhedralModel().getCustomDataFolder();
    }

    class CustomImageResultsTableModeListener implements TableModelListener
    {
        public void tableChanged(TableModelEvent e)
        {
        	modifiedTableRow = e.getFirstRow();
            List<List<String>> imageRawResults = model.getImageResults();
            results = model.getCustomImages();
            DefaultTableModel tableModel = (DefaultTableModel)imageResultsTableView.getResultList().getModel();
            if (e.getColumn() == imageResultsTableView.getMapColumnIndex())
            {
                int row = e.getFirstRow();
                String name = imageRawResults.get(row).get(0);
                name = (String)tableModel.getValueAt(row, imageResultsTableView.getFilenameColumnIndex());
                if ((Boolean)imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getMapColumnIndex()))
                {
                    model.loadImages(results.get(row));
                }
                else
                {
                    model.setImageVisibility(getConvertedKey(results.get(row)), false);
                    model.unloadImages(name, getConvertedKey(results.get(row)));
//                    renderer.setLighting(LightingType.LIGHT_KIT);	//removed due to request in #1667
                }
            }
            else if (e.getColumn() == imageResultsTableView.getShowFootprintColumnIndex())
            {
                int row = e.getFirstRow();
                boolean visible = (Boolean)imageResultsTableView.getResultList().getValueAt(row, imageResultsTableView.getShowFootprintColumnIndex());
                CustomImageKeyInterface info = getConvertedKey(model.getImageKeyForIndex(row));
                model.setImageVisibility(info, visible);
            }
            else if (e.getColumn() == imageResultsTableView.getFrusColumnIndex())
            {
                int row = e.getFirstRow();
                ImageKeyInterface key = model.getImageKeyForIndex(row);
                if (imageCollection.containsImage(key) && (imageCollection.getImage(key) instanceof PerspectiveImage))
                {
                    PerspectiveImage image = (PerspectiveImage) imageCollection.getImage(key);
                    image.setShowFrustum(!image.isFrustumShowing());
                }
            }
            else if (e.getColumn() == imageResultsTableView.getBndrColumnIndex())
            {
                int row = e.getFirstRow();
                CustomImageKeyInterface key;
                CustomImageKeyInterface info = results.get(row);
                if (info.getProjectionType() == ProjectionType.PERSPECTIVE)
        		{
        			key = new CustomPerspectiveImageKey( //
        			        SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + info.getImageFilename()), //
        			        info.getImageFilename(), info.getSource(), info.getImageType(), //
        			        ((CustomPerspectiveImageKey)info).getRotation(), ((CustomPerspectiveImageKey)info).getFlip(), //
        			        info.getFileType(), info.getPointingFile(), info.getDate(), info.getOriginalName());
        		}
        		else
        		{
        			key = new CustomCylindricalImageKey( //
        			        SafeURLPaths.instance().getUrl(getCustomDataFolder() + File.separator + info.getImageFilename()), //
        			        info.getImageFilename(), info.getImageType(), info.getSource(), info.getDate(), info.getOriginalName());
        		}
                // There used to be an assignment here of the key.imageType, but that field is now immutable.
                // However, it appears that this assignment is not necessary -- the correct ImageType is
                // injected when the key is created. Replaced the assignment with a check for mismatch inside
                // the try just for testing, to uncover any runtime cases where this may actually be needed.
                // key.imageType = results.get(row).imageType;
                try
                {
                	// TODO remove this check if it never triggers the AssertionError.
                    if (key.getImageType() != results.get(row).getImageType())
                    {
                        throw new AssertionError("Image type mismatch");
                    }
                    if (!boundaries.containsBoundary(key))
                        boundaries.addBoundary(key);
                    else
                        boundaries.removeBoundary(key);
                }
                catch (Exception e1) {
                    JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(imageResultsTableView),
                            "There was an error mapping the boundary.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);

                    e1.printStackTrace();
                }
            }

        }
    }

    class CustomDragDropRowTableUI extends BasicTableUI {

        private boolean draggingRow = false;
        private int startDragPoint;
        private int dyOffset;

       protected MouseInputListener createMouseInputListener() {
           return new DragDropRowMouseInputHandler();
       }

       public void paint(Graphics g, JComponent c) {
            super.paint(g, c);

            if (draggingRow) {
                 g.setColor(table.getParent().getBackground());
                  Rectangle cellRect = table.getCellRect(table.getSelectedRow(), 0, false);
                 g.copyArea(cellRect.x, cellRect.y, table.getWidth(), table.getRowHeight(), cellRect.x, dyOffset);

                 if (dyOffset < 0) {
                      g.fillRect(cellRect.x, cellRect.y + (table.getRowHeight() + dyOffset), table.getWidth(), (dyOffset * -1));
                 } else {
                      g.fillRect(cellRect.x, cellRect.y, table.getWidth(), dyOffset);
                 }
            }
       }

       class DragDropRowMouseInputHandler extends MouseInputHandler {

    	   int toRow;

           public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                startDragPoint = (int)e.getPoint().getY();
                toRow = table.getSelectedRow();
           }

           public void mouseDragged(MouseEvent e) {
                int fromRow = table.getSelectedRow();

                if (fromRow >= 0) {
                     draggingRow = true;

                     int rowHeight = table.getRowHeight();
                     int middleOfSelectedRow = (rowHeight * fromRow) + (rowHeight / 2);

                     toRow = fromRow;
                     int yMousePoint = (int)e.getPoint().getY();

                     if (yMousePoint < (middleOfSelectedRow - rowHeight)) {
                          // Move row up
                          toRow = fromRow - 1;
                     } else if (yMousePoint > (middleOfSelectedRow + rowHeight)) {
                          // Move row down
                          toRow = fromRow + 1;
                     }

                     DefaultTableModel model = (DefaultTableModel)table.getModel();

                     if (toRow >= 0 && toRow < table.getRowCount())
                     {
	                     model.moveRow(table.getSelectedRow(), table.getSelectedRow(), toRow);
	                     CustomImageKeyInterface fromKey = results.get(table.getSelectedRow());
	                     CustomImageKeyInterface toKey = results.get(toRow);
	                     results.set(fromRow, toKey);
	                     results.set(toRow, fromKey);
	                     table.setRowSelectionInterval(toRow, toRow);
                         startDragPoint = yMousePoint;
                     }
                     dyOffset = (startDragPoint - yMousePoint) * -1;
                     table.repaint();
                }
           }

           public void mouseReleased(MouseEvent e){
                super.mouseReleased(e);
                draggingRow = false;
                table.repaint();
           }
       }
   }
}
