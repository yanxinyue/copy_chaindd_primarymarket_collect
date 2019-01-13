package com.chaindd.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @Author: xinyueyan
 * @Date: 1/6/2019 4:25 PM
 */
@Entity
@Table(name = "primarymarket_icobench_ico_sociallink" )
@Data
public class ICObenchSocialLinks {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ico_name",unique=true, nullable=false)
    private String name="";
    @Column(name = "github")
    private String github="";
    @Column(name = "twitter")
    private String twitter="";
    @Column(name = "telegram")
    private String telegram="";

    public ICObenchSocialLinks() {
    }
}
