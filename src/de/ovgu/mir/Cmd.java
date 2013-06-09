package de.ovgu.mir;

import java.io.File;

import de.ovgu.mir.db.Db;


/**
 * java -jar MIR_P02.jar [index_folder_path][data_base_path][query_file_path]
 * @author mephju
 *
 */
public class Cmd {
	
	private static String help = "usage: java -jar MIR_P02.jar [index_folder_path][data_base_path][query_file_path]";
	
	private static String indexFilePath;
	private static String dbFolderPath;
	private static String queryFilePath;

	
	
	public static void main(String[] args) {
		
		System.out.println("start");
		extractParams(args);
		
		File dbFolder = new File(dbFolderPath);
		File indexFolder = new File(indexFilePath);
		try {
			new Db().createIndex(dbFolder, indexFolder);
		} catch (Exception e) {
			e.printStackTrace();
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
			
			indexFilePath = "/home/mephju/tmp/data_idx";
			dbFolderPath = "/home/mephju/tmp/data_raw";
			queryFilePath = "data_abstract_0002.jpg";
		} else {
			indexFilePath = args[0];
			dbFolderPath = args[1];
			queryFilePath = args[2];
		}
	}

}
