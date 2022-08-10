package edu.jhuapl.sbmt.image.gui.ui.images;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class OfflimbImageResultsTable extends ImageResultsTable
{
    protected int offLimbIndex;

    public OfflimbImageResultsTable()
    {
        mapColumnIndex = 0;
        showFootprintColumnIndex = 1;
        offLimbIndex = 2;
        frusColumnIndex = 3;
        bndrColumnIndex = 4;
        idColumnIndex = 5;
        filenameColumnIndex = 6;
        dateColumnIndex = 7;

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    public int getOffLimbIndex()
    {
        return offLimbIndex;
    }
}
