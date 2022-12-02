package Server;

import Constants.Constants;
import Implementation.HospSherbrookManager;
import Structure.Sherbrook_Data;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Sherbrook_Server {
    static HospSherbrookManager she=new HospSherbrookManager();
    static Logger logger;
    String output="";
    private long counter_s = 1;
    private HashMap<Long,String> q = new HashMap<>();

    public static void main(String[] args) throws Exception{
        Sherbrook_Server sherbrook_server=new Sherbrook_Server();
        Sherbrook_Server.setLogger("C:\\Users\\Dell\\Desktop\\CONCORDIA\\COMP 16\\Distributed-Appointment-Management\\Comp6231_Project\\src\\logs\\SHE.txt","SHE");
        logger.info("Sherbrooke server started...");
        Runnable stask=()->{
            sherbrook_server.receive();
        };
        Thread thread=new Thread(stask);
        thread.start();
        Runnable sRequestTask=()->{
            sherbrook_server.receiveMulticastRequest();
        };
        Runnable sResponseTask = () -> {
            receiveFailedResponse();
        };
        Runnable RMResponseTask = () -> {
            updateServerData();
        };
        Thread thread1=new Thread(sRequestTask);
        Thread thread2=new Thread(sResponseTask);
        Thread thread3=new Thread(RMResponseTask);
        thread1.start();
        thread2.start();
        thread3.start();
    }

    private void receive() {
        DatagramSocket ds=null;
        while (true){
            try {
                ds=new DatagramSocket(Constants.LOCAL_Sherbrook_PORT);
                byte[] receive=new byte[Constants.BYTE_LENGTH];
                DatagramPacket dp=new DatagramPacket(receive, receive.length);
                ds.receive(dp);
                byte[] data = dp.getData();
                String[] receiveData = new String(data).split(",");
                if(receiveData[receiveData.length-1].trim().equals(Constants.LIST_OPERATION))
                {
                    String str=she.s_data.retriveAppointment(receiveData[2]);
                    //logger.info("Message to patient:"+str);
                    DatagramPacket dp2=new DatagramPacket(str.getBytes(), str.length(), dp.getAddress(), dp.getPort());
                    updateJSONFile();
                    ds.send(dp2);
                }
                else if(receiveData[receiveData.length-1].trim().equals(Constants.ADD_OPERATION))
                {
                    boolean str=she.s_data.addAppoint(receiveData[1], receiveData[2], receiveData[3]);
                    String new_str=str==false?"Denies":"Approves";
                    //logger.info("Message to admin:"+str);
                    DatagramPacket dp2=new DatagramPacket(new_str.getBytes(), new_str.length(), dp.getAddress(), dp.getPort());
                    updateJSONFile();
                    ds.send(dp2);
                }
                else if(receiveData[receiveData.length-1].trim().equals(Constants.BOOK_OPERATION))
                {
                    String str=generateJSONObj(receiveData[0],receiveData[1],receiveData[2],"None","None","None",Constants.BOOK_OPERATION,she.s_data.bookAppoint(receiveData[0],receiveData[1],receiveData[2]));
                    //logger.info("Message to patient:"+str);
                    DatagramPacket dp2=new DatagramPacket(str.getBytes(), str.length(), dp.getAddress(), dp.getPort());
                    updateJSONFile();
                    ds.send(dp2);
                }
                else if(receiveData[receiveData.length-1].trim().equals(Constants.CANCEL_OPERATION))
                {
                    String str=generateJSONObj(receiveData[0],receiveData[1],receiveData[2],"None","None","None",Constants.CANCEL_OPERATION,she.s_data.removeAppoint(receiveData[0],receiveData[1],receiveData[2]));
                    //logger.info("Message to patient:"+str);
                    DatagramPacket dp2=new DatagramPacket(str.getBytes(), str.length(), dp.getAddress(), dp.getPort());
                    updateJSONFile();
                    ds.send(dp2);
                }
                else if(receiveData[receiveData.length-1].trim().equals(Constants.SCHEDULE_OPERATION))
                {
                    String str=she.s_data.getBookingSchedule(receiveData[0]);
                    //logger.info("Message to patient:"+str);
                    DatagramPacket dp2=new DatagramPacket(str.getBytes(), str.length(), dp.getAddress(), dp.getPort());
                    updateJSONFile();
                    ds.send(dp2);
                }
                else if(receiveData[receiveData.length-1].trim().equals("countAppointment"))
                {
                    String str=she.s_data.getBookingCount(receiveData[0], receiveData[1]);
                    //logger.info("Message to patient:"+str);
                    DatagramPacket dp2=new DatagramPacket(str.getBytes(), str.length(), dp.getAddress(), dp.getPort());
                    updateJSONFile();
                    ds.send(dp2);
                }
                else if(receiveData[receiveData.length-1].trim().equals("existingAppointment"))
                {
                    boolean b=she.s_data.getAppoint(receiveData[0], receiveData[1], receiveData[2]);
                    //logger.info("Message to patient:"+b);
                    String str=b==false?"Denies":"Approves";
                    DatagramPacket dp2=new DatagramPacket(str.getBytes(), str.length(), dp.getAddress(), dp.getPort());
                    updateJSONFile();
                    ds.send(dp2);
                }
                else {
                    logger.info("Server Error");
                }
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                ds.close();
            }
        }
    }

    private String generateJSONObj(String id, String appointId, String appointType, String capacity, String oldAppointId, String oldAppointType, String operation, boolean status) {
        JSONObject object=new JSONObject();
        object.put(Constants.ID,id.trim());
        object.put(Constants.APPOINTMENT_ID,appointId.trim());
        object.put(Constants.APPOINTMENT_TYPE,appointType.trim());
        object.put(Constants.APPOINTMENT_CAPACITY,capacity.trim());
        object.put(Constants.OLD_APPOINTMENT_ID,oldAppointId.trim());
        object.put(Constants.OLD_APPOINTMENT_TYPE,oldAppointType.trim());
        object.put(Constants.OPERATION,operation.trim());
        object.put(Constants.OPERATION_STATUS,status);
        return object.toString();
    }

    private void updateJSONFile() {
        JSONObject jsonObject=new JSONObject();
        Gson gson=new GsonBuilder().setPrettyPrinting().create();
        String str=gson.toJson(she.s_data);
        jsonObject.put("player",str);
        String str2=gson.toJson(jsonObject);
        try {
            FileWriter fileWriter=new FileWriter("sherbrook.json");
            fileWriter.write(str2);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void updateServerData() {
        DatagramSocket ds=null;
        try {
            ds=new DatagramSocket(Constants.FAIL_Sherbrook_PORT);
            while (true){
                byte[] buffer=new byte[Constants.BYTE_LENGTH];
                DatagramPacket dp=new DatagramPacket(buffer, buffer.length);
                ds.receive(dp);
                String data=new String(dp.getData()).trim();
                JSONParser parser=new JSONParser();
                Gson gson=new Gson();
                Object object=parser.parse(data.trim());
                JSONObject jsonObject=(JSONObject) object;
                she.s_data=gson.fromJson(String.valueOf(jsonObject.get("player")), Sherbrook_Data.class);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void receiveFailedResponse() {
        MulticastSocket msocket=null;
        DatagramSocket ds=null;
        try {
            msocket=new MulticastSocket(Constants.FAULT_PORT);
            msocket.joinGroup(InetAddress.getByName(Constants.FAULT_MULTICAST_IP));
            while (true){
                byte[] buffer=new byte[Constants.BYTE_LENGTH];
                DatagramPacket dp=new DatagramPacket(buffer, buffer.length);
                msocket.receive(dp);
                String[] data=new String(dp.getData()).trim().split(":");
                logger.info(data[1].trim()+" is sending data to "+data[2].trim());
                if (data[1].trim().equals(Constants.RM1_ID)){
                    if (data[2].trim().equals(Constants.RM1_ID)){
                        ds=new DatagramSocket();
                        JSONParser parser=new JSONParser();
                        Object obj=parser.parse(new FileReader("montreal.json"));
                        JSONObject jsonObject=(JSONObject) obj;
                        String mtlData=jsonObject.toString();
                        obj=parser.parse(new FileReader("quebec.json"));
                        jsonObject=(JSONObject) obj;
                        String queData=jsonObject.toString();
                        obj=parser.parse(new FileReader("sherbrook.json"));
                        jsonObject=(JSONObject) obj;
                        String sheData=jsonObject.toString();
                        byte[] mtlByte=mtlData.getBytes();
                        byte[] queByte=queData.getBytes();
                        byte[] sheByte=sheData.getBytes();
                        InetAddress mtlHost=InetAddress.getByName(Constants.FAIL_RM1_IP);
                        DatagramPacket mtlRequest=new DatagramPacket(mtlByte,mtlByte.length,mtlHost,Constants.FAIL_Montreal_PORT);
                        ds.send(mtlRequest);
                        InetAddress queHost=InetAddress.getByName(Constants.FAIL_RM1_IP);
                        DatagramPacket queRequest=new DatagramPacket(queByte,queByte.length,queHost,Constants.FAIL_Quebec_PORT);
                        ds.send(queRequest);
                        InetAddress sheHost=InetAddress.getByName(Constants.FAIL_RM1_IP);
                        DatagramPacket sheRequest=new DatagramPacket(sheByte,sheByte.length,sheHost,Constants.FAIL_Sherbrook_PORT);
                        ds.send(sheRequest);
                    }
                    else if (data[2].trim().equals(Constants.RM2_ID)){
                        ds=new DatagramSocket();
                        JSONParser parser=new JSONParser();
                        Object obj=parser.parse(new FileReader("montreal.json"));
                        JSONObject jsonObject=(JSONObject) obj;
                        String mtlData=jsonObject.toString();
                        obj=parser.parse(new FileReader("quebec.json"));
                        jsonObject=(JSONObject) obj;
                        String queData=jsonObject.toString();
                        obj=parser.parse(new FileReader("sherbrook.json"));
                        jsonObject=(JSONObject) obj;
                        String sheData=jsonObject.toString();
                        byte[] mtlByte=mtlData.getBytes();
                        byte[] queByte=queData.getBytes();
                        byte[] sheByte=sheData.getBytes();
//                        InetAddress mtlHost=InetAddress.getByName(Constants.FAIL_RM2_IP);
//                        DatagramPacket mtlRequest=new DatagramPacket(mtlByte,mtlByte.length,mtlHost,Constants.FAIL_Montreal_PORT);
//                        ds.send(mtlRequest);
//                        InetAddress queHost=InetAddress.getByName(Constants.FAIL_RM2_IP);
//                        DatagramPacket queRequest=new DatagramPacket(queByte,queByte.length,queHost,Constants.FAIL_Quebec_PORT);
//                        ds.send(queRequest);
                        InetAddress sheHost=InetAddress.getByName(Constants.FAIL_RM2_IP);
                        DatagramPacket sheRequest=new DatagramPacket(sheByte,sheByte.length,sheHost,Constants.FAIL_Sherbrook_PORT);
                        ds.send(sheRequest);
                        logger.info("here in sherbrrooke");
                    }
                    else if (data[2].trim().equals(Constants.RM3_ID)){
                        ds=new DatagramSocket();
                        JSONParser parser=new JSONParser();
                        Object obj=parser.parse(new FileReader("montreal.json"));
                        JSONObject jsonObject=(JSONObject) obj;
                        String mtlData=jsonObject.toString();
                        obj=parser.parse(new FileReader("quebec.json"));
                        jsonObject=(JSONObject) obj;
                        String queData=jsonObject.toString();
                        obj=parser.parse(new FileReader("sherbrook.json"));
                        jsonObject=(JSONObject) obj;
                        String sheData=jsonObject.toString();
                        byte[] mtlByte=mtlData.getBytes();
                        byte[] queByte=queData.getBytes();
                        byte[] sheByte=sheData.getBytes();
                        InetAddress mtlHost=InetAddress.getByName(Constants.FAIL_RM3_IP);
                        DatagramPacket mtlRequest=new DatagramPacket(mtlByte,mtlByte.length,mtlHost,Constants.FAIL_Montreal_PORT);
                        ds.send(mtlRequest);
                        InetAddress queHost=InetAddress.getByName(Constants.FAIL_RM3_IP);
                        DatagramPacket queRequest=new DatagramPacket(queByte,queByte.length,queHost,Constants.FAIL_Quebec_PORT);
                        ds.send(queRequest);
                        InetAddress sheHost=InetAddress.getByName(Constants.FAIL_RM3_IP);
                        DatagramPacket sheRequest=new DatagramPacket(sheByte,sheByte.length,sheHost,Constants.FAIL_Sherbrook_PORT);
                        ds.send(sheRequest);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private void receiveMulticastRequest() {
        MulticastSocket multicastSocket = null;
        try {
            multicastSocket = new MulticastSocket(Constants.RM_Sherbrook_PORT);
            multicastSocket.joinGroup(InetAddress.getByName(Constants.MULTICAST_IP));
            while (true) {
                byte[] buffer = new byte[65535];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(datagramPacket);

                String request = new String(datagramPacket.getData());
                Object object = new JSONParser().parse(request.trim());
                JSONObject jsonObject = (JSONObject) object;
                long counter = (long) jsonObject.get("Sequence");
                if(counter == counter_s) {
                    execute(request, counter);
                    counter_s++;
                    new Thread(
                            ()->{
                                try {
                                    Q(counter);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                }
                else
                {
                    q.put(counter,request);
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }finally {
            multicastSocket.close();
        }

    }

    private void Q(long counter) throws ParseException {

        if(q.containsKey(counter)) {
            String request = q.get(counter);

            execute(request, counter);
            q.remove(counter);
            counter_s++;
            Q(counter);
        }


    }

    private void execute(String request, long counter) throws ParseException {
        Object object = new JSONParser().parse(request.trim());
        JSONObject jsonObject = (JSONObject) object;
        switch (jsonObject.get(Constants.OPERATION).toString()) {
            case "addAppointment": {
                String id = jsonObject.get(Constants.ID).toString();
                String appointId = jsonObject.get(Constants.APPOINTMENT_ID).toString();
                String appointType = jsonObject.get(Constants.APPOINTMENT_TYPE).toString();
                String capacity = jsonObject.get(Constants.APPOINTMENT_CAPACITY).toString();
                output = she.addAppoint(id, appointId, appointType, capacity);
                break;
            }
            case "removeAppointment": {
                String id = jsonObject.get(Constants.ID).toString();
                String appointId = jsonObject.get(Constants.APPOINTMENT_ID).toString();
                String appointType = jsonObject.get(Constants.APPOINTMENT_TYPE).toString();
                output = she.removeAppoint(id, appointId, appointType);
                break;
            }
            case "listAppointment": {
                String id = jsonObject.get(Constants.ID).toString();
                String appointType = jsonObject.get(Constants.APPOINTMENT_TYPE).toString();
                output = she.listAppointAvailability(id, appointType);
                break;
            }
            case "bookAppointment": {
                String id = jsonObject.get(Constants.ID).toString();
                String appointId = jsonObject.get(Constants.APPOINTMENT_ID).toString();
                String appointType = jsonObject.get(Constants.APPOINTMENT_TYPE).toString();
                output = she.bookAppoint(id, appointId, appointType);
                break;
            }
            case "cancelAppointment": {
                String id = jsonObject.get(Constants.ID).toString();
                String appointId = jsonObject.get(Constants.APPOINTMENT_ID).toString();
                String appointType = jsonObject.get(Constants.APPOINTMENT_TYPE).toString();
                output = she.cancelAppoint(id, appointId, appointType);
                break;
            }
            case "scheduleAppointment": {
                String id = jsonObject.get(Constants.ID).toString();
                output = she.getBookingSchedule(id);
                break;
            }
            case "swapAppointment": {
                String id = jsonObject.get(Constants.ID).toString();
                String newAppointId = jsonObject.get(Constants.APPOINTMENT_ID).toString();
                String newAppointType = jsonObject.get(Constants.APPOINTMENT_TYPE).toString();
                String oldAppointId = jsonObject.get(Constants.OLD_APPOINTMENT_ID).toString();
                String oldAppointType = jsonObject.get(Constants.OLD_APPOINTMENT_TYPE).toString();
                output = she.swapAppoint(id, newAppointId, newAppointType, oldAppointId, oldAppointType);
                break;
            }
        }
        updateJSONFile();
        sendRequestToFrontEnd(counter + "#" + output);

    }

    private void sendRequestToFrontEnd(String data) {
        DatagramSocket datagramSocket=null;
        try {
            logger.info("Data sent to Front End:"+data);
            datagramSocket=new DatagramSocket();
            byte[] buffer=data.getBytes();
            InetAddress inetAddress=InetAddress.getByName(Constants.FRONTEND_IP);
            DatagramPacket datagramPacket=new DatagramPacket(buffer,buffer.length,inetAddress,Constants.RM1_FRONTEND_PORT);
            datagramSocket.send(datagramPacket);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void setLogger(String location, String id)
    {
        // TODO Auto-generated method stub
        try
        {
            logger = Logger.getLogger(id);
            FileHandler fh = new FileHandler(location, true);
            SimpleFormatter sf = new SimpleFormatter();
            fh.setFormatter(sf);
            logger.addHandler(fh);
        }
        catch (Exception err)
        {
            logger.info("Couldn't Initiate Logger. Please check file permission");
        }
    }
}

