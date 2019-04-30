package com.amazonaws.samples;

import java.awt.Choice;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateVpcRequest;
import com.amazonaws.services.ec2.model.CreateVpcResult;
import com.amazonaws.services.ec2.model.DeleteVpcRequest;
import com.amazonaws.services.ec2.model.DeleteVpcResult;
import com.amazonaws.services.ec2.model.DescribeVpcsRequest;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.Vpc;
import com.amazonaws.services.s3.model.AmazonS3Exception;

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
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
					tenancy.add("dedicated");
					tenancy.add("default");

					Object[] message = { "Name Tag:", nametag, "IPv4 CIDR block:", ipv4CIDRblock, "IPv6 CIDR block:",
							ipv6CIDRblock, "Tenancy", tenancy };

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
						System.out.println(
								name_tag + " " + IPv4_CIDR_block + " " + IPv6_CIDR_block + " " + Tenancy + "-- debug");
						CreateVpcRequest createVpcRequest = new CreateVpcRequest();
						if (IPv6_CIDR_block.equals("No IPv6 CIDR Block"))
							createVpcRequest = new CreateVpcRequest().withCidrBlock(IPv4_CIDR_block)
									.withInstanceTenancy(Tenancy).withAmazonProvidedIpv6CidrBlock(false);
						else
							createVpcRequest = new CreateVpcRequest().withCidrBlock(IPv4_CIDR_block)
									.withInstanceTenancy(Tenancy).withAmazonProvidedIpv6CidrBlock(true);

						CreateVpcResult createVpcResult = InitialWindow.ec2Client.createVpc(createVpcRequest);
						List<Tag> tag = new ArrayList<Tag>();
						tag.add(new Tag("Name", name_tag));
						Vpc tem = createVpcResult.getVpc();
						tem.setTags(tag);
						String createdVpcId = createVpcResult.getVpc().getVpcId();
						CreateTagsRequest createTagsRequest = new CreateTagsRequest();
						createTagsRequest.setTags(tag);
						createTagsRequest.withResources(createdVpcId);
						InitialWindow.ec2Client.createTags(createTagsRequest);
						System.out.println("creation done.");

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
					}
				} catch (AmazonS3Exception e1) {
					JOptionPane.showMessageDialog(null, "e1.getErrorMessage()");
					System.err.println(e1.getErrorMessage());
				}
			}
		});
		btnAddVpc.setBounds(446, 52, 118, 23);
		frame.getContentPane().add(btnAddVpc);

		JButton btnDeleteVpc = new JButton("Delete VPC");
		btnDeleteVpc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String selected_vpc = list.getSelectedValue();
				System.out.println(selected_vpc);
				Vpc temp = null;
				for (int i = 0; i < vpcs.size(); i++)
					if (vpcs.get(i).getVpcId().equals(list.getSelectedValue()))
						temp = vpcs.get(i);

				DeleteVpcRequest deleteVpcRequest = new DeleteVpcRequest().withVpcId(selected_vpc);
				DeleteVpcResult deleteVpcResult = InitialWindow.ec2Client.deleteVpc(deleteVpcRequest);
				System.out.println(deleteVpcResult.toString() + "Delete done");

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
			}
		});
		btnDeleteVpc.setBounds(446, 85, 118, 23);
		frame.getContentPane().add(btnDeleteVpc);

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