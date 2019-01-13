package com.chaindd.dao;

import com.chaindd.entity.ICObenchSocialLinks;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Author: xinyueyan
 * @Date: 1/6/2019 4:47 PM
 */
public interface SocialsDao extends JpaRepository<ICObenchSocialLinks, String> {
}
