package gui;

import exceptions.DataStreamException;
import logic.Client;
import logic.Manager;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**Class which provides GUI.*/
public class ClientApp  extends JFrame implements WindowListener{
    /**Path to icon of folder which is used in listing files.*/
    private static final String PathToFolder = "res/icon-folder.png";
    /**Path to icon of file which is used in listing files.*/
    private static final String PathToFile = "res/icon-file.png";
    /**Button which should be clicked to enter input data.*/
    private JButton button = new JButton("Enter");
    /**TextField where should be entered input data.*/
    private JTextField input = new JTextField("", 15);
    /**Just label with information.*/
    private JLabel label = new JLabel("Path:");
    /**Font of every text messages of application.*/
    private Font font = new Font("Verdana", Font.ITALIC, 12);
    /**Manager which provides easy working with Client and Server.*/
    private Manager manager;
    /**Map needed to create list of files.*/
    private Map<String, ImageIcon> imageMap;
    /**Path to file where should some file be downloaded.*/
    private String saveDirectory;

    /**Inits main screen and creates {@code Manager} object.*/
    private ClientApp() {
        super("Server");
        this.setBounds(100,0, 450, 700);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        try{
            manager = new Manager();
        } catch (Exception e){
            JOptionPane.showMessageDialog(null, "Sorry, failed to connect to server.",
                    "Inane error", JOptionPane.ERROR_MESSAGE);
        }

        init();
        showHelp();
    }

    /**Shows help to dealing with application. */
    private void showHelp(){
        JOptionPane.showMessageDialog(null,
                "Enter path to directory to get the list of files and folders in it.\n" +
                        "Enter path to file to download it.\n" +
                        "You can walk through the file tree.\n" +
                        "Click the folder to get into, click the file to download it.",
                "Info", JOptionPane.PLAIN_MESSAGE);
    }

    /**Inits main screen with all GUI objects.*/
    private void init() {
        label.setFont(font);
        label.setForeground(Color.BLACK);

        Container container = this.getContentPane();
        container.setBackground(new Color(229, 229, 229));

        container.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.PAGE_START;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 0;
        c.weighty = 1;
        container.add(label, c);

        c.gridwidth = 2;
        c.weightx = 1.;
        container.add(input, c);

        c.gridwidth = 1;
        c.weightx = 0;
        button.addActionListener(new ButtonEventListener());
        container.add(button, c);
    }

    /**Class needed to provide reaction on button clicks.*/
    private class ButtonEventListener implements ActionListener {
        /**
         * Void to react on button click.
         * Shows list of files in a directory if directory was entered or clicked.
         * Downloads a file if file was chosen.
         * Shows an error message if something goes wrong.
         * @param e -- event of button performed.
         */
        public void actionPerformed(ActionEvent e) {
            final String[] path = {input.getText()};
            input.setText("");
            try {
                File file = new File(path[0]);
                if (file.isDirectory()) {
                    // Create and show list of files
                    String[] names = fillFileNames(manager.getList(path[0]));
                    Container mainContainer = ClientApp.this.getContentPane();
                    mainContainer.removeAll();
                    init();
                    JList fileList = new JList(names);
                    fileList.setCellRenderer(new FileListRenderer());
                    fileList.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent evt) {
                            JList list = (JList)evt.getSource();
                            if (evt.getClickCount() == 2) {
                                int index = list.locationToIndex(evt.getPoint());
                                if (!path[0].substring(path[0].length() - 1).equals(File.separator)) {
                                    path[0] += File.separator;
                                }
                                input.setText(path[0] + names[index]);
                                actionPerformed(e);
                            }
                        }
                    });
                    setJFileList(fileList);
                } else if (file.isFile()) {
                    // Download file
                    saveDirectory = "";
                    String fileName = Paths.get(path[0]).getFileName().toString();
                    DialogDownload dialog = new DialogDownload(ClientApp.this, fileName);
                    dialog.setVisible(true);
                    if (!saveDirectory.substring(saveDirectory.length() - 1).equals(File.separator)) {
                        saveDirectory += File.separator;
                    }
                    saveDirectory += fileName;
                    File saveTo = new File(saveDirectory);
                    if (!saveTo.createNewFile()) {
                        JOptionPane.showMessageDialog(null, "Sorry, failed to create file " + saveDirectory + ".",
                                "Inane error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    OutputStream fout = Files.newOutputStream(Paths.get(saveDirectory));
                    IOUtils.copyLarge(new ByteArrayInputStream(manager.download(path[0])), fout);
                    if (!saveTo.exists() ||
                            !Arrays.equals(manager.download(path[0]), manager.download(saveTo.toString()))){
                        JOptionPane.showMessageDialog(null, "Sorry, failed to download selected file.",
                                "Inane error", JOptionPane.ERROR_MESSAGE);
                        if (saveTo.exists()){
                            if (!saveTo.delete()){
                                JOptionPane.showMessageDialog(null,
                                        "Sorry, failed to remove file " + saveDirectory + ".",
                                        "Inane error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "File was successfully downloaded.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Input data is incorrect.\nGiven path was not found.\n" +
                                    "Please, try once again.\nEnter only path to existing directory or file.",
                            "Wrong input",
                            JOptionPane.WARNING_MESSAGE);
                }
            } catch (DataStreamException ex){
                JOptionPane.showMessageDialog(null,
                        "Sorry, some problems with data streams... It happens..." + ex.getMessage(),
                        "Inane error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex){
                JOptionPane.showMessageDialog(null,
                        "Sorry, some unexpected problems... It happens...\n" + ex.getMessage(),
                        "Inane error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Returns names of all files and folders in given directory.
     * First of all go names of folder in alphabetic order, after go names of files.
     * @param list -- content of given directory
     * @return list of names of all files and folders
     */
    private String[] fillFileNames(Client.Element[] list){
        String[] names = new String[list.length + 1];
        imageMap = new HashMap<>();
        int cur = 0;
        imageMap.put("..", new ImageIcon(PathToFolder));
        names[cur] = "..";
        cur++;
        for (Client.Element element : list){
            if (element.isDirectory()) {
                imageMap.put(element.getName(), new ImageIcon(PathToFolder));
                names[cur] = element.getName();
                cur++;
            }
        }
        for (Client.Element element : list){
            if (!element.isDirectory()) {
                imageMap.put(element.getName(), new ImageIcon(PathToFile));
                names[cur] = element.getName();
                cur++;
            }
        }
        return names;
    }

    /**
     * Creates and fills GUI object with list of files.
     * @param fileList -- list of all files that should be shown
     */
    private void setJFileList(JList fileList){
        Container mainContainer = ClientApp.this.getContentPane();
        JScrollPane scroll = new JScrollPane(fileList);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.PAGE_END;
        c.gridy = 1;
        c.gridwidth = 4;
        c.weighty = 100;
        c.fill = GridBagConstraints.BOTH;
        mainContainer.add(scroll, c);
        mainContainer.invalidate();
        mainContainer.validate();
    }

    /**Class to provide creating of list of files.*/
    private class FileListRenderer extends DefaultListCellRenderer {
        /**Creates components of list -- name and icon.*/
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setIcon(imageMap.get(value));
            label.setHorizontalTextPosition(JLabel.RIGHT);
            label.setFont(font);
            return label;
        }
    }

    /**Class needed to show a dialog window asking for path to directory where ro download the document.*/
    private class DialogDownload extends JDialog {
        /**Creation of dialog window.
         * Gets name of file that should be downloaded.
         * Saves path to which a document should be downloaded.
         */
        DialogDownload(JFrame owner, String name) {
            super(owner, "Download this document", true);
            setLayout(new GridBagLayout());
            this.setBounds(90, 100, 1000, 100);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.PAGE_START;
            c.gridy = 0;
            c.weighty = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            add(new JLabel("Please select directory"), c);
            c.gridy = 1;
            add(new JLabel("to download \"" + name + "\""), c);
            c.gridy = 2;
            c.weighty = 1;
            JTextField directory = new JTextField("", 15);
            add(directory, c);

            JButton enter = new JButton("Enter");
            enter.addActionListener(event -> {
                saveDirectory = directory.getText();
                setVisible(false);
                dispose();
            });

            c.gridy = 3;
            c.weighty = 0;
            JPanel panel = new JPanel();
            panel.add(enter);
            add(panel, c);
            setSize(260, 160);
        }
    }

    /**Starts application.*/
    public static void main(String[] args) {
        ClientApp app = new ClientApp();
        app.setVisible(true);
    }

    @Override
    public void windowOpened(WindowEvent e) {}

    /**Shutdowns manager before closing the application.*/
    @Override
    public void windowClosing(WindowEvent e) {
        manager.shutdown();
    }

    @Override
    public void windowClosed(WindowEvent e) {}

    @Override
    public void windowIconified(WindowEvent e) {}

    @Override
    public void windowDeiconified(WindowEvent e) {}

    @Override
    public void windowActivated(WindowEvent e) {}

    @Override
    public void windowDeactivated(WindowEvent e) {}
}
