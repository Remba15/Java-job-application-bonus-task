package bonus_assignment;

public class Main {
	public static void main(String[] args) {
		CSVDataImporter importer = new CSVDataImporter(
				"F:\\data",
				"jdbc:postgresql://localhost:5432/Evolva_test_Renato_Kuna"
		);
		importer.dataImport();
		
		DatabasePrinter databasePrinter = new DatabasePrinter(
				"F:\\data",
				"jdbc:postgresql://localhost:5432/Evolva_test_Renato_Kuna"
		);
		databasePrinter.printData();
	}
	
}
