package edu.jhuapl.sbmt.image.pipelineComponents.operators.search;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.tuple.Pair;

import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.sbmt.image.model.ImagingInstrument;
import edu.jhuapl.sbmt.pipeline.operator.BasePipelineOperator;

public class LoadImageListOperator extends BasePipelineOperator<Pair<String, ImagingInstrument>, Pair<List<List<String>>, ImagingInstrument>>
{
	@Override
	public void processData() throws IOException, Exception
	{
		String filename = inputs.get(0).getLeft();
		ImagingInstrument instrument = (ImagingInstrument)inputs.get(0).getRight();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        List<List<String>> results = new ArrayList<List<String>>();
        List<String> lines = FileUtil.getFileLinesAsStringList(filename);

        for (int i = 0; i < lines.size(); ++i)
        {
            if (lines.get(i).startsWith("#"))
                continue;
            String[] words = lines.get(i).trim().split("\\s+");
            List<String> result = new ArrayList<String>();
            String name = instrument.searchQuery.getDataPath() + "/" + words[0];
            result.add(name);
            Date dt = sdf.parse(words[1]);
            result.add(String.valueOf(dt.getTime()));
            result.add(lines.get(i).trim().substring(lines.get(i).trim().indexOf(words[2])));
            results.add(result);
        }
		outputs = List.of(Pair.of(results, instrument));
	}
}
