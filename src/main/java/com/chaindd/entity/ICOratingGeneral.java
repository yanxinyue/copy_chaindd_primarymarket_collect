package com.chaindd.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @Author: xinyueyan
 * @Date: 1/7/2019 2:03 PM
 */
public class ICOratingGeneral implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "logo_url")
    private String logoUrl="";
    @Column(name = "ico_name",unique=true, nullable=false)
    private String name="";
    @Column(name = "token")
    private String token="";
    @Column(name = "introduction")
    private String introduction="";
    @Column(name = "restrictionskyc")
    private String restrictionsKYC="";
    @Column(name = "whitelist")
    private String whitelist="";
    @Column(name = "countries",columnDefinition = "text")
    private String countries="";
    @Column(name = "start")
    private String start="";
    @Column(name = "end")
    private String end="";
    @Column(name = "rate")
    private String rate="";
    @Column(name = "pre")
    private boolean pre=false;
}
