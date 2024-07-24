package com.matt.test.workload_identity.utils;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.identity.WorkloadIdentityCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.spring.messaging.checkpoint.Checkpointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.azure.spring.messaging.AzureHeaders.CHECKPOINTER;
import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class ASBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ASBHandler.class);

    @Value("${spring.cloud.azure.credential.client-id}")
    private String clientId;

    @Value("${spring.cloud.azure.servicebus.namespace}")
    private String namespace;

    @Value("${asb.entity.name}")
    private String topic;


    private TokenCredential credential;

    public String sendASBMsg(String msg) {
        try {
            //Step-1 : prepare the TokenCredential

            LOGGER.info("[Start::ASBHandler::run()::Step-1::prepare the WorkloadIdentityCredentialBuilder]");
            credential = new WorkloadIdentityCredentialBuilder()
                    .clientId(clientId)
                    .build();


            //Step-2 : prepare the ServiceBusSenderClient
            LOGGER.info("[Start::ASBHandler::run()::Step-2::prepare the ServiceBusSenderClient]");
            ServiceBusSenderClient client = new ServiceBusClientBuilder()
                    .credential(namespace, credential)
                    .sender()
                    .topicName(topic)
                    .buildClient();

            //Step-3 : Format the string input
            LOGGER.info("[Start::ASBHandler::run()::Step-3::Format the string input]");
            String input = formatString(msg);

            //Step-4 : prepare the ServiceBusMessage
            LOGGER.info("[Start::ASBHandler::run()::Step-4::prepare the ServiceBusMessage]");
            ServiceBusMessage asbMsg = new ServiceBusMessage(BinaryData.fromBytes(input.getBytes(UTF_8)));

            //Step-5 : send out the message
            LOGGER.info("[Start::ASBHandler::run()::Step-5::send out the message]");
            client.sendMessage(asbMsg);

            //Step-6 : close the client
            LOGGER.info("[Start::ASBHandler::run()::Step-6::close the client]");
            client.close();
        } catch (Exception e) {
            String errMsg = String.format("Error in ASBHandler::run()::", e.getMessage());
            LOGGER.error(errMsg, e);
            return errMsg;
        }
        return "Send success";
    }

    private String formatString(String msg) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return String.format("%s sent at %s", msg, LocalDateTime.now().format(fmt));
    }

    private int i = 0;
    @Bean
    public Consumer<Message<String>> consume() {
        return message -> {
            Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);
            LOGGER.info("New message received: '{}'", message.getPayload());
            checkpointer.success()
                    .doOnSuccess(s -> LOGGER.info("Message '{}' successfully checkpointed", message.getPayload()))
                    .doOnError(e -> LOGGER.error("Error found", e))
                    .block();
        };
    }

    @Bean
    public Sinks.Many<Message<String>> many() {
        return Sinks.many().unicast().onBackpressureBuffer();
    }

    @Bean
    public Supplier<Flux<Message<String>>> supply(Sinks.Many<Message<String>> many) {
        return () -> many.asFlux()
                .doOnNext(m -> LOGGER.info("Manually sending message {}", m))
                .doOnError(t -> LOGGER.error("Error encountered", t));
    }
}
