package com.amazonaws.samples;

import java.awt.Choice;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import com.amazonaws.services.ec2.model.AssociateRouteTableRequest;
import com.amazonaws.services.ec2.model.AttachInternetGatewayRequest;
import com.amazonaws.services.ec2.model.CreateInternetGatewayRequest;
import com.amazonaws.services.ec2.model.CreateInternetGatewayResult;
import com.amazonaws.services.ec2.model.CreateRouteRequest;
import com.amazonaws.services.ec2.model.CreateRouteTableRequest;
import com.amazonaws.services.ec2.model.CreateRouteTableResult;
import com.amazonaws.services.ec2.model.CreateSubnetRequest;
import com.amazonaws.services.ec2.model.CreateSubnetResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateVpcEndpointRequest;
import com.amazonaws.services.ec2.model.CreateVpcEndpointResult;
import com.amazonaws.services.ec2.model.CreateVpcRequest;
import com.amazonaws.services.ec2.model.CreateVpcResult;
import com.amazonaws.services.ec2.model.DeleteVpcRequest;
import com.amazonaws.services.ec2.model.DeleteVpcResult;
import com.amazonaws.services.ec2.model.DescribeRouteTablesResult;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.DescribeVpcsRequest;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.RouteTable;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.Tenancy;
import com.amazonaws.services.ec2.model.Vpc;
import com.amazonaws.services.s3.model.AmazonS3Exception;

public class VPCConsole {

	private JFrame frmVpcconsole;
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
					window.frmVpcconsole.setVisible(true);
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
		frmVpcconsole = new JFrame();
		frmVpcconsole.setTitle("VPCConsole");
		frmVpcconsole.setBounds(100, 100, 590, 361);
		frmVpcconsole.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmVpcconsole.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Console.frmConsole.setEnabled(true);
			}

		});
		frmVpcconsole.getContentPane().setLayout(null);

		model = new DefaultListModel<>();
		model_1 = new DefaultListModel<>();

		JLabel lblVPC = new JLabel("VPC(s)");
		lblVPC.setBounds(10, 27, 82, 15);
		frmVpcconsole.getContentPane().add(lblVPC);

		JLabel lblStatus = new JLabel("Status");
		lblStatus.setBounds(198, 27, 54, 15);
		frmVpcconsole.getContentPane().add(lblStatus);

		JList<String> list = new JList<>(model);
		list.setBounds(10, 52, 156, 260);
		frmVpcconsole.getContentPane().add(list);

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
		frmVpcconsole.getContentPane().add(scrollPane);

		JButton btnAddVpc = new JButton("Add VPC");
		btnAddVpc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String name_tag = null;
					String IPv4_CIDR_block = null;
					String IPv6_CIDR_block = null;
					Tenancy tenancy = null;
					System.out.println("Creating VPC ...");
					JTextField nametag = new JTextField();
					JTextField ipv4CIDRblock = new JTextField();
					Choice ipv6CIDRblock = new Choice();
					ipv6CIDRblock.add("No IPv6 CIDR Block");
					ipv6CIDRblock.add("Amazon provided IPv6 CIDR block");
					Choice tenancychoice = new Choice();
					tenancychoice.add("Default");
					tenancychoice.add("Dedicated");

					Object[] message = { "Name:", nametag, "IPv4 CIDR block:", ipv4CIDRblock, "IPv6 CIDR block:",
							ipv6CIDRblock, "Tenancy", tenancychoice };

					int option = JOptionPane.showConfirmDialog(null, message, "Creat VPC",
							JOptionPane.OK_CANCEL_OPTION);
					if (option == JOptionPane.OK_OPTION) {
						name_tag = nametag.getText();
						IPv4_CIDR_block = ipv4CIDRblock.getText();
						IPv6_CIDR_block = ipv6CIDRblock.getSelectedItem();
						if (tenancychoice.getSelectedItem().equals("Default"))
							tenancy = Tenancy.Default;
						else
							tenancy = Tenancy.Dedicated;
					} else {
						System.out.println("VPC Creation canceled");
					}
					if (IPv4_CIDR_block != null && IPv6_CIDR_block != null && tenancy != null) {
						System.out.println(name_tag + " " + IPv4_CIDR_block + " " + IPv6_CIDR_block + " "
								+ tenancy.name() + "-- debug");
						CreateVpcRequest createVpcRequest = new CreateVpcRequest();
						if (IPv6_CIDR_block.equals("No IPv6 CIDR Block"))
							createVpcRequest = new CreateVpcRequest().withCidrBlock(IPv4_CIDR_block)
									.withInstanceTenancy(tenancy).withAmazonProvidedIpv6CidrBlock(false);
						else
							createVpcRequest = new CreateVpcRequest().withCidrBlock(IPv4_CIDR_block)
									.withInstanceTenancy(tenancy).withAmazonProvidedIpv6CidrBlock(true);

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
		frmVpcconsole.getContentPane().add(btnAddVpc);

		JButton btnDeleteVpc = new JButton("Delete VPC");
		btnDeleteVpc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String selected_vpc = list.getSelectedValue();
				System.out.println(selected_vpc);

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
		frmVpcconsole.getContentPane().add(btnDeleteVpc);

		JButton button = new JButton("<html>Add Typical<br />VPC</html>");
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String name_tag = null;
					String IPv4_CIDR_block = "10.0.0.0/16";
					String IPv6_CIDR_block = "No IPv6 CIDR Block";
					Tenancy tenancy = Tenancy.Default;
					System.out.println("Creating VPC ...");
					JTextField nametag = new JTextField();

					Object[] message = { "Name:", nametag };

					int option = JOptionPane.showConfirmDialog(null, message, "Creat VPC",
							JOptionPane.OK_CANCEL_OPTION);
					if (option == JOptionPane.OK_OPTION) {
						name_tag = nametag.getText();
					} else {
						System.out.println("VPC Creation canceled");
					}

					System.out.println(name_tag + " " + IPv4_CIDR_block + " " + IPv6_CIDR_block + " " + tenancy.name()
							+ "-- debug");
					CreateVpcRequest createVpcRequest = new CreateVpcRequest().withCidrBlock(IPv4_CIDR_block)
							.withInstanceTenancy(tenancy).withAmazonProvidedIpv6CidrBlock(false);

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

				} catch (AmazonS3Exception e1) {
					JOptionPane.showMessageDialog(null, "e1.getErrorMessage()");
					System.err.println(e1.getErrorMessage());
				}
			}
		});
		button.setBounds(446, 118, 118, 39);
		frmVpcconsole.getContentPane().add(button);

		JButton button_1 = new JButton("<html>Typical VPC<br />with subnets</html>");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String name_tag = null;
					String IPv4_CIDR_block = "10.0.0.0/16";
					String IPv6_CIDR_block = "No IPv6 CIDR Block";
					String subnet_IPv4_CIDR_block_private = "";
					String subnet_IPv4_CIDR_block_public = "";
					Tenancy tenancy = Tenancy.Default;

					System.out.println("Creating VPC ...");
					JTextField nametag = new JTextField();
					JTextField sbCIDRprivate = new JTextField();
					JTextField sbCIDRpublic = new JTextField();

					Object[] message = { "VPC Name:", nametag, "private subnet CIDR", sbCIDRprivate,
							"public subnet CIDR", sbCIDRpublic };

					int option = JOptionPane.showConfirmDialog(null, message, "Creat VPC",
							JOptionPane.OK_CANCEL_OPTION);
					if (option == JOptionPane.OK_OPTION) {
						name_tag = nametag.getText();
						subnet_IPv4_CIDR_block_private = sbCIDRprivate.getText();
						subnet_IPv4_CIDR_block_public = sbCIDRpublic.getText();
					} else {
						System.out.println("VPC Creation canceled");
					}

					System.out.println(name_tag + " " + IPv4_CIDR_block + " " + IPv6_CIDR_block + " " + tenancy.name()
							+ "-- debug");
					CreateVpcRequest createVpcRequest = new CreateVpcRequest().withCidrBlock(IPv4_CIDR_block)
							.withInstanceTenancy(tenancy).withAmazonProvidedIpv6CidrBlock(false);

					CreateVpcResult createVpcResult = InitialWindow.ec2Client.createVpc(createVpcRequest);
					List<Tag> tag = new ArrayList<Tag>();
					tag.add(new Tag("Name", name_tag));
					String createdVpcId = createVpcResult.getVpc().getVpcId();
					CreateTagsRequest createTagsRequest = new CreateTagsRequest();
					createTagsRequest.setTags(tag);
					createTagsRequest.withResources(createdVpcId);
					InitialWindow.ec2Client.createTags(createTagsRequest);
					System.out.println("VPC creation done.");

					CreateRouteTableRequest createRouteTableRequest = new CreateRouteTableRequest()
							.withVpcId(createdVpcId);
					CreateRouteTableResult createRouteTableResult = InitialWindow.ec2Client
							.createRouteTable(createRouteTableRequest);
					String routetableid_private = createRouteTableResult.getRouteTable().getRouteTableId();
					List<Tag> tag1 = new ArrayList<Tag>();
					tag1.add(new Tag("Name", name_tag + "privateroutetable"));
					CreateTagsRequest createTagsRequest1 = new CreateTagsRequest();
					createTagsRequest1.setTags(tag1);
					createTagsRequest1.withResources(routetableid_private);
					InitialWindow.ec2Client.createTags(createTagsRequest1);

					CreateRouteTableRequest createRouteTableRequest1 = new CreateRouteTableRequest()
							.withVpcId(createdVpcId);
					CreateRouteTableResult createRouteTableResult1 = InitialWindow.ec2Client
							.createRouteTable(createRouteTableRequest1);
					String routetableid_public = createRouteTableResult1.getRouteTable().getRouteTableId();
					List<Tag> tag2 = new ArrayList<Tag>();
					tag2.add(new Tag("Name", name_tag + "publicroutetable"));
					CreateTagsRequest createTagsRequest2 = new CreateTagsRequest();
					createTagsRequest2.setTags(tag2);
					createTagsRequest2.withResources(routetableid_public);
					InitialWindow.ec2Client.createTags(createTagsRequest2);

					CreateSubnetRequest createSubnetRequest = new CreateSubnetRequest().withVpcId(createdVpcId)
							.withCidrBlock(subnet_IPv4_CIDR_block_private);
					CreateSubnetResult createSubnetResult = InitialWindow.ec2Client.createSubnet(createSubnetRequest);
					String subnetid_private = createSubnetResult.getSubnet().getSubnetId();
					List<Tag> tag3 = new ArrayList<Tag>();
					tag3.add(new Tag("Name", name_tag + "privaesubnet"));
					CreateTagsRequest createTagsRequest3 = new CreateTagsRequest();
					createTagsRequest3.setTags(tag3);
					createTagsRequest3.withResources(subnetid_private);
					InitialWindow.ec2Client.createTags(createTagsRequest3);

					CreateSubnetRequest createSubnetRequest1 = new CreateSubnetRequest().withVpcId(createdVpcId)
							.withCidrBlock(subnet_IPv4_CIDR_block_public);
					CreateSubnetResult createSubnetResult1 = InitialWindow.ec2Client.createSubnet(createSubnetRequest1);
					String subnetid_public = createSubnetResult1.getSubnet().getSubnetId();
					List<Tag> tag4 = new ArrayList<Tag>();
					tag4.add(new Tag("Name", name_tag + "privaesubnet"));
					CreateTagsRequest createTagsRequest4 = new CreateTagsRequest();
					createTagsRequest4.setTags(tag4);
					createTagsRequest4.withResources(subnetid_public);
					InitialWindow.ec2Client.createTags(createTagsRequest4);

					AssociateRouteTableRequest associateRouteTableRequest = new AssociateRouteTableRequest()
							.withRouteTableId(routetableid_private).withSubnetId(subnetid_private);
					InitialWindow.ec2Client.associateRouteTable(associateRouteTableRequest);

					AssociateRouteTableRequest associateRouteTableRequest1 = new AssociateRouteTableRequest()
							.withRouteTableId(routetableid_public).withSubnetId(subnetid_public);
					InitialWindow.ec2Client.associateRouteTable(associateRouteTableRequest1);

					CreateInternetGatewayRequest createInternetGatewayRequest = new CreateInternetGatewayRequest();
					CreateInternetGatewayResult createInternetGatewayResult = InitialWindow.ec2Client
							.createInternetGateway(createInternetGatewayRequest);
					String igwid = createInternetGatewayResult.getInternetGateway().getInternetGatewayId();
					List<Tag> tag5 = new ArrayList<Tag>();
					tag5.add(new Tag("Name", name_tag + "internetgw"));
					CreateTagsRequest createTagsRequest5 = new CreateTagsRequest();
					createTagsRequest5.setTags(tag5);
					createTagsRequest5.withResources(igwid);
					InitialWindow.ec2Client.createTags(createTagsRequest5);

					AttachInternetGatewayRequest attachInternetGatewayRequest = new AttachInternetGatewayRequest()
							.withInternetGatewayId(igwid).withVpcId(createdVpcId);
					InitialWindow.ec2Client.attachInternetGateway(attachInternetGatewayRequest);

					CreateRouteRequest createRouteRequest = new CreateRouteRequest()
							.withRouteTableId(routetableid_public).withGatewayId(igwid)
							.withDestinationCidrBlock("0.0.0.0/0");
					InitialWindow.ec2Client.createRoute(createRouteRequest);

					CreateVpcEndpointRequest createVpcEndpointRequest = new CreateVpcEndpointRequest()
							.withRouteTableIds(routetableid_private).withVpcId(createdVpcId)
							.withServiceName("com.amazonaws.us-east-2.s3");
					CreateVpcEndpointResult createVpcEndpointResult = InitialWindow.ec2Client
							.createVpcEndpoint(createVpcEndpointRequest);
					String vpceid = createVpcEndpointResult.getVpcEndpoint().getVpcEndpointId();
					List<Tag> tag6 = new ArrayList<Tag>();
					tag6.add(new Tag("Name", name_tag + "endpoint"));
					CreateTagsRequest createTagsRequest6 = new CreateTagsRequest();
					createTagsRequest6.setTags(tag6);
					createTagsRequest6.withResources(vpceid);
					InitialWindow.ec2Client.createTags(createTagsRequest6);

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

				} catch (AmazonS3Exception e1) {
					JOptionPane.showMessageDialog(null, "e1.getErrorMessage()");
					System.err.println(e1.getErrorMessage());
				}
			}
		});
		button_1.setHorizontalTextPosition(SwingConstants.CENTER);
		button_1.setBounds(446, 167, 118, 39);
		frmVpcconsole.getContentPane().add(button_1);

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
					DescribeRouteTablesResult describeRouteTablesResult = InitialWindow.ec2Client.describeRouteTables();
					List<String> rt_id = new ArrayList<String>();
					for (RouteTable routetable : describeRouteTablesResult.getRouteTables())
						if (routetable.getVpcId().equals(temp.getVpcId()))
							rt_id.add(routetable.getRouteTableId());

					DescribeSubnetsResult describeSubnetsResult = InitialWindow.ec2Client.describeSubnets();
					List<String> sn_id = new ArrayList<String>();
					for (Subnet subnet : describeSubnetsResult.getSubnets())
						if (subnet.getVpcId().equals(temp.getVpcId()))
							sn_id.add(subnet.getSubnetId());

					m.addRow(new Object[] { "Name", vpcNam });
					m.addRow(new Object[] { "Tenancy", temp.getInstanceTenancy() });
					m.addRow(new Object[] { "IPv4 CIDR", temp.getCidrBlock() });
					m.addRow(new Object[] { "State", temp.getState() });
					m.addRow(new Object[] { "Owner ID", temp.getOwnerId() });
					m.addRow(new Object[] { "Default VPC", temp.isDefault() });
					for (String snid : sn_id)
						m.addRow(new Object[] { "Subnet(s)", snid });
					for (String rtid : rt_id)
						m.addRow(new Object[] { "Routetable(s)", rtid });
				}

			}
		});

	}
}