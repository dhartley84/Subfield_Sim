package org.geotools.tutorial.quickstart;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Gui {
	
	private HarvestAreas hArea;
	private JFrame frame;
	private JTextField txtFileName;
	private JTextField txtRowOverlap;
	private JTextField txtMachWidth;
	private JTextField txtCost;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui window = new Gui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Gui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 770, 755);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
				
		JPanel panel = new JPanel();
		panel.setBounds(10, 0, 734, 262);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JTextArea textPane = new JTextArea();
		textPane.setBounds(10, 273, 734, 439);
		frame.getContentPane().add(textPane);
		
		JLabel lblHarvestAreaShapefile = new JLabel("Harvest Area Shapefile");
		lblHarvestAreaShapefile.setBounds(10, 11, 167, 14);
		panel.add(lblHarvestAreaShapefile);
		
		JLabel lblHarvestMachineWidth = new JLabel("Harvest Machine  Width");
		lblHarvestMachineWidth.setBounds(10, 60, 138, 14);
		panel.add(lblHarvestMachineWidth);
		
		JLabel lblPercentOverlap = new JLabel("Percent Overlap ");
		lblPercentOverlap.setBounds(10, 108, 111, 14);
		panel.add(lblPercentOverlap);
		
		txtFileName = new JTextField();
		txtFileName.setBounds(148, 8, 487, 20);
		panel.add(txtFileName);
		txtFileName.setColumns(10);
		
		txtMachWidth = new JTextField();
		txtMachWidth.setColumns(10);
		txtMachWidth.setBounds(148, 57, 86, 20);
		panel.add(txtMachWidth);
		
		txtRowOverlap = new JTextField();
		txtRowOverlap.setBounds(148, 105, 86, 20);
		panel.add(txtRowOverlap);
		txtRowOverlap.setColumns(10);
		
		JButton btnFileBrowse = new JButton("Browse");
		btnFileBrowse.setBounds(645, 7, 79, 23);
		panel.add(btnFileBrowse);
		
		JButton btnRun = new JButton("Run Analysis");
		btnRun.setBounds(148, 215, 111, 23);
		panel.add(btnRun);
		
		JButton btnSvShapeFile = new JButton("Save Shapefiles");
		btnSvShapeFile.setBounds(277, 215, 111, 23);
		panel.add(btnSvShapeFile);
		btnSvShapeFile.setEnabled(false);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.setBounds(527, 215, 89, 23);
		panel.add(btnCancel);
		
		txtCost = new JTextField();
		txtCost.setBounds(148, 147, 86, 20);
		panel.add(txtCost);
		txtCost.setColumns(10);
		
		JLabel lblHarvestingCostPer = new JLabel("Harvesting Cost Per Hour");
		lblHarvestingCostPer.setBounds(10, 150, 128, 14);
		panel.add(lblHarvestingCostPer);
		
		JCheckBox cbxOutputLines=new JCheckBox("Save Lines");
		cbxOutputLines.setBounds(277,245,111,23);
		panel.add(cbxOutputLines);
		cbxOutputLines.setSelected(false);
	/**
	 * Define Event Handlers
	 */
		//File Browser
		btnFileBrowse.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JFileChooser fc=new JFileChooser();
				FileNameExtensionFilter filter=new FileNameExtensionFilter("SHAPE FILES","shp");
				File dir=new File("c:/users/hartds/desktop/gis_data/");
				fc.setCurrentDirectory(dir);
				fc.setFileFilter(filter);
				int v=fc.showOpenDialog(frame);
				if (v==JFileChooser.APPROVE_OPTION){
					File file=fc.getSelectedFile();
					txtFileName.setText(file.getPath());
				}
			}
		});
		
		//Run Analysis
		btnRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				File file=new File(txtFileName.getText());
				double mw=Double.parseDouble(txtMachWidth.getText());
				double ol=Double.parseDouble(txtRowOverlap.getText());
				double cost=Double.parseDouble(txtCost.getText());
				try{
					hArea=new HarvestAreas(file,mw,ol,cost);
				} catch (Exception e){
					e.printStackTrace();
				}
					
				
				try{
					WriteToCSV(hArea);
				}catch (Exception e){
					e.printStackTrace();
				}
					
				
				btnSvShapeFile.setEnabled(true);
				
				
			}
		});
		
		//Save Shapefile
		btnSvShapeFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try{
					if(!cbxOutputLines.isSelected()){
						SaveShape.drawAreas(hArea);
					} else{
						SaveShape.drawAreas(hArea);
				        SaveShape.drawLines(hArea);
					}
					System.exit(0);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		});	
	}
	public void WriteToCSV(HarvestAreas h)throws Exception{
		FileWriter writer=new FileWriter("C:/Users/HARTDS/Desktop/HarvestOutput.csv");
		writer.append("ID,CROP,YIELD,AREA,PRODUCTION,TOT_COST,UNIT_COST\n");
		for(Poly p:h.polyCollection){
			writer.append(p.ID+","+p.Crop+","+p.Yield+","+p.Area+","+p.Production+","+p.TotalCost+","+p.CostTon+"\n");
			System.out.println("Poly:"+p.ID+" written to output");
		}
		writer.flush();
		writer.close();
	}
}
