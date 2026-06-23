package com.checkbalance.infrastructure.listener;

import com.checkbalance.domain.service.IngestionService;
import com.checkbalance.infrastructure.dto.TransactionMessageDTO;
import io.awspring.cloud.sqs.annotation.SqsListener;
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
    public void listen(TransactionMessageDTO message) {

        log.info("[THREAD {}] Gravando transação: {}",
                Thread.currentThread().getName(), message.getTransaction().getId());

        // Salva a transação no banco
        ingestionService.processIngestion(message);
    }

}
