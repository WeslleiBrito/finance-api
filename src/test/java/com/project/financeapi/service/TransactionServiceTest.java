package com.project.financeapi.service;

import com.project.financeapi.dto.transaction.*;
import com.project.financeapi.entity.*;
import com.project.financeapi.entity.account.CheckingAccount;
import com.project.financeapi.entity.base.AccountBase;
import com.project.financeapi.entity.base.PaymentInstrumentBase;
import com.project.financeapi.enumSystem.*;
import com.project.financeapi.exception.BusinessException;
import com.project.financeapi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private InstallmentRepository installmentRepository;
    @Mock private PaymentInstrumentRepository paymentInstrumentRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserContextService userContextService;

    private TransactionService service;

    private User user;
    private String userId;

    @BeforeEach
    void setUp() {
        service = new TransactionService(
                accountRepository, installmentRepository,
                paymentInstrumentRepository, transactionRepository, userContextService
        );

        userId = UUID.randomUUID().toString();
        user = mock(User.class);
        lenient().when(user.getId()).thenReturn(userId);
        when(userContextService.getAuthenticatedUser()).thenReturn(user);
    }


    // ================================================================
    // HELPERS
    // ================================================================

    private CheckingAccount mockCheckingAccount(BigDecimal balance, BigDecimal limit) {
        CheckingAccount account = mock(CheckingAccount.class);
        lenient().when(account.getId()).thenReturn(UUID.randomUUID());
        lenient().when(account.getAccountHolder()).thenReturn(user);
        lenient().when(account.getType()).thenReturn(AccountType.CHECKING);
        lenient().when(account.getBalance()).thenReturn(balance);
        lenient().when(account.getOverdraftLimit()).thenReturn(limit); // ajuste conforme os getters reais
        return account;
    }



    private AccountBase mockWalletAccount(BigDecimal balance) {
        AccountBase account = mock(AccountBase.class);
        lenient().when(account.getId()).thenReturn(UUID.randomUUID());
        lenient().when(account.getAccountHolder()).thenReturn(user);
        lenient().when(account.getType()).thenReturn(AccountType.WALLET);
        lenient().when(account.getBalance()).thenReturn(balance);
        return account;
    }



    private Installment mockInstallment(User owner, MovementDirection direction,
                                        BigDecimal amount, BigDecimal totalPaid,
                                        PaymentInstrumentBase instrument) {
        Installment installment = mock(Installment.class);
        Invoice invoice = mock(Invoice.class);
        lenient().when(invoice.getCreatedBy()).thenReturn(owner);
        lenient().when(installment.getId()).thenReturn(UUID.randomUUID());
        lenient().when(installment.getInvoice()).thenReturn(invoice);
        lenient().when(installment.getMovementDirection()).thenReturn(direction);
        lenient().when(installment.getMovementType()).thenReturn(MovementType.PAYMENT);
        lenient().when(installment.getAmount()).thenReturn(amount);
        lenient().when(installment.getTotalPaid()).thenReturn(totalPaid);
        lenient().when(installment.getPaymentInstrument()).thenReturn(instrument);
        return installment;
    }


    private PaymentInstrumentBase mockInstrument(InstrumentNature nature, PaymentType type) {
        PaymentInstrumentBase instrument = mock(PaymentInstrumentBase.class);
        when(instrument.getId()).thenReturn(UUID.randomUUID());
        lenient().when(instrument.getInstrumentNature()).thenReturn(nature);
        lenient().when(instrument.getPaymentType()).thenReturn(type);
        return instrument;
    }

    // ================================================================
    // createCommonTransactions
    // ================================================================

    @Nested
    class CreateCommonTransactions {

        @Test
        void deveLancarNotFound_quandoParcelaNaoExistir() {
            UUID installmentId = UUID.randomUUID();
            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.TEN, LocalDate.now(), "obs",
                    installmentId, UUID.randomUUID(), null, null, null, null
            );
            when(installmentRepository.findByIdForUpdate(installmentId)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createCommonTransactions(new CreateTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.NOT_FOUND, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("Parcela não encontrada"));
        }

        @Test
        void deveLancarForbidden_quandoParcelaNaoPertencerAoUsuario() {
            User outroUsuario = mock(User.class);
            when(outroUsuario.getId()).thenReturn(UUID.randomUUID().toString());
            Installment installment = mockInstallment(outroUsuario, MovementDirection.OUTFLOW,
                    BigDecimal.valueOf(100), BigDecimal.ZERO, null);

            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.TEN, LocalDate.now(), "obs",
                    installment.getId(), UUID.randomUUID(), null, null, null, null
            );
            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createCommonTransactions(new CreateTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.FORBIDDEN, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("Parcela não pertence"));
        }

        @Test
        void deveLancarNotFound_quandoContaNaoExistir() {
            Installment installment = mockInstallment(user, MovementDirection.OUTFLOW,
                    BigDecimal.valueOf(100), BigDecimal.ZERO, null);
            UUID accountId = UUID.randomUUID();

            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.TEN, LocalDate.now(), "obs",
                    installment.getId(), accountId, null, null, null, null
            );
            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));
            when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createCommonTransactions(new CreateTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.NOT_FOUND, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("Conta não encontrada"));
        }

        @Test
        void deveLancarForbidden_quandoContaNaoPertencerAoUsuario() {
            User outroUsuario = mock(User.class);
            when(outroUsuario.getId()).thenReturn(UUID.randomUUID().toString());

            AccountBase account = mock(AccountBase.class);
            when(account.getAccountHolder()).thenReturn(outroUsuario);
            UUID accountId = UUID.randomUUID();
            when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createInvestmentTransaction(
                            accountId, BigDecimal.TEN, MovementDirection.OUTFLOW, LocalDate.now(), "obs"));

            assertEquals(HttpStatus.FORBIDDEN, ex.getErrorCode());
        }



        @Test
        void deveLancarNotFound_quandoInstrumentoNaoExistir() {
            Installment installment = mockInstallment(user, MovementDirection.OUTFLOW,
                    BigDecimal.valueOf(100), BigDecimal.ZERO, null);
            AccountBase account = mockWalletAccount(BigDecimal.valueOf(1000));
            UUID instrumentId = UUID.randomUUID();

            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.TEN, LocalDate.now(), "obs",
                    installment.getId(), account.getId(), instrumentId, null, null, null
            );
            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));
            when(accountRepository.findByIdForUpdate(account.getId()))
                    .thenReturn(Optional.of(account));
            when(paymentInstrumentRepository.findByIdAndUser(instrumentId, userId))
                    .thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createCommonTransactions(new CreateTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.NOT_FOUND, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("Instrumento de pagamento não encontrado"));
        }

        @Test
        void deveLancarBadRequest_quandoInstrumentoForPurchase() {
            Installment installment = mockInstallment(user, MovementDirection.OUTFLOW,
                    BigDecimal.valueOf(100), BigDecimal.ZERO, null);
            AccountBase account = mockWalletAccount(BigDecimal.valueOf(1000));
            PaymentInstrumentBase instrument = mockInstrument(InstrumentNature.PURCHASE, PaymentType.CASH);

            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.TEN, LocalDate.now(), "obs",
                    installment.getId(), account.getId(), instrument.getId(), null, null, null
            );
            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));
            when(accountRepository.findByIdForUpdate(account.getId()))
                    .thenReturn(Optional.of(account));
            when(paymentInstrumentRepository.findByIdAndUser(instrument.getId(), userId))
                    .thenReturn(Optional.of(instrument));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createCommonTransactions(new CreateTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("PURCHASE"));
        }

        @Test
        void deveLancarBadRequest_quandoContaWalletComInstrumentoNaoCash() {
            Installment installment = mockInstallment(user, MovementDirection.OUTFLOW,
                    BigDecimal.valueOf(100), BigDecimal.ZERO, null);
            AccountBase account = mockWalletAccount(BigDecimal.valueOf(1000));
            PaymentInstrumentBase instrument = mockInstrument(InstrumentNature.PAYMENT, PaymentType.CREDIT_CARD);

            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.TEN, LocalDate.now(), "obs",
                    installment.getId(), account.getId(), instrument.getId(), null, null, null
            );
            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));
            when(accountRepository.findByIdForUpdate(account.getId()))
                    .thenReturn(Optional.of(account));
            when(paymentInstrumentRepository.findByIdAndUser(instrument.getId(), userId))
                    .thenReturn(Optional.of(instrument));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createCommonTransactions(new CreateTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("WALLET"));
        }

        @Test
        void deveLancarBadRequest_quandoContaNaoWalletComInstrumentoCash() {
            Installment installment = mockInstallment(user, MovementDirection.OUTFLOW,
                    BigDecimal.valueOf(100), BigDecimal.ZERO, null);
            CheckingAccount account = mockCheckingAccount(BigDecimal.valueOf(1000), BigDecimal.ZERO);
            PaymentInstrumentBase instrument = mockInstrument(InstrumentNature.PAYMENT, PaymentType.CASH);

            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.TEN, LocalDate.now(), "obs",
                    installment.getId(), account.getId(), instrument.getId(), null, null, null
            );
            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));
            when(accountRepository.findByIdForUpdate(account.getId()))
                    .thenReturn(Optional.of(account));
            when(paymentInstrumentRepository.findByIdAndUser(instrument.getId(), userId))
                    .thenReturn(Optional.of(instrument));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createCommonTransactions(new CreateTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("Instrumento CASH"));
        }

        @Test
        void deveLancarBadRequest_quandoDataForFutura() {
            Installment installment = mockInstallment(user, MovementDirection.OUTFLOW,
                    BigDecimal.valueOf(100), BigDecimal.ZERO, null);
            AccountBase account = mockWalletAccount(BigDecimal.valueOf(1000));

            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.TEN, LocalDate.now().plusDays(1), "obs",
                    installment.getId(), account.getId(), null, null, null, null
            );
            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));
            when(accountRepository.findByIdForUpdate(account.getId()))
                    .thenReturn(Optional.of(account));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createCommonTransactions(new CreateTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("futura"));
        }

        @Test
        void deveLancarBadRequest_quandoEffectiveAmountForZeroOuNegativo() {
            Installment installment = mockInstallment(user, MovementDirection.OUTFLOW,
                    BigDecimal.valueOf(100), BigDecimal.ZERO, null);
            AccountBase account = mockWalletAccount(BigDecimal.valueOf(1000));

            // principal 10 + juros 0 + multa 0 - desconto 20 = -10 (<=0)
            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.TEN, LocalDate.now(), "obs",
                    installment.getId(), account.getId(), null,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(20)
            );
            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));
            when(accountRepository.findByIdForUpdate(account.getId()))
                    .thenReturn(Optional.of(account));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createCommonTransactions(new CreateTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("Valor efetivo"));
        }

        @Test
        void deveLancarBadRequest_quandoPagamentoExcederValorOriginalDaParcela() {
            Installment installment = mockInstallment(user, MovementDirection.OUTFLOW,
                    BigDecimal.valueOf(100), BigDecimal.valueOf(90), null); // já pago 90 de 100
            AccountBase account = mockWalletAccount(BigDecimal.valueOf(1000));

            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.valueOf(20), LocalDate.now(), "obs", // 90 + 20 = 110 > 100
                    installment.getId(), account.getId(), null,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );
            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));
            when(accountRepository.findByIdForUpdate(account.getId()))
                    .thenReturn(Optional.of(account));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createCommonTransactions(new CreateTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("excede o valor original"));
        }

        @Test
        void deveLancarBadRequest_quandoSaldoInsuficienteNaConta() {
            Installment installment = mockInstallment(user, MovementDirection.OUTFLOW,
                    BigDecimal.valueOf(1000), BigDecimal.ZERO, null);
            CheckingAccount account = mockCheckingAccount(BigDecimal.valueOf(50), BigDecimal.ZERO);
            when(account.getName()).thenReturn("Conta Teste");

            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.valueOf(100), LocalDate.now(), "obs", // supera saldo(50)+limite(0)
                    installment.getId(), account.getId(), null,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );
            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));
            when(accountRepository.findByIdForUpdate(account.getId()))
                    .thenReturn(Optional.of(account));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createCommonTransactions(new CreateTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("Saldo insuficiente"));
        }

        @Test
        void deveCriarTransacaoComSucesso_semCartao() {
            Installment installment = mockInstallment(user, MovementDirection.OUTFLOW,
                    BigDecimal.valueOf(100), BigDecimal.ZERO, null);
            CheckingAccount account = mockCheckingAccount(BigDecimal.valueOf(1000), BigDecimal.ZERO);

            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.valueOf(50), LocalDate.now(), "pagamento",
                    installment.getId(), account.getId(), null,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );
            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));
            when(accountRepository.findByIdForUpdate(account.getId()))
                    .thenReturn(Optional.of(account));
            when(transactionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            List<TransactionResponseDTO> result = service.createCommonTransactions(
                    new CreateTransactionRequestDTO(List.of(dto)));

            assertEquals(1, result.size());
            verify(paymentInstrumentRepository, never()).save(any());
        }

        @Test
        void deveLiberarLimiteDoCartao_quandoInstalmentForCartaoDeCredito() {
            CreditCard card = mock(CreditCard.class);
            Installment installment = mockInstallment(user, MovementDirection.OUTFLOW,
                    BigDecimal.valueOf(100), BigDecimal.ZERO, card);
            CheckingAccount account = mockCheckingAccount(BigDecimal.valueOf(1000), BigDecimal.ZERO);

            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.valueOf(100), LocalDate.now(), "pagamento fatura",
                    installment.getId(), account.getId(), null,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );
            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));
            when(accountRepository.findByIdForUpdate(account.getId()))
                    .thenReturn(Optional.of(account));
            when(transactionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            service.createCommonTransactions(new CreateTransactionRequestDTO(List.of(dto)));

            verify(card).freeUpLimit(BigDecimal.valueOf(100));
            verify(paymentInstrumentRepository).save(card);
        }

        @Test
        void naoDeveValidarSaldo_quandoMovementDirectionForInflow() {
            // Recebimento não deduz saldo -> não deve nem chamar validação de fundos
            Installment installment = mockInstallment(user, MovementDirection.INFLOW,
                    BigDecimal.valueOf(100), BigDecimal.ZERO, null);
            AccountBase account = mockWalletAccount(BigDecimal.ZERO); // saldo zero, mas é INFLOW

            CreateTransactionDTO dto = new CreateTransactionDTO(
                    BigDecimal.valueOf(50), LocalDate.now(), "recebimento",
                    installment.getId(), account.getId(), null,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );
            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));
            when(accountRepository.findByIdForUpdate(account.getId()))
                    .thenReturn(Optional.of(account));
            when(transactionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> service.createCommonTransactions(
                    new CreateTransactionRequestDTO(List.of(dto))));
        }

        @Test
        void deveAcumularCorretamente_quandoMultiplasTransacoesMesmaParcelaEConta() {
            Installment installment = mockInstallment(user, MovementDirection.OUTFLOW,
                    BigDecimal.valueOf(100), BigDecimal.ZERO, null);
            CheckingAccount account = mockCheckingAccount(BigDecimal.valueOf(1000), BigDecimal.ZERO);

            CreateTransactionDTO dto1 = new CreateTransactionDTO(
                    BigDecimal.valueOf(40), LocalDate.now(), "parte1",
                    installment.getId(), account.getId(), null,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );
            CreateTransactionDTO dto2 = new CreateTransactionDTO(
                    BigDecimal.valueOf(60), LocalDate.now(), "parte2",
                    installment.getId(), account.getId(), null,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            );

            when(installmentRepository.findByIdForUpdate(installment.getId()))
                    .thenReturn(Optional.of(installment));
            when(accountRepository.findByIdForUpdate(account.getId()))
                    .thenReturn(Optional.of(account));
            when(transactionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            // 40 + 60 = 100 -> bate exatamente com o valor da parcela, não deve lançar
            assertDoesNotThrow(() -> service.createCommonTransactions(
                    new CreateTransactionRequestDTO(List.of(dto1, dto2))));
        }
    }

    // ================================================================
    // reverseTransaction
    // ================================================================

    @Nested
    class ReverseTransaction {

        @Test
        void deveLancarNotFound_quandoTransacaoNaoExistir() {
            UUID txId = UUID.randomUUID();
            when(transactionRepository.findByIdAndUserId(txId, userId)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reverseTransaction(txId, null));

            assertEquals(HttpStatus.NOT_FOUND, ex.getErrorCode());
        }

        @Test
        void deveLancarBadRequest_quandoTransacaoJaForEstorno() {
            Transaction original = mock(Transaction.class);
            UUID txId = UUID.randomUUID();
            when(original.getMovementType()).thenReturn(MovementType.REVERSAL);
            when(transactionRepository.findByIdAndUserId(txId, userId)).thenReturn(Optional.of(original));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reverseTransaction(txId, null));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("já é um estorno"));
        }

        @Test
        void deveLancarBadRequest_quandoJaEstornadaViaFlag() {
            Transaction original = mock(Transaction.class);
            UUID txId = UUID.randomUUID();
            when(original.getId()).thenReturn(txId);
            when(original.getMovementType()).thenReturn(MovementType.PAYMENT);
            when(original.isReversed()).thenReturn(true);
            when(transactionRepository.findByIdAndUserId(txId, userId)).thenReturn(Optional.of(original));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reverseTransaction(txId, null));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("já foi estornada"));
        }

        @Test
        void deveLancarBadRequest_quandoJaExisteRegistroDeEstorno() {
            Transaction original = mock(Transaction.class);
            UUID txId = UUID.randomUUID();
            when(original.getId()).thenReturn(txId);
            when(transactionRepository.findByIdAndUserId(txId, userId)).thenReturn(Optional.of(original));
            when(transactionRepository.existsByReversalOfId(txId)).thenReturn(true);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reverseTransaction(txId, null));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
        }



        @Test
        void deveUsarMotivoPadrao_quandoDtoForNulo() {
            Transaction original = mock(Transaction.class);
            UUID txId = UUID.randomUUID();
            AccountBase account = mockWalletAccount(BigDecimal.valueOf(1000));

            when(original.getId()).thenReturn(txId);
            when(original.getMovementType()).thenReturn(MovementType.PAYMENT);
            when(original.isReversed()).thenReturn(false);
            when(original.getMovementDirection()).thenReturn(MovementDirection.OUTFLOW);
            when(original.getAccount()).thenReturn(account);
            when(original.getAmount()).thenReturn(BigDecimal.TEN);
            when(original.getInterest()).thenReturn(BigDecimal.ZERO);
            when(original.getFine()).thenReturn(BigDecimal.ZERO);
            when(original.getDiscount()).thenReturn(BigDecimal.ZERO);
            when(original.getInstallment()).thenReturn(null);
            when(original.getPaymentInstrument()).thenReturn(null);

            when(transactionRepository.findByIdAndUserId(txId, userId)).thenReturn(Optional.of(original));
            when(transactionRepository.existsByReversalOfId(txId)).thenReturn(false);
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TransactionResponseDTO response = service.reverseTransaction(txId, null);

            assertNotNull(response);
            verify(original).setReversed(true);
        }

        @Test
        void deveLancarBadRequest_quandoEstornarInflowSemSaldoSuficiente() {
            Transaction original = mock(Transaction.class);
            UUID txId = UUID.randomUUID();
            CheckingAccount account = mockCheckingAccount(BigDecimal.valueOf(5), BigDecimal.ZERO);

            when(original.getId()).thenReturn(txId);
            when(original.getMovementType()).thenReturn(MovementType.PAYMENT);
            when(original.isReversed()).thenReturn(false);
            when(original.getMovementDirection()).thenReturn(MovementDirection.INFLOW);
            when(original.getAccount()).thenReturn(account);
            when(original.getAmount()).thenReturn(BigDecimal.valueOf(100)); // > saldo(5)+limite(0)

            when(transactionRepository.findByIdAndUserId(txId, userId)).thenReturn(Optional.of(original));
            when(transactionRepository.existsByReversalOfId(txId)).thenReturn(false);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reverseTransaction(txId, null));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("Saldo insuficiente"));
        }


        @Test
        void deveConsumirLimiteDoCartao_quandoEstornarPagamentoDeParcelaEmCartao() {
            Transaction original = mock(Transaction.class);
            Installment installment = mock(Installment.class);
            CreditCard card = mock(CreditCard.class);
            UUID txId = UUID.randomUUID();
            AccountBase account = mockWalletAccount(BigDecimal.valueOf(1000));

            when(original.getId()).thenReturn(txId);
            when(original.getMovementType()).thenReturn(MovementType.PAYMENT);
            when(original.isReversed()).thenReturn(false);
            when(original.getMovementDirection()).thenReturn(MovementDirection.OUTFLOW);
            when(original.getAccount()).thenReturn(account);
            when(original.getAmount()).thenReturn(BigDecimal.valueOf(50));
            when(original.getInterest()).thenReturn(BigDecimal.ZERO);
            when(original.getFine()).thenReturn(BigDecimal.ZERO);
            when(original.getDiscount()).thenReturn(BigDecimal.ZERO);
            when(original.getInstallment()).thenReturn(installment);
            when(installment.getPaymentInstrument()).thenReturn(card);
            when(original.getPaymentInstrument()).thenReturn(card);

            when(transactionRepository.findByIdAndUserId(txId, userId)).thenReturn(Optional.of(original));
            when(transactionRepository.existsByReversalOfId(txId)).thenReturn(false);
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.reverseTransaction(txId, new ReversalRequestDTO("motivo custom"));

            verify(card).consumeLimit(BigDecimal.valueOf(50));
            verify(paymentInstrumentRepository).save(card);
        }

        @Test
        void deveEstornarTransferenciaComParDuploVinculado() {
            Transaction original = mock(Transaction.class);
            Transaction sibling = mock(Transaction.class);
            UUID origId = UUID.randomUUID();
            UUID sibId = UUID.randomUUID();

            AccountBase sourceAccount = mockWalletAccount(BigDecimal.valueOf(1000));
            AccountBase destAccount = mockWalletAccount(BigDecimal.valueOf(1000));

            when(original.getId()).thenReturn(origId);
            when(original.getMovementType()).thenReturn(MovementType.TRANSFER);
            when(original.isReversed()).thenReturn(false);
            when(original.getLinkedTransaction()).thenReturn(sibling);
            when(original.getMovementDirection()).thenReturn(MovementDirection.OUTFLOW);
            when(original.getAccount()).thenReturn(sourceAccount);
            when(original.getAmount()).thenReturn(BigDecimal.valueOf(100));
            when(original.getInterest()).thenReturn(BigDecimal.ZERO);
            when(original.getFine()).thenReturn(BigDecimal.ZERO);
            when(original.getDiscount()).thenReturn(BigDecimal.ZERO);

            when(sibling.getId()).thenReturn(sibId);
            when(sibling.isReversed()).thenReturn(false);
            when(sibling.getMovementDirection()).thenReturn(MovementDirection.INFLOW);
            when(sibling.getAccount()).thenReturn(destAccount);
            when(sibling.getAmount()).thenReturn(BigDecimal.valueOf(100));
            when(sibling.getInterest()).thenReturn(BigDecimal.ZERO);
            when(sibling.getFine()).thenReturn(BigDecimal.ZERO);
            when(sibling.getDiscount()).thenReturn(BigDecimal.ZERO);

            when(transactionRepository.findByIdAndUserId(origId, userId)).thenReturn(Optional.of(original));
            when(transactionRepository.existsByReversalOfId(origId)).thenReturn(false);
            when(transactionRepository.existsByReversalOfId(sibId)).thenReturn(false);
            when(transactionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            TransactionResponseDTO response = service.reverseTransaction(origId, null);

            assertNotNull(response);
            verify(original).setReversed(true);
            verify(sibling).setReversed(true);
            verify(transactionRepository, times(2)).saveAll(anyList());
        }

        @Test
        void deveLancarBadRequest_quandoContraparteDaTransferenciaJaEstornada() {
            Transaction original = mock(Transaction.class);
            Transaction sibling = mock(Transaction.class);
            UUID origId = UUID.randomUUID();
            UUID sibId = UUID.randomUUID();

            when(original.getId()).thenReturn(origId);
            when(original.getMovementType()).thenReturn(MovementType.TRANSFER);
            when(original.isReversed()).thenReturn(false);
            when(original.getLinkedTransaction()).thenReturn(sibling);
            when(sibling.getId()).thenReturn(sibId);
            when(sibling.isReversed()).thenReturn(true);

            when(transactionRepository.findByIdAndUserId(origId, userId)).thenReturn(Optional.of(original));
            when(transactionRepository.existsByReversalOfId(origId)).thenReturn(false);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.reverseTransaction(origId, null));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("contraparte"));
        }
    }

    // ================================================================
    // createManualAdjustments
    // ================================================================

    @Nested
    class CreateManualAdjustments {

        @Test
        void deveLancarNotFound_quandoContaNaoExistir() {
            UUID accountId = UUID.randomUUID();
            CreateManualAdjustmentTransactionDTO dto = new CreateManualAdjustmentTransactionDTO(
                    BigDecimal.TEN, MovementDirection.INFLOW, LocalDate.now(), "motivo", accountId, null
            );
            when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createManualAdjustments(new CreateManualAdjustmentTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.NOT_FOUND, ex.getErrorCode());
        }

        @Test
        void deveLancarForbidden_quandoContaNaoPertencerAoUsuario() {
            User outroUsuario = mock(User.class);
            when(outroUsuario.getId()).thenReturn(UUID.randomUUID().toString()); // ⚠️ faltava

            AccountBase account = mock(AccountBase.class);
            when(account.getId()).thenReturn(UUID.randomUUID());
            when(account.getAccountHolder()).thenReturn(outroUsuario);

            CreateManualAdjustmentTransactionDTO dto = new CreateManualAdjustmentTransactionDTO(
                    BigDecimal.TEN, MovementDirection.INFLOW, LocalDate.now(), "motivo", account.getId(), null
            );
            when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createManualAdjustments(new CreateManualAdjustmentTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.FORBIDDEN, ex.getErrorCode());
        }


        @Test
        void deveLancarBadRequest_quandoDataForFutura() {
            AccountBase account = mockWalletAccount(BigDecimal.valueOf(1000));
            CreateManualAdjustmentTransactionDTO dto = new CreateManualAdjustmentTransactionDTO(
                    BigDecimal.TEN, MovementDirection.INFLOW, LocalDate.now().plusDays(1), "motivo", account.getId(), null
            );
            when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createManualAdjustments(new CreateManualAdjustmentTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("futura"));
        }

        @Test
        void deveLancarBadRequest_quandoAjusteOutflowSemSaldoSuficiente() {
            CheckingAccount account = mockCheckingAccount(BigDecimal.valueOf(10), BigDecimal.ZERO);

            CreateManualAdjustmentTransactionDTO dto = new CreateManualAdjustmentTransactionDTO(
                    BigDecimal.valueOf(100), MovementDirection.OUTFLOW, LocalDate.now(), "motivo", account.getId(), null
            );
            when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createManualAdjustments(new CreateManualAdjustmentTransactionRequestDTO(List.of(dto))));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
        }


        @Test
        void naoDeveValidarSaldo_quandoAjusteForInflow() {
            AccountBase account = mockWalletAccount(BigDecimal.ZERO);
            CreateManualAdjustmentTransactionDTO dto = new CreateManualAdjustmentTransactionDTO(
                    BigDecimal.valueOf(500), MovementDirection.INFLOW, LocalDate.now(), "motivo", account.getId(), null
            );
            when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));
            when(transactionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> service.createManualAdjustments(
                    new CreateManualAdjustmentTransactionRequestDTO(List.of(dto))));
        }

        @Test
        void deveCriarAjusteComSucesso() {
            CheckingAccount account = mockCheckingAccount(BigDecimal.valueOf(1000), BigDecimal.ZERO);
            CreateManualAdjustmentTransactionDTO dto = new CreateManualAdjustmentTransactionDTO(
                    BigDecimal.valueOf(50), MovementDirection.OUTFLOW, LocalDate.now(), "correção de saldo", account.getId(), null
            );
            when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));
            when(transactionRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            List<TransactionResponseDTO> result = service.createManualAdjustments(
                    new CreateManualAdjustmentTransactionRequestDTO(List.of(dto)));

            assertEquals(1, result.size());
        }

    }

    // ================================================================
    // transfer
    // ================================================================

    @Nested
    class Transfer {

        @Test
        void deveLancarBadRequest_quandoOrigemEDestinoForemIguais() {
            UUID mesmaConta = UUID.randomUUID();
            TransferRequestDTO request = new TransferRequestDTO(
                    mesmaConta, mesmaConta, BigDecimal.TEN, LocalDate.now(), "obs"
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> service.transfer(request));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("mesma"));
        }

        @Test
        void deveLancarBadRequest_quandoDataForFutura() {
            TransferRequestDTO request = new TransferRequestDTO(
                    UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN,
                    LocalDate.now().plusDays(1), "obs"
            );

            BusinessException ex = assertThrows(BusinessException.class, () -> service.transfer(request));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("futura"));
        }

        @Test
        void deveLancarNotFound_quandoContaOrigemNaoExistir() {
            UUID sourceId = UUID.randomUUID();
            TransferRequestDTO request = new TransferRequestDTO(
                    sourceId, UUID.randomUUID(), BigDecimal.TEN, LocalDate.now(), "obs"
            );
            when(accountRepository.findByIdForUpdate(sourceId)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class, () -> service.transfer(request));

            assertEquals(HttpStatus.NOT_FOUND, ex.getErrorCode());
        }

        @Test
        void deveLancarForbidden_quandoContaOrigemNaoPertencerAoUsuario() {
            User outroUsuario = mock(User.class);
            when(outroUsuario.getId()).thenReturn(UUID.randomUUID().toString());

            AccountBase source = mock(AccountBase.class);
            when(source.getAccountHolder()).thenReturn(outroUsuario); // ⚠️ faltava

            TransferRequestDTO request = new TransferRequestDTO(
                    UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, LocalDate.now(), "obs"
            );
            when(accountRepository.findByIdForUpdate(request.sourceAccountId())).thenReturn(Optional.of(source));

            BusinessException ex = assertThrows(BusinessException.class, () -> service.transfer(request));

            assertEquals(HttpStatus.FORBIDDEN, ex.getErrorCode());
        }

        @Test
        void deveLancarNotFound_quandoContaDestinoNaoExistir() {
            AccountBase source = mockWalletAccount(BigDecimal.valueOf(1000));
            UUID destId = UUID.randomUUID();

            TransferRequestDTO request = new TransferRequestDTO(
                    source.getId(), destId, BigDecimal.TEN, LocalDate.now(), "obs"
            );
            when(accountRepository.findByIdForUpdate(source.getId())).thenReturn(Optional.of(source));
            when(accountRepository.findByIdForUpdate(destId)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class, () -> service.transfer(request));

            assertEquals(HttpStatus.NOT_FOUND, ex.getErrorCode());
        }

        @Test
        void deveLancarForbidden_quandoContaDestinoNaoPertencerAoUsuario() {
            AccountBase contaOrigem = mock(AccountBase.class);
            when(contaOrigem.getAccountHolder()).thenReturn(user);

            User outroUsuario = mock(User.class);
            when(outroUsuario.getId()).thenReturn(UUID.randomUUID().toString());

            AccountBase contaDestino = mock(AccountBase.class);
            when(contaDestino.getAccountHolder()).thenReturn(outroUsuario);

            UUID origemId = UUID.randomUUID();
            UUID destinoId = UUID.randomUUID();
            when(accountRepository.findByIdForUpdate(origemId)).thenReturn(Optional.of(contaOrigem));
            when(accountRepository.findByIdForUpdate(destinoId)).thenReturn(Optional.of(contaDestino));

            TransferRequestDTO dto = new TransferRequestDTO(
                    origemId, destinoId, BigDecimal.TEN, LocalDate.now(), "obs"
            );

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.transfer(dto));

            assertEquals(HttpStatus.FORBIDDEN, ex.getErrorCode());
        }



        @Test
        void deveLancarBadRequest_quandoSaldoInsuficienteNaOrigem() {
            CheckingAccount source = mockCheckingAccount(BigDecimal.valueOf(5), BigDecimal.ZERO);
            AccountBase dest = mockWalletAccount(BigDecimal.valueOf(1000));

            TransferRequestDTO request = new TransferRequestDTO(
                    source.getId(), dest.getId(), BigDecimal.valueOf(100), LocalDate.now(), "obs"
            );
            when(accountRepository.findByIdForUpdate(source.getId())).thenReturn(Optional.of(source));
            when(accountRepository.findByIdForUpdate(dest.getId())).thenReturn(Optional.of(dest));

            BusinessException ex = assertThrows(BusinessException.class, () -> service.transfer(request));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
        }

        @Test
        void deveCriarTransferenciaComSucesso_comObservacaoPersonalizada() {
            CheckingAccount source = mockCheckingAccount(BigDecimal.valueOf(1000), BigDecimal.ZERO);
            when(source.getName()).thenReturn("Origem");
            AccountBase dest = mockWalletAccount(BigDecimal.valueOf(1000));
            when(dest.getName()).thenReturn("Destino");

            TransferRequestDTO request = new TransferRequestDTO(
                    source.getId(), dest.getId(), BigDecimal.valueOf(100), LocalDate.now(), "pagamento aluguel"
            );
            when(accountRepository.findByIdForUpdate(source.getId())).thenReturn(Optional.of(source));
            when(accountRepository.findByIdForUpdate(dest.getId())).thenReturn(Optional.of(dest));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            List<TransactionResponseDTO> result = service.transfer(request);

            assertEquals(2, result.size());
            verify(transactionRepository, times(3)).save(any());
        }

        @Test
        void deveUsarObservacaoPadrao_quandoObservacaoForNulaOuVazia() {
            CheckingAccount source = mockCheckingAccount(BigDecimal.valueOf(1000), BigDecimal.ZERO);
            when(source.getName()).thenReturn("Origem");
            AccountBase dest = mockWalletAccount(BigDecimal.valueOf(1000));
            when(dest.getName()).thenReturn("Destino");

            TransferRequestDTO request = new TransferRequestDTO(
                    source.getId(), dest.getId(), BigDecimal.valueOf(100), LocalDate.now(), "   "
            );
            when(accountRepository.findByIdForUpdate(source.getId())).thenReturn(Optional.of(source));
            when(accountRepository.findByIdForUpdate(dest.getId())).thenReturn(Optional.of(dest));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> service.transfer(request));
        }
    }

    // ================================================================
    // createInvestmentTransaction
    // ================================================================

    @Nested
    class CreateInvestmentTransaction {

        @Test
        void deveLancarNotFound_quandoContaNaoExistir() {
            UUID accountId = UUID.randomUUID();
            when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createInvestmentTransaction(
                            accountId, BigDecimal.TEN, MovementDirection.OUTFLOW, LocalDate.now(), "obs"));

            assertEquals(HttpStatus.NOT_FOUND, ex.getErrorCode());
        }

        @Test
        void deveLancarForbidden_quandoContaNaoPertencerAoUsuario() {
            User outroUsuario = mock(User.class);
            when(outroUsuario.getId()).thenReturn(UUID.randomUUID().toString()); // ⚠️ faltava isso

            AccountBase account = mock(AccountBase.class);
            when(account.getAccountHolder()).thenReturn(outroUsuario);
            UUID accountId = UUID.randomUUID();
            when(accountRepository.findByIdForUpdate(accountId)).thenReturn(Optional.of(account));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createInvestmentTransaction(
                            accountId, BigDecimal.TEN, MovementDirection.OUTFLOW, LocalDate.now(), "obs"));

            assertEquals(HttpStatus.FORBIDDEN, ex.getErrorCode());
        }


        @Test
        void deveLancarBadRequest_quandoDataForFutura() {
            AccountBase account = mockWalletAccount(BigDecimal.valueOf(1000));
            when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createInvestmentTransaction(
                            account.getId(), BigDecimal.TEN, MovementDirection.OUTFLOW,
                            LocalDate.now().plusDays(1), "obs"));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
        }

        @Test
        void deveLancarBadRequest_quandoAmountForZeroOuNegativo() {
            AccountBase account = mockWalletAccount(BigDecimal.valueOf(1000));
            when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createInvestmentTransaction(
                            account.getId(), BigDecimal.ZERO, MovementDirection.OUTFLOW, LocalDate.now(), "obs"));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("maior que zero"));
        }

        @Test
        void deveLancarBadRequest_quandoOutflowSemSaldoSuficiente() {
            CheckingAccount account = mockCheckingAccount(BigDecimal.valueOf(10), BigDecimal.ZERO);
            when(account.getName()).thenReturn("Conta Invest");
            when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.createInvestmentTransaction(
                            account.getId(), BigDecimal.valueOf(100), MovementDirection.OUTFLOW,
                            LocalDate.now(), "aporte"));

            assertEquals(HttpStatus.BAD_REQUEST, ex.getErrorCode());
        }

        @Test
        void naoDeveValidarSaldo_quandoInflow() {
            AccountBase account = mockWalletAccount(BigDecimal.ZERO);
            when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> service.createInvestmentTransaction(
                    account.getId(), BigDecimal.valueOf(500), MovementDirection.INFLOW,
                    LocalDate.now(), "resgate"));
        }

        @Test
        void deveCriarTransacaoDeInvestimentoComSucesso() {
            CheckingAccount account = mockCheckingAccount(BigDecimal.valueOf(1000), BigDecimal.ZERO);
            when(accountRepository.findByIdForUpdate(account.getId())).thenReturn(Optional.of(account));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> service.createInvestmentTransaction(
                    account.getId(), BigDecimal.valueOf(200), MovementDirection.OUTFLOW,
                    LocalDate.now(), "aporte CDB"));

            verify(transactionRepository).save(any());
        }
    }

    // ================================================================
    // findAllByUser / searchTransactions (delegação simples)
    // ================================================================

    @Nested
    class QueryMethods {

        @Test
        void findAllByUser_deveDelegarParaRepositorioComIdDoUsuario() {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
            org.springframework.data.domain.Page<Transaction> page = org.springframework.data.domain.Page.empty();
            when(transactionRepository.findAllByUserId(userId, pageable)).thenReturn(page);

            var result = service.findAllByUser(pageable);

            assertNotNull(result);
            verify(transactionRepository).findAllByUserId(userId, pageable);
        }

        @Test
        void searchTransactions_deveDelegarTodosOsParametrosParaRepositorio() {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
            org.springframework.data.domain.Page<Transaction> page = org.springframework.data.domain.Page.empty();

            when(user.getId()).thenReturn(userId); // reforça retorno como String? Ver nota abaixo
            when(transactionRepository.searchTransactions(
                    any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(page);

            var result = service.searchTransactions(
                    MovementDirection.OUTFLOW, "nome", UUID.randomUUID(),
                    LocalDate.now().minusDays(10), LocalDate.now(), pageable
            );

            assertNotNull(result);
            verify(transactionRepository).searchTransactions(
                    any(), eq(MovementDirection.OUTFLOW), eq("nome"), any(), any(), any(), eq(pageable)
            );
        }
    }
}
