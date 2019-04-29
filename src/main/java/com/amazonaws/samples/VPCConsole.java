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
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.CreateVpcRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DescribeVpcsRequest;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.amazonaws.services.ec2.model.Vpc;
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
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;

public class VPCConsole {

	private JFrame frame;
	public DefaultListModel<String> model;
	public DefaultListModel<String> model_1;
	public List<Vpc> vpcs = new ArrayList<Vpc>();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VPCConsole window = new VPCConsole();
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
	public VPCConsole() {
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

		JLabel lblVPC = new JLabel("VPC(s)");
		lblVPC.setBounds(10, 27, 82, 15);
		frame.getContentPane().add(lblVPC);

		JLabel lblStatus = new JLabel("Status");
		lblStatus.setBounds(198, 27, 54, 15);
		frame.getContentPane().add(lblStatus);

		JList<String> list = new JList<>(model);
		list.setBounds(10, 52, 156, 260);
		frame.getContentPane().add(list);

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
		
		JButton btnAddVpc = new JButton("Add VPC");
		btnAddVpc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					DescribeRegionsResult drr = InitialWindow.ec2Client.describeRegions();
					for (Region r : drr.getRegions())
						System.out.println(r.getRegionName());
					String name_tag = null;
					String IPv4_CIDR_block = null;
					String IPv6_CIDR_block = null;
					String Tenancy = null;
					System.out.println("Creating VPC ...");
					JTextField nametag = new JTextField();
					JTextField ipv4CIDRblock = new JTextField();
					Choice ipv6CIDRblock = new Choice();
					ipv6CIDRblock.add("No IPv6 CIDR Block");
					ipv6CIDRblock.add("Amazon provided IPv6 CIDR block");
					Choice tenancy = new Choice();
					tenancy.add("Dedicated");
					tenancy.add("Defult");
					
					Object[] message = { "Name Tag:", nametag, "IPv4 CIDR block:", ipv4CIDRblock, "IPv6 CIDR block:", ipv6CIDRblock,
							"Tenancy", tenancy };

					int option = JOptionPane.showConfirmDialog(null, message, "Creat VPC",
							JOptionPane.OK_CANCEL_OPTION);
					if (option == JOptionPane.OK_OPTION) {
						name_tag = nametag.getText();
						IPv4_CIDR_block = ipv4CIDRblock.getText();
						IPv6_CIDR_block = ipv6CIDRblock.getSelectedItem();
						Tenancy = tenancy.getSelectedItem();
					} else {
						System.out.println("VPC Creation canceled");
					}
					if (IPv4_CIDR_block != null && IPv6_CIDR_block != null && Tenancy != null) {
						System.out
								.println(name_tag + " " + IPv4_CIDR_block + " " + IPv6_CIDR_block + " " + Tenancy + "-- debug");
						CreateVpcRequest createVpcRequest = new CreateVpcRequest().withCidrBlock(IPv4_CIDR_block).withInstanceTenancy(Tenancy).with;
						RunInstancesResult result = InitialWindow.ec2Client.runInstances(runInstancesRequest);
					}
				} catch (AmazonS3Exception e1) {
					JOptionPane.showMessageDialog(null, "e1.getErrorMessage()");
					System.err.println(e1.getErrorMessage());
				}
			}
		});
		btnAddVpc.setBounds(446, 52, 93, 23);
		frame.getContentPane().add(btnAddVpc);

		boolean done = false;
		DescribeVpcsRequest request = new DescribeVpcsRequest();
		vpcs.clear();
		while (!done) {
			DescribeVpcsResult response = InitialWindow.ec2Client.describeVpcs(request);
			for (Vpc vpc : response.getVpcs())
				vpcs.add(vpc);
			request.setNextToken(response.getNextToken());
			if (response.getNextToken() == null)
				done = true;
		}

		model.clear();
		for (int i = 0; i < vpcs.size(); i++)
			model.addElement(vpcs.get(i).getVpcId());
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String selected_instance = list.getSelectedValue();
				System.out.println(selected_instance);
				if (list.getSelectedIndex() == -1)
					list.setSelectedIndex(0);
				else {
					Vpc temp = null;
					for (int i = 0; i < vpcs.size(); i++)
						if (vpcs.get(i).getVpcId().equals(list.getSelectedValue()))
							temp = vpcs.get(i);
					m.setRowCount(0);
					String vpcNam = "";
					for (Tag tag : temp.getTags())
						if (tag.getKey().equals("Name"))
							vpcNam = tag.getValue();
					m.addRow(new Object[] { "Name", vpcNam });
					m.addRow(new Object[] { "Tenancy", temp.getInstanceTenancy() });
					m.addRow(new Object[] { "IPv4 CIDR", temp.getCidrBlock() });
					m.addRow(new Object[] { "State", temp.getState() });
					m.addRow(new Object[] { "Owner ID", temp.getOwnerId() });
				}

			}
		});

	}
}