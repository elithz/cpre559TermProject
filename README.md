# cpre559xTermProject
1. Problem Definition
1.1 Problem Statement
Using security services/mechanisms offered by AWS often requires a solid technological background in security/cryptography, which average users may not hold. The AWS console is complicated for untraining users. There is a need for developing a more friendly user interface prototype for non-security-professionals to deploy their systems to AWS.

1.2 Goals
The goal of the project is developing and deliver a more friendly user interface prototype for users without knowledge in the corresponding area to deploy their systems to AWS easily. 
We will deliver a fully functioning GUI access and management tool with AWS services, including S3, EC2, and VPC. It will allow the users to access and manage the different service of AWS, deploy their systems/services.
In addition, the one-button operations will be supported, which will automatically configure and deploy the default settings. Users do not need to manually configure the settings one by one. However, users may change the settings after or do manually configurations.

2. Conceptual Design
2.1 Composition
It’s consist of 5 major pages, which includes Login Page, Main page, EC2 instance page, S3 bucket page, and VPC page.

2.2 Functionalities
Login Page(Login with Access Key ID and Secret Access Key)
Main Console (Choose the services)
S3 Bucket Console (Add and Delete bucket, Upload and Delete Files)
EC2 Instance Console (Manage instances, Details, One button shortcuts with ELB)
VPC Comsole (Add and Delete, One-Button Create)

2.3 Interactions

2.4 Major Principles
The major algorithm used to connect to AWS in the project is supported by the AWS Java SDK.(AmazonS3ClientBuilder, AmazonEc2ClientBuilder,etc) And GUI is used for building the user interface.

3. Implementation Description
3.1 Platform and Tools
The program is built on Java 1.8 with AWS Java SDK.
The IDE that we chose is Eclipse with WindowBuilder extension and AWS Toolkit extension to achieve the goal of GUI programming as well as AWS API supports (external libraries from SDK).
It runs on Linux, Mac OS, and Windows which installed with latest JRE and have the GUI of OS.
3.2 Major Data Structure
In this project, we used many List structures to achieve the information.
3.3 Major Classes
There are five classes of this project:
InitialWindow - the login interface
Console - main function window
EC2Console - EC2 function window
S3Console - S3 function window
VPCConsole - VPCfunction window
In InitialWindow, it will start a UI and let the users login with their access key and access secret key.
In Console, it will let the users choose EC2 instance, S3 bucket or VPC service offered by AWS.
In EC2Console,  it will let the users terminate, run, stop or reboot it. It also allows the users to launch a new instance or launch with ELB, and create a new security group.
In S3Conole,  it will let the users add or delete a bucket, upload, download or delete files in the bucket. 
In VPCConsole, it will let the users check the status of VPC. It also allows the users to add a new VPC, a typical VPC or a typical VPC with subnets.

Because we use WindowBuilder, there are many inner classes built into the code of each class, they are:
ActionListener - button action listener for actions
ListSelectionListener - list selection listener for actions
There are also Java swing classes and Java AWT classes for the components of GUI, which include but not limited to:
JFrame - frame of the window component
JButton - button component
JTable - table component for list objects
JTextFeild - text component to let the user input
Choice - drop down menu component to let the user select from options
JPanel - container component to hold other components
JLabel - text label component to show text
JPopupMenu - popup menu when the user right clicks on the text/password fields
JPasswordField - text field for sensitive information like access key and secret access key
JOptionPane - a popup window to show information or let the user input relative parameters
To functionalize the abilities of AWS services, two main packages are used to handle the needs of using AWS functions, they are:
com.amazonaws.auth - provides credential authentication for the program
com.amazonaws.services - provides classes to implement service client build, to handle action requests and action results

4. User Guide
4.1 Install
To install the AWS Simple GUI Tool, you could directly download the runnable .jar file for our Google Drive, the address is: https://drive.google.com/drive/folders/1dDOGaB4k1VJpE_9LKYaV_Oo-OnpHsBvF?usp=sharing
Or you could access the source code on our Git Repository: https://github.com/elithz/cpre559TermProject
and build from source as your preference.

4.2 Login
The Login page will show up once you initiate the AWS Simple GUI Tool. It will ask you to enter the IAM Access Key and IAM Secret Access Key for login. You can choose different regions. The current version only supports us-east-1,us-east-2,us-west-1,us-west-2.


4.3 Choose Service
Once you logged in, you can choose EC2 instance, S3 bucket or VPC service offered by AWS.



4.4 EC2 instance
In the EC2 console, the current instances are listed on the left. You can check the properties and value by clicking the instance name. Once the instances are chosen, you are able to terminate, run, stop or reboot it. It also allows you to launch a new instance or launch with ELB, and create a new security group under specified VPC with the specified group name.


4.4.1 Launch new Instance
It will ask you to enter the image ID, choose the instance type, the key pair, and the subnet. Once launched, you can connect to the instance by SSH with the chosen keypair.


4.4.2 Launch with ELB (One button shortcut)
It will ask you to choose the subnet, enter the instance name, ELB and instance port.

It will automatically create a new key pair, security group, ELB, and instance which are all linked together. It will pop up a new window to let you choose where to store the key. 
Important: Downloading the key pair is highly recommended. (Note: AWS will NOT store your key) 

Once finished the above steps, the new instance is ready for deploying your system.

4.4.3 new SG
It will create a new security group under specified VPC with the specified group name.


4.5 S3 bucket
In the S3 console, the current buckets are listed on the left. You can check the content in each bucket by clicking the bucket name. You can add or delete a bucket by the button on the bottom. Once the bucket is chosen, you are able to upload, download or delete items/files in it. 




4.6 VPC
In the VPC console, the current instances are listed on the left. You can check the properties and value by clicking the VPC name. Once the VPC is chosen, you are able to check its status. It also allows you to add a new VPC, a typical VPC or a typical VPC with subnets.



4.6.1 Add VPC
It will ask you to enter the name, IPv4 CIDR block for the VPC. You can also choose No IPv6 or Amazon Provided IPv6. For the Tenancy, you can choose default or dedicated.


4.6.2 Add Typical VPC (One button short cut)
It will only ask you to enter the name of the VPC. The default settings for this button is IPv4:10.0.0.0/16. No IPv6 CIDR Block and default tenancy.


4.6.3 Typical VPC with subnets(One button short cut)
It will ask you to enter the VPC name, CIDR for private and public subnets. This button will create two new routable, two subnets, an internet gateway, an endpoint support S3, and a VPC, which are all linked together.


