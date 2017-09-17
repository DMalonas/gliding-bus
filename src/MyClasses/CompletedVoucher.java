/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MyClasses;

import com.mysql.jdbc.Connection;
import java.sql.Statement;
import javax.swing.JOptionPane;

/**
 *
 * @author dmalonas
 */
public class CompletedVoucher extends RedeemedVoucher
{
    private String flightDuration;
    private String gliderNumber;
    private String glidingInstructor;
    
    public CompletedVoucher(String voucherCode, String purchaserName, boolean gift, String recipientName, String deliveryAddress, String emailAddress, 
            String purchaseDate, String flightDate, String flightTime, String flightClub, String flightType, 
            String flightDuration, String gliderNumber, String glidingInstructor)
    {
        super(voucherCode, purchaserName, gift, recipientName, deliveryAddress, emailAddress, purchaseDate, flightDate, flightTime, flightClub, flightType);
        this.flightDuration = flightDuration;
        this.gliderNumber = gliderNumber;
        this.glidingInstructor = glidingInstructor;
        this.completed = true;
    }
    
    // We create this convenient constructor to easily create a redeemed voucher out of a non-redeemed
    public CompletedVoucher(RedeemedVoucher redeemedVoucher, String flightDuration, String gliderNumber, String glidingInstructor)
    {
        super(redeemedVoucher.voucherCode, redeemedVoucher.purchaserName, redeemedVoucher.gift, redeemedVoucher.recipientName, 
                redeemedVoucher.deliveryAddress, redeemedVoucher.emailAddress, redeemedVoucher.purchaseDate, 
                redeemedVoucher.flightDate, redeemedVoucher.flightTime, redeemedVoucher.flightClub, redeemedVoucher.flightClub);
        this.flightDuration = flightDuration;
        this.gliderNumber = gliderNumber;
        this.glidingInstructor = glidingInstructor;
    }
    
    public void setFlightDuration(String flightDuration)
    {
        this.flightDuration = flightDuration;
    }
    
    public String getFlightDuration()
    {
        return flightDuration;
    }
    
    public void setGliderNumber(String gliderNumber)
    {
        this.gliderNumber = gliderNumber;
    }
    
    public String getGliderNumber()
    {
        return gliderNumber;
    }
    
    public void setGlidingInstructor(String glidingInstructor)
    {
        this.glidingInstructor = glidingInstructor;
    }
    
    public String getGlidingInstructor()
    {
        return glidingInstructor;
    }
    
    public int updateToDB(Connection con)
    {
        Statement st = null;
        try
        {
            st = con.createStatement();
            
            // Update voucher data
            String updateStatement = "UPDATE `vouchers` SET COMPLETED = 1, `FLIGHT_DURATION` = '" + flightDuration + "', `GLIDER_NUMBER` = '" + gliderNumber + "', `GLIDING_INSTRUCTOR` = '" + glidingInstructor + "' WHERE `vouchers`.`CODE` = '" + voucherCode + "'";
            
            st.executeUpdate(updateStatement);
            st.close();
            return 1;
        }
        catch (Exception e)
        {
            // Show error message
            JOptionPane.showMessageDialog(null, "Something went wrong with the voucher update.", "DB Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }
    
}
