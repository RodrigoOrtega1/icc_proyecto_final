import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata.GpsInfo;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;

public class MetadataRead{

    private static String METADATA_FILE_NAME = "";
    private static File METADATA_FILE;
    private static String STRING_TO_WRITE = "";
    private static double latitude;
    private static double longitude;
    private static boolean hasGpsInfo = false;

    public boolean hasGpsInfo() {
        return hasGpsInfo;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public File getMetadataFile(){
        return METADATA_FILE;
    }

    private static double degreesToDecimal(RationalNumber degrees, RationalNumber minutes, RationalNumber seconds) {
        double degreesToDouble = degrees.doubleValue();
        double minutesToDouble = minutes.doubleValue();
        double secondsToDouble = seconds.doubleValue();
        double minutesToDegrees = minutesToDouble/60;
        double secondsToDegrees = secondsToDouble/3600;
        double decimalDegrees = degreesToDouble + minutesToDegrees + secondsToDegrees;
        return decimalDegrees;
    }

    private static String removeExtension(String fname) {
        int pos = fname.lastIndexOf('.');
        if(pos > -1)
           return fname.substring(0, pos);
        else
           return fname;
    }

    private static void createFile() {
        METADATA_FILE = new File("./Metadatos/" + METADATA_FILE_NAME);
        try {
            if (METADATA_FILE.createNewFile()) {
                System.out.println("Creando archivo: " + METADATA_FILE.getName());
            } else {
                System.out.println("El archivo ya existe.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void write() throws IOException {        
        try (FileWriter writer = new FileWriter(METADATA_FILE, false)) {
            writer.write(STRING_TO_WRITE);
        }
    }

    public static void readImageMeta(final File imgFile) throws ImagingException, IOException {
        hasGpsInfo = false;
        latitude = 0;
        longitude = 0;
        /** get all metadata stored in EXIF format (ie. from JPEG or TIFF). **/
        final ImageMetadata metadata = Imaging.getMetadata(imgFile);
        METADATA_FILE_NAME = removeExtension(imgFile.getName()) + "-meta.txt";
        createFile();
        if (metadata == null || metadata.getItems().isEmpty()){
            STRING_TO_WRITE = "Tu imagen no contiene metadatos.";
        } else {
            STRING_TO_WRITE = metadata.toString() + '\n';
        }
        
        /** Get specific meta data information by drilling down the meta **/
        if (metadata instanceof JpegImageMetadata) {
            JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            writeTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
            writeTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LATITUDE);
            writeTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
            writeTagValue(jpegMetadata, GpsTagConstants.GPS_TAG_GPS_LONGITUDE);
            
            // simple interface to GPS data
            final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
            if (null != exifMetadata) {
                final GpsInfo gpsInfo = exifMetadata.getGpsInfo();
                if (null != gpsInfo) {
                    hasGpsInfo = true;
                    final String gpsDescription = gpsInfo.toString();
                    final double longitude = gpsInfo.getLongitudeAsDegreesEast();
                    final double latitude = gpsInfo.getLatitudeAsDegreesNorth();
    
                    STRING_TO_WRITE += "    " + "GPS Description: " + gpsDescription + '\n';
                    STRING_TO_WRITE += "    " + "GPS Longitude (Degrees East): " + longitude + '\n';
                    STRING_TO_WRITE += "    " + "GPS Latitude (Degrees North): " + latitude + '\n';
                }
            }
    
            // more specific example of how to manually access GPS values
            final TiffField gpsLatitudeRefField = jpegMetadata.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
            final TiffField gpsLatitudeField = jpegMetadata.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE);
            final TiffField gpsLongitudeRefField = jpegMetadata.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
            final TiffField gpsLongitudeField = jpegMetadata.findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE);
            if (gpsLatitudeRefField != null && gpsLatitudeField != null && gpsLongitudeRefField != null && gpsLongitudeField != null) {
                // all of these values are strings.
                final String gpsLatitudeRef = (String) gpsLatitudeRefField.getValue();
                final RationalNumber[] gpsLatitude = (RationalNumber[]) (gpsLatitudeField.getValue());
                final String gpsLongitudeRef = (String) gpsLongitudeRefField.getValue();
                final RationalNumber[] gpsLongitude = (RationalNumber[]) gpsLongitudeField.getValue();
    
                final RationalNumber gpsLatitudeDegrees = gpsLatitude[0];
                final RationalNumber gpsLatitudeMinutes = gpsLatitude[1];
                final RationalNumber gpsLatitudeSeconds = gpsLatitude[2];
    
                final RationalNumber gpsLongitudeDegrees = gpsLongitude[0];
                final RationalNumber gpsLongitudeMinutes = gpsLongitude[1];
                final RationalNumber gpsLongitudeSeconds = gpsLongitude[2];
    
                // This will format the gps info like so:
                //
                // gpsLatitude: 8 degrees, 40 minutes, 42.2 seconds S
                // gpsLongitude: 115 degrees, 26 minutes, 21.8 seconds E
                
                STRING_TO_WRITE += "    " + "GPS Latitude: " + gpsLatitudeDegrees.toDisplayString() + " degrees, " + gpsLatitudeMinutes.toDisplayString() + " minutes, " + gpsLatitudeSeconds.toDisplayString() + " seconds " + gpsLatitudeRef + '\n';
                STRING_TO_WRITE += "    " + "GPS Longitude: " + gpsLongitudeDegrees.toDisplayString() + " degrees, " + gpsLongitudeMinutes.toDisplayString() + " minutes, " + gpsLongitudeSeconds.toDisplayString() + " seconds " + gpsLongitudeRef + '\n';
                STRING_TO_WRITE += "    " + "Decimal Degrees Latitude: " + Double.toString(degreesToDecimal(gpsLatitudeDegrees, gpsLatitudeMinutes, gpsLatitudeSeconds)) + '\n';
                STRING_TO_WRITE += "    " + "Decimal Degrees Longitude: " + Double.toString(degreesToDecimal(gpsLongitudeDegrees,gpsLongitudeMinutes,gpsLongitudeSeconds)) + '\n';
                latitude = degreesToDecimal(gpsLatitudeDegrees, gpsLatitudeMinutes, gpsLatitudeSeconds);
                longitude = degreesToDecimal(gpsLongitudeDegrees,gpsLongitudeMinutes,gpsLongitudeSeconds);
            }
        }
    }

    private static void writeTagValue(final JpegImageMetadata jpegMetadata, TagInfo tagInfo) throws IOException {
        final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
        if (field == null) {
            STRING_TO_WRITE += tagInfo.name + ": " + "Not Found." + '\n';
        } else {
            STRING_TO_WRITE += tagInfo.name + ": " + field.getValueDescription() + '\n';
        }
    }
}
