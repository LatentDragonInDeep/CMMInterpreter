/*
 * Created by JFormDesigner on Thu Sep 21 17:00:14 CST 2017
 */

package interpreter.execute;

import interpreter.grammatical.GrammaticalParser;
import interpreter.lexical.LexicalParser;
import interpreter.semantic.Generator;
import interpreter.semantic.InterCode;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.*;

/**
 * @author chen shaojie
 */
public class MainWindow {
    public static void main (String[] args) {
        MainWindow window = new MainWindow();
        window.CMM解释器.setVisible(true);
    }
    public MainWindow() {
        initComponents();
    }

    private void selectSourceMouseClicked(MouseEvent e) {
        chooser.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
        sourceFile = chooser.getSelectedFile();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(sourceFile));
            StringBuilder builder = new StringBuilder();
            while (reader.ready()) {
                builder.append(reader.readLine());
                builder.append('\n');
            }
            textPane.setText(builder.toString());
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void exitMouseClicked(MouseEvent e) {
        System.exit(0);
    }

    private void runMouseClicked(MouseEvent e) {
        LexicalParser parser = new LexicalParser();
        parser.setSourceCode(textPane.getText());
        GrammaticalParser grammaticalParser = new GrammaticalParser(parser);
        grammaticalParser.startParse();
        Generator generator = new Generator(grammaticalParser);
        generator.startGenerate();
        java.util.List<InterCode> codes = generator.getCodes();
        Interpreter interpreter = new Interpreter(codes);
        interpreter.run();
    }

    private void buttonSelectSourceMouseClicked(MouseEvent e) {
        selectSourceMouseClicked(e);
    }

    private void buttonRunMouseClicked(MouseEvent e) {
        runMouseClicked(e);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        CMM解释器 = new JFrame();
        menuBar1 = new JMenuBar();
        file = new JMenu();
        selectSource = new JMenuItem();
        exit = new JMenuItem();
        run = new JMenu();
        toolBar = new JToolBar();
        buttonSelectSource = new JButton();
        buttonRun = new JButton();
        scrollPane1 = new JScrollPane();
        textPane = new JTextPane();

        //======== CMM解释器 ========
        {
            Container CMM解释器ContentPane = CMM解释器.getContentPane();
            CMM解释器ContentPane.setLayout(null);

            //======== menuBar1 ========
            {

                //======== file ========
                {
                    file.setText("\u6587\u4ef6(F)");

                    //---- selectSource ----
                    selectSource.setText("\u9009\u62e9\u6e90\u6587\u4ef6");
                    selectSource.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            selectSourceMouseClicked(e);
                        }
                    });
                    file.add(selectSource);

                    //---- exit ----
                    exit.setText("\u9000\u51fa");
                    exit.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            exitMouseClicked(e);
                        }
                    });
                    file.add(exit);
                }
                menuBar1.add(file);

                //======== run ========
                {
                    run.setText("\u89e3\u91ca(C)");
                    run.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            runMouseClicked(e);
                        }
                    });
                }
                menuBar1.add(run);
            }
            CMM解释器ContentPane.add(menuBar1);
            menuBar1.setBounds(0, 0, 885, menuBar1.getPreferredSize().height);

            //======== toolBar ========
            {

                //---- buttonSelectSource ----
                buttonSelectSource.setIcon(UIManager.getIcon("FileChooser.newFolderIcon"));
                buttonSelectSource.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        buttonSelectSourceMouseClicked(e);
                    }
                });
                toolBar.add(buttonSelectSource);

                //---- buttonRun ----
                buttonRun.setIcon(UIManager.getIcon("Tree.collapsedIcon"));
                buttonRun.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        buttonRunMouseClicked(e);
                    }
                });
                toolBar.add(buttonRun);
            }
            CMM解释器ContentPane.add(toolBar);
            toolBar.setBounds(0, 25, 885, 25);

            //======== scrollPane1 ========
            {
                scrollPane1.setViewportView(textPane);
            }
            CMM解释器ContentPane.add(scrollPane1);
            scrollPane1.setBounds(0, 50, 870, 420);

            { // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < CMM解释器ContentPane.getComponentCount(); i++) {
                    Rectangle bounds = CMM解释器ContentPane.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = CMM解释器ContentPane.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                CMM解释器ContentPane.setMinimumSize(preferredSize);
                CMM解释器ContentPane.setPreferredSize(preferredSize);
            }
            CMM解释器.pack();
            CMM解释器.setLocationRelativeTo(CMM解释器.getOwner());
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JFrame CMM解释器;
    private JMenuBar menuBar1;
    private JMenu file;
    private JMenuItem selectSource;
    private JMenuItem exit;
    private JMenu run;
    private JToolBar toolBar;
    private JButton buttonSelectSource;
    private JButton buttonRun;
    private JScrollPane scrollPane1;
    private JTextPane textPane;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private JFileChooser chooser = new JFileChooser();

    private File sourceFile;
}
