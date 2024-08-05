package bonus_assignment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabasePrinter {
	private String csvRootFolder;	//"F:\\data";
	private String connectionString;	//"jdbc:postgresql://localhost:5432/Evolva_test_Renato_Kuna"
	
	
	public DatabasePrinter(String rootFolder, String connectionString) {
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
	
	
	public void printData() {
		Connection connect = null;
		StringBuilder HTMLFile = new StringBuilder();
		List<String> totalCurrencyList = new ArrayList<String>();
		List<Integer> totalAmountList = new ArrayList<Integer>();
		
		//System.out.println("Retrieving data from database...");
		HTMLFile.append("<html>");
		HTMLFile.append("<head>");
		HTMLFile.append("<title>Money Counter Report</title>");
		HTMLFile.append("</head>");
		HTMLFile.append("<body>");
		HTMLFile.append("<p>Retrieving data from database...</p>");
		try
		{
			connect = DriverManager.getConnection(this.connectionString, "postgres", "StLj.575");
		}
		catch (SQLException e) {
			System.out.println("Error while connecting to database.");
			System.out.println(e.getMessage());
		}
		
		try
		{
			List<String> currencyList = new ArrayList<String>();
			List<Integer> amountList = new ArrayList<Integer>();
			List<Integer> countryIDList = new ArrayList<Integer>();
			String sqlQuery;
			ResultSet rs = null;
			PreparedStatement statement = null;
			
			//Retrieving country id
			sqlQuery = "SELECT id FROM country";
			statement = connect.prepareStatement(sqlQuery);
			rs = statement.executeQuery();
			while(rs.next()) {
				countryIDList.add(rs.getInt("id"));
			}
			
			//Printing currency and amount data for each country
			for(int i = 0; i < countryIDList.size(); i++) {
				sqlQuery = "SELECT c.currency, c.amount\r\n"
						+ "FROM country a inner join city b\r\n"
						+ "ON a.id = b.country_id\r\n"
						+ "inner join saving c\r\n"
						+ "ON b.id = c.city_id\r\n"
						+ "WHERE a.id = " + countryIDList.get(i);
				statement = connect.prepareStatement(sqlQuery);
				rs = statement.executeQuery();
				while(rs.next()) {
					if(!currencyList.contains(rs.getString("currency"))) {
						currencyList.add(rs.getString("currency"));
						amountList.add(rs.getInt("amount"));
					}
					else
					{
						int index = currencyList.indexOf(rs.getString("currency"));
						int sum = amountList.get(index);
						sum += rs.getInt("amount");
						amountList.set(index, sum);
					}
				}
				
				sqlQuery = "SELECT name FROM country WHERE id=" + countryIDList.get(i);
				statement = connect.prepareStatement(sqlQuery);
				rs = statement.executeQuery();
				while(rs.next()) {
					//System.out.println("\ndata for " + rs.getString("name") + " found:");
					HTMLFile.append("<p><br>data for " + rs.getString("name") + " found:<br>");
				}
				
				//System.out.println("  Totals by currencies:");
				HTMLFile.append("&ensp;Totals by currencies:<br>");
				
				for(int j = 0; j < currencyList.size(); j++) {
					//System.out.printf("    %s: %d\n", currencyList.get(j), amountList.get(j));
					HTMLFile.append("&emsp;" + currencyList.get(j)+ ": " + amountList.get(j).toString()+ "<br>");
					
					if(!totalCurrencyList.contains(currencyList.get(j))) {
						totalCurrencyList.add(currencyList.get(j));
						totalAmountList.add(amountList.get(j));
					}
					else {
						int index = totalCurrencyList.indexOf(currencyList.get(j));
						int sum = totalAmountList.get(index);
						sum += amountList.get(j);
						totalAmountList.set(index, sum);
					}
				}
				currencyList.clear();
				amountList.clear();
				HTMLFile.append("</p>");
			}
			
			//Printing total amounts for each currency
			//System.out.println("\nMoney in all countries:");
			HTMLFile.append("<p><br>Money in all countries:<br>");
			for(int i = 0; i < totalCurrencyList.size(); i++) {
				//System.out.printf("  %s: %d\n", totalCurrencyList.get(i), totalAmountList.get(i));
				HTMLFile.append("&ensp;" + totalCurrencyList.get(i) + ": " + totalAmountList.get(i).toString() + "<br>");
			}
			HTMLFile.append("</p>");
			HTMLFile.append("</body>");
			HTMLFile.append("</html>");
			
		}
		catch (SQLException e)
		{
			System.out.println("Error while retrieving data from database.");
			System.out.println(e.getMessage());
		}
		
		try
		{
			FileWriter fstream = new FileWriter(new File(this.csvRootFolder, "MoneyCounterReport.html"));
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(HTMLFile.toString());
			out.close();
		}
		catch (IOException e) {
			System.out.println("Error while exporting HTML file.");
			System.out.println(e.getMessage());
		}
		
	}
}
