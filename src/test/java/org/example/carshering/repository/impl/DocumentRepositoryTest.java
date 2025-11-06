package org.example.carshering.repository.impl;

import org.example.carshering.entity.Client;
import org.example.carshering.entity.Document;
import org.example.carshering.entity.DocumentType;
import org.example.carshering.repository.AbstractRepositoryTest;
import org.example.carshering.repository.ClientRepository;
import org.example.carshering.repository.DocumentRepository;
import org.example.carshering.repository.DocumentTypeRepository;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DataUtils.class)
@ActiveProfiles("test")
public class DocumentRepositoryTest extends AbstractRepositoryTest {


    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private DataUtils dataUtils;

    @BeforeEach
    public void setUp() {
        clientRepository.deleteAll();
        documentTypeRepository.deleteAll();
        documentRepository.deleteAll();
    }



    @Test
    @DisplayName("Test save document functionality")
    public void givenDocumentObject_whenSave_thenDocumentIsCreated() {
        // given
        DocumentType dt = documentTypeRepository.save (dataUtils.createAndSaveDocumentType("PASSPORT"));
        Client client = clientRepository.save(dataUtils.createAndSaveClient("docuser1", "doc1@example.com"));

        Document doc = Document.builder()
                .documentType(dt)
                .series("AA")
                .number("123456")
                .dateOfIssue(LocalDate.now())
                .issuingAuthority("Gov")
                .client(client)
                .verified(false)
                .deleted(false)
                .build();

        // when
        Document saved = documentRepository.save(doc);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test update document verified functionality")
    public void givenDocument_whenUpdateVerified_thenPersisted() {
        // given
        DocumentType dt = documentTypeRepository.save (dataUtils.createAndSaveDocumentType("DL"));
        Client client =  clientRepository.save(dataUtils.createAndSaveClient("docuser2", "doc2@example.com"));
        Document doc = Document.builder()
                .documentType(dt)
                .series("BB")
                .number("654321")
                .dateOfIssue(LocalDate.now())
                .issuingAuthority("Agency")
                .client(client)
                .verified(false)
                .deleted(false)
                .build();
        documentRepository.save(doc);

        // when
        Document loaded = documentRepository.findById(doc.getId()).orElse(null);
        assertThat(loaded).isNotNull();
        loaded.setVerified(true);
        Document updated = documentRepository.save(loaded);

        // then
        assertThat(updated.isVerified()).isTrue();
    }

    @Test
    @DisplayName("Test findByClientId returns document when present")
    public void givenDocumentSaved_whenFindByClientId_thenReturned() {
        // given
        DocumentType dt = documentTypeRepository.save (dataUtils.createAndSaveDocumentType("ID"));
        Client client =  clientRepository.save(dataUtils.createAndSaveClient("docuser3", "doc3@example.com"));
        Document doc = Document.builder()
                .documentType(dt)
                .series("CC")
                .number("111111")
                .dateOfIssue(LocalDate.now())
                .issuingAuthority("Authority")
                .client(client)
                .verified(false)
                .deleted(false)
                .build();
        documentRepository.save(doc);

        // when
        Document obtained = documentRepository.findByClientId(client.getId()).orElse(null);

        // then
        assertThat(obtained).isNotNull();
        assertThat(obtained.getClient().getId()).isEqualTo(client.getId());
    }

    @Test
    @DisplayName("Test findByClientId returns empty when no document")
    public void givenNoDocument_whenFindByClientId_thenEmpty() {
        // when
        Document obtained = documentRepository.findByClientId(9999L).orElse(null);

        // then
        assertThat(obtained).isNull();
    }

    @Test
    @DisplayName("findByVerifiedIsFalse returns only unverified documents")
    public void findByVerifiedIsFalse_returnsOnlyUnverified() {
        // given
        DocumentType dt = documentTypeRepository.save (dataUtils.createAndSaveDocumentType("TYPE1"));
        Client c1 =  clientRepository.save(dataUtils.createAndSaveClient("vuser1", "v1@example.com"));
        Client c2 = clientRepository.save(dataUtils.createAndSaveClient("vuser2", "v2@example.com"));
        Client c3 = clientRepository.save(dataUtils.createAndSaveClient("vuser3", "v3@example.com"));

        Document d1 = Document.builder().documentType(dt).series("S1").number("1").dateOfIssue(LocalDate.now()).issuingAuthority("A").client(c1).verified(false).deleted(false).build();
        Document d2 = Document.builder().documentType(dt).series("S2").number("2").dateOfIssue(LocalDate.now()).issuingAuthority("A").client(c2).verified(true).deleted(false).build();
        Document d3 = Document.builder().documentType(dt).series("S3").number("3").dateOfIssue(LocalDate.now()).issuingAuthority("A").client(c3).verified(false).deleted(false).build();

        documentRepository.saveAll(List.of(d1, d2, d3));

        Pageable pageable = PageRequest.of(0, 20);

        // when
        Page<Document> unverified = documentRepository.findByVerifiedIsFalse(pageable);

        // then
        assertThat(unverified).isNotNull();
        assertThat(unverified.getContent()).hasSize(2);
        assertThat(unverified.getTotalElements()).isEqualTo(2);
        assertThat(unverified.getContent()).allMatch(doc -> !doc.isVerified());
    }

    @Test
    @DisplayName("findByClientIdAndDeletedFalse respects deleted flag")
    public void findByClientIdAndDeletedFalse_behaviour() {
        // given
        DocumentType dt = documentTypeRepository.save (dataUtils.createAndSaveDocumentType("TYPE2"));
        Client client = clientRepository.save(dataUtils.createAndSaveClient("dduser", "dd@example.com"));
        Document doc = Document.builder().documentType(dt).series("X").number("9").dateOfIssue(LocalDate.now()).issuingAuthority("A").client(client).verified(false).deleted(false).build();
        documentRepository.save(doc);

        // when present
        Document present = documentRepository.findByClientIdAndDeletedFalse(client.getId()).orElse(null);
        assertThat(present).isNotNull();

        // when set deleted
        doc.setDeleted(true);
        documentRepository.save(doc);

        Document absent = documentRepository.findByClientIdAndDeletedFalse(client.getId()).orElse(null);
        // then
        assertThat(absent).isNull();
    }

    @Test
    @DisplayName("existsByClientId and existsByClientIdAndDeletedFalse behave correctly")
    public void existsByClientId_checks() {
        // given
        DocumentType dt = documentTypeRepository.save (dataUtils.createAndSaveDocumentType("TYPE3"));
        Client client = clientRepository.save(dataUtils.createAndSaveClient("exuser", "ex@example.com"));
        Document doc = Document.builder().documentType(dt).series("Z").number("77").dateOfIssue(LocalDate.now()).issuingAuthority("Auth").client(client).verified(false).deleted(false).build();
        documentRepository.save(doc);

        // when & then
        assertThat(documentRepository.existsByClientId(client.getId())).isTrue();
        assertThat(documentRepository.existsByClientIdAndDeletedFalse(client.getId())).isTrue();

        // when mark deleted
        doc.setDeleted(true);
        documentRepository.save(doc);

        assertThat(documentRepository.existsByClientId(client.getId())).isTrue();
        assertThat(documentRepository.existsByClientIdAndDeletedFalse(client.getId())).isFalse();
    }


    @Test
    @DisplayName("existsByDocumentSeriesAndNumberAndClientBannedTrue returns true when banned client has matching document")
    public void givenBannedClientWithMatchingDocument_whenExistsByDocumentSeriesAndNumberAndClientBannedTrue_thenTrue() {
        // given
        DocumentType dt = documentTypeRepository.save (dataUtils.createAndSaveDocumentType("PASSPORT"));
        Client bannedClient = clientRepository.save(dataUtils.createAndSaveClient("banned_user", "banned@example.com", true));
        documentRepository.save(dataUtils.createAndSaveDocument(bannedClient, dt, "AB", "123456"));

        // when
        boolean exists = documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue("AB", "123456");

        // then
        assertThat(exists).isTrue();
    }




    @Test
    @DisplayName("existsByDocumentSeriesAndNumberAndClientBannedTrue returns false when client is not banned")
    public void givenNonBannedClientWithMatchingDocument_whenExistsByDocumentSeriesAndNumberAndClientBannedTrue_thenFalse() {
        // given
        DocumentType dt = documentTypeRepository.save (dataUtils.createAndSaveDocumentType("PASSPORT"));
        Client notBannedClient = clientRepository.save(dataUtils.createAndSaveClient("good_user", "good@example.com", false));
        documentRepository.save(dataUtils.createAndSaveDocument(notBannedClient, dt, "AB", "123456"));

        // when
        boolean exists = documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue("AB", "123456");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByDocumentSeriesAndNumberAndClientBannedTrue returns false when document does not exist")
    public void givenNoDocument_whenExistsByDocumentSeriesAndNumberAndClientBannedTrue_thenFalse() {
        // when
        boolean exists = documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue("XX", "999999");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByDocumentSeriesAndNumberAndClientBannedTrue returns false when series or number mismatch")
    public void givenBannedClientWithDifferentDocument_whenExistsByDocumentSeriesAndNumberAndClientBannedTrue_thenFalse() {
        // given
        DocumentType dt = documentTypeRepository.save (dataUtils.createAndSaveDocumentType("PASSPORT"));
        Client bannedClient = clientRepository.save(dataUtils.createAndSaveClient("banned_user2", "banned2@example.com", true));
        documentRepository.save(dataUtils.createAndSaveDocument(bannedClient, dt, "AB", "123456"));

        // when
        boolean existsWrongSeries = documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue("CD", "123456");
        boolean existsWrongNumber = documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue("AB", "654321");
        boolean existsBothWrong = documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue("XY", "987654");

        // then
        assertThat(existsWrongSeries).isFalse();
        assertThat(existsWrongNumber).isFalse();
        assertThat(existsBothWrong).isFalse();
    }


    @Test
    @DisplayName("existsBySeriesAndNumber returns true when document with exact series and number exists")
    public void givenDocumentExists_whenExistsBySeriesAndNumber_thenTrue() {
        // given
        DocumentType dt = documentTypeRepository.save(dataUtils.createAndSaveDocumentType("PASSPORT"));
        Client client = clientRepository.save(dataUtils.createAndSaveClient("combo_user", "combo@example.com"));
        documentRepository.save(dataUtils.createAndSaveDocument(client, dt, "XY", "112233"));

        // when
        boolean exists = documentRepository.existsBySeriesAndNumber("XY", "112233");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsBySeriesAndNumber returns false when no document with exact series and number exists")
    public void givenNoMatchingDocument_whenExistsBySeriesAndNumber_thenFalse() {
        // when
        boolean exists = documentRepository.existsBySeriesAndNumber("ZZ", "999999");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsBySeriesAndNumber returns false when only series matches")
    public void givenOnlySeriesMatches_whenExistsBySeriesAndNumber_thenFalse() {
        // given
        DocumentType dt = documentTypeRepository.save(dataUtils.createAndSaveDocumentType("ID"));
        Client client = clientRepository.save(dataUtils.createAndSaveClient("partial_user1", "p1@example.com"));
        documentRepository.save(dataUtils.createAndSaveDocument(client, dt, "AB", "100000"));

        // when
        boolean exists = documentRepository.existsBySeriesAndNumber("AB", "999999");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsBySeriesAndNumber returns false when only number matches")
    public void givenOnlyNumberMatches_whenExistsBySeriesAndNumber_thenFalse() {
        // given
        DocumentType dt = documentTypeRepository.save(dataUtils.createAndSaveDocumentType("DL"));
        Client client = clientRepository.save(dataUtils.createAndSaveClient("partial_user2", "p2@example.com"));
        documentRepository.save(dataUtils.createAndSaveDocument(client, dt, "CD", "200000"));

        // when
        boolean exists = documentRepository.existsBySeriesAndNumber("ZZ", "200000");

        // then
        assertThat(exists).isFalse();
    }



    /**
     * Все методы в {@code DocumentRepository} безопасны от SQL-инъекций по конструкции:
     * <ul>
     *   <li>{@code existsByClientId}, {@code findByClientId},
     *       {@code findByClientIdAndDeletedFalse}, {@code existsByClientIdAndDeletedFalse}
     *       принимают только параметр типа {@code Long} (идентификатор клиента).
     *       Значения типа {@code Long} не могут содержать SQL-код — они передаются в запрос
     *       как параметризованные значения (например, {@code WHERE client_id = ?}),
     *       а не конкатенируются со строкой запроса.</li>
     *
     *   <li>Даже если HTTP-запрос содержит строку вроде {@code "1'; DROP TABLE--"},
     *       Spring сначала пытается преобразовать её в {@code Long}.
     *       При неудаче выбрасывается {@code TypeMismatchException} на уровне контроллера
     *       или конвертера — до вызова репозитория. Таким образом, вредоносный ввод
     *       физически не может попасть в SQL-запрос.</li>
     *
     *   <li>{@code findByVerifiedIsFalse()} не имеет параметров — неуязвим по определению.</li>
     * </ul>
     * <p>
     * Вывод: поскольку в репозитории отсутствуют методы с {@code String}-параметрами,
     * зависящими от пользовательского ввода, тестирование на SQL-инъекцию избыточно.
     */


    @Test
    @DisplayName("existsByDocumentSeriesAndNumberAndClientBannedTrue is safe against SQL injection in series and number parameters")
    public void existsByDocumentSeriesAndNumberAndClientBannedTrue_sqlInjectionAttempts_doNotCauseErrorsOrDataLeak() {
        // given
        DocumentType dt = documentTypeRepository.save (dataUtils.createAndSaveDocumentType("PASSPORT"));
        Client bannedClient = clientRepository.save(dataUtils.createAndSaveClient("inj_test_user", "inj@example.com", true));
        documentRepository.save(dataUtils.createAndSaveDocument(bannedClient, dt, "SAFE_SERIES", "SAFE_NUMBER"));

        List<String> injectionPayloads = Arrays.asList(
                "' OR '1'='1",
                "'; DROP TABLE documents; --",
                "SAFE_SERIES'--",
                "x' AND 1=1; --",
                "/*",
                "1' UNION SELECT null--",
                "\" OR \"\"=\"",
                "\\'; DELETE FROM clients; --",
                "SAFE_NUMBER'; SELECT * FROM clients WHERE 'x'='x"
        );

        // when & then: попытка внедрения не должна вызвать исключение и должна вернуть false
        for (String seriesPayload : injectionPayloads) {
            boolean result = documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue(seriesPayload, "SAFE_NUMBER");
            assertThat(result)
                    .as("Method should return false for series injection payload: %s", seriesPayload)
                    .isFalse();
        }

        for (String numberPayload : injectionPayloads) {
            boolean result = documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue("SAFE_SERIES", numberPayload);
            assertThat(result)
                    .as("Method should return false for number injection payload: %s", numberPayload)
                    .isFalse();
        }

        // Убедимся, что легитимный вызов всё ещё работает
        boolean validResult = documentRepository.existsByDocumentSeriesAndNumberAndClientBannedTrue("SAFE_SERIES", "SAFE_NUMBER");
        assertThat(validResult).isTrue();
    }
    @Test
    @DisplayName("existsBySeriesAndNumber is safe against SQL injection in series and number parameters")
    public void existsBySeriesAndNumber_sqlInjectionAttempts_returnFalseAndDoNotBreak() {
        // given
        DocumentType dt = documentTypeRepository.save(dataUtils.createAndSaveDocumentType("PASSPORT"));
        Client client = clientRepository.save(dataUtils.createAndSaveClient("inj_combo", "inj_combo@example.com"));
        documentRepository.save(dataUtils.createAndSaveDocument(client, dt, "SAFE_SER", "SAFE_NUM"));

        List<String> payloads = Arrays.asList(
                "' OR '1'='1",
                "'; DROP TABLE documents; --",
                "SAFE_SER'--",
                "x' AND 1=1; --",
                "/*",
                "1' UNION SELECT null--",
                "\" OR \"\"=\"",
                "\\'; DELETE FROM documents; --"
        );

        // Test series injection (number valid)
        for (String seriesPayload : payloads) {
            boolean result = documentRepository.existsBySeriesAndNumber(seriesPayload, "SAFE_NUM");
            assertThat(result)
                    .as("existsBySeriesAndNumber must return false for series payload: %s", seriesPayload)
                    .isFalse();
        }

        // Test number injection (series valid)
        for (String numberPayload : payloads) {
            boolean result = documentRepository.existsBySeriesAndNumber("SAFE_SER", numberPayload);
            assertThat(result)
                    .as("existsBySeriesAndNumber must return false for number payload: %s", numberPayload)
                    .isFalse();
        }

        // Test both parameters injected
        for (String payload : payloads) {
            boolean result = documentRepository.existsBySeriesAndNumber(payload, payload);
            assertThat(result)
                    .as("existsBySeriesAndNumber must return false when both params are payload: %s", payload)
                    .isFalse();
        }

        // Ensure legitimate query still works
        assertThat(documentRepository.existsBySeriesAndNumber("SAFE_SER", "SAFE_NUM")).isTrue();
    }


}
