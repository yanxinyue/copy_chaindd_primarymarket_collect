package com.chaindd.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * @Author: xinyueyan
 * @Date: 1/2/2019 5:17 PM
 */
@Entity
@Table(name = "primarymarket_icobench_ico_info" )
@Data
public class ICObenchInfo {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ico_name",unique=true, nullable=false)
    private String name="";
    @Column(name = "tokensymbol")
    private String tokenSymbol="";
    @Column(name = "tokenlogo")
    private String tokenLogo="";
    @Column(name = "funding_status")
    private String fundingStatus="";
    @Column(name = "ico_startdate")
    private String icoStartDate="";
    @Column(name = "ico_enddate")
    private String icoEndDate="";
    @Column(name = "ico_price")
    private String icoPrice="";
    @Column(name = "current_supply")
    private String currentSupply="";
    @Column(name = "overview",columnDefinition = "text")
    private String overview="";
    @Column(name = "features",columnDefinition = "text")
    private String features="";
    @Column(name = "country_of_incorporation")
    private String countryOfIncorporation="";
    @Column(name = "fundraising_instrument")
    private String fundraisingInstrument="";
    @Column(name = "token_distribution")
    private String tokenDistribution="";
    @Column(name = "funds_allocation")
    private String fundsAllocation="";
    @Column(name = "whitepaper_businessplan_url")
    private String whitepaperURLOrBusinessPlanURL="";
    @Column(name = "platform")
    private String platform="";
    @Column(name = "hardcap")
    private String hardCap="";
    @Column(name = "softcap")
    private String softCap="";
    @Column(name = "registration_year")
    private String registrationYear="";
    @Column(name = "token_type")
    private String tokenType="";
    @Column(name = "kyc")
    private String kyc="";
    @Column(name = "mvp_prototype")
    private String mvp_prototype="";
    @Column(name = "restricted_countries",columnDefinition = "text")
    private String restrictedCountries="";
    @Column(name = "core_team_member_names_titles",columnDefinition = "json")
    private String currentCoreTeamMembers;
    @Column(name = "linkedin_profile")
    private String linkedinProfile="";
    @Column(name = "price_preico")
    private String priceInPreICO="";
    @Column(name = "preico_start")
    private String preICOstart="";
    @Column(name = "preico_end")
    private String preICOend="";
    @Column(name = "bonus",columnDefinition = "text")
    private String bonus="";
    @Column(name = "milestone",columnDefinition = "json")
    private String milestone;
    @Column(name = "minimum_investment")
    private String minimumInvestment="";
    @Column(name = "distributed_in_ico")
    private String distributedInICO="";
    @Column(name = "bounty")
    private String bounty="";
    @Column(name = "official_website")
    private String officialWebsite="";

    public ICObenchInfo() {
    }

}
