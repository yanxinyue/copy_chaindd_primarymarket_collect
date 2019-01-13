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
    @Column(name = "goal")
    private String goal="";
    @Column(name = "hype_score")
    private String hype_score="";
    @Column(name = "risk_score")
    private String risk_score="";
    @Column(name = "investment_rating")
    private String investment_rating="";
    @Column(name = "investment_rating_end_date")
    private String investment_rating_end_date	="";

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

    public ICObenchGeneral() {
    }

}
