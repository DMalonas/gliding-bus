/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MyClasses;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javax.swing.JOptionPane;

/**
 *
 * @author dmalonas
 */
public abstract class Voucher implements Serializable
{
    protected String voucherCode;
    protected String purchaserName;
    protected boolean gift;
    protected String recipientName;
    protected String deliveryAddress;
    protected String emailAddress;
    protected String purchaseDate;
    protected boolean redeemed;
    protected boolean completed;
    
    public Voucher(String voucherCode, String purchaserName, boolean gift, String recipientName, String deliveryAddress, String emailAddress, String purchaseDate)
    {
        this.voucherCode = voucherCode;
        this.purchaserName = purchaserName;
        this.gift = gift;
        this.recipientName = recipientName;
        this.deliveryAddress = deliveryAddress;
        this.emailAddress = emailAddress;
        this.purchaseDate = purchaseDate;
        this.redeemed = false;
        this.completed = false;
    }
    
    public void setVoucherCode(String voucherCode)
    {
        this.voucherCode = voucherCode;
    }
    
    public String getVoucherCode()
    {
        return this.voucherCode;
    }
    
    public void setPurchaserName(String purchaserName)
    {
        this.purchaserName = purchaserName;
    }
    
    public String getPurchaserName()
    {
        return this.purchaserName;
    }
    
    public void setGift(boolean gift)
    {
        this.gift = gift;
    }
    
    public boolean getGift()
    {
        return this.gift;
    }
    
    public void setRecipientName(String recipientName)
    {
        this.recipientName = recipientName;
    }
    
    public String getRecipientName()
    {
        return this.recipientName;
    }
    
    public void setDeliveryAddress(String deliveryAddress)
    {
        this.deliveryAddress = deliveryAddress;
    }
    
    public String getDeliveryAddress()
    {
        return this.deliveryAddress;
    }
    
    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }
    
    public String getEmailAddress()
    {
        return this.emailAddress;
    }
    
    public void setPurchaseDate(String purchaseDate)
    {
        this.purchaseDate = purchaseDate;
    }
    
    public String getPurchaseDate()
    {
        return this.purchaseDate;
    }
    
    public void setRedeemed(boolean redeemed)
    {
        this.redeemed = redeemed;
    }
    
    public boolean getRedeemed()
    {
        return this.redeemed;
    }
    
    public void setCompleted(boolean completed)
    {
        this.completed = completed;
    }
    
    public boolean getCompleted()
    {
        return this.completed;
    }
}
