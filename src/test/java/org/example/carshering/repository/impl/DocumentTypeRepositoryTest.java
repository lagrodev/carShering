package org.example.carshering.repository.impl;


import org.example.carshering.entity.DocumentType;
import org.example.carshering.repository.AbstractRepositoryTest;
import org.example.carshering.repository.DocumentTypeRepository;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;


@DataJpaTest
@Import(DataUtils.class)
@ActiveProfiles("test")
public class DocumentTypeRepositoryTest extends AbstractRepositoryTest {

     @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private DataUtils dataUtils;
    @BeforeEach
    public void setUp() {
        documentTypeRepository.deleteAll();
    }

    @Test
    @DisplayName("Test save document type functionality")
    public void givenDocumentTypeObject_whenSave_thenDocumentTypeIsCreated() {
        // given
        DocumentType docTypeToSave = dataUtils.getDocumentTypeTransient();

        // when
        DocumentType savedDocType = documentTypeRepository.save(docTypeToSave);

        // then
        assertThat(savedDocType).isNotNull();
        assertThat(savedDocType.getId()).isNotNull();
        assertThat(savedDocType.getName()).isEqualTo(docTypeToSave.getName());
    }

    @Test
    @DisplayName("Test update document type functionality")
    public void givenDocumentTypeToUpdate_whenSave_thenDocumentTypeIsChanged() {
        // given
        DocumentType docTypeToSave = dataUtils.getDocumentTypeTransient();
        DocumentType savedDocType = documentTypeRepository.save(docTypeToSave);

        String updatedName = "UpdatedDocType";

        // when
        savedDocType.setName(updatedName);
        DocumentType updatedDocType = documentTypeRepository.save(savedDocType);

        // then
        assertThat(updatedDocType).isNotNull();
        assertThat(updatedDocType.getName()).isEqualTo(updatedName);
    }

    @Test
    @DisplayName("Test get document type by id functionality")
    public void givenDocumentTypeCreated_whenFindById_thenDocumentTypeIsReturned() {
        // given
        DocumentType docTypeToSave = dataUtils.getDocumentTypeTransient();
        DocumentType savedDocType = documentTypeRepository.save(docTypeToSave);

        // when
        DocumentType obtainedDocType = documentTypeRepository.findById(savedDocType.getId()).orElse(null);

        // then
        assertThat(obtainedDocType).isNotNull();
        assertThat(obtainedDocType.getName()).isEqualTo(docTypeToSave.getName());
    }

    @Test
    @DisplayName("Test document type not found by id functionality")
    public void givenDocumentTypeIsNotCreated_whenFindById_thenOptionalIsEmpty() {
        // when
        DocumentType obtainedDocType = documentTypeRepository.findById(1L).orElse(null);

        // then
        assertThat(obtainedDocType).isNull();
    }


    @Test
    @DisplayName("Test cannot save document type with same name in different case due to unique constraint")
    void givenDocumentTypeExists_whenSaveSameNameDifferentCase_thenThrowsDataIntegrityViolation() {
        // given
        documentTypeRepository.save(DocumentType.builder().name("DRIVER_LICENSE").build());

        // when & then
        DocumentType duplicate = DocumentType.builder().name("driver_license").build();
        assertThatThrownBy(() -> documentTypeRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Test get all document types functionality")
    public void givenThreeDocumentTypesAreStored_whenFindAll_thenAllDocumentTypesAreReturned() {
        // given
        DocumentType type1 = dataUtils.getDocumentTypeTransient();
        DocumentType type2 = DocumentType.builder().name("AnotherDocType").build();
        DocumentType type3 = DocumentType.builder().name("ThirdDocType").build();

        documentTypeRepository.saveAll(List.of(type1, type2, type3));

        // when
        List<DocumentType> obtainedDocTypes = documentTypeRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedDocTypes)).isFalse();
        assertThat(obtainedDocTypes).hasSize(3);
        assertThat(obtainedDocTypes)
                .extracting(DocumentType::getName)
                .containsExactlyInAnyOrder(type1.getName(), type2.getName(), type3.getName());
    }

    @Test
    @DisplayName("Test get all document types when no document types stored functionality")
    public void givenNoDocumentTypesAreStored_whenFindAll_thenEmptyListIsReturned() {
        // when
        List<DocumentType> obtainedDocTypes = documentTypeRepository.findAll();

        // then
        assertThat(CollectionUtils.isEmpty(obtainedDocTypes)).isTrue();
    }

    @Test
    @DisplayName("Test delete document type by id functionality")
    public void givenDocumentTypeIsSaved_whenDeleteById_thenDocumentTypeIsRemoved() {
        // given
        DocumentType docTypeToSave = dataUtils.getDocumentTypeTransient();
        DocumentType savedDocType = documentTypeRepository.save(docTypeToSave);

        // when
        documentTypeRepository.deleteById(savedDocType.getId());

        // then
        DocumentType obtainedDocType = documentTypeRepository.findById(savedDocType.getId()).orElse(null);
        assertThat(obtainedDocType).isNull();
    }

    @Test
    @DisplayName("Test save document type with duplicate name throws exception due to unique constraint")
    public void givenDocumentTypeWithDuplicateName_whenSaved_thenThrowsDataIntegrityViolationException() {
        // given
        String docTypeName = "UniqueDocType";
        DocumentType type1 = DocumentType.builder().name(docTypeName).build();
        documentTypeRepository.save(type1);

        DocumentType type2 = DocumentType.builder().name(docTypeName).build();

        // when & then
        assertThatThrownBy(() -> documentTypeRepository.saveAndFlush(type2))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("unique");
    }
    /**
     * Репозиторий {@code DocumentTypeRepository} не содержит методов, уязвимых к SQL-инъекции:
     * <ul>
     *   <li>Все унаследованные методы из {@link JpaRepository} (например, {@code findById}, {@code existsById},
     *       {@code deleteById}) работают с параметром типа {@code Long} — идентификатором сущности.</li>
     *   <li>Тип {@code Long} не позволяет внедрить SQL-код: Spring Data JPA передаёт его как параметризованный
     *       placeholder в запрос (например, {@code WHERE id = ?}), а не через конкатенацию строк.</li>
     *   <li>При передаче недопустимой строки (например, {@code "1'; DROP TABLE--"}) из HTTP,
     *       Spring пытается преобразовать её в {@code Long} на уровне контроллера или конвертера.
     *       В случае ошибки — выбрасывается {@code TypeMismatchException} ДО вызова репозитория.</li>
     *   <li>Пользовательские методы отсутствуют, а стандартные CRUD-операции не принимают строковые
     *       параметры, зависящие от ввода.</li>
     * </ul>
     * <p>
     * Следовательно, SQL-инъекция в этом репозитории технически невозможна — тестирование избыточно.
     */

}