package de.ovgu.mir;

import java.io.File;
import java.io.FilenameFilter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.ovgu.dke.mpeg7.binding.CompleteDescriptionType;
import de.ovgu.dke.mpeg7.binding.ContentEntityType;
import de.ovgu.dke.mpeg7.binding.ImageType;
import de.ovgu.dke.mpeg7.binding.MediaLocatorType;
import de.ovgu.dke.mpeg7.binding.Mpeg7;
import de.ovgu.dke.mpeg7.binding.ScalableColorType;
import de.ovgu.dke.mpeg7.binding.StillRegionType;



/**
 * This class is responsible for all index related tasks, namely creating the index and using the index.
 * @author mephju
 *
 */
public class Indexer {


	private static final int NUM_BINS_HUE = 32;
	private static final int NUM_BINS_SAT = 8;
	private static final int NUM_BINS_VAL = 1;

	private ImageFileFilter filter = new ImageFileFilter();

	private JAXBContext jc;
	private Unmarshaller unMarsh;
	private File srcFolder;
	private File indexFolder;
	
	
	/**
	 * A filter which filters all files but jpg images.
	 * @author mephju
	 *
	 */
	private class ImageFileFilter implements FilenameFilter  {

		@Override public boolean accept(File dir, String name) {
			if(name.matches(".*.jpg")) {
				return true;
			}
			return false;
		}

	}

	/**
	 * 
	 * @param srcFolder		The database or source folder. Contains all the images.
	 * @param indexFolder	The index folder. This is where the xml color histograms are going to be saved.
	 * @throws JAXBException
	 */
	public Indexer(File srcFolder, File indexFolder) throws JAXBException {
		jc = JAXBContext.newInstance("de.ovgu.dke.mpeg7.binding");
		unMarsh = jc.createUnmarshaller();
		
		this.srcFolder = srcFolder;
		this.indexFolder = indexFolder;
		
		if (srcFolder == null || (!srcFolder.isDirectory())) {
			throw new RuntimeException("Given path is not a valid directory!");
		}
		indexFolder.mkdirs();
		if (indexFolder == null || (!indexFolder.isDirectory())) {
			throw new RuntimeException("Given path is not a valid directory!");
		}
	}


	/** 
	 * Creates an index in the index folder. 
	 * The index merely contains the color histogram of every image in the db.
	 * @throws JAXBException 
	 */ 
	public void createIndex() throws JAXBException {
		Marshaller m = jc.createMarshaller(); 

		String[] imageNames = srcFolder.list(filter);

		for(String name : imageNames) {


			File img 		= new File(srcFolder.getAbsolutePath() + "/" + name);

			System.out.println(name);
			System.out.println(img.getAbsoluteFile());

			Picture pic 	= new Picture(img, NUM_BINS_HUE, NUM_BINS_SAT, NUM_BINS_VAL);
			Mpeg7 mpeg 		= createMpeg7Item(pic);

			m.marshal(mpeg, new File(indexFolder, pic.getFile().getName() + ".xml"));

		}

		System.out.println("done creating index");
	} 


	

	/**
	 * Creates a picture's color histogram and saves it into an Mpeg7 object
	 * @param pic 	The source from which the histogram will be taken
	 * @return 		The Mpeg7 object containing the color histogram
	 */
	private Mpeg7 createMpeg7Item(Picture pic) {

		ScalableColorType mpeg7SCD = new ScalableColorType();
		mpeg7SCD.setNumOfCoeff(BigInteger.valueOf(NUM_BINS_HUE + NUM_BINS_SAT + NUM_BINS_VAL)); 
		mpeg7SCD.setNumOfBitplanesDiscarded(BigInteger.ZERO); 
		List<BigInteger> coeffs = mpeg7SCD.getCoeff(); 


		for(int val : pic.getHistogram()) {
			coeffs.add(BigInteger.valueOf(val));
		} 



		MediaLocatorType mpeg7MediaLocator 	= new MediaLocatorType();
		StillRegionType mpeg7StillRegion 	= new StillRegionType();
		ImageType mpeg7Image 				= new ImageType();
		ContentEntityType mpeg7Content 		= new ContentEntityType();


		mpeg7MediaLocator.setMediaUri(pic.getFile().getPath()); 
		// create still region, add media locator and visual descriptor 

		mpeg7StillRegion.setMediaLocator(mpeg7MediaLocator); 
		mpeg7StillRegion 
		.getVisualDescriptorOrVisualDescriptionSchemeOrGridLayoutDescriptors() 
		.add(mpeg7SCD); 
		// create image type, set image/still region 

		mpeg7Image.setImage(mpeg7StillRegion); 
		// create content entity, add image 

		mpeg7Content.getMultimediaContent().add(mpeg7Image); 
		// create root item, add content 
		Mpeg7 mpeg7Root = new Mpeg7();
		mpeg7Root.getDescription().add(mpeg7Content); 

		return mpeg7Root;			
	}




	/**
	 * Performs a similarity query by computing the distance between a user provided image and images in the db by using 
	 * a previously created index.
	 * 
	 * @param queryFile		A user provided example which is used to find similar images in db.
	 * @throws JAXBException
	 */
	public void query(File queryFile) throws JAXBException {
		
		String[] imageNames = indexFolder.list();
		
		
		Picture pic 	= new Picture(queryFile, NUM_BINS_HUE, NUM_BINS_SAT, NUM_BINS_VAL);
		List<BigInteger> queryCoeffs = new ArrayList<>(); 
		for(int val : pic.getHistogram()) {
			queryCoeffs.add(BigInteger.valueOf(val));
		}
		
		SortedMap<Integer, File> map = new TreeMap<>();

		for(String name : imageNames) {
			File xml 		= new File(indexFolder.getAbsolutePath() + "/" + name);

			System.out.println(name);
			System.out.println(xml.getAbsoluteFile());
			
			List<BigInteger> coeffs = getCoeffsFrom(xml);
			
			int d = getDistance(queryCoeffs, coeffs);
			System.out.println("distance to q: " + d);
			map.put(d, xml);

		}
		
		System.out.println("RESULT");

		for(Integer key : map.keySet()) {
			System.out.println("" + map.get(key).getName() + "		" + key);
		}
	}
	
	
	
	/**
	 * Computes the distance by comparing the color buckets of query file to the bucket of a db file.
	 *
	 * @param q 	buckets of query file
	 * @param t		buckets of db file
	 * 
	 * @return Distance between 2 color buckets
	 */
	private int getDistance(List<BigInteger> q, List<BigInteger> t) {
		int distance = 0;
		for(int i=0; i<q.size(); i++) {
			BigInteger qVal = q.get(i);
			BigInteger tVal = t.get(i);
			distance += Math.abs(qVal.subtract(tVal).intValue());
		}
		
		return distance;
	}
	
	
	/**
	 * Retrieves the color buckets from an mpeg7 xml file.
	 * @param mpegXml	The file to retrieve the color buckets from. Must be an mpeg7 xml file.
	 * @return
	 * @throws JAXBException
	 */
	private List<BigInteger> getCoeffsFrom(File mpegXml) throws JAXBException {
		Mpeg7 mpeg7Root 		= (Mpeg7) unMarsh.unmarshal(mpegXml);
		
		ContentEntityType mpeg7Content = (ContentEntityType) mpeg7Root.getDescription().get(0);
		ImageType mpeg7Image = (ImageType) mpeg7Content.getMultimediaContent().get(0);
		StillRegionType mpeg7StillRegion = mpeg7Image.getImage();
		
		MediaLocatorType mpeg7MediaLocator = mpeg7StillRegion.getMediaLocator();
		ScalableColorType mpeg7Scd = (ScalableColorType) mpeg7StillRegion.getVisualDescriptorOrVisualDescriptionSchemeOrGridLayoutDescriptors().get(0);
		
		List<BigInteger> coeffs = mpeg7Scd.getCoeff();
		return coeffs;
	}

}

