package com.chaindd.service;

import com.chaindd.Constants.DataConstants;
import com.chaindd.dao.ICOdao;
import com.chaindd.dao.ICOinfoDao;
import com.chaindd.dao.SocialsDao;
import com.chaindd.entity.ICObenchGeneral;
import com.chaindd.entity.ICObenchInfo;
import com.chaindd.entity.ICObenchSocialLinks;
import com.chaindd.utils.DateUtil;
import com.chaindd.utils.OkHttpClientUtil;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.chaindd.utils.DateUtil.formatTransform;

/**
 * @Author: xinyueyan
 * @Date: 1/1/2019 10:34 PM
 */
@Service
@Slf4j
public class ICObenchService {
    protected final Gson gson = new GsonBuilder().disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
    @Autowired
    private EntityManager em;

    @Transactional
    public void getData() throws IOException {
        String listUrl = "";
        int pages = 1;
        LinkedList icoList = new LinkedList();
        String html = OkHttpClientUtil.getData(DataConstants.url + "/icos?page=1");
        //TODO  防反扒
        //Jsoup.connect(DataConstants.url+"/icos?page=1").

        if (StringUtils.isBlank(html)) {
            return;
        }
        Elements pages_link = Jsoup.parse(html).select("div[class=ico_list]").select("div.pages");
        if (pages_link != null && pages_link.size() > 0) {
            pages = Integer.parseInt(pages_link.first().select("a.next").first().previousElementSibling().text());
        }
        for (int i = 1; i <= pages; i++) {
            //设置间隔时间
            try {
                long longBounded = RandomUtils.nextLong(1000, 10000);
                TimeUnit.MILLISECONDS.sleep(longBounded);
                log.info("间隔" + TimeUnit.MILLISECONDS.toSeconds(longBounded) + "秒");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            listUrl = DataConstants.url + "/icos?page=" + i;
            log.info("ICObench_list==" + listUrl);
            LinkedList<ICObenchGeneral> icoPageList = new LinkedList();
            String htmlData = OkHttpClientUtil.getData(listUrl);
            if (StringUtils.isBlank(htmlData)) {
                continue;
            }
            Document doc = Jsoup.parse(htmlData);
            Elements ico_list = doc.select("div[class=ico_list]");
            for (Element ico : ico_list) {
                Elements trs = ico.select("tr");
                Elements ths = trs.get(0).select("th");
                for (Element tr : trs) {
                    ICObenchGeneral icOgeneral = new ICObenchGeneral();
                    Elements tds = tr.select("td");
                    if (tds.size() > 0) {
                        Element td0 = tds.get(0);
                        String start = tds.get(1).text();
                        String end = tds.get(2).text();
                        String rate = tds.get(3).select("div").text();
                        String logoUrl = DataConstants.url + td0.select("div[class=image_box]").select("a[class=image]").get(0).attr("style").split("'")[1];
                        Element content = td0.select("div[class=content]").get(0);
                        Element first = content.select("a[class=name notranslate]").first();
                        List<TextNode> textNodes = first.textNodes();
                        String nameStr = textNodes.get(0).text();
                        if (nameStr.contains("(PreICO)")) {
                            icOgeneral.setPre(true);
                        }
                        String name = StringUtils.substringBefore(nameStr, "(");
                        String ico_name = StringUtils.strip(name);
                        Element p = content.select("p[class=notranslate]").get(0);
                        List<Node> nodes = p.childNodes();
                        String introduction = nodes.get(0).toString();
                        icOgeneral.setLogoUrl(logoUrl);
                        icOgeneral.setName(ico_name);
                        icOgeneral.setIntroduction(introduction);
                        icOgeneral.setStart(start);
                        icOgeneral.setEnd(end);
                        icOgeneral.setRate(rate);
                        for (Node node : nodes) {
                            if (node.toString().contains("Restrictions KYC:")) {
                                String kycString = node.nextSibling().toString();
                                icOgeneral.setRestrictionsKYC(kycString);
                            }
                            if (node.toString().contains("Whitelist:")) {
                                String whitelistString = node.nextSibling().toString();
                                icOgeneral.setWhitelist(whitelistString);
                            }
                            if (node.toString().contains("Countries:")) {
                                String countriesString = node.nextSibling().toString();
                                icOgeneral.setCountries(countriesString);
                            }
                        }
                        icoPageList.add(icOgeneral);
                    }
                }
            }
            log.info("第" + i + "页项目列表分解完成");
            //TODO ICObench列表入库  有则更新  无则添加
            //saveICObenchList(icoPageList);
            //icoDao.saveAll(icoPageList);
            if (i == pages) {
                break;
            }
        }

    }

    /**
     * 列表入库
     *
     * @param icoPageList
     */
    public void saveICObenchList(LinkedList<ICObenchGeneral> icoPageList) {
        if (icoPageList != null || icoPageList.size() > 0) {
            StringBuffer sqlStr = new StringBuffer();
            sqlStr.append("INSERT INTO primarymarket_icobench_ico_list(logo_url,ico_name,introduction," +
                    "restrictionskyc,whitelist,countries,start,end,rate,pre) VALUES");
            for (int index = 0; index < icoPageList.size(); index++) {
                sqlStr.append("(?,?,?,?,?,?,?,?,?,?),");
            }
            sqlStr.deleteCharAt(sqlStr.length() - 1);
            sqlStr.append(" ON DUPLICATE KEY UPDATE logo_url=VALUES(logo_url),introduction=VALUES(introduction)," +
                    "restrictionskyc=VALUES(restrictionskyc),whitelist=VALUES(whitelist),countries=VALUES(countries)," +
                    "start=VALUES(start),end=VALUES(end),rate=VALUES(rate),pre=VALUES(pre)");
            int p = 1;
            Query nativeQuery = em.createNativeQuery(sqlStr.toString());
            for (ICObenchGeneral icOgeneral : icoPageList) {
                nativeQuery.setParameter(p, icOgeneral.getLogoUrl());
                nativeQuery.setParameter(p + 1, icOgeneral.getName());
                nativeQuery.setParameter(p + 2, icOgeneral.getIntroduction());
                nativeQuery.setParameter(p + 3, icOgeneral.getRestrictionsKYC());
                nativeQuery.setParameter(p + 4, icOgeneral.getWhitelist());
                nativeQuery.setParameter(p + 5, icOgeneral.getCountries());
                nativeQuery.setParameter(p + 6, icOgeneral.getStart());
                nativeQuery.setParameter(p + 7, icOgeneral.getEnd());
                nativeQuery.setParameter(p + 8, icOgeneral.getRate());
                nativeQuery.setParameter(p + 9, icOgeneral.isPre());
                p += 10;
            }
            nativeQuery.executeUpdate();
        }
    }

    /**
     * 项目详情抓取
     *
     * @param
     * @throws IOException
     */
    @Transactional
    public void getICOinfo() throws IOException, ParseException {
        String listUrl = "";
        int pages = 1;
        String html = OkHttpClientUtil.getData(DataConstants.url + "/icos?page=1");
        if (StringUtils.isBlank(html)) {
            return;
        }
        Elements pages_link = Jsoup.parse(html).select("div[class=ico_list]").select("div.pages");
        if (pages_link != null && pages_link.size() > 0) {
            pages = Integer.parseInt(pages_link.first().select("a.next").first().previousElementSibling().text());
        }
        for (int i = 1; i <= pages; i++) {
            try {
                long longBounded = RandomUtils.nextLong(1000, 10000);
                TimeUnit.MILLISECONDS.sleep(longBounded);
                log.info("间隔" + TimeUnit.MILLISECONDS.toSeconds(longBounded) + "秒");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LinkedList<ICObenchInfo> icoInfoList = new LinkedList();
            LinkedList<ICObenchSocialLinks> socialLinksList = new LinkedList();
            listUrl = DataConstants.url + "/icos?page=" + i;
            log.info("ICObench_info==" + listUrl);
            String htmlData = OkHttpClientUtil.getData(listUrl);
            if (StringUtils.isBlank(htmlData)) {
                continue;
            }
            Document doc = Jsoup.parse(htmlData);
            Elements ico_list = doc.select("div[class=ico_list]");
            for (Element ico : ico_list) {
                Elements trs = ico.select("tr");
                for (Element tr : trs) {
                    Elements tds = tr.select("td");
                    if (tds.size() > 0) {
                        Element td0 = tds.get(0);
                        Element content = td0.select("div[class=content]").get(0);
                        String infoUrl = DataConstants.url + content.select("a[class=name notranslate]").attr("href");
                        //String infoUrl ="https://icobench.com/icos?page=43";
                        String features = "";
                        ICObenchInfo icoInfo = new ICObenchInfo();
                        ICObenchSocialLinks socialLinks = new ICObenchSocialLinks();
                        String infoHtmlData = OkHttpClientUtil.getData(infoUrl);
                        Document infoDoc = Jsoup.parse(infoHtmlData);
                        Element icoPage = infoDoc.getElementById("page");
                        //上
                        Element profile_header = icoPage.getElementById("profile_header");
                        //上左
                        if (profile_header == null) {
                            continue;
                        }

                        Element ico_information = profile_header.select("div[class=ico_information]").get(0);
                        Element row = ico_information.select("div[class=row]").get(0);
                        //logo链接
                        String logoUrl = DataConstants.url + row.select("div[class=image]").get(0).select("img").get(0).attr("src");
                        icoInfo.setTokenLogo(logoUrl);
                        //项目名称 (preICO)
                        String nameText = row.select("div.name").get(0).select("h1").get(0).text();
                        if (nameText.contains("(PreICO)")) {
                            //icOgeneral.setPre(true);
                        }
                        String name = StringUtils.substringBefore(nameText, "(");
                        name = StringUtils.strip(name);
                        icoInfo.setName(name);
                        socialLinks.setName(name);
                        //overview
                        String overview = ico_information.select("p").get(0).text();
                        icoInfo.setOverview(overview);

                        //上右
                        Element fixed_data = profile_header.select("div[class=fixed_data]").get(0);
                        //ratingFeature
                        Element rating_data = fixed_data.select("div[class=rating]").get(0);
                        String rating_expert = rating_data.select("div[itemprop=ratingValue]").get(0).select("small").get(0).text();
                        if (!"0 expert ratings".equals(rating_expert)) {
                            features += "," + DataConstants.FEATURE_EXPERT_RATINGS;
                        }
                        Element financial_data = fixed_data.select("div[class=financial_data]").get(0);
                        //freetokenFeature
                        Elements free_data = fixed_data.select("div[class=rating freetokens]");
                        if (free_data != null && free_data.size() > 0) {
                            features += "," + DataConstants.FEATURE_FREE_TOKENS;
                        }
                        //  时间处理+项目status处理
                        Elements timeRaws = financial_data.select("div[class=row]");
                        if (timeRaws != null && timeRaws.size() > 0) {
                            String nowTime = DateUtil.getNowTime(DataConstants.DATE_FORMAT_2);
                            for (Element timeRow : timeRaws) {
                                String label = timeRow.getElementsByTag("label").first().text();
                                if ("Status".equals(label)) {
                                    String statusText = timeRow.select("div.number").first().text();
                                    if ("Ended".equals(statusText)) {
                                        icoInfo.setFundingStatus("Completed");
                                    }
                                }
                                if ("Time".equals(label)) {
                                    String statusText = timeRow.select("div.number").first().text();
                                    if ("Unknown".equals(statusText)) {
                                        //icoInfo.setFundingStatus("Completed");
                                        //时间均未知,不做处理
                                    }
                                }
                                if ("ICO Time".equals(label)) {
                                    String statusText = timeRow.select("div.number").first().text();
                        /*if("Ended".equals(statusText)){
                        }*/
                                    //获取正式时间   分割起止时间
                                    String icoDateStr = timeRow.getElementsByTag("small").first().text();
                                    String[] dates = icoDateStr.split(" - ");
                                    if (dates != null || dates.length > 0) {
                                        String start = dates[0];
                                        String end = dates[1];
                                        //  与当前时间对比,判断status
                                        //nowTime = DateUtil.getNowTime(DataConstants.DATE_FORMAT_2);
                                        int status = DateUtil.compareDate(DataConstants.DATE_FORMAT_2, nowTime, start);
                                        if (status == 1) //未开始
                                            icoInfo.setFundingStatus(DataConstants.FUNDING_STATUS_UPCOMING);
                                        int j = DateUtil.compareDate(DataConstants.DATE_FORMAT_2, nowTime, end);
                                        if (j == 3) //已结束
                                            icoInfo.setFundingStatus(DataConstants.FUNDING_STATUS_COMPLETED);
                                        if ((status == 3 && j == 1) || (status == 2 || j == 2))
                                            icoInfo.setFundingStatus(DataConstants.FUNDING_STATUS_ONGOING);

                                        icoInfo.setIcoStartDate(start);
                                        icoInfo.setIcoEndDate(end);
                                    }

                                }
                                if ("PreICO time".equals(label)) {
                                    String statusText = timeRow.select("div.number").first().text();
                        /*if("Ended".equals(statusText)){
                        }*/
                                    //获取预售时间   分割起止时间
                                    String preIcoDateStr = timeRow.getElementsByTag("small").first().text();
                                    String[] dates = preIcoDateStr.split(" - ");
                                    if (dates != null || dates.length > 0) {
                                        String start = dates[0];
                                        String end = dates[1];
                                        icoInfo.setPreICOstart(start);
                                        icoInfo.setPreICOend(end);
                                    }
                                }
                            }
                        }

                        //社交媒体
                        Elements socials = fixed_data.select("div[class=socials]");

                        if (socials != null && socials.size() > 0) {
                            Elements socials_a = socials.first().select("a");
                            if (socials_a != null && socials.size() > 0) {
                                for (Element a : socials_a) {
                                    if ("Linkedin".equals(a.text())) {
                                        String url = a.attr("href");
                                        icoInfo.setLinkedinProfile(url);
                                    }
                                    if ("GitHub".equals(a.text())) {
                                        String url = a.attr("href");
                                        socialLinks.setGithub(url);
                                    }
                                    if ("Twitter".equals(a.text())) {
                                        String url = a.attr("href");
                                        socialLinks.setTwitter(url);
                                    }
                                    if ("Telegram".equals(a.text())) {
                                        String url = a.attr("href");
                                        socialLinks.setTelegram(url);
                                    }
                                }
                            }
                        }
                        socialLinksList.add(socialLinks);
                        // dataRaws处理+Feature拼接
                        Elements dataRaws = financial_data.select("div[class=data_row]");
                        Elements button_big = financial_data.select("a.button_big");
                        if (button_big != null && button_big.size() > 0) {
                            String more_on_ICO = StringUtils.strip(button_big.first().text());
                            if ("More on ICO".equals(more_on_ICO)) {
                                String official_web_href = button_big.first().attr("href");
                                if (StringUtils.isNotBlank(official_web_href)) {
                                    String[] split = official_web_href.split("\\?");
                                    if (split.length > 0)
                                        icoInfo.setOfficialWebsite(split[0]);
                                }
                            }
                        }
                        for (Element dataRaw : dataRaws) {
                            String key = StringUtils.strip(dataRaw.child(0).text());
                            Element b = dataRaw.child(1).select("b").first();
                            if ("Token".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                icoInfo.setTokenSymbol(value);//symbol
                            }
                            if ("PreICO Price".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                icoInfo.setPriceInPreICO(value);//preICOprice
                            }
                            if ("Price".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                icoInfo.setIcoPrice(value);//ICOprice
                            }
                            if ("Bonus".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                if ("Available".equals(value)) {
                                    features += "," + DataConstants.FEATURE_BONUS_AVAILABLE;
                                }
                            }
                            if ("Bounty".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                if ("Available".equals(value)) {
                                    features += "," + DataConstants.FEATURE_BOUNTY_AVAILABLE;
                                }
                                String bountyLink = b.select("a").get(0).attr("href");
                                icoInfo.setBounty(bountyLink);//bounty
                            }
                            if ("MVP/Prototype".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                if ("Available".equals(value)) {
                                    features += "," + DataConstants.FEATURE_MVP_PROTOTYPE_AVAILABLE;
                                }
                                String prototypeLink = b.select("a").get(0).attr("href");
                                icoInfo.setMvp_prototype(prototypeLink);//Prototype
                            }
                            if ("Platform".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                icoInfo.setPlatform(value);//Platform
                            }
                            if ("Accepting".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                icoInfo.setFundraisingInstrument(value);//接受货币
                            }
                            if ("Soft cap".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                icoInfo.setSoftCap(value);//Soft cap
                            }
                            if ("Hard cap".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                icoInfo.setHardCap(value);//Hard cap
                            }
                            if ("Country".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                icoInfo.setCountryOfIncorporation(value);//注册地
                            }
                            if ("Restricted areas".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                icoInfo.setRestrictedCountries(value);//受限地区
                            }
                            if ("Minimum investment".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                icoInfo.setMinimumInvestment(value);//最小投资额
                            }
                            if ("Whitelist/KYC".equals(key)) {
                                String value = StringUtils.strip(b.text());
                                if (value.contains("KYC"))
                                    icoInfo.setKyc("Yes");//kyc
                                else
                                    icoInfo.setKyc("No");
                            }
                            // 处理时间格式,to:yyyy-MM-dddd
                            String nowTime = DateUtil.getNowTime(DataConstants.DATE_FORMAT_2);
                            int status = 0;
                            if ("ICO start".equals(key)) {
                                String valueStr = StringUtils.strip(b.text());
                                String value = formatTransform(valueStr, DataConstants.DATE_FORMAT_1, DataConstants.DATE_FORMAT_2);
                                status = DateUtil.compareDate(DataConstants.DATE_FORMAT_2, nowTime, value);
                                if (status == 1) //未开始
                                    icoInfo.setFundingStatus(DataConstants.FUNDING_STATUS_UPCOMING);
                                if (status == 2)
                                    icoInfo.setFundingStatus(DataConstants.FUNDING_STATUS_ONGOING);
                                icoInfo.setIcoStartDate(value);//正式开始时间
                            }
                            if ("ICO end".equals(key)) {
                                String valueStr = StringUtils.strip(b.text());
                                String value = formatTransform(valueStr, DataConstants.DATE_FORMAT_1, DataConstants.DATE_FORMAT_2);
                                int j = DateUtil.compareDate(DataConstants.DATE_FORMAT_2, nowTime, value);
                                if (status == 3) //已结束
                                    icoInfo.setFundingStatus(DataConstants.FUNDING_STATUS_COMPLETED);
                                if (status == 2)
                                    icoInfo.setFundingStatus(DataConstants.FUNDING_STATUS_ONGOING);
                                icoInfo.setIcoEndDate(value);//正式结束时间
                            }

                        }

                        //下
                        Element profile_content = icoPage.getElementById("profile_content");
                        //下左
                        Element content_left = profile_content.select("div[class=content]").get(0);
                        Element content_right = profile_content.select("div[class=content_right]").get(0);
                        Elements kyc_passed = content_right.select("div[class=icon passed]");
                        if (kyc_passed != null && kyc_passed.size() > 0) {
                            features += "," + DataConstants.FEATURE_KYC_PASSED;
                        }
                        Element tabs = content_left.select("div[class=tabs]").get(0);
                        //白皮书链接
                        Elements tab_a = tabs.select("a");
                        if (tab_a != null && tab_a.size() > 0) {
                            for (Element a : tab_a) {
                                if ("White paper".equals(a.text())) {
                                    String whitePalerUrl = a.attr("href");
                                    icoInfo.setWhitepaperURLOrBusinessPlanURL(whitePalerUrl);
                                }
                            }
                        }

                        Element about = content_left.getElementById("about");
                        //团队成员
                        LinkedHashMap<String, HashMap<String, String>> allMembersMap = new LinkedHashMap<>();
                        Element team = content_left.getElementById("team");
                        Elements h2 = team.select("h2");
                        if (h2 != null && h2.size() > 0) {
                            Element teamTitle = h2.first();
                        }
                        Elements teamRows = team.select("div.row");
                        if (teamRows != null && teamRows.size() > 0) {
                            for (Element teamRow : teamRows) {
                                String key = "";
                                HashMap<String, String> name_title = new HashMap<>();
                                Element element = teamRow.previousElementSibling();
                                if (element == null)
                                    continue;
                                String tagName = element.tagName();
                                //如果有小标题  以小标题为键
                                if ("h3".equals(tagName)) {
                                    key = teamRow.previousElementSibling().text();
                                } else {//无小标题 键值为"TEAM"
                                    key = "Team";
                                }
                                Elements membersOfThisKey = teamRow.select("div.col_3");
                                for (Element member : membersOfThisKey) {
                                    String memberName = member.getElementsByTag("h3").first().text();
                                    String memberTitle = "";
                                    Elements memberTitles = member.getElementsByTag("h4");
                                    if (memberTitles != null && memberTitles.size() > 0) {
                                        memberTitle = memberTitles.first().text();
                                    }
                                    name_title.put(memberName, memberTitle);
                                }
                                allMembersMap.put(key, name_title);
                            }
                            if (StringUtils.isNotBlank(gson.toJson(allMembersMap)) && allMembersMap.size() > 0) {
                                icoInfo.setCurrentCoreTeamMembers(gson.toJson(allMembersMap));
                            }

                        }
                        //重大事件
                        Element milestones = content_left.getElementById("milestones");
                        //String milestonesTitle = milestones.select("h2").get(0).text();
                        Element milestonesBox = milestones.select("div[class=box]").get(0);
                        Elements milestonesRaws = milestonesBox.select("div[class=row]");
                        if (milestonesRaws != null && milestonesRaws.size() > 0) {
                            LinkedHashMap<String, String> milestonesMap = new LinkedHashMap<>();
                            for (Element milestoneRaw : milestonesRaws) {
                                int number = Integer.parseInt(milestoneRaw.select("div[class=number]").get(0).text());
                                String milestoneDate = milestoneRaw.select("div[class=condition]").get(0).text();
                                String milestoneText = milestoneRaw.select("p").get(0).text();
                                milestonesMap.put(milestoneDate, milestoneText);
                            }
                            if (StringUtils.isNotBlank(gson.toJson(milestonesMap)))
                                icoInfo.setMilestone(gson.toJson(milestonesMap));
                        }
                        //leftBox  financial详情
                        Element financial = content_left.select("div[id=financial]").get(0);
                        //String financialTitle = financial.select("h2").get(0).text();
                        Elements financialBoxLefts = financial.select("div[class=box_left]");
                        Elements financialBoxRights = financial.select("div[class=box_right]");
                        if (financialBoxLefts != null && financialBoxLefts.size() > 0) {
                            Element financialLeft = financialBoxLefts.get(0);
                            if ("Token info".equals(financialLeft.select("h3").get(0).text())) {
                                Elements tokenInfoRaws = financialLeft.select("div[class=row]");
                                if (tokenInfoRaws != null && tokenInfoRaws.size() > 0) {
                                    for (Element tokenInfoRaw : tokenInfoRaws) {

                                        Elements label = tokenInfoRaw.select("div.label");
                                        if (label != null && label.size() > 0) {
                                            String labelText = label.get(0).text();
                                            if ("Token".equals(labelText)) {
                                                String valueText = tokenInfoRaw.select("div.value").get(0).text();
                                                icoInfo.setTokenSymbol(valueText);
                                            }
                                            if ("Platform".equals(labelText)) {
                                                String valueText = tokenInfoRaw.select("div.value").get(0).text();
                                                icoInfo.setPlatform(valueText);
                                            }
                                            if ("Price in ICO".equals(labelText)) {
                                                String valueText = tokenInfoRaw.select("div.value").get(0).text();
                                                icoInfo.setIcoPrice(valueText);
                                            }
                                            if ("PreICO price".equals(labelText)) {
                                                String valueText = tokenInfoRaw.select("div.value").get(0).text();
                                                icoInfo.setPriceInPreICO(valueText);
                                            }
                                            if ("Type".equals(labelText)) {
                                                String valueText = tokenInfoRaw.select("div.value").get(0).text();
                                                icoInfo.setTokenType(valueText);
                                            }


                                        }
                                        //处理bonus
                                        Elements h4 = tokenInfoRaw.select("h4");
                                        if (h4 != null && h4.size() > 0) {
                                            if ("Bonus".equals(h4.first().text())) {
                                                Element bonus_text = tokenInfoRaw.select("div.bonus_text").first();
                                                icoInfo.setBonus(bonus_text.text());

                                            }
                                        }
                                    }
                                }
                            }

                        }
                        // rightbox  financial信息
                        if (financialBoxRights != null && financialBoxRights.size() > 0) {
                            Element financialRight = financialBoxRights.get(0);
                            if ("Investment info".equals(financialRight.select("h3").get(0).text())) {
                                Elements tokenInfoRaws = financialRight.select("div[class=row]");
                                if (tokenInfoRaws != null && tokenInfoRaws.size() > 0) {
                                    for (Element tokenInfoRaw : tokenInfoRaws) {
                                        Elements label = tokenInfoRaw.select("div.label");
                                        if (label != null && label.size() > 0) {
                                            String labelText = label.get(0).text();
                                            if ("Min. investment".equals(labelText)) {
                                                String valueText = tokenInfoRaw.select("div.value").get(0).text();
                                                icoInfo.setMinimumInvestment(valueText);
                                            }
                                            if ("Accepting".equals(labelText)) {
                                                String valueText = tokenInfoRaw.select("div.value").get(0).text();
                                                icoInfo.setFundraisingInstrument(valueText);
                                            }
                                            if ("Distributed in ICO".equals(labelText)) {
                                                String valueText = tokenInfoRaw.select("div.value").get(0).text();
                                                icoInfo.setDistributedInICO(valueText);
                                            }
                                            if ("Soft cap".equals(labelText)) {
                                                String valueText = tokenInfoRaw.select("div.value").get(0).text();
                                                icoInfo.setSoftCap(valueText);
                                            }
                                            if ("Hard cap".equals(labelText)) {
                                                String valueText = tokenInfoRaw.select("div.value").get(0).text();
                                                icoInfo.setHardCap(valueText);
                                            }
                                        }

                                    }
                                }
                            }
                        }
                        //Element ratings = content_left.select("div[id=ratings]").get(0);
                        if (StringUtils.isNotBlank(features)) {
                            icoInfo.setFeatures(features.substring(1));
                        }
                        icoInfoList.add(icoInfo);
                    }
                }
            }
            //入库
            //saveICObenchInfo(icoInfoList);
            log.info("====第" + i + "页项目详情已入库");
            //saveICObenchSocial(socialLinksList);
            log.info("====第" + i + "页项目社交主页已入库");

        }
    }

    /**
     * 详情入库
     *
     * @param icoInfoList
     */
    public void saveICObenchInfo(LinkedList<ICObenchInfo> icoInfoList) {
        if (icoInfoList != null || icoInfoList.size() > 0) {
            StringBuffer sqlStr = new StringBuffer();
            sqlStr.append("INSERT INTO primarymarket_icobench_ico_info(ico_name,tokensymbol,tokenlogo,overview,funding_status," +
                    "ico_startdate,ico_enddate,ico_price,current_supply,features,country_of_incorporation," +
                    "fundraising_instrument,token_distribution,funds_allocation,whitepaper_businessplan_url,platform," +
                    "hardcap,softcap,registration_year,token_type,kyc,mvp_prototype,restricted_countries," +
                    "core_team_member_names_titles,linkedin_profile,price_preico,preico_start,preico_end,bonus,milestone," +
                    "minimum_investment,distributed_in_ico,bounty,official_website) VALUES");
            for (int index = 0; index < icoInfoList.size(); index++) {
                sqlStr.append("(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?),");
            }
            sqlStr.deleteCharAt(sqlStr.length() - 1);
            sqlStr.append(" ON DUPLICATE KEY UPDATE overview=VALUES(overview),funding_status=VALUES(funding_status)," +
                    "ico_startdate=VALUES(ico_startdate),ico_enddate=VALUES(ico_enddate)," +
                    "ico_price=VALUES(ico_price),current_supply=VALUES(current_supply),features=VALUES(features)," +
                    "country_of_incorporation=VALUES(country_of_incorporation),fundraising_instrument=VALUES(fundraising_instrument)," +
                    "token_distribution=VALUES(token_distribution),funds_allocation=VALUES(funds_allocation)," +
                    "whitepaper_businessplan_url=VALUES(whitepaper_businessplan_url)," +
                    "platform=VALUES(platform),hardcap=VALUES(hardcap),softcap=VALUES(softcap)," +
                    "registration_year=VALUES(registration_year),token_type=VALUES(token_type),kyc=VALUES(kyc)," +
                    "mvp_prototype=VALUES(mvp_prototype),restricted_countries=VALUES(restricted_countries)," +
                    "core_team_member_names_titles=VALUES(core_team_member_names_titles),linkedin_profile=VALUES(linkedin_profile)," +
                    "price_preico=VALUES(price_preico),preico_start=VALUES(preico_start),preico_end=VALUES(preico_end)," +
                    "bonus=VALUES(bonus),milestone=VALUES(milestone)," +
                    "minimum_investment=VALUES(minimum_investment),distributed_in_ico=VALUES(distributed_in_ico)," +
                    "bounty=VALUES(bounty),official_website=VALUES(official_website)");
            int p = 1;
            Query nativeQuery = em.createNativeQuery(sqlStr.toString());
            for (ICObenchInfo icoInfo : icoInfoList) {
                nativeQuery.setParameter(p, icoInfo.getName());
                nativeQuery.setParameter(p + 1, icoInfo.getTokenSymbol());
                nativeQuery.setParameter(p + 2, icoInfo.getTokenLogo());
                nativeQuery.setParameter(p + 3, icoInfo.getOverview());
                nativeQuery.setParameter(p + 4, icoInfo.getFundingStatus());
                nativeQuery.setParameter(p + 5, icoInfo.getIcoStartDate());
                nativeQuery.setParameter(p + 6, icoInfo.getIcoEndDate());
                nativeQuery.setParameter(p + 7, icoInfo.getIcoPrice());
                nativeQuery.setParameter(p + 8, icoInfo.getCurrentSupply());
                nativeQuery.setParameter(p + 9, icoInfo.getFeatures());
                nativeQuery.setParameter(p + 10, icoInfo.getCountryOfIncorporation());
                nativeQuery.setParameter(p + 11, icoInfo.getFundraisingInstrument());
                nativeQuery.setParameter(p + 12, icoInfo.getTokenDistribution());
                nativeQuery.setParameter(p + 13, icoInfo.getFundsAllocation());
                nativeQuery.setParameter(p + 14, icoInfo.getWhitepaperURLOrBusinessPlanURL());
                nativeQuery.setParameter(p + 15, icoInfo.getPlatform());
                nativeQuery.setParameter(p + 16, icoInfo.getHardCap());
                nativeQuery.setParameter(p + 17, icoInfo.getSoftCap());
                nativeQuery.setParameter(p + 18, icoInfo.getRegistrationYear());
                nativeQuery.setParameter(p + 19, icoInfo.getTokenType());
                nativeQuery.setParameter(p + 20, icoInfo.getKyc());
                nativeQuery.setParameter(p + 21, icoInfo.getMvp_prototype());
                nativeQuery.setParameter(p + 22, icoInfo.getRestrictedCountries());
                nativeQuery.setParameter(p + 23, icoInfo.getCurrentCoreTeamMembers());
                nativeQuery.setParameter(p + 24, icoInfo.getLinkedinProfile());
                nativeQuery.setParameter(p + 25, icoInfo.getPriceInPreICO());
                nativeQuery.setParameter(p + 26, icoInfo.getPreICOstart());
                nativeQuery.setParameter(p + 27, icoInfo.getPreICOend());
                nativeQuery.setParameter(p + 28, icoInfo.getBonus());
                nativeQuery.setParameter(p + 29, icoInfo.getMilestone());
                nativeQuery.setParameter(p + 30, icoInfo.getMinimumInvestment());
                nativeQuery.setParameter(p + 31, icoInfo.getDistributedInICO());
                nativeQuery.setParameter(p + 32, icoInfo.getBounty());
                nativeQuery.setParameter(p + 33, icoInfo.getOfficialWebsite());
                p += 34;
            }
            nativeQuery.executeUpdate();
        }
    }

    public void saveICObenchSocial(LinkedList<ICObenchSocialLinks> socialList) {
        if (socialList != null || socialList.size() > 0) {
            StringBuffer sqlStr = new StringBuffer();
            sqlStr.append("INSERT INTO primarymarket_icobench_ico_sociallink(ico_name,github,twitter,telegram) VALUES");
            for (int index = 0; index < socialList.size(); index++) {
                sqlStr.append("(?,?,?,?),");
            }
            sqlStr.deleteCharAt(sqlStr.length() - 1);
            sqlStr.append(" ON DUPLICATE KEY UPDATE github=VALUES(github),twitter=VALUES(twitter),telegram=VALUES(telegram)");
            int p = 1;
            Query nativeQuery = em.createNativeQuery(sqlStr.toString());
            for (ICObenchSocialLinks socialLinks : socialList) {
                nativeQuery.setParameter(p, socialLinks.getName());
                nativeQuery.setParameter(p + 1, socialLinks.getGithub());
                nativeQuery.setParameter(p + 2, socialLinks.getTwitter());
                nativeQuery.setParameter(p + 3, socialLinks.getTelegram());
                p += 4;
            }
            nativeQuery.executeUpdate();
        }
    }

}
