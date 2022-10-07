package edu.jhuapl.sbmt.image.gui.ui.images;

import edu.jhuapl.sbmt.core.image.ImagingInstrument;
import edu.jhuapl.sbmt.image.model.ImageCollection;

public class OfflimbImageResultsTableView extends ImageResultsTableView
{
//    JButton offlimbControlsButton;

    /**
     * @wbp.parser.constructor
     */
    public OfflimbImageResultsTableView(ImagingInstrument instrument, ImageCollection imageCollection, ImagePopupMenu imagePopupMenu)
    {
        super(instrument, imageCollection, imagePopupMenu);
        resultList = new OfflimbImageResultsTable();
    }

//    @Override
//    protected void init()
//    {
//        // TODO Auto-generated method stub
//        super.init();
//        offlimbControlsButton = new JButton("Offlimb Settings");
//    }

//    @Override
//    public void setup()
//    {
//        // TODO Auto-generated method stub
//        super.setup();
//        buttonPanel3.add(offlimbControlsButton);
//    }

    public int getOffLimbIndex()
    {
        return ((OfflimbImageResultsTable)resultList).getOffLimbIndex();
    }

//    public JButton getOfflimbControlsButton()
//    {
//        return offlimbControlsButton;
//    }
}
