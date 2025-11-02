package org.example.carshering.repository;

import org.example.carshering.entity.Client;
import org.example.carshering.entity.Role;
import org.example.carshering.util.DataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(DataUtils.class)
@ActiveProfiles("test")
public class ClientRepositoryTest extends AbstractRepositoryTest {


    private final Pageable pageable = PageRequest.of(0, 10);
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private DataUtils dataUtils;

    @BeforeEach
    public void setUp() {
        clientRepository.deleteAll();
        roleRepository.deleteAll();
    }


    private Client createClient(String login, String email, String phone, boolean deleted, boolean banned, Role role) {
        Client c = Client.builder()
                .firstName("First")
                .lastName("Last")
                .login(login)
                .password("pwd")
                .email(email)
                .phone(phone)
                .deleted(deleted)
                .banned(banned)
                .role(role)
                .build();
        return clientRepository.save(c);
    }

    private Role createRole(String name) {
        return roleRepository.save(Role.builder().name(name).build());
    }

    @Test
    @DisplayName("Test save client functionality")
    public void givenClientObject_whenSave_thenClientIsCreated() {
        // given
        Client client = createClient("user1", "u1@example.com", "123", false, false, null);

        // when
        Client saved = clientRepository.save(client);

        // then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test update client functionality")
    public void givenClientToUpdate_whenSave_thenClientIsChanged() {
        // given
        Client client = createClient("user2", "u2@example.com", "111", false, false, null);

        // when
        Client loaded = clientRepository.findById(client.getId()).orElse(null);
        assertThat(loaded).isNotNull();
        loaded.setFirstName("Updated");
        Client updated = clientRepository.save(loaded);

        // then
        assertThat(updated.getFirstName()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("Test get client by id functionality")
    public void givenClientCreated_whenFindById_thenClientIsReturned() {
        // given
        Client client = createClient("user3", "u3@example.com", "222", false, false, null);

        // when
        Client obtained = clientRepository.findById(client.getId()).orElse(null);

        // then
        assertThat(obtained).isNotNull();
        assertThat(obtained.getLogin()).isEqualTo("user3");
    }

    @Test
    @DisplayName("Test client not found functionality")
    public void givenClientIsNotCreated_whenGetById_thenOptionalIsEmpty() {
        // when
        Client obtained = clientRepository.findById(999L).orElse(null);

        // then
        assertThat(obtained).isNull();
    }

    @Test
    @DisplayName("existByLogin native query returns true when login exists")
    public void existByLogin_returnsTrueWhenExists() {
        // given
        createClient("loginA", "la@example.com", "1", false, false, null);

        // when
        boolean exists = clientRepository.existByLogin("loginA");

        // then
        assertThat(exists).isTrue();
        assertThat(clientRepository.existByLogin("notexist")).isFalse();
    }

    @Test
    @DisplayName("existsByLoginAndDeletedFalse respects deleted flag")
    public void existsByLoginAndDeletedFalse_checksDeleted() {
        // given
        Client c = createClient("loginB", "lb@example.com", "2", false, false, null);
        assertThat(clientRepository.existsByLoginAndDeletedFalse("loginB")).isTrue();

        c.setDeleted(true);
        clientRepository.save(c);

        assertThat(clientRepository.existsByLoginAndDeletedFalse("loginB")).isFalse();
    }

    @Test
    @DisplayName("existsByEmail and existsByEmailAndDeletedFalse behave correctly")
    public void existsByEmail_checks() {
        // given
        Client c = createClient("loginE", "email@example.com", "3", false, false, null);

        assertThat(clientRepository.existsByEmail("email@example.com")).isTrue();
        assertThat(clientRepository.existsByEmailAndDeletedFalse("email@example.com")).isTrue();

        c.setDeleted(true);
        clientRepository.save(c);

        assertThat(clientRepository.existsByEmail("email@example.com")).isTrue();
        assertThat(clientRepository.existsByEmailAndDeletedFalse("email@example.com")).isFalse();
    }

    @Test
    @DisplayName("getClientByLogin returns only non-banned non-deleted clients")
    public void getClientByLogin_checksBannedAndDeleted() {
        // given
        Client ok = createClient("oklogin", "ok@example.com", "5", false, false, null);
        Client banned = createClient("banlogin", "ban@example.com", "6", false, true, null);
        Client deleted = createClient("dellogin", "del@example.com", "7", true, false, null);

        // when
        Client foundOk = clientRepository.getClientByLogin("oklogin").orElse(null);
        Client foundBanned = clientRepository.getClientByLogin("banlogin").orElse(null);
        Client foundDeleted = clientRepository.getClientByLogin("dellogin").orElse(null);

        // then
        assertThat(foundOk).isNotNull();
        assertThat(foundBanned).isNull();
        assertThat(foundDeleted).isNull();
    }

    @Test
    @DisplayName("findByFilter filters by banned and roleName")
    public void findByFilter_filters() {
        // given
        Role rAdmin = createRole("ADMIN");
        Role rUser = createRole("USER");

        createClient("r1", "r1@example.com", "10", false, false, rAdmin);
        createClient("r2", "r2@example.com", "11", false, false, rUser);
        createClient("r3", "r3@example.com", "12", false, true, rAdmin);

        // when: filter banned = false -> should return r1 and r2 (not r3)
        Page<Client> notBanned = clientRepository.findByFilter(false, null, pageable);
        // when: filter role ADMIN and banned null -> should return r1 and r3 (both ADMIN)
        Page<Client> adminAll = clientRepository.findByFilter(null, "ADMIN", pageable);

        // then
        assertThat(notBanned.getContent()).hasSize(2);
        assertThat(adminAll.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("existsByPhoneAndIdNot works as expected")
    public void existsByPhoneAndIdNot_checks() {
        // given
        Client c1 = createClient("ph1", "ph1@example.com", "555", false, false, null);
        Client c2 = createClient("ph2", "ph2@example.com", "666", false, false, null);

        // when
        boolean existsForOther = clientRepository.existsByPhoneAndIdNot("555", c2.getId());
        boolean existsForSame = clientRepository.existsByPhoneAndIdNot("555", c1.getId());

        // then
        assertThat(existsForOther).isTrue();
        assertThat(existsForSame).isFalse();
    }

    @Test
    @DisplayName("existsByEmail handles null, empty, and invalid email gracefully")
    public void existsByEmail_handlesEdgeCases() {
        // when & then
        assertThat(clientRepository.existsByEmail(null)).isFalse();
        assertThat(clientRepository.existsByEmail("")).isFalse();
        assertThat(clientRepository.existsByEmail("   ")).isFalse();
        assertThat(clientRepository.existsByEmail("not-an-email")).isFalse();
    }

    @Test
    @DisplayName("findByFilter correctly applies both banned=false and roleName=ADMIN filters")
    public void findByFilter_combinedBannedAndRoleFilters() {
        // given
        Role admin = createRole("ADMIN");
        Role user = createRole("USER");

        Client activeAdmin = createClient("activeAdmin", "a1@example.com", "1", false, false, admin);   // should match
        createClient("bannedAdmin", "a2@example.com", "2", false, true, admin);                        // should not match
        createClient("activeUser", "u1@example.com", "3", false, false, user);                         // should not match

        // when
        Page<Client> result = clientRepository.findByFilter(false, "ADMIN", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLogin()).isEqualTo("activeAdmin");
    }

    @Test
    @DisplayName("findByFilter returns banned clients when banned=true")
    public void findByFilter_returnsBannedClientsWhenRequested() {
        // given
        Role dummyRole = createRole("DUMMY");
        createClient("ok", "ok@example.com", "1", false, false, dummyRole);
        createClient("banned", "ban@example.com", "2", false, true, dummyRole);

        // when
        Page<Client> bannedOnly = clientRepository.findByFilter(true, null, pageable);

        // then
        assertThat(bannedOnly.getContent()).hasSize(1);
        assertThat(bannedOnly.getContent().get(0).getLogin()).isEqualTo("banned");
    }

    @Test
    @DisplayName("findByFilter returns empty page when no results match")
    public void findByFilter_returnsEmptyPageWhenNoMatches() {
        // when
        Page<Client> empty = clientRepository.findByFilter(false, "NONEXISTENT_ROLE", pageable);

        // then
        assertThat(empty.getContent()).isEmpty();
        assertThat(empty.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("getClientByLogin handles null and empty login correctly")
    public void getClientByLogin_handlesNullAndEmptyLogin() {
        // when & then
        assertThat(clientRepository.getClientByLogin(null)).isEmpty();
        assertThat(clientRepository.getClientByLogin("")).isEmpty();
    }

    @Test
    @DisplayName("existByLogin works correctly with special characters in login")
    public void existByLogin_handlesSpecialCharactersInLogin() {
        // given
        createClient("user'name", "test1@example.com", "123", false, false, null);
        createClient("user\"name", "test2@example.com", "456", false, false, null);
        createClient("user name", "test3@example.com", "789", false, false, null);

        // when & then
        assertThat(clientRepository.existByLogin("user'name")).isTrue();
        assertThat(clientRepository.existByLogin("user\"name")).isTrue();
        assertThat(clientRepository.existByLogin("user name")).isTrue();

        // Ensure no unintended matches (e.g., due to injection)
        assertThat(clientRepository.existByLogin("nonexistent' OR '1'='1")).isFalse();
    }

    @Test
    @DisplayName("existsByPhoneAndIdNot handles edge cases correctly")
    public void existsByPhoneAndIdNot_handlesEdgeCases() {
        // given
        Client client = createClient("test", "t@example.com", "999", false, false, null);

        // when & then
        // Same ID — should not count as duplicate
        assertThat(clientRepository.existsByPhoneAndIdNot("999", client.getId())).isFalse();

        // Different (non-existent) ID — should detect duplicate phone
        assertThat(clientRepository.existsByPhoneAndIdNot("999", 999999L)).isTrue();

        // Null phone
        assertThat(clientRepository.existsByPhoneAndIdNot(null, client.getId())).isFalse();
        assertThat(clientRepository.existsByPhoneAndIdNot(null, 999999L)).isFalse();

        // Empty phone
        assertThat(clientRepository.existsByPhoneAndIdNot("", client.getId())).isFalse();
    }

    @Test
    @DisplayName("save client with duplicate active unique fields throws exception")
    public void saveClient_duplicateActiveUniqueFields_throws() {
        // given
        createClient("dupLogin", "dup@example.com", "1000", false, false, null);

        // when & then
        Client duplicate = Client.builder()
                .firstName("F")
                .lastName("L")
                .login("dupLogin")
                .password("pwd")
                .email("dup@example.com")
                .phone("1000")
                .deleted(false)
                .banned(false)
                .build();

        assertThatThrownBy(() -> clientRepository.saveAndFlush(duplicate))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("save client with duplicate unique field allowed when original is deleted")
    public void saveClient_duplicateButOriginalDeleted_allowed() {
        // given
        Client original = createClient("delLogin", "del@example.com", "2000", true, false, null);

        // when: try to insert new active client with same login/email/phone
        Client newClient = Client.builder()
                .firstName("F")
                .lastName("L")
                .login("delLogin")
                .password("pwd")
                .email("del@example.com")
                .phone("2000")
                .deleted(false)
                .banned(false)
                .build();

        // then: should be allowed because unique indexes are partial (is_deleted = false)
        Client saved = clientRepository.saveAndFlush(newClient);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
    }

    @ParameterizedTest(name = "banned={0}, role={1} -> expectedCount={2}")
    @CsvSource({
            "false, , 2",    // not banned, any role -> r1 and r2 in earlier test
            "true, , 1",     // banned true -> only banned
            "false, ADMIN, 1", // not banned and ADMIN -> only activeAdmin
            "false, USER, 1",  // not banned and USER -> activeUser
            "true, ADMIN, 1",  // banned and ADMIN -> bannedAdmin
            "true, USER, 0"    // banned and USER -> none
    })
    @DisplayName("parameterized findByFilter combinations")
    public void findByFilter_parameterized(String bannedStr, String roleName, int expectedCount) {
        // Setup roles and data - clear repository first
        clientRepository.deleteAll();
        roleRepository.deleteAll();

        Role admin = createRole("ADMIN");
        Role user = createRole("USER");

        // create data set
        createClient("r1", "r1@example.com", "10", false, false, admin); // active admin
        createClient("r2", "r2@example.com", "11", false, false, user);  // active user
        createClient("r3", "r3@example.com", "12", false, true, admin);  // banned admin

        Boolean banned = null;
        if (bannedStr != null && !bannedStr.isEmpty()) {
            banned = Boolean.valueOf(bannedStr);
        }

        Page<Client> page = clientRepository.findByFilter(banned, roleName == null || roleName.isEmpty() ? null : roleName, pageable);
        assertThat(page.getContent()).hasSize(expectedCount);
    }

    @Test
    @DisplayName("save client with duplicate login (active) throws exception")
    public void saveClient_duplicateLoginActive_throwsException() {
        // given
        createClient("existingLogin", "unique1@example.com", "1111", false, false, null);

        Client duplicate = Client.builder()
                .firstName("F")
                .lastName("L")
                .login("existingLogin") // duplicate login
                .password("pwd")
                .email("unique2@example.com")
                .phone("2222")
                .deleted(false)
                .banned(false)
                .build();

        // then
        assertThatThrownBy(() -> clientRepository.saveAndFlush(duplicate))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("login");
    }

    @Test
    @DisplayName("save client with duplicate email (active) throws exception")
    public void saveClient_duplicateEmailActive_throwsException() {
        // given
        createClient("uniqueLogin1", "existing@example.com", "1111", false, false, null);

        Client duplicate = Client.builder()
                .firstName("F")
                .lastName("L")
                .login("uniqueLogin2")
                .password("pwd")
                .email("existing@example.com") // duplicate email
                .phone("2222")
                .deleted(false)
                .banned(false)
                .build();

        // then
        assertThatThrownBy(() -> clientRepository.saveAndFlush(duplicate))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("save client with duplicate phone (active) throws exception")
    public void saveClient_duplicatePhoneActive_throwsException() {
        // given
        createClient("uniqueLogin1", "e1@example.com", "9999", false, false, null);

        Client duplicate = Client.builder()
                .firstName("F")
                .lastName("L")
                .login("uniqueLogin2")
                .password("pwd")
                .email("e2@example.com")
                .phone("9999") // duplicate phone
                .deleted(false)
                .banned(false)
                .build();

        // then
        assertThatThrownBy(() -> clientRepository.saveAndFlush(duplicate))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("phone");
    }

    @Test
    @DisplayName("save client with duplicate login allowed if original is deleted")
    public void saveClient_duplicateLoginOriginalDeleted_allowed() {
        // given
        createClient("deletedLogin", "e1@example.com", "1001", true, false, null); // deleted = true

        Client newClient = Client.builder()
                .firstName("F")
                .lastName("L")
                .login("deletedLogin") // same login
                .password("pwd")
                .email("e2@example.com")
                .phone("1002")
                .deleted(false)
                .banned(false)
                .build();

        // when & then — should succeed
        Client saved = clientRepository.saveAndFlush(newClient);
        assertThat(saved).isNotNull();
        assertThat(saved.getLogin()).isEqualTo("deletedLogin");
    }

    @Test
    @DisplayName("save client with duplicate email allowed if original is deleted")
    public void saveClient_duplicateEmailOriginalDeleted_allowed() {
        // given
        createClient("l1", "deleted@example.com", "1003", true, false, null);

        Client newClient = Client.builder()
                .firstName("F")
                .lastName("L")
                .login("l2")
                .password("pwd")
                .email("deleted@example.com") // same email
                .phone("1004")
                .deleted(false)
                .banned(false)
                .build();

        // when & then
        Client saved = clientRepository.saveAndFlush(newClient);
        assertThat(saved).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("deleted@example.com");
    }

    @Test
    @DisplayName("save client with duplicate phone allowed if original is deleted")
    public void saveClient_duplicatePhoneOriginalDeleted_allowed() {
        // given
        createClient("l3", "e3@example.com", "8888", true, false, null);

        Client newClient = Client.builder()
                .firstName("F")
                .lastName("L")
                .login("l4")
                .password("pwd")
                .email("e4@example.com")
                .phone("8888") // same phone
                .deleted(false)
                .banned(false)
                .build();

        // when & then
        Client saved = clientRepository.saveAndFlush(newClient);
        assertThat(saved).isNotNull();
        assertThat(saved.getPhone()).isEqualTo("8888");
    }

    @Test
    @DisplayName("save client with duplicate login fails even if original is banned but NOT deleted")
    public void saveClient_duplicateLoginBannedButNotDeleted_throwsException() {
        // given: banned but active (deleted = false)
        createClient("bannedLogin", "b@example.com", "7777", false, true, null);

        Client duplicate = Client.builder()
                .firstName("F")
                .lastName("L")
                .login("bannedLogin") // same login
                .password("pwd")
                .email("b2@example.com")
                .phone("7778")
                .deleted(false)
                .banned(false)
                .build();

        // then — should still conflict (banned ≠ deleted)
        assertThatThrownBy(() -> clientRepository.saveAndFlush(duplicate))
                .isInstanceOf(Exception.class);
    }



    /**
     * Проверяет устойчивость метода {@code existByLogin} к SQL-инъекциям.
     * <p>
     * Особое внимание — попыткам обхода через комментарии ({@code 'legitUser'--}),
     * которые могут обмануть незащищённые реализации.
     * <p>
     * После атак подтверждается работоспособность легитимного логина.
     */
    @Test
    @DisplayName("existByLogin is safe against SQL injection")
    void existByLogin_sqlInjectionAttempts_returnFalse() {
        // given
        createClient("legitUser", "user@example.com", "123", false, false, null);

        List<String> payloads = Arrays.asList(
                "' OR '1'='1",
                "'; DROP TABLE client; --",
                "legitUser'--",
                "legitUser' OR 'x'='x",
                "\" OR \"\"=\"",
                "*/ UNION SELECT * FROM client; /*",
                "\\'; DELETE FROM client; --",
                "x' AND 1=1; --",
                "/*",
                "1' UNION SELECT null--"
        );

        // when & then
        for (String payload : payloads) {
            boolean result = clientRepository.existByLogin(payload);
            assertThat(result)
                    .as("existByLogin should return false for payload: %s", payload)
                    .isFalse();
        }

        // Проверяем, что легитимный логин всё ещё работает
        assertThat(clientRepository.existByLogin("legitUser")).isTrue();
    }

    /**
     * Валидирует безопасность JPQL-запроса в {@code getClientByLogin}.
     * <p>
     * JPQL по своей природе устойчив к SQL-инъекциям при использовании параметризованных выражений
     * (а не конкатенации строк). Однако если разработчик ошибочно использует {@code entityManager.createQuery("... " + param)},
     * уязвимость возможна. Данный тест подтверждает, что реализация корректна:
     * злонамеренные строки интерпретируются как литералы, а не как часть запроса.
     * <p>
     * Проверяются как классические, так и специфичные для JPQL/SQL векторы.
     * Легитимный логин продолжает работать.
     */
    @Test
    @DisplayName("getClientByLogin (JPQL) is safe against SQL injection")
    void getClientByLogin_jpql_sqlInjectionAttempts_returnEmpty() {
        // given
        createClient("activeUser", "a@example.com", "123", false, false, null);

        List<String> payloads = Arrays.asList(
                "' OR '1'='1",
                "activeUser'--",
                "activeUser' OR 'x'='x",
                "*/ SELECT * FROM client; /*",
                "\\'; DROP DATABASE; --"
        );

        // when & then
        for (String payload : payloads) {
            Optional<Client> result = clientRepository.getClientByLogin(payload);
            assertThat(result)
                    .as("getClientByLogin should return empty for payload: %s", payload)
                    .isEmpty();
        }

        // Легитимный логин должен работать
        assertThat(clientRepository.getClientByLogin("activeUser")).isPresent();
    }

    /**
     * Тестирует параметр {@code roleName} в методе {@code findByFilter} на устойчивость к SQL-инъекциям.
     * <p>
     * Фильтрация по роли — частый сценарий с пользовательским вводом (например, из UI-фильтра).
     * Поскольку роль передаётся как {@code String}, она потенциально уязвима, если используется
     * динамический SQL без параметризации.
     * <p>
     * Тест подаёт атаки в поле роли и убеждается, что:
     * <ul>
     *   <li>Нет возврата клиентов при поддельных ролях;</li>
     *   <li>Запрос не ломается и не выполняет UNION/DROP;</li>
     *   <li>Легитимная фильтрация по "USER" работает.</li>
     * </ul>
     */
    @Test
    @DisplayName("findByFilter (roleName parameter) is safe against SQL injection")
    void findByFilter_roleName_sqlInjectionAttempts_returnEmpty() {
        // given
        Role userRole = createRole("USER");
        createClient("c1", "c1@example.com", "1", false, false, userRole);

        List<String> payloads = Arrays.asList(
                "' OR '1'='1",
                "USER'--",
                "USER' OR 'x'='x",
                "*/ UNION SELECT * FROM roles; /*"
        );

        // when & then
        for (String payload : payloads) {
            Page<Client> result = clientRepository.findByFilter(null, payload, pageable);
            // Должен вернуть пусто, потому что нет роли с именем "' OR '1'='1"
            assertThat(result.getContent())
                    .as("findByFilter should return empty for roleName payload: %s", payload)
                    .isEmpty();
        }

        // Проверяем, что нормальный фильтр работает
        Page<Client> validResult = clientRepository.findByFilter(null, "USER", pageable);
        assertThat(validResult.getContent()).hasSize(1);
    }

    /**
     * Подтверждает безопасность автоматически сгенерированных методов:
     * {@code existsByEmail} и {@code existsByLoginAndDeletedFalse}.
     * <p>
     * Spring Data JPA генерирует такие методы на основе именования и использует
     * параметризованные JPQL- или Criteria-запросы. Однако доверие без проверки — риск.
     * Тест явно валидирует, что:
     * <ul>
     *   <li>Строковые параметры (email, login) не интерпретируются как SQL;</li>
     *   <li>Даже при попытках обхода через UNION или комментарии — результат {@code false};</li>
     *   <li>Легитимные значения продолжают работать корректно.</li>
     * </ul>
     * <p>
     * Это часть аудита: даже "магические" методы Spring требуют верификации в security-critical контексте.
     */
    @Test
    @DisplayName("existsByEmail and existsByLoginAndDeletedFalse are safe against SQL injection")
    void autoGeneratedExistsMethods_sqlInjectionAttempts_returnFalse() {
        // given
        createClient("testUser", "real@example.com", "123", false, false, null);

        List<String> emailPayloads = Arrays.asList(
                "' OR '1'='1' --",
                "real@example.com'--",
                "hacker' UNION SELECT 'admin@example.com"
        );

        List<String> loginPayloads = Arrays.asList(
                "' OR '1'='1",
                "testUser'--",
                "admin'/*"
        );

        // existsByEmail
        for (String payload : emailPayloads) {
            boolean result = clientRepository.existsByEmail(payload);
            assertThat(result).isFalse();
        }

        // existsByLoginAndDeletedFalse
        for (String payload : loginPayloads) {
            boolean result = clientRepository.existsByLoginAndDeletedFalse(payload);
            assertThat(result).isFalse();
        }

        // Проверяем легитимные значения
        assertThat(clientRepository.existsByEmail("real@example.com")).isTrue();
        assertThat(clientRepository.existsByLoginAndDeletedFalse("testUser")).isTrue();
    }
    /**
     * Проверяет уязвимость только в строковом параметре {@code phone} метода {@code existsByPhoneAndIdNot}.
     * <p>
     * Параметр {@code id} имеет тип {@code Long} → безопасен по конструкции (см. объяснение ниже).
     * Параметр {@code phone}, напротив, — строка от пользователя и может содержать вредоносный ввод.
     * <p>
     * Тест фокусируется исключительно на {@code phone}, подавая в него известные векторы атак,
     * и убеждается, что:
     * <ul>
     *   <li>Результат {@code false} для всех злонамеренных значений;</li>
     *   <li>Нормальная логика (исключение по ID) сохраняется.</li>
     * </ul>
     * <p>
     * Почему {@code id} не тестируется:
     * <ul>
     *   <li>Тип {@code Long} не позволяет внедрить SQL-синтаксис;</li>
     *   <li>Spring парсит строку в число до вызова репозитория;</li>
     *   <li>Ошибка парсинга → исключение, а не инъекция.</li>
     * </ul>
     */
    @Test
    @DisplayName("existsByPhoneAndIdNot is safe against SQL injection in phone parameter")
    void existsByPhoneAndIdNot_phone_sqlInjectionAttempts_returnFalse() {
        // given
        Client c = createClient("p1", "p1@example.com", "5555", false, false, null);

        List<String> phonePayloads = Arrays.asList(
                "' OR '1'='1",
                "5555'--",
                "*/ SELECT * FROM client; /*",
                "\\'; DROP TABLE; --"
        );

        // when & then
        for (String payload : phonePayloads) {
            boolean result = clientRepository.existsByPhoneAndIdNot(payload, c.getId() + 100); // другой ID
            assertThat(result)
                    .as("existsByPhoneAndIdNot should return false for phone payload: %s", payload)
                    .isFalse();
        }

        // Проверяем нормальную работу
        assertThat(clientRepository.existsByPhoneAndIdNot("5555", c.getId())).isFalse(); // тот же ID → false
        assertThat(clientRepository.existsByPhoneAndIdNot("5555", 999L)).isTrue();       // другой ID → true
    }




}
