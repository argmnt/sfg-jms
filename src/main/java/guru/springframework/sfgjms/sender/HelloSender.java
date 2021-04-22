package guru.springframework.sfgjms.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.sfgjms.config.JmsConfig;
import guru.springframework.sfgjms.model.HelloWorldMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.UUID;

/**
 * Created by jt on 2019-07-17.
 */
@RequiredArgsConstructor
@Component
public class HelloSender {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 2000)
    public void sendMessage(){

        System.out.println("I'm Sending a message");

        HelloWorldMessage message = HelloWorldMessage
                .builder()
                .id(UUID.randomUUID())
                .message("Hello World!")
                .build();

        jmsTemplate.convertAndSend(JmsConfig.MY_QUEUE, message);

        System.out.println("Message Sent!");

    }

    @Scheduled(fixedRate = 2000)
    public void sendMessageAndReceive() throws JMSException {
        HelloWorldMessage helloWorldMessage = HelloWorldMessage.builder()
                .id(UUID.randomUUID())
                .message("Hello")
                .build();

        Message receivedMessage = jmsTemplate.sendAndReceive(JmsConfig.SEND_AND_RECV_QUEUE, session -> {
            Message helloMessage = null;
            try {
                helloMessage = session.createTextMessage(objectMapper.writeValueAsString(helloWorldMessage));
            } catch (JsonProcessingException e) {
                throw new JMSException("Boom");
            }
            helloMessage.setStringProperty("_type", "guru.springframework.sfgjms.model.HelloWorldMessage");
            System.out.println("Sending Hello");
            return helloMessage;
        });
        String parsedReceivedMessageText = receivedMessage.getBody(String.class);
        System.out.println("Received message is " + parsedReceivedMessageText);
    }

}
