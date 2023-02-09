package br.com.yawarasolution.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailConfig {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
  private String remetente;

    public void sendEmail(String para, String assunto, String texto) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(remetente);
        message.setTo(para);
        message.setSubject(assunto);
        message.setText(texto);
        javaMailSender.send(message);
    }
}
