package com.sgkhmjaes.jdias.service.impl;

import com.sgkhmjaes.jdias.domain.PollAnswer;
import com.sgkhmjaes.jdias.domain.Post;
import com.sgkhmjaes.jdias.domain.StatusMessage;
import com.sgkhmjaes.jdias.repository.LocationRepository;
import com.sgkhmjaes.jdias.repository.PhotoRepository;
import com.sgkhmjaes.jdias.repository.PostRepository;
import com.sgkhmjaes.jdias.repository.StatusMessageRepository;
import com.sgkhmjaes.jdias.service.dto.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;

@Service
@Transactional
public class PostDTOServiceImpl {

    private final Logger log = LoggerFactory.getLogger(AuthorDTOServiceImpl.class);
    private final PostRepository postRepository;
    private final LocationRepository locationRepository;
    private final AuthorDTOServiceImpl authorDTOServiceImpl;
    private final PhotoRepository photoRepository;
    private final InteractionDTOServiceImpl interactionDTOServiceImpl;
    private final StatusMessageRepository statusMessageRepository;

    public PostDTOServiceImpl(PostRepository postRepository, LocationRepository locationRepository, AuthorDTOServiceImpl authorDTOServiceImpl, PhotoRepository photoRepository, InteractionDTOServiceImpl interactionDTOServiceImpl, StatusMessageRepository statusMessageRepository) {
        this.postRepository = postRepository;
        this.locationRepository = locationRepository;
        this.authorDTOServiceImpl = authorDTOServiceImpl;
        this.photoRepository = photoRepository;
        this.interactionDTOServiceImpl = interactionDTOServiceImpl;
        this.statusMessageRepository = statusMessageRepository;
    }



    public PostDTO findOne(Long id) {
        log.debug("Request to get Post by ID: {}", id);
        return createPostDTOfromPost(postRepository.getOne(id));
    }

    public List<PostDTO> findAll() {
        List<Post> postList = postRepository.findAllByOrderByIdDesc();
        log.debug("Request to get all Posts : {}", postList.size());
        List<PostDTO> postDtoList = new ArrayList<>();
        postList.forEach((post) -> {postDtoList.add(createPostDTOfromPost(post));});
        Collections.sort(postDtoList, (PostDTO p1, PostDTO p2) -> p2.getId().compareTo(p1.getId()));
        return postDtoList;
    }

    private PostDTO createPostDTOfromPost (Post post) {

        AuthorDTO authorDTO = authorDTOServiceImpl.findOne(post.getPerson().getId());
        InteractionDTO interactionDTO = interactionDTOServiceImpl.findOneByPost(post.getId());
        System.out.println("++++++++++" + interactionDTO);
        PostDTO postDTO = new PostDTO();
        StatusMessage statusMessage = statusMessageRepository.findByPosts(post);
        try {
            System.out.println("-----post" + post.getId() + "\n author-----------\n" + authorDTO.getId() +
                    "\n interaction \n" + interactionDTO + "\n stst\n" + statusMessage);
            if(statusMessage.getPoll() != null){
                PollDTO pollDTO = new PollDTO();
                pollDTO.setPostId(post.getId());
                pollDTO.mappingToDTO(statusMessage.getPoll());
                Set<PollAnswerDTO> pollAnswerDTOS = new HashSet<>();
                for(PollAnswer answer: statusMessage.getPoll().getPollanswers()){
                    PollAnswerDTO pollAnswerDTO = new PollAnswerDTO();
                    pollAnswerDTO.setPollId(pollDTO.getId());
                    pollAnswerDTO.mappingToDTO(answer);
                    pollAnswerDTOS.add(pollAnswerDTO);
                }
                pollDTO.setPollAnswerDTOS(pollAnswerDTOS);
                postDTO.mappingToDTO(post, authorDTO, interactionDTO, statusMessage, pollDTO);
                postDTO.setId(post.getId());
            }else {
                postDTO.mappingToDTO(post, authorDTO, interactionDTO, statusMessage);
                postDTO.setId(post.getId());
            }

        } catch (InvocationTargetException ex) {
            java.util.logging.Logger.getLogger(PostDTOServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return postDTO;

    }

}
