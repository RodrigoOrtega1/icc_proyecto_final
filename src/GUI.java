import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkIJTheme;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GUI implements ActionListener{
    private JFrame frame;
    private JButton uploadImageButton;
    private JButton uploadMetadataFileButton;
    private File fileToWorkOn;
    private MetadataRead metadataRead = new MetadataRead();
    private JScrollPane scrollPane;
    private JLabel imageLabel;

    GUI() throws IOException, IllegalAccessException {
        FlatAtomOneDarkIJTheme.setup();
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("MetaInfo");

        uploadImageButton = new JButton("Sube tu imagen");
        uploadImageButton.setFocusable(false);
        uploadImageButton.addActionListener(this);

        frame.setLayout(new BorderLayout());
        frame.add(uploadImageButton, BorderLayout.NORTH);

        frame.setSize(1000, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @SuppressWarnings("static-access")
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==uploadImageButton){
            JFileChooser file_upload = new JFileChooser();
            int res = file_upload.showSaveDialog(null);

            if(res == JFileChooser.APPROVE_OPTION){
                fileToWorkOn = new File(file_upload.getSelectedFile().getAbsolutePath());
                try {
                    metadataRead.readImageMeta(fileToWorkOn);
                    metadataRead.write();

                    BufferedImage originalImage = ImageIO.read(fileToWorkOn);
                    int newWidth = 200; // Desired width
                    int newHeight = (originalImage.getHeight() * newWidth) / originalImage.getWidth(); // Maintain aspect ratio
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

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
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