package edu.jhuapl.sbmt.image.ui.table;

import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImage;
import edu.jhuapl.sbmt.image.interfaces.IPerspectiveImageTableRepresentable;
import edu.jhuapl.sbmt.image.model.PerspectiveImageCollection;
import edu.jhuapl.sbmt.image.ui.color.SingleImagePreviewPanel.PerspectiveImageTransferable;
import glum.gui.GuiUtil;
import glum.gui.action.PopupMenu;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.TablePopupHandler;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import glum.item.ItemManagerUtil;

public class ImageListTableView<G1 extends IPerspectiveImage & IPerspectiveImageTableRepresentable> extends JPanel
{
	private JButton newImageButton;

	/**
	 * JButton to load image list from file
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

	private JButton galleryButton;


	/**
	 * JButton to save image list to file
	 */
	private JButton saveImageButton;

	private JButton colorImageButton;

	private JButton imageCubeButton;

	private JButton previousBoundariesButton, nextBoundariesButton;
	private JTextField boundaryOffsetTextField;

	protected JTable resultList;
	private JLabel resultsLabel;

	// for table
	private JButton selectAllB, selectInvertB, selectNoneB;
	private PerspectiveImageCollection<G1> imageCollection;
	private ItemListPanel<G1> imageILP;
	private ItemHandler<G1> imageItemHandler;

	private PopupMenu<G1> popupMenu;

	public ImageListTableView(PerspectiveImageCollection<G1> collection, PopupMenu<G1> popupMenu)
	{
		this.imageCollection = collection;
		this.popupMenu = popupMenu;
		this.imageCollection.addPropertyChangeListener(new PropertyChangeListener()
		{

			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				resultsLabel.setText(imageCollection.getNumItems() + " Results");
				resultList.repaint();
			}
		});
		this.imageCollection.addListener(new ItemEventListener()
		{

			@Override
			public void handleItemEvent(Object aSource, ItemEventType aEventType)
			{
				resultsLabel.setText(imageCollection.getNumItems() + " Results");
				resultList.repaint();
			}
		});
		init();
	}

	protected void init()
	{
		resultsLabel = new JLabel(imageCollection.size() + " Result(s)");
		resultList = buildTable();
	}

	public void setup()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new TitledBorder(null, "Available Images", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		JPanel panel_4 = new JPanel();
		add(panel_4);
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

		panel_4.add(resultsLabel);

		Component horizontalGlue = Box.createHorizontalGlue();
		panel_4.add(horizontalGlue);

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

		newImageButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.fileIcon"));
		newImageButton.setToolTipText(ToolTipUtil.getCustomImage());

		loadImageButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.directoryIcon"));
		loadImageButton.setToolTipText(ToolTipUtil.getItemLoadImageList());

		saveImageButton = GuiUtil.formButton(listener, UIManager.getIcon("FileView.hardDriveIcon"));
		saveImageButton.setToolTipText(ToolTipUtil.getItemSaveImageList());
		saveImageButton.setEnabled(false);

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
//		imageCubeButton.setToolTipText(ToolTipUtil.getImageCube());
		imageCubeButton.setToolTipText("This feature is currently disabled");
		imageCubeButton.setEnabled(false);

		galleryButton = GuiUtil.formButton(listener, IconUtil.getGallery());
		galleryButton.setToolTipText(ToolTipUtil.getGallery());
		galleryButton.setEnabled(false);

		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setLayout(new BoxLayout(topButtonPanel, BoxLayout.X_AXIS));
		topButtonPanel.add(loadImageButton);
		topButtonPanel.add(saveImageButton);
		topButtonPanel.add(galleryButton);
		topButtonPanel.add(Box.createHorizontalGlue());
		topButtonPanel.add(colorImageButton);
		topButtonPanel.add(imageCubeButton);
		add(topButtonPanel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		buttonPanel.add(showImageButton);
		buttonPanel.add(hideImageButton);
		buttonPanel.add(showImageBorderButton);
		buttonPanel.add(hideImageBorderButton);

		buttonPanel.add(Box.createHorizontalStrut(50));
		buttonPanel.add(new JLabel("Bndr:"));
		boundaryOffsetTextField = new JTextField("10");
		boundaryOffsetTextField.setMaximumSize( new Dimension(100, boundaryOffsetTextField.getPreferredSize().height) );
		buttonPanel.add(boundaryOffsetTextField);
		previousBoundariesButton = new JButton("<");
		nextBoundariesButton = new JButton(">");
		buttonPanel.add(previousBoundariesButton);
		buttonPanel.add(nextBoundariesButton);

		buttonPanel.add(Box.createHorizontalStrut(50));
		buttonPanel.add(selectInvertB, "w 24!,h 24!");
		buttonPanel.add(selectNoneB, "w 24!,h 24!");
		buttonPanel.add(selectAllB, "w 24!,h 24!,wrap 2");
		add(buttonPanel);

		// Table Content
		QueryComposer<ImageColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(ImageColumnLookup.Map, Boolean.class, "Map", null);
		tmpComposer.addAttribute(ImageColumnLookup.Status, String.class, "Status", null);
		tmpComposer.addAttribute(ImageColumnLookup.Offlimb, Boolean.class, "Offlimb", null);
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
		imageTable.setTransferHandler(new PerspectiveImageTransferHandler());
		imageTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		imageTable.addMouseListener(new TablePopupHandler(imageCollection, popupMenu));
		return imageTable;
	}

	public JTable getResultList()
	{
		return resultList;
	}

	public JLabel getResultsLabel()
	{
		return resultsLabel;
	}

	public void setResultsLabel(JLabel resultsLabel)
	{
		this.resultsLabel = resultsLabel;
	}

	public ItemHandler<G1> getTableHandler()
	{
		return imageItemHandler;
	}

	public JButton getPreviousBoundariesButton()
	{
		return previousBoundariesButton;
	}

	public JButton getNextBoundariesButton()
	{
		return nextBoundariesButton;
	}

	public JTextField getBoundaryOffsetTextField()
	{
		return boundaryOffsetTextField;
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

	public JButton getGalleryButton()
	{
		return galleryButton;
	}

	public class PerspectiveImageTransferHandler extends TransferHandler
	{

		@Override
		protected void exportDone(JComponent arg0, Transferable arg1, int arg2)
		{
			// TODO Auto-generated method stub
			super.exportDone(arg0, arg1, arg2);
		}

		@Override
        protected Transferable createTransferable(JComponent c) {
			return new PerspectiveImageTransferable((G1)imageCollection.getSelectedItems().asList().get(0));
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