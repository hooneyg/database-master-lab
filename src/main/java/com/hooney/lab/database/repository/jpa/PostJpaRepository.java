package com.hooney.lab.database.repository.jpa;

import com.hooney.lab.database.domain.post.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostJpaRepository extends JpaRepository<PostEntity, Long> {

}
