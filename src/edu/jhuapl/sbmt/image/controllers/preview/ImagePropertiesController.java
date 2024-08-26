package edu.jhuapl.sbmt.image.controllers.preview;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.sbmt.image.model.ImageProperty;
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
		switch (aEnum)
		{
			case Property:
				return property.property();
			case Value:
				return property.value();
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
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
//		gridBagConstraints.gridwidth = 3;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		setLayout(new GridBagLayout());

		properties.sort(new Comparator<ImageProperty>()
		{

			@Override
			public int compare(ImageProperty o1, ImageProperty o2)
			{
				return o1.property().compareTo(o2.property());
			}
		});

		QueryComposer<ImagePropertiesColumnLookup> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(ImagePropertiesColumnLookup.Property, String.class, "Property", null);
		tmpComposer.addAttribute(ImagePropertiesColumnLookup.Value, String.class, "Value", null);

		ImagePropertiesItemHandler imagePropertiesTableHandler = new ImagePropertiesItemHandler(properties, tmpComposer);
		ItemProcessor<ImageProperty> tmpIP = new ItemProcessor<ImageProperty>()
		{
			@Override
			public void addListener(ItemEventListener aListener) {}

			@Override
			public void delListener(ItemEventListener aListener) {}

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
		JTable propertiesTable = imagePropertiesILP.getTable();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(500, 300));
        add(scrollPane, gridBagConstraints);

        scrollPane.setViewportView(propertiesTable);
	}
}
