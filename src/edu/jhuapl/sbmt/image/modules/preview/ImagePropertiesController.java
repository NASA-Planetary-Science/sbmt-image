package edu.jhuapl.sbmt.image.modules.preview;

import java.awt.Dimension;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.google.common.collect.ImmutableList;

import glum.gui.panel.itemList.BasicItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.item.ItemEventListener;

public class ImagePropertiesController
{
	ImagePropertiesTableView tableView;

	public ImagePropertiesController(List<ImageProperty> properties)
	{
		this.tableView = new ImagePropertiesTableView(properties);
	}

	public JPanel getView()
	{
		return tableView;
	}

}

enum ImagePropertiesColumnLookup
{
	Property,
	Value
}

class ImagePropertiesItemHandler extends BasicItemHandler<ImageProperty, ImagePropertiesColumnLookup>
{
	private List<ImageProperty> properties;

	public ImagePropertiesItemHandler(List<ImageProperty> properties, QueryComposer<ImagePropertiesColumnLookup> aComposer)
	{
		super(aComposer);

		this.properties = properties;
	}

	@Override
	public Object getColumnValue(ImageProperty property, ImagePropertiesColumnLookup aEnum)
	{
		//TODO: Switch to using an index so the get all items doesn't take so long to look up
		switch (aEnum)
		{
			case Property:
				return property.getProperty();
			case Value:
				return property.getValue();
			default:
				break;
		}

		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}

	@Override
	public void setColumnValue(ImageProperty property, ImagePropertiesColumnLookup aEnum, Object aValue)
	{
		throw new UnsupportedOperationException("Column is not supported. Enum: " + aEnum);
	}
}

class ImagePropertiesTableView extends JPanel
{
	protected JTable table;
	private List<ImageProperty> properties;

	public ImagePropertiesTableView(List<ImageProperty> properties)
	{
		this.properties = properties;
		init();
	}

	protected void init()
	{
		BoxLayout boxLayout = new BoxLayout(this, BoxLayout.X_AXIS);
		setLayout(boxLayout);

		QueryComposer<ImagePropertiesColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(ImagePropertiesColumnLookup.Property, String.class, "Property", null);
		tmpComposer.addAttribute(ImagePropertiesColumnLookup.Value, String.class, "Value", null);

		ImagePropertiesItemHandler imagePropertiesTableHandler = new ImagePropertiesItemHandler(properties, tmpComposer);
		ItemProcessor<ImageProperty> tmpIP = new ItemProcessor<ImageProperty>()
		{

			@Override
			public void addListener(ItemEventListener aListener)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void delListener(ItemEventListener aListener)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public ImmutableList<ImageProperty> getAllItems()
			{
				return ImmutableList.copyOf(ImagePropertiesTableView.this.properties);
			}

			@Override
			public int getNumItems()
			{
				return ImagePropertiesTableView.this.properties.size();
			}
		};
		ItemListPanel<ImageProperty> imagePropertiesILP =
				new ItemListPanel<>(imagePropertiesTableHandler, tmpIP, true);
		imagePropertiesILP.setSortingEnabled(true);
//		configureColumnWidths();
		JTable propertiesTable = imagePropertiesILP.getTable();
//		propertiesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		propertiesTable.addMouseListener(new SpectrumTablePopupListener<>(spectrumCollection, boundaryCollection, spectrumPopupMenu, spectrumTable));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(452, 200));
        add(scrollPane);

        scrollPane.setViewportView(propertiesTable);

	}











}
