import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.imaging.ImagingException;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkIJTheme;

public class GUI implements ActionListener{
    private JFrame frame;
    private JPanel topPanel;
    private JButton uploadImageButton;
    private JButton openInMapsButton;
    private JButton openTextFileButton;
    private File fileToWorkOn;
    private MetadataRead metadataRead = new MetadataRead();
    private MetadataWrite metadataWrite = new MetadataWrite();
    private JScrollPane scrollPane;
    private JLabel imageLabel;
    private double latitude;
    private double longitude;

    GUI() throws IOException, IllegalAccessException {
        FlatAtomOneDarkIJTheme.setup();
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("MetaInfo");

        uploadImageButton = new JButton("Sube tu imagen");
        uploadImageButton.setFocusable(false);
        uploadImageButton.addActionListener(this);

        openTextFileButton = new JButton("Cambia los metadatos");
        openTextFileButton.setFocusable(false);
        openTextFileButton.setVisible(false);
        openTextFileButton.addActionListener(e -> {
            try {
                changeMetadata();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        openInMapsButton = new JButton("Abrir en Google Maps");
        openInMapsButton.setFocusable(false);
        openInMapsButton.setVisible(false);
        openInMapsButton.addActionListener(e -> openInGoogleMaps());

        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(uploadImageButton);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(openInMapsButton, BorderLayout.SOUTH);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setSize(1000, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @SuppressWarnings("static-access")
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==uploadImageButton){
            JFileChooser file_upload = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "bmp", "gif", "jpeg", "jfif", "png", "jpg", "tiff", "wbmp", "xbm", "xpm", "pcx", "dcx");
            file_upload.setFileFilter(filter);
            int res = file_upload.showSaveDialog(null);

            if(res == JFileChooser.APPROVE_OPTION){
                fileToWorkOn = new File(file_upload.getSelectedFile().getAbsolutePath());
                try {
                    metadataRead.readImageMeta(fileToWorkOn);
                    metadataRead.write();

                    BufferedImage originalImage = ImageIO.read(fileToWorkOn);
                    int newWidth = 300;
                    int newHeight = (originalImage.getHeight() * newWidth) / originalImage.getWidth();
                    Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                    ImageIcon imageIcon = new ImageIcon(scaledImage);
                    
                    if (imageLabel == null) {
                        imageLabel = new JLabel(imageIcon);
                        frame.add(imageLabel, BorderLayout.WEST);
                    } else {
                        imageLabel.setIcon(imageIcon);
                    }

                    JTextArea textArea = new JTextArea(20, 50);
                    FileReader reader = new FileReader(metadataRead.getMetadataFile());
                    textArea.read(reader, "File");
                    JScrollPane newScrollPane = new JScrollPane(textArea);

                    if (scrollPane != null){
                        frame.remove(scrollPane);
                    }

                    scrollPane = newScrollPane;
                    frame.add(scrollPane, BorderLayout.CENTER);
                    frame.revalidate();
                    frame.repaint();

                    if (metadataRead.hasGpsInfo()) {
                        latitude = metadataRead.getLatitude();
                        longitude = metadataRead.getLongitude();
                        openInMapsButton.setVisible(true);
                    } else {
                        openInMapsButton.setVisible(false);
                    }

                    if (!topPanel.isAncestorOf(openTextFileButton)) {
                        topPanel.add(openTextFileButton);
                    }
                    openTextFileButton.setVisible(true);
                    topPanel.revalidate();
                    topPanel.repaint();

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("static-access")
    private void changeMetadata() throws ImagingException, IOException{
        JFileChooser file_upload = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
        file_upload.setFileFilter(filter);
        int res = file_upload.showOpenDialog(null);

        if (res == JFileChooser.APPROVE_OPTION) {
            File metadataFile = new File(file_upload.getSelectedFile().getAbsolutePath());

            metadataWrite.changeExifMetadata(fileToWorkOn, metadataFile.getName());

            File newMetadata = new File(metadataWrite.getNewMetadataFileDestinationName());
            File newImage = new File(metadataWrite.getNewImageFileDestination());

            JTextArea textArea = new JTextArea(20, 50);
            FileReader reader = new FileReader(newMetadata);
            textArea.read(reader, "File");
            JScrollPane newScrollPane = new JScrollPane(textArea);

            if (scrollPane != null){
                frame.remove(scrollPane);
            }

            scrollPane = newScrollPane;
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.revalidate();
            frame.repaint();

            MetadataRead newMetadataRead = new MetadataRead();
            newMetadataRead.readImageMeta(newImage);

            if (newMetadataRead.hasGpsInfo()) {
                latitude = newMetadataRead.getLatitude();
                longitude = newMetadataRead.getLongitude();
                openInMapsButton.setVisible(true);
            } else {
                openInMapsButton.setVisible(false);
            }

            JOptionPane.showMessageDialog(frame, "Se ha creado una copia de la imagen con los nuevos metadatos en la carpeta writeResults\n");
        }
    }

    private void openInGoogleMaps() {
        String url = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                String[] browsers = {"xdg-open", "gnome-open", "kde-open"};
                boolean success = false;
                for (String browser : browsers) {
                    try {
                        Runtime.getRuntime().exec(new String[]{browser, url});
                        success = true;
                        break;
                    } catch (IOException ignored) {
                    }
                }
                if (!success) {
                    JOptionPane.showMessageDialog(frame, "No fue posible abrir en buscador, por favor inserta el URL manualmente:\n" + url, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        SwingUtilities.invokeLater(() -> {
            try {
                new GUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}