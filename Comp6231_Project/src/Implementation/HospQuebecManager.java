package Implementation;

import Structure.Montreal_Data;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import Structure.Quebec_Data;
import org.json.simple.JSONObject;

import Constants.Constants;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//import static Server.Montreal_Server.setLogger;


public class HospQuebecManager {
    public Quebec_Data q_data;
    static Logger logger;
    public HospQuebecManager(){
        super();
        q_data=new Quebec_Data();
        setLogger("C:\\Users\\Dell\\Desktop\\CONCORDIA\\COMP 16\\Distributed-Appointment-Management\\Comp6231_Project\\src\\logs\\QUE\\QUE.txt","QUE");
    }
    public String addAppoint(String id, String appointId, String appointType, String capacity)
    {
        // TODO Auto-generated method stub
        String result="";
        logger.info("Admin id:"+id+" has added appointment of id"+appointId+" of type:"+appointType);
        if(appointType.equals("physician")||appointType.equals("surgeon")||appointType.equals("dental"))
        {
            if(appointId.substring(0, 3).trim().equals(id.substring(0, 3).trim()))
            {
                if(appointId.substring(0, 3).trim().equals("QUE"))
                    result=generateJSONObject(id,appointId,appointType,capacity,Constants.NONE,Constants.NONE,Constants.ADD_OPERATION,q_data.addAppoint(appointId,appointType,capacity));
                else
                    result=generateJSONObject(id,appointId,appointType,capacity,Constants.NONE,Constants.NONE,Constants.ADD_OPERATION,false);
                return result;
            }
            else
                return generateJSONObject(id,appointId,appointType,capacity,Constants.NONE,Constants.NONE,Constants.ADD_OPERATION,false);
        }
        else
            return generateJSONObject(id,appointId,appointType,capacity,Constants.NONE,Constants.NONE,Constants.ADD_OPERATION,false);
    }

    public String removeAppoint(String id, String appointId, String appointType)
    {
        // TODO Auto-generated method stub
        String result="";
        if(appointType.equals("physician")||appointType.equals("surgeon")||appointType.equals("dental"))
        {
            if(appointId.substring(0, 3).trim().equals(id.substring(0, 3).trim()))
            {
                if(appointId.substring(0, 3).trim().equals("QUE"))
                    result=generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.REMOVE_OPERATION,q_data.removeAppoint(appointId, appointType));
                else
                    result=generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.REMOVE_OPERATION,false);
                return result;
            }
            else
                return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.REMOVE_OPERATION,false);
        }
        else
            return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.REMOVE_OPERATION,false);
    }

    public String listAppointAvailability(String id, String appointType)
    {
        // TODO Auto-generated method stub
        String result="";
        if(appointType.equals("physician")||appointType.equals("surgeon")||appointType.equals("dental"))
        {
            result=q_data.retriveAppointment(appointType).trim();
            result+=requestOnServer(id,Constants.NONE,appointType,Constants.NONE,Constants.LOCAL_Montreal_PORT,Constants.LIST_OPERATION).trim();
            result=result+requestOnServer(id,Constants.NONE,appointType,Constants.NONE,Constants.LOCAL_Sherbrook_PORT,Constants.LIST_OPERATION).trim();
            boolean output=result.trim().isEmpty()?false:true;
            return appointAvailableJSONObject(id,appointType,result,Constants.LIST_OPERATION,output);
        }
        else
            return appointAvailableJSONObject(id,appointType,"",Constants.LIST_OPERATION,false);
    }

    public String bookAppoint(String id, String appointId, String appointType)
    {
        String result="",msg="";
        // TODO Auto-generated method stub
        if(appointType.equals("physician")||appointType.equals("surgeon")||appointType.equals("dental"))
        {
            StringBuilder str=new StringBuilder();
            if(!id.substring(0, 3).equals(appointId.substring(0, 3)))
            {
                str.append(requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Montreal_PORT,"countAppointment")+",");
                str.append(requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Sherbrook_PORT,"countAppointment")+",");
                String[] c=str.toString().trim().split(",");
                int count=0;
                for(int i=0;i<c.length;i++)
                {
                    count+=Integer.parseInt(c[i].trim());
                }
                if(count>=3)
                    return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.BOOK_OPERATION,false);

            }
            if(id.substring(0, 3).equals(appointId.substring(0, 3)))
            {
                return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.BOOK_OPERATION,q_data.bookAppoint(id,appointId,appointType));
            }
            else if(id.substring(0, 3).equals("QUE"))
            {
                msg=requestOnServer(id,appointId,appointType,Constants.NONE,Constants.LOCAL_Quebec_PORT,Constants.BOOK_OPERATION);
                return !msg.trim().isEmpty()?msg.trim():generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.BOOK_OPERATION,false);
            }
            else if(id.substring(0, 3).equals("MTL"))
            {
                msg=requestOnServer(id,appointId,appointType,Constants.NONE,Constants.LOCAL_Montreal_PORT,Constants.BOOK_OPERATION);
                return !msg.trim().isEmpty()?msg.trim():generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.BOOK_OPERATION,false);
            }
            else if(id.substring(0, 3).equals("SHE"))
            {
                msg=requestOnServer(id,appointId,appointType,Constants.NONE,Constants.LOCAL_Sherbrook_PORT,Constants.BOOK_OPERATION);
                return !msg.trim().isEmpty()?msg.trim():generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.BOOK_OPERATION,false);
            }
            else
                return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.BOOK_OPERATION,false);

        }
        else
            return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.BOOK_OPERATION,false);
    }

    public String cancelAppoint(String id, String appointId, String appointType)
    {
        // TODO Auto-generated method stub
        String result="",msg="";
        if(appointType.equals("physician")||appointType.equals("surgeon")||appointType.equals("dental"))
        {
            if(appointId.substring(0, 3).trim().equals(id.substring(0, 3).trim()))
            {
                return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.CANCEL_OPERATION,q_data.removeAppoint(id,appointId,appointType));
            }
            else if(id.substring(0, 3).equals("MTL"))
            {
                msg=requestOnServer(id,appointId,appointType,Constants.NONE,Constants.LOCAL_Montreal_PORT,Constants.CANCEL_OPERATION);
                return !msg.trim().isEmpty()?msg.trim():generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.CANCEL_OPERATION,false);
            }
            else if(id.substring(0, 3).equals("QUE"))
            {
                msg=requestOnServer(id,appointId,appointType,Constants.NONE,Constants.LOCAL_Quebec_PORT,Constants.CANCEL_OPERATION);
                return !msg.trim().isEmpty()?msg.trim():generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.CANCEL_OPERATION,false);
            }
            else if(id.substring(0, 3).equals("SHE"))
            {
                msg=requestOnServer(id,appointId,appointType,Constants.NONE,Constants.LOCAL_Sherbrook_PORT,Constants.CANCEL_OPERATION);
                return !msg.trim().isEmpty()?msg.trim():generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.CANCEL_OPERATION,false);
            }
            else
                return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.CANCEL_OPERATION,false);

        }
        else
            return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.CANCEL_OPERATION,false);
    }

    public String getBookingSchedule(String id)
    {
        // TODO Auto-generated method stub
        //String result="",msg="";
        StringBuilder str=new StringBuilder();
        str.append(q_data.getBookingSchedule(id));
        str.append(requestOnServer(id,"No appoint id",Constants.NONE,Constants.NONE,Constants.LOCAL_Sherbrook_PORT,Constants.SCHEDULE_OPERATION).trim());
        str.append(requestOnServer(id,"No appoint id",Constants.NONE,Constants.NONE,Constants.LOCAL_Montreal_PORT,Constants.SCHEDULE_OPERATION).trim());
        return str.toString().length() == 0 ? appointScheduleJSONObject(id,"",Constants.SCHEDULE_OPERATION,false):appointScheduleJSONObject(id,str.toString().trim(),Constants.SCHEDULE_OPERATION,true);
    }

    public String swapAppoint(String id, String newAppointId, String newAppointType, String oldAppointId,
                              String oldAppointType)
    {
        // TODO Auto-generated method stub
        boolean existanceFlag=checkExistingAppoint(id, oldAppointId, oldAppointType);
        if(existanceFlag==false)
            return generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,false);
        if(id.trim().substring(0, 3).equals(newAppointId.trim().substring(0, 3)))
        {
            boolean bookFlag=unpackJSON(swapAppointBooking(id,newAppointId,newAppointType));
            if(bookFlag)
            {
                boolean cancelFlag=unpackJSON(swapCancelBooking(id,oldAppointId,oldAppointType));
                return cancelFlag ? generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,true):generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,false);
            }
            else
                return generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,false);
        }
        else if(!id.trim().substring(0, 3).equals(newAppointId.trim().substring(0, 3)) && id.trim().substring(0, 3).equals(oldAppointId.trim().substring(0, 3)))
        {
            boolean flag=checkMaxLimit(id,newAppointId);
            if (flag){
                return generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,false);
            }
            boolean bookFlag=unpackJSON(swapAppointBooking(id,newAppointId,newAppointType));
            if (bookFlag){
                boolean cancelFlag=unpackJSON(swapCancelBooking(id,oldAppointId,oldAppointType));
                return cancelFlag? generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,true): generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,false);
            }
            else
            {
                return generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,false);
            }
        }
        else if(!id.trim().substring(0, 3).equals(newAppointId.trim().substring(0, 3)) && !id.trim().substring(0, 3).equals(oldAppointId.trim().substring(0, 3)))
        {
            if (newAppointId.trim().substring(6,newAppointId.length()).equals(oldAppointId.trim().substring(6,oldAppointId.length()))){
                boolean bookFlag=unpackJSON(swapAppointBooking(id,newAppointId,newAppointType));
                if (bookFlag){
                    boolean cancelFlag=unpackJSON(swapCancelBooking(id,oldAppointId,oldAppointType));
                    return cancelFlag? generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,true): generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,false);
                }
                else
                    return generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,false);
            }
            else {
                boolean flag=checkMaxLimit(id,newAppointId);
                if (flag)
                    return generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,false);
                else {
                    boolean bookFlag=unpackJSON(swapAppointBooking(id,newAppointId,newAppointType));
                    if (bookFlag){
                        boolean cancelFlag=unpackJSON(swapCancelBooking(id,oldAppointId,oldAppointType));
                        return cancelFlag? generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,true): generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,false);
                    }
                    else
                        generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,false);
                }
            }
        }
        return generateJSONObject(id,newAppointId,newAppointType,Constants.NONE,oldAppointId,oldAppointType,Constants.SWAP_OPERATION,false);
    }

    private String swapAppointBooking(String id, String appointId, String appointType) {
        if(appointType.trim().equals("physician")||appointType.trim().equals("surgeon")||appointType.trim().equals("dental"))
        {
            if(id.substring(0, 3).trim().equals(appointId.substring(0, 3).trim()))
            {
                return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.SWAP_OPERATION,q_data.bookAppoint(id,appointId,appointType));
            }
            else if(appointId.substring(0, 3).trim().equals("MTL"))
            {
                String msg="";
                msg=requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Montreal_PORT,Constants.BOOK_OPERATION);
                return !msg.trim().isEmpty()?msg.trim():generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.SWAP_OPERATION,false);
            }
            else if(appointId.substring(0, 3).trim().equals("QUE"))
            {
                String msg="";
                msg=requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Quebec_PORT,Constants.BOOK_OPERATION);
                return !msg.trim().isEmpty()?msg.trim():generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.SWAP_OPERATION,false);
            }
            else if(appointId.substring(0, 3).trim().equals("SHE"))
            {
                String msg="";
                msg=requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Sherbrook_PORT,Constants.BOOK_OPERATION);
                return !msg.trim().isEmpty()?msg.trim():generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.SWAP_OPERATION,false);
            }
            else
                return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.SWAP_OPERATION,false);
        }
        else
            return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.SWAP_OPERATION,false);
    }

    private boolean checkMaxLimit(String id, String appointId) {
        StringBuilder str=new StringBuilder();
        if(!id.substring(0, 3).trim().equals(appointId.substring(0, 3).trim()))
        {
            if(id.substring(0, 3).trim().equals("MTL"))
            {
                str.append(requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Sherbrook_PORT,"countOperation")+",");
                str.append(requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Quebec_PORT,"countOperation")+",");
            }
            else if(id.substring(0, 3).trim().equals("QUE"))
            {
                str.append(requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Sherbrook_PORT,"countOperation")+",");
                str.append(requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Montreal_PORT,"countOperation")+",");
            }
            else if(id.substring(0, 3).trim().equals("SHE"))
            {
                str.append(requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Montreal_PORT,"countOperation")+",");
                str.append(requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Quebec_PORT,"countOperation")+",");
            }
            String[] split=str.toString().trim().split(",");
            int total=0;
            for (int i = 0; i < split.length; i++)
            {
                total+=Integer.parseInt(split[i].trim());
            }
            if (total>=3)
            {
                return true;
            }
        }
        return false;
    }

    private boolean unpackJSON(String jsonString) {
        Object object=null;
        try {
            object=new JSONParser().parse(jsonString.trim());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject=(JSONObject) object;
        return Boolean.parseBoolean(jsonObject.get(Constants.OPERATION_STATUS).toString().trim());
    }

    private String swapCancelBooking(String id, String appointId, String appointType) {
        if(appointType.trim().equals("physician")||appointType.trim().equals("surgeon")||appointType.trim().equals("dental"))
        {
            if(id.substring(0, 3).trim().equals(appointId.substring(0, 3).trim()))
            {
                return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.SWAP_OPERATION,q_data.removeAppoint(id,appointId,appointType));
            }
            else if(appointId.substring(0, 3).trim().equals("MTL"))
            {
                String msg="";
                msg=requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Montreal_PORT,Constants.CANCEL_OPERATION);
                return !msg.trim().isEmpty()?msg.trim():generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.SWAP_OPERATION,false);
            }
            else if(appointId.substring(0, 3).trim().equals("QUE"))
            {
                String msg="";
                msg=requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Quebec_PORT,Constants.CANCEL_OPERATION);
                return !msg.trim().isEmpty()?msg.trim():generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.SWAP_OPERATION,false);
            }
            else if(appointId.substring(0, 3).trim().equals("SHE"))
            {
                String msg="";
                msg=requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Sherbrook_PORT,Constants.CANCEL_OPERATION);
                return !msg.trim().isEmpty()?msg.trim():generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.SWAP_OPERATION,false);
            }
            else
                return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.SWAP_OPERATION,false);

        }
        else
            return generateJSONObject(id,appointId,appointType,Constants.NONE,Constants.NONE,Constants.NONE,Constants.SWAP_OPERATION,false);
    }

    private boolean checkExistingAppoint(String id, String appointId, String appointType) {
        boolean result=false,msg=false;
        String data="";
        if(appointType.equals("physician")||appointType.equals("surgeon")||appointType.equals("dental"))
        {
            if(appointId.substring(0, 3).trim().equals(id.substring(0, 3).trim()))
            {
                msg=q_data.getAppoint(id,appointId,appointType);
                return msg==false?msg:true;
            }
            else if(appointId.substring(0, 3).equals("MTL"))
            {
                data=requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Montreal_PORT,"existanceOperation");
                return data.equals("Denies")? false:true;
            }
            else if(appointId.substring(0, 3).equals("QUE"))
            {
                data=requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Quebec_PORT,"existanceOperation");
                return data.equals("Denies")? false:true;
            }
            else if(appointId.substring(0, 3).equals("SHE"))
            {
                data=requestOnServer(id,appointId,Constants.NONE,Constants.NONE,Constants.LOCAL_Sherbrook_PORT,"existanceOperation");
                return data.equals("Denies")? false:true;
            }
            else
                return false;
        }
        else
            return false;
    }

    private String appointScheduleJSONObject(String id, String appointments, String operation, boolean status) {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(Constants.ID,id.trim());
        String[] splitappoint=appointments.trim().split(",");
        ArrayList<String> result=new ArrayList<String>();
        for (int i=0;i<splitappoint.length;i++){
            result.add(splitappoint[i].replaceAll("\\s+","").trim());
        }
        Collections.sort(result);
        jsonObject.put(Constants.LIST_APPOINTMENT_SCHEDULE,result.toString().trim());
        jsonObject.put(Constants.OPERATION,operation.trim());
        jsonObject.put(Constants.OPERATION_STATUS,status);
        return jsonObject.toString();
    }


    private String appointAvailableJSONObject(String id, String appointType, String appoint, String operation, boolean status) {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(Constants.ID,id.trim());
        jsonObject.put(Constants.APPOINTMENT_TYPE,appointType.trim());
        jsonObject.put(Constants.LIST_APPOINTMENT_AVAILABLE,appoint.trim());
        jsonObject.put(Constants.OPERATION,operation.trim());
        jsonObject.put(Constants.OPERATION_STATUS,status);
        return jsonObject.toString();

    }

    private String requestOnServer(String id, String appointId, String appointType, String capacity, int port, String operation) {
        DatagramSocket ds=null;
        try
        {
            ds=new DatagramSocket();
            String data=id+","+appointId+","+appointType+","+capacity+","+operation;
            DatagramPacket dp=new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName("localhost"), port);
            ds.send(dp);
            byte[] b=new byte[65535];
            DatagramPacket msg=new DatagramPacket(b, b.length);
            ds.receive(msg);
            String output=new String(msg.getData());
            return output;
        }
        catch (Exception e)
        {
            // TODO: handle exception
            System.out.println(e.getMessage());
        }
        finally {
            ds.close();
        }
        return "";
    }

    private String generateJSONObject(String id, String appointId, String appointType, String capacity, String oldAppointId, String oldAppointType, String operation, boolean status) {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(Constants.ID,id.trim());
        jsonObject.put(Constants.APPOINTMENT_ID,appointId.trim());
        jsonObject.put(Constants.APPOINTMENT_TYPE,appointType.trim());
        jsonObject.put(Constants.APPOINTMENT_CAPACITY,capacity.trim());
        jsonObject.put(Constants.OLD_APPOINTMENT_ID,oldAppointId.trim());
        jsonObject.put(Constants.OLD_APPOINTMENT_TYPE,oldAppointType.trim());
        jsonObject.put(Constants.OPERATION,operation.trim());
        jsonObject.put(Constants.OPERATION_STATUS,status);
        return jsonObject.toString();
    }

    private void setLogger(String loc, String id) {
        try
        {
            logger=Logger.getLogger(id);
            FileHandler fh = new FileHandler(loc, true);
            SimpleFormatter sf = new SimpleFormatter();
            fh.setFormatter(sf);
            logger.addHandler(fh);
        }
        catch (Exception e)
        {
            // TODO: handle exception
            System.out.println(e.getMessage());
        }
    }
}
