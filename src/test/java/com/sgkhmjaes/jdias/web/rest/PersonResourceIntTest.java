package com.sgkhmjaes.jdias.web.rest;

import com.sgkhmjaes.jdias.JDiasApp;

import com.sgkhmjaes.jdias.domain.Person;
import com.sgkhmjaes.jdias.domain.User;
import com.sgkhmjaes.jdias.repository.PersonRepository;
import com.sgkhmjaes.jdias.repository.UserRepository;
import com.sgkhmjaes.jdias.service.PersonService;
import com.sgkhmjaes.jdias.repository.search.PersonSearchRepository;
import com.sgkhmjaes.jdias.security.SecurityUtils;
import com.sgkhmjaes.jdias.service.UserService;
import com.sgkhmjaes.jdias.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import org.junit.After;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the PersonResource REST controller.
 *
 * @see PersonResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JDiasApp.class)
public class PersonResourceIntTest {

    private static final String DEFAULT_GUID = "AAAAAAAAAA";
    private static final String UPDATED_GUID = "BBBBBBBBBB";

    private static final String DEFAULT_DIASPORA_ID = "AAAAAAAAAA";
    private static final String UPDATED_DIASPORA_ID = "BBBBBBBBBB";

    private static final String DEFAULT_SERIALIZED_PUBLIC_KEY = "AAAAAAAAAA";
    private static final String UPDATED_SERIALIZED_PUBLIC_KEY = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_CREATED_AT = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CREATED_AT = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_UPDATED_AT = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_UPDATED_AT = LocalDate.now(ZoneId.systemDefault());

    private static final Boolean DEFAULT_CLOSED_ACCOUNT = false;
    private static final Boolean UPDATED_CLOSED_ACCOUNT = true;

    private static final Integer DEFAULT_FETCH_STATUS = 1;
    private static final Integer UPDATED_FETCH_STATUS = 2;

    private static final Integer DEFAULT_POD_ID = 1;
    private static final Integer UPDATED_POD_ID = 2;
    
    private static Long userID;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonService personService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PersonSearchRepository personSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restPersonMockMvc;

    //private Person person;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PersonResource personResource = new PersonResource(personService);
        this.restPersonMockMvc = MockMvcBuilders.standaloneSetup(personResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Person createEntity() {
        Person person = new Person()
            .guid(DEFAULT_GUID)
            .diasporaId(DEFAULT_DIASPORA_ID)
            .serializedPublicKey(DEFAULT_SERIALIZED_PUBLIC_KEY)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT)
            .closedAccount(DEFAULT_CLOSED_ACCOUNT)
            .fetchStatus(DEFAULT_FETCH_STATUS)
            .podId(DEFAULT_POD_ID);
        return person;
    }

    @Before
    public void initTest() {
        
        User user = userService.createUser("johndoe", "johndoe", "John", "Doe", "john.doe@localhost", "http://placehold.it/50x50", "en-US");
        user.setActivated(true);
        userRepository.saveAndFlush(user);
        
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken("johndoe", "johndoe"));
        SecurityContextHolder.setContext(securityContext);
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get().getId();
        
        userID = user.getId();
        
        //personSearchRepository.deleteAll();
        //person = createEntity(em);
    }
    
    @After
    public void deleteCreatedAccount(){
        userService.deleteUser("johndoe");
    }
/*
    @Test
    @Transactional
    public void createPerson() throws Exception {
        int databaseSizeBeforeCreate = personRepository.findAll().size();
        Person person = createEntity(); 
        person.setId(userID);
        person = personRepository.saveAndFlush(person);
        personSearchRepository.save(person);
        personRepository.findOne(userID);

        // Create the Person
        restPersonMockMvc.perform(post("/api/people")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(person)))
            .andExpect(status().isCreated());

        // Validate the Person in the database
        List<Person> personList = personRepository.findAll();
        assertThat(personList).hasSize(databaseSizeBeforeCreate + 1);
        Person testPerson = personList.get(personList.size() - 1);
        assertThat(testPerson.getGuid()).isEqualTo(DEFAULT_GUID);
        assertThat(testPerson.getDiasporaId()).isEqualTo(DEFAULT_DIASPORA_ID);
        assertThat(testPerson.getSerializedPublicKey()).isEqualTo(DEFAULT_SERIALIZED_PUBLIC_KEY);
        assertThat(testPerson.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testPerson.getUpdatedAt()).isEqualTo(DEFAULT_UPDATED_AT);
        assertThat(testPerson.isClosedAccount()).isEqualTo(DEFAULT_CLOSED_ACCOUNT);
        assertThat(testPerson.getFetchStatus()).isEqualTo(DEFAULT_FETCH_STATUS);
        assertThat(testPerson.getPodId()).isEqualTo(DEFAULT_POD_ID);

        // Validate the Person in Elasticsearch
        Person personEs = personSearchRepository.findOne(testPerson.getId());
        assertThat(personEs).isEqualToComparingFieldByField(testPerson);
    }
*/
    /*
    @Test
    @Transactional
    public void createPersonWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = personRepository.findAll().size();
        Person person = personRepository.findOne(userID);

        // Create the Person with an existing ID
        person.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPersonMockMvc.perform(post("/api/people")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(person)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Person> personList = personRepository.findAll();
        assertThat(personList).hasSize(databaseSizeBeforeCreate);
    }
*/
    @Test
    @Transactional
    public void getAllPeople() throws Exception {
        // Initialize the database
        //personRepository.saveAndFlush(person);
        Person person = createEntity(); 
        person.setId(userID);
        person = personRepository.saveAndFlush(person);
        personSearchRepository.save(person);
        personRepository.findOne(userID);

        // Get all the personList
        restPersonMockMvc.perform(get("/api/people?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(person.getId().intValue())))
            .andExpect(jsonPath("$.[*].guid").value(hasItem(DEFAULT_GUID.toString())))
            .andExpect(jsonPath("$.[*].diasporaId").value(hasItem(DEFAULT_DIASPORA_ID.toString())))
            .andExpect(jsonPath("$.[*].serializedPublicKey").value(hasItem(DEFAULT_SERIALIZED_PUBLIC_KEY.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())))
            .andExpect(jsonPath("$.[*].closedAccount").value(hasItem(DEFAULT_CLOSED_ACCOUNT.booleanValue())))
            .andExpect(jsonPath("$.[*].fetchStatus").value(hasItem(DEFAULT_FETCH_STATUS)))
            .andExpect(jsonPath("$.[*].podId").value(hasItem(DEFAULT_POD_ID)));
    }

    @Test
    @Transactional
    public void getPerson() throws Exception {
        // Initialize the database
        //personRepository.saveAndFlush(person);
        Person person = createEntity(); 
        person.setId(userID);
        person = personRepository.saveAndFlush(person);
        personSearchRepository.save(person);
        personRepository.findOne(userID);

        // Get the person
        restPersonMockMvc.perform(get("/api/people/{id}", person.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(person.getId().intValue()))
            .andExpect(jsonPath("$.guid").value(DEFAULT_GUID.toString()))
            .andExpect(jsonPath("$.diasporaId").value(DEFAULT_DIASPORA_ID.toString()))
            .andExpect(jsonPath("$.serializedPublicKey").value(DEFAULT_SERIALIZED_PUBLIC_KEY.toString()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()))
            .andExpect(jsonPath("$.closedAccount").value(DEFAULT_CLOSED_ACCOUNT.booleanValue()))
            .andExpect(jsonPath("$.fetchStatus").value(DEFAULT_FETCH_STATUS))
            .andExpect(jsonPath("$.podId").value(DEFAULT_POD_ID));
    }

    @Test
    @Transactional
    public void getNonExistingPerson() throws Exception {
        // Get the person
        restPersonMockMvc.perform(get("/api/people/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePerson() throws Exception {
        
        int databaseSizeBeforeUpdate = personRepository.findAll().size();
                Person person = createEntity(); 
        person.setId(userID);
        person = personRepository.saveAndFlush(person);
        personSearchRepository.save(person);
        //personRepository.findOne(userID);

        // Update the person
        Person updatedPerson = personRepository.findOne(person.getId());
        updatedPerson
            .guid(UPDATED_GUID)
            .diasporaId(UPDATED_DIASPORA_ID)
            .serializedPublicKey(UPDATED_SERIALIZED_PUBLIC_KEY)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .closedAccount(UPDATED_CLOSED_ACCOUNT)
            .fetchStatus(UPDATED_FETCH_STATUS)
            .podId(UPDATED_POD_ID);

        restPersonMockMvc.perform(put("/api/people")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedPerson)))
            .andExpect(status().isOk());

        // Validate the Person in the database
        List<Person> personList = personRepository.findAll();
        assertThat(personList).hasSize(databaseSizeBeforeUpdate);
        Person testPerson = personList.get(personList.size() - 1);
        assertThat(testPerson.getGuid()).isEqualTo(UPDATED_GUID);
        assertThat(testPerson.getDiasporaId()).isEqualTo(UPDATED_DIASPORA_ID);
        assertThat(testPerson.getSerializedPublicKey()).isEqualTo(UPDATED_SERIALIZED_PUBLIC_KEY);
        assertThat(testPerson.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testPerson.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
        assertThat(testPerson.isClosedAccount()).isEqualTo(UPDATED_CLOSED_ACCOUNT);
        assertThat(testPerson.getFetchStatus()).isEqualTo(UPDATED_FETCH_STATUS);
        assertThat(testPerson.getPodId()).isEqualTo(UPDATED_POD_ID);

        // Validate the Person in Elasticsearch
        Person personEs = personSearchRepository.findOne(testPerson.getId());
        assertThat(personEs).isEqualToComparingFieldByField(testPerson);
    }
/*
    @Test
    @Transactional
    public void updateNonExistingPerson() throws Exception {
        int databaseSizeBeforeUpdate = personRepository.findAll().size();
        Person person = personRepository.findOne(userID);
        person.setId(1L);

        // Create the Person

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restPersonMockMvc.perform(put("/api/people")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(person)))
            .andExpect(status().isCreated());

        // Validate the Person in the database
        List<Person> personList = personRepository.findAll();
        assertThat(personList).hasSize(databaseSizeBeforeUpdate + 1);
    }*/
/*
    @Test
    @Transactional
    public void deletePerson() throws Exception {
        
        int databaseSizeBeforeDelete = personRepository.findAll().size();
        Person person = createEntity(); 
        person.setId(userID);
        person = personRepository.saveAndFlush(person);
        personSearchRepository.save(person);
        personRepository.findOne(userID);

        // Get the person
        restPersonMockMvc.perform(delete("/api/people/{id}", person.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean personExistsInEs = personSearchRepository.exists(person.getId());
        assertThat(personExistsInEs).isFalse();

        // Validate the database is empty
        List<Person> personList = personRepository.findAll();
        assertThat(personList).hasSize(databaseSizeBeforeDelete - 1);
    }
*/
    @Test
    @Transactional
    public void searchPerson() throws Exception {
        
        Person person = createEntity(); 
        person.setId(userID);
        person = personRepository.saveAndFlush(person);
        personSearchRepository.save(person);
        personRepository.findOne(userID);
        
        restPersonMockMvc.perform(get("/api/_search/people?query=id:" + person.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(person.getId().intValue())))
            .andExpect(jsonPath("$.[*].guid").value(hasItem(DEFAULT_GUID.toString())))
            .andExpect(jsonPath("$.[*].diasporaId").value(hasItem(DEFAULT_DIASPORA_ID.toString())))
            .andExpect(jsonPath("$.[*].serializedPublicKey").value(hasItem(DEFAULT_SERIALIZED_PUBLIC_KEY.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())))
            .andExpect(jsonPath("$.[*].closedAccount").value(hasItem(DEFAULT_CLOSED_ACCOUNT.booleanValue())))
            .andExpect(jsonPath("$.[*].fetchStatus").value(hasItem(DEFAULT_FETCH_STATUS)))
            .andExpect(jsonPath("$.[*].podId").value(hasItem(DEFAULT_POD_ID)));
    }
    
    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Person.class);
        Person person1 = new Person();
        person1.setId(1L);
        Person person2 = new Person();
        person2.setId(person1.getId());
        assertThat(person1).isEqualTo(person2);
        person2.setId(2L);
        assertThat(person1).isNotEqualTo(person2);
        person1.setId(null);
        assertThat(person1).isNotEqualTo(person2);
    }
}
