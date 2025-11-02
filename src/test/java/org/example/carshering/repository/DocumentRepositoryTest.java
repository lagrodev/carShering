package org.example.carshering.repository;

import org.example.carshering.entity.Client;
import org.example.carshering.entity.Document;
import org.example.carshering.entity.DocumentType;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
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

    private Client createAndSaveClient(String login, String email) {
        Client client = Client.builder()
                .firstName("First")
                .lastName("Last")
                .login(login)
                .password("pwd")
                .email(email)
                .build();
        return clientRepository.save(client);
    }

    private DocumentType createAndSaveDocumentType(String name) {
        DocumentType dt = DocumentType.builder().name(name).build();
        return documentTypeRepository.save(dt);
    }

    @Test
    @DisplayName("Test save document functionality")
    public void givenDocumentObject_whenSave_thenDocumentIsCreated() {
        // given
        DocumentType dt = createAndSaveDocumentType("PASSPORT");
        Client client = createAndSaveClient("docuser1", "doc1@example.com");

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
        DocumentType dt = createAndSaveDocumentType("DL");
        Client client = createAndSaveClient("docuser2", "doc2@example.com");
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
        DocumentType dt = createAndSaveDocumentType("ID");
        Client client = createAndSaveClient("docuser3", "doc3@example.com");
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
        DocumentType dt = createAndSaveDocumentType("TYPE1");
        Client c1 = createAndSaveClient("vuser1", "v1@example.com");
        Client c2 = createAndSaveClient("vuser2", "v2@example.com");
        Client c3 = createAndSaveClient("vuser3", "v3@example.com");

        Document d1 = Document.builder().documentType(dt).series("S1").number("1").dateOfIssue(LocalDate.now()).issuingAuthority("A").client(c1).verified(false).deleted(false).build();
        Document d2 = Document.builder().documentType(dt).series("S2").number("2").dateOfIssue(LocalDate.now()).issuingAuthority("A").client(c2).verified(true).deleted(false).build();
        Document d3 = Document.builder().documentType(dt).series("S3").number("3").dateOfIssue(LocalDate.now()).issuingAuthority("A").client(c3).verified(false).deleted(false).build();

        documentRepository.saveAll(List.of(d1, d2, d3));

        // when
        List<Document> unverified = documentRepository.findByVerifiedIsFalse();

        // then
        assertThat(unverified).isNotNull();
        assertThat(unverified).hasSize(2);
        assertThat(unverified).allMatch(doc -> !doc.isVerified());
    }

    @Test
    @DisplayName("findByClientIdAndDeletedFalse respects deleted flag")
    public void findByClientIdAndDeletedFalse_behaviour() {
        // given
        DocumentType dt = createAndSaveDocumentType("TYPE2");
        Client client = createAndSaveClient("dduser", "dd@example.com");
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
        DocumentType dt = createAndSaveDocumentType("TYPE3");
        Client client = createAndSaveClient("exuser", "ex@example.com");
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

}
