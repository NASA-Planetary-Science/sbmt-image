package edu.jhuapl.sbmt.image.query;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.joda.time.DateTime;

import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.sbmt.core.pointing.PointingSource;
import edu.jhuapl.sbmt.query.database.ImageDatabaseSearchMetadata;
import edu.jhuapl.sbmt.query.v2.DataQuerySourcesMetadata;
import edu.jhuapl.sbmt.query.v2.DatabaseDataQuery;
import edu.jhuapl.sbmt.query.v2.DatabaseSearchMetadata;
import edu.jhuapl.sbmt.query.v2.FetchedResults;
import edu.jhuapl.sbmt.query.v2.ISearchMetadata;
import edu.jhuapl.sbmt.query.v2.QueryException;
import edu.jhuapl.sbmt.query.v2.QueryException.QueryExceptionReason;
import edu.jhuapl.sbmt.query.v2.QueryException.Severity;
import edu.jhuapl.ses.jsqrl.api.Key;
import edu.jhuapl.ses.jsqrl.api.Version;
import edu.jhuapl.ses.jsqrl.impl.FixedMetadata;
import edu.jhuapl.ses.jsqrl.impl.InstanceGetter;
import edu.jhuapl.ses.jsqrl.impl.SettableMetadata;

public class ImageDataQuery extends DatabaseDataQuery
{
	private String tablePrefixSpc;
	private String tablePrefixSpice;
	private PointingSource imageSource;
	private String searchString;
	private String imagesDatabase;
	private String cubesDatabase;
	private boolean publicOnly = false;
    private String imageNameTable = null;
	
	public ImageDataQuery()
	{
		super(null);
	}
	
	public ImageDataQuery(DataQuerySourcesMetadata searchMetadata)
	{
		super(searchMetadata);
		this.tablePrefixSpc = dataSourceMetadata.getDBSPCTable();
		this.tablePrefixSpice = dataSourceMetadata.getDBSpiceTable();
		this.rootPath = dataSourceMetadata.getRootPath();
		this.dataPath = dataSourceMetadata.getDataPath();
		this.galleryPath = dataSourceMetadata.getGalleryPath();
	}
	
	   public void setPublicOnly(boolean publicOnly)
	    {
	    	this.publicOnly = publicOnly;

	    }

	    public void setImageNameTable(String imageNameTable)
	    {
	    	this.imageNameTable = imageNameTable;
	    }
	
	private HashMap<String, String> convertSearchCriteriaToArgs(ISearchMetadata queryMetadata)
	{
		FixedMetadata metadata = queryMetadata.getMetadata();
        double fromIncidence = metadata.get(DatabaseSearchMetadata.FROM_INCIDENCE);
        double toIncidence = metadata.get(DatabaseSearchMetadata.TO_INCIDENCE);
        double fromEmission = metadata.get(DatabaseSearchMetadata.FROM_EMISSION);
        double toEmission = metadata.get(DatabaseSearchMetadata.TO_EMISSION);
        double fromPhase = metadata.get(DatabaseSearchMetadata.FROM_PHASE);
        double toPhase = metadata.get(DatabaseSearchMetadata.TO_PHASE);
        searchString = metadata.get(DatabaseSearchMetadata.SEARCH_STRING);
        double startDistance = metadata.get(DatabaseSearchMetadata.FROM_DISTANCE);
        double stopDistance = metadata.get(DatabaseSearchMetadata.TO_DISTANCE);
        imageSource = PointingSource.valueOf(metadata.get(ImageDatabaseSearchMetadata.IMAGE_SOURCE));
        double startResolution = metadata.get(ImageDatabaseSearchMetadata.FROM_RESOLUTION);
        double stopResolution = metadata.get(ImageDatabaseSearchMetadata.TO_RESOLUTION);
        boolean sumOfProductsSearch = metadata.get(ImageDatabaseSearchMetadata.SUM_OF_PRODUCTS);
        TreeSet<Integer> cubeList = metadata.get(ImageDatabaseSearchMetadata.CUBE_LIST);
        List<Integer> camerasSelected = metadata.get(ImageDatabaseSearchMetadata.CAMERAS_SELECTED);
        List<Integer> filtersSelected = metadata.get(ImageDatabaseSearchMetadata.FILTERS_SELECTED);
        int limbType = metadata.get(ImageDatabaseSearchMetadata.HAS_LIMB);
        DateTime startDate = new DateTime(metadata.get(DatabaseSearchMetadata.START_DATE));
        DateTime stopDate = new DateTime(metadata.get(DatabaseSearchMetadata.STOP_DATE));
//        List<Integer> polygonTypes = metadata.get(DatabaseSearchMetadata.POLYGON_TYPES);
        
        double minIncidence = Math.min(fromIncidence, toIncidence);
        double maxIncidence = Math.max(fromIncidence, toIncidence);
        double minEmission = Math.min(fromEmission, toEmission);
        double maxEmission = Math.max(fromEmission, toEmission);
        double minPhase = Math.min(fromPhase, toPhase);
        double maxPhase = Math.max(fromPhase, toPhase);

        // Get table name.  Examples: erosimages_gaskell, amicacubes_pds_beta
        imagesDatabase = getTablePrefix(imageSource).toLowerCase() + "images_" + imageSource.getDatabaseTableName();
        cubesDatabase = getTablePrefix(imageSource).toLowerCase() + "cubes_" + imageSource.getDatabaseTableName();

        imagesDatabase += Configuration.getDatabaseSuffix();
        cubesDatabase += Configuration.getDatabaseSuffix();
        
        if (searchString != null)
        {
            HashMap<String, String> args = new HashMap<>();
            args.put("imagesDatabase", imagesDatabase);
            args.put("searchString", searchString);
            if (imageNameTable != null)
            {
            	String visibilityStr = publicOnly ? "public" : "public,private";
            	args.put("imageLocationDatabase", imagesDatabase);
            	args.put("visibilityStr", visibilityStr);
            }
            return args;
        }

        double minScDistance = Math.min(startDistance, stopDistance);
        double maxScDistance = Math.max(startDistance, stopDistance);
        double minResolution = Math.min(startResolution, stopResolution) / 1000.0;
        double maxResolution = Math.max(startResolution, stopResolution) / 1000.0;

        HashMap<String, String> args = new HashMap<>();
        args.put("imagesDatabase", imagesDatabase);
        args.put("cubesDatabase", cubesDatabase);
        args.put("minResolution", String.valueOf(minResolution));
        args.put("maxResolution", String.valueOf(maxResolution));
        args.put("minScDistance", String.valueOf(minScDistance));
        args.put("maxScDistance", String.valueOf(maxScDistance));
        args.put("startDate", String.valueOf(startDate.getMillis()));
        args.put("stopDate", String.valueOf(stopDate.getMillis()));
        args.put("minIncidence", String.valueOf(minIncidence));
        args.put("maxIncidence", String.valueOf(maxIncidence));
        args.put("minEmission", String.valueOf(minEmission));
        args.put("maxEmission", String.valueOf(maxEmission));
        args.put("minPhase", String.valueOf(minPhase));
        args.put("maxPhase", String.valueOf(maxPhase));
        args.put("limbType", String.valueOf(limbType));

        // Populate args for camera and filter search
        if(sumOfProductsSearch)
        {
            // Sum of products (hierarchical) search: (CAMERA 1 AND FILTER 1) OR ... OR (CAMERA N AND FILTER N)
            args.put("sumOfProductsSearch", "1");
            Integer[] camerasSelectedArray = camerasSelected.toArray(new Integer[0]);
            Integer[] filtersSelectedArray = filtersSelected.toArray(new Integer[0]);
            int numProducts = camerasSelectedArray.length;

            // Populate search parameters
            args.put("numProducts", Integer.valueOf(numProducts).toString());
            for(int i=0; i<numProducts; i++)
            {
                args.put("cameraType"+i, Integer.valueOf(camerasSelectedArray[i]+1).toString());
                args.put("filterType"+i, Integer.valueOf(filtersSelectedArray[i]+1).toString());
            }
        }
        else
        {
            // Product of sums (legacy) search: (CAMERA 1 OR ... OR CAMERA N) AND (FILTER 1 OR ... FILTER M)
            args.put("sumOfProductsSearch", "0");

            // Populate search parameters
            for(Integer c : camerasSelected)
            {
                args.put("cameraType"+(c+1), "1");
            }
            for(Integer f : filtersSelected)
            {
                args.put("filterType"+(f+1), "1");
            }
        }
        if (cubeList != null && cubeList.size() > 0)
        {
            String cubes = "";
            int size = cubeList.size();
            int count = 0;
            for (Integer i : cubeList)
            {
                cubes += "" + i;
                if (count < size-1)
                    cubes += ",";
                ++count;
            }
            args.put("cubes", cubes);
        }
        return args;
	}

	@Override
	public FetchedResults runQuery(ISearchMetadata queryMetadata) throws QueryException
	{
		getFixedListQuery().setShowFixedListPrompt(true);
		HashMap<String, String> args = convertSearchCriteriaToArgs(queryMetadata);


        if (imageSource == PointingSource.CORRECTED)
        {
            FetchedResults resultsFromFileListOnServer = getFixedListQuery().getResultsFromFileListOnServerWithSearch("sumfiles-corrected/imagelist.txt",
                     searchString);
            return resultsFromFileListOnServer;   
        }
        else if (imageSource == PointingSource.CORRECTED_SPICE)
        {
            FetchedResults resultsFromFileListOnServer = getFixedListQuery().getResultsFromFileListOnServerWithSearch("infofiles-corrected/imagelist.txt",
                     searchString);
            return resultsFromFileListOnServer;   
        }
        /*else if (imageSource == ImageSource.GASKELL_UPDATED)
        {
            return getResultsFromFileListOnServer(rootPath + "/sumfiles_to_be_delivered/imagelist.txt",
                    rootPath + "/images/", galleryPath);
        }*/

        FetchedResults results;

//        try
//        {
        	boolean tableExists = DatabaseDataQuery.checkForDatabaseTable(imagesDatabase);
            if (!tableExists) throw new QueryException("Database table " + imagesDatabase + " is not available now.",
            		Severity.ERROR, QueryExceptionReason.DB_TABLE_NOT_FOUND);

            results = doQuery("searchimages.php", constructUrlArguments(args));

//        }
//        catch (QueryException e)
//        {
////            System.err.println("ImageDataQuery: runQuery: Can't reach database server, or some other database access "
////            		+ "failure; falling back to cached results");
////            results = getFixedListQuery().getCachedDataQuery().getCachedResults(searchString);
//            results = fallbackQuery(queryMetadata);
//        }

        return results;   
	}
	
	@Override
    public FetchedResults fallbackQuery(ISearchMetadata queryMetadata) throws QueryException
    {
		return super.fallbackQuery(queryMetadata);
//		String imageListName = "imagelist-info.txt";
//        if (imageSource.equals(PointingSource.GASKELL))
//        {
//        	imageListName = "imagelist-sum.txt";
//        	if (!FileCache.instance().isAccessible(rootPath + "/" + imageListName))
//        		imageListName = "imagelist.txt";
//        }
//        return getFixedListQuery().getResultsFromFileListOnServerWithSearch(rootPath + "/" + imageListName, searchString);
    }
	
	public String getTablePrefix(PointingSource source)
    {
        return source == PointingSource.SPICE ? tablePrefixSpice : tablePrefixSpc;
    }
	
	@Override
	public String getGalleryPath()
	{
		return galleryPath;
	}

	public String getRootPath()
	{
		return rootPath;
	}

	private static final Key<String> rootPathKey = Key.of("rootPath");
	private static final Key<String> tablePrefixSpcKey = Key.of("tablePrefixSpc");
    private static final Key<String> tablePrefixSpiceKey = Key.of("tablePrefixSpice");
    private static final Key<String> galleryPathKey = Key.of("galleryPath");
    private static final Key<String> dataPathKey = Key.of("dataPath");
    private static final Key<ImageDataQuery> IMAGEDATAQUERY_KEY = Key.of("imageDataQuery");

    /**
	 * Registers this class with the metadata system
	 * 
	 *
	 */
	public static void initializeSerializationProxy()
	{
		//TODO Finish defining this
    	InstanceGetter.defaultInstanceGetter().register(IMAGEDATAQUERY_KEY, (metadata) -> {

    		String rootPath = metadata.get(rootPathKey);
            String tablePrefixSpc = metadata.get(tablePrefixSpcKey);
            String tablePrefixSpice = metadata.get(tablePrefixSpiceKey);
            String dataPath = metadata.get(dataPathKey);
            String galleryPath = null;
            try
            {
                galleryPath = metadata.get(galleryPathKey);
            }
            catch (IllegalArgumentException iae)
            {

            }
            DataQuerySourcesMetadata sourceMetadata =
            		DataQuerySourcesMetadata.of(rootPath, dataPath, tablePrefixSpc, tablePrefixSpice, galleryPath);
            ImageDataQuery query = new ImageDataQuery(sourceMetadata);
    		return query;

    	}, ImageDataQuery.class, query -> {

    		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));
    		result.put(rootPathKey, query.getRootPath());
    		result.put(tablePrefixSpcKey, query.getTablePrefix(PointingSource.GASKELL));
    		result.put(tablePrefixSpiceKey, query.getTablePrefix(PointingSource.SPICE));
    		result.put(galleryPathKey, query.getGalleryPath());
    		result.put(dataPathKey, query.getDataPath());
    		return result;
    	});

	}
}
