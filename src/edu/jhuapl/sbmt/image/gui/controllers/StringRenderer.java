package edu.jhuapl.sbmt.image.gui.controllers;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.sbmt.core.image.Image;
import edu.jhuapl.sbmt.core.image.ImageKeyInterface;
import edu.jhuapl.sbmt.image.gui.model.images.ImageSearchModel;

public class StringRenderer extends DefaultTableCellRenderer
{
    private ImageSearchModel imageSearchModel;
    private List<List<String>> imageRawResults;

    public StringRenderer(ImageSearchModel imageSearchModel, List<List<String>> imageRawResults)
    {
        this.imageSearchModel = imageSearchModel;
        this.imageRawResults = imageRawResults;
    }

    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column)
    {
    	int actualRow = row;
    	if (table.getRowSorter() != null)
    		actualRow = table.getRowSorter().convertRowIndexToModel(row);
        Component co = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, actualRow, column);
        if (imageRawResults.size() == 0 || imageRawResults.get(actualRow).size() == 0) return co;
        String name = imageRawResults.get(actualRow).get(0);
        String filename = FilenameUtils.getBaseName(name);
        ImageKeyInterface key = imageSearchModel.createImageKey(filename, imageSearchModel.getImageSourceOfLastQuery(), imageSearchModel.getInstrument());
        Image image = imageSearchModel.getImageCollection().getImage(key);
        if (image == null)
    	{
        	co.setForeground(table.getForeground());
            co.setBackground(table.getBackground());
        	return co;
    	}
        Color borderColor = imageSearchModel.getImageCollection().getImage(key).getBoundaryColor();

        if (image.isBoundaryVisible() == true)
        {
            if (isSelected)
            {
                co.setForeground(borderColor);
                co.setBackground(table.getSelectionBackground());
            }
            else
            {
                co.setForeground(borderColor);
                co.setBackground(table.getBackground());
            }
        }
        else
        {
            if (isSelected)
            {
                co.setForeground(table.getSelectionForeground());
                co.setBackground(table.getSelectionBackground());
            }
            else
            {
                co.setForeground(table.getForeground());
                co.setBackground(table.getBackground());
            }
        }

        return co;
    }

    public void setImageRawResults(List<List<String>> imageRawResults)
    {
        this.imageRawResults = imageRawResults;
    }
}