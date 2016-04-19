package org.geotools.tutorial.quickstart;

import com.vividsolutions.jts.geom.Coordinate;

public class Edge {
	int ID;
	Coordinate Start;
	Coordinate End;
	Double Xmax;
	Double Xmin;
	Double Ymax;
	Double Ymin;
	Double[] Norm_Vector;
	Double Slope;
	
	Edge(){}
	
	Edge(int id, Coordinate start, Coordinate end, Double[] extent)throws Exception{
		ID=id;
		Start=start;
		End=end;
		Xmax=extent[0];
		Xmin=extent[1];
		Ymax=extent[2];
		Ymin=extent[3];
		Norm_Vector=findNormalVector(Start,End,Xmax,Xmin,Ymax,Ymin);
		Slope=(Start.y-End.y)/(Start.x-End.x);
	}
	
	public static Edge[] EdgeArray(int size)throws Exception{
		Edge[] e=new Edge[size];
		for(int i=0;i<size;i++){
			e[i]=new Edge();
		}
		return e;
	}
	
	private static Double[] findNormalVector(Coordinate start, Coordinate end, double Xmax, double Xmin, double Ymax, double Ymin)throws Exception{
		double x=end.x-start.x;
		double y=end.y-start.y;
		Double[] vector=normVectorCalc(x,y);
		if (x<0 & y<0){
			vector[1]=vector[1]*-1;
		} else if (x<0 & y>0){
			vector[0]=vector[0]*-1;
			vector[1]=vector[1]*-1;
		} else if (x>0 & y>0){
			vector[0]=vector[0]*-1;
		} else if(x==0 & x==Xmin){
			vector[0]=-1.0;
			vector[1]=0.0;
		} else if(x==0 & x==Xmax){
			vector[0]=1.0;
			vector[1]=0.0;
		} else if(y==0 & y==Ymin){
			vector[0]=0.0;
			vector[1]=-1.0;
		} else if (y==0 & y==Ymax){
			vector[0]=0.0;
			vector[1]=-1.0;
		}
		return vector;
	}
	
	private static Double[] normVectorCalc(double x, double y)throws Exception{
		Double[] vector=new Double[2];
		if(Math.abs(x)>=Math.abs(y)){
			vector[0]=1.0;
			vector[1]=Math.abs(x/y);
		} else {
			vector[0]=Math.abs(y/x);
			vector[1]=1.0;
		}
		return vector;	
	}
}
