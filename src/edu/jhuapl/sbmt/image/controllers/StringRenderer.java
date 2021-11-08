package edu.jhuapl.sbmt.image.controllers;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.io.FilenameUtils;

import edu.jhuapl.sbmt.image.common.ImageKeyInterface;
import edu.jhuapl.sbmt.image.types.ImageSearchModel;
import edu.jhuapl.sbmt.image.types.perspectiveImage.PerspectiveImageBoundaryCollection;

public class StringRenderer extends DefaultTableCellRenderer
{
    public PerspectiveImageBoundaryCollection model;
    private ImageSearchModel imageSearchModel;
    private List<List<String>> imageRawResults;

    public StringRenderer(ImageSearchModel imageSearchModel, List<List<String>> imageRawResults)
    {
        this.imageSearchModel = imageSearchModel;
        this.imageRawResults = imageRawResults;
        model = (PerspectiveImageBoundaryCollection)imageSearchModel.getModelManager().getModel(imageSearchModel.getImageBoundaryCollectionModelName()).get(0);

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
//        ImageKey key = new ImageKey(name.substring(0, name.length()-4), sourceOfLastQuery, instrument);
//        String filename = name.substring(0, name.lastIndexOf("."));
        String filename = FilenameUtils.getBaseName(name);
        ImageKeyInterface key = imageSearchModel.createImageKey(filename, imageSearchModel.getImageSourceOfLastQuery(), imageSearchModel.getInstrument());
        if (model.containsBoundary(key))
        {
            int[] c = model.getBoundary(key).getBoundaryColor();
            if (isSelected)
            {
                co.setForeground(new Color(c[0], c[1], c[2]));
                co.setBackground(table.getSelectionBackground());
            }
            else
            {
                co.setForeground(new Color(c[0], c[1], c[2]));
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