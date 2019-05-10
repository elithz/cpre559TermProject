package com.amazonaws.samples;

import java.awt.Choice;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeKeyPairsResult;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.DescribeVpcsRequest;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.KeyPairInfo;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.ResourceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Subnet;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagSpecification;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.amazonaws.services.ec2.model.Vpc;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.s3.model.AmazonS3Exception;

/***
 * 
 * EC2Console.java
 * 
 * @author elith, daiyuan
 * @version 2.1 NERVE Software 2019/5/10
 *
 */

public class EC2Console {

	private JFrame frmEcconsole;
	public DefaultListModel<String> model;
	public DefaultListModel<String> model_1;
	public List<Instance> insts = new ArrayList<Instance>();

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EC2Console window = new EC2Console();
					window.frmEcconsole.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public EC2Console() {
		initialize();
	}

	private void initialize() {
		frmEcconsole = new JFrame();
		frmEcconsole.setTitle("EC2Console");
		frmEcconsole.setBounds(100, 100, 589, 361);
		frmEcconsole.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmEcconsole.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Console.frmConsole.setEnabled(true);
			}

		});
		frmEcconsole.getContentPane().setLayout(null);

		model = new DefaultListModel<>();
		model_1 = new DefaultListModel<>();

		JLabel lblInstance = new JLabel("Instance(s)");
		lblInstance.setBounds(10, 27, 82, 15);
		frmEcconsole.getContentPane().add(lblInstance);

		JLabel lblStatus = new JLabel("Status");
		lblStatus.setBounds(198, 27, 54, 15);
		frmEcconsole.getContentPane().add(lblStatus);

		JList<String> list = new JList<>(model);
		list.setBounds(10, 52, 156, 260);
		frmEcconsole.getContentPane().add(list);

		JButton btnLauch = new JButton("Lauch New");
		btnLauch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// get available key pairs on aws
				DescribeKeyPairsResult rsps_kp = InitialWindow.ec2Client.describeKeyPairs();
				List<String> kp_name = new ArrayList<String>();
				for (KeyPairInfo key_pair : rsps_kp.getKeyPairs()) {
					System.out.printf("Found key pair with name %s " + "and fingerprint %s", key_pair.getKeyName(),
							key_pair.getKeyFingerprint() + "\n");
					kp_name.add(key_pair.getKeyName());
				}

				// get string list of instance type names
				List<String> it_name = new ArrayList<String>();
				for (InstanceType it : InstanceType.values())
					it_name.add(it.toString());
				// following code is originally used for list security groups, but from the
				// requirements of the runinstancerequest, the withsecuritygroup method is not
				// compatible with withsubnet,
				// so it is disabled
//				DescribeSecurityGroupsResult rsps_sg = InitialWindow.ec2Client.describeSecurityGroups();
//				List<String> sg_name = new ArrayList<String>();
//				for (SecurityGroup security_group : rsps_sg.getSecurityGroups()) {
//					System.out.printf("Found security group with name %s ", security_group.getDescription() + "\n");
//					sg_name.add(security_group.getGroupName());
//				}

				// get subnet names from the aws
				DescribeSubnetsResult describeSubnetsResult = InitialWindow.ec2Client.describeSubnets();
				List<String> sn_name = new ArrayList<String>();
				for (Subnet subnet : describeSubnetsResult.getSubnets())
					for (Tag tag : subnet.getTags())
						if (tag.getKey().equals("Name"))
							sn_name.add(tag.getValue());

				// these statements were used to list the image ids to run the instance, but it
				// took too long to
				// get them listed, so they are disabled
//				DescribeImagesResult rsps_ii = InitialWindow.ec2Client.describeImages();
//				List<String> ii_id = new ArrayList<String>();
//				for (Image image_id : rsps_ii.getImages()) {
//					System.out.printf("Found image with id %s ", image_id.getDescription() + "\n");
//					ii_id.add(image_id.getImageId());
//				}

				try {
					DescribeRegionsResult drr = InitialWindow.ec2Client.describeRegions();
					for (Region r : drr.getRegions())
						System.out.println(r.getRegionName());
					String image_id = null;
					String instance_type = null;
					String key_pair = null;
//					String security_group = null;
//					String sg_id = "";
					String subnet_1 = "";
					String sn_id = "";
					String inst_name = "";

					System.out.println("Creating ec2 instance");
					JTextField imageId = new JTextField();
					Choice instanceType = new Choice();
					for (String itname : it_name)
						instanceType.add(itname);

					Choice keyPair = new Choice();
					for (String keypair : kp_name)
						keyPair.add(keypair);

//					Choice securityGroup = new Choice();
//					for (String securitygroup : sg_name)
//						securityGroup.add(securitygroup);

					Choice subNet = new Choice();
					for (String sub_net : sn_name)
						subNet.add(sub_net);

					JTextField instName = new JTextField();

					Object[] message = { "Image ID:", imageId, "Instance Type:", instanceType, "Key Pair:", keyPair,
							"Subnet:", subNet, "Instance Name:", instName };

//					, "security group:", securityGroup

					// create option pane to ask user for instance information
					int option = JOptionPane.showConfirmDialog(null, message, "Creat Instance",
							JOptionPane.OK_CANCEL_OPTION);
					if (option == JOptionPane.OK_OPTION) {
						image_id = imageId.getText();
						instance_type = instanceType.getSelectedItem();
						key_pair = keyPair.getSelectedItem();
//						security_group = securityGroup.getSelectedItem();
						subnet_1 = subNet.getSelectedItem();
						inst_name = instName.getText();

//						for (SecurityGroup security_group_id : rsps_sg.getSecurityGroups())
//							if (security_group_id.getGroupName().equals(security_group))
//								sg_id = security_group_id.getGroupId();

						// when user finish input, program starts to create instance with provide info
						for (Subnet subnet_id : describeSubnetsResult.getSubnets())
							for (Tag tag : subnet_id.getTags())
								if (tag.getValue().equals(subnet_1))
									sn_id = subnet_id.getSubnetId();

						if (image_id != null && instance_type != null && key_pair != null && sn_id != null) {
							System.out.println(image_id + " " + instance_type + " " + key_pair + " " + sn_id + " "
									+ inst_name + " --debug");

							List<Tag> tags = new ArrayList<Tag>();
							tags.add(new Tag("Name", inst_name));
							TagSpecification tagconfig = new TagSpecification().withTags(tags)
									.withResourceType(ResourceType.Instance);
							RunInstancesRequest runInstancesRequest = new RunInstancesRequest().withImageId(image_id)
									.withInstanceType(instance_type).withMinCount(1).withMaxCount(1)
									.withKeyName(key_pair).withSubnetId(sn_id).withTagSpecifications(tagconfig);
//							withSecurityGroups(security_group).withSecurityGroupIds(sg_id).

							InitialWindow.ec2Client.runInstances(runInstancesRequest);
						}
					} else
						System.out.println("InstanceCreation canceled");

				} catch (AmazonS3Exception e1) {
					JOptionPane.showMessageDialog(null, "e1.getErrorMessage()");
					System.err.println(e1.getErrorMessage());
				}

				// instance list refresh
				boolean done = false;
				DescribeInstancesRequest request = new DescribeInstancesRequest();
				insts.clear();
				while (!done) {
					DescribeInstancesResult response = InitialWindow.ec2Client.describeInstances(request);
					for (Reservation reservation : response.getReservations())
						for (Instance instance : reservation.getInstances())
							insts.add(instance);

					request.setNextToken(response.getNextToken());
					if (response.getNextToken() == null)
						done = true;
				}
				model.clear();
				for (int i = 0; i < insts.size(); i++)
					model.addElement(insts.get(i).getInstanceId());
				list.setSelectedIndex(0);
			}
		});
		btnLauch.setBounds(446, 49, 117, 23);
		frmEcconsole.getContentPane().add(btnLauch);

		JButton btnTerminate = new JButton("Terminate");
		btnTerminate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selected_instance = list.getSelectedValue();
				System.out.println(selected_instance);
				Instance temp = null;
				for (int i = 0; i < insts.size(); i++)
					if (insts.get(i).getInstanceId().equals(list.getSelectedValue()))
						temp = insts.get(i);

				// terminate instance by submitting terminate request
				TerminateInstancesRequest tir = new TerminateInstancesRequest().withInstanceIds(temp.getInstanceId());
				TerminateInstancesResult tirst = InitialWindow.ec2Client.terminateInstances(tir);
				// debug the termination status
				for (InstanceStateChange isc : tirst.getTerminatingInstances())
					System.out.println("Terminated: " + isc.getInstanceId() + ". Previous: "
							+ isc.getPreviousState().getName() + ". Now: " + isc.getCurrentState().getName());

				// refresh instance list
				boolean done = false;
				DescribeInstancesRequest request = new DescribeInstancesRequest();
				insts.clear();
				while (!done) {
					DescribeInstancesResult response = InitialWindow.ec2Client.describeInstances(request);
					for (Reservation reservation : response.getReservations())
						for (Instance instance : reservation.getInstances())
							insts.add(instance);

					request.setNextToken(response.getNextToken());
					if (response.getNextToken() == null)
						done = true;
				}
				model.clear();
				for (int i = 0; i < insts.size(); i++)
					model.addElement(insts.get(i).getInstanceId());
				list.setSelectedIndex(0);
			}
		});
		btnTerminate.setBounds(446, 82, 117, 23);
		frmEcconsole.getContentPane().add(btnTerminate);

		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selected_instance = list.getSelectedValue();
				System.out.println(selected_instance);
				Instance temp = null;
				for (int i = 0; i < insts.size(); i++)
					if (insts.get(i).getInstanceId().equals(list.getSelectedValue()))
						temp = insts.get(i);

				// stop instance by submitting stop request
				StopInstancesRequest stop_request = new StopInstancesRequest().withInstanceIds(temp.getInstanceId());

				InitialWindow.ec2Client.stopInstances(stop_request);

				// refresh instance list
				boolean done = false;
				DescribeInstancesRequest request = new DescribeInstancesRequest();
				insts.clear();
				while (!done) {
					DescribeInstancesResult response = InitialWindow.ec2Client.describeInstances(request);
					for (Reservation reservation : response.getReservations())
						for (Instance instance : reservation.getInstances())
							insts.add(instance);

					request.setNextToken(response.getNextToken());
					if (response.getNextToken() == null)
						done = true;
				}
				model.clear();
				for (int i = 0; i < insts.size(); i++)
					model.addElement(insts.get(i).getInstanceId());
				list.setSelectedIndex(0);
			}
		});
		btnStop.setBounds(446, 148, 117, 23);
		frmEcconsole.getContentPane().add(btnStop);

		JButton btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selected_instance = list.getSelectedValue();
				System.out.println(selected_instance);
				Instance temp = null;
				for (int i = 0; i < insts.size(); i++)
					if (insts.get(i).getInstanceId().equals(list.getSelectedValue()))
						temp = insts.get(i);

				// run instance
				StartInstancesRequest start_request = new StartInstancesRequest().withInstanceIds(temp.getInstanceId());

				InitialWindow.ec2Client.startInstances(start_request);

				// refresh instance list
				boolean done = false;
				DescribeInstancesRequest request = new DescribeInstancesRequest();
				insts.clear();
				while (!done) {
					DescribeInstancesResult response = InitialWindow.ec2Client.describeInstances(request);
					for (Reservation reservation : response.getReservations())
						for (Instance instance : reservation.getInstances())
							insts.add(instance);

					request.setNextToken(response.getNextToken());
					if (response.getNextToken() == null)
						done = true;
				}
				model.clear();
				for (int i = 0; i < insts.size(); i++)
					model.addElement(insts.get(i).getInstanceId());
				list.setSelectedIndex(0);
			}
		});
		btnRun.setBounds(446, 115, 117, 23);
		frmEcconsole.getContentPane().add(btnRun);

		JButton btnReboot = new JButton("Reboot");
		btnReboot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selected_instance = list.getSelectedValue();
				System.out.println(selected_instance);
				Instance temp = null;
				for (int i = 0; i < insts.size(); i++)
					if (insts.get(i).getInstanceId().equals(list.getSelectedValue()))
						temp = insts.get(i);

				// reboot instance
				RebootInstancesRequest start_request = new RebootInstancesRequest()
						.withInstanceIds(temp.getInstanceId());
				InitialWindow.ec2Client.rebootInstances(start_request);

				// refresh instance list
				boolean done = false;
				DescribeInstancesRequest request = new DescribeInstancesRequest();
				insts.clear();
				while (!done) {
					DescribeInstancesResult response = InitialWindow.ec2Client.describeInstances(request);
					for (Reservation reservation : response.getReservations())
						for (Instance instance : reservation.getInstances())
							insts.add(instance);

					request.setNextToken(response.getNextToken());
					if (response.getNextToken() == null)
						done = true;
				}
				model.clear();
				for (int i = 0; i < insts.size(); i++)
					model.addElement(insts.get(i).getInstanceId());
				list.setSelectedIndex(0);
			}
		});
		btnReboot.setBounds(446, 181, 117, 23);
		frmEcconsole.getContentPane().add(btnReboot);

		// add jtable to display instance properties on frame
		DefaultTableModel m = new DefaultTableModel();
		JTable table = new JTable(m);

		m.addColumn("Property");
		m.addColumn("Value");

		table.setBounds(30, 40, 200, 300);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(176, 52, 260, 260);
		frmEcconsole.getContentPane().add(scrollPane);

		// this button create an instance with a new key pair, a new security group,
		// and a new ELB
		JButton btnLaunchWithElb = new JButton("<html>Launch<br /> With ELB</html>");
		btnLaunchWithElb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				DescribeSubnetsResult describeSubnetsResult = InitialWindow.ec2Client.describeSubnets();
				List<String> sn_name = new ArrayList<String>();
				for (Subnet subnet : describeSubnetsResult.getSubnets())
					for (Tag tag : subnet.getTags())
						if (tag.getKey().equals("Name"))
							sn_name.add(tag.getValue());

				try {
					DescribeRegionsResult drr = InitialWindow.ec2Client.describeRegions();
					for (Region r : drr.getRegions())
						System.out.println(r.getRegionName());
					String key_pair = null;
					String subnet_1 = "";
					String sn_id = "";
					String inst_name = "";
					int listener_port = 0;
					int instance_port = 0;

					System.out.println("Creating ec2 instance");

					JTextField instName = new JTextField();
					Choice subNet = new Choice();
					for (String sub_net : sn_name)
						subNet.add(sub_net);
					JTextField listenerPort = new JTextField();
					JTextField instancePort = new JTextField();

					Object[] message = { "Subnet:", subNet, "Instance Name:", instName, "ELB Port:", listenerPort,
							"Instance Port:", instancePort };

					// ask users to input necessary info
					int option = JOptionPane.showConfirmDialog(null, message, "Creat Instance",
							JOptionPane.OK_CANCEL_OPTION);
					if (option == JOptionPane.OK_OPTION) {
						subnet_1 = subNet.getSelectedItem();
						inst_name = instName.getText();
						listener_port = Integer.parseInt(listenerPort.getText());
						instance_port = Integer.parseInt(instancePort.getText());

						for (Subnet subnet_id : describeSubnetsResult.getSubnets())
							for (Tag tag : subnet_id.getTags())
								if (tag.getValue().equals(subnet_1))
									sn_id = subnet_id.getSubnetId();
						System.out.println("selected sn id: " + sn_id);
						// create new key pair
						CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest()
								.withKeyName(inst_name + "keypair");
						CreateKeyPairResult createKeyPairResult = InitialWindow.ec2Client
								.createKeyPair(createKeyPairRequest);
						key_pair = createKeyPairResult.getKeyPair().getKeyName();
						String privatekey = createKeyPairResult.getKeyPair().getKeyMaterial();
						// store the key pair to local directory specified by user
						System.out.println("Storing key pair to local derictory");
						JFileChooser chooser = new JFileChooser();
						chooser.setCurrentDirectory(new java.io.File("."));
						chooser.setDialogTitle("Choose the directory you want to save the file");
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						chooser.setAcceptAllFileFilterUsed(false);
						FileNameExtensionFilter filter = new FileNameExtensionFilter("PEM", "pem");
						chooser.setFileFilter(filter);
						if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
							String save_path = chooser.getSelectedFile().getAbsolutePath();
							try {
								FileOutputStream fos = new FileOutputStream(
										new File(save_path + File.separator + key_pair + ".pem"));
								fos.write(privatekey.getBytes(), 0, privatekey.length());
								System.out.println("Download finished");
								fos.close();
							} catch (AmazonServiceException e1) {
								System.err.println(e1.getErrorMessage());
								System.exit(1);
							} catch (FileNotFoundException e1) {
								System.err.println(e1.getMessage());
								System.exit(1);
							} catch (IOException e1) {
								System.err.println(e1.getMessage());
								System.exit(1);
							}
						} else
							System.out.println("user canceled download");
						if (key_pair != null && sn_id != null && listener_port != 0 && instance_port != 0) {
							System.out.println(key_pair + " " + sn_id + " " + inst_name + " --debug");

							String vpcID = "";
							for (Subnet subnet : describeSubnetsResult.getSubnets())
								for (Tag tag : subnet.getTags())
									if (tag.getValue().equals(subnet_1))
										vpcID = subnet.getVpcId();

							// create new security group
							CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest()
									.withGroupName(subnet_1 + "secugrp").withVpcId(vpcID)
									.withDescription(inst_name + "sg" + vpcID);
							String sgid = InitialWindow.ec2Client.createSecurityGroup(createSecurityGroupRequest)
									.getGroupId();

							List<Tag> tags = new ArrayList<Tag>();
							tags.add(new Tag("Name", inst_name));
							TagSpecification tagconfig = new TagSpecification().withTags(tags)
									.withResourceType(ResourceType.Instance);
							// create new instance with created key pair and security group and specified
							// subnet
							RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
									.withImageId("ami-02bcbb802e03574ba").withInstanceType("t2.micro").withMinCount(1)
									.withMaxCount(1).withKeyName(key_pair).withSecurityGroupIds(sgid)
									.withSubnetId(sn_id).withTagSpecifications(tagconfig);

							RunInstancesResult runInstancesResult = InitialWindow.ec2Client
									.runInstances(runInstancesRequest);
							com.amazonaws.services.elasticloadbalancing.model.Instance tempinstance = new com.amazonaws.services.elasticloadbalancing.model.Instance();
							tempinstance.setInstanceId(
									runInstancesResult.getReservation().getInstances().get(0).getInstanceId());

							// create the ELB
							CreateLoadBalancerRequest createLoadBalancerRequest = new CreateLoadBalancerRequest()
									.withLoadBalancerName(inst_name + "elb").withSubnets(sn_id).withSecurityGroups(sgid)
									.withListeners(
											new Listener().withInstancePort(instance_port).withInstanceProtocol("TCP")
													.withProtocol("TCP").withLoadBalancerPort(listener_port));
							InitialWindow.elbClient.createLoadBalancer(createLoadBalancerRequest);

							RegisterInstancesWithLoadBalancerRequest registerInstancesWithLoadBalancerRequest = new RegisterInstancesWithLoadBalancerRequest()
									.withLoadBalancerName(inst_name + "elb").withInstances(tempinstance);
							InitialWindow.elbClient
									.registerInstancesWithLoadBalancer(registerInstancesWithLoadBalancerRequest);

						}
					} else
						System.out.println("InstanceCreation canceled");

				} catch (AmazonS3Exception e1) {
					JOptionPane.showMessageDialog(null, "e1.getErrorMessage()");
					System.err.println(e1.getErrorMessage());
				}

				// refresh the instance list
				boolean done = false;
				DescribeInstancesRequest request = new DescribeInstancesRequest();
				insts.clear();
				while (!done) {
					DescribeInstancesResult response = InitialWindow.ec2Client.describeInstances(request);
					for (Reservation reservation : response.getReservations())
						for (Instance instance : reservation.getInstances())
							insts.add(instance);

					request.setNextToken(response.getNextToken());
					if (response.getNextToken() == null)
						done = true;
				}
				model.clear();
				for (int i = 0; i < insts.size(); i++)
					model.addElement(insts.get(i).getInstanceId());
				list.setSelectedIndex(0);
			}
		});
		btnLaunchWithElb.setBounds(446, 214, 117, 58);
		frmEcconsole.getContentPane().add(btnLaunchWithElb);

		// this button add a new security group with default rules and user specified
		// group name (not tag)
		JButton btnNewSg = new JButton("New SG");
		btnNewSg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean done = false;
				DescribeVpcsRequest describeVpcsRequest = new DescribeVpcsRequest();
				List<Vpc> vpcs = new ArrayList<Vpc>();
				while (!done) {
					DescribeVpcsResult describeVpcsResult = InitialWindow.ec2Client.describeVpcs(describeVpcsRequest);
					for (Vpc vpc : describeVpcsResult.getVpcs())
						vpcs.add(vpc);
					describeVpcsRequest.setNextToken(describeVpcsResult.getNextToken());
					if (describeVpcsResult.getNextToken() == null)
						done = true;
				}

				JTextField sgname = new JTextField();
				Choice vpcid = new Choice();
				for (int i = 0; i < vpcs.size(); i++)
					vpcid.add(vpcs.get(i).getVpcId());
				JTextField description = new JTextField();
				Object[] message = { "SG Name:", sgname, "VPC:", vpcid, "Description:", description };
				String sgName = null;
				String vpcId = null;
				String desCription = null;
				int option = JOptionPane.showConfirmDialog(null, message, "Creat Instance",
						JOptionPane.OK_CANCEL_OPTION);
				if (option == JOptionPane.OK_OPTION) {
					sgName = sgname.getText();
					vpcId = vpcid.getSelectedItem();
					desCription = description.getText();
				} else
					System.out.println("InstanceCreation canceled");

				// create security group with specified vpc id and group name
				CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest()
						.withGroupName(sgName).withVpcId(vpcId).withDescription(desCription);
				String sgid = InitialWindow.ec2Client.createSecurityGroup(createSecurityGroupRequest).getGroupId();
			}
		});
		btnNewSg.setBounds(446, 282, 117, 23);
		frmEcconsole.getContentPane().add(btnNewSg);

		// refresh instance list (initial)
		boolean done = false;
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		insts.clear();
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
				// list the instance properties when select different instance from the instance
				// list
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

					m.setRowCount(0);
					String instNam = "";
					for (Tag tag : temp.getTags())
						if (tag.getKey().equals("Name"))
							instNam = tag.getValue();
					m.addRow(new Object[] { "Name", instNam });
					m.addRow(new Object[] { "AMI", temp.getImageId() });
					m.addRow(new Object[] { "Type", temp.getInstanceType() });
					m.addRow(new Object[] { "State", temp.getState().getName() });
					m.addRow(new Object[] { "Monit State", temp.getMonitoring().getState() });
				}

			}
		});

	}
}