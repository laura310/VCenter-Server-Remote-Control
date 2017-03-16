import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;

/**
 * Java-based program to interact with CMPE vCenter server.
 * 
 * @author laurajoe
 *
 */
public class HelloVM {
	public static void main(String[] args) throws Exception {
		
//		ServiceInstance si = new ServiceInstance(new URL("https://130.65.159.14/sdk"), 
//				"cmpe281_sec3_student@vsphere.local", "cmpe-LXKN", true);
		String a = "https://" + args[0] + "/sdk";
		String b = args[1];
		String c = args[2];

		ServiceInstance si = new ServiceInstance(new URL(a), b, c, true);
		Folder rootFolder = si.getRootFolder();
		String name = rootFolder.getName();
		System.out.println("root:" + name);
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		ManagedEntity[] mesHosts = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		
		if(mes == null || mes.length == 0) {
			System.out.println("No virtual machines.");
			return;
		}
		if(mesHosts == null || mesHosts.length == 0) {
			System.out.println("No host systems.");
			return;
		}
		
		System.out.println("CMPE281 HW2 from Xiaoyu Zhou");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			System.out.println("XiaoyuZhou-038: ");
			String input = br.readLine();
			input = input.trim().replace(" +", " "); //remove the spaces at the beginning and in the end
			input = input.toLowerCase();  //convert all to lower case
			
			//command "exit": exit the program
			if( input.equals("exit")) {
				break;
			}
			//command "help": print out the usage, e.g., the entire list of commands
			else if(input.equals("help")) {
				System.out.println("usage: \n");
				System.out.println("exit					exit the program \n");
				System.out.println("host					enumerate hosts \n");
				System.out.println("host hname info				show info for hname \n");
				System.out.println("host hname datastore			enumerate datastores for hname \n");
				System.out.println("host hname network			enumerate networks for hname \n");
				System.out.println("vm					enumerate vms \n");
				System.out.println("vm vname info				show info for vname \n");
				System.out.println("vm vname shutdown			shutdown OS on vname \n");
				System.out.println("vm vname on				power on vname \n");
				System.out.println("vm vname off				power off vname \n");
			}
			
			//command "host": enumerate all hosts
			else if(input.equals("host")) {
				int i = 0;
				for(ManagedEntity me : mesHosts) {
					HostSystem host = (HostSystem) me;
					System.out.println("host[" + (i++) + "]: Name = " + host.getName());	
				}				
			}
			
			//command "vm": enumerate all virtual machines
			else if(input.equals("vm")) {
				int i = 0;
				
				for(ManagedEntity me : mes) {
					VirtualMachine vm = (VirtualMachine) me;
					
					System.out.println("vm[" + (i++) + "]: Name = " + vm.getName());
				}
			}
			
			//following are the commands that contain a "changeable" part
			else {
				String[] tokens = input.split(" ");
				if(tokens.length != 3) {
					System.out.println("Wrong command (format). Please enter three parameters");
					break;
				}
				
				//command "host hname info": show info of host hname, e.g., host 130.65.159.11 info
				if(tokens[0].equals("host") && tokens[2].equals("info")) {
					HostSystem host = getHostFromHname(tokens[1], mesHosts);
					
					System.out.println("Name = " + tokens[1]);					
					HostHardwareInfo hw = host.getHardware();
					System.out.println("ProductFullName = VMware ESXi " + si.getAboutInfo().getVersion() + " build-" + si.getAboutInfo().getBuild());		
					System.out.println("CPU cores = " + hw.getCpuInfo().getNumCpuCores());
					System.out.println("RAM = " + (int)(hw.getMemorySize()/Math.pow(1024, 3)) + " GB");				
				}
				
				//command "host hname datastore": enumerate datastores of host hname, e.g., host 130.65.159.11 datastore
				else if(tokens[0].equals("host") && tokens[2].equals("datastore")) {			
					HostSystem host = getHostFromHname(tokens[1], mesHosts);
					
					System.out.println("Name = " + tokens[1]);
					Datastore[] dss = host.getDatastores();
					int i = 0;
					for(Datastore ds : dss) {
						DatastoreSummary dsm = ds.getSummary();
						System.out.println("Datastore[" + (i++) + "]: name = " + dsm.name + ", capacity = " + (int)(dsm.capacity/Math.pow(1024, 3)) + " GB, FreeSpace = " + (int)(dsm.freeSpace/Math.pow(1024, 3)) + " GB.");
					}
				}
				
				//command "host hname network": enumerate networks of host hname, e.g., host 130.65.159.11 network
				else if(tokens[0].equals("host") && tokens[2].equals("network")) {
					HostSystem host = getHostFromHname(tokens[1], mesHosts);
					
					System.out.println("Name = " + tokens[1]);
					Network[] nets = host.getNetworks();
					int i = 0;
					for(Network net : nets) {
						NetworkSummary netm = net.getSummary();
						System.out.println("Network[" + (i++) + "]: name = " + netm.name);
					}
				}
				
				//command "vm vname info": show info of VM name, e.g., vm demo-centos7-123 info
				else if(tokens[0].equals("vm") && tokens[2].equals("info")) {
					VirtualMachine vm = getVmFromVname(tokens[1], mes);

					System.out.println("Name = " + tokens[1]);
					VirtualMachineConfigInfo vminfo = vm.getConfig();
					System.out.println("Guest full name = " + vminfo.getGuestFullName());
					GuestInfo gsInfo = vm.getGuest();
					System.out.println("Guest state = " + gsInfo.guestState);					
					System.out.println("IP addr = " + gsInfo.ipAddress);
					System.out.println("Tool running status = " + gsInfo.toolsRunningStatus);
					VirtualMachineRuntimeInfo vmRunInfo = vm.getRuntime();
					System.out.println("Power state = " + vmRunInfo.getPowerState());
				}
				
				//command "vm vname on": power on VM vname and wait until task completes, e.g., vm demo-centos7-123 on
				else if(tokens[0].equals("vm") && tokens[2].equals("on")) {
					VirtualMachine vm = getVmFromVname(tokens[1], mes);
					
					System.out.println("Name = " + tokens[1]);
					Task task = vm.powerOnVM_Task(null);	
					task.waitForTask();
					TaskInfo taskInfo = task.getTaskInfo();
					
					SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
					String completionTime = formatter.format(taskInfo.getCompleteTime().getTime());
					if(taskInfo.getState().toString().equals("success")) {
						System.out.println("Power on VM: status = " + taskInfo.getState() + ", completion time = " + completionTime);
					} else {
						System.out.println("Power on VM: status = The attempted operation cannot be performed in the current state (Powered on), completion time = " + completionTime);  
					}
				}
				
				//command "vm vname off": power off VM vname and wait until task completes, e.g., vm demo-centos7-123 off
				else if(tokens[0].equals("vm") && tokens[2].equals("off")) {
					VirtualMachine vm = getVmFromVname(tokens[1], mes);
					
					System.out.println("Name = " + tokens[1]);
					Task task = vm.powerOffVM_Task();
					task.waitForTask();
					TaskInfo taskInfo = task.getTaskInfo();
					
					SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
					String completionTime = formatter.format(taskInfo.getCompleteTime().getTime());
					if(taskInfo.getState().toString().equals("success")) {
						System.out.println("Power off VM: status = " + taskInfo.getState() + ", completion time = " + completionTime);
					} else {
						System.out.println("Power off VM: status = The attempted operation cannot be performed in the current state (Powered on), completion time = " + completionTime);  
					}
				}
				
				//command "vm vname shutdown": shutdown guest of VM vname, e.g., vm demo-centos7-123 shutdown
				else if(tokens[0].equals("vm") && tokens[2].equals("shutdown")) {
					VirtualMachine vm = getVmFromVname(tokens[1], mes);
					
					System.out.println("Name = " + tokens[1]);
					
					vm.shutdownGuest();			//this calls the shutdown function without waiting
					
					int i = 0;  int checkTimes = (2 * 60 ) / 2;		//check power state every 2 secs for 2 minutes. 
					for( ; i < checkTimes; i++) {	
						Thread.sleep(2000);
						
						if(vm.getSummary().getRuntime().getPowerState().toString().equals("poweredOff")) {
							SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");					
							String curTime = formatter.format(si.getServerClock().getTime());
							System.out.println("Shutdown guest: completed, time = " +  curTime);
							
							break;
						}							
					}
					
					//i == checkTimes means: the shutdown doesn't work in 2 minutes, then we try a hard power off.
					if(i == checkTimes) {
						System.out.println("Graceful shutdown failed. Now try a hard power off.");
						Task task = vm.powerOffVM_Task();
						task.waitForTask();
						TaskInfo taskInfo = task.getTaskInfo();
					
						SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
						String completionTime = formatter.format(taskInfo.getCompleteTime().getTime());
						if(taskInfo.getState().toString().equals("success")) {
							System.out.println("Power off VM: status = " + taskInfo.getState() + ", completion time = " + completionTime);
						} else {
							System.out.println("Power off VM: status = The attempted operation cannot be performed in the current state (Powered on), completion time = " + completionTime);  
						}
					}
				}
				
				else {
					System.out.println("Unrecognized command.");
					break;
				}
			}	
		}
		
		//close the BufferedReader
		if(br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		si.getServerConnection().logout();
	}
	
	//get the host based on provided hname, not efficient way, better to modify
	public static HostSystem getHostFromHname(String hname, ManagedEntity[] mesHosts) {
		HostSystem host = null;
		
		for(ManagedEntity me : mesHosts) {
			HostSystem curHost = (HostSystem) me;
			if(curHost.getName().equals(hname)) {
				host = curHost;
				break;
			}
		}
		
		return host;
	}
	
	//get the vm based on provided vname, not efficient way, better to modify
		public static VirtualMachine getVmFromVname(String vname, ManagedEntity[] mes) {
			VirtualMachine vm = null;
			
			for(ManagedEntity me : mes) {
				VirtualMachine curVm = (VirtualMachine) me;
				if(curVm.getName().equals(vname)) {
					vm = curVm;
					break;
				}
			}
			
			return vm;
		}
		
		//should log out here
		
}
