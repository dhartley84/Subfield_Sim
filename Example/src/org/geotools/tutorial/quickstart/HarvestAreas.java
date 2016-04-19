package org.geotools.tutorial.quickstart;

import java.io.File;
import java.util.List;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class HarvestAreas {
	Poly[] polyCollection;
	CoordinateReferenceSystem CRS;
	
	HarvestAreas(File featureClass, double d, double e,double c) throws Exception{
		polyCollection=populate(featureClass, d, e,c);
		CRS=findCRS(featureClass);
	}
	
	private Poly[] populate(File featureClass, double d, double e, double c) throws Exception{
		FileDataStore store = FileDataStoreFinder.getDataStore(featureClass);
        SimpleFeatureSource featureSource = store.getFeatureSource();
        SimpleFeatureCollection collection=featureSource.getFeatures();
        int size=collection.size();
		Poly[] pcollect=new Poly[size];
		try(SimpleFeatureIterator iterator=collection.features()){
			int i=0;
        	while(iterator.hasNext()){
        		SimpleFeature feature = iterator.next();
        		MultiPolygon polygon= (MultiPolygon) feature.getDefaultGeometry();
        		for(int j=0;j<polygon.getNumGeometries();j++){
        			Polygon poly=(Polygon) polygon.getGeometryN(j);
        			LineString shell=poly.getExteriorRing();
        			//Geometry geometry = (Geometry) feature.getDefaultGeometry();
        			List<Object> attributes=feature.getAttributes();
        			double area=(double) attributes.get(2);
        			String crop=(String) attributes.get(3);
        			double yield=(double) attributes.get(4);
        			Coordinate[] coords=shell.getCoordinates();
        			Poly p=new Poly(i,area,crop,yield,coords,d,e,c);
        			pcollect[i]=p;
        			System.out.println("Polygon:"+i+" processed");
        			i++;
        			
        		}
        	}
        }
		store.dispose();
		return pcollect;
	}
	
	private CoordinateReferenceSystem findCRS(File featureClass)throws Exception{
		FileDataStore store=FileDataStoreFinder.getDataStore(featureClass);
		SimpleFeatureSource featureSource=store.getFeatureSource();
		SimpleFeatureType featureType=featureSource.getSchema();
		CoordinateReferenceSystem crs=featureType.getCoordinateReferenceSystem();
		store.dispose();
		return crs;
	}
}
