package com.sgkhmjaes.jdias.service.impl;

import com.sgkhmjaes.jdias.domain.Person;
import com.sgkhmjaes.jdias.domain.Post;
import com.sgkhmjaes.jdias.service.CommentService;
import com.sgkhmjaes.jdias.domain.Comment;
import com.sgkhmjaes.jdias.repository.CommentRepository;
import com.sgkhmjaes.jdias.repository.search.CommentSearchRepository;
import com.sgkhmjaes.jdias.service.PostService;
import com.sgkhmjaes.jdias.service.UserService;
import com.sgkhmjaes.jdias.service.dto.CommentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing Comment.
 */
@Service
@Transactional
public class CommentServiceImpl implements CommentService{

    private final Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;

    private final CommentSearchRepository commentSearchRepository;
    private final PostService postService;
    private final UserService userService;

    public CommentServiceImpl(CommentRepository commentRepository, CommentSearchRepository commentSearchRepository, PostService postService, UserService userService) {
        this.commentRepository = commentRepository;
        this.commentSearchRepository = commentSearchRepository;
        this.postService = postService;
        this.userService = userService;
    }

    /**
     * Save a comment.
     *
     * @param comment the entity to save
     * @return the persisted entity
     */
    @Override
    public Comment save(Comment comment) {
        log.debug("Request to save Comment : {}", comment);
        Comment result = commentRepository.save(comment);
        commentSearchRepository.save(result);
        return result;
    }

    @Override
    public Comment save(CommentDTO commentDTO) {
        Post post = postService.findOnePost(commentDTO.getPostId());
        Person person = userService.getCurrentPerson();
        Comment comment = save(new Comment(person.getDiasporaId(), UUID.nameUUIDFromBytes(commentDTO.getText().getBytes()).toString(),
            commentDTO.getText(), LocalDate.now(), post, person));
        return comment;
    }

    /**
     *  Get all the comments.
     *
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Comment> findAll() {
        log.debug("Request to get all Comments");
        return commentRepository.findAll();
    }

    /**
     *  Get one comment by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Comment findOne(Long id) {
        log.debug("Request to get Comment : {}", id);
        return commentRepository.findOne(id);
    }

    /**
     *  Delete the  comment by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Comment : {}", id);
        commentRepository.delete(id);
        commentSearchRepository.delete(id);
    }

    /**
     * Search for the comment corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Comment> search(String query) {
        log.debug("Request to search Comments for query {}", query);
        return StreamSupport
            .stream(commentSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }
}
