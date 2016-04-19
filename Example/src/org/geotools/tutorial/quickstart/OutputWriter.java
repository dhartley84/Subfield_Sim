package org.geotools.tutorial.quickstart;

import java.io.FileWriter;

public class OutputWriter {

	public void WriteToCSV(HarvestAreas h)throws Exception{
		FileWriter writer=new FileWriter("C:/Users/HARTDS/Desktop/HarvestOutput.csv");
		String output="ID,CROP,YIELD,AREA,PRODUCTION,TOT_COST,UNIT_COST\n";
		for(Poly p:h.polyCollection){
			output+=p.ID+","+p.Crop+","+p.Yield+","+p.Area+","+p.Production+","+p.TotalCost+","+p.CostTon+"\n";
		}
		writer.append(output);
		writer.flush();
		writer.close();
	}
}
