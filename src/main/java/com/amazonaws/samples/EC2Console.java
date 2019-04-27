package com.amazonaws.samples;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.mediastoredata.model.PutObjectRequest;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;

import javax.swing.JLabel;
import javax.swing.ListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Component;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;

public class EC2Console {

	private JFrame frame;
	public DefaultListModel<String> model;
	public DefaultListModel<String> model_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EC2Console window = new EC2Console();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public EC2Console() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 590, 361);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		model = new DefaultListModel<>();
		model_1 = new DefaultListModel<>();

		JLabel lblInstance = new JLabel("Instance(s)");
		lblInstance.setBounds(10, 27, 82, 15);
		frame.getContentPane().add(lblInstance);

		JLabel lblStatus = new JLabel("Status");
		lblStatus.setBounds(198, 27, 54, 15);
		frame.getContentPane().add(lblStatus);

		JList<String> list = new JList<>(model);
		list.setBounds(10, 52, 156, 260);
		frame.getContentPane().add(list);

		JButton btnLauch = new JButton("Lauch New");
		btnLauch.setBounds(446, 49, 93, 23);
		frame.getContentPane().add(btnLauch);

		JButton btnTerminate = new JButton("Terminate");
		btnTerminate.setBounds(446, 82, 93, 23);
		frame.getContentPane().add(btnTerminate);

		JButton btnStop = new JButton("Stop");
		btnStop.setBounds(446, 148, 93, 23);
		frame.getContentPane().add(btnStop);

		JButton btnRun = new JButton("Run");
		btnRun.setBounds(446, 115, 93, 23);
		frame.getContentPane().add(btnRun);

		JButton btnReboot = new JButton("Reboot");
		btnReboot.setBounds(446, 181, 93, 23);
		frame.getContentPane().add(btnReboot);

		DefaultTableModel m = new DefaultTableModel();
		JTable table = new JTable(m);

		// Create a couple of columns
		m.addColumn("Property");
		m.addColumn("Value");

		// Append a row
		// m.addRow(new Object[]{"Property", "Value"});
		table.setBounds(30, 40, 200, 300);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(176, 52, 260, 260);
		frame.getContentPane().add(scrollPane);

		boolean done = false;
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		List<Instance> insts = new ArrayList<Instance>();
		while (!done) {
			DescribeInstancesResult response = InitialWindow.ec2Client.describeInstances(request);
			for (Reservation reservation : response.getReservations())
				for (Instance instance : reservation.getInstances()) {
					System.out.printf(
							"Found instance with id %s, " + "AMI %s, " + "type %s, " + "state %s "
									+ "and monitoring state %s" + "\n",
							instance.getInstanceId(), instance.getImageId(), instance.getInstanceType(),
							instance.getState().getName(), instance.getMonitoring().getState());
					insts.add(instance);

				}

			request.setNextToken(response.getNextToken());
			if (response.getNextToken() == null)
				done = true;
		}

		model.clear();
		for (int i = 0; i < insts.size(); i++)
			model.addElement(insts.get(i).getInstanceId());
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
//				String selected_bucket = list.getSelectedValue();
//				System.out.println(selected_bucket);
//				System.out.println(list.getSelectedIndex());
//				if (list.getSelectedIndex() == -1)
//					list.setSelectedIndex(0);
//				else {
//					ListObjectsV2Result result = InitialWindow.s3Client.listObjectsV2(selected_bucket);
//					List<S3ObjectSummary> objects = result.getObjectSummaries();
//					model_1.clear();
//					for (int i = 0; i < objects.size(); i++)
//						model_1.addElement(objects.get(i).getKey());
//				}
				String selected_instance = list.getSelectedValue();
				System.out.println(selected_instance);
				if (list.getSelectedIndex() == -1)
					list.setSelectedIndex(0);
				else {
					Instance temp = null;
					for (int i = 0; i < insts.size(); i++)
						if (insts.get(i).getInstanceId().equals(list.getSelectedValue()))
							temp = insts.get(i);
					System.out.printf(
							"Found instance with id %s, " + "AMI %s, " + "type %s, " + "state %s "
									+ "and monitoring state %s" + "\n",
							temp.getInstanceId(), temp.getImageId(), temp.getInstanceType(), temp.getState().getName(),
							temp.getMonitoring().getState());
//					for(int i = 0; i < m.getRowCount(); i++)
//						m.removeRow(i);
					m.setRowCount(0);
					String instNam = "";
					for(Tag tag : temp.getTags())
						if(tag.getKey().equals("Name"))
							instNam = tag.getValue();
					m.addRow(new Object[] {"Name", instNam});
					m.addRow(new Object[] {"AIM", temp.getImageId()});
					m.addRow(new Object[] {"Type", temp.getInstanceType()});
					m.addRow(new Object[] {"State", temp.getState().getName()});
					m.addRow(new Object[] {"Monit State", temp.getMonitoring().getState()});
				}
				
			}
		});

	}

}