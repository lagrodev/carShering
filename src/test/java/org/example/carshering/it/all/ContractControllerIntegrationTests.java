//package org.example.carshering.it.all;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import lombok.extern.slf4j.Slf4j;
//import org.example.carshering.rental.api.dto.request.CreateContractRequest;
//import org.example.carshering.rental.api.dto.request.UpdateContractRequest;
//import org.example.carshering.entity.*;
//import org.example.carshering.it.BaseWebIntegrateTest;
//import org.example.carshering.repository.*;
//import org.example.carshering.util.DataUtils;
//import org.example.carshering.util.WithMockClientDetails;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.ResultActions;
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.List;
//
//import static org.hamcrest.CoreMatchers.is;
//import static org.hamcrest.CoreMatchers.notNullValue;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.doAnswer;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@Slf4j
//@ActiveProfiles("test")
//@AutoConfigureMockMvc
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class ContractControllerIntegrationTests extends BaseWebIntegrateTest {
//
//    private final DataUtils dataUtils = new DataUtils();
//    private final String apiUrl = "/api/contracts";
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @Autowired
//    private ClientRepository clientRepository;
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private ContractRepository contractRepository;
//
//    @Autowired
//    private CarModelRepository carModelRepository;
//
//    @Autowired
//    private BrandRepository brandRepository;
//
//    @Autowired
//    private ModelNameRepository modelNameRepository;
//
//    @Autowired
//    private CarClassRepository carClassRepository;
//
//    @Autowired
//    private CarRepository carRepository;
//
//    @Autowired
//    private CarStateRepository carStateRepository;
//
//    @Autowired
//    private RentalStateRepository rentalStateRepository;
//    @Autowired
//    private DocumentRepository documentRepository;
//    @Autowired
//    private DocumentTypeRepository documentTypeRepository;
//
//    @BeforeEach
//    void resetSequences() {
//        jdbcTemplate.execute("ALTER SEQUENCE car_rental.document_id_seq RESTART WITH 1");
//        jdbcTemplate.execute("ALTER SEQUENCE car_rental.doctype_id_seq RESTART WITH 1");
//        jdbcTemplate.execute("ALTER SEQUENCE car_rental.contract_id_seq RESTART WITH 1");
//        jdbcTemplate.execute("ALTER SEQUENCE car_rental.client_id_seq RESTART WITH 1");
//        jdbcTemplate.execute("ALTER SEQUENCE car_rental.role_id_seq RESTART WITH 1");
//        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_id_seq RESTART WITH 1");
//        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_model_id_model_seq RESTART WITH 1");
//        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_state_id_seq RESTART WITH 1");
//        jdbcTemplate.execute("ALTER SEQUENCE car_rental.brands_id_seq RESTART WITH 1");
//        jdbcTemplate.execute("ALTER SEQUENCE car_rental.models_id_seq RESTART WITH 1");
//        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_classes_id_seq RESTART WITH 1");
//        jdbcTemplate.execute("ALTER SEQUENCE car_rental.rental_state_id_seq RESTART WITH 1");
//    }
//
//    @BeforeEach
//    @Transactional
//    public void setup() {
//        documentRepository.deleteAll();
//        documentTypeRepository.deleteAll();
//        contractRepository.deleteAll();
//        carRepository.deleteAll();
//        carStateRepository.deleteAll();
//        carModelRepository.deleteAll();
//        carClassRepository.deleteAll();
//        modelNameRepository.deleteAll();
//        brandRepository.deleteAll();
//        clientRepository.deleteAll();
//        roleRepository.deleteAll();
//        rentalStateRepository.deleteAll();
//    }
//
//    @BeforeEach
//    void setUp() throws ServletException, IOException {
//        // Настройте мок фильтра, чтобы он просто пропускал запросы
//        doAnswer(invocation -> {
//            FilterChain chain = invocation.getArgument(2);
//            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
//            return null;
//        }).when(jwtRequestFilter).doFilter(any(), any(), any());
//    }
//
//    private List<?> getCarStateAndCarModelAndSaveAllDependencies() {
//        Brand brand = brandRepository
//                .findByNameIgnoreCase(dataUtils.getBrandTransient().getName())
//                .orElseGet(() -> brandRepository.save(dataUtils.getBrandTransient()));
//
//        CarClass carClass = carClassRepository
//                .findByNameIgnoreCase(dataUtils.getCarClassTransient().getName())
//                .orElseGet(() -> carClassRepository.save(dataUtils.getCarClassTransient()));
//
//        Model modelName = modelNameRepository
//                .findByNameIgnoreCase(dataUtils.getModelNameTransient().getName())
//                .orElseGet(() -> modelNameRepository.save(dataUtils.getModelNameTransient()));
//
//        CarModel carModel = carModelRepository.save(
//                dataUtils.getCarModelSEDAN(brand, modelName, carClass)
//        );
//
//        CarStateType carState = carStateRepository
//                .findByStatusIgnoreCase(dataUtils.getCarStateTransient().getStatus())
//                .orElseGet(() -> carStateRepository.save(dataUtils.getCarStateTransient()));
//
//        return List.of(carState, carModel);
//    }
//
//    private List<?> getCarWithSpecificAttributes(
//            String brandStr, String modelNameStr, String carClassStr, String carStateStr, String bodyType) {
//
//        Brand brand = brandRepository
//                .findByNameIgnoreCase(brandStr)
//                .orElseGet(() -> brandRepository.save(dataUtils.getBrandTransient(brandStr)));
//
//        CarClass carClass = carClassRepository
//                .findByNameIgnoreCase(carClassStr)
//                .orElseGet(() -> carClassRepository.save(dataUtils.getCarClassTransient(carClassStr)));
//
//        Model modelName = modelNameRepository
//                .findByNameIgnoreCase(modelNameStr)
//                .orElseGet(() -> modelNameRepository.save(dataUtils.getModelNameTransient(modelNameStr)));
//
//        CarModel carModel = carModelRepository.save(
//                dataUtils.getCarModelBody(brand, modelName, carClass, bodyType)
//        );
//
//        CarStateType carState = carStateRepository
//                .findByStatusIgnoreCase(carStateStr)
//                .orElseGet(() -> carStateRepository.save(dataUtils.getCarStateTransient(carStateStr)));
//
//        return List.of(carState, carModel);
//    }
//
//    private List<?> saveContract(String prefix, Car car, String stateName, LocalDate start, LocalDate end) {
//        RentalState state = rentalStateRepository.findByNameIgnoreCase(stateName)
//                .orElseGet(() -> rentalStateRepository.save(dataUtils.getRentalState(stateName)));
//
//        Client client = clientRepository.findByEmailAndDeletedFalse(prefix + "@mail.ru")
//                .orElseGet(() -> clientRepository.save(dataUtils.createUniqueClient(prefix)));
//
//        Contract contract = dataUtils.createContract(client, car, state, start, end);
//
//        return List.of(contract, client);
//    }
//
//    private HashMap<String, Object> createAndSaveClientWithVerifiedDocument() {
//        Role role = roleRepository.save(Role.builder().name("USER").build());
//
//
//        Client client = clientRepository.save(Client.builder()
//                .firstName("Test User")
//                .lastName("SIN 001")
//                .login("testuser")
//                .email("testuser@mail.ru")
//                .role(role)
//                .phone("+1234567890")
//                .password("password")
//                .build()
//        );
//
//        var dt = documentTypeRepository.save(DocumentType.builder().name("PASSPORT").build());
//
//        Document document = dataUtils.createAndSaveDocument(client, dt, "222222", "XYU");
//
//        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("Client", client);
//        hashMap.put("Role", role);
//        hashMap.put("Document", document);
//        hashMap.put("DocumentType", dt);
//
//        return hashMap;
//    }
//
//    @Test
//    @WithMockClientDetails(username = "testuser")
//    @DisplayName("Test create contract functionality")
//    public void givenContractDto_whenCreateContract_thenSuccessResponse() throws Exception {
//        // given
//        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
//                (CarStateType) getCarStateAndCarModelAndSaveAllDependencies().get(0),
//                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
//        ));
//        HashMap<String, Object> hashMap = createAndSaveClientWithVerifiedDocument();
//        Client client = (Client) hashMap.get("Client");
//
//        Document document = (Document) hashMap.get("Document");
//
//        document.setVerified(true);
//
//        documentRepository.save(document);
//
//        rentalStateRepository.save(dataUtils.getRentalState("PENDING"));
//
//        CreateContractRequest request = new CreateContractRequest(
//                car.getId(),
//                LocalDate.now().plusDays(1),
//                LocalDate.now().plusDays(10)
//        );
//
//        // when
//        ResultActions resultActions = mockMvc.perform(post(apiUrl)
//
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsBytes(request)));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id", notNullValue()))
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.state").value("PENDING"))
//                .andExpect(jsonPath("$.brand").value("TransientBrand"))
//                .andExpect(jsonPath("$.model").value("TransientModelName"));
//    }
//
//    @Test
//    @DisplayName("Test create contract with invalid data functionality")
//    @WithMockClientDetails(username = "testuser")
//    public void givenInvalidContractDto_whenCreateContract_thenValidationErrorResponse() throws Exception {
//        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
//                (CarStateType) getCarStateAndCarModelAndSaveAllDependencies().get(0),
//                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
//        ));
//        HashMap<String, Object> hashMap = createAndSaveClientWithVerifiedDocument();
//        Client client = (Client) hashMap.get("Client");
//
//        Document document = (Document) hashMap.get("Document");
//
//        document.setVerified(true);
//
//        documentRepository.save(document);
//
//        rentalStateRepository.save(dataUtils.getRentalState("PENDING"));
//
//        // given
//        CreateContractRequest invalidRequest = new CreateContractRequest(
//                1L, // carId — null
//                LocalDate.now().minusDays(1), // дата в прошлом
//                LocalDate.now().minusDays(5) // дата в прошлом
//        );
//
//        // when
//        ResultActions resultActions = mockMvc.perform(post(apiUrl)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsBytes(invalidRequest)));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status", is(400)))
//                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
//    }
//
//
//    @Test
//    @DisplayName("Test create contract with invalid start data functionality")
//    @WithMockClientDetails(username = "testuser")
//    public void givenInvalidContractDto_whenCreateContract_thenValidationError() throws Exception {
//        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
//                (CarStateType) getCarStateAndCarModelAndSaveAllDependencies().get(0),
//                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
//        ));
//        HashMap<String, Object> hashMap = createAndSaveClientWithVerifiedDocument();
//        Client client = (Client) hashMap.get("Client");
//
//        Document document = (Document) hashMap.get("Document");
//
//        document.setVerified(true);
//
//        documentRepository.save(document);
//
//        rentalStateRepository.save(dataUtils.getRentalState("PENDING"));
//
//        // given
//        CreateContractRequest invalidRequest = new CreateContractRequest(
//                1L,
//                LocalDate.now().plusDays(3), // дата начала позже даты окончания
//                LocalDate.now().plusDays(1) // дата окончания, норм
//        );
//
//        // when
//        ResultActions resultActions = mockMvc.perform(post(apiUrl)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsBytes(invalidRequest)));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status", is(400)))
//                .andExpect(jsonPath("$.error", is("InvalidContractDateRangeException")));
//    }
//
//    @Test
//    @WithMockClientDetails(username = "testuser")
//    @DisplayName("Test create contract with non-existent car functionality")
//    public void givenContractDtoWithNonExistentCar_whenCreateContract_thenErrorResponse() throws Exception {
//        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
//                (CarStateType) getCarStateAndCarModelAndSaveAllDependencies().get(0),
//                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
//        ));
//        HashMap<String, Object> hashMap = createAndSaveClientWithVerifiedDocument();
//        Client client = (Client) hashMap.get("Client");
//
//        Document document = (Document) hashMap.get("Document");
//
//        document.setVerified(true);
//
//        documentRepository.save(document);
//
//        rentalStateRepository.save(dataUtils.getRentalState("PENDING"));
//        // given
//        CreateContractRequest request = new CreateContractRequest(
//                999L,
//                LocalDate.now().plusDays(1),
//                LocalDate.now().plusDays(10)
//        );
//
//        // when
//        ResultActions resultActions = mockMvc.perform(post(apiUrl)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsBytes(request)));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status", is(404)))
//                .andExpect(jsonPath("$.error", is("CarNotFoundException")))
//                .andExpect(jsonPath("$.message").value("Car not found"));
//    }
//
//    @Test
//    @WithMockClientDetails(username = "testuser")
//    @DisplayName("Test create contract with non-verificate document functionality")
//    public void givenContractDtoWithNonHaveVerificateDocument_whenCreateContract_thenErrorResponse() throws Exception {
//        getCarStateAndCarModelAndSaveAllDependencies();
//        HashMap<String, Object> hashMap = createAndSaveClientWithVerifiedDocument();
//        Client client = (Client) hashMap.get("Client");
//
//        Document document = (Document) hashMap.get("Document");
//
//
//        documentRepository.save(document);
//
//        rentalStateRepository.save(dataUtils.getRentalState("PENDING"));
//        // given
//        CreateContractRequest request = new CreateContractRequest(
//                1L,
//                LocalDate.now().plusDays(1),
//                LocalDate.now().plusDays(10)
//        );
//
//        // when
//        ResultActions resultActions = mockMvc.perform(post(apiUrl)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsBytes(request)));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status", is(400)))
//                .andExpect(jsonPath("$.error", is("UnverifiedClientDocumentException")))
//                .andExpect(jsonPath("$.message").value("The document is not verified. Please wait for verification or attach the relevant document"));
//    }
//
//
//
//
//
//    @Test
//    @DisplayName("Test get all contracts functionality")
//    @WithMockClientDetails(username = "testuser")
//    public void whenGetAllContracts_thenReturnPagedContracts() throws Exception {
//        HashMap<String, Object> hashMap = createAndSaveClientWithVerifiedDocument();
//        Client client = (Client) hashMap.get("Client");
//
//        Document document = (Document) hashMap.get("Document");
//
//        document.setVerified(true);
//        // given
//        List<?> carAttributes1 = getCarWithSpecificAttributes("Toyota", "Camry", "ECONOMY", "AVAILABLE", "SEDAN");
//        List<?> carAttributes2 = getCarWithSpecificAttributes("Honda", "Accord", "ECONOMY", "AVAILABLE", "SEDAN");
//
//        Car carEntity1 = dataUtils.getJohnDoeTransient(
//                (CarStateType) carAttributes1.get(0),
//                (CarModel) carAttributes1.get(1),
//                "A123BC", "VIN123",
//                2022, 100.0
//        );
//        carRepository.save(carEntity1);
//
//        Car carEntity2 = dataUtils.getJohnDoeTransient(
//                (CarStateType) carAttributes2.get(0),
//                (CarModel) carAttributes2.get(1),
//                "B456CD", "VIN456",
//                2021, 150.0
//        );
//        carRepository.save(carEntity2);
//
//        var list1 = saveContract("testuser", carEntity1, "PENDING",
//                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
//        Contract saved1 = contractRepository.save((Contract) list1.get(0));
//
//        var list2 = saveContract("testuser", carEntity2, "CONFIRMED",
//                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
//        Contract saved2 = contractRepository.save((Contract) list2.get(0));
//
//        // when
//        ResultActions resultActions = mockMvc.perform(get(apiUrl)
//                .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.content", notNullValue()))
//                .andExpect(jsonPath("$.content[0].id").value(1L))
//                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
//                .andExpect(jsonPath("$.content[0].state").value("PENDING"))
//                .andExpect(jsonPath("$.content[1].id").value(2L))
//                .andExpect(jsonPath("$.content[1].brand").value("Honda"))
//                .andExpect(jsonPath("$.content[1].state").value("CONFIRMED"));
//    }
//
//    @Test
//    @DisplayName("Test get contract by id functionality")
//    @WithMockClientDetails(username = "testuser")
//    public void givenContractId_whenGetContract_thenSuccessResponse() throws Exception {
//        // given
//        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
//                (CarStateType) getCarStateAndCarModelAndSaveAllDependencies().get(0),
//                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
//        ));
//        HashMap<String, Object> hashMap = createAndSaveClientWithVerifiedDocument();
//        Client client = (Client) hashMap.get("Client");
//
//        Document document = (Document) hashMap.get("Document");
//
//        document.setVerified(true);
//        var list = saveContract("testuser", car, "PENDING",
//                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
//        Contract saved = contractRepository.save((Contract) list.get(0));
//
//        // when
//        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/" + saved.getId())
//                .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", notNullValue()))
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.brand").value("TransientBrand"))
//                .andExpect(jsonPath("$.model").value("TransientModelName"))
//                .andExpect(jsonPath("$.state").value("PENDING"));
//    }
//
//    @Test
//    @DisplayName("Test get contract by incorrect id functionality")
//    @WithMockClientDetails(username = "testuser")
//    public void givenIncorrectContractId_whenGetContract_thenErrorResponse() throws Exception {
//
//        // given
//        HashMap<String, Object> hashMap = createAndSaveClientWithVerifiedDocument();
//        // when
//        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/999")
//                .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status", is(404)))
//                .andExpect(jsonPath("$.error", is("NotFoundException")))
//                .andExpect(jsonPath("$.message").value("Contract not found"));
//    }
//
//    @Test
//    @WithMockClientDetails(username = "testuser")
//    @DisplayName("Test get contract with non-numeric contractId functionality")
//    public void givenNonNumericContractId_whenGetContract_thenBadRequestResponse() throws Exception {
//
//        // given
//
//        // when
//        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/invalid-id")
//                .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status", is(400)))
//                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
//                .andExpect(jsonPath("$.message", is("Invalid value for parameter 'contractId': 'invalid-id'")));
//    }
//
//    @Test
//    @WithMockClientDetails(username = "testuser")
//    @DisplayName("Test cancel contract functionality")
//    public void givenContractId_whenCancelContract_thenNoContentResponse() throws Exception {
//
//        // given
//        HashMap<String, Object> hashMap = createAndSaveClientWithVerifiedDocument();
//        Client client = (Client) hashMap.get("Client");
//
//        Document document = (Document) hashMap.get("Document");
//
//        document.setVerified(true);
//
//        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
//                (CarStateType) getCarStateAndCarModelAndSaveAllDependencies().get(0),
//                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
//        ));
//
//        var list = saveContract("testuser", car, "PENDING",
//                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
//        Contract saved = contractRepository.save((Contract) list.get(0));
//
//        rentalStateRepository.save(dataUtils.getRentalState("CANCELLED"));
//
//        Long contractId = saved.getId();
//
//        rentalStateRepository.save(dataUtils.getRentalState("CANCELLATION_REQUESTED"));
//
//        // when
//        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{contractId}/cancel", contractId)
//                .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isNoContent());
//    }
//
//    @Test
//    @WithMockClientDetails(username = "testuser")
//    @DisplayName("Test cancel contract by incorrect id functionality")
//    public void givenIncorrectContractId_whenCancelContract_thenErrorResponse() throws Exception {
//
//        // given
//        Long contractId = 999L;
//        HashMap<String, Object> hashMap = createAndSaveClientWithVerifiedDocument();
//        Client client = (Client) hashMap.get("Client");
//
//        Document document = (Document) hashMap.get("Document");
//
//        document.setVerified(true);
//        rentalStateRepository.save(dataUtils.getRentalState("CANCELLATION_REQUESTED"));
//
//        // when
//        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{contractId}/cancel", contractId)
//                .contentType(MediaType.APPLICATION_JSON));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status", is(404)))
//                .andExpect(jsonPath("$.error", is("NotFoundException")))
//                .andExpect(jsonPath("$.message").value("Contract not found"));
//    }
//
//    @Test
//    @DisplayName("Test cancel contract by another user functionality")
//    @WithMockClientDetails(username = "testuser", id = 999L)
//    public void givenContractIdOfAnotherUser_whenCancelContract_thenForbiddenResponse() throws Exception {
//
//        // given
//        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
//                (CarStateType) getCarStateAndCarModelAndSaveAllDependencies().get(0),
//                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
//        ));
//
//        var list = saveContract("user1", car, "PENDING",
//                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
//        Contract saved = contractRepository.save((Contract) list.get(0));
//
//        Client anotherClient = clientRepository.save(list.get(1) instanceof Client c ? c : null);
//
//        Long contractId = saved.getId();
//        rentalStateRepository.save(dataUtils.getRentalState("CANCELLATION_REQUESTED"));
//
//        // when
//        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{contractId}/cancel", contractId)
//                .contentType(MediaType.APPLICATION_JSON));
//        Contract contract = (Contract)  list.getFirst();
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isForbidden())
//                .andExpect(jsonPath("$.status", is(403)))
//                .andExpect(jsonPath("$.error", is("UnauthorizedContractAccessException")))
//                .andExpect(jsonPath("$.message").value("You can't terminate someone else's contract"));
//    }
//
//    @Test
//    @WithMockClientDetails(username = "testuser")
//    @DisplayName("Test update contract functionality")
//    public void givenUpdateContractDto_whenUpdateContract_thenSuccessResponse() throws Exception {
//
//        // given
//
//        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
//                (CarStateType) getCarStateAndCarModelAndSaveAllDependencies().get(0),
//                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
//        ));
//        var list = saveContract("user1", car, "PENDING",
//                LocalDate.now().plusDays(15), LocalDate.now().plusDays(20));
//        Contract saved = contractRepository.save((Contract) list.get(0));
//
//        Client anotherClient = clientRepository.save(list.get(1) instanceof Client c ? c : null);
//
//        Long contractId = saved.getId();
//        rentalStateRepository.save(dataUtils.getRentalState("CANCELLATION_REQUESTED"));
//
//
//        UpdateContractRequest request = new UpdateContractRequest(
//                LocalDate.now().plusDays(2),
//                LocalDate.now().plusDays(12)
//        );
//
//        // when
//        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/" + saved.getId())
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsBytes(request)));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id", notNullValue()))
//                .andExpect(jsonPath("$.id").value(1L))
//                .andExpect(jsonPath("$.brand").value("TransientBrand"))
//                .andExpect(jsonPath("$.model").value("TransientModelName"))
//                .andExpect(jsonPath("$.state").value("PENDING"));
//    }
//
//    @Test
//    @WithMockClientDetails(username = "testuser")
//    @DisplayName("Test update contract with invalid data functionality")
//    public void givenInvalidUpdateContractDto_whenUpdateContract_thenValidationErrorResponse() throws Exception {
//
//        // given
//        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
//                (CarStateType) getCarStateAndCarModelAndSaveAllDependencies().get(0),
//                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
//        ));
//        var list = saveContract("user1", car, "PENDING",
//                LocalDate.now().plusDays(15), LocalDate.now().plusDays(20));
//        Contract saved = contractRepository.save((Contract) list.get(0));
//
//        Client anotherClient = clientRepository.save(list.get(1) instanceof Client c ? c : null);
//
//        Long contractId = saved.getId();
//        rentalStateRepository.save(dataUtils.getRentalState("CANCELLATION_REQUESTED"));
//
//        UpdateContractRequest invalidRequest = new UpdateContractRequest(
//                LocalDate.now().minusDays(1), // дата в прошлом
//                null // null дата
//        );
//
//        // when
//        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsBytes(invalidRequest)));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status", is(400)))
//                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
//    }
//
//    @Test
//    @WithMockClientDetails(username = "testuser")
//    @DisplayName("Test update contract with incorrect id functionality")
//    public void givenUpdateContractDtoWithIncorrectId_whenUpdateContract_thenErrorResponse() throws Exception {
//
//        // given
//        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
//                (CarStateType) getCarStateAndCarModelAndSaveAllDependencies().get(0),
//                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
//        ));
//        var list = saveContract("user1", car, "PENDING",
//                LocalDate.now().plusDays(15), LocalDate.now().plusDays(20));
//        Contract saved = contractRepository.save((Contract) list.get(0));
//
//        Client anotherClient = clientRepository.save(list.get(1) instanceof Client c ? c : null);
//
//        Long contractId = saved.getId();
//        rentalStateRepository.save(dataUtils.getRentalState("CANCELLATION_REQUESTED"));
//
//        UpdateContractRequest request = new UpdateContractRequest(
//                LocalDate.now().plusDays(2),
//                LocalDate.now().plusDays(12)
//        );
//
//        // when
//        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/999")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsBytes(request)));
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status", is(404)))
//                .andExpect(jsonPath("$.error", is("NotFoundException")))
//                .andExpect(jsonPath("$.message").value("Contract not found"));
//    }
//
//    @Test
//    @WithMockClientDetails(username = "testuser", id = 999L)
//    @DisplayName("Test update contract by another user functionality")
//    public void givenUpdateContractDtoByAnotherUser_whenUpdateContract_thenForbiddenResponse() throws Exception {
//
//        // given
//
//
//        Car car = carRepository.save(dataUtils.getJohnDoeTransient(
//                (CarStateType) getCarStateAndCarModelAndSaveAllDependencies().get(0),
//                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
//        ));
//
//        var list = saveContract("user1", car, "PENDING",
//                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
//        Contract saved = contractRepository.save((Contract) list.get(0));
//        Client anotherClient = clientRepository.save(list.get(1) instanceof Client c ? c : null);
//
//        Long contractId = saved.getId();
//        rentalStateRepository.save(dataUtils.getRentalState("CANCELLATION_REQUESTED"));
//        UpdateContractRequest request = new UpdateContractRequest(
//                LocalDate.now().plusDays(2),
//                LocalDate.now().plusDays(12)
//        );
//
//        // when
//        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/" + saved.getId())
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsBytes(request))
//        );
//
//        // then
//        resultActions
//                .andDo(MockMvcResultHandlers.print())
//                .andExpect(status().isForbidden())
//                .andExpect(jsonPath("$.status", is(403)))
//                .andExpect(jsonPath("$.error", is("UnauthorizedContractAccessException")))
//                .andExpect(jsonPath("$.message").value("You can't terminate someone else's contract"));
//    }
//}
//
