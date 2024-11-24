import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import org.apache.commons.io.FileUtils;

public class MetadataWrite {

    Hashtable<String, String> hashtable = new Hashtable<>();

    private void readFile(String filename) {
        try {
            File myObj = new File(filename);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split(":",2);
                hashtable.put(data[0].trim(), data[1].trim());
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * This example illustrates how to add/update EXIF metadata in a JPEG file.
     *
     * @param jpegImageFile A source image file.
     * @param dst           The output file.
     * @throws IOException
     * @throws ImagingException
     * @throws ImagingException
     */
    public void changeExifMetadata(final File jpegImageFile, final File dst, String filename) throws IOException, ImagingException, ImagingException {
        readFile(filename);
        try (FileOutputStream fos = new FileOutputStream(dst);
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
                //Writting into "Lens Model"
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_LENS_MODEL);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_LENS_MODEL, hashtable.get("lens_model"));
                //Writing into the "Date taken" field
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, hashtable.get("date_time_original"));
                //Writing into the "Digitized Date"
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, hashtable.get("date_time_digitized"));
                //Writting into "User Comment"
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_USER_COMMENT);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_USER_COMMENT, hashtable.get("user_comment"));
                //Writting into "Owner Name"
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_OWNER_NAME);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_OWNER_NAME, hashtable.get("owner_name"));
                //Writting into "Unique ID"
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_CAMERA_OWNER_NAME);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_CAMERA_OWNER_NAME, hashtable.get("camera_owner_name"));
                //Writting into "Body Serial Number"
                exifDirectory.removeField(ExifTagConstants.EXIF_TAG_BODY_SERIAL_NUMBER);
                exifDirectory.add(ExifTagConstants.EXIF_TAG_BODY_SERIAL_NUMBER, hashtable.get("body_serial_number"));
            }

            {
                // Example of how to add/update GPS info to output set.

                // New York City
                final double longitude = -74.0; // 74 degrees W (in Degrees East)
                final double latitude = 40 + 43 / 60.0; // 40 degrees N (in Degrees
                // North)

                outputSet.setGpsInDegrees(longitude, latitude);
            }

            // printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);

            new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os, outputSet);
        }
    }

    public static void main(String[] args) {
        File sourceFile = new File("/home/rodrigo/icc_proyecto_final/jvak0pwtp4sd1.jpeg");
        File destinationFile = new File("/home/rodrigo/icc_proyecto_final/writeResults/jvak0pwtp4sd1-newMetadata.jpeg");
        String filename = "newMetadata2.txt";

        MetadataWrite metadataWrite = new MetadataWrite();
        try {
            metadataWrite.changeExifMetadata(sourceFile, destinationFile, filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}