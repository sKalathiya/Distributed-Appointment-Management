package FrontEnd;

import app.*;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

public class startFrontEnd {

    public static void main(String[] args) {
        try {

            // create and initialize the ORB //// get reference to rootpoa &amp; activate
            // the POAManager
            ORB orb = ORB.init(args, null);
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant

            FrontEndObj fe = new FrontEndObj();

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // get object reference from the servant
            org.omg.CORBA.Object feRef = rootpoa.servant_to_reference(fe);
            FrontEnd fehref = FrontEndHelper.narrow(feRef);

            NameComponent path1[] = ncRef.to_name("FE");

            ncRef.rebind(path1, fehref);

            System.out.println("Server ready and waiting ...");

            // wait for invocations from clients
            for (;;) {
                orb.run();
            }
        }

        catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }

        System.out.println("FrontEnd Exiting ...");

    }
}
