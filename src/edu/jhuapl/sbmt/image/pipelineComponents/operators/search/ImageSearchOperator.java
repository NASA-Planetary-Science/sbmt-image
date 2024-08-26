package edu.jhuapl.sbmt.image.pipelineComponents.operators.search;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.Triple;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.sbmt.core.body.SmallBodyModel;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.core.search.HierarchicalSearchSpecification.Selection;
import edu.jhuapl.sbmt.image.config.ImagingInstrumentConfig;
import edu.jhuapl.sbmt.image.interfaces.IImagingInstrument;
import edu.jhuapl.sbmt.image.model.ImageSearchParametersModel;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;
import edu.jhuapl.sbmt.query.database.ImageDatabaseSearchMetadata;
import edu.jhuapl.sbmt.query.fixedlist.FixedListSearchMetadata;
import edu.jhuapl.sbmt.query.v2.FixedListDataQuery;
import edu.jhuapl.sbmt.query.v2.QueryException;
import vtk.vtkPolyData;

public class ImageSearchOperator extends BasePipelineOperator<ImageSearchParametersModel, Triple<List<List<String>>, IImagingInstrument, PointingSource>>
{
	private ImageSearchParametersModel searchParameterModel;
	private ImagingInstrumentConfig config;
	private PickManager refPickManager;
	private ModelManager modelManager;

	public ImageSearchOperator(ImagingInstrumentConfig config, ModelManager modelManager, PickManager aPickManager)
	{
		this.modelManager = modelManager;
		this.config = config;
		this.refPickManager = aPickManager;
	}

	@Override
	public void processData() throws IOException, Exception
	{
		this.searchParameterModel = inputs.get(0);
		runSearch();
	}

	private void runSearch() throws QueryException
	{
		double minDistanceQuery = searchParameterModel.getMinDistanceQuery();
		double maxDistanceQuery = searchParameterModel.getMaxDistanceQuery();
		double minEmissionQuery = searchParameterModel.getMinEmissionQuery();
		double maxEmissionQuery = searchParameterModel.getMaxEmissionQuery();
		double minIncidenceQuery = searchParameterModel.getMinIncidenceQuery();
		double maxIncidenceQuery = searchParameterModel.getMaxIncidenceQuery();
		double minPhaseQuery = searchParameterModel.getMinPhaseQuery();
		double maxPhaseQuery = searchParameterModel.getMaxPhaseQuery();
		double minResolutionQuery = searchParameterModel.getMinResolutionQuery();
		double maxResolutionQuery = searchParameterModel.getMaxResolutionQuery();
		List<Integer> camerasSelected = searchParameterModel.getCamerasSelected();
		List<Integer> filtersSelected = searchParameterModel.getFiltersSelected();
		int selectedLimbIndex = searchParameterModel.getSelectedLimbIndex();
		String searchFilename = searchParameterModel.getSearchFilename();
		boolean excludeGaskell = searchParameterModel.isExcludeGaskell();
		IImagingInstrument instrument = searchParameterModel.getInstrument();

		GregorianCalendar startDateGreg = new GregorianCalendar();
        GregorianCalendar endDateGreg = new GregorianCalendar();
        startDateGreg.setTime(searchParameterModel.getStartDate());
        endDateGreg.setTime(searchParameterModel.getEndDate());
        DateTime startDateJoda = new DateTime(
                startDateGreg.get(GregorianCalendar.YEAR),
                startDateGreg.get(GregorianCalendar.MONTH)+1,
                startDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                startDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                startDateGreg.get(GregorianCalendar.MINUTE),
                startDateGreg.get(GregorianCalendar.SECOND),
                startDateGreg.get(GregorianCalendar.MILLISECOND),
                DateTimeZone.UTC);
        DateTime endDateJoda = new DateTime(
                endDateGreg.get(GregorianCalendar.YEAR),
                endDateGreg.get(GregorianCalendar.MONTH)+1,
                endDateGreg.get(GregorianCalendar.DAY_OF_MONTH),
                endDateGreg.get(GregorianCalendar.HOUR_OF_DAY),
                endDateGreg.get(GregorianCalendar.MINUTE),
                endDateGreg.get(GregorianCalendar.SECOND),
                endDateGreg.get(GregorianCalendar.MILLISECOND),
                DateTimeZone.UTC);

        TreeSet<Integer> cubeList = null;
        var selectionModel = refPickManager.getSelectionPicker().getSelectionManager();
        SmallBodyModel smallBodyModel = (SmallBodyModel)modelManager.getModel(ModelNames.SMALL_BODY);
        if (selectionModel.getNumItems() > 0)
        {
            int numberOfSides = selectionModel.getRenderAttr().numRoundSides();
            Ellipse region = (Ellipse)selectionModel.getItem(0);

            // Always use the lowest resolution model for getting the intersection cubes list.
            // Therefore, if the selection region was created using a higher resolution model,
            // we need to recompute the selection region using the low res model.
            if (smallBodyModel.getModelResolution() > 0)
            {
                vtkPolyData interiorPoly = new vtkPolyData();
                smallBodyModel.drawRegularPolygonLowRes(region.getCenter().toArray(), region.getRadius(), numberOfSides, interiorPoly, null);
                cubeList = smallBodyModel.getIntersectingCubes(interiorPoly);
            }
            else
            {
                cubeList = smallBodyModel.getIntersectingCubes(selectionModel.getVtkInteriorPolyDataFor(region));
            }
            smallBodyModel.setCubeVisibility(cubeList);
            smallBodyModel.calculateCubeSize(false, 0.0);
            smallBodyModel.clearCubes();
        }

        PointingSource imageSource = searchParameterModel.getImageSourceOfLastQuery(); // ImageSource.valueOf(((Enum)panel.getSourceComboBox().getSelectedItem()).name());
        // Populate camera and filter list differently based on if we are doing sum-of-products or product-of-sums search
        boolean sumOfProductsSearch;
//        List<Integer> camerasSelected;
//        List<Integer> filtersSelected;
//        SmallBodyViewConfig smallBodyConfig = viewConfig;
        if(config.hasHierarchicalImageSearch)
        {
            // Sum of products (hierarchical) search: (CAMERA 1 AND FILTER 1) OR ... OR (CAMERA N AND FILTER N)
            sumOfProductsSearch = true;
            Selection selection = config.hierarchicalImageSearchSpecification.processTreeSelections();
            camerasSelected = selection.getSelectedCameras();
            filtersSelected = selection.getSelectedFilters();
        }
        else
        {
            // Product of sums (legacy) search: (CAMERA 1 OR ... OR CAMERA N) AND (FILTER 1 OR ... FILTER M)
            sumOfProductsSearch = false;
        }
        List<List<String>> results = null;
        if (instrument.getSearchQuery() instanceof FixedListDataQuery)
        {
            FixedListDataQuery query = (FixedListDataQuery) instrument.getSearchQuery();
            results = query.runQuery(FixedListSearchMetadata.of("Imaging Search", "imagelist", "images", query.getRootPath(), imageSource, searchFilename)).getFetchedData();
        }
        else
        {
            // Run queries based on user specifications
            ImageDatabaseSearchMetadata searchMetadata = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                    Range.closed(minDistanceQuery, maxDistanceQuery),
                    searchFilename, null,
                    Range.closed(minIncidenceQuery, maxIncidenceQuery),
                    Range.closed(minEmissionQuery, maxEmissionQuery),
                    Range.closed(minPhaseQuery, maxPhaseQuery),
                    sumOfProductsSearch, camerasSelected, filtersSelected,
                    Range.closed(minResolutionQuery, maxResolutionQuery),
                    cubeList, imageSource, selectedLimbIndex);
            results = searchParameterModel.getInstrument().getSearchQuery().runQuery(searchMetadata).getFetchedData();
       }

        // If SPICE Derived (exclude Gaskell) or Gaskell Derived (exlude SPICE) is selected,
        // then remove from the list images which are contained in the other list by doing
        // an additional search.
        if (imageSource == PointingSource.SPICE && excludeGaskell)
        {
            List<List<String>> resultsOtherSource = null;
            if (instrument.getSearchQuery() instanceof FixedListDataQuery)
            {
                FixedListDataQuery query = (FixedListDataQuery)instrument.getSearchQuery();
                resultsOtherSource = query.runQuery(FixedListSearchMetadata.of("Imaging Search", "imagelist", "images", query.getRootPath(), imageSource)).getFetchedData();
            }
            else
            {

                ImageDatabaseSearchMetadata searchMetadataOther = ImageDatabaseSearchMetadata.of("", startDateJoda, endDateJoda,
                        Range.closed(minDistanceQuery, maxDistanceQuery),
                        searchFilename, null,
                        Range.closed(minIncidenceQuery, maxIncidenceQuery),
                        Range.closed(minEmissionQuery, maxEmissionQuery),
                        Range.closed(minPhaseQuery, maxPhaseQuery),
                        sumOfProductsSearch, camerasSelected, filtersSelected,
                        Range.closed(minResolutionQuery, maxResolutionQuery),
                        cubeList, imageSource == PointingSource.SPICE ? PointingSource.GASKELL_UPDATED : PointingSource.SPICE, selectedLimbIndex);

                    resultsOtherSource = instrument.getSearchQuery().runQuery(searchMetadataOther).getFetchedData();

            }

            int numOtherResults = resultsOtherSource.size();
            for (int i=0; i<numOtherResults; ++i)
            {
                String imageName = resultsOtherSource.get(i).get(0);
                int numResults = results.size();
                for (int j=0; j<numResults; ++j)
                {
                    if (results.get(j).get(0).startsWith(imageName))
                    {
                        results.remove(j);
                        break;
                    }
                }
            }
        }

        outputs = List.of(Triple.of(results, instrument, imageSource));
	}
}
