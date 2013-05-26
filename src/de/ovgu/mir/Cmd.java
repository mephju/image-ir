package de.ovgu.mir;

import java.io.File;


/**
 * java -jar MIR_P02.jar [index_folder_path][data_base_path][query_file_path]
 * @author mephju
 *
 */
public class Cmd {
	
	private static String help = "usage: java -jar MIR_P02.jar [index_folder_path][data_base_path][query_file_path]";
	
	private static String indexFolderPath;
	private static String dataBasePath;
	private static String queryFilePath;
	
	
	
	public static void main(String[] args) {
		
		System.out.println("start");
		extractParams(args);
		
		File d = new File(dataBasePath);
		try {
			new Db().createDb(d);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	
	/**
	 * Extracts parameters from user supplied arguments. We must find 2 params here:
	 * 1. param tells us where to search
	 * 2. param tells us what to search for
	 * @param args
	 */
	private static void extractParams(String[] args) {
		if(args.length != 3) {
//			System.out.println(help);
//			System.exit(0);
			
			indexFolderPath = "data_index.idx";
			dataBasePath = "data";
			queryFilePath = "data_abstract_0002.jpg";
		} else {
			indexFolderPath = args[0];
			dataBasePath = args[1];
			queryFilePath = args[2];
		}
	}

}
