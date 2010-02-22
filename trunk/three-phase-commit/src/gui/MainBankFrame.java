package gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;



public class MainBankFrame extends JFrame{
	
   public MainBankFrame() {

        setSize(600, 600);
        setTitle("Transaction Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        Toolkit toolkit = getToolkit();
        Dimension size = toolkit.getScreenSize();
        setLocation(size.width/2 - getWidth()/2, 
		size.height/2 - getHeight()/2);
        
        initButtons();
        initMenu();
       
   }
   
   private void initButtons(){
	   JPanel panel = new JPanel();
       getContentPane().add(panel);
       
       panel.setSize(200, 200);
       panel.setLocation(100, 100);
       JButton submit = new JButton("Submit");
       submit.setToolTipText("Submit Transaction");
       submit.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent event) {
            
           }
       });
       
       JTextField textBox = new JTextField();
       textBox.setSize(200, 50);
       textBox.setVisible(true);
       Dimension size = this.getSize();
       size.setSize(200, 50);
       textBox.setMinimumSize(size);
       panel.add(textBox);
       panel.add(submit);
   }

   
   private void initMenu(){
       JMenuBar menubar = new JMenuBar();
       ImageIcon icon = new ImageIcon("exit.png");

       JMenu file = new JMenu("File");
       file.setMnemonic(KeyEvent.VK_F);

       JMenuItem fileClose = new JMenuItem("Close", icon);
       fileClose.setMnemonic(KeyEvent.VK_C);
       fileClose.setToolTipText("Exit application");
       fileClose.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent event) {
               System.exit(0);
           }

       });
       
       file.add(fileClose);

       JMenu testCase = new JMenu("Test Case");
       
       menubar.add(testCase);
       menubar.add(file);

       setJMenuBar(menubar);
   }
   
   public static void main(String[] args) {

        MainBankFrame mainFrame = new MainBankFrame();
        mainFrame.setVisible(true);

    } 

}
