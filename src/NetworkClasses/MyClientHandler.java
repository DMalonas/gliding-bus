package MyNetworkClasses;

import MyClasses.Club;
import MyClasses.CompletedVoucher;
import MyClasses.NonRedeemedVoucher;
import MyClasses.RedeemedVoucher;
import MyClasses.Voucher;
import com.mysql.jdbc.Connection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dmalonas
 */
public class MyClientHandler implements Runnable
{
    //declare a Socket 
    private final Socket socket;            
    
    //declare PrintWriter for sending Voucher objects to the server
    private final PrintWriter printWriter;
   
    /**
     * declare BufferedReader to wrap around our InputStreamReader Reader 
     * to reduce the cost of the operation of reading from the Server
     */
    private final BufferedReader bufferedReader;
    
    //counter to keep count of the number of connected clients to the server
    private static int connectionCount = 0;
    
    //each client will have a connection number
    private final int connectionNumber;

    /**
     * Constructor just initialises the connection to client.
     * 
     * @throws IOException if an I/O error occurs when creating the input and
     * @param socket the socket to establish the connection to client.
     * @param printWriter Writer for writing to the server
     * @param bufferedReader Reader to read from server
     * output streams, or if the socket is closed, or socket is not connected.
     */
    public MyClientHandler(Socket socket) throws IOException
    {
        this.socket = socket;
        printWriter = new PrintWriter(socket.getOutputStream(), true);
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //increment the coutner that indicates the number connections to the server
        connectionCount++;
        connectionNumber = connectionCount;
        threadSays("Connection " + connectionNumber + " established.");
    }

    /**
     * The run method is overridden from the Runnable interface. It is called
     * when the Thread is in a 'running' state - usually after thread.start()
     * is called. This method reads client requests and processes names until
     * an exception is thrown.
     * A connection with the glidingdb mysql database is established 
     * @param con is the Connection variable with the db's information
     * @param textFromClient used to store the info that defines
     * the type of interaction with the db
     * 
     */
    @Override
    public void run()
    {
        // Read type of communication and establish connection with the db
        try
        {
            // Read and process names until an exception is thrown.
            threadSays("Waiting for type of communication from client...");
            
            
            String textFromClient = bufferedReader.readLine();
            Connection con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/glidingdb","root","");
            // Handle communication according to its type
            if (textFromClient.equals("NEW_VOUCHER"))
                handleNewVoucher(con);
            else if (textFromClient.equals("REDEEM_VOUCHER"))
                handleRedeemVoucher(con);
            else if (textFromClient.equals("COMPLETE_VOUCHER"))
                handleCompleteVoucher(con);
            else if (textFromClient.equals("DELETE_VOUCHER"))
                handleDeleteVoucher(con);
            else if (textFromClient.equals("VIEW_ALL_VOUCHERS"))
                handleViewAllVouchers(con);
            else if (textFromClient.equals("SEARCH_BY_CODE"))
                handleSearchByCode(con);
            else if (textFromClient.equals("FILTER_BY_CLUB"))
                handleFilterByClub(con);
        }
        catch (Exception ex)
        {
            Logger.getLogger(MyClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                threadSays("We have lost connection to client " + connectionNumber + ".");
                socket.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(MyClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    /**
     * The method handleNewVoucer is used to handle the creation of a new
     * Voucher object
     * 
     * @param con The first and only param at the handleNewVoucher method 
     * is used to establish connection with the database
     * 
     */
    private void handleNewVoucher(Connection con)
    {
        try
        {
            printWriter.print("OK\r\n"); // Send confirmation to client
            printWriter.flush();
            System.out.println("Received request for new voucher. Receiving data...");
            // Receive new voucher
            ObjectInputStream serverInputStream = new ObjectInputStream(socket.getInputStream());
            NonRedeemedVoucher newVoucher = (NonRedeemedVoucher)serverInputStream.readObject();
            serverInputStream.close();
            System.out.println("Data received");
            // Connect to DB
            con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/glidingdb","root","");
            // Write to DB

            //Insert to DB
            if (newVoucher.insertToDB(con) > 0)
            {
                // Confirm everything is fine
                System.out.println("Voucher succesfully created in DB.");
            }
        }
        catch (Exception ex)
        {
            Logger.getLogger(MyClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try {
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(MyClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void handleRedeemVoucher(Connection con)
    {
        try {
            printWriter.print("OK\r\n"); // Send confirmation to client
            printWriter.flush();
            System.out.println("Received request for redeeming voucher. Sending data...");
            // Send non-redeemed vouchers
            // Connect to DB
            con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/glidingdb","root","");
            // Retrieve and load to the memory the available vouchers to redeem
            ArrayList<NonRedeemedVoucher> listVouchers = new ArrayList<NonRedeemedVoucher>(); // It is our memory that keeps/edits temp data from DB for better speed performance
            Statement st = con.createStatement();
            String queryStatement = "select * from vouchers where redeemed = 0";
            ResultSet rs = st.executeQuery(queryStatement);
            while (rs.next())
            {
                // Create Voucher instance and insert to ArrayList
                NonRedeemedVoucher tempVoucher = new NonRedeemedVoucher(rs.getString("CODE"), 
                        rs.getString("CUSTOMER_NAME"), 
                        (rs.getString("GIFT").equals("1")) ? true:false, 
                        rs.getString("GIFT_RECIPIENT_NAME"),
                        rs.getString("DELIVERY_ADDRESS"), 
                        rs.getString("CUSTOMER_EMAIL_ADDRESS"),
                        rs.getString("PURCHASE_DATE"));
                listVouchers.add(tempVoucher);
            }
            rs.close();
            st.close();
            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientOutputStream.writeObject(listVouchers);
            
            // Send clubs' information
            ArrayList<Club> listClubs = new ArrayList<Club>(); // It is our memory that keeps/edits temp data from DB for better speed performance
            // Retrieve and load to the memory the available clubs and flight types per club
            st = con.createStatement();
            queryStatement = "select * from clubs";
            rs = st.executeQuery(queryStatement);
            while (rs.next())
            {
                // Create Club instance and insert to ArrayList
                Club tempClub = new Club(rs.getString("NAME"), 
                        (rs.getString("AEROTOW").equals("1")) ? true:false,
                        (rs.getString("WINCH").equals("1")) ? true:false);
                listClubs.add(tempClub);
            }
            rs.close();
            st.close();
            try
            {
                clientOutputStream.writeObject(listClubs);
                // Receive voucher to redeem
                ObjectInputStream serverInputStream = new ObjectInputStream(socket.getInputStream());
                RedeemedVoucher newRedeemedVoucher = (RedeemedVoucher)serverInputStream.readObject();
                serverInputStream.close();
                // Redeem in DB
                if (newRedeemedVoucher.updateToDB(con) > 0)
                    System.out.println("Voucher succesfully redeemed in DB.");
            }
            catch (Exception ex)
            {
                System.out.println("Lost connection");
            }
        } catch (Exception ex) {
            Logger.getLogger(MyClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleCompleteVoucher(Connection con)
    {
        try 
        {
            printWriter.print("OK\r\n"); // Send confirmation to client
            printWriter.flush();
            System.out.println("Received request for completing voucher. Sending data...");
            // Send redeemed vouchers
            // Connect to DB
            con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/glidingdb","root","");
            // Retrieve and load to the memory the available vouchers to complete
            ArrayList<RedeemedVoucher> listVouchers = new ArrayList<RedeemedVoucher>(); // It is our memory that keeps/edits temp data from DB for better speed performance
            Statement st = con.createStatement();
            String queryStatement = "select * from vouchers where redeemed = 1";
            ResultSet rs = st.executeQuery(queryStatement);
            while (rs.next())
            {
                // Create Voucher instance and insert to ArrayList
                RedeemedVoucher tempVoucher = new RedeemedVoucher(rs.getString("CODE"), 
                        rs.getString("CUSTOMER_NAME"), 
                       (rs.getString("GIFT").equals("1")) ? true:false,
                        rs.getString("GIFT_RECIPIENT_NAME"), 
                        rs.getString("DELIVERY_ADDRESS"),
                        rs.getString("CUSTOMER_EMAIL_ADDRESS"),
                        rs.getString("PURCHASE_DATE"), 
                        rs.getString("FLIGHT_DATE"), 
                        rs.getString("FLIGHT_TIME"), 
                        rs.getString("CLUB"), 
                        rs.getString("FLIGHT_TYPE")   );
                listVouchers.add(tempVoucher);
            }
            rs.close();
            st.close();
            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientOutputStream.writeObject(listVouchers);
            
            try
            {
                // Receive voucher to complete
                ObjectInputStream serverInputStream = new ObjectInputStream(socket.getInputStream());
                CompletedVoucher newCompletedVoucher = (CompletedVoucher)serverInputStream.readObject();
                serverInputStream.close();
                // Complete in DB
                if (newCompletedVoucher.updateToDB(con) > 0)
                    System.out.println("Voucher succesfully completed in DB.");
            }
            catch (Exception ex)
            {
                System.out.println("Lost connection");
            }
        } catch (Exception ex) {
            Logger.getLogger(MyClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleDeleteVoucher(Connection con)
    {
        try 
        {
            printWriter.print("OK\r\n"); // Send confirmation to client
            printWriter.flush();
            System.out.println("Received request for deleting voucher. Sending data...");
            // Send redeemed vouchers
            // Connect to DB
            con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/glidingdb","root","");
            // Retrieve and load to the memory the available vouchers to complete
            ArrayList<Voucher> listVouchers = new ArrayList<Voucher>(); // It is our memory that keeps/edits temp data from DB for better speed performance
            Statement st = con.createStatement();
            String queryStatement = "select * from vouchers";
            ResultSet rs = st.executeQuery(queryStatement);
            while (rs.next())
            {
                // Create Voucher instance according to its type and insert to ArrayList (Polymorphism)
                if (rs.getString("REDEEMED").equals("0")) // This means it is non-redeemed
                {
                    NonRedeemedVoucher tempVoucher = new NonRedeemedVoucher(rs.getString("CODE"),
                            rs.getString("CUSTOMER_NAME"), 
                            (rs.getString("GIFT").equals("1")) ? true:false,
                            rs.getString("GIFT_RECIPIENT_NAME"), 
                            rs.getString("DELIVERY_ADDRESS"), 
                            rs.getString("CUSTOMER_EMAIL_ADDRESS"), 
                            rs.getString("PURCHASE_DATE"));
                    listVouchers.add(tempVoucher);
                }
                else if (rs.getString("COMPLETED").equals("1")) // This means it is redeemed and completed
                {
                    CompletedVoucher tempVoucher = new CompletedVoucher(rs.getString("CODE"),
                            rs.getString("CUSTOMER_NAME"), 
                            (rs.getString("GIFT").equals("1")) ? true:false,
                            rs.getString("GIFT_RECIPIENT_NAME"), 
                            rs.getString("DELIVERY_ADDRESS"),
                            rs.getString("CUSTOMER_EMAIL_ADDRESS"),
                            rs.getString("PURCHASE_DATE"), 
                            rs.getString("FLIGHT_DATE"),
                            rs.getString("FLIGHT_TIME"),
                            rs.getString("CLUB"),
                            rs.getString("FLIGHT_TYPE"), 
                            rs.getString("FLIGHT_DURATION"),
                            rs.getString("GLIDER_NUMBER"),
                            rs.getString("GLIDING_INSTRUCTOR"));
                    listVouchers.add(tempVoucher);
                }
                else // This means it is redeemed and non-completed
                {
                    RedeemedVoucher tempVoucher = new RedeemedVoucher(rs.getString("CODE"),
                            rs.getString("CUSTOMER_NAME"), 
                            (rs.getString("GIFT").equals("1")) ? true:false,
                            rs.getString("GIFT_RECIPIENT_NAME"), 
                            rs.getString("DELIVERY_ADDRESS"),
                            rs.getString("CUSTOMER_EMAIL_ADDRESS"),
                            rs.getString("PURCHASE_DATE"), 
                            rs.getString("FLIGHT_DATE"),
                            rs.getString("FLIGHT_TIME"),
                            rs.getString("CLUB"),
                            rs.getString("FLIGHT_TYPE")   );
                    listVouchers.add(tempVoucher);
                }
            }
            rs.close();
            st.close();
            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientOutputStream.writeObject(listVouchers);
            
            try
            {
                // Receive voucher to delete (just the voucher code actually) 
                ObjectInputStream serverInputStream = new ObjectInputStream(socket.getInputStream());
                String voucherCodeToDelete = (String)serverInputStream.readObject();
                serverInputStream.close();
                System.out.println(voucherCodeToDelete);
                // Delete from DB
                st = con.createStatement();
                String deleteStatement = "DELETE FROM VOUCHERS WHERE CODE = '" + voucherCodeToDelete + "'";
                st.executeUpdate(deleteStatement);
                st.close();
            }
            catch (Exception ex)
            {
                System.out.println("Lost connection");
            }
        } catch (Exception ex) {
            Logger.getLogger(MyClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleViewAllVouchers(Connection con)
    {
        try 
        {
            printWriter.print("OK\r\n"); // Send confirmation to client
            printWriter.flush();
            System.out.println("Received request for viewing all vouchers. Sending data...");
            // Send redeemed vouchers
            // Connect to DB
            con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/glidingdb","root","");
            // Retrieve and load to the memory the available vouchers to complete
            ArrayList<Voucher> listVouchers = new ArrayList<Voucher>(); // It is our memory that keeps/edits temp data from DB for better speed performance
            Statement st = con.createStatement();
            String queryStatement = "select * from vouchers";
            ResultSet rs = st.executeQuery(queryStatement);
            while (rs.next())
            {
                // Create Voucher instance according to its type and insert to ArrayList (Polymorphism)
                if (rs.getString("REDEEMED").equals("0")) // This means it is non-redeemed
                {
                    NonRedeemedVoucher tempVoucher = new NonRedeemedVoucher(rs.getString("CODE"),
                            rs.getString("CUSTOMER_NAME"), 
                            (rs.getString("GIFT").equals("1")) ? true:false,
                            rs.getString("GIFT_RECIPIENT_NAME"), 
                            rs.getString("DELIVERY_ADDRESS"),
                            rs.getString("CUSTOMER_EMAIL_ADDRESS"),
                            rs.getString("PURCHASE_DATE"));
                    listVouchers.add(tempVoucher);
                }
                else if (rs.getString("COMPLETED").equals("1")) // This means it is redeemed and completed
                {
                    CompletedVoucher tempVoucher = new CompletedVoucher(rs.getString("CODE"),
                            rs.getString("CUSTOMER_NAME"), 
                            (rs.getString("GIFT").equals("1")) ? true:false,
                            rs.getString("GIFT_RECIPIENT_NAME"), 
                            rs.getString("DELIVERY_ADDRESS"),
                            rs.getString("CUSTOMER_EMAIL_ADDRESS"),
                            rs.getString("PURCHASE_DATE"), 
                            rs.getString("FLIGHT_DATE"),
                            rs.getString("FLIGHT_TIME"),
                            rs.getString("CLUB"),
                            rs.getString("FLIGHT_TYPE"), 
                            rs.getString("FLIGHT_DURATION"),
                            rs.getString("GLIDER_NUMBER"),
                            rs.getString("GLIDING_INSTRUCTOR"));
                    listVouchers.add(tempVoucher);
                }
                else // This means it is redeemed and non-completed
                {
                    RedeemedVoucher tempVoucher = new RedeemedVoucher(rs.getString("CODE"),
                            rs.getString("CUSTOMER_NAME"), 
                            (rs.getString("GIFT").equals("1")) ? true:false,
                            rs.getString("GIFT_RECIPIENT_NAME"), 
                            rs.getString("DELIVERY_ADDRESS"),
                            rs.getString("CUSTOMER_EMAIL_ADDRESS"),
                            rs.getString("PURCHASE_DATE"), 
                            rs.getString("FLIGHT_DATE"),
                            rs.getString("FLIGHT_TIME"),
                            rs.getString("CLUB"),
                            rs.getString("FLIGHT_TYPE")   );
                    listVouchers.add(tempVoucher);
                }
            }
            rs.close();
            st.close();
            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientOutputStream.writeObject(listVouchers);
            
        } catch (Exception ex) {
            Logger.getLogger(MyClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleSearchByCode(Connection con)
    {
        try 
        {
            printWriter.print("OK\r\n"); // Send confirmation to client
            printWriter.flush();
            String codeFromClient = bufferedReader.readLine();
            System.out.println("Received request for searching vouchers by code " + codeFromClient + ". Sending data...");
            // Send redeemed vouchers
            // Connect to DB
            con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/glidingdb","root","");
            // Retrieve and load to the memory the available vouchers to complete
            ArrayList<Voucher> listVouchers = new ArrayList<Voucher>(); // It is our memory that keeps/edits temp data from DB for better speed performance
            Statement st = con.createStatement();
            String queryStatement = "select * from vouchers where code like '%" + codeFromClient + "%'";
            ResultSet rs = st.executeQuery(queryStatement);
            while (rs.next())
            {
                // Create Voucher instance according to its type and insert to ArrayList (Polymorphism)
                if (rs.getString("REDEEMED").equals("0")) // This means it is non-redeemed
                {
                    NonRedeemedVoucher tempVoucher = new NonRedeemedVoucher(rs.getString("CODE"),
                            rs.getString("CUSTOMER_NAME"), 
                            (rs.getString("GIFT").equals("1")) ? true:false,
                            rs.getString("GIFT_RECIPIENT_NAME"), 
                            rs.getString("DELIVERY_ADDRESS"),
                            rs.getString("CUSTOMER_EMAIL_ADDRESS"),
                            rs.getString("PURCHASE_DATE"));
                    listVouchers.add(tempVoucher);
                }
                else if (rs.getString("COMPLETED").equals("1")) // This means it is redeemed and completed
                {
                    CompletedVoucher tempVoucher = new CompletedVoucher(rs.getString("CODE"),
                            rs.getString("CUSTOMER_NAME"), 
                            (rs.getString("GIFT").equals("1")) ? true:false,
                            rs.getString("GIFT_RECIPIENT_NAME"), 
                            rs.getString("DELIVERY_ADDRESS"),
                            rs.getString("CUSTOMER_EMAIL_ADDRESS"),
                            rs.getString("PURCHASE_DATE"), 
                            rs.getString("FLIGHT_DATE"),
                            rs.getString("FLIGHT_TIME"),
                            rs.getString("CLUB"),
                            rs.getString("FLIGHT_TYPE"), 
                            rs.getString("FLIGHT_DURATION"),
                            rs.getString("GLIDER_NUMBER"),
                            rs.getString("GLIDING_INSTRUCTOR"));
                    listVouchers.add(tempVoucher);
                }
                else // This means it is redeemed and non-completed
                {
                    RedeemedVoucher tempVoucher = new RedeemedVoucher(rs.getString("CODE"),
                            rs.getString("CUSTOMER_NAME"), 
                            (rs.getString("GIFT").equals("1")) ? true:false,
                            rs.getString("GIFT_RECIPIENT_NAME"), 
                            rs.getString("DELIVERY_ADDRESS"),
                            rs.getString("CUSTOMER_EMAIL_ADDRESS"),
                            rs.getString("PURCHASE_DATE"), 
                            rs.getString("FLIGHT_DATE"),
                            rs.getString("FLIGHT_TIME"),
                            rs.getString("CLUB"),
                            rs.getString("FLIGHT_TYPE")   );
                    listVouchers.add(tempVoucher);
                }
            }
            rs.close();
            st.close();
            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientOutputStream.writeObject(listVouchers);
            
        } catch (Exception ex) {
            Logger.getLogger(MyClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void handleFilterByClub(Connection con)
    {
        try 
        {
            printWriter.print("OK\r\n"); // Send confirmation to client
            printWriter.flush();
            System.out.println("Received request for filtering vouchers by club. Sending available club names...");
            // Send redeemed vouchers
            // Connect to DB
            con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/glidingdb","root","");
            
            // Send clubs' information
            ArrayList<String> listClubs = new ArrayList<String>(); // The club name is enough for our purposes
            // Retrieve and load to the memory the available club names
            Statement st = con.createStatement();
            String queryStatement = "select name from clubs";
            ResultSet rs = st.executeQuery(queryStatement);
            while (rs.next())
            {
                // Create Club name string instance and insert to ArrayList
                listClubs.add(rs.getString("NAME"));
            }
            rs.close();
            st.close();
            try
            {
                ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
                clientOutputStream.writeObject(listClubs);
            }
            catch (Exception ex)
            {
                System.out.println("Lost connection");
            }
            
            // Receive the club name to filter
            String clubFromClient = bufferedReader.readLine();
            System.out.println("Received request for searching filtering by club name " + clubFromClient + ". Sending data...");
            // Connect to DB
            con = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/glidingdb","root","");
            // Retrieve and load to the memory the available vouchers
            ArrayList<Voucher> listVouchers = new ArrayList<Voucher>(); // It is our memory that keeps/edits temp data from DB for better speed performance
            st = con.createStatement();
            queryStatement = "select * from vouchers where club = '" + clubFromClient + "'";
            rs = st.executeQuery(queryStatement);
            while (rs.next())
            {
                // Create Voucher instance according to its type and insert to ArrayList (Polymorphism)
                if (rs.getString("REDEEMED").equals("0")) // This means it is non-redeemed
                {
                    NonRedeemedVoucher tempVoucher = new NonRedeemedVoucher(rs.getString("CODE"),
                            rs.getString("CUSTOMER_NAME"), 
                            (rs.getString("GIFT").equals("1")) ? true:false,
                            rs.getString("GIFT_RECIPIENT_NAME"), 
                            rs.getString("DELIVERY_ADDRESS"),
                            rs.getString("CUSTOMER_EMAIL_ADDRESS"),
                            rs.getString("PURCHASE_DATE"));
                    listVouchers.add(tempVoucher);
                }
                else if (rs.getString("COMPLETED").equals("1")) // This means it is redeemed and completed
                {
                    CompletedVoucher tempVoucher = new CompletedVoucher(rs.getString("CODE"),
                            rs.getString("CUSTOMER_NAME"), 
                            (rs.getString("GIFT").equals("1")) ? true:false,
                            rs.getString("GIFT_RECIPIENT_NAME"), 
                            rs.getString("DELIVERY_ADDRESS"),
                            rs.getString("CUSTOMER_EMAIL_ADDRESS"),
                            rs.getString("PURCHASE_DATE"), 
                            rs.getString("FLIGHT_DATE"),
                            rs.getString("FLIGHT_TIME"),
                            rs.getString("CLUB"),
                            rs.getString("FLIGHT_TYPE"), 
                            rs.getString("FLIGHT_DURATION"),
                            rs.getString("GLIDER_NUMBER"),
                            rs.getString("GLIDING_INSTRUCTOR"));
                    listVouchers.add(tempVoucher);
                }
                else // This means it is redeemed and non-completed
                {
                    RedeemedVoucher tempVoucher = new RedeemedVoucher(rs.getString("CODE"),
                            rs.getString("CUSTOMER_NAME"), 
                            (rs.getString("GIFT").equals("1")) ? true:false,
                            rs.getString("GIFT_RECIPIENT_NAME"), 
                            rs.getString("DELIVERY_ADDRESS"),
                            rs.getString("CUSTOMER_EMAIL_ADDRESS"),
                            rs.getString("PURCHASE_DATE"), 
                            rs.getString("FLIGHT_DATE"),
                            rs.getString("FLIGHT_TIME"),
                            rs.getString("CLUB"),
                            rs.getString("FLIGHT_TYPE")   );
                    listVouchers.add(tempVoucher);
                }
            }
            rs.close();
            st.close();
            ObjectOutputStream clientOutputStream = new ObjectOutputStream(socket.getOutputStream());
            clientOutputStream.writeObject(listVouchers);
            
        } catch (Exception ex) {
            Logger.getLogger(MyClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Private helper method outputs to standard output stream for debugging.
     * @param say the String to write to standard output stream.
     */
    private void threadSays(String say) {
        System.out.println("ClientHandlerThread" + connectionNumber + ": " + say);
    }
}
