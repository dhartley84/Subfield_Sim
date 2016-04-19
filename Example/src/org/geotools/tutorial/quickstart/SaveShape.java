package org.geotools.tutorial.quickstart;

import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.io.Serializable;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class SaveShape {
	public static void  drawAreas(HarvestAreas harvestAreas) throws Exception{
		final SimpleFeatureType TYPE=createFeatureType(harvestAreas.CRS,"Polygon");
		List<SimpleFeature> features= new ArrayList<SimpleFeature>();
		SimpleFeatureBuilder featureBuilder=new SimpleFeatureBuilder(TYPE);
		GeometryFactory geometryFactory=JTSFactoryFinder.getGeometryFactory();
		for(Poly p:harvestAreas.polyCollection){
			Polygon poly=geometryFactory.createPolygon(p.Vertices);
			featureBuilder.add(poly);
			featureBuilder.add(p.Area);
			featureBuilder.add(p.Crop);
			featureBuilder.add(p.Yield);
			featureBuilder.add(p.HarvestTime);
			featureBuilder.add(p.TurnTime);
			featureBuilder.add(p.TotalTime);
			featureBuilder.add(p.Production);
			featureBuilder.add(p.TotalCost);
			featureBuilder.add(p.CostTon);
			SimpleFeature feature=featureBuilder.buildFeature(null);
			features.add(feature);
			System.out.println("Polygon:"+p.ID+" added");
		}
		
		File newFile=getNewShapeFile();
		ShapefileDataStoreFactory dataStoreFactory=new ShapefileDataStoreFactory();
		Map<String,Serializable> params=new HashMap<String,Serializable>();
		params.put("url",newFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		System.out.println("newFile created");
		
		ShapefileDataStore newDataStore=(ShapefileDataStore)dataStoreFactory.createNewDataStore(params);
		newDataStore.createSchema(TYPE);
		System.out.println("newDataStore created");
		
		Transaction transaction=new DefaultTransaction("create");
		String typeName=newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource=newDataStore.getFeatureSource(typeName);

		
		if (featureSource instanceof SimpleFeatureStore){
			SimpleFeatureStore featureStore=(SimpleFeatureStore) featureSource;
			SimpleFeatureCollection collection=new ListFeatureCollection(TYPE, features);
			featureStore.setTransaction(transaction);
			try{
				featureStore.addFeatures(collection);
				transaction.commit();
				System.out.println("transaction committed");
				newDataStore.dispose();
			} catch (Exception problem){
				problem.printStackTrace();
				transaction.rollback();
			} finally {
				transaction.close();
			}
			convertToEsri(newFile);
			System.out.println("Converted to ESRIformat");
		}else{
			System.out.println(typeName+" does not support read/write access");
			System.exit(1);
		}
		
	}

	public static String drawLines(HarvestAreas harvestAreas)throws Exception{
		final SimpleFeatureType TYPE=createFeatureType(harvestAreas.CRS,"Line");
		List<SimpleFeature> features= new ArrayList<SimpleFeature>();
		SimpleFeatureBuilder featureBuilder=new SimpleFeatureBuilder(TYPE);
		GeometryFactory geometryFactory=JTSFactoryFinder.getGeometryFactory();
		for (Poly p:harvestAreas.polyCollection){
			for(HLine l:p.HarvestLines){
				Coordinate[] coords={l.Start,l.End};
				LineString line=geometryFactory.createLineString(coords);
				Coordinate anchor=new Coordinate(p.LongAxis[0],p.LongAxis[2]);
				AffineTransform affineTransform=AffineTransform.getRotateInstance(Math.toRadians(p.LongAxis[5]),anchor.x,anchor.y);
				MathTransform mathTransform=new AffineTransform2D(affineTransform);
				Geometry rotated=JTS.transform(line,mathTransform);
				LineString liner=(LineString)rotated;
				featureBuilder.add(liner);
				SimpleFeature feature=featureBuilder.buildFeature(null);
				features.add(feature);
				System.out.println("Line:"+l.ID+" added");
			}
		
		}
		File newFile=getNewLineFile();
		ShapefileDataStoreFactory dataStoreFactory=new ShapefileDataStoreFactory();
		Map<String,Serializable> params=new HashMap<String,Serializable>();
		params.put("url",newFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		
		ShapefileDataStore newDataStore=(ShapefileDataStore)dataStoreFactory.createNewDataStore(params);
		newDataStore.createSchema(TYPE);
		
		Transaction transaction=new DefaultTransaction("create");
		String typeName=newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource=newDataStore.getFeatureSource(typeName);

		
		if (featureSource instanceof SimpleFeatureStore){
			SimpleFeatureStore featureStore=(SimpleFeatureStore) featureSource;
			SimpleFeatureCollection collection=new ListFeatureCollection(TYPE, features);
			featureStore.setTransaction(transaction);
			try{
				featureStore.addFeatures(collection);
				transaction.commit();
				newDataStore.dispose();
			} catch (Exception problem){
				problem.printStackTrace();
				transaction.rollback();
			} finally {
				transaction.close();
			}
			convertToEsri(newFile);
			
		}else{
			System.out.println(typeName+" does not support read/write access");
			System.exit(1);
		}
		return newFile.toString();
	}
	private static File getNewShapeFile(){
		String path="C:/Users/HARTDS/Desktop/GIS_Data/HarvestArea.shp";
		JFileDataStoreChooser chooser=new JFileDataStoreChooser("shp");
		chooser.setDialogTitle("Save Shapefile");
		chooser.setSelectedFile(new File(path));
		
		int returnVal=chooser.showSaveDialog(null);
		
		if(returnVal!=JFileDataStoreChooser.APPROVE_OPTION){
			System.exit(0);
		}
		
		File newFile=chooser.getSelectedFile();
		return newFile;
	}
	
	private static File getNewLineFile(){
		String path="C:/Users/HARTDS/Desktop/GIS_Data/HarvestAreaLines.shp";
		JFileDataStoreChooser chooser=new JFileDataStoreChooser("shp");
		chooser.setDialogTitle("Save Shapefile");
		chooser.setSelectedFile(new File(path));
		
		int returnVal=chooser.showSaveDialog(null);
		
		if(returnVal!=JFileDataStoreChooser.APPROVE_OPTION){
			System.exit(0);
		}
		
		File newFile=chooser.getSelectedFile();
		return newFile;
	}
	
	private static SimpleFeatureType createFeatureType(CoordinateReferenceSystem crs,String type)throws Exception{
		SimpleFeatureTypeBuilder builder=new SimpleFeatureTypeBuilder();
		if(type=="Polygon"){
			builder.setName("Area");
			builder.setCRS(crs);
			builder.add("the_geom",Polygon.class);
			builder.add("Area",Double.class);
			builder.add("Crop",String.class);
			builder.add("Yield",Double.class);
			builder.add("HarvTime",Double.class);
			builder.add("TurnTime",Double.class);
			builder.add("TotTime",Double.class);
			builder.add("Prod",Double.class);
			builder.add("TotCost",Double.class);
			builder.add("UnitCost",Double.class);
		} else if(type=="Line"){
			builder.setName("Line");
			builder.setCRS(crs);
			builder.add("the_geom",LineString.class);
		}
		final SimpleFeatureType TYPE=builder.buildFeatureType();
		
		return TYPE;
	}
	
	private static void convertToEsri(File file)throws Exception{
		String newFilePath=file.getPath();
		String projFilePath=newFilePath.substring(0,newFilePath.length()-4)+".prj";
		BufferedReader br=new BufferedReader(new FileReader(projFilePath));
		String wkt=br.readLine();
		br.close();
		String regex="[\"][m][\"]";
		
		Pattern r=Pattern.compile(regex);
		
		Matcher m=r.matcher(wkt);
		wkt=m.replaceAll("\"Meter\"");
		BufferedWriter bw=new BufferedWriter(new FileWriter(projFilePath));
		bw.write(wkt);
		bw.close();
	}
}