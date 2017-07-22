package com.codesmell.app.model;



public class User  extends BaseEntity{
	
	private String email1;
    private String pwd;
    
 

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getEmail1() {
        return email1;
    }

    public void setEmail1(String email1) {
        this.email1 = email1;
    }


}