package com.chaindd.entity;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @Author: xinyueyan
 * @Date: 12/29/2018 3:51 PM
 */
@Entity
@Table(name = "primarymarket_icobench_ico_list" )
@Data
public class ICObenchGeneral implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "logo_url")
    private String logoUrl="";
    @Column(name = "ico_name",unique=true, nullable=false)
    private String name="";
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
    @Column(name = "info_url")
    private String infoUrl="";

    public ICObenchGeneral() {
    }

}
