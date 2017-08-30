/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include "ns3/core-module.h"
#include "ns3/network-module.h"
#include "ns3/internet-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/applications-module.h"
#include "ns3/constant-position-mobility-model.h"
#include "ns3/mobility-model.h"
#include "ns3/mobility-helper.h"
#include "ns3/mobility-module.h"
#include "ns3/wifi-module.h"
#include "ns3/aodv-module.h"
#include "ns3/olsr-module.h"
#include "ns3/dsdv-module.h"
#include "ns3/dsr-module.h"
#include "ns3/csma-module.h"
#include "ns3/ipv4-global-routing-helper.h"
#include "ns3/ipv4-route.h"
#include "ns3/ipv4-routing-protocol.h"

#include <iostream>
#include <fstream> 
#include <string>
#include <cassert>
#include <map>


#include "ns3/netanim-module.h"

using namespace ns3;
using namespace std;


NS_LOG_COMPONENT_DEFINE ("FirstScriptExample");
NodeContainer nodes;
string delay;
string protocol;
string dataRate;
string IP;
string subnetMask;
int NumOfNodes;
int bytesTotal;
int packetsReceived;
std::map<int, Ptr<Socket> > m_socketAddresses;
std::map<Ipv4Address,int> m_nodeAddress;

std::ofstream results;
std::ifstream positions;


#define PositionFile "scratch/Positions.txt"
#define ResultsFile "/home/fasih58/Desktop/results.xml"
#define SettingsFile "scratch/Settings.txt"
#define TotalTime 10.0



void
READ_SIMBEEOTIC_STATE(){
  NS_LOG_UNCOND ("Start Reading State...");



  positions.open(PositionFile);
  if(positions.is_open()){
    positions >> NumOfNodes;
    nodes.Create (NumOfNodes);
    MobilityHelper mobility;
    mobility.SetPositionAllocator ("ns3::GridPositionAllocator",
                             "MinX", DoubleValue (0.0),
                             "MinY", DoubleValue (0.0),
                             "DeltaX", DoubleValue (500),
                             "DeltaY", DoubleValue (500),
                             "GridWidth", UintegerValue (5),
                             "LayoutType", StringValue ("RowFirst"));

    mobility.SetMobilityModel ("ns3::ConstantPositionMobilityModel");
    mobility.Install (nodes);

    int NodeNumber;
    double X, Y, Z;

    for(int i=0;i<NumOfNodes;i++) {
      
      Ptr<MobilityModel> mob = nodes.Get(i)->GetObject<MobilityModel>();
      positions >> NodeNumber >> X >> Y >> Z;

      Vector new_pos(X,Y,Z);
      mob->SetPosition(new_pos);

      Vector Pos = mob->GetPosition();
     
      NS_LOG_UNCOND ("NodeNumber: "<<NodeNumber<<"\t\tX = "<<Pos.x<<"\t Y = "<<Pos.y<<"\t Z = "<<Pos.z);
    }

    NS_LOG_UNCOND ("State Finished Mapping to Simbeeotic!");
  }
  else {
    NS_LOG_UNCOND ("COULD NOT OPEN FILE FOR MAPPING");
  }
}

void SET_VALUE(string var, string value) {
  
  if(var=="delay")
    delay = value;
  else if(var=="protocol")
    protocol = value;    
  else if(var=="dataRate")
    dataRate = value;
  else if(var=="baseIP")
    IP = value;
  else if(var=="subnetMask")
    subnetMask = value;
  else
    ;
}

int
GET_SETTINGS() {

  NS_LOG_UNCOND ("\nSetting up protocols...");

  std::ifstream settings(SettingsFile);
  
  if(settings.is_open()) {
    string str;
    bool is_next_line = true;
    while(is_next_line && std::getline(settings,str,'\n')){
      if(str[str.length()-1]==',') {
         is_next_line = true; //there is a new setting in file
         str = str.substr(0,str.length()-1);  //remove comma
      }
      else {
        is_next_line = false;//this was last line
      }

      int delimeter = str.find(":");
      string var = str.substr(0,delimeter);
      str.erase(0,delimeter+1);
      SET_VALUE(var,str);

      NS_LOG_UNCOND ("Setting "<<var<<" to "<<str);
    }

    NS_LOG_UNCOND ("Finished setting up protocols\n\n");
    return 1;
  }
  else {
    NS_LOG_UNCOND ("Could Not Open Settings File. Environment Not Set Up\n\n");
    return 0;
  }
}

static inline std::string
PrintReceivedPacket (Ptr<Socket> socket, Ptr<Packet> packet)
{
  SocketAddressTag tag;
  bool found;
  
  found = packet->PeekPacketTag (tag);
  std::ostringstream oss;
  uint8_t *buffer = new uint8_t[packet->GetSize ()];
  packet->CopyData(buffer, packet->GetSize ());
  std::string data = std::string((char*)buffer);

  oss << "<p Time=\""<<Simulator::Now ().GetSeconds () << "\" Rcvr=\"" << socket->GetNode ()->GetId ();

  if (found)
    {
      InetSocketAddress addr = InetSocketAddress::ConvertFrom (tag.GetAddress ());
      
      oss << "\" Sndr=\"" << m_nodeAddress.at(addr.GetIpv4 ())<<"\" bytes=\"" << packet->GetSize() << "\" payload=\""<< data << "\" />";

    }
  else
    {
      oss << "\" Sndr=\"" <<-1<<"\" bytes=\"" << packet->GetSize() << "\" payload=\""<< data << "\" />";
    }
    results << oss.str()<<"\n";
  return oss.str ();
}

void
 ReceivePacket (Ptr<Socket> socket)
{
  Ptr<Packet> packet;
  while ((packet = socket->Recv ()))
    {
      bytesTotal += packet->GetSize ();
      packetsReceived += 1;
      NS_LOG_UNCOND (PrintReceivedPacket (socket, packet));
      //NS_LOG_UNCOND("DIDNT HAPPEN");
    }
}

void
 placeEndTagInXml()
 {
    string endTag = "</simbeenet>\n";
    results << endTag;
 }

Ptr<Socket>
 SetupPacketReceive (Ipv4Address addr, Ptr<Node> node,int nodeNum)
{
  TypeId tid = TypeId::LookupByName ("ns3::UdpSocketFactory");
  Ptr<Socket> sink = Socket::CreateSocket (node, tid);

  InetSocketAddress local = InetSocketAddress (addr, 9);

  sink->Bind (local);
  sink->SetRecvCallback (MakeCallback (&ReceivePacket));
  sink->SetAllowBroadcast (true);
  


  m_socketAddresses.insert(std::pair<int, Ptr<Socket> >(nodeNum,sink));
  m_nodeAddress.insert(std::pair<Ipv4Address ,int >(addr,nodeNum));
  

  return sink;
}

int
main (int argc, char *argv[])
{  
  Time::SetResolution (Time::NS);
 
  results.open(ResultsFile);
  //add root tag to cater for xml file requirements
  string rootTag = "<simbeenet ver=\"1.0\" fileType=\"results\" >\n";
  results << rootTag;
  
  //initialize variables 
  bytesTotal = 0;
  packetsReceived=0;

  READ_SIMBEEOTIC_STATE();

  
  if(GET_SETTINGS() == 1) {

    AodvHelper aodv;
    OlsrHelper olsr;
    DsdvHelper dsdv;
    DsrHelper dsr;
    DsrMainHelper dsrMain;
    Ipv4ListRoutingHelper list;
    InternetStackHelper internet;
    int m_protocol=5;

        


          //setup the nodes to be connected wirelessly from the settings that have been read 

    double txp = 7.5;

    NS_LOG_UNCOND("SETTING UP WIRELESS CONNECTION BETWEEN NODES");
    //double TotalTime = 10.0;
    std::string phyMode ("DsssRate11Mbps");
    std::string tr_name ("manet-routing-compare");


        //Config::SetDefault  ("ns3::OnOffApplication::PacketSize",StringValue ("64"));
    Config::SetDefault ("ns3::OnOffApplication::DataRate",  StringValue (dataRate));

        //Set Non-unicastMode rate to unicast mode
    Config::SetDefault ("ns3::WifiRemoteStationManager::NonUnicastMode",StringValue (phyMode));



    WifiHelper wifi;
    wifi.SetStandard (WIFI_PHY_STANDARD_80211b);

    YansWifiPhyHelper wifiPhy =  YansWifiPhyHelper::Default ();
    YansWifiChannelHelper wifiChannel;
    wifiChannel.SetPropagationDelay ("ns3::ConstantSpeedPropagationDelayModel");
    wifiChannel.AddPropagationLoss ("ns3::FriisPropagationLossModel");
    wifiPhy.SetChannel (wifiChannel.Create ());

        // Add a non-QoS upper mac, and disable rate control
    NqosWifiMacHelper wifiMac = NqosWifiMacHelper::Default ();
    wifi.SetRemoteStationManager ("ns3::ConstantRateWifiManager",
                                  "DataMode",StringValue (phyMode),
                                  "ControlMode",StringValue (phyMode));

    

    wifiPhy.Set ("TxPowerStart",DoubleValue (txp));
    wifiPhy.Set ("TxPowerEnd", DoubleValue (txp));

    wifiMac.SetType ("ns3::AdhocWifiMac");


    //csma to check routes
    CsmaHelper csma;
    csma.SetChannelAttribute ("DataRate", StringValue (dataRate));
    csma.SetChannelAttribute ("Delay", StringValue (delay));
    

    NS_LOG_UNCOND("INSTALLING CONNECTIONS AND ATTRIBUTES ON NODES");
    //connect all the nodes
    NetDeviceContainer devices;
    devices = wifi.Install (wifiPhy, wifiMac, nodes);
    devices = csma.Install (nodes);

    
    NS_LOG_UNCOND("DELEGATING PROTOCOL TO ENTIRE SYSTEM");
    //determine which helper needed for protocol
    if(protocol=="OLSR")
    {
      list.Add (olsr, 100);
      m_protocol = 1;
    }
    else if(protocol=="AODV")
    {
      list.Add (aodv, 100);
      m_protocol = 2;
    }
    else if(protocol=="DSDV")
    {
      list.Add (dsdv, 100);
      m_protocol = 3;
    }
    else if(protocol=="DSR")
      m_protocol = 4;
    else
      NS_FATAL_ERROR ("No such protocol:" <<protocol);



          //set the protocol
    if (m_protocol < 4)
    {
      internet.SetRoutingHelper (list);
      internet.Install (nodes);
    }
    else if (m_protocol == 4)
    {
      internet.Install (nodes);
      dsrMain.Install (dsr, nodes);
    }




            //set base ip and subnet mask
    NS_LOG_UNCOND("ASSIGNING IP ADDRESSES TO NODES ACCORDING TO PROTOCOL");
    Ipv4AddressHelper address;
    address.SetBase ("10.1.1.0", "255.255.255.0");

    Ipv4InterfaceContainer interfaces = address.Assign (devices);


    


        //make each node into a sink so they can recieve and consume packets
        NS_LOG_UNCOND("MAKING SOCKETS AND SINKS\n\n");
            for (int i = 0; i < NumOfNodes; i++)
              {
                Ptr<Socket> sink = SetupPacketReceive (interfaces.GetAddress (i), nodes.Get (i),i);
              }
          NS_LOG_UNCOND("\n\nFINISHED TWEAKING NODES\n\n");



              //do all communications for your parent application
              int numOfCommunications;
              int from;
              int to;
              string data;

              positions >> numOfCommunications;

             NS_LOG_UNCOND("STARTING COMMUNICATIONS. TOTAL COMMMUNICATIONS IN THIS RUN: "<<numOfCommunications<<"\n\n");
            for (int i = 0; i < numOfCommunications; i++)
              {
               
                positions >> from >> to;
                NS_LOG_UNCOND("COMMUNICATION FROM NODE "<<from<<"TO NODE "<<to);
                getline(positions,data,'\n');

                //NS_LOG_UNCOND("from"<<from<<"to"<<to << "data:" <<data);

                //create custom packet
                NS_LOG_UNCOND("CREATING PACKET"); 
                std::ostringstream msg; 
                msg << data << '\0';
                Ptr<Packet> packet = Create<Packet> ((uint8_t*) msg.str().c_str(), msg.str().length());

                //send over topology
                NS_LOG_UNCOND("GETTING SOCKET OF "<<from);
                Ptr<Socket> src = m_socketAddresses.at(from);
                //if broadcast or not   -1 signifies broadcast
                NS_LOG_UNCOND("SENDING...");
                if(to!=-1)
                  {
                    InetSocketAddress remoteAddress (InetSocketAddress (interfaces.GetAddress (to), 9));
                    src->Connect(remoteAddress);
                    src->Send(packet);
                  }
                 else
                 {
                    InetSocketAddress remoteAddress (InetSocketAddress ("255.255.255.255", 9));
                    src->Connect(remoteAddress);
                    src->Send(packet);
                 }
                 NS_LOG_UNCOND("SENT. \n\n");
                
                //TotalTime+=0;

              }

              NS_LOG_UNCOND("COMMUNICATIONS DONE. CLOSING CONNECTIONS IN 2 secs");
              //since all communications are done we can go ahead and add end tag
              //2 secs is an approx.. it could be more or less
  }
  else {
    NS_LOG_UNCOND("SETTINGS COULD NOT BE RETRIEVED. GET_SETTINGS EXITTED WITH STATUS CODE 0.");
  }

//adding blank spaces in terminal
  NS_LOG_UNCOND (" ");
  NS_LOG_UNCOND ("SCRIPT FINISHED\n");

  Simulator::Stop(Seconds (TotalTime));
  Simulator::ScheduleDestroy(&placeEndTagInXml);

  Simulator::Run ();
  Simulator::Destroy ();
  return 0;
}