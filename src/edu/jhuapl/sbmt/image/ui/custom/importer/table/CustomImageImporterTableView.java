package edu.jhuapl.sbmt.image.ui.custom.importer.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;

import glum.gui.GuiUtil;
import glum.gui.action.PopupMenu;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.item.BaseItemManager;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import glum.item.ItemManagerUtil;

public class CustomImageImporterTableView<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends JPanel
{
	protected JTable resultList;

	// for table
	private JButton selectAllB, selectInvertB, selectNoneB, deleteImageButton, editImageButton, loadImageButton;
	private BaseItemManager<G1> imageCollection;
	private ItemListPanel<G1> imageILP;
	private ItemHandler<G1> imageItemHandler;
	private String type = "Perspective Projection";
	private JScrollPane scrollPane;

	private PopupMenu<G1> popupMenu;

	public CustomImageImporterTableView(BaseItemManager<G1> collection, PopupMenu<G1> popupMenu)
	{
		this.imageCollection = collection;
		this.popupMenu = popupMenu;
//		collection.addPropertyChangeListener(new PropertyChangeListener()
//		{
//
//			@Override
//			public void propertyChange(PropertyChangeEvent evt)
//			{
////				resultsLabel.setText(imageCollection.size() + " Results");
//				resultList.repaint();
//			}
//		});
		collection.addListener(new ItemEventListener()
		{

			@Override
			public void handleItemEvent(Object aSource, ItemEventType aEventType)
			{
				editImageButton.setEnabled(imageCollection.getSelectedItems().size() == 1);
				resultList.repaint();
			}
		});

		init();
	}

	protected void init()
	{
//		resultsLabel = new JLabel(imageCollection.size() + " Result(s)");
		buildTopRow();
		resultList = buildTable();
	}

	public void setup()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder(null, "Custom Images", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scrollPane = new JScrollPane();
		add(scrollPane);

		scrollPane.setViewportView(resultList);
	}

	private void buildTopRow()
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

//		loadImageButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.directoryIcon"));
		loadImageButton = GuiUtil.formButton(listener, IconUtil.getItemAdd());
		loadImageButton.setToolTipText(ToolTipUtil.getCustomImage());

		deleteImageButton = GuiUtil.formButton(listener, IconUtil.getItemDel());
		deleteImageButton.setToolTipText(ToolTipUtil.getItemDel());
		deleteImageButton.setEnabled(false);

		editImageButton = GuiUtil.formButton(listener, IconUtil.getItemEdit());
		editImageButton.setToolTipText(ToolTipUtil.getItemEdit());
		editImageButton.setEnabled(false);

		selectInvertB = GuiUtil.formButton(listener, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(listener, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(listener, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		buttonPanel.add(loadImageButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(editImageButton);
		buttonPanel.add(deleteImageButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(selectInvertB, "w 24!,h 24!");
		buttonPanel.add(selectNoneB, "w 24!,h 24!");
		buttonPanel.add(selectAllB, "w 24!,h 24!,wrap 2");
		add(buttonPanel);
	}

	private JTable buildTable()
	{
		// Table Content
		QueryComposer<CustomImageImporterColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(CustomImageImporterColumnLookup.IMAGE_PATH, Boolean.class, "Path", null);
		tmpComposer.addAttribute(CustomImageImporterColumnLookup.IMAGE_NAME, String.class, "Name", null);

		if (type.equals("Perspective Projection"))
		{
			tmpComposer.addAttribute(CustomImageImporterColumnLookup.POINTING_FILE, Boolean.class, "Pointing", null);
			tmpComposer.addAttribute(CustomImageImporterColumnLookup.IMAGE_ROTATION, Boolean.class, "Rot (deg)", null);
			tmpComposer.addAttribute(CustomImageImporterColumnLookup.IMAGE_FLIP, Boolean.class, "Flip", null);

			tmpComposer.getItem(CustomImageImporterColumnLookup.POINTING_FILE).defaultSize *= 2;
		}
		else
		{
			tmpComposer.addAttribute(CustomImageImporterColumnLookup.LATITUDE_MIN, Integer.class, "Min Lat (deg)", null);
			tmpComposer.addAttribute(CustomImageImporterColumnLookup.LATITUDE_MAX, String.class, "Max Lat (deg)", null);
			tmpComposer.addAttribute(CustomImageImporterColumnLookup.LONGITUDE_MIN, Integer.class, "Min Lon (deg)", null);
			tmpComposer.addAttribute(CustomImageImporterColumnLookup.LONGITUDE_MAX, Date.class, "Max Lon (deg)", null);
			tmpComposer.addAttribute(CustomImageImporterColumnLookup.IMAGE_FLIP_ABOUT_X, Boolean.class, "Invert About X?", null);
			tmpComposer.setEditor(CustomImageImporterColumnLookup.IMAGE_FLIP_ABOUT_X, new BooleanCellEditor());
			tmpComposer.setRenderer(CustomImageImporterColumnLookup.IMAGE_FLIP_ABOUT_X, new BooleanCellRenderer());
		}
//		tmpComposer.addAttribute(ImageColumnLookup.Source, String.class, "Source", null);

//		tmpComposer.setEditor(ImageColumnLookup.Map, new BooleanCellEditor());
//		tmpComposer.setRenderer(ImageColumnLookup.Map, new BooleanCellRenderer());
//		tmpComposer.setEditor(ImageColumnLookup.Offlimb, new BooleanCellEditor());
//		tmpComposer.setRenderer(ImageColumnLookup.Offlimb, new BooleanCellRenderer());
//		tmpComposer.setEditor(ImageColumnLookup.Frustum, new BooleanCellEditor());
//		tmpComposer.setRenderer(ImageColumnLookup.Frustum, new BooleanCellRenderer());
//		tmpComposer.setEditor(ImageColumnLookup.Boundary, new BooleanCellEditor());
//		tmpComposer.setRenderer(ImageColumnLookup.Boundary, new BooleanCellRenderer());

		tmpComposer.getItem(CustomImageImporterColumnLookup.IMAGE_PATH).defaultSize *= 3;
		tmpComposer.getItem(CustomImageImporterColumnLookup.IMAGE_NAME).defaultSize *= 4;


		CustomImageImporterItemHandler<G1> imageItemHandler = new CustomImageImporterItemHandler<G1>(imageCollection, tmpComposer);
		ItemProcessor<G1> tmpIP = imageCollection;
		imageILP = new ItemListPanel<>(imageItemHandler, tmpIP, true);
		imageILP.setSortingEnabled(true);
		JTable imageTable = imageILP.getTable();
		imageTable.setDragEnabled(true);
//		imageTable.setTransferHandler(new CustomImageTransferHandler());
		imageTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		imageTable.addMouseListener(new TablePopupHandler(imageCollection, popupMenu));
		return imageTable;
	}

	public JTable getResultList()
	{
		return resultList;
	}

	public ItemHandler<G1> getTableHandler()
	{
		return imageItemHandler;
	}

	public void setType(String type)
	{
		this.type = type;
		resultList = buildTable();
		scrollPane.setViewportView(resultList);
		repaint();
		validate();
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

	/**
	 * @return the loadImageButton
	 */
	public JButton getLoadImageButton()
	{
		return loadImageButton;
	}
}