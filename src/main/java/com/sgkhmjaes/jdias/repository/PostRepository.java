package com.sgkhmjaes.jdias.repository;

import com.sgkhmjaes.jdias.domain.Post;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the Post entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    List<Post> findAllByOrderByIdDesc();

}
