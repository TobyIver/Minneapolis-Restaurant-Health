package com.iverson.toby.rhealth;

/**
 * Created by Toby on 3/23/2015.
 */
public class Violation {
    private  int id;
    private String name="";
    private String address="";
    private String date="";
    private String riskLevel="";
    private String codeViolation="";
    private String violationText="";
    private String critical="";
    private int rating;

    /************* Define Setter Methods *********/

    public void setid(int id)
    {
        this.id = id;
    }
    public void setName(String a)
    {
        this.name = a;
    }
    public void setAddress(String a)
    {
        this.address = a;
    }
    public void setDate(String a)
    {
        this.date = a;
    }
    public void setRiskLevel(String a)
    {
        this.riskLevel = a;
    }
    public void setViolationText(String a)
    {
        this.violationText = a;
    }
    public void setCodeViolation(String a)
    {
        this.codeViolation = a;
    }
    public void setCritial(String a)
    {
        this.critical = a;
    }
    public void setRating(int a)
    {
        this.rating = a;
    }



    /************* Define Getter Methods *********/

    public int getid()
    {
        return id;
    }

    public String getName()
    {
        return this.name;
    }

    public String getAddress()
    {
        return this.address;
    }

    public String getDate()
    {
        return this.date;
    }

    public String getRiskLevel()
    {
        return this.riskLevel;
    }

    public String getCodeViolation() { return this.codeViolation; }

    public String getViolationText()
    {
        return this.violationText;
    }

    public String getCritical()
    {
        return this.critical;
    }

    public int getRating()
    {
        return this.rating;
    }

}