package edu.jhuapl.sbmt.image.ui.custom.table;

import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.ui.table.ImageColumnLookup;
import edu.jhuapl.sbmt.image.ui.table.ImageListItemHandler;
import glum.gui.GuiUtil;
import glum.gui.action.PopupMenu;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.TablePopupHandler;
import glum.item.ItemManagerUtil;

public class CustomImageListTableView<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends JPanel
{
	private JButton newImageButton;

	/**
	 * JButton to load image listfrom file
	 */
	private JButton loadImageButton;

	/**
	 * JButton to remove image from table
	 */
	private JButton hideImageButton;

	/**
	 * JButton to show image in renderer
	 */
	private JButton showImageButton;

	/**
	 * JButton to remove image border from table
	 */
	private JButton hideImageBorderButton;

	/**
	 * JButton to show image border in renderer
	 */
	private JButton showImageBorderButton;

	/**
	 * JButton to save image list to file
	 */
	private JButton saveImageButton;

	private JButton colorImageButton;

	private JButton imageCubeButton;

	protected JTable resultList;
//	private JLabel resultsLabel;

	// for table
	private JButton selectAllB, selectInvertB, selectNoneB, deleteImageButton, editImageButton;
	private PerspectiveImageCollection<G1> imageCollection;
	private ItemListPanel<G1> imageILP;
	private ItemHandler<G1> imageItemHandler;

	private PopupMenu<G1> popupMenu;

	public CustomImageListTableView(PerspectiveImageCollection<G1> collection, PopupMenu<G1> popupMenu)
	{
		this.imageCollection = collection;
		this.popupMenu = popupMenu;
		collection.addPropertyChangeListener(new PropertyChangeListener()
		{

			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
//				resultsLabel.setText(imageCollection.size() + " Results");
				resultList.repaint();
			}
		});
		init();
	}

	protected void init()
	{
//		resultsLabel = new JLabel(imageCollection.size() + " Result(s)");
		resultList = buildTable();
	}

	public void setup()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder(null, "Custom Images", TitledBorder.LEADING, TitledBorder.TOP, null, null));
//		JPanel panel_4 = new JPanel();
//		add(panel_4);
//		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));
//
//		panel_4.add(resultsLabel);
//
//		Component horizontalGlue = Box.createHorizontalGlue();
//		panel_4.add(horizontalGlue);

		JScrollPane scrollPane = new JScrollPane();
//        scrollPane.setPreferredSize(new java.awt.Dimension(150, 250));
		add(scrollPane);

		scrollPane.setViewportView(resultList);
	}

	private JTable buildTable()
	{
		ActionListener listener = new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object source = e.getSource();

//				List<G1> tmpL = imageCollection.getSelectedItems().asList();
				if (source == selectAllB)
					ItemManagerUtil.selectAll(imageCollection);
				else if (source == selectNoneB)
					ItemManagerUtil.selectNone(imageCollection);
				else if (source == selectInvertB)
				{
					ItemManagerUtil.selectInvert(imageCollection);
				}
			}
		};

		// Table header

//		newImageButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.fileIcon"));
		newImageButton = GuiUtil.formButton(listener, IconUtil.getItemAdd());
		newImageButton.setToolTipText(ToolTipUtil.getCustomImage());

		deleteImageButton = GuiUtil.formButton(listener, IconUtil.getItemDel());
		deleteImageButton.setToolTipText(ToolTipUtil.getItemDel());
		deleteImageButton.setEnabled(false);

		editImageButton = GuiUtil.formButton(listener, IconUtil.getItemEdit());
		editImageButton.setToolTipText(ToolTipUtil.getItemEdit());
		editImageButton.setEnabled(false);

		loadImageButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.directoryIcon"));
		loadImageButton.setToolTipText(ToolTipUtil.getItemLoadCustomImageList());

		saveImageButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.hardDriveIcon"));
		saveImageButton.setToolTipText(ToolTipUtil.getItemSaveCustomImageList());
//		saveImageButton.setEnabled(false);

		showImageButton = GuiUtil.formButton(listener, IconUtil.getItemShow());
		showImageButton.setToolTipText(ToolTipUtil.getItemShow());
		showImageButton.setEnabled(false);

		hideImageButton = GuiUtil.formButton(listener, IconUtil.getItemHide());
		hideImageButton.setToolTipText(ToolTipUtil.getItemHide());
		hideImageButton.setEnabled(false);

		showImageBorderButton = GuiUtil.formButton(listener, IconUtil.getShowBorder());
		showImageBorderButton.setToolTipText(ToolTipUtil.getShowBorder());
		showImageBorderButton.setEnabled(false);

		hideImageBorderButton = GuiUtil.formButton(listener, IconUtil.getHideBorder());
		hideImageBorderButton.setToolTipText(ToolTipUtil.getHideBorder());
		hideImageBorderButton.setEnabled(false);

		selectInvertB = GuiUtil.formButton(listener, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(listener, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(listener, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

		colorImageButton = GuiUtil.formButton(listener, IconUtil.getColor());
		colorImageButton.setToolTipText(ToolTipUtil.getColorImage());

		imageCubeButton = GuiUtil.formButton(listener, IconUtil.getLayers());
		imageCubeButton.setToolTipText(ToolTipUtil.getImageCube());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		buttonPanel.add(newImageButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(loadImageButton);
		buttonPanel.add(saveImageButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(showImageButton);
		buttonPanel.add(hideImageButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(editImageButton);
		buttonPanel.add(deleteImageButton);
//		buttonPanel.add(colorImageButton);
//		buttonPanel.add(imageCubeButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(selectInvertB, "w 24!,h 24!");
		buttonPanel.add(selectNoneB, "w 24!,h 24!");
		buttonPanel.add(selectAllB, "w 24!,h 24!,wrap 2");
		add(buttonPanel);

		// Table Content
		QueryComposer<ImageColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(ImageColumnLookup.Map, Boolean.class, "Map", null);
		tmpComposer.addAttribute(ImageColumnLookup.Status, String.class, "Status", null);
		tmpComposer.addAttribute(ImageColumnLookup.Offlimb, Boolean.class, "Off", null);
		tmpComposer.addAttribute(ImageColumnLookup.Frustum, Boolean.class, "Frus", null);
		tmpComposer.addAttribute(ImageColumnLookup.Boundary, Boolean.class, "Bndr", null);
		tmpComposer.addAttribute(ImageColumnLookup.Id, Integer.class, "ID", null);
		tmpComposer.addAttribute(ImageColumnLookup.Filename, String.class, "Name", null);
		tmpComposer.addAttribute(ImageColumnLookup.Dimension, Integer.class, "Dim.", null);
		tmpComposer.addAttribute(ImageColumnLookup.Date, Date.class, "Date (UTC)", null);
//		tmpComposer.addAttribute(ImageColumnLookup.Source, String.class, "Source", null);

		tmpComposer.setEditor(ImageColumnLookup.Map, new BooleanCellEditor());
		tmpComposer.setRenderer(ImageColumnLookup.Map, new BooleanCellRenderer());
		tmpComposer.setEditor(ImageColumnLookup.Offlimb, new BooleanCellEditor());
		tmpComposer.setRenderer(ImageColumnLookup.Offlimb, new BooleanCellRenderer());
		tmpComposer.setEditor(ImageColumnLookup.Frustum, new BooleanCellEditor());
		tmpComposer.setRenderer(ImageColumnLookup.Frustum, new BooleanCellRenderer());
		tmpComposer.setEditor(ImageColumnLookup.Boundary, new BooleanCellEditor());
		tmpComposer.setRenderer(ImageColumnLookup.Boundary, new BooleanCellRenderer());

		tmpComposer.getItem(ImageColumnLookup.Status).defaultSize *= 2;
		tmpComposer.getItem(ImageColumnLookup.Filename).defaultSize *= 4;
		tmpComposer.getItem(ImageColumnLookup.Date).defaultSize *= 2;

		ImageListItemHandler<G1> imageItemHandler = new ImageListItemHandler<G1>(imageCollection, tmpComposer);
		ItemProcessor<G1> tmpIP = imageCollection;
		imageILP = new ItemListPanel<G1>(imageItemHandler, tmpIP, true);
		imageILP.setSortingEnabled(true);
		JTable imageTable = imageILP.getTable();
		imageTable.setDragEnabled(true);
		imageTable.setTransferHandler(new CustomImageTransferHandler());
		imageTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		imageTable.addMouseListener(new TablePopupHandler(imageCollection, popupMenu));
		return imageTable;
	}

	public JTable getResultList()
	{
		return resultList;
	}

//	public JLabel getResultsLabel()
//	{
//		return resultsLabel;
//	}
//
//	public void setResultsLabel(JLabel resultsLabel)
//	{
//		this.resultsLabel = resultsLabel;
//	}

	public ItemHandler<G1> getTableHandler()
	{
		return imageItemHandler;
	}

	/**
	 * @return the loadImageButton
	 */
	public JButton getLoadImageButton()
	{
		return loadImageButton;
	}

	/**
	 * @return the hideImageButton
	 */
	public JButton getHideImageButton()
	{
		return hideImageButton;
	}

	/**
	 * @return the showImageButton
	 */
	public JButton getShowImageButton()
	{
		return showImageButton;
	}

	/**
	 * @return the hideImageButton
	 */
	public JButton getHideImageBorderButton()
	{
		return hideImageBorderButton;
	}

	/**
	 * @return the showImageButton
	 */
	public JButton getShowImageBorderButton()
	{
		return showImageBorderButton;
	}

	/**
	 * @return the saveImageButton
	 */
	public JButton getSaveImageButton()
	{
		return saveImageButton;
	}

	public JButton getColorImageButton()
	{
		return colorImageButton;
	}

	public JButton getImageCubeButton()
	{
		return imageCubeButton;
	}

	public JButton getNewImageButton()
	{
		return newImageButton;
	}

	/**
	 * @return the deleteImageButton
	 */
	public JButton getDeleteImageButton()
	{
		return deleteImageButton;
	}

	/**
	 * @return the editImageButton
	 */
	public JButton getEditImageButton()
	{
		return editImageButton;
	}

	public class CustomImageTransferHandler extends TransferHandler
	{

		@Override
		protected void exportDone(JComponent arg0, Transferable arg1, int arg2)
		{
			// TODO Auto-generated method stub
			super.exportDone(arg0, arg1, arg2);
		}

		@Override
        protected Transferable createTransferable(JComponent c) {
			return null;
//			return new CustomImageTransferable(imageCollection.getSelectedItems().asList().get(0));
        }

		public int getSourceActions(JComponent c)
		{
			return COPY_OR_MOVE;
		}

		protected void cleanup(JComponent c, boolean remove)
		{

		}
	}
}