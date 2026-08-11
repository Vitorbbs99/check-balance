package com.checkbalance.infrastructure.listener;

import com.checkbalance.domain.service.IngestionService;
import com.checkbalance.infrastructure.dto.TransactionMessageDTO;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionListener.class);
    private final IngestionService ingestionService;

    @SqsListener(value ="${aws.sqs.queue-name}")
    @CircuitBreaker(name = "sqsListenerCircuitBreaker", fallbackMethod = "fallbackProcessarTransacao")
    public void listen(TransactionMessageDTO message) {

        log.info("[THREAD {}] Gravando transação: {}",
                Thread.currentThread().getName(), message.getTransaction().getId());

        // Salva a transação no banco
        ingestionService.processIngestion(message);
    }

  // Método de Fallback invocado quando o circuito está ABERTO ou ao lançar exceção tratada
  public void fallbackProcessarTransacao(TransactionMessageDTO message, Throwable t) {
    // Lança exceção para que o Spring Cloud AWS SQS acione a política de retry/backoff do container
    throw new RuntimeException("Circuit Breaker aberto/falha no processamento da transacao: " + message.getTransaction().getId(), t);
  }

}
