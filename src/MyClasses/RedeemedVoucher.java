/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MyClasses;

import com.mysql.jdbc.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javax.swing.JOptionPane;

/**
 *
 * @author dmalonas
 */
public class RedeemedVoucher extends Voucher
{
    protected String flightDate;
    protected String flightTime;
    protected String flightClub;
    protected String flightType;
    
    public RedeemedVoucher(String voucherCode, String purchaserName, boolean gift, String recipientName, String deliveryAddress, String emailAddress, String purchaseDate, String flightDate, String flightTime, String flightClub, String flightType)
    {
        super(voucherCode, purchaserName, gift, recipientName, deliveryAddress, emailAddress, purchaseDate);
        this.flightDate = flightDate;
        this.flightTime = flightTime;
        this.flightClub = flightClub;
        this.flightType = flightType;
        this.redeemed = true;
    }
    
    // We create this convenient constructor to easily create a redeemed voucher out of a non-redeemed
    public RedeemedVoucher(NonRedeemedVoucher nonRedeemedVoucher, String flightDate, String flightTime, String flightClub, String flightType)
    {
        super(nonRedeemedVoucher.voucherCode, nonRedeemedVoucher.purchaserName, nonRedeemedVoucher.gift, nonRedeemedVoucher.recipientName, nonRedeemedVoucher.deliveryAddress, nonRedeemedVoucher.emailAddress, nonRedeemedVoucher.purchaseDate);
        this.flightDate = flightDate;
        this.flightTime = flightTime;
        this.flightClub = flightClub;
        this.flightType = flightType;
    }
    
    public void setFlightDate(String flightDate)
    {
        this.flightDate = flightDate;
    }
    
    public String getFlightDate()
    {
        return flightDate;
    }
    
    public void setFlightTime(String flightTime)
    {
        this.flightTime = flightTime;
    }
    
    public String getFlightTime()
    {
        return flightTime;
    }
    
    public void setFlightClub(String flightClub)
    {
        this.flightClub = flightClub;
    }
    
    public String getFlightClub()
    {
        return flightClub;
    }
    
    public void setFlightType(String flightType)
    {
        this.flightType = flightType;
    }
    
    public String getFlightType()
    {
        return flightType;
    }
    
    public int updateToDB(Connection con)
    {
        Statement st = null;
        try
        {
            // Connection to DB
            //con = (com.mysql.jdbc.Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/glidingdb","root","");
            st = con.createStatement();
            // Update voucher data
            
            String updateStatement = "UPDATE `vouchers` SET REDEEMED = 1, `FLIGHT_DATE` = '" + flightDate + "', `FLIGHT_TYPE` = '" + flightType + "', `CLUB` = '" + flightClub + "', `FLIGHT_TIME` = '" + flightTime + "' WHERE `vouchers`.`CODE` = '" + voucherCode + "'";
            
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
