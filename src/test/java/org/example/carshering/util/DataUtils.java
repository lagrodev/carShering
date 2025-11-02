package org.example.carshering.util;

import org.example.carshering.entity.*;
import org.example.carshering.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;

import java.time.LocalDate;

@TestConfiguration
public class DataUtils {

    private final ClientRepository clientRepository;


    private final CarRepository carRepository;


    private final RentalStateRepository rentalStateRepository;


    private final ContractRepository contractRepository;
    private final CarStateRepository carStateRepository;
    private final CarModelRepository carModelRepository;
    private final CarClassRepository carClassRepository;
    private final BrandRepository brandRepository;
    private final ModelNameRepository modelNameRepository;

    @Autowired
    public DataUtils(ClientRepository clientRepository, CarRepository carRepository, RentalStateRepository rentalStateRepository, ContractRepository contractRepository, CarStateRepository carStateRepository,
                     CarModelRepository carModelRepository,
                     CarClassRepository carClassRepository,
                     BrandRepository brandRepository,
                     ModelNameRepository modelNameRepository) {
        this.clientRepository = clientRepository;
        this.carRepository = carRepository;
        this.rentalStateRepository = rentalStateRepository;
        this.contractRepository = contractRepository;
        this.carStateRepository = carStateRepository;
        this.carModelRepository = carModelRepository;
        this.carClassRepository = carClassRepository;
        this.brandRepository = brandRepository;
        this.modelNameRepository = modelNameRepository;
    }

    private Model createAndSaveModel() {
        return modelNameRepository.findByNameIgnoreCase("nameModel")
                .orElseGet(() -> modelNameRepository.save(Model.builder().name("nameModel").build()));
    }

    private Brand createAndSaveBrand() {
        return brandRepository.findByNameIgnoreCase("brand")
                .orElseGet(() -> brandRepository.save(Brand.builder().name("brand").build()));
    }

    private CarClass createAndSaveCarClass() {
        return carClassRepository.findByNameIgnoreCase("carClass")
                .orElseGet(() -> carClassRepository.save(CarClass.builder().name("carClass").build()));
    }

    private CarState createAndSaveCarState() {
        return carStateRepository.findByStatusIgnoreCase("NEWSTATE")
                .orElseGet(() -> carStateRepository.save(CarState.builder().status("NEWSTATE").build()));
    }

    public CarModel createAndSaveCarModel() {
        return carModelRepository.save(CarModel.builder()
                .brand(createAndSaveBrand())
                .model(createAndSaveModel())
                .bodyType("SEDAN")
                .carClass(createAndSaveCarClass())
                .deleted(false)
                .build());
    }


    public Car getJohnDoeTransient() {
        CarState state = createAndSaveCarState();
        CarModel model = createAndSaveCarModel();

        return Car.builder()
                .state(state)
                .model(model)
                .yearOfIssue(2020)
                .rent(10.)
                .vin("JOHNDOE")
                .gosNumber("1123")
                .build();
    }

    public Car getFrankJonesTransient() {
        CarState state = createAndSaveCarState();
        CarModel model = createAndSaveCarModel();

        return Car.builder()
                .state(state)
                .model(model)
                .yearOfIssue(2020)
                .rent(10.)
                .vin("FRANKJONES")
                .gosNumber("1124")
                .build();
    }

    public Car getMikeSmithTransient() {
        CarState state = createAndSaveCarState();
        CarModel model = createAndSaveCarModel();

        return Car.builder()
                .state(state)
                .model(model)
                .yearOfIssue(2020)
                .rent(10.)
                .vin("MIKESMITH")
                .gosNumber("1125")
                .build();
    }

    public Car getMikeSmithPersisted() {
        CarState state = createAndSaveCarState();
        CarModel model = createAndSaveCarModel();

        return Car.builder()
                .id(1L)
                .state(state)
                .model(model)
                .yearOfIssue(2020)
                .rent(10.)
                .vin("MIKESMITH")
                .gosNumber("1123")
                .build();
    }

    public Car getJohnDoePersisted() {
        CarState state = createAndSaveCarState();
        CarModel model = createAndSaveCarModel();

        return Car.builder()
                .id(1L)
                .state(state)
                .model(model)
                .yearOfIssue(2020)
                .rent(10.)
                .vin("JOHNDOE")
                .gosNumber("1124")
                .build();
    }

    public Car getFrankJonesPersisted() {
        CarState state = createAndSaveCarState();
        CarModel model = createAndSaveCarModel();

        return Car.builder()
                .id(1L)
                .state(state)
                .model(model)
                .yearOfIssue(2020)
                .rent(10.)
                .vin("FRANKJONES")
                .gosNumber("1125")
                .build();
    }



    // <!-- Add more utility methods as needed --!>
    // <!-- new methods --!>
    private CarModel createAndSaveCarModel(String brandName, String modelName, String bodyType, String carClassName) {
        Brand brand = brandRepository.findByNameIgnoreCase(brandName)
                .orElseGet(() -> brandRepository.save(Brand.builder().name(brandName).build()));
        Model model = modelNameRepository.findByNameIgnoreCase(modelName)
                .orElseGet(() -> modelNameRepository.save(Model.builder().name(modelName).build()));
        CarClass carClass = carClassRepository.findByNameIgnoreCase(carClassName)
                .orElseGet(() -> carClassRepository.save(CarClass.builder().name(carClassName).build()));

        return carModelRepository.save(CarModel.builder()
                .brand(brand)
                .model(model)
                .bodyType(bodyType)
                .carClass(carClass)
                .deleted(false)
                .build());
    }

    private CarState createAndSaveCarState(String status) {
        return carStateRepository.findByStatusIgnoreCase(status)
                .orElseGet(() -> carStateRepository.save(CarState.builder().status(status).build()));
    }

    public Car getCarWithSpecificAttributes(String vin, String gosNumber, int year, String brand, String model,
                                            String bodyType, String carClass, String state) {
        CarState carState = createAndSaveCarState(state);
        CarModel carModel = createAndSaveCarModel(brand, model, bodyType, carClass);

        return Car.builder()
                .state(carState)
                .model(carModel)
                .yearOfIssue(year)
                .rent(15.0)
                .vin(vin)
                .gosNumber(gosNumber)
                .build();
    }

    public Car getOldCarTransient() {
        CarState state = createAndSaveCarState();
        CarModel model = createAndSaveCarModel();
        model.setBodyType("HATCHBACK");
        carModelRepository.save(model);

        return Car.builder()
                .state(state)
                .model(model)
                .yearOfIssue(1999)
                .rent(5.0)
                .vin("OLDCARVIN")
                .gosNumber("1999")
                .build();
    }

    public Car getFutureCarTransient() {
        CarState state = createAndSaveCarState();
        CarModel model = createAndSaveCarModel();
        model.setBodyType("COUPE");
        carModelRepository.save(model);

        return Car.builder()
                .state(state)
                .model(model)
                .yearOfIssue(2030)
                .rent(50.0)
                .vin("FUTURECARVIN")
                .gosNumber("2030")
                .build();
    }

    // <!-- brand methods --!>
    public Brand getBrandTransient() {
        return Brand.builder()
                .name("TransientBrand")
                .build();
    }

    public Brand getBrandPersisted() {
        return Brand.builder()
                .id(1L)
                .name("PersistedBrand")
                .build();
    }


    public CarClass getCarClassTransient() {
        return CarClass.builder()
                .name("TransientCarClass")
                .build();
    }

    public CarClass getCarClassPersisted() {
        return CarClass.builder()
                .id(1L)
                .name("PersistedCarClass")
                .build();
    }

    // <!-- car state methods --!>

    public CarState getCarStateTransient() {
        return CarState.builder()
                .status("Available")
                .build();
    }

    public CarState getCarStatePersisted() {
        return CarState.builder()
                .id(1L)
                .status("Available")
                .build();
    }



    // <!-- model name methods --!>
    public Model getModelNameTransient() {
        return Model.builder()
                .name("TransientModelName")
                .build();
    }
    public Model getModelNamePersisted() {
        return Model.builder()
                .id(1L)
                .name("PersistedModelName")
                .build();
    }

    // <!-- document type methods -->
    public DocumentType getDocumentTypeTransient() {
        return DocumentType.builder()
                .name("TransientDocType")
                .build();
    }

    public DocumentType getDocumentTypePersisted() {
        return DocumentType.builder()
                .id(1L)
                .name("PersistedDocType")
                .build();
    }

    // <!-- role methods -->
    public Role getRoleTransient() {
        return Role.builder()
                .name("TransientRole")
                .build();
    }

    public Role getRolePersisted() {
        return Role.builder()
                .id(1L)
                .name("PersistedRole")
                .build();
    }

    // <!-- rental state methods -->
    public RentalState getRentalStateTransient() {
        return RentalState.builder()
                .name("TransientRentalState")
                .build();
    }

    public RentalState getRentalStatePersisted() {
        return RentalState.builder()
                .id(1L)
                .name("PersistedRentalState")
                .build();
    }

    public Client createUniqueClient(String prefix) {
        long unique = System.nanoTime();
        return clientRepository.save(Client.builder()
                .firstName("First")
                .lastName("Last")
                .login(prefix + "_login_" + unique)
                .password("pwd")
                .email(prefix + "_mail_" + unique + "@example.com")
                .phone(String.valueOf(100 + unique))
                .deleted(false)
                .banned(false)
                .build());
    }

    // ---- RENTAL STATE ----
    public RentalState getOrCreateRentalState(String name) {
        return rentalStateRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> rentalStateRepository.save(RentalState.builder().name(name).build()));
    }

    // ---- CONTRACT ----
    public Contract createContract(Client client, Car car, String stateName,
                                   LocalDate start, LocalDate end) {
        RentalState state = getOrCreateRentalState(stateName);
        return contractRepository.save(Contract.builder()
                .client(client)
                .car(car)
                .state(state)
                .dataStart(start)
                .dataEnd(end)
                .totalCost(100.0)
                .comment("cmt")
                .build());
    }


}