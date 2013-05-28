package de.ovgu.mir.db;

import java.io.File;
import java.io.FilenameFilter;
import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import de.ovgu.dke.mpeg7.binding.ContentEntityType;
import de.ovgu.dke.mpeg7.binding.ImageType;
import de.ovgu.dke.mpeg7.binding.MediaLocatorType;
import de.ovgu.dke.mpeg7.binding.Mpeg7;
import de.ovgu.dke.mpeg7.binding.ScalableColorType;
import de.ovgu.dke.mpeg7.binding.StillRegionType;
import de.ovgu.mir.Picture;



public class Db {

	
	private static final int NUM_BINS_HUE = 32;
	private static final int NUM_BINS_SAT = 8;
	private static final int NUM_BINS_VAL = 1;

	private FileFilter filter = new FileFilter();
	
	private class FileFilter implements FilenameFilter  {

		@Override public boolean accept(File dir, String name) {
			if(name.matches(".*.jpg")) {
				return true;
			}
			return false;
		}

	}



	
	/** 
	 * @param dbDir		This is the directory where data resides and where the xml files are stored.
	 * @throws JAXBException 
	 */ 
	public void createDb(File srcFolder, File dbDir) throws JAXBException {

		dbDir.mkdirs();
		if (dbDir == null || dbDir.isDirectory() == false) {
			throw new RuntimeException("Given path is not a valid directory!");
		}
		
		JAXBContext jc = JAXBContext.newInstance("de.ovgu.dke.mpeg7.binding"); 
		Marshaller m = jc.createMarshaller(); 
		
		String[] imageNames = srcFolder.list(filter);

		for(String name : imageNames) {
			System.out.println(name);
	
			File img 		= new File(name);
			Picture pic 	= new Picture(img, NUM_BINS_HUE, NUM_BINS_SAT, NUM_BINS_VAL);
			Mpeg7 mpeg 		= createMpeg7Item(pic);
			
			m.marshal(mpeg, new File(dbDir, pic.getFile().getName() + ".xml"));
		}
		
		System.out.println("done");
	} 


	
	
	private Mpeg7 createMpeg7Item(Picture pic) {

			ScalableColorType mpeg7SCD = new ScalableColorType();
			mpeg7SCD.setNumOfCoeff(BigInteger.valueOf(NUM_BINS_HUE + NUM_BINS_SAT + NUM_BINS_VAL)); 
			mpeg7SCD.setNumOfBitplanesDiscarded(BigInteger.ZERO); 
			List<BigInteger> coeffs = mpeg7SCD.getCoeff(); 
			
//			for(HSV hsv : pic.getHistogram().keySet()) {
//				double coeff = pic.getHistogram().get(hsv);
//				coeffs.add(BigInteger.valueOf((long) coeff*255));
//			} 

			coeffs.add(BigInteger.TEN);

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
	
}
