import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.commons.imaging.ImagingException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class GUI implements ActionListener{
    private JFrame frame;
    private JButton uploadImageButton;
    private JButton uploadMetadataFileButton;
    private File fileToWorkOn;
    private MetadataRead metadataRead = new MetadataRead();

    GUI() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("MetaInfo");

        uploadImageButton = new JButton("Sube tu imagen");
        uploadImageButton.setFocusable(false);
        uploadImageButton.addActionListener(this);
        frame.add(uploadImageButton);

        //uploadMetadataFileButton = new JButton("Sube tu archivo metadata"); TODO:VER COMO ANADIR DOS BOTONES A LA VEZ
        //frame.add(uploadMetadataFileButton);

        frame.pack();
        frame.setSize(1000,500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @SuppressWarnings("static-access")
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==uploadImageButton){
            JFileChooser file_upload = new JFileChooser();
            //file_upload.setCurrentDirectory(null); Si se quiere cambiar que directorio abre el boton para encontrar la imagen
            int res = file_upload.showSaveDialog(null);

            if(res == JFileChooser.APPROVE_OPTION){
                fileToWorkOn = new File(file_upload.getSelectedFile().getAbsolutePath());
                try {
                    metadataRead.readImageMeta(fileToWorkOn);
                } catch (ImagingException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        GUI gui = new GUI();
    }
}