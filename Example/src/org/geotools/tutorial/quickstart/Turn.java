package org.geotools.tutorial.quickstart;

import com.vividsolutions.jts.geom.Coordinate;

public class Turn {
	int ID;
	Coordinate Start;
	Coordinate End;
	double Length;
	
	Turn(int id, Coordinate start, Coordinate end)throws Exception{
		ID=id;
		Start=start;
		End=end;
		Length=(((Math.PI*Math.abs(Start.y-End.y))/2)+Math.abs(Start.x-End.x));
	}
}
