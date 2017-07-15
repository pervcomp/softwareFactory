package org.springframework.sample.module;


import org.springframework.data.annotation.Id;

public class User {


	@Id
    private String _id;

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

    public void setEmail(String email1) {
        this.email1 = email1;
    }


}