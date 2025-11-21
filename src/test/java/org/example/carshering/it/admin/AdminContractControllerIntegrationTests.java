package org.example.carshering.it.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.entity.*;
import org.example.carshering.it.BaseWebIntegrateTest;
import org.example.carshering.repository.*;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminContractControllerIntegrationTests extends BaseWebIntegrateTest {

    private final DataUtils dataUtils = new DataUtils();
    private final String apiUrl = "/api/admin/contracts";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ModelNameRepository modelNameRepository;

    @Autowired
    private CarClassRepository carClassRepository;
    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarStateRepository carStateRepository;

    @Autowired
    private RentalStateRepository rentalStateRepository;

    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @BeforeEach
    void resetSequences() {
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.document_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.doctype_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.contract_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.client_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.role_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_model_id_model_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_state_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.brands_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.models_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.car_classes_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE car_rental.rental_state_id_seq RESTART WITH 1");
    }

    @BeforeEach
    @Transactional
    public void setup() {
        documentRepository.deleteAll();
        documentTypeRepository.deleteAll();
        contractRepository.deleteAll();
        carRepository.deleteAll();
        carStateRepository.deleteAll();
        carModelRepository.deleteAll();
        carClassRepository.deleteAll();
        modelNameRepository.deleteAll();
        brandRepository.deleteAll();
        clientRepository.deleteAll();
        roleRepository.deleteAll();
        rentalStateRepository.deleteAll();
    }

    private List<?> getCarStateAndCarModelAndSaveAllDependencies() {
        Brand brand = brandRepository
                .findByNameIgnoreCase(dataUtils.getBrandTransient().getName())
                .orElseGet(() -> brandRepository.save(dataUtils.getBrandTransient()));

        CarClass carClass = carClassRepository
                .findByNameIgnoreCase(dataUtils.getCarClassTransient().getName())
                .orElseGet(() -> carClassRepository.save(dataUtils.getCarClassTransient()));

        Model modelName = modelNameRepository
                .findByNameIgnoreCase(dataUtils.getModelNameTransient().getName())
                .orElseGet(() -> modelNameRepository.save(dataUtils.getModelNameTransient()));

        CarModel carModel = carModelRepository.save(
                dataUtils.getCarModelSEDAN(brand, modelName, carClass)
        );

        CarState carState = carStateRepository
                .findByStatusIgnoreCase(dataUtils.getCarStateTransient().getStatus())
                .orElseGet(() -> carStateRepository.save(dataUtils.getCarStateTransient()));

        return List.of(carState, carModel);
    }

    private List<?> getCarWithSpecificAttributes(
            String brandStr, String modelNameStr, String carClassStr, String carStateStr, String bodyType) {

        Brand brand = brandRepository
                .findByNameIgnoreCase(brandStr)
                .orElseGet(() -> brandRepository.save(dataUtils.getBrandTransient(brandStr)));

        CarClass carClass = carClassRepository
                .findByNameIgnoreCase(carClassStr)
                .orElseGet(() -> carClassRepository.save(dataUtils.getCarClassTransient(carClassStr)));

        Model modelName = modelNameRepository
                .findByNameIgnoreCase(modelNameStr)
                .orElseGet(() -> modelNameRepository.save(dataUtils.getModelNameTransient(modelNameStr)));

        CarModel carModel = carModelRepository.save(
                dataUtils.getCarModelBody(brand, modelName, carClass, bodyType)
        );

        CarState carState = carStateRepository
                .findByStatusIgnoreCase(carStateStr) // или findByNameIgnoreCase(carStateStr), если status хранится в name
                .orElseGet(() -> carStateRepository.save(dataUtils.getCarStateTransient(carStateStr)));

        return List.of(carState, carModel);
    }


    private List<?> saveContract(String prefix, Car car, String stateName, LocalDate start, LocalDate end) {
        RentalState state = rentalStateRepository.findByNameIgnoreCase(stateName)
                .orElseGet(() -> rentalStateRepository.save(dataUtils.getRentalState(stateName)));


        Client client = clientRepository.findByEmailAndDeletedFalse(prefix + "_mail@example.com")
                .orElseGet(() -> clientRepository.save(dataUtils.createUniqueClient(prefix)));

        Contract contract = dataUtils.createContract(client, car, state, start, end);

        return List.of(contract, client);

    }

    @Test
    @DisplayName("Test confirm contract functionality")
    public void givenContractId_whenConfirmContract_thenSuccessResponse() throws Exception {
        Car car = carRepository.save(dataUtils.getJohnDoeTransient(

                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        // when
        var list = saveContract("save", car, "PENDING",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));


        Client client = (Client) list.get(1);
        Contract saved = contractRepository.save((Contract) list.get(0));
        rentalStateRepository.save(RentalState.builder().name("CONFIRMED").build());


        // given
        Long contractId = saved.getId();
        ContractResponse contractResponse = DataUtils.contractResponseConfirmed();


        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/{contractId}/confirm", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.state").value("CONFIRMED"))
                .andExpect(jsonPath("$.brand").value("TransientBrand"))
                .andExpect(jsonPath("$.model").value("TransientModelName"));
    }

    @Test
    @DisplayName("Test confirm contract with incorrect id functionality")
    public void givenIncorrectContractId_whenConfirmContract_thenErrorResponse() throws Exception {

        // given
        Long contractId = 999L;

        Car car = carRepository.save(dataUtils.getJohnDoeTransient(

                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        // when
        var list = saveContract("save", car, "PENDING",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));


        Client client = (Client) list.get(1);
        Contract saved = contractRepository.save((Contract) list.get(0));
        rentalStateRepository.save(RentalState.builder().name("CONFIRMED").build());


        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/{contractId}/confirm", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Contract not found"));
    }

    @Test
    @DisplayName("Test confirm already confirmed contract functionality")
    public void givenAlreadyConfirmedContract_whenConfirmContract_thenErrorResponse() throws Exception {

        // given


        Car car = carRepository.save(dataUtils.getJohnDoeTransient(

                (CarState) getCarStateAndCarModelAndSaveAllDependencies().get(0),
                (CarModel) getCarStateAndCarModelAndSaveAllDependencies().get(1)
        ));

        // when
        var list = saveContract("save", car, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));


        Client client = (Client) list.get(1);
        Contract saved = contractRepository.save((Contract) list.get(0));

        Long contractId = saved.getId();

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/{contractId}/confirm", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("InvalidContractStateException")))
                .andExpect(jsonPath("$.message").value("Status expected PENDINGbut current: CONFIRMED"));
    }


    @Test
    @DisplayName("Test get all contracts without filters functionality")
    public void whenGetAllContractsWithoutFilters_thenReturnPagedContracts() throws Exception {

        // given
        ContractResponse contract1 = DataUtils.contractResponsePersisted();
        ContractResponse contract2 = DataUtils.contractResponseCancelled();

        List<?> carAttributes1 = getCarWithSpecificAttributes("Tesla", "Model S", "PREMIUM", "AVAILABLE", "SEDAN");
        List<?> carAttributes2 = getCarWithSpecificAttributes("BMW", "X5", "PREMIUM", "BOOKED", "SUV");

        Car carEntity1 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes1.get(0),
                (CarModel) carAttributes1.get(1),
                "112123", "332131",
                2022, 100.0
        );
        carRepository.save(carEntity1);


        Car carEntity2 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes2.get(0),
                (CarModel) carAttributes2.get(1),
                "11223", "33231",
                2021, 150.0
        );


        carRepository.save(carEntity2);


        // when
        var list1 = saveContract("save", carEntity1, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));


        Client client = (Client) list1.get(1);
        Contract saved = contractRepository.save((Contract) list1.get(0));

        var list2 = saveContract("save", carEntity2, "CANCELLED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));


        Client client2 = (Client) list2.get(1);
        Contract saved2 = contractRepository.save((Contract) list2.get(0));


        Page<ContractResponse> page = new PageImpl<>(List.of(contract1, contract2));


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].brand").value("Tesla"))
                .andExpect(jsonPath("$.content[0].model").value("Model S"))
                .andExpect(jsonPath("$.content[0].state").value("CONFIRMED"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].brand").value("BMW"))
                .andExpect(jsonPath("$.content[1].state").value("CANCELLED"));
    }

    @Test
    @DisplayName("Test get all contracts with status filter functionality")
    public void givenStatusFilter_whenGetContracts_thenReturnFilteredContracts() throws Exception {

        // given
        ContractResponse contract = DataUtils.contractResponseConfirmed();
        Page<ContractResponse> page = new PageImpl<>(List.of(contract));
        ContractResponse contract1 = DataUtils.contractResponsePersisted();
        ContractResponse contract2 = DataUtils.contractResponseCancelled();

        List<?> carAttributes1 = getCarWithSpecificAttributes("Tesla", "Model S", "PREMIUM", "AVAILABLE", "SEDAN");
        List<?> carAttributes2 = getCarWithSpecificAttributes("BMW", "X5", "PREMIUM", "BOOKED", "SUV");

        Car carEntity1 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes1.get(0),
                (CarModel) carAttributes1.get(1),
                "112123", "332131",
                2022, 100.0
        );
        carRepository.save(carEntity1);


        Car carEntity2 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes2.get(0),
                (CarModel) carAttributes2.get(1),
                "11223", "33231",
                2021, 150.0
        );


        carRepository.save(carEntity2);


        // when
        var list1 = saveContract("save", carEntity1, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));


        Client client = (Client) list1.get(1);
        Contract saved = contractRepository.save((Contract) list1.get(0));

        var list2 = saveContract("save", carEntity2, "CANCELLED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));


        Client client2 = (Client) list2.get(1);
        Contract saved2 = contractRepository.save((Contract) list2.get(0));


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("status", "CONFIRMED")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].state").value("CONFIRMED"))
                .andExpect(jsonPath("$.page.totalElements").value(1))

        ;
    }

    @Test
    @DisplayName("Test get all contracts with user id filter functionality")
    public void givenUserIdFilter_whenGetContracts_thenReturnFilteredContracts() throws Exception {

        // given

        ContractResponse contract1 = DataUtils.contractResponsePersisted();
        ContractResponse contract2 = DataUtils.contractResponseCancelled();

        List<?> carAttributes1 = getCarWithSpecificAttributes("Tesla", "Model S", "PREMIUM", "AVAILABLE", "SEDAN");

        Car carEntity1 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes1.get(0),
                (CarModel) carAttributes1.get(1),
                "112123", "332131",
                2022, 100.0
        );
        carRepository.save(carEntity1);

        List<?> carAttributes2 = getCarWithSpecificAttributes("BMW", "X5", "PREMIUM", "BOOKED", "SUV");

        Car carEntity2 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes2.get(0),
                (CarModel) carAttributes2.get(1),
                "11223", "33231",
                2021, 150.0
        );


        carRepository.save(carEntity2);


        // when
        var list1 = saveContract("save", carEntity1, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));


        Client client = (Client) list1.get(1);
        Contract saved = contractRepository.save((Contract) list1.get(0));

        var list2 = saveContract("save", carEntity2, "CANCELLED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));


        Client client2 = (Client) list2.get(1);
        Contract saved2 = contractRepository.save((Contract) list2.get(0));

        var list3 = saveContract("ivan", carEntity2, "CANCELLED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));


        Client client3 = (Client) list2.get(1);
        Contract saved3 = contractRepository.save((Contract) list2.get(0));


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("idUser", "1")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].lastName").value("Last"));
    }

    @Test
    @DisplayName("Test get all contracts with car id filter functionality")
    public void givenCarIdFilter_whenGetContracts_thenReturnFilteredContracts() throws Exception {

        // given

        List<?> carAttributes1 = getCarWithSpecificAttributes("Tesla", "Model S", "PREMIUM", "AVAILABLE", "SEDAN");

        Car carEntity1 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes1.get(0),
                (CarModel) carAttributes1.get(1),
                "VIN123456", "332131",
                2022, 100.0
        );
        carRepository.save(carEntity1);

        var list1 = saveContract("save", carEntity1, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));


        Client client = (Client) list1.get(1);
        Contract saved = contractRepository.save((Contract) list1.get(0));


        var list3 = saveContract("save", carEntity1, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        Client client3 = (Client) list3.get(1);
        Contract saved3 = contractRepository.save((Contract) list3.get(0));
        List<?> carAttributes2 = getCarWithSpecificAttributes("BMW", "X5", "PREMIUM", "BOOKED", "SUV");

        Car carEntity2 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes2.get(0),
                (CarModel) carAttributes2.get(1),
                "11223", "33231",
                2021, 150.0
        );


        carRepository.save(carEntity2);
        var list2 = saveContract("save", carEntity2, "CANCELLED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));


        Client client2 = (Client) list2.get(1);
        Contract saved2 = contractRepository.save((Contract) list2.get(0));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("idCar", "1")
                .contentType(MediaType.APPLICATION_JSON));


        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].vin").value("VIN123456"))

                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    @DisplayName("Test get all contracts with multiple filters functionality")
    public void givenMultipleFilters_whenGetContracts_thenReturnFilteredContracts() throws Exception {

        // given - создаем 10 контрактов с различными параметрами
        // Два контракта будут соответствовать всем фильтрам: status=CONFIRMED, brand=Toyota, body_type=SEDAN, car_class=ECONOMY, model=Camry

        // Контракт 1 - соответствует всем фильтрам
        List<?> carAttributes1 = getCarWithSpecificAttributes("Toyota", "Camry", "ECONOMY", "AVAILABLE", "SEDAN");
        Car carEntity1 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes1.get(0),
                (CarModel) carAttributes1.get(1),
                "VIN001", "A001BC",
                2020, 100.0
        );
        carRepository.save(carEntity1);
        var list1 = saveContract("client1", carEntity1, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        contractRepository.save((Contract) list1.get(0));

        // Контракт 2 - соответствует всем фильтрам
        List<?> carAttributes2 = getCarWithSpecificAttributes("Toyota", "Camry", "ECONOMY", "BOOKED", "SEDAN");
        Car carEntity2 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes2.get(0),
                (CarModel) carAttributes2.get(1),
                "VIN002", "A002BC",
                2021, 110.0
        );
        carRepository.save(carEntity2);
        var list2 = saveContract("client2", carEntity2, "CONFIRMED",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(6));
        contractRepository.save((Contract) list2.get(0));

        // Контракт 3 - не соответствует: другой brand
        List<?> carAttributes3 = getCarWithSpecificAttributes("BMW", "Camry", "ECONOMY", "AVAILABLE", "SEDAN");
        Car carEntity3 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes3.get(0),
                (CarModel) carAttributes3.get(1),
                "VIN003", "A003BC",
                2020, 120.0
        );
        carRepository.save(carEntity3);
        var list3 = saveContract("client3", carEntity3, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        contractRepository.save((Contract) list3.get(0));

        // Контракт 4 - не соответствует: другой status
        List<?> carAttributes4 = getCarWithSpecificAttributes("Toyota", "Camry", "ECONOMY", "AVAILABLE", "SEDAN");
        Car carEntity4 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes4.get(0),
                (CarModel) carAttributes4.get(1),
                "VIN004", "A004BC",
                2019, 95.0
        );
        carRepository.save(carEntity4);
        var list4 = saveContract("client4", carEntity4, "PENDING",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        contractRepository.save((Contract) list4.get(0));

        // Контракт 5 - не соответствует: другой body_type
        List<?> carAttributes5 = getCarWithSpecificAttributes("Toyota", "Camry", "ECONOMY", "AVAILABLE", "SUV");
        Car carEntity5 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes5.get(0),
                (CarModel) carAttributes5.get(1),
                "VIN005", "A005BC",
                2022, 130.0
        );
        carRepository.save(carEntity5);
        var list5 = saveContract("client5", carEntity5, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        contractRepository.save((Contract) list5.get(0));

        // Контракт 6 - не соответствует: другой car_class
        List<?> carAttributes6 = getCarWithSpecificAttributes("Toyota", "Camry", "PREMIUM", "AVAILABLE", "SEDAN");
        Car carEntity6 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes6.get(0),
                (CarModel) carAttributes6.get(1),
                "VIN006", "A006BC",
                2020, 150.0
        );
        carRepository.save(carEntity6);
        var list6 = saveContract("client6", carEntity6, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        contractRepository.save((Contract) list6.get(0));

        // Контракт 7 - не соответствует: другая model
        List<?> carAttributes7 = getCarWithSpecificAttributes("Toyota", "Corolla", "ECONOMY", "AVAILABLE", "SEDAN");
        Car carEntity7 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes7.get(0),
                (CarModel) carAttributes7.get(1),
                "VIN007", "A007BC",
                2020, 90.0
        );
        carRepository.save(carEntity7);
        var list7 = saveContract("client7", carEntity7, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        contractRepository.save((Contract) list7.get(0));

        // Контракт 8 - не соответствует: CANCELLED status
        List<?> carAttributes8 = getCarWithSpecificAttributes("Toyota", "Camry", "ECONOMY", "AVAILABLE", "SEDAN");
        Car carEntity8 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes8.get(0),
                (CarModel) carAttributes8.get(1),
                "VIN008", "A008BC",
                2020, 100.0
        );
        carRepository.save(carEntity8);
        var list8 = saveContract("client8", carEntity8, "CANCELLED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        contractRepository.save((Contract) list8.get(0));

        // Контракт 9 - не соответствует: несколько параметров (brand и body_type)
        List<?> carAttributes9 = getCarWithSpecificAttributes("Honda", "Accord", "ECONOMY", "AVAILABLE", "HATCHBACK");
        Car carEntity9 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes9.get(0),
                (CarModel) carAttributes9.get(1),
                "VIN009", "A009BC",
                2021, 105.0
        );
        carRepository.save(carEntity9);
        var list9 = saveContract("client9", carEntity9, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        contractRepository.save((Contract) list9.get(0));

        // Контракт 10 - не соответствует: несколько параметров (status и class)
        List<?> carAttributes10 = getCarWithSpecificAttributes("Toyota", "Camry", "LUXURY", "AVAILABLE", "SEDAN");
        Car carEntity10 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes10.get(0),
                (CarModel) carAttributes10.get(1),
                "VIN010", "A010BC",
                2023, 200.0
        );
        carRepository.save(carEntity10);
        var list10 = saveContract("client10", carEntity10, "PENDING",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        contractRepository.save((Contract) list10.get(0));

        // when - применяем 5 фильтров
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("status", "CONFIRMED")
                .param("brand", "Toyota")
                .param("body_type", "SEDAN")
                .param("car_class", "ECONOMY")
                .param("model", "Camry")
                .contentType(MediaType.APPLICATION_JSON));

        // then - ожидаем только 2 контракта, соответствующие всем фильтрам
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.page.totalElements").value(3)) // todo сделать фильтр по модели, его пока нет
                .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[0].model").value("Camry"))
                .andExpect(jsonPath("$.content[0].bodyType").value("SEDAN"))
                .andExpect(jsonPath("$.content[0].carClass").value("ECONOMY"))
                .andExpect(jsonPath("$.content[0].state").value("CONFIRMED"))
                .andExpect(jsonPath("$.content[1].brand").value("Toyota"))
                .andExpect(jsonPath("$.content[1].model").value("Camry"))
                .andExpect(jsonPath("$.content[1].bodyType").value("SEDAN"))
                .andExpect(jsonPath("$.content[1].carClass").value("ECONOMY"))
                .andExpect(jsonPath("$.content[1].state").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Test get contract by id functionality")
    public void givenContractId_whenGetById_thenSuccessResponse() throws Exception {

        // given
        Long contractId = 1L;
        List<?> carAttributes1 = getCarWithSpecificAttributes("Toyota", "Camry", "ECONOMY", "AVAILABLE", "SEDAN");
        Car carEntity1 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes1.get(0),
                (CarModel) carAttributes1.get(1),
                "VIN123456", "A001BC",
                2020, 100.0
        );
        carRepository.save(carEntity1);
        var list1 = saveContract("client1", carEntity1, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        contractRepository.save((Contract) list1.get(0));

        ContractResponse contractResponse = DataUtils.contractResponsePersisted();


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/{contractId}", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.totalCost").value(100.0))
                .andExpect(jsonPath("$.vin").value("VIN123456"));
    }

    //
    @Test
    @DisplayName("Test get contract by incorrect id functionality")
    public void givenIncorrectId_whenGetById_thenErrorResponse() throws Exception {

        // given
        Long contractId = 999L;


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/{contractId}", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Contract not found"));
    }

    @Test
    @DisplayName("Test get contract with non-numeric contractId functionality")
    public void givenNonNumericContractId_whenGetContract_thenBadRequestResponse() throws Exception {

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl + "/invalid-id")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.message", is("Invalid value for parameter 'contractId': 'invalid-id'")));
    }

    @Test
    @DisplayName("Test cancel contract by admin functionality")
    public void givenContractId_whenCancelContract_thenSuccessResponse() throws Exception {

        // given

        List<?> carAttributes1 = getCarWithSpecificAttributes("Toyota", "Camry", "ECONOMY", "AVAILABLE", "SEDAN");
        Car carEntity1 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes1.get(0),
                (CarModel) carAttributes1.get(1),
                "VIN123456", "A001BC",
                2020, 100.0
        );
        carRepository.save(carEntity1);
        var list1 = saveContract("client1", carEntity1, "PENDING",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        Contract contract = contractRepository.save((Contract) list1.get(0));
        rentalStateRepository.save(RentalState.builder().name("CANCELLED").build());

        Long contractId = contract.getId();
        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{contractId}/cancel", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        Contract updated = contractRepository.findById(contract.getId()).orElse(null);

        // then
        Assertions.assertNotNull(updated);
        assertEquals("CANCELLED", updated.getState().getName());


        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test cancel contract by admin with incorrect id functionality")
    public void givenIncorrectContractId_whenCancelContract_thenErrorResponse() throws Exception {

        // given
        Long contractId = 999L;


        // when
        ResultActions resultActions = mockMvc.perform(delete(apiUrl + "/{contractId}/cancel", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Contract not found"));
    }

    @Test
    @DisplayName("Test confirm cancellation by admin functionality")
    public void givenContractId_whenConfirmCancellation_thenSuccessResponse() throws Exception {

        // given

        List<?> carAttributes1 = getCarWithSpecificAttributes("Toyota", "Camry", "ECONOMY", "AVAILABLE", "SEDAN");
        Car carEntity1 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes1.get(0),
                (CarModel) carAttributes1.get(1),
                "VIN123456", "A001BC",
                2020, 100.0
        );
        carRepository.save(carEntity1);
        var list1 = saveContract("client1", carEntity1, "CANCELLATION_REQUESTED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        Contract contract = contractRepository.save((Contract) list1.get(0));
        rentalStateRepository.save(RentalState.builder().name("CANCELLED").build());

        Long contractId = contract.getId();
        // when



        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/contracts/{id}/confirm-cancellation", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        Contract updated = contractRepository.findById(contract.getId()).orElse(null);

        // then
        Assertions.assertNotNull(updated);
        assertEquals("CANCELLED", updated.getState().getName());

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());
    }
//
    @Test
    @DisplayName("Test confirm cancellation with incorrect id functionality")
    public void givenIncorrectContractId_whenConfirmCancellation_thenErrorResponse() throws Exception {

        // given
        Long contractId = 999L;


        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/contracts/{id}/confirm-cancellation", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        // then

        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("NotFoundException")))
                .andExpect(jsonPath("$.message").value("Contract not found"));
    }

    @Test
    @DisplayName("Test confirm cancellation with invalid state functionality")
    public void givenInvalidContractState_whenConfirmCancellation_thenErrorResponse() throws Exception {


        List<?> carAttributes1 = getCarWithSpecificAttributes("Toyota", "Camry", "ECONOMY", "AVAILABLE", "SEDAN");
        Car carEntity1 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes1.get(0),
                (CarModel) carAttributes1.get(1),
                "VIN123456", "A001BC",
                2020, 100.0
        );
        carRepository.save(carEntity1);
        var list1 = saveContract("client1", carEntity1, "PAY",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));

        Contract contract = contractRepository.save((Contract) list1.get(0));
        rentalStateRepository.save(RentalState.builder().name("CANCELLATION_REQUESTED").build());

        Long contractId = contract.getId();

        // when
        ResultActions resultActions = mockMvc.perform(patch(apiUrl + "/contracts/{id}/confirm-cancellation", contractId)
                .contentType(MediaType.APPLICATION_JSON));

        Contract updated = contractRepository.findById(contract.getId()).orElse(null);

        // then
        Assertions.assertNotNull(updated);
        assertEquals("PAY", updated.getState().getName());
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("InvalidContractCancellationStateException")))
                .andExpect(jsonPath("$.message").value("The contract is not at the cancellation request stage"));
    }

    @Test
    @DisplayName("Test get all contracts with pagination functionality")
    public void givenPageableParams_whenGetAllContracts_thenReturnPagedResponse() throws Exception {

        // given
        ContractResponse contract1 = DataUtils.contractResponsePersisted();
        ContractResponse contract2 = DataUtils.contractResponseCancelled();

        Page<ContractResponse> page = new PageImpl<>(
                List.of(contract1, contract2),
                org.springframework.data.domain.PageRequest.of(0, 20),
                2
        );
        List<?> carAttributes1 = getCarWithSpecificAttributes("Toyota", "Camry", "ECONOMY", "AVAILABLE", "SEDAN");
        Car carEntity1 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes1.get(0),
                (CarModel) carAttributes1.get(1),
                "VIN001", "A001BC",
                2020, 100.0
        );
        carRepository.save(carEntity1);
        var list1 = saveContract("client1", carEntity1, "CONFIRMED",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
        contractRepository.save((Contract) list1.get(0));

        // Контракт 2 - соответствует всем фильтрам
        List<?> carAttributes2 = getCarWithSpecificAttributes("Toyota", "Camry", "ECONOMY", "BOOKED", "SEDAN");
        Car carEntity2 = dataUtils.getJohnDoeTransient(
                (CarState) carAttributes2.get(0),
                (CarModel) carAttributes2.get(1),
                "VIN002", "A002BC",
                2021, 110.0
        );
        carRepository.save(carEntity2);
        var list2 = saveContract("client2", carEntity2, "CONFIRMED",
                LocalDate.now().plusDays(2), LocalDate.now().plusDays(6));
        contractRepository.save((Contract) list2.get(0));

        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    @Test
    @DisplayName("Test get all contracts returns empty page functionality")
    public void whenGetAllContractsWithNoData_thenReturnEmptyPage() throws Exception {

        // given
        Page<ContractResponse> emptyPage = new PageImpl<>(List.of());


        // when
        ResultActions resultActions = mockMvc.perform(get(apiUrl)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }
}

