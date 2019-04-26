package com.amazonaws.samples;

//import javax.swing.JFrame; 
//import javax.swing.JScrollPane; 
//import javax.swing.JTable; 
//  
//public class testWindow { 
//    // frame 
//    JFrame f; 
//    // Table 
//    JTable j; 
//  
//    // Constructor 
//    testWindow() 
//    { 
//        // Frame initiallization 
//        f = new JFrame(); 
//  
//        // Frame Title 
//        f.setTitle("JTable Example"); 
//  
//        // Data to be displayed in the JTable 
//        String[][] data = { 
//            { "Kundan Kumar Jha", "4031", "CSE" }, 
//            { "Anand Jha", "6014", "IT" } 
//        }; 
//  
//        // Column Names 
//        String[] columnNames = { "Name", "Roll Number", "Department" }; 
//  
//        // Initializing the JTable 
//        j = new JTable(data, columnNames); 
//        j.setBounds(30, 40, 200, 300); 
//  
//        // adding it to JScrollPane 
//        JScrollPane sp = new JScrollPane(j); 
//        f.add(sp); 
//        // Frame Size 
//        f.setSize(500, 200); 
//        // Frame Visible = true 
//        f.setVisible(true); 
//    } 
//  
//    // Driver  method 
//    public static void main(String[] args) 
//    { 
//        new testWindow(); 
//    } 
//} 
import javax.swing.JFrame; 
import javax.swing.JScrollPane; 
import javax.swing.JTable; 
import javax.swing.table.DefaultTableModel; 

public class testWindow extends JFrame { 

    public testWindow() { 
        DefaultTableModel model = new DefaultTableModel() { 
            String[] employee = {"emp 1", "emp 2"}; 

            @Override 
            public int getColumnCount() { 
                return employee.length; 
            } 

            @Override 
            public String getColumnName(int index) { 
                return employee[index]; 
            } 
        }; 

        JTable table = new JTable(model); 
        add(new JScrollPane(table)); 
        pack(); 
        setDefaultCloseOperation(EXIT_ON_CLOSE); 
        setVisible(true); 
    } 

    public static void main(String[] args) { 
        new testWindow(); 
    } 
}