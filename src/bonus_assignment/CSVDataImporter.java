package bonus_assignment;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;

public class CSVDataImporter {
	
	private String csvRootFolder; //"F:\\data"
	private String connectionString; //"jdbc:postgresql://localhost:5432/Evolva_test_Renato_Kuna"
	
	
	public CSVDataImporter(String rootFolder, String connectionString) {
		this.csvRootFolder = rootFolder;
		this.connectionString = connectionString;
	}
	
	
	public String getCsvFolder() {
		return this.csvRootFolder;
	}
	
	public String getConnectionString() {
		return this.connectionString;
	}
	
	public void setCsvFolder(String rootFolder) {
		this.csvRootFolder = rootFolder;
	}
	
	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}
	
	
	public void dataImport() {
		List<String> csvFiles = new ArrayList<String>();
		
		csvFiles = getCSVFileList(this.csvRootFolder);
		
		for(int i = 0; i < csvFiles.size(); i++) {
			saveIntoDatabase(this.csvRootFolder + "\\" + csvFiles.get(i));
		}
	}
	
	private List<String> getCSVFileList(String rootFolder) {
		
		List<String> csvFiles = new ArrayList<String>();
		File folder = new File(rootFolder);
		File[] listOfFiles = folder.listFiles();
		
		if(listOfFiles != null) {
			for(int i = 0; i < listOfFiles.length; i++) {
				if(listOfFiles[i].getName().contains(".csv")) {
					csvFiles.add(listOfFiles[i].getName());
				}
			}
		}
		
		return csvFiles;
	}
	
	private void saveIntoDatabase(String filePath) {
		File csvFile = new File(filePath);
		Connection connect = null;
		
		try
		{
			connect = DriverManager.getConnection(this.connectionString, "postgres", "StLj.575");
		}
		catch(SQLException e)
		{
			System.out.println("Unable to connect to database.");
			System.exit(1);
		}
		
		if(!csvFile.isFile()) {
			System.out.println("Unable to find CSV file.");
			System.exit(1);
		}
		else
		{
			try
			{
				String fileName = csvFile.getName();
				String countryName = fileName.substring(0, fileName.indexOf("."));
				List<List<String>> csvData = new ArrayList<>();
				String sqlQuery, city, currency;
				int amount, countryID = 0, cityID = 0;
				ResultSet rs = null;
				PreparedStatement statement = null;
				
				//Inserting country name from file name
				sqlQuery = "INSERT INTO country (name) VALUES ('" + countryName + "')";
				
				statement = connect.prepareStatement(sqlQuery);
				statement.executeUpdate();
				
				//Retrieving ID of country
				sqlQuery = "SELECT id FROM country WHERE name='" + countryName + "'";
				statement = connect.prepareStatement(sqlQuery);
				rs = statement.executeQuery();
				while(rs.next()) {
					countryID = rs.getInt("id");
				}
				
				//CSV data retrieval
				csvData = getCSVData(fileName);
				
				//Inserting CSV data into database
				for(int i = 0; i < csvData.size(); i++) {
					city = csvData.get(i).get(0).toLowerCase();
					currency = csvData.get(i).get(1).trim();
					amount = Integer.valueOf(csvData.get(i).get(2).trim());
					
					//Inserting data into city table
					sqlQuery = ("INSERT INTO city (name,country_id) VALUES (" + "'" + city + "'" + "," + countryID + ")");
					statement = connect.prepareStatement(sqlQuery);
					statement.executeUpdate();
					
					//Retrieving City ID
					sqlQuery = "SELECT id FROM city WHERE name='" + city + "'";
					statement = connect.prepareStatement(sqlQuery);
					rs = statement.executeQuery();
					while(rs.next()) {
						cityID = rs.getInt("id");
					}
					
					//Inserting data into saving table
					sqlQuery = ("INSERT INTO saving (amount,currency,city_id) VALUES (" +
								amount + "," + "'" + currency + "'" + "," + cityID + ")");
					statement = connect.prepareStatement(sqlQuery);
					statement.executeUpdate();
				}
			}
			catch(Exception e) {
				System.out.println("Error while importing data.");
				System.out.println(e.getMessage());
				System.exit(1);
			}
		}
	
		try {
			connect.close();
		} catch (SQLException e) {
			System.out.println("Error while closing connetion with database.");
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	private List<List<String>> getCSVData(String fileName){
		List<List<String>> csvItems = new ArrayList<>();
		
		try(Scanner scan = new Scanner(new File(this.csvRootFolder + "\\" + fileName))){
			while (scan.hasNextLine()) {
				csvItems.add(getRowItem(scan.nextLine()));
			}
		}
		catch(Exception e)
		{
			System.out.println("File not found!");
		}
		
		return csvItems;
	}
	
	private List<String> getRowItem(String line) {
		List<String> items = new ArrayList<String>();
		try (Scanner rowScan = new Scanner(line)){
			rowScan.useDelimiter(",");
			while(rowScan.hasNext()) {
				items.add(rowScan.next());
			}
		}
			
		return items;
	}

}
