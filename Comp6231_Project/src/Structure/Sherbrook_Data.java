package Structure;

import java.util.HashMap;
import java.util.Map.Entry;

public class Sherbrook_Data
{

    HashMap<String, HashMap<String, HashMap<String, String>>> serverData;
    String serverName;

    public HashMap<String, HashMap<String, HashMap<String, String>>> getServerData()
    {
        return serverData;
    }

    public void setServerData(HashMap<String, HashMap<String, HashMap<String, String>>> serverData)
    {
        this.serverData = serverData;
    }

    public String getServerName()
    {
        return serverName;
    }

    public synchronized boolean addAppoint(String appointId, String appointType, String capacity)
    {
        //String result="";
        if(!serverData.containsKey(appointType))
        {
            //result="This appointment Type does not exist !";
            return false;
        }
        HashMap<String, HashMap<String, String>> sub_hash=serverData.get(appointType);
        if(sub_hash.containsKey(appointId))
        {
            HashMap<String, String> in_list=sub_hash.get(appointId);
            in_list.replace("capacity", in_list.get("capacity"), capacity);
            //System.out.println(in_list.get("total"))
            in_list.replace("totalbooking", in_list.get("totalbooking"), in_list.get("totalbooking"));
            in_list.replace("id", in_list.get("id"), in_list.get("id"));
            sub_hash.replace(appointId, serverData.get(appointType).get(appointId), in_list);
            serverData.replace(appointType, serverData.get(appointType), sub_hash);
            //result= appointId+" appointment capacity is updated to "+capacity+" for type"+appointType;
            return true;
        }
        else
        {
            HashMap<String, String> temp= new HashMap<>();
            temp.put("capacity", capacity);
            temp.put("totalbooking", "0");
            temp.put("id", "");
            sub_hash.put(appointId, temp);
            serverData.replace(appointType, serverData.get(appointType), sub_hash);
            //result=appointId+" is created for type "+appointType+" with capacity: "+capacity;
            return true;
        }
    }

    public synchronized boolean removeAppoint(String appointId, String appointType)
    {
        //String result="";
        if(!serverData.containsKey(appointType))
        {
            //result="This appointment Type does not exist !";
            return false;
        }
        HashMap<String, HashMap<String, String>> sub_hash=serverData.get(appointType);
        if(sub_hash.containsKey(appointId))
        {
            serverData.get(appointType).remove(appointId);
            //result= appointId+" is removed for type "+appointType;
            return true;
        }
        else
        {
            //result=appointId+" does not exist for type: "+appointType;
            return false;
        }
    }

    public synchronized String retriveAppointment(String appointType)
    {
        String result="";
        System.out.println("Appointment Type:"+appointType);
        if(serverData.containsKey(appointType))
        {
            HashMap<String, HashMap<String, String>> temp = serverData.get(appointType);
            if(temp.size()==0)
            {
                System.out.println("No data found");
                result="";
            }
            else
            {
                StringBuilder str = new StringBuilder();
                for (Entry<String, HashMap<String, String>> entry : temp.entrySet())
                {
                    if (Integer.parseInt(entry.getValue().get("capacity")) >= Integer.parseInt(entry.getValue().get("totalbooking")))
                        str.append(entry.getKey() + " " + (Integer.parseInt(entry.getValue().get("capacity"))- Integer.parseInt(entry.getValue().get("totalbooking"))) + ",");
                }
                return str.toString().trim();
            }
        }
        else
            result= "No appointment type found";
        return result;

    }

    public synchronized boolean bookAppoint(String id, String appointId, String appointType)
    {
        //String result="";
        if(serverData.containsKey(appointType))
        {
            HashMap<String, HashMap<String, String>> sub_hash=serverData.get(appointType);
            if(sub_hash.size()==0)
            {
                //result="No appointments found";
                return false;
            }
            else
            {
                if(sub_hash.containsKey(appointId))
                {
                    HashMap<String, String> in_hash=sub_hash.get(appointId);
                    //System.out.println(in_hash.get("totalbooking"));
                    if(Integer.parseInt(in_hash.get("capacity"))==Integer.parseInt(in_hash.get("totalbooking")))
                    {
                        //result= appointType+" is not available for booking";
                        return true;
                    }
                    else
                    {
                        StringBuilder str=new StringBuilder(in_hash.get("id"));
                        if(in_hash.get("id").contains(id))
                        {
                            //result= id+" has already booked the appointment "+appointId+" of type:"+appointType;
                            return false;
                        }
                        str.append(id.trim());
                        in_hash.replace("id", in_hash.get("id"),in_hash.get(id)+str.toString().trim()+",");
                        in_hash.replace("capacity", in_hash.get("capacity"), Integer.toString(Integer.parseInt(in_hash.get("capacity"))));
                        in_hash.replace("totalbooking", in_hash.get("totalbooking"), Integer.toString(Integer.parseInt(in_hash.get("totalbooking"))+1));
                        sub_hash.replace(appointId, sub_hash.get(appointId), in_hash);
                        serverData.replace(appointType, serverData.get(appointType), sub_hash);
                        //result=id+" has booked the appointment "+appointId+" of type "+appointType;
                        return true;
                    }
                }
                else
                    return false;
            }
        }
        else
            return false;
    }

    public synchronized boolean removeAppoint(String id,String appointId,String appointType)
    {
        //String result="";
        if(serverData.containsKey(appointType))
        {
            HashMap<String, HashMap<String, String>> sub_hash=serverData.get(appointType);
            if(sub_hash.size()==0)
            {
                //result="No appointments found";
                return false;
            }
            else
            {
                if(sub_hash.containsKey(appointId))
                {
                    HashMap<String, String> in_hash=sub_hash.get(appointId);
                    if(Integer.parseInt(in_hash.get("totalbooking"))==0)
                    {
                        //result=appointId+" is not booked anymore";
                        return false;
                    }
                    else
                    {
                        StringBuilder str=new StringBuilder(in_hash.get("id"));
                        if(!in_hash.get("id").contains(id))
                        {
                            //result= id+" has not booked event"+appointId+" of type:"+appointType;
                            return false;
                        }
                        //in_hash.replace(id, str.toString().replace(id.trim()+",",""));
                        //in_hash.replace("id", in_hash.get("id"),in_hash.get(id)+str.toString().trim()+",");
                        in_hash.replace("id", in_hash.get("id"),"");
                        in_hash.replace("capacity", in_hash.get("capacity"), Integer.toString(Integer.parseInt(in_hash.get("capacity"))));
                        in_hash.replace("totalbooking", in_hash.get("totalbooking"), Integer.toString(Integer.parseInt(in_hash.get("totalbooking"))-1));
                        sub_hash.replace(appointId, sub_hash.get(appointId), in_hash);
                        serverData.replace(appointType, serverData.get(appointType), sub_hash);
                        //result=id+" has cancelled the event "+appointId+" of type "+appointType;
                        return true;
                    }
                }
                else
                    return false;
            }
        }
        else
            return false;
    }

    public synchronized String getBookingSchedule(String id)
    {
        StringBuilder str = new StringBuilder();
        for (Entry<String, HashMap<String, HashMap<String, String>>> data : serverData.entrySet())
        {
            for (Entry<String, HashMap<String, String>> sub_data : data.getValue().entrySet())
            {
                if (sub_data.getValue().get("id").contains(id.trim()))
                {
                    str.append(data.getKey() + " = " + sub_data.getKey() + ",");
                }
            }
        }
        return str.length() == 0 ? "" : str.toString();
    }

    public synchronized boolean getAppoint(String id, String appointId, String appointType)
    {
        //String result="";
        if(serverData.containsKey(appointType))
        {
            HashMap<String, HashMap<String, String>> sub_hash = serverData.get(appointType);
            if(sub_hash.size()==0)
            {
                //result="No data found";
                return false;
            }
            else
            {
                if(sub_hash.containsKey(appointId))
                {
                    HashMap<String, String> in_hash = sub_hash.get(appointId);
                    return in_hash.get("id").contains(id.trim());
                }
                else
                    return false;
            }
        }
        return false;

    }

    public Sherbrook_Data()
    {
        // TODO Auto-generated constructor stub
        serverData=new HashMap<>();
        serverData.put("physician", new HashMap<>());
        serverData.put("surgeon", new HashMap<>());
        serverData.put("dental", new HashMap<>());
    }

    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }

    public synchronized String getBookingCount(String id, String appointType)
    {
        int c=0;
        String m=appointType.trim().substring(6, appointType.trim().length());
        for (Entry<String, HashMap<String, HashMap<String, String>>> data : serverData.entrySet())
        {
            for (Entry<String, HashMap<String, String>> sub_data : data.getValue().entrySet())
            {
                if (sub_data.getValue().get("id").trim().contains(id.trim()) && sub_data.getKey().substring(6, sub_data.getKey().trim().length()).trim().equals(m.trim()))
                    c++;
            }
        }
        return Integer.toString(c);
    }

}


