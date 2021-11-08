package edu.jhuapl.sbmt.image.gui.images;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

public class ImageResultsTable extends JTable
{
    protected int mapColumnIndex,showFootprintColumnIndex,frusColumnIndex,bndrColumnIndex,dateColumnIndex,idColumnIndex,filenameColumnIndex;

    public ImageResultsTable()
    {
        mapColumnIndex=0;
        showFootprintColumnIndex=1;
        frusColumnIndex=2;
        bndrColumnIndex=3;
        idColumnIndex=4;
        filenameColumnIndex=5;
        dateColumnIndex=6;

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    public int getMapColumnIndex()
    {
        return mapColumnIndex;
    }

    public int getShowFootprintColumnIndex()
    {
        return showFootprintColumnIndex;
    }

    public int getFrusColumnIndex()
    {
        return frusColumnIndex;
    }

    public int getBndrColumnIndex()
    {
        return bndrColumnIndex;
    }

    public int getDateColumnIndex()
    {
        return dateColumnIndex;
    }

    public int getIdColumnIndex()
    {
        return idColumnIndex;
    }

    public int getFilenameColumnIndex()
    {
        return filenameColumnIndex;
    }
}
