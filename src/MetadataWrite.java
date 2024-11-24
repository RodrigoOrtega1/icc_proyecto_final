import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Hashtable;
import java.util.Scanner;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

public class MetadataWrite {

    private Hashtable<String, String> hashtable = new Hashtable<>();
    private String FILE_DESTINATION_NAME = "";
    private String NEW_IMAGE_FILE_DESTINATION = "";

    private boolean readFile(String filename) {
        boolean rigthFormat = false;
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split(":",2);
                if (data.length == 2) {
                    hashtable.put(data[0].trim(), data[1].trim());
                } else {
                    System.out.println("Formato incorrecto");
                    rigthFormat = false;
                    break;
                }
                rigthFormat = true;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
                return rigthFormat;
    }

    private static String removeExtension(String fname) {
        int pos = fname.lastIndexOf('.');
        if(pos > -1)
           return fname.substring(0, pos);
        else
           return fname;
    }

    public String getNewMetadataFileDestinationName(){
        return "./Metadatos/" + FILE_DESTINATION_NAME + "-newMetadata-meta.txt";
    }

    public String getNewImageFileDestination(){
        return NEW_IMAGE_FILE_DESTINATION;
    }

    /**
     * This example illustrates how to add/update EXIF metadata in a JPEG file.
     *
     * @param jpegImageFile A source image file.
     * @param filename Archivo de texto que contiene los nuevos metadatos
     * @throws IOException
     * @throws ImagingException
     * @throws ImagingException
     */
    public int changeExifMetadata(final File jpegImageFile, String filename) throws IOException, ImagingException, ImagingException {
        boolean hasMetadata = false;

        Files.createDirectories(Paths.get("./ImagenesNuevasMetadatos/"));

        FILE_DESTINATION_NAME = removeExtension(jpegImageFile.getName());
        NEW_IMAGE_FILE_DESTINATION = "./ImagenesNuevasMetadatos/" + FILE_DESTINATION_NAME + "-newMetadata.jpeg";

        if (!readFile(filename)){
            return 1;
        }
        
        try (FileOutputStream fos = new FileOutputStream(NEW_IMAGE_FILE_DESTINATION);
                OutputStream os = new BufferedOutputStream(fos)) {

            TiffOutputSet outputSet = null;

            // note that metadata might be null if no metadata is found.
            final ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            if (null != jpegMetadata) {
                // note that exif might be null if no Exif metadata is found.
                final TiffImageMetadata exif = jpegMetadata.getExif();

                if (null != exif) {
                    // TiffImageMetadata class is immutable (read-only).
                    // TiffOutputSet class represents the Exif data to write.
                    //
                    // Usually, we want to update existing Exif metadata by
                    // changing
                    // the values of a few fields, or adding a field.
                    // In these cases, it is easiest to use getOutputSet() to
                    // start with a "copy" of the fields read from the image.
                    outputSet = exif.getOutputSet();
                }
            }

            // if file does not contain any exif metadata, we create an empty
            // set of exif metadata. Otherwise, we keep all of the other
            // existing tags.
            if (null == outputSet) {
                outputSet = new TiffOutputSet();
            }

            {
                // Example of how to add a field/tag to the output set.
                //
                // Note that you should first remove the field/tag if it already
                // exists in this directory, or you may end up with duplicate
                // tags. See above.
                //
                // Certain fields/tags are expected in certain Exif directories;
                // Others can occur in more than one directory (and often have a
                // different meaning in different directories).
                //
                // TagInfo constants often contain a description of what
                // directories are associated with a given tag.
                //
                final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
                // make sure to remove old value if present (this method will
                // not fail if the tag does not exist).
                if(hashtable.containsKey("lens_model")){
                    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_LENS_MODEL);
                    exifDirectory.add(ExifTagConstants.EXIF_TAG_LENS_MODEL, hashtable.get("lens_model"));
                    hasMetadata = true;
                }
                if(hashtable.containsKey("date_time_original")){
                    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
                    exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, hashtable.get("date_time_original"));
                    hasMetadata = true;
                }
                if(hashtable.containsKey("date_time_digitized")){
                    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
                    exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, hashtable.get("date_time_digitized"));
                    hasMetadata = true;
                }
                if(hashtable.containsKey("user_comment")){
                    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_USER_COMMENT);
                    exifDirectory.add(ExifTagConstants.EXIF_TAG_USER_COMMENT, hashtable.get("user_comment"));
                    hasMetadata = true;
                }
                if(hashtable.containsKey("owner_name")){
                    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_OWNER_NAME);
                    exifDirectory.add(ExifTagConstants.EXIF_TAG_OWNER_NAME, hashtable.get("owner_name"));
                    hasMetadata = true;
                }
                if(hashtable.containsKey("camera_owner_name")){
                    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_CAMERA_OWNER_NAME);
                    exifDirectory.add(ExifTagConstants.EXIF_TAG_CAMERA_OWNER_NAME, hashtable.get("camera_owner_name"));
                    hasMetadata = true;
                }
                if(hashtable.containsKey("body_serial_number")){
                    exifDirectory.removeField(ExifTagConstants.EXIF_TAG_BODY_SERIAL_NUMBER);
                    exifDirectory.add(ExifTagConstants.EXIF_TAG_BODY_SERIAL_NUMBER, hashtable.get("body_serial_number"));
                    hasMetadata = true;
                }
            }

            {
                if(hashtable.containsKey("latitude") && hashtable.containsKey("longitude")){
                    final double longitude = Double.parseDouble(hashtable.get("latitude"));
                    final double latitude = Double.parseDouble(hashtable.get("longitude"));
                    outputSet.setGpsInDegrees(longitude, latitude);
                    hasMetadata = true;
                }
            }

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
        }
                if (hasMetadata){
                    return 0;
                } else {
                    return 2;
                }
    }
}