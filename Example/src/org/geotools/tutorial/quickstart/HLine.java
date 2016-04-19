package org.geotools.tutorial.quickstart;

import com.vividsolutions.jts.geom.Coordinate;

public class HLine {
	int ID;
	Coordinate Start;
	Coordinate End;
	Double Length;
	
	HLine(int id,Coordinate start, Coordinate end){
		ID=id;
		Start=start;
		End=end;
		Length=Math.sqrt(Math.pow((Start.x-End.x),2)+Math.pow((Start.y-End.y),2));
	}
}
