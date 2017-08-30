/*
 * Copyrights whatever
 */

package harvard.robobees.simbeeotic.model.ns;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import harvard.robobees.simbeeotic.SimTime;
import harvard.robobees.simbeeotic.model.Model;
import harvard.robobees.simbeeotic.model.SimpleBee;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.vecmath.Vector3f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A model that is used to read ns parameters provided in Scenario xml file
 * It will be responsible for communicating between Simbeeotic and NS-3
 *
 * It will hopefully be replaced by more classes and will be made abstract
 *
 * @author unnamed
 */
public class NS extends AbstractNS {

    private String protocol = "DSDV";
    private String dataRate = "5Mbps";
    private String delay = "2ms";
    private String baseIP = "10.1.1.0";
    private String subnetMask = "255.255.255.0";
//    private String propagationDelay = "ConstantSpeedPropagationDelayModel";
//    private String propagationLoss = "FriisPropagationLossModel";

    private List<Integer> beeMaps = new ArrayList<Integer>();

    @Override
    public void receive(SimTime time, NSPacket nsPacket) {
        super.receive(time, nsPacket);
        notifyListeners(time, nsPacket, 0);
    }

    @Override
    public void transmit(NSPacket nsPacket) {
        super.transmit(nsPacket);
        clock.pause();
        printNSConfigurations(nsPacket);
        callWaf();
        try {
            readNSXML(nsPacket);
        }
        catch (Exception e) {}

        clock.start();
    }

    private void printNSConfigurations(NSPacket nsPacket) {
        try {
            File file = new File("/home/fasih58/Desktop/ns-allinone-3.25/ns-3.25/scratch/Settings.txt");
            if(file.exists()) {
                file.delete();
            }
            file.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), true));

            bw.write("protocol:" + protocol + ",\n");
            bw.write("dataRate:" + dataRate + ",\n");
            bw.write("delay:" + delay + ",\n");
            bw.write("ipBase:" + baseIP + ",\n");
            bw.write("subnetMask:" + subnetMask + "\n");

            bw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }

        try {
            File file = new File("/home/fasih58/Desktop/ns-allinone-3.25/ns-3.25/scratch/Positions.txt");
            if(file.exists()) {
                file.delete();
            }
            file.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), true));
            int senderNode = 0, recvNode = 0;
            List<SimpleBee> simpleBees = getSimEngine().findModelsByType(SimpleBee.class);
            bw.write(simpleBees.size() + "\n");
            Vector3f beePos = null;
            int index = 0;
            for (SimpleBee simpleBee : simpleBees) {
                beePos = simpleBee.getTruthPosition();
                beeMaps.add(simpleBee.getModelId());
                //bw.write(simpleBee.getModelId() + " " + beePos.x + " " + beePos.y + " " + beePos.z + "\n");
                bw.write(index++ + " " + beePos.x + " " + beePos.y + " " + beePos.z + "\n");
            }
            bw.write("1\n");

            for(int i = 0; i < beeMaps.size(); i++)
            {
                if(nsPacket.getFromId() == beeMaps.get(i))
                    senderNode = i;
                if(nsPacket.getToId() == beeMaps.get(i))
                    recvNode = i;
                if(nsPacket.getToId() == -1)
                    recvNode = -1;
            }
            //bw.write(nsPacket.getFromId() + " " + nsPacket.getToId() + " " + nsPacket.getData() + "\n");
            bw.write(senderNode + " " + recvNode + " " + nsPacket.getData() + "\n");
            bw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void callWaf() {
        try {
            File tempScript = File.createTempFile("script", null);
            Writer streamWriter = new OutputStreamWriter(new FileOutputStream(tempScript));
            PrintWriter printWriter = new PrintWriter(streamWriter);
            printWriter.println("#!/bin/bash");
            printWriter.println("cd /home/fasih58/Desktop/ns-allinone-3.25/ns-3.25");
            printWriter.println("ls");
            printWriter.println("./waf --run scratch/Working-model");
            printWriter.close();
            String line;
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
            //  pb.inheritIO();
            Process process = pb.start();
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = stdoutReader.readLine()) != null) {
//                logger.info(" .. stdout: " + line);
            }
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = stderrReader.readLine()) != null) {
//                logger.error(" .. stderr: " + line);
            }
            process.waitFor();
        }
        catch (IOException ex) {} catch (InterruptedException ex1) {}
    }


    private void readNSXML(NSPacket nsPacket) throws SAXException, IOException, ParserConfigurationException {

        File xmlFile = new File("/home/fasih58/Desktop/results.xml");
        while(!xmlFile.exists()) {
            xmlFile = new File("/home/fasih58/Desktop/results.xml");
        }
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        // Getting packets for bees
        NodeList posList = doc.getElementsByTagName("p");
        for (int i = 0; i < posList.getLength(); i++) {
            if (posList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element ele = (Element) posList.item(i);

                double lbRxTime = Double.parseDouble(ele.getAttribute("Time")) * TimeUnit.SECONDS.toMillis(1);
                SimTime lbRx = new SimTime((long)lbRxTime, TimeUnit.MILLISECONDS);
                lbRx.add(nsPacket.getTxTime().getTime(), TimeUnit.NANOSECONDS);

                String payload = ele.getAttribute("payload");
                int modelFromId = Integer.parseInt(ele.getAttribute("Sndr"));
                // Gets bee mapping, from NS id to Simbeeotic id
                int modelToId = beeMaps.get(Integer.parseInt(ele.getAttribute("Rcvr")));
                Model modelTo = getSimEngine().findModelById(modelToId);

                AbstractNS toBeeNs = null;
                if (modelTo instanceof SimpleBee) {
                    // Gets NS model attached to the bee
                    toBeeNs = ((SimpleBee)modelTo).getNs();
                }
                NSPacket nsPkt = new NSPacket(payload, modelFromId, modelToId, getCurrTime(), lbRx);
                getSimEngine().scheduleEvent(toBeeNs.getModelId(), lbRx, new NSEvent(nsPkt));
            }
        }
    }


    @Inject(optional = true)
    public final void setProtocol(@Named("protocol") final String protocol) {
        this.protocol = protocol;
    }

    @Inject(optional = true)
    public final void setDataRate(@Named("data-rate") final String dataRate) {
        this.dataRate = dataRate;
    }

    @Inject(optional = true)
    public final void setDelay(@Named("delay") final String delay) {
        this.delay = delay;
    }

    @Inject(optional = true)
    public final void setBaseIP(@Named("base-ip") final String baseIP) {
        this.baseIP = baseIP;
    }

    @Inject(optional = true)
    public final void setSubnetMask(@Named("subnet-mask") final String subnetMask) {
        this.subnetMask = subnetMask;
    }
}
