package org.example.carshering.util;

import org.example.carshering.domain.entity.*;
import org.example.carshering.dto.request.create.CreateCarRequest;
import org.example.carshering.dto.request.create.CreateDocumentRequest;
import org.example.carshering.dto.request.update.UpdateCarRequest;
import org.example.carshering.dto.request.update.UpdateCarStateRequest;
import org.example.carshering.dto.request.update.UpdateDocumentRequest;
import org.example.carshering.dto.response.CarDetailResponse;
import org.example.carshering.dto.response.CarStateResponse;
import org.example.carshering.dto.response.ContractResponse;
import org.example.carshering.dto.response.DocumentResponse;
import org.springframework.boot.test.context.TestConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TestConfiguration
public class DataUtils {

    public static CarState getCarStateNEWSTATEPersisted(String status) {
        return CarState.builder().status(status).build();
    }

    public static DocumentType getDocumentTypePersisted(String status) {
        return DocumentType.builder().name(status).build();
    }

    public static CarDetailResponse carDetailResponsePersisted() {
        return CarDetailResponse.builder()
                .id(1L)
                .vin("JOHNDOE")
                .gosNumber("1123")
                .status("AVAILABLE")
                .rent(10.0)
                .build();
    }

    public static DocumentResponse documentResponsePersisted(String documentType, String series, String number, String issuingAuthority, boolean vera) {
        return DocumentResponse.builder()
                .id(1L)
                .series(series)
                .issuingAuthority(issuingAuthority)
                .documentType(documentType)
                .number(number)
                .verified(vera)

                .build();
    }

    // <!-- contract methods -->
    public static ContractResponse contractResponsePersisted() {
        return new ContractResponse(
                1L,
                1000.0,
                "Toyota",
                "Camry",
                "SEDAN",
                "ECONOMY",
                2020,
                "Ivanov",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10),
                "VIN123456",
                "A123BC",
                "PENDING"
        );
    }

    public static ContractResponse contractResponseConfirmed() {
        return new ContractResponse(
                1L,
                1000.0,
                "Toyota",
                "Camry",
                "SEDAN",
                "ECONOMY",
                2020,
                "Ivanov",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10),
                "VIN123456",
                "A123BC",
                "CONFIRMED"
        );
    }

    public static ContractResponse contractResponseCancelled() {
        return new ContractResponse(
                2L,
                2000.0,
                "BMW",
                "X5",
                "SUV",
                "PREMIUM",
                2021,
                "Petrov",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(15),
                "VIN789012",
                "B456CD",
                "CANCELLED"
        );
    }

    public Car getJohnDoeTransient(CarState state, CarModel model) {

        return Car.builder()
                .state(state)
                .model(model)
                .yearOfIssue(2020)
                .rent(10.)
                .vin("JOHNDOE")
                .gosNumber("1123")
                .build();
    }

    public Car getJohnDoeTransient(CarState state, CarModel model, String vin, String gosNumber, int yearOfIssue, Double rent) {

        return Car.builder()
                .state(state)
                .model(model)
                .yearOfIssue(yearOfIssue)
                .rent(rent)
                .vin(vin)
                .gosNumber(gosNumber)
                .build();
    }


    public Car getFrankJonesTransient(CarState state, CarModel model) {
        return Car.builder()
                .state(state)
                .model(model)
                .yearOfIssue(2020)
                .rent(10.)
                .vin("FRANKJONES")
                .gosNumber("1124")
                .build();
    }

    public Car getMikeSmithTransient(CarState state, CarModel model) {
        return Car.builder()
                .state(state)
                .model(model)
                .yearOfIssue(2020)
                .rent(10.)
                .vin("MIKESMITH")
                .gosNumber("1125")
                .build();
    }

    public CarModel getCarModelSEDAN(Brand brand, Model model, CarClass carClass) {
        return CarModel.builder()
                .brand(brand)
                .model(model)
                .bodyType("SEDAN")
                .carClass(carClass)
                .deleted(false)
                .build();
    }

    public CarModel getCarModelBody(Brand brand, Model model, CarClass carClass, String bodyType) {
        return CarModel.builder()
                .brand(brand)
                .model(model)
                .bodyType(bodyType)
                .carClass(carClass)
                .deleted(false)
                .build();
    }

    public Car getJohnDoePersisted(CarState state, CarModel model) {
        return Car.builder()
                .id(1L)
                .state(state)
                .model(model)
                .yearOfIssue(2020)
                .rent(10.)
                .vin("JOHNDOE")
                .gosNumber("1123")
                .build();
    }

    public Car getMikeSmithPersisted(CarState state, CarModel model) {


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

    public Car getFrankJonesPersisted(CarState state, CarModel model) {


        return Car.builder()
                .id(1L)
                .state(state)
                .model(model)
                .yearOfIssue(2020)
                .rent(10.)
                .vin("JOHNDOE")
                .gosNumber("1125")
                .build();
    }

    public Car getCarWithSpecificAttributes(
            String vin, String gosNumber, int year, CarState carState, CarModel carModel) {
        return Car.builder()
                .state(carState)
                .model(carModel)
                .yearOfIssue(year)
                .rent(15.0)
                .vin(vin)
                .gosNumber(gosNumber)
                .build();
    }

    public Car getOldCarTransient(CarState state, CarModel model) {

        model.setBodyType("HATCHBACK");

        return Car.builder()
                .state(state)
                .model(model)
                .yearOfIssue(1999)
                .rent(5.0)
                .vin("OLDCARVIN")
                .gosNumber("1999")
                .build();
    }

    public Car getFutureCarTransient(CarState state, CarModel model) {

        model.setBodyType("COUPE");


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

    // <!-- car state methods --!>

    public Brand getBrandTransient(String brandName) {
        return Brand.builder()
                .name(brandName)
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

    public CarClass getCarClassTransient(String className) {
        return CarClass.builder()
                .name(className)
                .build();
    }

    public CarClass getCarClassPersisted() {
        return CarClass.builder()
                .id(1L)
                .name("PersistedCarClass")
                .build();
    }

    public CarState getCarStateTransient() {
        return CarState.builder()
                .status("Available")
                .build();
    }

    public CarStateResponse getCarStateResponse() {
        return CarStateResponse.builder()
                .id(1L)
                .status("Available")
                .build();
    }

    public UpdateCarStateRequest getCarStateRequest() {
        return UpdateCarStateRequest.builder()
                .stateName("Available")
                .build();
    }

    public UpdateCarStateRequest getCarStateRequest(String stateName) {
        return UpdateCarStateRequest.builder()
                .stateName(stateName)
                .build();
    }

    public CarState getCarStateTransient(String stateName) {
        return CarState.builder()
                .status(stateName)
                .build();
    }

    public CarState getCarStatePersisted() {
        return CarState.builder()
                .id(1L)
                .status("Available")
                .build();
    }

    public CarState getCarStatePersisted(String s) {
        return CarState.builder()
                .id(1L)
                .status(s)
                .build();
    }

    // <!-- model name methods --!>
    public Model getModelNameTransient() {
        return Model.builder()
                .name("TransientModelName")
                .build();
    }

    public Model getModelNameTransient(String modelName) {
        return Model.builder()
                .name(modelName)
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
        return Client.builder()
                .firstName("First")
                .lastName("Last")
                .login(prefix + "_login_" + unique)
                .password("pwd")
                .email(prefix + "_mail@example.com")
                .phone(String.valueOf(100 + unique))
                .deleted(false)
                .banned(false)
                .build();
    }

    // ---- RENTAL STATE ----
    public RentalState getRentalState(String name) {
        return RentalState
                .builder()
                .name(name)
                .build();
    }

    // ---- CONTRACT ----
    public Contract createContract(Client client, Car car, RentalState stateName,
                                   LocalDate start, LocalDate end) {
        long durationMinutes = java.time.Duration.between(start.atStartOfDay(), end.atStartOfDay()).toMinutes();
        return Contract.builder()
                .client(client)
                .car(car)
                .state(stateName)
                .dataStart(start.atStartOfDay())
                .dataEnd(end.atStartOfDay())
                .totalCost(100.0)
                .durationMinutes(durationMinutes)
                .comment("cmt")
                .build();
    }

    public Contract createContractWithDateTime(Client client, Car car, RentalState stateName,
                                               LocalDateTime start, LocalDateTime end) {
        long durationMinutes = java.time.Duration.between(start, end).toMinutes();
        return Contract.builder()
                .client(client)
                .car(car)
                .state(stateName)
                .dataStart(start)
                .dataEnd(end)
                .totalCost(100.0)
                .durationMinutes(durationMinutes)
                .comment("cmt")
                .build();
    }

    public CreateCarRequest createCarRequestTransient() {
        return CreateCarRequest.builder()
                .modelId(1L)
                .yearOfIssue(2020)
                .gosNumber("1123")
                .vin("JOHNDOE")
                .rent(10.0)
                .build();
    }

    public UpdateCarRequest updateCarRequestTransient() {
        return UpdateCarRequest.builder()
                .modelId(1L)
                .yearOfIssue(2020)
                .gosNumber("1123")
                .vin("JOHNDOE")
                .rent(10.0)
                .build();
    }

    public UpdateCarRequest updateCarRequestNeverUsedTransient() {
        return UpdateCarRequest.builder()
                .modelId(1L)
                .yearOfIssue(2020)
                .gosNumber("2025")
                .vin("ZOV2025")
                .rent(10.0)
                .build();
    }

    public Car getNeverUsedTransient(CarState state, CarModel model) {
        return Car.builder()
                .id(1L)
                .state(state)
                .model(model)
                .yearOfIssue(2020)
                .rent(10.)
                .vin("ZOV2025")
                .gosNumber("2025")
                .build();
    }

    public UpdateCarRequest updateCarRequest() {
        return UpdateCarRequest.builder()
                .modelId(1L)
                .yearOfIssue(2020)
                .gosNumber("1123")
                .vin("JOHNDOE")
                .rent(10.0)
                .build();
    }

    public UpdateCarRequest updateCarRequestForVin() {
        return UpdateCarRequest.builder()

                .vin("JOHNDOE")
                .rent(50.0)
                .build();
    }

    public UpdateCarRequest updateCarRequestWithoutModelId() {
        return UpdateCarRequest.builder()
                .modelId(null)
                .yearOfIssue(2021)
                .gosNumber("1123")
                .vin("JOHNDOE")
                .rent(15.0)
                .build();
    }

    public UpdateCarRequest updateCarRequestWithBlankVin() {
        return UpdateCarRequest.builder()
                .modelId(1L)
                .yearOfIssue(2020)
                .gosNumber("1123")
                .vin("   ")
                .rent(10.0)
                .build();
    }

    public UpdateCarRequest updateCarRequestWithBlankGosNumber() {
        return UpdateCarRequest.builder()
                .modelId(1L)
                .yearOfIssue(2020)
                .gosNumber("   ")
                .vin("JOHNDOE")
                .rent(10.0)
                .build();
    }

    public UpdateCarRequest updateCarRequestWithBlankVinAndGosNumber() {
        return UpdateCarRequest.builder()
                .modelId(1L)
                .yearOfIssue(2020)
                .gosNumber("   ")
                .vin("   ")
                .rent(10.0)
                .build();
    }

    public UpdateCarRequest updateCarRequestWithEmptyVin() {
        return UpdateCarRequest.builder()
                .modelId(1L)
                .yearOfIssue(2020)
                .gosNumber("1123")
                .vin("")
                .rent(10.0)
                .build();
    }

    public UpdateCarRequest updateCarRequestWithEmptyGosNumber() {
        return UpdateCarRequest.builder()
                .modelId(1L)
                .yearOfIssue(2020)
                .gosNumber("")
                .vin("JOHNDOE")
                .rent(10.0)
                .build();
    }

    public UpdateCarRequest updateCarRequestWithNullVin() {
        return UpdateCarRequest.builder()
                .modelId(1L)
                .yearOfIssue(2020)
                .gosNumber("1123")
                .vin(null)
                .rent(10.0)
                .build();
    }

    public Client createAndSaveClient(String login, String email, boolean banned) {
        Client client = Client.builder()
                .firstName("First")
                .lastName("Last")
                .login(login)
                .password("pwd")
                .email(email)
                .banned(banned)
                .build();
        return client;
    }

    public Document createAndSaveDocument(Client client, DocumentType docType, String series, String number) {
        Document doc = Document.builder()
                .documentType(docType)
                .series(series)
                .number(number)
                .dateOfIssue(LocalDate.now())
                .issuingAuthority("Authority")
                .client(client)
                .verified(false)
                .deleted(false)
                .build();
        return doc;
    }

    public Client createAndSaveClient(String login, String email) {
        Client client = Client.builder()
                .firstName("First")
                .lastName("Last")
                .login(login)
                .password("pwd")
                .email(email)
                .build();
        return client;
    }


    public Client createAndSaveClient(String firstName, String lastName,
                                      String   login,String phone ,String email, Role role, String password) {
        Client client = Client.builder()
                .firstName(firstName)
                .lastName(lastName)
                .login(login)

                .phone(phone)
                .email(email)
                .role(role)
                .password(password)

                .build();
        return client;
    }


    public DocumentType createAndSaveDocumentType(String name) {
        DocumentType dt = DocumentType.builder().name(name).build();
        return dt;
    }

    public CreateDocumentRequest createDocumentRequestTransient(String series, String number, String issuingAuthority) {
        return CreateDocumentRequest.builder()
                .documentTypeId(1L)
                .series(series)
                .number(number)
                .dateOfIssue(LocalDate.now())
                .issuingAuthority(issuingAuthority)
                .build();
    }

    public Document createDocumentTransient(Client client, DocumentType documentType, String series, String number, String issuingAuthority, boolean vera) {
        return Document.builder()
                .documentType(documentType)
                .series(series)
                .number(number)
                .dateOfIssue(LocalDate.now())
                .verified(vera)
                .client(client)
                .issuingAuthority(issuingAuthority)
                .build();
    }

    public Document createDocumentTransient(String series, String number, String issuingAuthority, boolean vera) {
        return Document.builder()
                .series(series)
                .number(number)
                .dateOfIssue(LocalDate.now())
                .verified(vera)
                .issuingAuthority(issuingAuthority)
                .build();
    }

    public Document createDocumentPersisted(Client client, DocumentType documentType, String series, String number, String issuingAuthority, boolean vera) {
        return Document.builder()
                .id(1L)
                .documentType(documentType)
                .series(series)
                .number(number)
                .dateOfIssue(LocalDate.now())
                .verified(vera)
                .client(client)
                .issuingAuthority(issuingAuthority)
                .build();
    }

    public UpdateDocumentRequest createUpdateDocumentRequest(String series, String number, String auto) {
        return UpdateDocumentRequest.builder()
                .series(series)
                .number(number)
                .issuingAuthority(auto)
                .dateOfIssue(LocalDate.now())
                .build();
    }
}

