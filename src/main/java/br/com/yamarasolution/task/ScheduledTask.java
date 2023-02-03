package br.com.yamarasolution.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.yamarasolution.service.RefreshTokenService;

@Component
@EnableScheduling
public class ScheduledTask {

  @Autowired
  private RefreshTokenService refreshTokenService;

  /**
   * Every Sunday at midnight, delete all refresh tokens that have expired.
   * a tarefa será executada à meia-noite (0 horas, 0 minutos, 0 segundos) todos
   * os domingos.
   */
  @Scheduled(cron = "0 0 0 * * 0", zone = "America/Sao_Paulo")
  public void scheduleTaskWithCronExpression() {
    refreshTokenService.deleteAllRefreshTokensExpired();
  }

}
