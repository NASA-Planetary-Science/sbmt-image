package edu.jhuapl.sbmt.image.controllers.images;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.sbmt.image.SbmtInfoWindowManager;
import edu.jhuapl.sbmt.image.SbmtSpectrumWindowManager;
import edu.jhuapl.sbmt.image.common.ImageKeyInterface;
import edu.jhuapl.sbmt.image.controllers.StringRenderer;
import edu.jhuapl.sbmt.image.core.Image;
import edu.jhuapl.sbmt.image.core.ImagingInstrument;
import edu.jhuapl.sbmt.image.gui.images.OfflimbImageResultsTableView;
import edu.jhuapl.sbmt.image.types.ImageCollection;
import edu.jhuapl.sbmt.image.types.ImageSearchModel;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImage;
import edu.jhuapl.sbmt.util.TimeUtil;

public class OfflimbImageResultsTableController extends ImageResultsTableController
{
    private OfflimbImageResultsTableView offlimbTableView;

    public OfflimbImageResultsTableController(ImagingInstrument instrument, ImageCollection imageCollection, ImageSearchModel model,
    											Renderer renderer, SbmtInfoWindowManager infoPanelManager,
    											SbmtSpectrumWindowManager spectrumPanelManager)
    {
        super(instrument, imageCollection, model, renderer, infoPanelManager, spectrumPanelManager);
        if (this.propertyChangeListener != null)
        {
            this.imageCollection.removePropertyChangeListener(this.propertyChangeListener);
            this.boundaries.removePropertyChangeListener(this.propertyChangeListener);
            this.propertyChangeListener = new OfflimbImageResultsPropertyChangeListener();
        }
    }

    @Override
    public void setImageResultsPanel()
    {
        offlimbTableView = new OfflimbImageResultsTableView(instrument, imageCollection, imagePopupMenu);
        offlimbTableView.setup();
        this.imageResultsTableView = offlimbTableView;
//        imageResultsTableView.getResultList().setUI(new DragDropRowTableUI());
        setupWidgets();
        setupTable();
    }


    @Override
    public void setupTable()
    {
        String[] columnNames = new String[]{
                "Map",
                "Show",
                "Offlimb",
                "Frus",
                "Bndr",
                "Id",
                "Filename",
                "Date"
        };
        offlimbTableView.getResultList().setModel(new OfflimbImagesTableModel(new Object[0][8], columnNames));

        offlimbTableView.getResultList().getTableHeader().setReorderingAllowed(false);
        offlimbTableView.getResultList().getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        offlimbTableView.getResultList().getColumnModel().getColumn(offlimbTableView.getOffLimbIndex()).setPreferredWidth(60);
        offlimbTableView.getResultList().getColumnModel().getColumn(offlimbTableView.getOffLimbIndex()).setResizable(true);

        tableModelListener = new OfflimbImageResultsTableModeListener();

        this.imageResultsTableView.addComponentListener(new ComponentListener()
		{

			@Override
			public void componentShown(ComponentEvent e)
			{
		        imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);

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
		        imageResultsTableView.getResultList().getModel().removeTableModelListener(tableModelListener);

			}
		});



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
                    imageSearchModel.setSelectedImageIndex(imageResultsTableView.getResultList().getSelectedRows());
//                    updateBodyPositions(null);
                    updateSearchResultsControls();
                }
            }
        });

        stringRenderer = new StringRenderer(imageSearchModel, imageRawResults);
        imageResultsTableView.getResultList().setDefaultRenderer(Date.class, new DateCellRenderer());
        imageResultsTableView.getResultList().setDefaultRenderer(String.class, stringRenderer);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getMapColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getShowFootprintColumnIndex()).setPreferredWidth(35);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getFrusColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getBndrColumnIndex()).setPreferredWidth(31);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getMapColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getShowFootprintColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getFrusColumnIndex()).setResizable(true);
        imageResultsTableView.getResultList().getColumnModel().getColumn(imageResultsTableView.getBndrColumnIndex()).setResizable(true);

        imageResultsTableView.getResultList().getRowSorter().addRowSorterListener(new RowSorterListener()
		{

			@Override
			public void sorterChanged(RowSorterEvent e)
			{
				imageResultsTableView.repaint();
				imageResultsTableView.getResultList().repaint();
				stringRenderer.updateUI();
			}
		});
    }

    protected JTable getResultList()
    {
        return imageResultsTableView.getResultList();
    }

    @Override
    public void setImageResults(List<List<String>> results)
    {
        super.setImageResults(results);
        stringRenderer.setImageRawResults(results);
        int i=0;
        imageResultsTableView.getResultList().getModel().removeTableModelListener(tableModelListener);
        for (List<String> str : results)
        {
            String name = imageRawResults.get(i).get(0);
            ImageKeyInterface key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), imageSearchModel.getImageSourceOfLastQuery(), instrument);
            if (imageCollection.containsImage(key))
            {
                PerspectiveImage image = (PerspectiveImage) imageCollection.getImage(key);
                image.setOffLimbFootprintVisibility(false);   // hide off limb footprint by default
                getResultList().setValueAt(false, i, offlimbTableView.getOffLimbIndex());   // hide off limb footprint by default
            }
            else
            {
                getResultList().setValueAt(false, i, offlimbTableView.getOffLimbIndex());   // hide off limb footprint by default
            }

            ++i;
        }
        imageResultsTableView.getResultList().getModel().addTableModelListener(tableModelListener);
    }

    class OfflimbImageResultsPropertyChangeListener extends ImageResultsPropertyChangeListener
    {
        @Override
        protected void updateTableRow(DefaultTableModel tableModel, int index, ImageKeyInterface key)
        {
            super.updateTableRow(tableModel, index, key);

            if (imageCollection.containsImage(key))
            {
                Image image = imageCollection.getImage(key);

                if (image instanceof PerspectiveImage)
                {
                    PerspectiveImage perspectiveImage = (PerspectiveImage) imageCollection.getImage(key);
//                    System.out.println(
//							"OfflimbImageResultsTableController.OfflimbImageResultsPropertyChangeListener: updateTableRow: setting " + offlimbTableView.getOffLimbIndex());
                    tableModel.setValueAt(perspectiveImage.offLimbFootprintIsVisible(), index, offlimbTableView.getOffLimbIndex());

                }
            }
            else
            {
                tableModel.setValueAt(false, index, offlimbTableView.getOffLimbIndex());
            }

            tableModel.setValueAt(boundaries.containsBoundary(key), index, imageResultsTableView.getBndrColumnIndex());
        }

    }

    private void updateBodyPositions(int row, Runnable completionBlock)
    {
    	Date dt = new Date(Long.parseLong(imageRawResults.get(row).get(1)));
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    	try
		{
    		if (positionOrientationManager != null)
    			positionOrientationManager.run(TimeUtil.str2et(sdf.format(dt)));
        	String name = imageRawResults.get(row).get(0);
            ImageKeyInterface key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), imageSearchModel.getImageSourceOfLastQuery(), instrument);
        	Image image = (imageCollection.getImage(key));
        	if (image == null) {
        		if (completionBlock != null) completionBlock.run();
        		return;
        	}
        	image.propertyChange(new PropertyChangeEvent(this, Properties.MODEL_CHANGED, null, null));
        	if (completionBlock != null) completionBlock.run();
		}
		catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }

    class OfflimbImageResultsTableModeListener extends ImageResultsTableModeListener
    {
        public void tableChanged(TableModelEvent e)
        {
        	int actualRow = imageResultsTableView.getResultList().getRowSorter().convertRowIndexToView(e.getFirstRow());
            int row = (Integer) imageResultsTableView.getResultList().getValueAt(actualRow, imageResultsTableView.getIdColumnIndex()) - 1;
            if (e.getColumn() == offlimbTableView.getMapColumnIndex())
            {
            	updateBodyPositions(row, new Runnable()
				{

					@Override
					public void run()
					{
						String name = imageRawResults.get(row).get(0);
		                String namePrefix = name.substring(0, name.length()-4);
		                OfflimbImageResultsTableModeListener.super.tableChanged(e);
		                offlimbTableView.getResultList().setValueAt(false, actualRow, offlimbTableView.getOffLimbIndex());
		                setOffLimbFootprintVisibility(namePrefix, false);   // set visibility to false if we are mapping or unmapping the image
					}
				});

            }
            else if (e.getColumn() == offlimbTableView.getOffLimbIndex())
            {
                String name = imageRawResults.get(row).get(0);
                String namePrefix = name.substring(0, name.length()-4);
                boolean visible = (Boolean)getResultList().getValueAt(actualRow, offlimbTableView.getOffLimbIndex());
                setOffLimbFootprintVisibility(namePrefix, visible);
                super.tableChanged(e);
            }
            else
            	super.tableChanged(e);
        }
    }

    protected void setOffLimbFootprintVisibility(String name, boolean visible)
    {
        List<ImageKeyInterface> keys = imageSearchModel.createImageKeys(name, imageSearchModel.getImageSourceOfLastQuery(), instrument);
        ImageCollection images = (ImageCollection)imageSearchModel.getModelManager().getModel(imageSearchModel.getImageCollectionModelName()).get(0);
        for (ImageKeyInterface key : keys)
        {
            if (images.containsImage(key))
            {
                PerspectiveImage image = (PerspectiveImage) images.getImage(key);
                image.setOffLimbFootprintVisibility(visible);
            }
        }
    }

    class DateCellRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object v, boolean selected, boolean focus, int r, int c)
        {
        	int actualRow = table.getRowSorter().convertRowIndexToModel(r);
            JLabel rendComp = (JLabel) super.getTableCellRendererComponent(table, v, selected, focus, actualRow, c);
            Object sortedValue = table.getValueAt(actualRow, c);
            SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            rendComp.setText(formatter.format(sortedValue));
//            System.out.println(formatter.format(sortedValue));
            return rendComp;
        }
    }


    public class OfflimbImagesTableModel extends DefaultTableModel
    {
        public OfflimbImagesTableModel(Object[][] data, String[] columnNames)
        {
            super(data, columnNames);
        }

        public boolean isCellEditable(int row, int column)
        {
            // Only allow editing the hide column if the image is mapped
            if (column == offlimbTableView.getShowFootprintColumnIndex() || column == offlimbTableView.getOffLimbIndex() || column == offlimbTableView.getFrusColumnIndex())
            {
                String name = imageRawResults.get(row).get(0);
                ImageKeyInterface key = imageSearchModel.createImageKey(name.substring(0, name.length()-4), imageSearchModel.getImageSourceOfLastQuery(), instrument);
                ImageCollection images = (ImageCollection)imageSearchModel.getModelManager().getModel(imageSearchModel.getImageCollectionModelName()).get(0);
                return images.containsImage(key);
            }
            else
            {
                return column == offlimbTableView.getMapColumnIndex() || column == offlimbTableView.getBndrColumnIndex();
            }
        }

        @Override
        public Object getValueAt(int row, int column)
        {
        	if (column == offlimbTableView.getDateColumnIndex())
        	{
    			int actualRow = offlimbTableView.getResultList().getRowSorter().convertRowIndexToModel(row);
                String dtStr = imageRawResults.get(actualRow).get(1);
                Date dt = new Date(Long.parseLong(dtStr));
				return dt;
        	}
        	else
        		return super.getValueAt(row, column);
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            if (columnIndex <= offlimbTableView.getBndrColumnIndex())
                return Boolean.class;
            else if (columnIndex == offlimbTableView.getIdColumnIndex())
            	return Integer.class;
            else if (columnIndex == offlimbTableView.getDateColumnIndex())
            	return Date.class;
            else
                return String.class;
        }
    }
}
