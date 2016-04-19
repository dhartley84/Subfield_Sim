package org.geotools.tutorial.quickstart;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.referencing.operation.MathTransform;

import java.awt.geom.AffineTransform;

import org.geotools.geometry.jts.JTS;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class Poly {
	int ID;
	double Area;
	String Crop;
	double Yield;
	double Cost;
	Coordinate[] Vertices;
	Coordinate[] Hull;
	Double[] LongAxis;
	Coordinate[] TranslatedVertices;
	Double[] BoundingEnvelope;
	Edge[] Edges;
	HLine[] HarvestLines;
	Turn[] Turns;
	LineString[] RotatedLines;
	double HarvestTime;
	double TurnTime;
	double TotalTime;
	double Production;
	double TotalCost;
	double CostTon;
	Poly(){}
	
	Poly(int id,double area, String crop, double yield, Coordinate[] coords, double d, double e,double c) throws Exception{
		ID=id;
		Area=area;
		Crop=crop;
		Yield=yield;
		Cost=c;
		Vertices=coords;
		Hull=Poly.convexHull(Vertices);
		LongAxis=Poly.FindLongAxis(Hull);
		TranslatedVertices=Poly.translateVertices(Vertices,LongAxis);
		BoundingEnvelope=boundingEnvelope(TranslatedVertices);
		Edges=calcEdges(BoundingEnvelope,TranslatedVertices);
		HarvestLines=harvestLines(BoundingEnvelope,Edges,d,e);
		Turns=createTurns(HarvestLines);
		RotatedLines=rotateLines(HarvestLines,LongAxis);
		HarvestTime=CalcHarvestTime(HarvestLines,8,.95);
		TurnTime=CalcTurnTime(Turns,4);
		TotalTime=HarvestTime+TurnTime;
		Production=(Area*Yield)/TotalTime;
		TotalCost=TotalTime*Cost;
		CostTon=Cost/Production;
	}
	
	public static Poly[] PolyArray(int size){
		Poly[] p=new Poly[size];
		for(int i=0;i<size;i++){
			p[i]=new Poly();
		}
		return p;
	}
	
	public static Coordinate[] convexHull(Coordinate[] Vertices) throws Exception{
		GeometryFactory geometryFactory=JTSFactoryFinder.getGeometryFactory();
		Polygon polygon=geometryFactory.createPolygon(Vertices);
		Geometry geom=polygon.convexHull();
		Coordinate[] hull=geom.getCoordinates();
		return hull;
	}
	
	public static Double[] FindLongAxis(Coordinate[] Hull) throws Exception{
		Double[] longAxis=new Double[6];
		double length=0;
		for(int i=0;i<Hull.length;i++){
			for(int j=0;j<Hull.length;j++){
				if(i!=j){
					double seg_len=Math.sqrt(Math.pow((Hull[i].x-Hull[j].x), 2)+Math.pow((Hull[i].y-Hull[j].y), 2));
					if(seg_len>length & seg_len>0){
						longAxis[4]=length=seg_len;
						longAxis[0]=Hull[i].x;
						longAxis[1]=Hull[j].x;
						longAxis[2]=Hull[i].y;
						longAxis[3]=Hull[j].y;
						longAxis[5]=Math.toDegrees(Math.atan((Hull[i].y-Hull[j].y)/(Hull[i].x-Hull[j].x)));
					}
				}
			}
		}
		return longAxis;
	}
	
	public static Coordinate[] translateVertices(Coordinate[] Hull, Double[] LongAxis)throws Exception{
		GeometryFactory geometryFactory=JTSFactoryFinder.getGeometryFactory();
		Polygon rotate=geometryFactory.createPolygon(Hull);
		Coordinate anchor= new Coordinate(LongAxis[0],LongAxis[2]);
		AffineTransform affineTransform=AffineTransform.getRotateInstance(Math.toRadians(-LongAxis[5]),anchor.x,anchor.y);
		MathTransform mathTransform=new AffineTransform2D(affineTransform);
		Geometry rotated=JTS.transform(rotate, mathTransform);
		Coordinate[] transpoly=rotated.getCoordinates();
		return transpoly;
	}
	
	public static Double[] boundingEnvelope(Coordinate[] TranslatedVertices)throws Exception{
		Double[] envelope={Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY};
		for(Coordinate c:TranslatedVertices){
			if (c.x<envelope[0])
				envelope[0]=c.x;
			if(c.x>envelope[1])
				envelope[1]=c.x;
			if(c.y<envelope[2])
				envelope[2]=c.y;
			if(c.y>envelope[3])
				envelope[3]=c.y;
		}
		return envelope;
	}
	
	public static Edge[] calcEdges(Double[] boundingEnvelope,Coordinate[] trans_vertices)throws Exception{
		Edge[] edges=new Edge[trans_vertices.length-1];
		int i=0;
		Double[] extent=boundingEnvelope;
		while(i<trans_vertices.length-1){
			Coordinate P0=trans_vertices[i];
			Coordinate P1=trans_vertices[i+1];
			edges[i]=new Edge(i,P0,P1,extent);
			i++;
		}
		return edges;
	}
	
	public static HLine[] harvestLines(Double[] boundingEnvelope,Edge[] edges,Double cutWidth, Double overlap){
		int numLines=(int) ((boundingEnvelope[3]-boundingEnvelope[2])/(cutWidth*(1-overlap)));
		double y=boundingEnvelope[2];
		HLine[] hline=new HLine[numLines];
		int i=0;
		while (i<numLines){
			if(i==0){
				y+=(.5*cutWidth*(1-overlap));
				hline[i]=new HLine(i,new Coordinate(boundingEnvelope[0],y),new Coordinate(boundingEnvelope[1],y));
				i++;
			} else {
				y+=(cutWidth*(1-overlap));
				hline[i]=new HLine(i,new Coordinate(boundingEnvelope[0],y),new Coordinate(boundingEnvelope[1],y));
				i++;
			}
		}
		HLine[] clippedLines=clipLineToEdge(hline,edges);
		return clippedLines;
	}

	private static HLine[] clipLineToEdge(HLine[] hline,Edge[] edges) {
		HLine[] outLine=new HLine[hline.length];
		int i=0;
		for(HLine h:hline){
			double minX=Double.POSITIVE_INFINITY;
			double maxX=Double.NEGATIVE_INFINITY;
			
			for(Edge e:edges){

				double clip_pt=e.Start.x+((h.Start.y-e.Start.y)/(e.Slope));
				
				if ((h.Start.y>=e.Start.y & h.Start.y<=e.End.y)|(h.Start.y<=e.Start.y &h.Start.y>=e.End.y)){
					if(clip_pt<minX)
						minX=clip_pt;
					if(clip_pt>maxX)
						maxX=clip_pt;
				}
			}
			
			outLine[i]=new HLine(i,new Coordinate(minX,h.Start.y),new Coordinate(maxX,h.End.y));
			i++;
		}
		return outLine;
	}
	
	private static Turn[] createTurns(HLine[] HarvestLines)throws Exception{
		Turn[] turns=new Turn[HarvestLines.length-1];
		int i=0;
		while(i<HarvestLines.length-1){
			if(i%2==0){
				turns[i]=new Turn(i,HarvestLines[i].Start,HarvestLines[i+1].End);
				i++;
			} else {
				turns[i]=new Turn(i,HarvestLines[i].End,HarvestLines[i+1].Start);
				i++;
			}
		}
		return turns;
	}
	
	private static double CalcHarvestTime(HLine[] harvestLines, double harvestSpeed,double harvestEfficiency)throws Exception{
		double totHarvDist=0.0;
		for(HLine h:harvestLines)
			totHarvDist+=(h.Length/1000);
		double harvTime=(totHarvDist/harvestSpeed)/harvestEfficiency;
		return harvTime;
	}
	
	private static double CalcTurnTime(Turn[] turns, double turnSpeed)throws Exception{
		double totTurnDist=0.0;
		for(Turn t:turns)
			totTurnDist+=(t.Length/1000);
		double turnTime=totTurnDist/turnSpeed;
		return turnTime;
	}
	
	private static LineString[] rotateLines(HLine[] harvestLines,Double[] LongAxis)throws Exception{
		LineString[] lines=new LineString[harvestLines.length];
		GeometryFactory geometryFactory=JTSFactoryFinder.getGeometryFactory();
		int i=0;
		for(HLine h:harvestLines){
			Coordinate[] coords={h.Start,h.End};
			LineString line=geometryFactory.createLineString(coords);
			AffineTransform affineTransform=AffineTransform.getRotateInstance(LongAxis[5],LongAxis[0],LongAxis[2]);
			MathTransform mathTransform=new AffineTransform2D(affineTransform);
			lines[i]=(LineString) JTS.transform(line, mathTransform);
			i++;
		}
		return lines;
	}
}
